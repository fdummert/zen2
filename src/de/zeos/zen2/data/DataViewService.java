package de.zeos.zen2.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.zeos.db.DBAccessor;
import de.zeos.db.Ref;
import de.zeos.script.ScriptEngineCreator;
import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.DataView.CommandMode;
import de.zeos.zen2.app.model.Field;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.db.InternalDBAccessor;

@Service("dv")
@Component
public class DataViewService {

    @Session
    private ServerSession serverSession;

    @Inject
    private ScriptEngineCreator engineCreator;

    @Inject
    private ApplicationRegistry appRegistry;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Listener("/service/**")
    protected void receiveOnDsChannel(ServerSession remote, Mutable message) {
        String channel = message.getChannel();
        String[] parts = channel.substring(1).split("/", 5);
        if (parts.length >= 5 && parts[2].equals("dv") && parts[3].equals("req")) {
            String app = parts[1];
            String view = parts[4];
            String scope = null;
            if (parts.length > 5)
                scope = parts[5];
            process(remote, message, app, view, scope);
        }
    }

    private void process(ServerSession remote, Mutable message, String app, String dataView, String scope) {
        InternalDBAccessor internalAccessor = this.appRegistry.getInternalDBAccessor(app);
        DataView view = internalAccessor.getDataView(dataView);

        DBAccessor accessor = this.appRegistry.getDBAccessor(app);
        Map<String, Object> data = message.getDataAsMap();
        CommandMode mode = CommandMode.valueOf((String) data.get("mode"));

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("requestId", data.get("requestId"));
        try {
            if (!view.getAllowedModes().contains(mode))
                throw new IllegalStateException("errDataViewModeNotAllowed");

            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) data.get("criteria");

            Map<String, Field> fields = new HashMap<>();
            for (Field f : view.getEntity().getFields()) {
                boolean add = true;
                if (view.getFields() != null && !view.getFields().isEmpty()) {
                    boolean found = false;
                    for (FieldView fv : view.getFields()) {
                        if (f.getName().equals(fv.getName())) {
                            if (fv.getMandatory() != null)
                                f.setMandatory(fv.getMandatory());
                            if (fv.getReadOnly() != null)
                                f.setReadOnly(fv.getReadOnly());
                            if (fv.getLazy() != null)
                                f.getType().setLazy(fv.getLazy());
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        add = false;
                }
                if (add) {
                    fields.put(f.getName(), f);
                }
            }

            String entity = view.getEntity().getId();
            Object result = null;
            switch (mode) {
            case CREATE:
                filterCriteria(criteria, fields, true, true);
                processRefs(accessor, criteria, fields);
                result = accessor.insert(criteria, entity);
                break;
            case READ:
                filterCriteria(criteria, fields, false, false);
                Integer pageFrom = (Integer) data.get("pageFrom");
                Integer pageTo = (Integer) data.get("pageTo");
                String[] sorts = (String[]) data.get("sorts");
                Integer count = null;
                List<Map<String, Object>> rows = accessor.select(criteria, pageFrom, pageTo, sorts, entity);
                if (pageFrom != null || pageTo != null) {
                    count = new Long(accessor.count(criteria, entity)).intValue();
                    res.put("pageFrom", pageFrom);
                    res.put("pageTo", pageFrom + (rows == null ? 0 : rows.size() - 1));
                    res.put("count", count);
                }
                result = rows;
                break;
            case UPDATE:
                filterCriteria(criteria, fields, true, true);
                processRefs(accessor, criteria, fields);
                result = accessor.update(criteria, entity);
                break;
            case DELETE:
                filterCriteria(criteria, fields, false, false);
                result = accessor.delete(criteria, entity);
                break;
            }
            res.put("result", result);
        } catch (IllegalStateException e) {
            res.put("error", e.getMessage());
        } catch (ValidationException e) {
            res.put("validationErrors", Collections.singletonMap(e.getProperty(), e.getMessage()));
        } catch (Exception e) {
            res.put("error", "errDataViewGeneral");
            logger.error("Exception while accessing database", e);
        }

        String responseChannel = "/service/" + app + "/dv/res/" + dataView;
        if (scope != null)
            responseChannel += "/" + scope;
        remote.deliver(this.serverSession, responseChannel, res, null);
    }

    private void processRefs(DBAccessor accessor, Map<String, Object> criteria, Map<String, Field> fields) {
        for (Ref ref : findRefs(criteria, fields)) {
            criteria.put(ref.getProperty(), ref);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Ref> findRefs(Map<String, Object> criteria, Map<String, Field> fields) {
        ArrayList<Ref> refs = new ArrayList<>();
        for (String c : criteria.keySet()) {
            Field f = fields.get(c);
            if (f.getType().getDataClass() == DataClass.ENTITY && !f.getType().getRefEntity().isEmbeddable()) {
                Object refObj = criteria.get(c);
                Ref ref = new Ref(c, f.getType().getRefEntity().getId());
                if (refObj instanceof Map) {
                    ref.setRefObj((Map<String, Object>) refObj);
                } else {
                    ref.setId(refObj);
                }
                ref.setLazy(f.getType().isLazy());
                refs.add(ref);
            }
        }
        return refs;
    }

    private void filterCriteria(Map<String, Object> criteria, Map<String, Field> fields, boolean removeRO, boolean checkMandatory) throws ValidationException {
        for (Iterator<String> iter = criteria.keySet().iterator(); iter.hasNext();) {
            String crit = iter.next();
            Field f = fields.get(crit);
            if (f == null || (removeRO && !f.isPk() && f.isReadOnly())) {
                iter.remove();
            } else if (checkMandatory && f.isMandatory() && criteria.get(crit) == null)
                throw new ValidationException(crit, "errMandatory");
        }
    }
}

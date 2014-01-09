package de.zeos.ctrl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Field;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.security.AuthSecurityPolicy;
import de.zeos.zen2.security.Authorization;

@Controller
@RequestMapping("/dv")
public class DataViewController {

    class EntityInfo {
        private Entity entity;
        private List<Field> fields = new ArrayList<>();

        public EntityInfo(Entity entity, List<FieldView> fields) {
            this.entity = entity;
            if (fields == null || fields.isEmpty()) {
                this.fields.addAll(entity.getFields());
            } else {
                for (FieldView fv : fields) {
                    Field field = null;
                    for (Field f : entity.getFields()) {
                        if (f.getName().equals(fv.getName())) {
                            field = f;
                            break;
                        }
                    }
                    if (fv.getMandatory() != null)
                        field.setMandatory(fv.getMandatory());
                    if (fv.getReadOnly() != null)
                        field.setReadOnly(fv.getReadOnly());
                    this.fields.add(field);
                }
            }
        }

        public String getId() {
            return this.entity.getId();
        }

        public List<Field> getFields() {
            return this.fields;
        }
    }

    class DataViewInfo {
        private DataView dataView;
        private EntityInfo entity;

        public DataViewInfo(DataView dataView) {
            this.dataView = dataView;
            this.entity = new EntityInfo(dataView.getEntity(), dataView.getFields());
        }

        public String getId() {
            return this.dataView.getId();
        }

        public String getScope() {
            return this.dataView.getScope();
        }

        public boolean isPushable() {
            return this.dataView.isPushable();
        }

        public List<String> getPushScopes() {
            return this.dataView.getPushScopes();
        }

        public EntityInfo getEntity() {
            return this.entity;
        }
    }

    @Inject
    private BayeuxServer bayeuxServer;
    @Inject
    private ApplicationRegistry appRegistry;

    @RequestMapping(value = "/{app}/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<DataViewInfo> getDataViews(@PathVariable String app, @PathVariable String id) throws JsonProcessingException {
        ServerSession session = this.bayeuxServer.getSession(id);
        if (session == null)
            throw new IllegalStateException("Invalid session");
        String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
        if (!sessionApp.equals(app))
            throw new IllegalStateException("Invalid application");
        Authorization auth = (Authorization) session.getAttribute(AuthSecurityPolicy.AUTH_KEY);
        List<DataViewInfo> views = new ArrayList<>();
        InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
        for (String view : auth.getDataViews()) {
            views.add(new DataViewInfo(accessor.getDataView(view)));
        }
        return views;
    }
}

package de.zeos.ds;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.util.ReflectionUtils;

import de.zeos.cometd.security.Authorization;

public abstract class AbstractDataSource<D, REQ extends DataSourceRequest<D>> {
    private Authorization auth;
    private MongoOperations ops;
    private EnumSet<OperationType> allowedOperations = EnumSet.noneOf(OperationType.class);

    private List<D> entries;

    protected AbstractDataSource(Authorization auth, MongoOperations ops, OperationType... allowedOperations) {
        this.auth = auth;
        this.ops = ops;
        this.allowedOperations.addAll(Arrays.asList(allowedOperations));
    }

    public Authorization getAuthorization() {
        return this.auth;
    }

    public MongoOperations getOperations() {
        return this.ops;
    }

    public EnumSet<OperationType> getAllowedOperations() {
        return this.allowedOperations;
    }

    public final void fetch(REQ request, DataSourceResponse response) throws DataSourceException {
        int rowCount = getRowCount(request);
        List<?> rows = getRows(request);
        response.setStartRow(request.getStartRow());
        response.setEndRow(request.getEndRow() == null ? null : (request.getStartRow() + (rows == null ? 0 : rows.size())));
        response.setTotalRows(rowCount);
        response.setData(rows);
    }

    public final void add(REQ request, DataSourceResponse response) throws DataSourceException {
        response.setData(add(request));
    }

    public final void update(REQ request, DataSourceResponse response) throws DataSourceException {
        response.setData(update(request));
    }

    public final void remove(REQ request, DataSourceResponse response) throws DataSourceException {
        response.setData(remove(request));
    }

    protected abstract Class<D> getDataClass();

    protected int getRowCount(REQ req) {
        this.entries = getOperations().findAll(getDataClass());
        return this.entries.size();
    }

    protected List<D> getRows(REQ req) {
        return this.entries;
    }

    protected D add(REQ req) throws DataSourceException {
        D data = req.getData();
        D existing = getOperations().findById(getId(data), getDataClass());
        if (existing != null)
            throw new DataSourceValidationException("id", "errExists");
        getOperations().save(data);
        return data;
    }

    protected String getId(D data) {
        Method m = BeanUtils.getPropertyDescriptor(getDataClass(), "id").getReadMethod();
        return (String) ReflectionUtils.invokeMethod(m, data);
    }

    protected D update(REQ req) throws DataSourceException {
        getOperations().save(req.getData());
        return req.getData();
    }

    protected D remove(REQ req) throws DataSourceException {
        getOperations().remove(req.getData());
        return req.getData();
    }
}

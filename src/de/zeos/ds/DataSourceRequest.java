package de.zeos.ds;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSourceRequest<D> {

    private String requestId;
    @JsonProperty("_operationType")
    private OperationType operationType;
    @JsonProperty("_dataSource")
    private String dataSource;
    @JsonProperty("_startRow")
    private Integer startRow;
    @JsonProperty("_endRow")
    private Integer endRow;
    @JsonProperty("_sortBy")
    private SortSpecifier[] sortBy;
    private D data;

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public Integer getStartRow() {
        return this.startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public Integer getEndRow() {
        return this.endRow;
    }

    public void setEndRow(Integer endRow) {
        this.endRow = endRow;
    }

    public SortSpecifier[] getSortBy() {
        return this.sortBy;
    }

    public void setSortBy(SortSpecifier[] sortBy) {
        this.sortBy = sortBy;
    }

    public D getData() {
        return this.data;
    }

    public void setData(D data) {
        this.data = data;
    }
}

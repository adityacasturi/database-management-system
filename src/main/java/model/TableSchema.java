package model;

import java.util.List;

public class TableSchema {
    private String tableName;
    private List<ColumnSchema> columns;

    public TableSchema(List<ColumnSchema> columns, String tableName) {
        this.columns = columns;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnSchema> getColumns() {
        return columns;
    }
}

package model;

public class SimpleQuery {
    private final String databaseName;
    private final TableSchema tableSchema;
    private final String columnName;
    private final String value;

    public SimpleQuery(String dbName, TableSchema tableSchema, String columnName, String value) {
        this.databaseName = dbName;
        this.tableSchema = tableSchema;
        this.columnName = columnName;
        this.value = value;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getValue() {
        return value;
    }
}

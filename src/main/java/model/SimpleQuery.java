package model;

public class SimpleQuery {
    private String databaseName;
    private String tableName;
    private String columnName;
    private String value;

    public SimpleQuery(String dbName, String tableName, String columnName, String value) {
        this.databaseName = dbName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.value = value;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SimpleQuery{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

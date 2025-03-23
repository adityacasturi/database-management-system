package model;

public class SimpleQuery {
    private String tableName;
    private String columnName;
    private String databaseName;
    private String value;

    public SimpleQuery(String tableName, String columnName, String value) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.value = value;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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

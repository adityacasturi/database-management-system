package model;

import java.util.List;

public class QueryResult {
    private List<String[]> rows;
    private String databaseName;
    private String tableName;

    public QueryResult(List<String[]> rows, String databaseName, String tableName) {
        this.rows = rows;
        this.databaseName = databaseName;
        this.tableName = tableName;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public void setRows(List<String[]> rows) {
        this.rows = rows;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}

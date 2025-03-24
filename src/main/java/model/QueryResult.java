package model;

import java.util.List;

public class QueryResult {
    private final String databaseName;
    private final String tableName;
    private final List<String[]> rows;

    public QueryResult(String databaseName, String tableName, List<String[]> rows) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.rows = rows;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String[]> getRows() {
        return rows;
    }
}

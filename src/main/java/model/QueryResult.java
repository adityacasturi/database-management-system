package model;

public class QueryResult {
    private final String databaseName;
    private final String tableName;
    private final int rowsFound;

    public QueryResult(String databaseName, String tableName, int rowsFound) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.rowsFound = rowsFound;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public int getRowsFound() {
        return rowsFound;
    }
}

package model;

public class QueryResult {
    private final String databaseName;
    private final String tableName;
    private final int numRows;

    public QueryResult(String databaseName, String tableName, int numRows) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.numRows = numRows;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public int getNumRows() {
        return numRows;
    }
}

package model;

public class QueryResult {
    private final int numRows;

    public QueryResult(int numRows) {
        this.numRows = numRows;
    }

    public int getNumRows() {
        return numRows;
    }
}

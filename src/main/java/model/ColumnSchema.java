package model;

public class ColumnSchema {
    private final String columnName;
    private final String columnType;
    private final int numBytes;

    public ColumnSchema(String columnName, String columnType, int numBytes) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.numBytes = numBytes;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public int getNumBytes() {
        return numBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColumnSchema)) {
            return false;
        }
        return this.columnName.equals(((ColumnSchema) o).columnName);
    }
}

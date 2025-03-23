package model;

public class ColumnSchema {
    private String columnName;
    private String columnType;

    public ColumnSchema(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColumnSchema)) {
            return false;
        }
        return this.columnName.equals(((ColumnSchema) o).columnName);
    }
}

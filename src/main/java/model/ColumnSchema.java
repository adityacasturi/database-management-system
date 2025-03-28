package model;

import java.util.Objects;

public abstract class ColumnSchema {
    private final String columnName;
    protected String columnType;
    private final boolean indexed;

    ColumnSchema(String columnName, boolean indexed) {
        this.columnName = columnName;
        this.indexed = indexed;
        this.columnType = getColumnType();
    }

    public abstract int getNumBytes();

    public abstract String getColumnType();

    public String getColumnName() {
        return columnName;
    }

    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ColumnSchema that = (ColumnSchema) o;
        return Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(columnName);
    }
}

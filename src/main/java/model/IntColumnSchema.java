package model;

public class IntColumnSchema extends ColumnSchema {
    public IntColumnSchema(String columnName, boolean indexed) {
        super(columnName, indexed);
    }

    @Override
    public int getNumBytes() {
        return 4;
    }

    @Override
    public COLUMN_TYPES getColumnType() {
        return COLUMN_TYPES.INT_TYPE;
    }
}

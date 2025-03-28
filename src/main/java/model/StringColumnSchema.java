package model;

public class StringColumnSchema extends ColumnSchema {
    private final int maxLength;

    public StringColumnSchema(String columnName, int maxLength, boolean indexed) {
        super(columnName, indexed);
        this.maxLength = maxLength;
    }

    @Override
    public int getNumBytes() {
        return maxLength;
    }

    @Override
    public String getColumnType() {
        return "String";
    }
}

import model.ColumnSchema;
import model.QueryResult;
import model.SimpleQuery;
import model.TableSchema;

public class QueryHandler {
    public static QueryResult handle(String dbName, String tableName, String columnName, String columnValue) throws Exception {
        TableSchema tableSchema;
        try {
            tableSchema = DatabaseExplorer.getTableSchema(dbName, tableName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Table [" + tableName + "] does not exist");
        }

        boolean columnExists = false;
        for (ColumnSchema colSchema : tableSchema.getColumns()) {
            if (colSchema.getColumnName().equals(columnName)) {
                columnExists = true;

                if (!colSchema.isIndexed()) {
                    throw new IllegalArgumentException("Column [" + columnName + "] is not indexed in table [" + tableName + "]");
                }

                break;
            }
        }

        if (!columnExists) {
            throw new IllegalArgumentException("Column [" + columnName + "] does not exist in table [" + tableName + "]");
        }

        SimpleQuery sq = new SimpleQuery(dbName, tableSchema, columnName, columnValue);
        int rows = QueryExecutor.execute(sq);

        return new QueryResult(rows);
    }
}

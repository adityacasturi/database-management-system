import model.QueryResult;
import model.SimpleQuery;

public class QueryHandler {
    public static QueryResult handle(String dbName, String query) throws Exception {
        SimpleQuery sq = QueryParser.parse(dbName, query);
        return new QueryResult(dbName, sq.getTableName(), QueryExecutor.execute(sq));
    }
}

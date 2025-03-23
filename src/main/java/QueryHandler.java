import model.QueryResult;
import model.SimpleQuery;

import java.util.List;

public class QueryHandler {
    public static QueryResult handle(String query, String dbName) throws Exception {
        SimpleQuery sq = QueryParser.parse(query);
        sq.setDatabaseName(dbName);

        return new QueryResult(QueryExecutor.execute(sq), dbName, sq.getTableName());
    }
}

import model.SimpleQuery;

import java.util.List;

public class QueryHandler {
    public static List<String[]> handle(String query, String dbName) throws Exception {
        SimpleQuery sq = QueryParser.parse(query);

        return QueryExecutor.execute(sq, dbName);
    }
}

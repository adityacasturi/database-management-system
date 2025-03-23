import model.SimpleQuery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    public static SimpleQuery parse(String dbName, String query) throws Exception {
        Pattern pattern = Pattern.compile("select \\* from ([0-9a-zA-Z]*) where ([a-zA-Z0-9]*) = ([a-zA-Z0-9]*)");
        Matcher matcher = pattern.matcher(query);
        if (!matcher.matches()) {
            throw new Exception("Invalid query.");
        }

        String tableName = matcher.group(1);
        String columnName = matcher.group(2);
        String value = matcher.group(3);

        return new SimpleQuery(dbName, tableName, columnName, value);
    }
}

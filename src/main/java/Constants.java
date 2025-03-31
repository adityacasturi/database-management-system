import java.io.File;
import java.util.regex.Pattern;

public final class Constants {
    public static final String STORAGE_LOC = System.getProperty("user.home") + File.separator + "dbs";
    public static final String SCHEMA_FILE_LOC = STORAGE_LOC + File.separator + "%s" + File.separator + "%s.schema";;
    public static final String SHARD_LOC = STORAGE_LOC + File.separator + "%s" + File.separator + "%s_%s.data";
    public static final String INDEX_FILE_LOC = STORAGE_LOC + File.separator + "%s" + File.separator + "%s_%s_%s.index";

    public static final Pattern USE_DATABASE_COMMAND_PATTERN = Pattern.compile("^\\s*use\\s+database\\s+(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
    public static final Pattern SHOW_TABLES_PATTERN = Pattern.compile("^\\s*show\\s+tables\\s*$", Pattern.CASE_INSENSITIVE);
    public static final Pattern SHOW_DATABASES_PATTERN = Pattern.compile("^\\s*show\\s+databases\\s*$", Pattern.CASE_INSENSITIVE);
    public static final Pattern COUNT_QUERY_PATTERN = Pattern.compile("^\\s*select\\s+count\\s*\\(\\s*\\*\\s*\\)\\s+from\\s+(\\S+)\\s+where\\s+(\\S+)\\s*=\\s*(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
}

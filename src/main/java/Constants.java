import java.io.File;

public final class Constants {
    public static final String STORAGE_LOC = System.getProperty("user.home") + File.separator + "dbs";
    public static final String SCHEMA_FILE_LOC = STORAGE_LOC + File.separator + "%s" + File.separator + "%s.schema";;
    public static final String SHARD_LOC = STORAGE_LOC + File.separator + "%s" + File.separator + "%s_%s.data";
    public static final String INDEX_FILE_LOC = STORAGE_LOC + File.separator + "%s" + File.separator + "%s_%s_%s.index";
}

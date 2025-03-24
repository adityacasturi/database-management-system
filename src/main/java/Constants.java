import java.io.File;

public final class Constants {
    public static final String STORAGE_LOC = System.getProperty("user.home") + File.separator + "dbs";
    public static final String DB_LOC = STORAGE_LOC + File.separator + "{0}";
    public static final String TABLE_LOC = STORAGE_LOC + File.separator + "{0}";
    public static final int LINES_PER_FILE = 1_000_000;
}

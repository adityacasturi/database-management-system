import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DatabaseExplorer {
    private final String storageDirLoc;
    private final String dbsDir;

    // Constructor sets the location of the "dbs" folder.
    public DatabaseExplorer(String storageDirLoc) {
        this.storageDirLoc = storageDirLoc;
        this.dbsDir = storageDirLoc + File.separator + "dbs";
    }

    // List all databases by listing directories inside the "dbs" folder.
    public List<String> listDatabases() {
        List<String> databases = new ArrayList<>();
        File dbsFolder = new File(dbsDir);
        if (dbsFolder.exists() && dbsFolder.isDirectory()) {
            File[] files = dbsFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        databases.add(file.getName());
                    }
                }
            }
        }
        return databases;
    }

    // List all tables in a given database.
    // Tables are determined by files ending with "_data.csv".
    public List<String> listTables(String dbName) {
        List<String> tables = new ArrayList<>();
        File dbFolder = new File(dbsDir + File.separator + dbName);
        if (dbFolder.exists() && dbFolder.isDirectory()) {
            File[] files = dbFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".csv")) {
                        String tableName = fileName.substring(0, fileName.length() - ".csv".length());
                        tables.add(tableName);
                    }
                }
            }
        }
        return tables;
    }

    // Read and return the schema for a given table.
    public String viewSchema(String dbName, String tableName) {
        String schemaFilePath = dbsDir + File.separator + dbName + File.separator + tableName + "_schema.csv";
        return readFileContents(schemaFilePath);
    }

    // Read and return the data for a given table.
    public String viewData(String dbName, String tableName) {
        String dataFilePath = dbsDir + File.separator + dbName + File.separator + tableName + "_data.csv";
        return readFileContents(dataFilePath);
    }

    // A stub for query execution.
    // Currently, it simply prints out the executed query.
    public void queryDatabase(String dbName, String query) {
        System.out.println("Query executed on database '" + dbName + "': " + query);
        // Extend this method to actually parse and execute queries if needed.
    }

    // Utility method to read the content of a file into a String.
    private String readFileContents(String filePath) {
        StringBuilder content = new StringBuilder();
        File file = new File(filePath);
        if (!file.exists()) {
            return "File not found: " + filePath;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            return "Error reading file: " + filePath;
        }
        return content.toString();
    }
}
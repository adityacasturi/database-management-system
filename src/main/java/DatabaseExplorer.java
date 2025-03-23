import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.vandermeer.asciitable.AsciiTable;

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
        String schemaFilePath = dbsDir + File.separator + dbName + File.separator + tableName + ".schema";
        File schemaFile = new File(schemaFilePath);

        if (!schemaFile.exists()) {
            return "Schema file does not exist: " + schemaFilePath;
        }

        List<String[]> schemaEntries = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(schemaFile))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (row.length >= 2) {
                    schemaEntries.add(new String[]{ row[0].trim(), row[1].trim() });
                }
            }
        } catch (IOException | CsvValidationException e) {
            return "Error reading schema file: " + e.getMessage();
        }

        if (schemaEntries.isEmpty()) {
            return "No schema information found in file.";
        }

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Column Name", "Data Type");
        at.addRule();

        for (String[] entry : schemaEntries) {
            at.addRow(entry[0], entry[1]);
            at.addRule();
        }

        return at.render();
    }

    // Read and return the data for a given table.
    public String viewTableData(String dbName, String tableName) {
        String dataFilePath = dbsDir + File.separator + dbName + File.separator + tableName + ".csv";
        File dataFile = new File(dataFilePath);
        if (!dataFile.exists()) {
            return "Data file does not exist: " + dataFilePath;
        }

        List<String[]> rows = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(dataFile))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rows.add(row);
            }
        } catch (IOException | CsvValidationException e) {
            return "Error reading data file: " + e.getMessage();
        }

        if (rows.isEmpty()) {
            return "No data found in file.";
        }

        AsciiTable at = new AsciiTable();
        String[] header = rows.getFirst();
        at.addRule();
        at.addRow((Object[]) header);
        at.addRule();

        // max 20 rows
        int maxRowsToPrint = Math.min(rows.size(), 20);
        for (int i = 1; i < maxRowsToPrint; i++) {
            String[] row = rows.get(i);
            at.addRow((Object[]) row);
            at.addRule();
        }

        return at.render();
    }

    // A stub for query execution.
    // Currently, it simply prints out the executed query.
    public void queryDatabase(String dbName, String query) {
        System.out.println("Query executed on database '" + dbName + "': " + query);
        // Extend this method to actually parse and execute queries if needed.
    }
}
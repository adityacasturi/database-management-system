import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import model.ColumnSchema;
import model.TableSchema;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DatabaseExplorer {
    // List all databases by listing directories inside the "dbs" folder.
    public static List<String> listDatabases() {
        List<String> databases = new ArrayList<>();
        File dbsFolder = new File(Constants.STORAGE_LOC);
        if (dbsFolder.exists() && dbsFolder.isDirectory()) {
            File[] files = dbsFolder.listFiles();
            if (files == null) {
                return databases;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    databases.add(file.getName());
                }
            }
        }
        return databases;
    }

    public static List<String> listTables(String dbName) {
        List<String> tables = new ArrayList<>();
        File dbFolder = new File(Constants.STORAGE_LOC + File.separator + dbName);
        if (dbFolder.exists() && dbFolder.isDirectory()) {
            File[] files = dbFolder.listFiles();
            if (files == null) {
                return tables;
            }
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith("_0.csv")) {
                    tables.add(fileName.split("_")[0]);
                }
            }
        }
        return tables;
    }

    // Read and return the schema for a given table.
    public static TableSchema getTableSchema(String dbName, String tableName) throws Exception {
        String schemaFilePath = Constants.STORAGE_LOC + File.separator + dbName + File.separator + tableName + ".schema";
        File schemaFile = new File(schemaFilePath);

        if (!schemaFile.exists()) {
            throw new Exception("Schema file does not exist");
        }

        List<String[]> schemaEntries = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(schemaFile))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (row.length >= 2) {
                    schemaEntries.add(new String[]{row[0].trim(), row[1].trim(), row[2].trim()});
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new Exception("Error parsing schema file.", e);
        }

        if (schemaEntries.isEmpty()) {
            throw new Exception("No schema information found in file.");
        }

        List<ColumnSchema> columns = new ArrayList<>();
        for (String[] row : schemaEntries) {
            columns.add(new ColumnSchema(row[0], row[1], Integer.parseInt(row[2])));
        }
        return new TableSchema(columns, tableName);
    }

    // Read and return the data for a given table.
    public static List<String[]> getTableData(String dbName, String tableName) throws Exception {
        String dataFilePath = Constants.STORAGE_LOC + File.separator + dbName + File.separator + tableName + ".csv";
        File dataFile = new File(dataFilePath);
        if (!dataFile.exists()) {
            throw new Exception("Data file does not exist");
        }

        List<String[]> rows = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(dataFile))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rows.add(row);
            }
        } catch (IOException | CsvValidationException e) {
            throw new Exception("Error parsing data file.", e);
        }

        return rows;
    }
}
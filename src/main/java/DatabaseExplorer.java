import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import model.ColumnSchema;
import model.ColumnSchemaTypeAdapter;
import model.TableSchema;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class DatabaseExplorer {
    // List all databases by listing directories inside the "dbs" folder.
    public static List<String> getDatabases() {
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

    public static List<String> getTables(String dbName) {
        List<String> tables = new ArrayList<>();
        File dbFolder = new File(Constants.STORAGE_LOC + File.separator + dbName);
        if (dbFolder.exists() && dbFolder.isDirectory()) {
            File[] files = dbFolder.listFiles();
            if (files == null) {
                return tables;
            }
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith("_0.data")) {
                    tables.add(fileName.split("_")[0]);
                }
            }
        }
        return tables;
    }

    // Read and return the schema for a given table.
    public static TableSchema getTableSchema(String dbName, String tableName) throws Exception {
        String schemaFilePath = String.format(Constants.SCHEMA_FILE_LOC, dbName, tableName);
        File schemaFile = new File(schemaFilePath);

        if (!schemaFile.exists()) {
            throw new Exception("Schema file does not exist");
        }

        String schemaFileJson = new String(Files.readAllBytes(schemaFile.toPath()));
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ColumnSchema.class, new ColumnSchemaTypeAdapter())
                .create();

        return gson.fromJson(schemaFileJson, TableSchema.class);
    }

    // Read and return the data for a given table.
    public static List<String[]> getTableData(String dbName, String tableName) throws Exception {
        String dataFilePath = Constants.STORAGE_LOC + File.separator + dbName + File.separator + tableName + ".data";
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
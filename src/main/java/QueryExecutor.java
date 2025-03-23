import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import model.ColumnSchema;
import model.SimpleQuery;
import model.TableSchema;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryExecutor {
    public static List<String[]> execute(SimpleQuery query) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<List<String[]>>> futures = new ArrayList<>();

        String dataFilesPath = StorageLocations.STORAGE_LOC + File.separator + query.getDatabaseName();

        List<String[]> results = new ArrayList<>();

        TableSchema tableSchema;
        try {
            tableSchema = DatabaseExplorer.getTableSchema(query.getDatabaseName(), query.getTableName());
        } catch (Exception e) {
            throw new Exception("Specified table " + query.getTableName() + "does not exist in " +
                    "database " + query.getDatabaseName());
        }

        final int colIndex = getColIndex(query, tableSchema);

        int suffix = 0;
        while (true) {
            File dataFile = new File(dataFilesPath + File.separator + query.getTableName() + "_" + suffix + ".csv");
            if (!dataFile.exists()) {
                break;
            }

            futures.add(executor.submit(() -> {
                List<String[]> localResults = new ArrayList<>();

                try (CSVReader csvReader = new CSVReader(new FileReader(dataFile))) {
                    String[] row;
                    while ((row = csvReader.readNext()) != null) {
                        if (row[colIndex].equals(query.getValue())) {
                            localResults.add(row);
                        }
                    }
                } catch (IOException | CsvValidationException e) {
                    throw new Exception("Error parsing data file.", e);
                }

                return localResults;
            }));

            suffix++;
        }

        for (Future<List<String[]>> future : futures) {
            try {
                results.addAll(future.get());
            } catch (Exception e) {
                throw new Error("Error while executing query", e);
            }
        }

        executor.shutdown();
        return results;
    }

    private static int getColIndex(SimpleQuery query, TableSchema tableSchema) throws Exception {
        int colIndex = 0;
        boolean colFound = false;
        for (ColumnSchema columnSchema : tableSchema.getColumns()) {
            if (columnSchema.getColumnName().equals(query.getColumnName())) {
                colFound = true;
                break;
            }
            colIndex++;
        }

        if (!colFound) {
            throw new Exception("Specified column " + query.getColumnName() + " does not exist in" +
                    " table " + query.getTableName());
        }
        return colIndex;
    }
}

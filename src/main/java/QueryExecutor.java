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
    public static int execute(SimpleQuery query) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Integer>> futures = new ArrayList<>();

        String dataFilesPath = StorageLocations.STORAGE_LOC + File.separator + query.getDatabaseName();

        TableSchema tableSchema;
        try {
            tableSchema = DatabaseExplorer.getTableSchema(query.getDatabaseName(), query.getTableName());
        } catch (Exception e) {
            throw new Exception("Specified table " + query.getTableName() + "does not exist in " +
                    "database " + query.getDatabaseName());
        }

        final int colIndex = getColIndex(tableSchema, query.getColumnName());
        int shardIdx = 0;
        while (true) {
            File dataFile = new File(dataFilesPath + File.separator + query.getTableName() + "_" + shardIdx + ".csv");
            if (!dataFile.exists()) {
                break;
            }

            futures.add(executor.submit(() -> {
                int rowsFound = 0;

                try (CSVReader csvReader = new CSVReader(new FileReader(dataFile))) {
                    String[] row;
                    while ((row = csvReader.readNext()) != null) {
                        if (row[colIndex].equals(query.getValue())) {
                            rowsFound++;
                        }
                    }
                } catch (IOException | CsvValidationException e) {
                    throw new Exception("Error parsing data file.", e);
                }

                return rowsFound;
            }));

            shardIdx++;
        }

        int totalRowsFound = 0;
        for (Future<Integer> future : futures) {
            try {
                totalRowsFound += future.get();
            } catch (Exception e) {
                throw new Error("Error while executing query", e);
            }
        }

        executor.shutdown();

        return totalRowsFound;
    }

    private static int getColIndex(TableSchema tableSchema, String columnName) throws Exception {
        int colIndex = 0;
        for (ColumnSchema columnSchema : tableSchema.getColumns()) {
            if (columnSchema.getColumnName().equals(columnName)) {
                return colIndex;
            }
            colIndex++;
        }

        throw new Exception("Specified column " + columnName + " does not exist in" +
                " table " + tableSchema.getTableName());
    }
}

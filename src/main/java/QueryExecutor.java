import model.ColumnSchema;
import model.SimpleQuery;
import model.TableSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryExecutor {
    public static List<String[]> execute(SimpleQuery query, String dbName) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<List<String[]>>> futures = new ArrayList<>();

        String dataFilesPath = StorageLocations.STORAGE_LOC + File.separator + dbName;

        List<String[]> results = new ArrayList<>();

        int suffix = 0;
        while (true) {
            File dataFile = new File(dataFilesPath + File.separator + query.getTableName() + "_" + suffix + ".csv");
            if (!dataFile.exists()) {
                break;
            }

            TableSchema tableSchema = DatabaseExplorer.getTableSchema(dbName, query.getTableName());
            int i = 0;
            for (ColumnSchema columnSchema : tableSchema.getColumns()) {
                if (columnSchema.getColumnName().equals(query.getColumnName())) {
                    break;
                }
                i++;
            }

            final int colIndex = i;
            final int currSuffix = suffix;

            futures.add(executor.submit(() -> {
                List<String[]> localResults = new ArrayList<>();
                for (String[] row : DatabaseExplorer.getTableData(dbName, query.getTableName() + "_" + currSuffix)) {
                    if (row[colIndex].equals(query.getValue())) {
                        localResults.add(row);
                    }
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
}

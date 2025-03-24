import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import model.ColumnSchema;
import model.SimpleQuery;
import model.TableSchema;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryExecutor {
    public static List<String[]> execute(SimpleQuery query) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<List<String[]>>> futures = new ArrayList<>();

        TableSchema tableSchema;
        try {
            tableSchema = DatabaseExplorer.getTableSchema(query.getDatabaseName(), query.getTableName());
        } catch (Exception e) {
            throw new Exception("Specified table " + query.getTableName() + " does not exist in " +
                    "database " + query.getDatabaseName());
        }

        int byteCount = 0;
        for (ColumnSchema colSchema : tableSchema.getColumns()) {
            byteCount += colSchema.getNumBytes();
        }
        final int bytesPerRow = byteCount;

        Map<Integer, List<Long>> pools = getIndexPools(query);
        for (int dataFileSuffix : pools.keySet()) {
            futures.add(executor.submit(() -> {
                File dataFile = new File(Constants.STORAGE_LOC + File.separator +
                        query.getDatabaseName() + File.separator + query.getTableName() + "_" +
                        dataFileSuffix + ".csv");

                List<Long> indexes = pools.get(dataFileSuffix);
                return getRowsFromIndexes(indexes, dataFile, bytesPerRow,
                        dataFileSuffix * Constants.LINES_PER_FILE, tableSchema);
            }));
        }

        List<String[]> rows = new ArrayList<>();
        for (Future<List<String[]>> future : futures) {
            try {
                rows.addAll(future.get());
            } catch (Exception e) {
                throw new Error("Error while executing query", e);
            }
        }

        executor.shutdown();

        return rows;
    }

    private static List<String[]> getRowsFromIndexes(List<Long> indexes, File dataFile, int bytesPerRow,
                                                     int startingRowIndex, TableSchema schema) throws Exception {
        try {
            List<String[]> rows = new ArrayList<>();
            FileInputStream fis = new FileInputStream(dataFile);

            for (int i = 0; i < indexes.size(); i++) {
                long rowIndex = indexes.get(i);
                long amtToSkip = i == 0 ? (rowIndex - startingRowIndex) * bytesPerRow : (rowIndex - indexes.get(i - 1) - 1) * bytesPerRow;

                fis.skipNBytes(amtToSkip);

                String[] row = new String[schema.getColumns().size()];

                for (int j = 0; j < schema.getColumns().size(); j++) {
                    ColumnSchema colSchema = schema.getColumns().get(j);
                    byte[] bytes = fis.readNBytes(colSchema.getNumBytes());
                    row[j] = new String(bytes, StandardCharsets.US_ASCII);
                }

                rows.add(row);
            }

            fis.close();
            return rows;
        } catch (FileNotFoundException e) {
            throw new Exception("Error while reading file: ", e);
        }
    }

    private static Map<Integer, List<Long>> getIndexPools(SimpleQuery query) throws Exception {
        File colIndexFile = new File(Constants.STORAGE_LOC + File.separator +
                query.getDatabaseName() + File.separator + query.getTableName() + "_" + query.getColumnName() + ".index");

        if (!colIndexFile.exists()) {
            throw new Exception("Column " + query.getColumnName() + " does not exist.");
        }

        try (CSVReader csvReader = new CSVReader(new FileReader(colIndexFile))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (row[0].equals(query.getValue())) {
                    break;
                }
            }

            Map<Integer, List<Long>> indexPools = new HashMap<>();
            if (row == null) {
                return indexPools;
            }

            for (int i = 1; i < row.length; i++) {
                int tableNum = Integer.parseInt(row[i]) / Constants.LINES_PER_FILE;

                indexPools.computeIfAbsent(tableNum, _ -> new ArrayList<>()).add(Long.parseLong(row[i]));
            }

            return indexPools;
        } catch (IOException | CsvValidationException e) {
            throw new Exception("Error parsing data file.", e);
        }
    }
}

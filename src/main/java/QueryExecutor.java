import model.ColumnSchema;
import model.SimpleQuery;
import model.TableSchema;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryExecutor {
    public static int execute(SimpleQuery query) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Integer>> futures = new ArrayList<>();

        TableSchema tableSchema;
        try {
            tableSchema = DatabaseExplorer.getTableSchema(query.getDatabaseName(), query.getTableName());
        } catch (Exception e) {
            throw new Exception("Specified table " + query.getTableName() + " does not exist in " +
                    "database " + query.getDatabaseName());
        }

        boolean colExists = false;
        int maxColValueSize = 0;
        int byteCount = 0;
        boolean indexed = false;
        String colType = "";
        for (ColumnSchema colSchema : tableSchema.getColumns()) {
            if (colSchema.getColumnName().equals(query.getColumnName())) {
                colExists = true;
                colType = colSchema.getColumnType();
                maxColValueSize = colSchema.getNumBytes();
                indexed = colSchema.isIndexed();
            }
            byteCount += colSchema.getNumBytes();
        }
        final int bytesPerRow = byteCount;

        if (!colExists) {
            throw new Exception("Specified column does not exist.");
        }

        if (!indexed) {
            throw new Exception("Specified column not indexed.");
        }

        int shardSuffix = 0;
        File shard = new File(String.format(Constants.SHARD_LOC, query.getDatabaseName(), query.getTableName(), shardSuffix));

        while (shard.exists()) {
            futures.add(executor.submit(new Task(shardSuffix, maxColValueSize, bytesPerRow, colType, query, shard, tableSchema)));

            shardSuffix++;
            shard = new File(String.format(Constants.SHARD_LOC, query.getDatabaseName(), query.getTableName(), shardSuffix));
        }

        int numRows = 0;
        for (Future<Integer> future : futures) {
            try {
                numRows += future.get();
            } catch (Exception e) {
                throw new Error("Error while executing query", e);
            }
        }

        executor.shutdown();

        return numRows;
    }
}

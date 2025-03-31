import model.ColumnSchema;
import model.ColumnSchema.COLUMN_TYPES;
import model.SimpleQuery;
import model.TableSchema;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryExecutor {
    public static int execute(SimpleQuery query) throws ExecutionException, InterruptedException {
        int numRows;

        try (ExecutorService executor = Executors.newCachedThreadPool()) {
            List<Future<Integer>> futures = new ArrayList<>();
            TableSchema tableSchema = query.getTableSchema();

            int maxColValueSize = 0;
            int bytesPerRow = 0;
            COLUMN_TYPES colType = null;
            for (ColumnSchema colSchema : tableSchema.getColumns()) {
                if (colSchema.getColumnName().equals(query.getColumnName())) {
                    colType = colSchema.getColumnType();
                    maxColValueSize = colSchema.getNumBytes();
                }

                bytesPerRow += colSchema.getNumBytes();
            }

            int shardSuffix = 0;

            while (true) {
                File dataShardFile = new File(String.format(Constants.SHARD_LOC, query.getDatabaseName(), tableSchema.getTableName(), shardSuffix));
                if (!dataShardFile.exists()) {
                    break;
                }

                String colIndexShardFilePath = String.format(Constants.INDEX_FILE_LOC, query.getDatabaseName(),
                        query.getTableSchema().getTableName(), shardSuffix, query.getColumnName());

                futures.add(executor.submit(new CountTask(colType, query, new File(colIndexShardFilePath), dataShardFile, maxColValueSize, bytesPerRow)));
                shardSuffix++;
            }

            numRows = 0;
            for (Future<Integer> future : futures) {
                numRows += future.get();
            }

            executor.shutdown();
        }

        return numRows;
    }
}

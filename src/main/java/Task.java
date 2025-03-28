import model.SimpleQuery;
import model.TableSchema;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

public class Task implements Callable<Integer> {
    private final int shardSuffix;
    private final int maxColValueSize;
    private final int bytesPerRow;
    private final String colType;
    private final SimpleQuery query;
    private final File shard;
    private final TableSchema tableSchema;

    private final Object parsedQueryValue;
    private final byte[] stringBytes;

    public Task(int shardSuffix, int maxColValueSize, int bytesPerRow, String colType, SimpleQuery query, File shard, TableSchema tableSchema) {
        this.shardSuffix = shardSuffix;
        this.maxColValueSize = maxColValueSize;
        this.bytesPerRow = bytesPerRow;
        this.colType = colType;
        this.query = query;
        this.shard = shard;
        this.tableSchema = tableSchema;

        // to get rid of needing to constantly do parseInt/valueOf etc. can just do 1 cast instead
        if (colType.equals("int")) {
            this.parsedQueryValue = Integer.parseInt(query.getValue());
        } else {
            this.parsedQueryValue = query.getValue();
        }

        this.stringBytes = new byte[maxColValueSize];
    }

    @Override
    public Integer call() throws Exception {
        String shardColIndexPath = String.format(Constants.INDEX_FILE_LOC, query.getDatabaseName(),
                query.getTableName(), shardSuffix, query.getColumnName());

        File shardColIndexFile = new File(shardColIndexPath);

        if (!shardColIndexFile.exists()) {
            throw new Exception("Column index file not found for shard.");
        }

        try (FileChannel idxChannel = FileChannel.open(shardColIndexFile.toPath(), StandardOpenOption.READ);
             FileChannel dataChannel = FileChannel.open(shard.toPath(), StandardOpenOption.READ)) {

            long idxSize = idxChannel.size();
            long dataSize = dataChannel.size();
            if (dataSize == 0) return 0; // this means that the table might be empty since there are no vals for col

            // from my understanding, this is faster since the whole thing is initially read to memory
            // then bytes are accessed from memory not using I/O operations on the file
            MappedByteBuffer idxBuffer = idxChannel.map(FileChannel.MapMode.READ_ONLY, 0, idxSize);
            MappedByteBuffer dataBuffer = dataChannel.map(FileChannel.MapMode.READ_ONLY, 0, dataSize);

            int numIndexes = 0;
            int position = 0; // pos we are at in the idx file

            // find better way to do this than if/else depending on type
            if (colType.equals("String")) {
                String queryValue = (String) parsedQueryValue;

                // get to "row" with right val
                while (position < idxSize) {
                    idxBuffer.position(position); // set buffer position to beginning of file or new position
                    idxBuffer.get(stringBytes, 0, maxColValueSize); // reads maxColValueSize bytes and puts them into stringBytes
                    position += maxColValueSize; // since we need to update this manually

                    // this whole thing is faster than calling trim(). looked at trim docs and it is very inefficient. memory intensive on a large scale
                    // the point of this is to figure out how long val is so we can read exactly that many bytes and not have to do trimming
                    // when we see 0 byte string is over, otherwise add to str length since 1 byte from buffer = 1 char in val
                    int strLength = 0;
                    while (strLength < maxColValueSize && stringBytes[strLength] != 0) {
                        strLength++;
                    }

                    if (strLength == 0) break; // some sort of bug here, since there should be some bytes being read. figure out handling this later

                    // only read necessary number of bytes and get colvalue
                    String colValue = new String(stringBytes, 0, strLength, StandardCharsets.UTF_8);

                    if (queryValue.equals(colValue)) {
                        idxBuffer.position(position); // need to set this after updating it in line 83. basically puts 'cursor' after val
                        numIndexes = idxBuffer.getInt();

                        byte[][] rows = getRows(idxBuffer, dataBuffer, numIndexes);

                        for (byte[] row : rows) {
                            parseRowData(row); // see if it makes it slower
                        }

                        return rows.length;
                    } else {
                        int numIndexesToSkip = idxBuffer.getInt();
                        position += 4 + (numIndexesToSkip * 4); // first 4 is to move past num indexes
                    }
                }
            } else if (colType.equals("int")) {
                int queryValue = (int) parsedQueryValue;

                while (position < idxSize) {
                    idxBuffer.position(position);
                    int colValue = idxBuffer.getInt();
                    position += 4; // move it 4 bytes for int val

                    if (queryValue == colValue) {
                        numIndexes = idxBuffer.getInt();
                        position += 4; // move past the num indexes
                        idxBuffer.position(position);

                        byte[][] rows = getRows(idxBuffer, dataBuffer, numIndexes);

                        for (byte[] row : rows) {
                            parseRowData(row);
                        }

                        return rows.length;
                    } else {
                        int skipIndexes = idxBuffer.getInt();
                        position += 4 + (skipIndexes * 4); // move it 4 bytes for num indexes
                    }
                }
            } else {
                throw new Exception("Unknown column type: " + colType);
            }

            return numIndexes;
        } catch (IOException e) {
            throw new Exception("Error while reading file: ", e);
        }
    }

    public byte[][] getRows(MappedByteBuffer idxBuffer, MappedByteBuffer dataBuffer, int numIndexes) {
        if (numIndexes <= 0) {
            return new byte[0][];
        }

        int[] rowIndexes = new int[numIndexes];
        for (int i = 0; i < numIndexes; i++) {
            rowIndexes[i] = idxBuffer.getInt();
        }

        byte[][] rows = new byte[numIndexes][bytesPerRow]; // num indexes = num rows

        for (int i = 0; i < numIndexes; i++) {
            int offset = rowIndexes[i] * bytesPerRow;
            dataBuffer.position(offset); // basically puts the 'cursor' at where the row starts

            byte[] rowData = new byte[bytesPerRow];
            dataBuffer.get(rowData, 0, bytesPerRow);
            rows[i] = rowData;
        }

        return rows; // returns array of byte arrays since each row is a byte array
    }

    public Object[] parseRowData(byte[] rowData) throws Exception {
        Object[] parsedRow = new Object[tableSchema.getColumns().size()];
        int offset = 0;

        for (int i = 0; i < tableSchema.getColumns().size(); i++) {
            String colType = tableSchema.getColumns().get(i).getColumnType();

            if (colType.equals("int")) {
                ByteBuffer buffer = ByteBuffer.wrap(rowData, offset, 4);
                int value = buffer.getInt();
                parsedRow[i] = value;
                offset += 4;
            } else if (colType.equals("String")) {
                int maxSize = tableSchema.getColumns().get(i).getNumBytes();

                int strLength = 0;
                while (strLength < maxSize && rowData[offset + strLength] != 0) { // same 2nd condition to check if string ends early
                    strLength++;
                }

                parsedRow[i] = new String(rowData, offset, strLength, StandardCharsets.UTF_8);
                offset += maxSize; // move past whole allocated space for string
            } else {
                throw new Exception("Unknown column type: " + colType);
            }
        }

        return parsedRow;
    }
}

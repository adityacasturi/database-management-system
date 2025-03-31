import model.ColumnSchema;
import model.ColumnSchema.COLUMN_TYPES;
import model.SimpleQuery;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

public class CountTask implements Callable<Integer> {
    private final ColumnSchema.COLUMN_TYPES colType;
    private final SimpleQuery query;
    private final File colIndexShardFile;
    private final File dataShardFile;
    private final int maxColValueSize;
    private final int bytesPerRow;

    private final byte[] stringBytes;

    public CountTask(COLUMN_TYPES colType, SimpleQuery query, File colIndexShardFile,
                     File dataShardFile, int maxColValueSize, int bytesPerRow) {
        this.colType = colType;
        this.query = query;
        this.colIndexShardFile = colIndexShardFile;
        this.dataShardFile = dataShardFile;
        this.maxColValueSize = maxColValueSize;
        this.bytesPerRow = bytesPerRow;

        this.stringBytes = new byte[maxColValueSize];
    }

    @Override
    public Integer call() throws Exception {
        try (FileChannel idxChannel = FileChannel.open(colIndexShardFile.toPath(), StandardOpenOption.READ);
             FileChannel dataChannel = FileChannel.open(dataShardFile.toPath(), StandardOpenOption.READ)) {

            long idxSize = idxChannel.size();
            long dataSize = dataChannel.size();
            if (dataSize == 0) return 0;

            MappedByteBuffer idxBuffer = idxChannel.map(FileChannel.MapMode.READ_ONLY, 0, idxSize);
            MappedByteBuffer dataBuffer = dataChannel.map(FileChannel.MapMode.READ_ONLY, 0, dataSize);

            int numIndexes = 0;
            int position = 0;

            while (position < idxSize) {
                idxBuffer.position(position);

                Object colValue;
                if (colType == COLUMN_TYPES.INT_TYPE) {
                    colValue = idxBuffer.getInt();
                } else {
                    idxBuffer.get(stringBytes, 0, maxColValueSize);
                    int strLength = 0;
                    while (strLength < maxColValueSize && stringBytes[strLength] != 0) {
                        strLength++;
                    }

                    colValue = new String(stringBytes, 0, strLength, StandardCharsets.UTF_8);
                }

                position += maxColValueSize;
                idxBuffer.position(position);

                if (isColValueEqualToQueryValue(colType, colValue, query.getValue())) {
                    numIndexes = idxBuffer.getInt();
                    position += 4;
                    idxBuffer.position(position);

                    byte[][] rows = getRows(idxBuffer, dataBuffer, numIndexes);

                    for (byte[] row : rows) {
                        parseRowData(row);
                    }

                    return rows.length;
                } else {
                    int numIndexesToSkip = idxBuffer.getInt();
                    position += 4 + (numIndexesToSkip * 4);
                }
            }

            return numIndexes;
        } catch (IOException e) {
            throw new Exception("Error while reading file: ", e);
        }
    }

    private boolean isColValueEqualToQueryValue(COLUMN_TYPES colType, Object colValue, String queryValue) {
        if (colType == COLUMN_TYPES.INT_TYPE) {
            return (int) colValue == Integer.parseInt(queryValue);
        } else {
            return colValue.equals(queryValue);
        }
    }

    public byte[][] getRows(MappedByteBuffer idxBuffer, MappedByteBuffer dataBuffer, int numIndexes) {
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
        Object[] parsedRow = new Object[query.getTableSchema().getColumns().size()];
        int offset = 0;

        for (int i = 0; i < query.getTableSchema().getColumns().size(); i++) {
            COLUMN_TYPES colType = query.getTableSchema().getColumns().get(i).getColumnType();

            if (colType == COLUMN_TYPES.INT_TYPE) {
                ByteBuffer buffer = ByteBuffer.wrap(rowData, offset, 4);
                int value = buffer.getInt();
                parsedRow[i] = value;
                offset += 4;
            } else if (colType == COLUMN_TYPES.STRING_TYPE) {
                int maxSize = query.getTableSchema().getColumns().get(i).getNumBytes();

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

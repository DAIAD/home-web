package eu.daiad.web.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.hbase.EnumHBaseColumnFamily;
import eu.daiad.web.hbase.EnumHBaseTable;
import eu.daiad.web.hbase.HBaseConnectionManager;
import eu.daiad.web.model.error.SharedErrorCode;

/**
 * Base repository for deriving HBase specific repositories.
 */
public abstract class AbstractHBaseRepository extends BaseRepository {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(AbstractHBaseRepository.class);

    /**
     * Logger for amphiro b1 sessions.
     */
    protected static final String LOGGER_SESSION = "AmphiroSessionLogger";

    /**
     * Logger for amphiro b1 session time series.
     */
    protected static final String LOGGER_MEASUREMENT = "AmphiroMeasurementLogger";

    /**
     * Logger for amphiro b1 membership updates..
     */
    protected static final String LOGGER_MEMBER = "AmphiroSessionMemberLogger";

    /**
     * Logger for amphiro b1 for writing shower that should be ignored.
     */
    protected static final String LOGGER_IGNORE = "AmphiroSessionIgnoreLogger";

    /**
     * Logger for amphiro b1 for writing historical showers that are converted to real-time.
     */
    protected static final String LOGGER_REAL_TIME = "AmphiroSessionRealTimeLogger";

    /**
     * HBase connection.
     */
    @Autowired
    protected HBaseConnectionManager connection;

    /**
     * Number of partitions used for distributing row keys that are ordered by
     * time.
     */
    @Value("${hbase.data.time.partitions}")
    protected short timePartitions;

    /**
     * HBase scanner cache size.
     */
    @Value("${scanner.cache.size}")
    protected int scanCacheSize = 1;

    /**
     * Provides methods for serializing Java objects to JSON strings.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Returns the closest row key next to the given row prefix.
     *
     * @param rowKeyPrefix the row prefix for which the next row key is returned.
     * @return the next row key.
     */
    protected byte[] calculateTheClosestNextRowKeyForPrefix(byte[] rowKeyPrefix) {
        // Essentially we are treating it like an 'unsigned very very long' and
        // doing +1 manually. Search for the place where the trailing 0xFFs
        // start
        int offset = rowKeyPrefix.length;
        while (offset > 0) {
            if (rowKeyPrefix[offset - 1] != (byte) 0xFF) {
                break;
            }
            offset--;
        }

        if (offset == 0) {
            // We got an 0xFFFF... (only FFs) stopRow value which is the last
            // possible prefix before the end of the table. So set it to stop at
            // the 'end of the table'
            return HConstants.EMPTY_END_ROW;
        }

        // Copy the right length of the original
        byte[] newStopRow = Arrays.copyOfRange(rowKeyPrefix, 0, offset);
        // And increment the last one
        newStopRow[newStopRow.length - 1]++;
        return newStopRow;
    }

    /**
     * Concatenates two byte arrays.
     *
     * @param a the first byte array to concatenate.
     * @param b the second byte array to concatenate.
     * @return a new array containing the bytes of both input arrays.
     */
    protected byte[] concatenate(byte[] a, byte[] b) {
        int lengthA = a.length;
        int lengthB = b.length;
        byte[] concat = new byte[lengthA + lengthB];
        System.arraycopy(a, 0, concat, 0, lengthA);
        System.arraycopy(b, 0, concat, lengthA, lengthB);
        return concat;
    }

    /**
     * Prefixes a byte array with a single byte that holds the initial byte
     * array length.
     *
     * @param array the byte array.
     * @return a new byte array.
     * @throws Exception if the size of the array is greater than 255.
     */
    protected byte[] appendLength(byte[] array) throws Exception {
        if (array.length > 255) {
            throw new Exception("Invalid byte array size.");
        }
        byte[] length = { (byte) array.length };

        return concatenate(length, array);
    }

    /**
     * Checks if the a byte array is contained in a list of byte arrays.
     * @param array the list of arrays to search.
     * @param hash the array to find.
     * @return if the array is contained in the list.
     */
    protected int inArray(List<byte[]> array, byte[] hash) {
        int index = 0;
        for (byte[] entry : array) {
            if (Arrays.equals(entry, hash)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param value the object to serialize.
     * @return the serialized string.
     */
    protected String jsonToString(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception ex) {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("type", value.getClass().getName());

            logger.warn(getMessage(SharedErrorCode.JSON_SERIALIZE_ERROR, properties));
        }

        return StringUtils.EMPTY;
    }

    /**
     * Time intervals used for creating a timestamp prefix.
     */
    protected enum EnumTimeInterval {
        UNDEFINED(0), HOUR(3600), DAY(86400);

        private final int value;

        private EnumTimeInterval(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Increments a counter.
     *
     * @param row the counter row.
     * @param column the counter column.
     * @return the new counter value.
     * @throws IOException if increasing the counter or resource release has failed.
     */
    protected long increment(String row, String column) throws IOException {
        Table table = null;

        try {
            table = connection.getTable(EnumHBaseTable.COUNTERS.getValue());

            return table.incrementColumnValue(Bytes.toBytes(row),
                                              Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue()),
                                              Bytes.toBytes(column),
                                              1);
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }
}

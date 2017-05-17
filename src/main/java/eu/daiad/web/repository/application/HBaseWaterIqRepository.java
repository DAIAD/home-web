package eu.daiad.web.repository.application;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.application.mappings.SavingsPotentialWaterIqMappingEntity;
import eu.daiad.web.hbase.EnumHBaseColumnFamily;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.ComparisonRanking;
import eu.daiad.web.model.profile.ComparisonRanking.DailyConsumption;
import eu.daiad.web.model.profile.ComparisonRanking.MonthlyConsumtpion;
import eu.daiad.web.model.profile.ComparisonRanking.WaterIq;
import eu.daiad.web.repository.AbstractHBaseRepository;

/**
 * Provides methods for updating and querying user Water IQ status.
 */
@Repository("hBaseWaterIqRepository")
public class HBaseWaterIqRepository extends AbstractHBaseRepository implements IWaterIqRepository {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(HBaseWaterIqRepository.class);

    /**
     * HBase table for comparison and ranking daily consumption data.
     */
    private static final String HBASE_TABLE_COMPARISON_RANKING_DAILY = "daiad:comparison-ranking-daily";

    /**
     * HBase comparison and ranking table columns.
     */
    private static enum EnumColumn {
        UNKNOWN(null),

        USER_VOLUME("u:v"),
        SIMILAR_VOLUME("s:v"),
        NEAREST_VOLUME("n:v"),
        ALL_VOLUME("a:v"),

        WEEK("w");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumColumn(String value) {
            this.value = value;
        }

        public static final Map<String, EnumColumn> stringToTypeMap = new HashMap<String, EnumColumn>();
        static {
            for (EnumColumn type : EnumColumn.values()) {
                if (stringToTypeMap.containsKey(type.value)) {
                    throw new RuntimeException(String.format("HBase column [%s] already defined.", type.value));
                }
                stringToTypeMap.put(type.value, type);
            }
        }

    };

    /**
     * Deletes stale water IQ data.
     *
     * @param days number of dates after which water IQ data is considered stale.
     */
    @Override
    public void clean(int days) {
        // Ignore request
    }

    /**
     * Update user Water IQ.
     *
     * @param userKey the user key.
     * @param from time interval start date formatted using the pattern {@code yyyyMMdd}.
     * @param to time interval end date formatted using the pattern {@code yyyyMMdd}.
     * @param user water IQ data for a single user.
     * @param similar water IQ data for a group of similar users.
     * @param neighbor water IQ data for the group of neighbors.
     * @param all water IQ data for all users.
     * @param monthlyConsumtpion monthly consumption data.
     * @param dailyConsumption daily consumption data.
     */
    @Override
    public void update(UUID userKey,
                       String from,
                       String to,
                       WaterIq user,
                       WaterIq similar,
                       WaterIq neighbor,
                       WaterIq all,
                       MonthlyConsumtpion monthlyConsumtpion,
                       List<DailyConsumption> dailyConsumption) {
        Table table = null;
        try {
            table = connection.getTable(HBASE_TABLE_COMPARISON_RANKING_DAILY);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] userKeyHash = md.digest(userKey.toString().getBytes("UTF-8"));

            int month = Integer.parseInt(from.substring(4, 6));

            for (DailyConsumption day : dailyConsumption) {
                if (day.month != month) {
                    continue;
                }
                Put p = new Put(createRowKey(userKeyHash, day.year, day.month, day.day));

                byte[] column = Bytes.toBytes(EnumColumn.WEEK.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.week));

                column = Bytes.toBytes(EnumColumn.USER_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.user));

                column = Bytes.toBytes(EnumColumn.SIMILAR_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.similar));

                column = Bytes.toBytes(EnumColumn.NEAREST_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.nearest));

                column = Bytes.toBytes(EnumColumn.ALL_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.all));

                table.put(p);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
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

    /**
     * Update user Water IQ.
     *
     * @param userKey the user key.
     * @param dailyConsumption daily consumption data.
     */
    @Override
    public void storeDailyData(UUID userKey, List<ComparisonRanking.DailyConsumption> dailyConsumption) {
        Table table = null;
        try {
            table = connection.getTable(HBASE_TABLE_COMPARISON_RANKING_DAILY);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] userKeyHash = md.digest(userKey.toString().getBytes("UTF-8"));

            for (DailyConsumption day : dailyConsumption) {
                Put p = new Put(createRowKey(userKeyHash, day.year, day.month, day.day));

                byte[] column = Bytes.toBytes(EnumColumn.WEEK.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.week));

                column = Bytes.toBytes(EnumColumn.USER_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.user));

                column = Bytes.toBytes(EnumColumn.SIMILAR_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.similar));

                column = Bytes.toBytes(EnumColumn.NEAREST_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.nearest));

                column = Bytes.toBytes(EnumColumn.ALL_VOLUME.getValue());
                p.addColumn(columnFamily, column, Bytes.toBytes(day.all));

                table.put(p);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
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

    private byte[] createRowKey(byte[] userKeyHash, int year, int month, int day) {
        byte[] timestamp = Bytes.toBytes((int) year * 10000 + month * 100 + day);

        byte[] rowKey = new byte[userKeyHash.length + timestamp.length];
        System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
        System.arraycopy(timestamp, 0, rowKey, userKeyHash.length, timestamp.length);

        return rowKey;
    }

    private byte[] createRowKey(byte[] userKeyHash, int year, int month) {
        byte[] timestamp = Bytes.toBytes((int) year * 10000 + month * 100);

        byte[] rowKey = new byte[userKeyHash.length + timestamp.length];
        System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
        System.arraycopy(timestamp, 0, rowKey, userKeyHash.length, timestamp.length);

        return rowKey;
    }

    /**
     * Returns HBase comparison and ranking table column enumeration value for a given name.
     *
     * @param value the column name.
     * @param throwException throws an exception if the column does not exist in the enumeration.
     * @return the associated enumeration value.
     */
    private EnumColumn getColumn(String value, boolean throwException) {
        EnumColumn type = EnumColumn.stringToTypeMap.get(value);
        if (type == null) {
            if (throwException) {
                throw createApplicationException(DataErrorCode.HBASE_INVALID_COLUMN).set("column", value);
            }
            return EnumColumn.UNKNOWN;
        }
        return type;
    }

    /**
     * Returns water IQ data for the user with the given key.
     *
     * @param key the user key.
     * @param year reference year.
     * @param month reference month.
     * @return water IQ data.
     */
    @Override
    public ComparisonRanking getWaterIqByUserKey(UUID key, int year, int month) {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Returns the daily consumption for the given key for the selected year and month.
     *
     * @param userKey the user key.
     * @param year the year.
     * @param month the month.
     * @return a list of {@link DailyConsumption}.
     */
    @Override
    public List<DailyConsumption> getComparisonDailyConsumption(UUID userKey, int year, int month) {
        return getComparisonDailyConsumptionData(userKey, year, month);
    }

    /**
     * Returns all the daily consumption data for the given user key.
     *
     * @param userKey the user key.
     * @return a list of {@link DailyConsumption}.
     */
    @Override
    public List<ComparisonRanking.DailyConsumption> getAllComparisonDailyConsumption(UUID userKey) {
        return getComparisonDailyConsumptionData(userKey, null, null);
    }

    private List<DailyConsumption> getComparisonDailyConsumptionData(UUID userKey, Integer year, Integer month) {
        Table table = null;
        ResultScanner scanner = null;

        List<DailyConsumption> result = new ArrayList<DailyConsumption>();

        try {
            table = connection.getTable(HBASE_TABLE_COMPARISON_RANKING_DAILY);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] userKeyHash = md.digest(userKey.toString().getBytes("UTF-8"));

            Scan scan = new Scan();
            scan.setCaching(scanCacheSize);
            scan.addFamily(columnFamily);
            if ((year != null) && (month != null)) {
                int minYear = (month < 6 ? (year - 1) : year);
                int minMonth = ((month - 5) > 0 ? (month - 5) : (month + 7));
                scan.setStartRow(createRowKey(userKeyHash, minYear, minMonth));
                scan.setStopRow(createRowKey(userKeyHash, year, month + 1));
            } else {
                scan.setStartRow(createRowKey(userKeyHash, 0, 0));
                scan.setStopRow(createRowKey(userKeyHash, 9999, 13));
            }

            scanner = table.getScanner(scan);

            for (Result r = scanner.next(); r != null; r = scanner.next()) {
                NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                int timestamp = Bytes.toInt(Arrays.copyOfRange(r.getRow(), 16, 20));

                int dateYear = timestamp / 10000;
                int dateMonth = (timestamp / 100) % 100;
                int dateDay = timestamp % 100;

                ComparisonRanking.DailyConsumption dailyConsumption = new ComparisonRanking.DailyConsumption(dateYear, dateMonth, dateDay);

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    String qualifier = Bytes.toString(entry.getKey());

                    switch (getColumn(qualifier, false)) {
                         case USER_VOLUME:
                             dailyConsumption.user = Bytes.toDouble(entry.getValue());
                            break;
                        case SIMILAR_VOLUME:
                            dailyConsumption.similar = Bytes.toDouble(entry.getValue());
                            break;
                        case NEAREST_VOLUME:
                            dailyConsumption.nearest = Bytes.toDouble(entry.getValue());
                            break;
                        case ALL_VOLUME:
                            dailyConsumption.all = Bytes.toDouble(entry.getValue());
                            break;
                        case WEEK:
                            dailyConsumption.week = Bytes.toInt(entry.getValue());
                            break;
                        default:
                            // Ignore
                            break;
                    }
                }
                result.add(dailyConsumption);
            }

            return result;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                    scanner = null;
                }
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }


    /**
     * Returns the Water IQ for similar users as computed by the savings potential algorithm.
     * @param utilityId the utility id.
     * @param month the month.
     * @param serial the meter serial number.
     * @return a list of Water IQ values as computed by the savings potential algorithm.
     */
    @Override
    public List<SavingsPotentialWaterIqMappingEntity> getWaterIqForSimilarUsersFromSavingsPotential(int utilityId, int month, String serial) {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

}

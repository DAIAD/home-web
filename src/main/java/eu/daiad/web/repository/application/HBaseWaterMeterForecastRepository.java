package eu.daiad.web.repository.application;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.WaterMeterForecast;
import eu.daiad.web.model.meter.WaterMeterForecastCollection;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.query.MeterUserDataPoint;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;
import eu.daiad.web.repository.AbstractHBaseRepository;

@Repository
public class HBaseWaterMeterForecastRepository extends AbstractHBaseRepository implements IWaterMeterForecastRepository {

    private static final Log logger = LogFactory.getLog(HBaseWaterMeterForecastRepository.class);

    private final String meterTableForecastByUser = "daiad:meter-forecast-by-user";

    private final String meterTableForecastByTime = "daiad:meter-forecast-by-time";

    @Override
    public void store(String serial, WaterMeterForecastCollection data) {
        try {
            if ((data == null) || (data.getMeasurements() == null) || (data.getMeasurements().size() == 0)) {
                return;
            }

            // Sort measurements
            Collections.sort(data.getMeasurements(), new Comparator<WaterMeterForecast>() {
                @Override
                public int compare(WaterMeterForecast o1, WaterMeterForecast o2) {
                    if (o1.getTimestamp() <= o2.getTimestamp()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            storeDataByMeter(serial, data);
            storeDataByTime(serial, data);
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @SuppressWarnings("resource")
    private void storeDataByMeter(String serial, WaterMeterForecastCollection data) {
        Table table = null;
        try {
            table = connection.getTable(meterTableForecastByUser);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterForecast m = data.getMeasurements().get(i);

                if (m.getDifference() <= 0) {
                    continue;
                }

                long timestamp = (Long.MAX_VALUE / 1000) - (m.getTimestamp() / 1000);

                long timeSlice = timestamp % EnumTimeInterval.DAY.getValue();
                byte[] timeSliceBytes = Bytes.toBytes((int) timeSlice);
                if (timeSliceBytes.length != 4) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                long timeBucket = timestamp - timeSlice;

                byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
                if (timeBucketBytes.length != 8) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                byte[] rowKey = new byte[meterSerialHash.length + timeBucketBytes.length];
                System.arraycopy(meterSerialHash, 0, rowKey, 0, meterSerialHash.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, meterSerialHash.length, timeBucketBytes.length);

                Put p = new Put(rowKey);

                byte[] column = concatenate(timeSliceBytes, appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

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

    @SuppressWarnings("resource")
    private void storeDataByTime(String serial, WaterMeterForecastCollection data) {
        Table table = null;

        try {
            table = connection.getTable(meterTableForecastByTime);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterForecast m = data.getMeasurements().get(i);

                if (m.getDifference() <= 0) {
                    continue;
                }
                short partition = (short) (m.getTimestamp() % timePartitions);
                byte[] partitionBytes = Bytes.toBytes(partition);

                long timestamp = (Long.MAX_VALUE / 1000) - (m.getTimestamp() / 1000);

                long timeSlice = timestamp % EnumTimeInterval.DAY.getValue();
                byte[] timeSliceBytes = Bytes.toBytes((int) timeSlice);
                if (timeSliceBytes.length != 4) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                long timeBucket = timestamp - timeSlice;

                byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
                if (timeBucketBytes.length != 8) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                byte[] rowKey = new byte[partitionBytes.length + timeBucketBytes.length + meterSerialHash.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
                System.arraycopy(meterSerialHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
                                meterSerialHash.length);

                Put p = new Put(rowKey);

                byte[] column = concatenate(timeSliceBytes, appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

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

    @Override
    public ArrayList<GroupDataSeries> forecast(ExpandedDataQuery query) throws ApplicationException {
        Table table = null;
        ResultScanner scanner = null;

        ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        for (ExpandedPopulationFilter filter : query.getGroups()) {
            result.add(new GroupDataSeries(filter.getLabel(), filter.getUsers().size(), filter.getAreaId()));
        }
        try {
            table = connection.getTable(meterTableForecastByTime);
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            DateTime startDate = new DateTime(query.getStartDateTime(), DateTimeZone.UTC);
            DateTime endDate = new DateTime(query.getEndDateTime(), DateTimeZone.UTC);

            switch (query.getGranularity()) {
                case HOUR:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
                                    endDate.getHourOfDay(), 59, 59, DateTimeZone.UTC);
                    break;
                case DAY:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), 0, 0, 0, DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
                                    59, 59, DateTimeZone.UTC);
                    break;
                case WEEK:
                    DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                    DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                    startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0,
                                    0, DateTimeZone.UTC);
                    endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59,
                                    59, DateTimeZone.UTC);
                    break;
                case MONTH:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0,
                                    DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                    .getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
                    break;
                case YEAR:
                    startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
                    break;
                case ALL:
                    // Ignore
                    break;
                default:
                    throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
                                    query.getGranularity());
            }

            for (short p = 0; p < timePartitions; p++) {
                Scan scan = new Scan();
                scan.setCaching(scanCacheSize);
                scan.addFamily(columnFamily);

                byte[] partitionBytes = Bytes.toBytes(p);

                long from = (Long.MAX_VALUE / 1000) - (endDate.getMillis() / 1000);
                from = from - (from % EnumTimeInterval.DAY.getValue());
                byte[] fromBytes = Bytes.toBytes(from);

                long to = (Long.MAX_VALUE / 1000) - (startDate.getMillis() / 1000);
                to = to - (to % EnumTimeInterval.DAY.getValue());
                byte[] toBytes = Bytes.toBytes(to);

                // Scanner row key prefix start
                byte[] rowKey = new byte[partitionBytes.length + fromBytes.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(fromBytes, 0, rowKey, partitionBytes.length, fromBytes.length);

                scan.setStartRow(rowKey);

                // Scanner row key prefix end
                rowKey = new byte[partitionBytes.length + toBytes.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(toBytes, 0, rowKey, partitionBytes.length, toBytes.length);

                scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(rowKey));

                scanner = table.getScanner(scan);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 2, 10));
                    byte[] serialHash = Arrays.copyOfRange(r.getRow(), 10, 26);

                    float difference = 0;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        int offset = Bytes.toInt(Arrays.copyOfRange(entry.getKey(), 0, 4));
                        long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

                        if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
                            int length = (int) Arrays.copyOfRange(entry.getKey(), 4, 5)[0];
                            byte[] slice = Arrays.copyOfRange(entry.getKey(), 5, 5 + length);

                            String columnQualifier = Bytes.toString(slice);
                            if (columnQualifier.equals("d")) {
                                difference = Bytes.toFloat(entry.getValue());
                            }

                            if (difference > 0) {
                                int filterIndex = 0;
                                for (ExpandedPopulationFilter filter : query.getGroups()) {
                                    GroupDataSeries series = result.get(filterIndex);

                                    int index = inArray(filter.getSerials(), serialHash);
                                    if (index >= 0) {
                                        series.addMeterRankingDataPoint(query.getGranularity(), filter.getUsers().get(
                                                        index), filter.getLabels().get(index), timestamp, difference,
                                                        0, query.getMetrics(), query.getTimezone());

                                    }

                                    filterIndex++;
                                }
                            }
                            difference = 0;
                        }
                    }
                }
            }
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

        // Post process results
        int filterIndex = 0;
        for (final ExpandedPopulationFilter filter : query.getGroups()) {
            GroupDataSeries series = result.get(filterIndex);

            if (filter.getRanking() == null) {
                // Aggregate all user data points of a ranking data point to a
                // single meter data point
                ArrayList<DataPoint> points = new ArrayList<DataPoint>();

                for (DataPoint point : series.getPoints()) {
                    points.add(((RankingDataPoint) point).aggregate(query.getMetrics(),
                                    DataPoint.EnumDataPointType.METER));
                }

                series.setPoints(points);
            } else {
                // Truncate (n-k) users and keep top/bottom-k only
                for (DataPoint point : series.getPoints()) {
                    RankingDataPoint ranking = (RankingDataPoint) point;

                    Collections.sort(ranking.getUsers(), new Comparator<UserDataPoint>() {

                        @Override
                        public int compare(UserDataPoint u1, UserDataPoint u2) {
                            MeterUserDataPoint m1 = (MeterUserDataPoint) u1;
                            MeterUserDataPoint m2 = (MeterUserDataPoint) u2;

                            if (m1.getVolume().get(EnumMetric.SUM) < m2.getVolume().get(EnumMetric.SUM)) {
                                return -1;
                            }
                            if (m1.getVolume().get(EnumMetric.SUM) > m2.getVolume().get(EnumMetric.SUM)) {
                                return 1;
                            }
                            return 0;
                        }
                    });

                    int limit = filter.getRanking().getLimit();
                    switch (filter.getRanking().getType()) {
                        case TOP:
                            for (int i = 0, max = ranking.getUsers().size() - limit; i < max; i++) {
                                ranking.getUsers().remove(0);
                            }
                            break;
                        case BOTTOM:
                            for (int i = ranking.getUsers().size() - 1, max = limit - 1; i > max; i--) {
                                ranking.getUsers().remove(i);
                            }
                            break;
                        default:
                            series.getPoints().clear();
                            break;
                    }
                }
            }
            filterIndex++;
        }

        cleanSeries(query, result);

        return result;
    }

    private void cleanSeries(ExpandedDataQuery query, ArrayList<GroupDataSeries> result) {
        int filterIndex = 0;
        for (final ExpandedPopulationFilter filter : query.getGroups()) {
            GroupDataSeries series = result.get(filterIndex);
            if (filter.getRanking() == null) {
                // MIN, MAX are not supported for ranking queries
                for (Object p : series.getPoints()) {
                    MeterDataPoint meterDataPoint = (MeterDataPoint) p;

                    meterDataPoint.getVolume().remove(EnumMetric.MIN);
                    meterDataPoint.getVolume().remove(EnumMetric.MAX);
                }
            } else {
                // MIN, MAX, AVERAGE, COUNT are not supported for ranking
                // queries
                for (Object p : series.getPoints()) {
                    RankingDataPoint rankingDataPoint = (RankingDataPoint) p;
                    for (UserDataPoint userDataPoint : rankingDataPoint.getUsers()) {
                        MeterUserDataPoint meterUserDataPoint = (MeterUserDataPoint) userDataPoint;

                        meterUserDataPoint.getVolume().remove(EnumMetric.MIN);
                        meterUserDataPoint.getVolume().remove(EnumMetric.MAX);
                        meterUserDataPoint.getVolume().remove(EnumMetric.AVERAGE);
                        meterUserDataPoint.getVolume().remove(EnumMetric.COUNT);
                    }
                }
            }
            filterIndex++;
        }
    }
}

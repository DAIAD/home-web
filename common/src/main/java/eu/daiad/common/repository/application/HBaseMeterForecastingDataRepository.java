package eu.daiad.common.repository.application;

import java.nio.charset.StandardCharsets;
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
import org.springframework.stereotype.Repository;

import eu.daiad.common.hbase.EnumHBaseColumnFamily;
import eu.daiad.common.hbase.EnumHBaseTable;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.DataErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.meter.WaterMeterForecast;
import eu.daiad.common.model.meter.WaterMeterForecastCollection;
import eu.daiad.common.model.query.DataPoint;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.ExpandedDataQuery;
import eu.daiad.common.model.query.ExpandedPopulationFilter;
import eu.daiad.common.model.query.GroupDataSeries;
import eu.daiad.common.model.query.MeterDataPoint;
import eu.daiad.common.model.query.MeterUserDataPoint;
import eu.daiad.common.model.query.RankingDataPoint;
import eu.daiad.common.model.query.UserDataPoint;

@Repository()
public class HBaseMeterForecastingDataRepository extends AbstractHBaseMeterDataRepository implements IMeterForecastingDataRepository {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(HBaseMeterForecastingDataRepository.class);

    public HBaseMeterForecastingDataRepository() {
        interval = EnumTimeInterval.DAY;
    }

    /**
     * Stores smart water meter forecasting data.
     *
     * @param serial the smart water meter unique serial number.
     * @param data a collection of {@link WaterMeterForecastCollection}.
     */
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

    /**
     * Stores smart water meter forecasting data indexed by serial number.
     *
     * @param serial the smart water meter data unique serial number.
     * @param data a collection of {@link WaterMeterForecast}.
     */
    private void storeDataByMeter(String serial, WaterMeterForecastCollection data) {
        Table table = null;
        try {
            table = connection.getTable(EnumHBaseTable.SWM_FORECAST_USER.getValue());

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterForecast m = data.getMeasurements().get(i);

                if (m.getDifference() < 0) {
                    continue;
                }

                RowKeyQualifierPrefix key = createMeterRowKeyQualifierPrefix(meterSerialHash, m.getTimestamp());

                Put p = new Put(key.rowKey);

                byte[] column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

                column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("s")));
                p.addColumn(columnFamily, column, serial.getBytes(StandardCharsets.UTF_8));

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
     * Stores smart water meter forecasting data partitioned by time.
     *
     * @param serial the smart water meter data unique serial number.
     * @param data a collection of {@link WaterMeterForecast}.
     */
    private void storeDataByTime(String serial, WaterMeterForecastCollection data) {
        Table table = null;

        try {
            table = connection.getTable(EnumHBaseTable.SWM_FORECAST_TIME.getValue());

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterForecast m = data.getMeasurements().get(i);

                if (m.getDifference() < 0) {
                    continue;
                }

                RowKeyQualifierPrefix key = createPartitionedRowKeyQualifierPrefix(meterSerialHash, m.getTimestamp());

                Put p = new Put(key.rowKey);

                byte[] column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

                column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("s")));
                p.addColumn(columnFamily, column, serial.getBytes(StandardCharsets.UTF_8));

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
     * Executes a query for smart water meter forecasting data.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    @Override
    public ArrayList<GroupDataSeries> forecast(ExpandedDataQuery query) throws ApplicationException {
        Table table = null;
        ResultScanner scanner = null;

        ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        for (ExpandedPopulationFilter filter : query.getGroups()) {
            result.add(new GroupDataSeries(filter.getLabel(), filter.getSize(), filter.getAreaId()));
        }
        try {
            table = connection.getTable(EnumHBaseTable.SWM_FORECAST_TIME.getValue());
            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            DateTime startDate = new DateTime(query.getStartDateTime(), query.getTimezone());
            DateTime endDate = new DateTime(query.getEndDateTime(), query.getTimezone());

            switch (query.getGranularity()) {
                case HOUR:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), startDate.getHourOfDay(), 0, 0, query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
                                    endDate.getHourOfDay(), 59, 59, query.getTimezone());
                    break;
                case DAY:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), 0, 0, 0, query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
                                    59, 59, query.getTimezone());
                    break;
                case WEEK:
                    DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                    DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                    startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0,
                                    0, query.getTimezone());
                    endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59,
                                    59, query.getTimezone());
                    break;
                case MONTH:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0,
                                    query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                    .getMaximumValue(), 23, 59, 59, query.getTimezone());
                    break;
                case YEAR:
                    startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, query.getTimezone());
                    break;
                default:
                    throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED)
                    	.set("level", query.getGranularity());
            }

            for (short p = 0; p < timePartitions; p++) {
                Scan scan = new Scan();
                scan.setCaching(scanCacheSize);
                scan.addFamily(columnFamily);

                byte[] rowKey = createPartitionedRowKey(p, endDate.getMillis());
                scan.setStartRow(rowKey);

                rowKey = createPartitionedRowKey(p, startDate.getMillis());
                scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(rowKey));

                scanner = table.getScanner(scan);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 2, 10));
                    byte[] serialHash = Arrays.copyOfRange(r.getRow(), 10, 26);

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        int offset = Bytes.toInt(Arrays.copyOfRange(entry.getKey(), 0, 4));
                        long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

                        if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
                            int length = (int) Arrays.copyOfRange(entry.getKey(), 4, 5)[0];
                            byte[] slice = Arrays.copyOfRange(entry.getKey(), 5, 5 + length);

                            String columnQualifier = Bytes.toString(slice);
                            if (columnQualifier.equals("d")) {
                                float difference = Bytes.toFloat(entry.getValue());

                                int filterIndex = 0;
                                for (ExpandedPopulationFilter filter : query.getGroups()) {
                                    GroupDataSeries series = result.get(filterIndex);

                                    int index = inArray(filter.getSerialHashes(), serialHash);
                                    if (index >= 0) {
                                        series.addMeterRankingDataPoint(
                                            query.getGranularity(),
                                            filter.getUserKeys().get(index),
                                            filter.getLabels().get(index),
                                            timestamp,
                                            difference,
                                            0,
                                            query.getMetrics(),
                                            query.getTimezone());
                                    }

                                    filterIndex++;
                                }
                            }
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
                    points.add(((RankingDataPoint) point).aggregate(query.getMetrics(), DataPoint.EnumDataPointType.METER));
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

    /**
     * Removes any unsupported metrics from a query's result.
     *
     * @param query the query.
     * @param result the query result.
     */
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

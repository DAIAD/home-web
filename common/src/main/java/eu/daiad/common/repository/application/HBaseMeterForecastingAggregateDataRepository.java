package eu.daiad.common.repository.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import eu.daiad.common.model.query.DataPoint;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.EnumPopulationFilterType;
import eu.daiad.common.model.query.EnumRankingType;
import eu.daiad.common.model.query.ExpandedDataQuery;
import eu.daiad.common.model.query.ExpandedPopulationFilter;
import eu.daiad.common.model.query.GroupDataSeries;
import eu.daiad.common.model.query.MeterDataPoint;
import eu.daiad.common.model.query.MeterUserDataPoint;
import eu.daiad.common.model.query.RankingDataPoint;
import eu.daiad.common.model.query.UserDataPoint;

@Repository()
public class HBaseMeterForecastingAggregateDataRepository  extends AbstractHBaseMeterAggregateDataRepository implements IMeterForecastingAggregateDataRepository {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(HBaseMeterForecastingAggregateDataRepository.class);

    public HBaseMeterForecastingAggregateDataRepository() {
        interval = EnumTimeInterval.DAY;
    }

    /**
     * Executes a query for smart water meter forecasting data using aggregates.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    @Override
    public List<GroupDataSeries> forecast(ExpandedDataQuery query) throws ApplicationException {
        Table detailTable = null;
        Table aggregateTable = null;
        ResultScanner scanner = null;

        List<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            detailTable = connection.getTable(EnumHBaseTable.SWM_FORECAST_USER.getValue());
            aggregateTable = connection.getTable(EnumHBaseTable.SWM_FORECAST_AGGREGATE.getValue());

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            DateTime startDate = new DateTime(query.getStartDateTime(), query.getTimezone());
            DateTime endDate = new DateTime(query.getEndDateTime(), query.getTimezone());

            switch (query.getGranularity()) {
                case HOUR:
                    startDate = startDate.minuteOfHour().setCopy(0)
                                         .secondOfMinute().setCopy(0);
                    endDate = endDate.minuteOfHour().setCopy(59)
                                     .secondOfMinute().setCopy(59);
                    break;
                case DAY:
                    startDate = startDate.hourOfDay().setCopy(0)
                                         .minuteOfHour().setCopy(0)
                                         .secondOfMinute().setCopy(0);
                    endDate = endDate.hourOfDay().setCopy(23)
                                     .minuteOfHour().setCopy(59)
                                     .secondOfMinute().setCopy(59);
                    break;
                case WEEK:
                    DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                    DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);

                    startDate = monday.hourOfDay().setCopy(0)
                                      .minuteOfHour().setCopy(0)
                                      .secondOfMinute().setCopy(0);
                    endDate = sunday.hourOfDay().setCopy(23)
                                    .minuteOfHour().setCopy(59)
                                    .secondOfMinute().setCopy(59);

                    break;
                case MONTH:
                    startDate = startDate.dayOfMonth().withMinimumValue()
                                         .hourOfDay().setCopy(0)
                                         .minuteOfHour().setCopy(0)
                                         .secondOfMinute().setCopy(0);
                    endDate = endDate.dayOfMonth().withMaximumValue()
                                     .hourOfDay().setCopy(23)
                                     .minuteOfHour().setCopy(59)
                                     .secondOfMinute().setCopy(59);
                    break;
                case YEAR:
                    startDate = startDate.monthOfYear().setCopy(1)
                                         .monthOfYear().withMinimumValue()
                                         .hourOfDay().setCopy(0)
                                         .minuteOfHour().setCopy(0)
                                         .secondOfMinute().setCopy(0);
                    endDate = endDate.monthOfYear().setCopy(12)
                                     .monthOfYear().withMaximumValue()
                                     .hourOfDay().setCopy(23)
                                     .minuteOfHour().setCopy(59)
                                     .secondOfMinute().setCopy(59);
                    break;
                default:
                    throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED)
                    	.set("level", query.getGranularity());
            }
            startDate = startDate.millisOfSecond().setCopy(0);
            endDate = endDate.millisOfSecond().setCopy(0);
            for (ExpandedPopulationFilter filter : query.getGroups()) {
                GroupDataSeries series = new GroupDataSeries(filter.getLabel(), filter.getSize(), filter.getAreaId());
                if(filter.getType() == EnumPopulationFilterType.USER) {
                    for (int index = 0, count = filter.getSerialHashes().size(); index < count; index++) {
                        Scan scan = new Scan();
                        scan.setCaching(scanCacheSize);
                        scan.addFamily(columnFamily);

                        scan.setStartRow(createMeterRowKey(filter.getSerialHashes().get(index), endDate.getMillis()));
                        scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(createMeterRowKey(filter.getSerialHashes().get(index), startDate.getMillis())));

                        scanner = detailTable.getScanner(scan);

                        for (Result r = scanner.next(); r != null; r = scanner.next()) {
                            NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                            long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                            Float difference = null;

                            for (Entry<byte[], byte[]> entry : map.entrySet()) {
                                int offset = Bytes.toInt(Arrays.copyOfRange(entry.getKey(), 0, 4));
                                long timestamp = ((Long.MAX_VALUE / 1000L) - (timeBucket + (long) offset)) * 1000L;

                                if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
                                    int length = (int) Arrays.copyOfRange(entry.getKey(), 4, 5)[0];
                                    byte[] slice = Arrays.copyOfRange(entry.getKey(), 5, 5 + length);

                                    String columnQualifier = Bytes.toString(slice);
                                    if (columnQualifier.equals("d")) {
                                        difference = Bytes.toFloat(entry.getValue());
                                    }

                                    if (difference != null) {
                                        series.addMeterRankingDataPoint(
                                            query.getGranularity(),
                                            filter.getUserKeys().get(index),
                                            filter.getLabels().get(index),
                                            timestamp,
                                            difference,
                                            0,
                                            query.getMetrics(),
                                            query.getTimezone());
                                        difference = null;
                                    }

                                }
                            }
                        }
                        if (scanner != null) {
                            scanner.close();
                            scanner = null;
                        }
                    }

                    flatProjectSeries(query, filter, series);
                } else {
                    Scan scan = new Scan();
                    scan.setCaching(scanCacheSize);
                    scan.addFamily(columnFamily);

                    Long from = unixTimestampToLong(startDate.getMillis(), query.getTimezone(), query.getGranularity());
                    Long to = unixTimestampToLong(endDate.getMillis(), query.getTimezone(), query.getGranularity());

                    byte[] rowKey = createAggregateRowKey(md, filter.getGroupKey(), filter.getAreaKey(), query.getGranularity(), from);
                    scan.setStartRow(rowKey);

                    rowKey = createAggregateRowKey(md, filter.getGroupKey(), filter.getAreaKey(), query.getGranularity(), to);
                    scan.setStopRow(rowKey);

                    scanner = aggregateTable.getScanner(scan);

                    for (Result r = scanner.next(); r != null; r = scanner.next()) {
                        NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                        // The row key has the form [MD5 Hash(16), Aggregation Level(1), Time(8)]
                        long time = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 17, 25));

                        if ((from <= time) && (time <= to)) {
                            long timestamp = longToUnixTimestamp(time, query.getTimezone(), query.getGranularity());

                            DataPoint point;
                            if (filter.getRanking() == null) {
                                point = new MeterDataPoint(timestamp);
                            } else {
                                point = new RankingDataPoint(timestamp);
                            }
                            series.getPoints().add(point);

                            for (Entry<byte[], byte[]> entry : map.entrySet()) {
                                if (filter.getRanking() == null) {
                                    MeterDataPoint meterDataPoint = (MeterDataPoint) point;

                                    String columnQualifier = Bytes.toString(entry.getKey());
                                    switch (columnQualifier) {
                                        case "sum":
                                            if (query.getMetrics().contains(EnumMetric.SUM)) {
                                                meterDataPoint.getVolume().put(EnumMetric.SUM, (double) Bytes.toFloat(entry.getValue()));
                                            }
                                            break;
                                        case "avg":
                                            if (query.getMetrics().contains(EnumMetric.AVERAGE)) {
                                                meterDataPoint.getVolume().put(EnumMetric.AVERAGE, (double) Bytes.toFloat(entry.getValue()));
                                            }
                                            break;
                                        case "cnt":
                                            if (query.getMetrics().contains(EnumMetric.COUNT)) {
                                                meterDataPoint.getVolume().put(EnumMetric.COUNT, (double) Bytes.toInt(entry.getValue()));
                                            }
                                            break;
                                    }
                                } else {
                                    RankingDataPoint rankingDataPoint = (RankingDataPoint) point;

                                    String columnQualifier = Bytes.toString(entry.getKey());
                                    switch (columnQualifier) {
                                        case "top":
                                            if(filter.getRanking().getType() == EnumRankingType.TOP) {
                                                rankingDataPoint.getUsers().addAll(
                                                    parseMeterUserDataPoints(filter.getRanking().getType(),
                                                                             timestamp,
                                                                             new String(entry.getValue(), StandardCharsets.UTF_8),
                                                                             filter.getRanking().getLimit()));
                                            }
                                            break;
                                        case "bottom":
                                            if(filter.getRanking().getType() == EnumRankingType.BOTTOM) {
                                                rankingDataPoint.getUsers().addAll(
                                                    parseMeterUserDataPoints(filter.getRanking().getType(),
                                                                             timestamp,
                                                                             new String(entry.getValue(), StandardCharsets.UTF_8),
                                                                             filter.getRanking().getLimit()));
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    if (scanner != null) {
                        scanner.close();
                        scanner = null;
                    }
                }
                result.add(series);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                    scanner = null;
                }
                if (detailTable != null) {
                    detailTable.close();
                    detailTable = null;
                }
                if (aggregateTable != null) {
                    aggregateTable.close();
                    aggregateTable = null;
                }
            } catch (Exception ex) {
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }

        cleanSeries(query, result);

        return result;
    }

    private void cleanSeries(ExpandedDataQuery query, List<GroupDataSeries> result) {
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

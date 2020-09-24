package eu.daiad.common.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.hadoop.hbase.util.Bytes;

import eu.daiad.common.model.meter.WaterMeterMeasurement;
import eu.daiad.common.model.query.DataPoint;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.ExpandedDataQuery;
import eu.daiad.common.model.query.ExpandedPopulationFilter;
import eu.daiad.common.model.query.GroupDataSeries;
import eu.daiad.common.model.query.MeterUserDataPoint;
import eu.daiad.common.model.query.RankingDataPoint;
import eu.daiad.common.model.query.UserDataPoint;
import eu.daiad.common.repository.AbstractHBaseRepository;

public class AbstractHBaseMeterDataRepository  extends AbstractHBaseRepository {

    /**
     * The time interval used for grouping data in a single HBase row.
     */
    protected EnumTimeInterval interval;

    public AbstractHBaseMeterDataRepository() {

    }

    /**
     * Computes the key and the column qualifier prefix for an instance of
     * {@link WaterMeterMeasurement}.
     *
     * @param meterSerialHash the MD5 hash of the serial number.
     * @param timestamp the value timestamp.
     * @return an instance of {@link RowKeyQualifierPrefix}.
     */
    protected RowKeyQualifierPrefix createMeterRowKeyQualifierPrefix(byte[] meterSerialHash, long timestamp) {
        RowKeyQualifierPrefix key = new RowKeyQualifierPrefix();

        timestamp = (Long.MAX_VALUE / 1000) - (timestamp / 1000);

        long timeSlice;
        byte[] timeSliceBytes;

        switch(interval) {
            case HOUR:
                timeSlice = timestamp % interval.getValue();
                timeSliceBytes = Bytes.toBytes((short) timeSlice);
                if (timeSliceBytes.length != 2) {
                    throw new RuntimeException("Invalid byte array length!");
                }
                break;
            case DAY:
                timeSlice = timestamp % interval.getValue();
                timeSliceBytes = Bytes.toBytes((int) timeSlice);
                if (timeSliceBytes.length != 4) {
                    throw new RuntimeException("Invalid byte array length!");
                }
                break;
            default:
                throw new RuntimeException(String.format("Interval [%s] is not supported.", interval));
        }

        key.qualifierPrefix = timeSliceBytes;

        long timeBucket = timestamp - timeSlice;

        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
        if (timeBucketBytes.length != 8) {
            throw new RuntimeException("Invalid byte array length!");
        }

        key.rowKey = new byte[meterSerialHash.length + timeBucketBytes.length];
        System.arraycopy(meterSerialHash, 0, key.rowKey, 0, meterSerialHash.length);
        System.arraycopy(timeBucketBytes, 0, key.rowKey, meterSerialHash.length, timeBucketBytes.length);

        return key;
    }

    /**
     * Creates a row key for the given smart water meter serial number,
     * timestamp and time interval.
     *
     * @param meterSerialHash the MD5 hash of the serial number.
     * @param timestamp the timestamp.
     * @return a HBase row key.
     * @throws Exception if the time interval is not supported.
     */
    protected byte[] createMeterRowKey(byte[] meterSerialHash, long timestamp) throws Exception {
        return createMeterRowKeyQualifierPrefix(meterSerialHash, timestamp).rowKey;
    }

    /**
     * Transforms data series points depending on the query type. For ranking
     * queries, only the top/bottom k results are returned. For other queries
     * the {@link UserDataPoint} are aggregated to {@link DataPoint}.
     *
     * @param query the query.
     * @param filter the applied filter for the given series.
     * @param series the series to transform.
     */
    protected void flatProjectSeries(ExpandedDataQuery query, ExpandedPopulationFilter filter, GroupDataSeries series) {
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
    }

    /**
     * Computes the partitioned key and the column qualifier prefix for a
     * smart water meter reading.
     *
     * @param meterSerialHash the MD5 hash of the serial number.
     * @param timestamp the measurement timestamp.
     * @return an instance of {@link RowKeyQualifierPrefix}.
     */
    protected RowKeyQualifierPrefix createPartitionedRowKeyQualifierPrefix(byte[] meterSerialHash, long timestamp) {
        RowKeyQualifierPrefix key = new RowKeyQualifierPrefix();

        short partition = (short) (timestamp % timePartitions);
        byte[] partitionBytes = Bytes.toBytes(partition);

        timestamp = (Long.MAX_VALUE / 1000) - (timestamp / 1000);

        long timeSlice;
        byte[] timeSliceBytes;

        switch (interval) {
            case HOUR:
                timeSlice = timestamp % interval.getValue();
                timeSliceBytes = Bytes.toBytes((short) timeSlice);
                if (timeSliceBytes.length != 2) {
                    throw new RuntimeException("Invalid byte array length!");
                }
                break;
            case DAY:
                timeSlice = timestamp % interval.getValue();
                timeSliceBytes = Bytes.toBytes((int) timeSlice);
                if (timeSliceBytes.length != 4) {
                    throw new RuntimeException("Invalid byte array length!");
                }
                break;
            default:
                throw new RuntimeException(String.format("Interval [%s] is not supported.", interval));
        }

        key.qualifierPrefix = timeSliceBytes;

        long timeBucket = timestamp - timeSlice;

        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
        if (timeBucketBytes.length != 8) {
            throw new RuntimeException("Invalid byte array length!");
        }

        key.rowKey = new byte[partitionBytes.length + timeBucketBytes.length + meterSerialHash.length];

        System.arraycopy(partitionBytes, 0, key.rowKey, 0, partitionBytes.length);
        System.arraycopy(timeBucketBytes, 0, key.rowKey, partitionBytes.length, timeBucketBytes.length);
        System.arraycopy(meterSerialHash, 0, key.rowKey, (partitionBytes.length + timeBucketBytes.length), meterSerialHash.length);

        return key;
    }

    /**
     * Creates a row key for the given smart water meter serial number,
     * timestamp and time interval.
     *
     * @param partition partition index.
     * @param timestamp the timestamp.
     * @return a HBase row key.
     * @throws Exception if the time interval is not supported.
     */
    protected byte[] createPartitionedRowKey(short partition, long timestamp) throws Exception {
        byte[] partitionBytes = Bytes.toBytes(partition);

        timestamp = (Long.MAX_VALUE / 1000) - (timestamp / 1000);

        long timeBucket;

        switch (interval) {
            case HOUR:
                timeBucket = timestamp - (timestamp % interval.getValue());
                break;
            case DAY:
                timeBucket = timestamp - (timestamp % interval.getValue());
                break;
            default:
                throw new RuntimeException(String.format("Interval [%s] is not supported.", interval));
        }

        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
        if (timeBucketBytes.length != 8) {
            throw new RuntimeException("Invalid byte array length!");
        }

        byte[] rowKey = new byte[partitionBytes.length + timeBucketBytes.length];

        System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
        System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);

        return rowKey;
    }


    /**
     * Represents a HBase row key and column qualifier prefix.
     */
    protected static class RowKeyQualifierPrefix {

        byte[] rowKey;

        byte[] qualifierPrefix;

    }
}

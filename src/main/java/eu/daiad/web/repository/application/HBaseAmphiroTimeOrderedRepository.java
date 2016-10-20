package eu.daiad.web.repository.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import eu.daiad.web.hbase.HBaseConnectionManager;
import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.amphiro.AmphiroAbstractDataPoint;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroDataSeries;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionDetails;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.query.AmphiroDataPoint;
import eu.daiad.web.model.query.AmphiroUserDataPoint;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;
import eu.daiad.web.model.security.AuthenticatedUser;

@Repository()
public class HBaseAmphiroTimeOrderedRepository extends HBaseBaseRepository implements IAmphiroTimeOrderedRepository {

    private static final String dataSessionLoggerName = "AmphiroSessionLogger";
    
    private static final String dataMeasurementLoggerName = "AmphiroMeasurementLogger";

    private static final Log logger = LogFactory.getLog(HBaseAmphiroTimeOrderedRepository.class);

    private static final Log dataSessionLogger = LogFactory.getLog(dataSessionLoggerName);
    
    private static final Log dataMeasurementLogger = LogFactory.getLog(dataMeasurementLoggerName);

    private final String ERROR_RELEASE_RESOURCES = "Failed to release resources";

    private enum EnumTimeInterval {
        UNDEFINED(0), HOUR(3600), DAY(86400);

        private final int value;

        private EnumTimeInterval(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    @Value("${hbase.data.time.partitions}")
    private short timePartitions;

    @Value("${scanner.cache.size}")
    private int scanCacheSize = 1;

    private final String amphiroTableSessionIndex = "daiad:amphiro-sessions-index";

    private final String amphiroTableMeasurements = "daiad:amphiro-measurements";

    private final String amphiroTableSessionByTime = "daiad:amphiro-sessions-by-time";

    private final String amphiroTableSessionByUser = "daiad:amphiro-sessions-by-user";

    private final String columnFamilyName = "cf";

    @Autowired
    private HBaseConnectionManager connection;

    private void updateSessionIndex(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionIndex);

            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);
            byte[] columnQualifier = Bytes.toBytes("ts");

            for (int i = data.getSessions().size() - 1; i >= 0; i--) {
                AmphiroSession s = data.getSessions().get(i);

                byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
                byte[] userKeyHash = md.digest(userKeyBytes);

                byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                byte[] sessionIdBytes = Bytes.toBytes(s.getId());

                byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                Get get = new Get(rowKey);

                boolean isDelete = (!data.getSessions().get(i).isHistory())
                                && (data.getSessions().get(i).getDelete() != null);

                if ((table.get(get).getRow() == null) || (isDelete)) {
                    Put put = new Put(rowKey);
                    put.addColumn(columnFamily, columnQualifier, Bytes.toBytes(s.getTimestamp()));

                    table.put(put);
                } else {
                    data.getSessions().remove(i);
                    for (int j = data.getMeasurements().size() - 1; j >= 0; j--) {
                        if (data.getMeasurements().get(j).getSessionId() == s.getId()) {
                            data.getMeasurements().remove(j);
                        }
                    }
                }
            }
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    private void storeSessionByUser(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            for (int i = 0; i < data.getSessions().size(); i++) {
                AmphiroSession s = data.getSessions().get(i);

                long timestamp, timeBucket, offset;

                byte[] timeBucketBytes, rowKey;

                byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
                byte[] userKeyHash = md.digest(userKeyBytes);

                byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                byte[] sessionIdBytes = Bytes.toBytes(s.getId());

                // Delete existing record
                if ((!s.isHistory()) && (s.getDelete() != null)) {
                    timestamp = s.getDelete().getTimestamp() / 1000;
                    offset = timestamp % EnumTimeInterval.DAY.getValue();

                    timeBucket = timestamp - offset;
                    timeBucketBytes = Bytes.toBytes(timeBucket);

                    rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length
                                    + sessionIdBytes.length];

                    System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                    System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                    System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                    timeBucketBytes.length);
                    System.arraycopy(sessionIdBytes, 0, rowKey,
                                    (userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length),
                                    sessionIdBytes.length);

                    Delete delete = new Delete(rowKey);
                    table.delete(delete);
                }

                // Insert record
                timestamp = s.getTimestamp() / 1000;
                offset = timestamp % EnumTimeInterval.DAY.getValue();
                timeBucket = timestamp - offset;

                timeBucketBytes = Bytes.toBytes(timeBucket);

                rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length
                                + sessionIdBytes.length];

                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                timeBucketBytes.length);
                System.arraycopy(sessionIdBytes, 0, rowKey,
                                (userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length),
                                sessionIdBytes.length);

                Put put = new Put(rowKey);
                byte[] column;

                column = Bytes.toBytes("m:offset");
                put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

                column = Bytes.toBytes("m:t");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                column = Bytes.toBytes("m:v");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                column = Bytes.toBytes("m:f");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

                column = Bytes.toBytes("m:e");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                column = Bytes.toBytes("m:d");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                column = Bytes.toBytes("r:h");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.isHistory()));

                for (int p = 0, count = s.getProperties().size(); p < count; p++) {
                    column = Bytes.toBytes(s.getProperties().get(p).getKey());
                    put.addColumn(columnFamily, column, s.getProperties().get(p).getValue().getBytes(
                                    StandardCharsets.UTF_8));
                }

                table.put(put);
            }
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    private void storeSessionByTime(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByTime);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            for (int i = 0; i < data.getSessions().size(); i++) {
                AmphiroSession s = data.getSessions().get(i);

                long timestamp, offset, timeBucket;

                byte[] partitionBytes, timeBucketBytes, rowKey;

                byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
                byte[] userKeyHash = md.digest(userKeyBytes);

                byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                byte[] sessionIdBytes = Bytes.toBytes(s.getId());

                // Delete existing record
                if ((!s.isHistory()) && (s.getDelete() != null)) {
                    for (short partitionIndex = 0; partitionIndex < this.timePartitions; partitionIndex++) {
                        partitionBytes = Bytes.toBytes(partitionIndex);

                        timestamp = s.getDelete().getTimestamp() / 1000;
                        offset = timestamp % EnumTimeInterval.DAY.getValue();

                        timeBucket = timestamp - offset;
                        timeBucketBytes = Bytes.toBytes(timeBucket);

                        rowKey = new byte[partitionBytes.length + timeBucketBytes.length + userKeyHash.length
                                        + deviceKeyHash.length + sessionIdBytes.length];

                        System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                        System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
                        System.arraycopy(userKeyHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
                                        userKeyHash.length);
                        System.arraycopy(deviceKeyHash, 0, rowKey,
                                        (partitionBytes.length + timeBucketBytes.length + userKeyHash.length),
                                        deviceKeyHash.length);
                        System.arraycopy(sessionIdBytes, 0, rowKey, (partitionBytes.length + timeBucketBytes.length
                                        + userKeyHash.length + deviceKeyHash.length), sessionIdBytes.length);

                        Delete delete = new Delete(rowKey);
                        table.delete(delete);
                    }
                }

                // Insert new record

                short partition = (short) (s.getTimestamp() % this.timePartitions);
                partitionBytes = Bytes.toBytes(partition);

                timestamp = s.getTimestamp() / 1000;
                offset = timestamp % EnumTimeInterval.DAY.getValue();
                timeBucket = timestamp - offset;

                timeBucketBytes = Bytes.toBytes(timeBucket);

                rowKey = new byte[partitionBytes.length + timeBucketBytes.length + userKeyHash.length
                                + deviceKeyHash.length + sessionIdBytes.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
                System.arraycopy(userKeyHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
                                userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey,
                                (partitionBytes.length + timeBucketBytes.length + userKeyHash.length),
                                deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (partitionBytes.length + timeBucketBytes.length
                                + userKeyHash.length + deviceKeyHash.length), sessionIdBytes.length);

                Put put = new Put(rowKey);

                byte[] column;

                column = Bytes.toBytes("m:offset");
                put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

                column = Bytes.toBytes("m:t");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                column = Bytes.toBytes("m:v");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                column = Bytes.toBytes("m:f");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

                column = Bytes.toBytes("m:e");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                column = Bytes.toBytes("m:d");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                column = Bytes.toBytes("r:h");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.isHistory()));

                for (int p = 0, count = s.getProperties().size(); p < count; p++) {
                    column = Bytes.toBytes(s.getProperties().get(p).getKey());
                    put.addColumn(columnFamily, column, s.getProperties().get(p).getValue().getBytes(
                                    StandardCharsets.UTF_8));
                }

                table.put(put);
            }
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    private void logData(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data) {
        if (data.getSessions() != null) {
            for (AmphiroSession session : data.getSessions()) {
                List<String> tokens = new ArrayList<String>();

                tokens.add("v1");

                tokens.add(Integer.toString(user.getId()));
                tokens.add(user.getKey().toString());
                tokens.add(user.getUsername());

                tokens.add(Integer.toString(device.getId()));
                tokens.add(device.getKey().toString());

                tokens.add(Long.toString(session.getId()));
                tokens.add(Boolean.toString(session.isHistory()));
                tokens.add(Long.toString(session.getTimestamp()));
                tokens.add(Integer.toString(session.getDuration()));

                tokens.add(Float.toString(session.getVolume()));
                tokens.add(Float.toString(session.getEnergy()));
                tokens.add(Float.toString(session.getTemperature()));
                tokens.add(Float.toString(session.getFlow()));

                dataSessionLogger.info(StringUtils.join(tokens, ";"));
            }
        }
        if (data.getMeasurements() != null) {
            for (AmphiroMeasurement measurement : data.getMeasurements()) {
                List<String> tokens = new ArrayList<String>();

                tokens.add("v1");

                tokens.add(Integer.toString(user.getId()));
                tokens.add(user.getKey().toString());
                tokens.add(user.getUsername());

                tokens.add(Integer.toString(device.getId()));
                tokens.add(device.getKey().toString());

                tokens.add(Long.toString(measurement.getSessionId()));
                tokens.add(Integer.toString(measurement.getIndex()));
                tokens.add(Boolean.toString(measurement.isHistory()));
                tokens.add(Long.toString(measurement.getTimestamp()));

                tokens.add(Float.toString(measurement.getVolume()));
                tokens.add(Float.toString(measurement.getEnergy()));
                tokens.add(Float.toString(measurement.getTemperature()));

                dataMeasurementLogger.info(StringUtils.join(tokens, ";"));
            }
        }
    }

    private void preProcessData(AmphiroMeasurementCollection data) {
        try {
            // Sort sessions
            ArrayList<AmphiroSession> sessions = data.getSessions();

            Collections.sort(sessions, new Comparator<AmphiroSession>() {

                @Override
                public int compare(AmphiroSession s1, AmphiroSession s2) {
                    if (s1.getId() == s2.getId()) {
                        throw new RuntimeException("Session id must be unique.");
                    } else if (s1.getId() < s2.getId()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            // Check if historical data require a delete operation
            for (AmphiroSession s : sessions) {
                if ((s.isHistory()) && (s.getDelete() != null)) {
                    throw createApplicationException(DataErrorCode.DELETE_NOT_ALLOWED_FOR_HISTORY).set("session",
                                    s.getId());
                }
            }

            // Sort measurements
            ArrayList<AmphiroMeasurement> measurements = data.getMeasurements();

            if ((measurements != null) && (measurements.size() > 0)) {

                Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {

                    @Override
                    public int compare(AmphiroMeasurement m1, AmphiroMeasurement m2) {
                        if (m1.getSessionId() == m2.getSessionId()) {
                            if (m1.getIndex() == m2.getIndex()) {
                                throw new RuntimeException("Session measurement indexes must be unique.");
                            }
                            if (m1.getTimestamp() == m2.getTimestamp()) {
                                throw new RuntimeException("Session measurement timestamps must be unique.");
                            }
                            if (m1.getIndex() < m2.getIndex()) {
                                if (m1.getTimestamp() > m2.getTimestamp()) {
                                    throw new RuntimeException(
                                                    "Session measurements timestamp and index has ambiguous orderning.");
                                }
                                return -1;
                            } else {
                                if (m1.getTimestamp() < m2.getTimestamp()) {
                                    throw new RuntimeException(
                                                    "Session measurements timestamp and index has ambiguous orderning.");
                                }
                                return 1;
                            }
                        } else if (m1.getSessionId() < m2.getSessionId()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                // Compute difference for volume and energy
                for (int i = measurements.size() - 1; i > 0; i--) {
                    if (measurements.get(i).getSessionId() == measurements.get(i - 1).getSessionId()) {
                        // Set volume
                        float diff = measurements.get(i).getVolume() - measurements.get(i - 1).getVolume();
                        measurements.get(i).setVolume((float) Math.round(diff * 1000f) / 1000f);
                        // Set energy
                        diff = measurements.get(i).getEnergy() - measurements.get(i - 1).getEnergy();
                        measurements.get(i).setEnergy((float) Math.round(diff * 1000f) / 1000f);
                    }
                }

                // Set session for every measurement
                for (AmphiroMeasurement m : measurements) {
                    for (AmphiroSession s : sessions) {
                        if (m.getSessionId() == s.getId()) {
                            if (s.isHistory()) {
                                throw createApplicationException(DataErrorCode.HISTORY_SESSION_MEASUREMENT_FOUND).set(
                                                "session", m.getSessionId()).set("index", m.getIndex());
                            }
                            m.setSession(s);
                            break;
                        }
                    }
                    if (m.getSession() == null) {
                        throw createApplicationException(DataErrorCode.NO_SESSION_FOUND_FOR_MEASUREMENT).set("session",
                                        m.getSessionId()).set("index", m.getIndex());
                    }
                }
            }
        } catch (ApplicationException ex) {
            logger.warn(this.jsonToString(data));

            throw ex;
        }
    }

    private void storeMeasurements(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableMeasurements);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                AmphiroMeasurement m = data.getMeasurements().get(i);

                if (m.getVolume() < 0) {
                    continue;
                }

                byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
                byte[] userKeyHash = md.digest(userKeyBytes);

                byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                long timestamp = m.getTimestamp() / 1000;

                long timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
                byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);

                long timeBucket = timestamp - timeSlice;
                byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

                byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length];
                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                timeBucketBytes.length);

                Put p = new Put(rowKey);

                byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("s")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getSessionId()));

                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("i")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getIndex()));
                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));
                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("t")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getTemperature()));
                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("e")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getEnergy()));
                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("h")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.isHistory()));

                table.put(p);
            }
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    @Override
    public void storeData(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data)
                    throws ApplicationException {
        try {
            if ((data == null) || (data.getSessions() == null) || (data.getSessions().size() == 0)) {
                return;
            }

            this.logData(user, device, data);

            this.preProcessData(data);

            this.updateSessionIndex(user.getKey(), data);

            if (data.getSessions().size() > 0) {
                this.storeSessionByUser(user.getKey(), data);
                this.storeSessionByTime(user.getKey(), data);

                if ((data.getMeasurements() != null) && (data.getMeasurements().size() > 0)) {
                    this.storeMeasurements(user.getKey(), data);
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private byte[] getUserDeviceHourRowKey(byte[] userKeyHash, byte[] deviceKeyHash, DateTime date) throws Exception {
        return this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash, date.getMillis(), EnumTimeInterval.HOUR);
    }

    private byte[] getUserDeviceDayRowKey(byte[] userKeyHash, byte[] deviceKeyHash, DateTime date) throws Exception {
        return this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash, date.getMillis(), EnumTimeInterval.DAY);
    }

    private byte[] getUserDeviceTimeRowKey(byte[] userKeyHash, byte[] deviceKeyHash, long date,
                    EnumTimeInterval interval) throws Exception {

        long intervalInSeconds = EnumTimeInterval.HOUR.getValue();
        switch (interval) {
            case HOUR:
                intervalInSeconds = interval.getValue();
                break;
            case DAY:
                intervalInSeconds = interval.getValue();
                break;
            default:
                throw new RuntimeException(String.format("Time interval [%s] is not supported.", interval.toString()));
        }

        long timestamp = date / 1000;
        long timeSlice = timestamp % intervalInSeconds;
        long timeBucket = timestamp - timeSlice;
        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

        byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length];
        System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
        System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
        System.arraycopy(timeBucketBytes, 0, rowKey, (deviceKeyHash.length + deviceKeyHash.length),
                        timeBucketBytes.length);

        return rowKey;
    }

    private byte[] appendLength(byte[] array) throws Exception {
        byte[] length = { (byte) array.length };

        return concatenate(length, array);
    }

    private byte[] concatenate(byte[] a, byte[] b) {
        int lengthA = a.length;
        int lengthB = b.length;
        byte[] concat = new byte[lengthA + lengthB];
        System.arraycopy(a, 0, concat, 0, lengthA);
        System.arraycopy(b, 0, concat, lengthA, lengthB);
        return concat;
    }

    @Override
    public AmphiroMeasurementTimeIntervalQueryResult searchMeasurements(DateTimeZone timezone,
                    AmphiroMeasurementTimeIntervalQuery query) {
        AmphiroMeasurementTimeIntervalQueryResult data = new AmphiroMeasurementTimeIntervalQueryResult();

        DateTime startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
        DateTime endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);

        switch (query.getGranularity()) {
            case TemporalConstants.NONE:
                // Retrieve values at the highest granularity, that is at the
                // measurement level
                break;
            case TemporalConstants.HOUR:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(),
                                startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), endDate
                                .getHourOfDay(), 59, 59, DateTimeZone.UTC);
                break;
            case TemporalConstants.DAY:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
                                0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59,
                                59, DateTimeZone.UTC);
                break;
            case TemporalConstants.WEEK:
                DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0, 0,
                                DateTimeZone.UTC);
                endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59, 59,
                                DateTimeZone.UTC);
                break;
            case TemporalConstants.MONTH:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                .getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
                break;
            case TemporalConstants.YEAR:
                startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
                break;
            default:
                throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
                                query.getGranularity());
        }

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableMeasurements);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
            byte[] userKeyHash = md.digest(userKey);

            UUID deviceKeys[] = query.getDeviceKey();

            for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
                byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);
                scan.setStartRow(this.getUserDeviceHourRowKey(userKeyHash, deviceKeyHash, startDate));
                scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceHourRowKey(userKeyHash,
                                deviceKeyHash, endDate)));

                scanner = table.getScanner(scan);

                AmphiroDataSeries series = new AmphiroDataSeries(deviceKeys[deviceIndex], query.getGranularity());

                data.getSeries().add(series);

                ArrayList<eu.daiad.web.model.amphiro.AmphiroDataPoint> points = new ArrayList<eu.daiad.web.model.amphiro.AmphiroDataPoint>();

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

                    short offset = -1;
                    eu.daiad.web.model.amphiro.AmphiroDataPoint point = null;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        short entryOffset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

                        if (offset != entryOffset) {
                            if ((point != null) && (point.getTimestamp() >= startDate.getMillis())
                                            && (point.getTimestamp() <= endDate.getMillis())) {
                                points.add(point);
                            }
                            offset = entryOffset;
                            point = new eu.daiad.web.model.amphiro.AmphiroDataPoint();
                            point.setTimestamp((timeBucket + offset) * 1000L);
                        }

                        int length = Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                        byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);
                        String qualifier = Bytes.toString(slice);

                        switch (qualifier) {
                            case "h":
                                point.setHistory(Bytes.toBoolean(entry.getValue()));
                                break;
                            case "v":
                                point.setVolume(Bytes.toFloat(entry.getValue()));
                                break;
                            case "e":
                                point.setEnergy(Bytes.toFloat(entry.getValue()));
                                break;
                            case "t":
                                point.setTemperature(Bytes.toFloat(entry.getValue()));
                                break;
                            case "s":
                                point.setSessionId(Bytes.toLong(entry.getValue()));
                                break;
                            case "i":
                                point.setIndex(Bytes.toInt(entry.getValue()));
                                break;
                        }
                    }
                    if ((point != null) && (point.getTimestamp() >= startDate.getMillis())
                                    && (point.getTimestamp() <= endDate.getMillis())) {
                        points.add(point);
                    }
                }
                scanner.close();
                scanner = null;

                series.setPoints(points, timezone);

                Collections.sort(series.getPoints(), new Comparator<AmphiroAbstractDataPoint>() {

                    @Override
                    public int compare(AmphiroAbstractDataPoint o1, AmphiroAbstractDataPoint o2) {
                        if (o1.getTimestamp() <= o2.getTimestamp()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            }

            return data;
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    @Override
    public AmphiroSessionCollectionTimeIntervalQueryResult searchSessions(String[] names, DateTimeZone timezone,
                    AmphiroSessionCollectionTimeIntervalQuery query) {
        AmphiroSessionCollectionTimeIntervalQueryResult data = new AmphiroSessionCollectionTimeIntervalQueryResult();

        DateTime startDate = null;
        if (query.getStartDate() != null) {
            startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
        } else {
            startDate = new DateTime(0L, DateTimeZone.UTC);
        }
        DateTime endDate = null;
        if (query.getEndDate() != null) {
            endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);
        } else {
            endDate = new DateTime(DateTimeZone.UTC);
        }

        switch (query.getGranularity()) {
            case TemporalConstants.NONE:
                // Retrieve values at the highest granularity, that is at the
                // measurement level
                break;
            case TemporalConstants.HOUR:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(),
                                startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), endDate
                                .getHourOfDay(), 59, 59, DateTimeZone.UTC);
                break;
            case TemporalConstants.DAY:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
                                0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59,
                                59, DateTimeZone.UTC);
                break;
            case TemporalConstants.WEEK:
                DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0, 0,
                                DateTimeZone.UTC);
                endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59, 59,
                                DateTimeZone.UTC);
                break;
            case TemporalConstants.MONTH:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                .getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
                break;
            case TemporalConstants.YEAR:
                startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
                break;
            default:
                throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
                                query.getGranularity());
        }

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
            byte[] userKeyHash = md.digest(userKey);

            UUID deviceKeys[] = query.getDeviceKey();

            for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
                ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

                byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);
                scan.setStartRow(this.getUserDeviceDayRowKey(userKeyHash, deviceKeyHash, startDate));
                scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceDayRowKey(userKeyHash,
                                deviceKeyHash, endDate)));

                scanner = table.getScanner(scan);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

                    AmphiroSession session = new AmphiroSession();
                    session.setId(Bytes.toLong(Arrays.copyOfRange(r.getRow(), 40, 48)));

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {

                        String qualifier = Bytes.toString(entry.getKey());

                        switch (qualifier) {
                            case "m:offset":
                                int offset = Bytes.toInt(entry.getValue());
                                session.setTimestamp((timeBucket + offset) * 1000L);
                                break;
                            case "m:t":
                                session.setTemperature(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:v":
                                session.setVolume(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:f":
                                session.setFlow(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:e":
                                session.setEnergy(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:d":
                                session.setDuration(Bytes.toInt(entry.getValue()));
                                break;
                            case "r:h":
                                session.setHistory(Bytes.toBoolean(entry.getValue()));
                                break;
                            default:
                                session.addProperty(qualifier, new String(entry.getValue(), StandardCharsets.UTF_8));
                                break;
                        }
                    }

                    if ((session.getTimestamp() >= startDate.getMillis())
                                    && (session.getTimestamp() <= endDate.getMillis())) {
                        sessions.add(session);
                    }
                }

                scanner.close();
                scanner = null;

                AmphiroSessionCollection collection = new AmphiroSessionCollection(deviceKeys[deviceIndex],
                                names[deviceIndex], query.getGranularity());

                collection.addSessions(sessions, timezone);

                if (collection.getSessions().size() > 0) {
                    Collections.sort(collection.getSessions(), new Comparator<AmphiroAbstractSession>() {
                        @Override
                        public int compare(AmphiroAbstractSession o1, AmphiroAbstractSession o2) {
                            if (o1.getTimestamp() <= o2.getTimestamp()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });
                }

                data.getDevices().add(collection);
            }

            return data;
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    @Override
    public AmphiroSessionTimeIntervalQueryResult getSession(AmphiroSessionTimeIntervalQuery query) {
        AmphiroSessionTimeIntervalQueryResult data = new AmphiroSessionTimeIntervalQueryResult();

        // Compute temporal buffer
        DateTime startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
        DateTime endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
            byte[] userKeyHash = md.digest(userKey);

            byte[] deviceKey = query.getDeviceKey().toString().getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKey);

            Scan scan = new Scan();
            scan.addFamily(columnFamily);

            scan.setStartRow(this.getUserDeviceDayRowKey(userKeyHash, deviceKeyHash, startDate));
            scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceDayRowKey(userKeyHash,
                            deviceKeyHash, endDate)));

            scanner = table.getScanner(scan);

            for (Result r = scanner.next(); r != null; r = scanner.next()) {
                NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

                AmphiroSessionDetails session = new AmphiroSessionDetails();
                session.setId(Bytes.toLong(Arrays.copyOfRange(r.getRow(), 40, 48)));

                for (Entry<byte[], byte[]> entry : map.entrySet()) {

                    String qualifier = Bytes.toString(entry.getKey());

                    switch (qualifier) {
                        case "m:offset":
                            int offset = Bytes.toInt(entry.getValue());
                            session.setTimestamp((timeBucket + offset) * 1000L);
                            break;
                        case "m:t":
                            session.setTemperature(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:v":
                            session.setVolume(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:f":
                            session.setFlow(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:e":
                            session.setEnergy(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:d":
                            session.setDuration(Bytes.toInt(entry.getValue()));
                            break;
                        case "r:h":
                            session.setHistory(Bytes.toBoolean(entry.getValue()));
                            break;
                        default:
                            session.addProperty(qualifier, new String(entry.getValue(), StandardCharsets.UTF_8));
                            break;
                    }
                }

                if ((session.getTimestamp() >= startDate.getMillis())
                                && (session.getTimestamp() <= endDate.getMillis())
                                && (session.getId() == query.getSessionId())) {

                    session.setMeasurements(this.getSessionMeasurements(query));

                    data.setSession(session);
                    break;
                }
            }

            return data;
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    private ArrayList<AmphiroMeasurement> getSessionMeasurements(AmphiroSessionTimeIntervalQuery query) {
        ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

        DateTime startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
        DateTime endDate = (new DateTime(query.getEndDate(), DateTimeZone.UTC)).plusHours(12);

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableMeasurements);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
            byte[] userKeyKey = md.digest(userKey);

            byte[] deviceKey = query.getDeviceKey().toString().getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKey);

            Scan scan = new Scan();
            scan.addFamily(columnFamily);
            scan.setStartRow(this.getUserDeviceHourRowKey(userKeyKey, deviceKeyHash, startDate));
            scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceHourRowKey(userKeyKey,
                            deviceKeyHash, endDate)));

            scanner = table.getScanner(scan);

            for (Result r = scanner.next(); r != null; r = scanner.next()) {
                NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

                short offset = -1;

                AmphiroMeasurement measurement = null;

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    short entryOffset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

                    if (offset != entryOffset) {
                        if ((measurement != null) && (measurement.getSessionId() == query.getSessionId())) {
                            measurements.add(measurement);
                        }
                        offset = entryOffset;
                        measurement = new AmphiroMeasurement();
                        measurement.setTimestamp((timeBucket + offset) * 1000L);
                    }

                    int length = Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                    byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

                    String qualifier = Bytes.toString(slice);

                    switch (qualifier) {
                        case "h":
                            measurement.setHistory(Bytes.toBoolean(entry.getValue()));
                            break;
                        case "v":
                            measurement.setVolume(Bytes.toFloat(entry.getValue()));
                            break;
                        case "e":
                            measurement.setEnergy(Bytes.toFloat(entry.getValue()));
                            break;
                        case "t":
                            measurement.setTemperature(Bytes.toFloat(entry.getValue()));
                            break;
                        case "s":
                            measurement.setSessionId(Bytes.toLong(entry.getValue()));
                            break;
                        case "i":
                            measurement.setIndex(Bytes.toInt(entry.getValue()));
                            break;
                    }
                }
                if ((measurement != null) && (measurement.getSessionId() == query.getSessionId())) {
                    measurements.add(measurement);
                }
            }

            return measurements;
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    @Override
    public ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException {
        Table table = null;
        ResultScanner scanner = null;

        ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        for (ExpandedPopulationFilter filter : query.getGroups()) {
            result.add(new GroupDataSeries(filter.getLabel(), filter.getUsers().size(), filter.getAreaId()));
        }
        try {
            table = connection.getTable(this.amphiroTableSessionByTime);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

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
                scan.setCaching(this.scanCacheSize);
                scan.addFamily(columnFamily);

                byte[] partitionBytes = Bytes.toBytes(p);

                long from = startDate.getMillis() / 1000;
                from = from - (from % EnumTimeInterval.DAY.getValue());
                byte[] fromBytes = Bytes.toBytes(from);

                long to = endDate.getMillis() / 1000;
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

                    if (map != null) {
                        rowKey = r.getRow();

                        long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 2, 10));
                        byte[] userHash = Arrays.copyOfRange(r.getRow(), 10, 26);

                        long timestamp = 0;
                        int duration = 0;
                        float volume = 0, energy = 0, temperature = 0, flow = 0;

                        for (Entry<byte[], byte[]> entry : map.entrySet()) {
                            String qualifier = Bytes.toString(entry.getKey());

                            switch (qualifier) {
                                case "m:offset":
                                    timestamp = (timeBucket + Bytes.toInt(entry.getValue())) * 1000L;
                                    break;
                                case "m:v":
                                    volume = Bytes.toFloat(entry.getValue());
                                    break;
                                case "m:t":
                                    temperature = Bytes.toFloat(entry.getValue());
                                    break;
                                case "m:e":
                                    energy = Bytes.toFloat(entry.getValue());
                                    break;
                                case "m:f":
                                    flow = Bytes.toFloat(entry.getValue());
                                    break;
                                case "m:d":
                                    duration = Bytes.toInt(entry.getValue());
                                    break;
                                default:
                                    // Ignore
                                    break;
                            }
                        }

                        int filterIndex = 0;
                        for (ExpandedPopulationFilter filter : query.getGroups()) {
                            GroupDataSeries series = result.get(filterIndex);

                            int index = inArray(filter.getHashes(), userHash);
                            if (index >= 0) {
                                if (filter.getRanking() == null) {
                                    series.addAmhiroDataPoint(query.getGranularity(), timestamp, volume, energy,
                                                    duration, temperature, flow, query.getMetrics(), query
                                                                    .getTimezone());
                                } else {
                                    series.addAmphiroRankingDataPoint(query.getGranularity(), filter.getUsers().get(
                                                    index), filter.getLabels().get(index), timestamp, volume, energy,
                                                    duration, temperature, flow, query.getMetrics(), query
                                                                    .getTimezone());
                                }
                            }

                            filterIndex++;
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }

        // Post process results
        int filterIndex = 0;
        for (final ExpandedPopulationFilter filter : query.getGroups()) {
            GroupDataSeries series = result.get(filterIndex);

            if (filter.getRanking() != null) {
                // Truncate (n-k) users and keep top/bottom-k only
                for (DataPoint point : series.getPoints()) {
                    RankingDataPoint ranking = (RankingDataPoint) point;

                    final EnumDataField field = filter.getRanking().getField();

                    Collections.sort(ranking.getUsers(), new Comparator<UserDataPoint>() {

                        @Override
                        public int compare(UserDataPoint u1, UserDataPoint u2) {
                            AmphiroUserDataPoint m1 = (AmphiroUserDataPoint) u1;
                            AmphiroUserDataPoint m2 = (AmphiroUserDataPoint) u2;

                            switch (field) {
                                case VOLUME:
                                    if (m1.getVolume().get(filter.getRanking().getMetric()) < m2.getVolume().get(
                                                    filter.getRanking().getMetric())) {
                                        return -1;
                                    }
                                    if (m1.getVolume().get(filter.getRanking().getMetric()) > m2.getVolume().get(
                                                    filter.getRanking().getMetric())) {
                                        return 1;
                                    }

                                    break;
                                case ENERGY:
                                    if (m1.getEnergy().get(filter.getRanking().getMetric()) < m2.getEnergy().get(
                                                    filter.getRanking().getMetric())) {
                                        return -1;
                                    }
                                    if (m1.getEnergy().get(filter.getRanking().getMetric()) > m2.getEnergy().get(
                                                    filter.getRanking().getMetric())) {
                                        return 1;
                                    }

                                    break;
                                case DURATION:
                                    if (m1.getDuration().get(filter.getRanking().getMetric()) < m2.getDuration().get(
                                                    filter.getRanking().getMetric())) {
                                        return -1;
                                    }
                                    if (m1.getDuration().get(filter.getRanking().getMetric()) > m2.getDuration().get(
                                                    filter.getRanking().getMetric())) {
                                        return 1;
                                    }

                                    break;
                                case TEMPERATURE:
                                    if (m1.getTemperature().get(filter.getRanking().getMetric()) < m2.getTemperature()
                                                    .get(filter.getRanking().getMetric())) {
                                        return -1;
                                    }
                                    if (m1.getTemperature().get(filter.getRanking().getMetric()) > m2.getTemperature()
                                                    .get(filter.getRanking().getMetric())) {
                                        return 1;
                                    }

                                    break;
                                case FLOW:
                                    if (m1.getFlow().get(filter.getRanking().getMetric()) < m2.getFlow().get(
                                                    filter.getRanking().getMetric())) {
                                        return -1;
                                    }
                                    if (m1.getFlow().get(filter.getRanking().getMetric()) > m2.getFlow().get(
                                                    filter.getRanking().getMetric())) {
                                        return 1;
                                    }

                                    break;
                                default:
                                    break;
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
                for (Object p : series.getPoints()) {
                    AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) p;

                    amphiroDataPoint.getTemperature().remove(EnumMetric.SUM);
                    amphiroDataPoint.getFlow().remove(EnumMetric.SUM);
                }
            } else {
                for (Object p : series.getPoints()) {
                    RankingDataPoint rankingDataPoint = (RankingDataPoint) p;
                    for (UserDataPoint userDataPoint : rankingDataPoint.getUsers()) {
                        AmphiroUserDataPoint amphiroUserDataPoint = (AmphiroUserDataPoint) userDataPoint;

                        amphiroUserDataPoint.getTemperature().remove(EnumMetric.SUM);
                        amphiroUserDataPoint.getFlow().remove(EnumMetric.SUM);
                    }
                }
            }
            filterIndex++;
        }
    }

}

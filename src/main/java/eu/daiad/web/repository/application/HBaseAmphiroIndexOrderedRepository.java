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

import com.google.common.collect.ImmutableMap;

import eu.daiad.web.hbase.HBaseConnectionManager;
import eu.daiad.web.model.amphiro.AmphiroAbstractDataPoint;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroDataSeries;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionDeleteAction;
import eu.daiad.web.model.amphiro.AmphiroSessionDetails;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionUpdate;
import eu.daiad.web.model.amphiro.AmphiroSessionUpdateCollection;
import eu.daiad.web.model.amphiro.MemberAssignmentRequest;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.EnumMemberSelectionMode;
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
public class HBaseAmphiroIndexOrderedRepository extends HBaseBaseRepository implements IAmphiroIndexOrderedRepository {

    private static final String dataSessionLoggerName = "AmphiroSessionLogger";

    private static final String dataSessionMemberLoggerName = "AmphiroSessionMemberLogger";

    private static final String dataMeasurementLoggerName = "AmphiroMeasurementLogger";

    private static final Log logger = LogFactory.getLog(HBaseAmphiroIndexOrderedRepository.class);

    private static final Log dataSessionLogger = LogFactory.getLog(dataSessionLoggerName);
    
    private static final Log dataSessionMemberLogger = LogFactory.getLog(dataSessionMemberLoggerName);

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

    @Value("${daiad.amphiro.validation-string:true}")
    private boolean strictAmphiroValidation;

    private final String amphiroTableSessionIndex = "daiad:amphiro-sessions-index-v2";

    private final String amphiroTableMeasurements = "daiad:amphiro-measurements-v2";

    private final String amphiroTableSessionByTime = "daiad:amphiro-sessions-by-time-v2";

    private final String amphiroTableSessionByUser = "daiad:amphiro-sessions-by-user-v2";

    private final String columnFamilyName = "cf";

    @Autowired
    private HBaseConnectionManager connection;

    private void refreshSessionTimestampIndex(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionIndex);

            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);
            byte[] columnQualifier = Bytes.toBytes("ts");

            for (int i = 0, length = data.getSessions().size(); i < length; i++) {
                AmphiroSession s = data.getSessions().get(i);

                if (!s.isHistory()) {
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

                    // Get index entry for the specific session Id
                    Get get = new Get(rowKey);
                    Result existingIndexEntry = table.get(get);

                    if (existingIndexEntry.getRow() == null) {
                        Put put = new Put(rowKey);
                        put.addColumn(columnFamily, columnQualifier, Bytes.toBytes(s.getTimestamp()));

                        table.put(put);
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

    private void storeSessionByUser(UUID userKey, AmphiroMeasurementCollection data,
                    AmphiroSessionUpdateCollection updates) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            for (int i = data.getSessions().size() - 1; i >= 0; i--) {
                AmphiroSession s = data.getSessions().get(i);

                // Ignore any delete operation from the client. We have to set
                // this property to null since the client can always send an
                // invalid delete operation. We set this property manually only
                // when we want to replace a session explicitly.
                s.setDelete(null);

                byte[] rowKey;

                byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
                byte[] userKeyHash = md.digest(userKeyBytes);

                byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                byte[] sessionIdBytes = Bytes.toBytes(Long.MAX_VALUE - s.getId());

                // Construct row key
                rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                // Get existing row if any exists and set update properties
                Get get = new Get(rowKey);
                Result existingRow = table.get(get);

                boolean isExistingRowHistorical = false;
                Long existingRowTimestamp = null;

                if (existingRow.getRow() != null) {
                    NavigableMap<byte[], byte[]> map = existingRow.getFamilyMap(columnFamily);

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        String qualifier = Bytes.toString(entry.getKey());

                        switch (qualifier) {
                            case "s:h":
                                isExistingRowHistorical = Bytes.toBoolean(entry.getValue());
                                break;
                            case "s:t":
                                existingRowTimestamp = Bytes.toLong(entry.getValue());
                                break;
                        }

                    }
                }

                // Insert row
                Put put = new Put(rowKey);
                byte[] column;

                if (existingRow.getRow() == null) {
                    column = Bytes.toBytes("s:t");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                    column = Bytes.toBytes("s:h");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.isHistory()));

                    column = Bytes.toBytes("s:u");
                    put.addColumn(columnFamily, column, Bytes.toBytes(false));

                    column = Bytes.toBytes("m:v");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                    column = Bytes.toBytes("m:e");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                    column = Bytes.toBytes("m:d");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                    column = Bytes.toBytes("m:t");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                    column = Bytes.toBytes("m:f");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

                    if (s.getMember() != null) {
                        column = Bytes.toBytes("l:i");
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getIndex()));

                        column = Bytes.toBytes("l:m");
                        put.addColumn(columnFamily, column, s.getMember().getMode().toString().getBytes(StandardCharsets.UTF_8));

                        if (s.getMember().getTimestamp() == null) {
                            column = Bytes.toBytes("l:t");
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));
                        } else {
                            column = Bytes.toBytes("l:t");
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getTimestamp()));
                        }
                    }

                    for (int p = 0, count = s.getProperties().size(); p < count; p++) {
                        column = Bytes.toBytes(s.getProperties().get(p).getKey());
                        put.addColumn(columnFamily, column, s.getProperties().get(p).getValue().getBytes(
                                        StandardCharsets.UTF_8));
                    }

                    table.put(put);
                } else {
                    if (isExistingRowHistorical) {
                        if (s.isHistory()) {
                            // Keep the less recent time-stamp
                            if (s.getTimestamp() < existingRowTimestamp) {
                                column = Bytes.toBytes("s:t");
                                put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                                table.put(put);

                                // Propagate update to time indexed table
                                s.setDelete(new AmphiroSessionDeleteAction(existingRowTimestamp));
                            } else {
                                data.removeSession(i);
                            }
                        } else {
                            // Real-time session data delayed arrival

                            // Mark session as updated
                            column = Bytes.toBytes("s:u");
                            put.addColumn(columnFamily, column, Bytes.toBytes(true));

                            // Mark session as real-time
                            column = Bytes.toBytes("s:h");
                            put.addColumn(columnFamily, column, Bytes.toBytes(false));

                            // Update time stamp
                            column = Bytes.toBytes("s:t");
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                            // Store existing time stamp
                            column = Bytes.toBytes("h:t");
                            put.addColumn(columnFamily, column, Bytes.toBytes(existingRowTimestamp));

                            table.put(put);

                            // Propagate update to time indexed table
                            s.setDelete(new AmphiroSessionDeleteAction(existingRowTimestamp));
                        }
                    } else {
                        // Session already exists and is a real-time one!
                        updates.getUpdates().add(
                                        new AmphiroSessionUpdate(data.getDeviceKey(), s.getId(), existingRowTimestamp
                                                        .longValue()));

                        // Stop propagation to time indexed table
                        data.removeSession(i);
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
                if (s.getDelete() != null) {
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

                column = Bytes.toBytes("s:offset");
                put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

                column = Bytes.toBytes("m:v");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                column = Bytes.toBytes("m:e");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                column = Bytes.toBytes("m:d");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                column = Bytes.toBytes("m:t");  
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                column = Bytes.toBytes("m:f");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

                column = Bytes.toBytes("s:h");
                put.addColumn(columnFamily, column, Bytes.toBytes(s.isHistory()));

                if (s.getMember() != null) {
                    column = Bytes.toBytes("l:i");
                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getIndex()));

                    column = Bytes.toBytes("l:m");
                    put.addColumn(columnFamily, column, s.getMember().getMode().toString().getBytes(StandardCharsets.UTF_8));

                    if (s.getMember().getTimestamp() == null) {
                        column = Bytes.toBytes("l:t");
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));
                    } else {
                        column = Bytes.toBytes("l:t");
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getTimestamp()));
                    }
                }

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

                tokens.add("v2");

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
                
                if (session.getMember() == null) {
                    tokens.add("");
                    tokens.add("");
                    tokens.add("");
                } else {
                    tokens.add(Integer.toString(session.getMember().getIndex()));
                    tokens.add(session.getMember().getMode().toString());
                    if (session.getMember().getTimestamp() == null) {
                        tokens.add(Long.toString(session.getTimestamp()));
                    } else {
                        tokens.add(Long.toString(session.getMember().getTimestamp()));
                    }
                }

                dataSessionLogger.info(StringUtils.join(tokens, ";"));
            }
        }
        if (data.getMeasurements() != null) {
            for (AmphiroMeasurement measurement : data.getMeasurements()) {
                List<String> tokens = new ArrayList<String>();

                tokens.add("v2");

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

    private void logMemberAssignmentData(AuthenticatedUser user, List<MemberAssignmentRequest.Assignment> assignments) {
        if (assignments != null) {
            for (MemberAssignmentRequest.Assignment assignment : assignments) {
                List<String> tokens = new ArrayList<String>();

                tokens.add("v2");

                tokens.add(Integer.toString(user.getId()));
                tokens.add(user.getKey().toString());
                tokens.add(user.getUsername());

                tokens.add(assignment.getDeviceKey().toString());

                tokens.add("MANUAL");
                tokens.add(Long.toString(assignment.getSessionId()));
                tokens.add(Long.toString(assignment.getTimestamp()));
                tokens.add(Integer.toString(assignment.getMemberIndex()));

                dataSessionMemberLogger.info(StringUtils.join(tokens, ";"));
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

            // Sort measurements
            ArrayList<AmphiroMeasurement> measurements = data.getMeasurements();

            if ((measurements != null) && (measurements.size() > 0)) {

                Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {

                    @Override
                    public int compare(AmphiroMeasurement m1, AmphiroMeasurement m2) {
                        if (m1.getSessionId() == m2.getSessionId()) {
                            if (m1.getIndex() == m2.getIndex()) {
                                throw createApplicationException(DataErrorCode.MEASUREMENT_NO_UNIQUE_INDEX).set(
                                                "session", m1.getSessionId()).set("index", m1.getIndex());
                            }
                            if (m1.getIndex() < m2.getIndex()) {
                                return -1;
                            } else {
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
                for (int m = measurements.size() - 1; m >= 0; m--) {
                    boolean rejected = false;

                    // Find session
                    for (AmphiroSession s : sessions) {
                        if (measurements.get(m).getSessionId() == s.getId()) {
                            if (s.isHistory()) {
                                if (strictAmphiroValidation) {
                                    throw createApplicationException(DataErrorCode.HISTORY_SESSION_MEASUREMENT_FOUND)
                                                    .set("session", measurements.get(m).getSessionId()).set("index",
                                                                    measurements.get(m).getIndex());
                                } else {
                                    ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                                                    .put("index", measurements.get(m).getIndex()).put("session",
                                                                    measurements.get(m).getSessionId()).build();

                                    logger.warn(getMessage(DataErrorCode.HISTORY_SESSION_MEASUREMENT_FOUND, properties));

                                    measurements.remove(m);

                                    rejected = true;

                                    break;
                                }
                            }

                            measurements.get(m).setSession(s);
                            break;
                        }
                    }

                    // Ignore rejected measurements
                    if (rejected) {
                        continue;
                    }

                    // Check if measurement has no session
                    if (measurements.get(m).getSession() == null) {
                        if (strictAmphiroValidation) {
                            throw createApplicationException(DataErrorCode.NO_SESSION_FOUND_FOR_MEASUREMENT).set(
                                            "session", measurements.get(m).getSessionId()).set("index",
                                            measurements.get(m).getIndex());
                        } else {
                            ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder().put(
                                            "index", measurements.get(m).getIndex()).put("session",
                                            measurements.get(m).getSessionId()).build();

                            logger.warn(getMessage(DataErrorCode.NO_SESSION_FOUND_FOR_MEASUREMENT, properties));

                            measurements.remove(m);
                        }
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

                byte[] sessionIdBytes = Bytes.toBytes(Long.MAX_VALUE - m.getSessionId());
                byte[] indexBytes = Bytes.toBytes(m.getIndex());

                byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];
                byte[] column = null;

                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                Put p = new Put(rowKey);

                column = this.concatenate(indexBytes, this.appendLength(Bytes.toBytes("ts")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getTimestamp()));

                column = this.concatenate(indexBytes, this.appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

                column = this.concatenate(indexBytes, this.appendLength(Bytes.toBytes("e")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getEnergy()));

                column = this.concatenate(indexBytes, this.appendLength(Bytes.toBytes("t")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getTemperature()));

                column = this.concatenate(indexBytes, this.appendLength(Bytes.toBytes("h")));
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
    public AmphiroSessionUpdateCollection storeData(AuthenticatedUser user, AmphiroDevice device,
                    AmphiroMeasurementCollection data) throws ApplicationException {
        AmphiroSessionUpdateCollection updates = new AmphiroSessionUpdateCollection();

        try {
            if ((data != null) && (data.getSessions() != null) && (data.getSessions().size() != 0)) {
                this.logData(user, device, data);

                this.preProcessData(data);

                this.refreshSessionTimestampIndex(user.getKey(), data);

                this.storeSessionByUser(user.getKey(), data, updates);
                this.storeSessionByTime(user.getKey(), data);

                if ((data.getMeasurements() != null) && (data.getMeasurements().size() != 0)) {
                    this.storeMeasurements(user.getKey(), data);
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return updates;
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
    public AmphiroMeasurementIndexIntervalQueryResult searchMeasurements(DateTimeZone timezone,
                    AmphiroMeasurementIndexIntervalQuery query) {
        AmphiroMeasurementIndexIntervalQueryResult data = new AmphiroMeasurementIndexIntervalQueryResult();

        long startIndex = Long.MAX_VALUE;
        long endIndex = Long.MAX_VALUE;
        int maxTotalSessions = Integer.MAX_VALUE;

        switch (query.getType()) {
            case ABSOLUTE:
                startIndex -= query.getEndIndex();
                endIndex -= query.getStartIndex();
                break;
            case SLIDING:
                startIndex = 0;
                maxTotalSessions = query.getLength();
                break;
            default:
                return data;
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
                AmphiroDataSeries series = new AmphiroDataSeries(deviceKeys[deviceIndex]);

                data.getSeries().add(series);

                if (startIndex > endIndex) {
                    continue;
                }

                Scan scan = new Scan();
                scan.addFamily(columnFamily);

                byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                byte[] sessionIdBytes = Bytes.toBytes(startIndex);

                byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                scan.setStartRow(rowKey);

                sessionIdBytes = Bytes.toBytes(endIndex);

                rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(rowKey));

                scanner = table.getScanner(scan);

                ArrayList<eu.daiad.web.model.amphiro.AmphiroDataPoint> points = new ArrayList<eu.daiad.web.model.amphiro.AmphiroDataPoint>();

                int totalSessions = 0;
                long currentSessionId = -1;

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long sessionId = Long.MAX_VALUE - Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));
                    if (currentSessionId != sessionId) {
                        currentSessionId = sessionId;
                        totalSessions++;
                    }
                    if (totalSessions > maxTotalSessions) {
                        break;
                    }

                    int currentIndex = -1;
                    eu.daiad.web.model.amphiro.AmphiroDataPoint point = null;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        int currentColumnIndex = Bytes.toInt(Arrays.copyOfRange(entry.getKey(), 0, 4));

                        if (currentIndex != currentColumnIndex) {
                            currentIndex = currentColumnIndex;

                            point = new eu.daiad.web.model.amphiro.AmphiroDataPoint();
                            point.setSessionId(sessionId);
                            point.setIndex(currentIndex);

                            points.add(point);
                        }

                        int qualifierLength = Arrays.copyOfRange(entry.getKey(), 4, 5)[0];
                        byte[] qualifierBytes = Arrays.copyOfRange(entry.getKey(), 5, 5 + qualifierLength);
                        String qualifier = Bytes.toString(qualifierBytes);

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
                            case "ts":
                                point.setTimestamp(Bytes.toLong(entry.getValue()));
                                break;
                        }
                    }
                }

                scanner.close();
                scanner = null;

                series.setPoints(points, timezone);

                Collections.sort(series.getPoints(), new Comparator<AmphiroAbstractDataPoint>() {

                    @Override
                    public int compare(AmphiroAbstractDataPoint o1, AmphiroAbstractDataPoint o2) {
                        eu.daiad.web.model.amphiro.AmphiroDataPoint m1 = (eu.daiad.web.model.amphiro.AmphiroDataPoint) o1;
                        eu.daiad.web.model.amphiro.AmphiroDataPoint m2 = (eu.daiad.web.model.amphiro.AmphiroDataPoint) o2;

                        if (m1.getSessionId() < m2.getSessionId()) {
                            return -1;
                        } else if (m1.getSessionId() > m2.getSessionId()) {
                            return 1;
                        }
                        if (m1.getIndex() < m2.getIndex()) {
                            return -1;
                        } else if (m1.getIndex() < m2.getIndex()) {
                            return 1;
                        }
                        return 0;
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
    public AmphiroSessionCollectionIndexIntervalQueryResult searchSessions(String[] names, DateTimeZone timezone,
                    AmphiroSessionCollectionIndexIntervalQuery query) {
        AmphiroSessionCollectionIndexIntervalQueryResult data = new AmphiroSessionCollectionIndexIntervalQueryResult();

        long startIndex = Long.MAX_VALUE;
        long endIndex = Long.MAX_VALUE;
        int maxTotalSessions = Integer.MAX_VALUE;

        switch (query.getType()) {
            case ABSOLUTE:
                startIndex -= query.getEndIndex();
                endIndex -= query.getStartIndex();
                break;
            case SLIDING:
                startIndex = 0;
                maxTotalSessions = query.getLength();
                break;
            default:
                return data;
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
                int totalSessions = 0;

                AmphiroSessionCollection collection = new AmphiroSessionCollection(deviceKeys[deviceIndex],
                                names[deviceIndex]);

                data.getDevices().add(collection);

                if (startIndex > endIndex) {
                    continue;
                }

                ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

                byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
                byte[] deviceKeyHash = md.digest(deviceKey);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);

                byte[] sessionIdBytes = Bytes.toBytes(startIndex);

                byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];
                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                scan.setStartRow(rowKey);

                sessionIdBytes = Bytes.toBytes(endIndex);

                rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];
                System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
                System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
                System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                                sessionIdBytes.length);

                scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(rowKey));

                scanner = table.getScanner(scan);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long sessionId = Long.MAX_VALUE - Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

                    AmphiroSession session = new AmphiroSession();
                    session.setId(sessionId);

                    Integer memberIndex = null;
                    EnumMemberSelectionMode memberMode = EnumMemberSelectionMode.AUTO;
                    Long memberTimestamp = null;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {

                        String qualifier = Bytes.toString(entry.getKey());

                        switch (qualifier) {
                            case "s:t":
                                session.setTimestamp(Bytes.toLong(entry.getValue()));
                                break;
                            case "s:h":
                                session.setHistory(Bytes.toBoolean(entry.getValue()));
                                break;
                            case "s:u":
                                // Ignore
                                break;
                            case "h:t":
                                // Ignore
                                break;
                            case "m:v":
                                session.setVolume(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:e":
                                session.setEnergy(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:d":
                                session.setDuration(Bytes.toInt(entry.getValue()));
                                break;
                            case "m:t":
                                session.setTemperature(Bytes.toFloat(entry.getValue()));
                                break;
                            case "m:f":
                                session.setFlow(Bytes.toFloat(entry.getValue()));
                                break;
                            case "l:i":
                                memberIndex = Bytes.toInt(entry.getValue());
                                break;
                            case "l:m":
                                memberMode = EnumMemberSelectionMode.fromString(new String(entry.getValue(), StandardCharsets.UTF_8));
                                break;
                            case "l:t":
                                memberTimestamp = Bytes.toLong(entry.getValue());
                                break;
                            default:
                                session.addProperty(qualifier, new String(entry.getValue(), StandardCharsets.UTF_8));
                                break;
                        }
                    }

                    if (totalSessions < maxTotalSessions) {
                        if (memberIndex != null) {
                            session.setMember(new AmphiroSession.Member());
                            
                            session.getMember().setIndex(memberIndex);
                            session.getMember().setMode(memberMode);
                            session.getMember().setTimestamp(memberTimestamp == null ? session.getTimestamp() : memberTimestamp);
                        }

                        
                        sessions.add(session);
                        totalSessions++;
                    }
                }

                scanner.close();
                scanner = null;

                collection.addSessions(sessions, timezone);

                if (collection.getSessions().size() > 0) {
                    Collections.sort(collection.getSessions(), new Comparator<AmphiroAbstractSession>() {
                        @Override
                        public int compare(AmphiroAbstractSession o1, AmphiroAbstractSession o2) {
                            AmphiroSession s1 = (AmphiroSession) o1;
                            AmphiroSession s2 = (AmphiroSession) o2;

                            if (s1.getId() < s2.getId()) {
                                return -1;
                            } else if (s1.getId() > s2.getId()) {
                                return 1;
                            }
                            return 0;
                        }
                    });
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

    @Override
    public AmphiroSessionIndexIntervalQueryResult getSession(AmphiroSessionIndexIntervalQuery query) {
        AmphiroSessionIndexIntervalQueryResult data = new AmphiroSessionIndexIntervalQueryResult();

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

            byte[] sessionIdBytes = Bytes.toBytes(Long.MAX_VALUE - query.getSessionId());

            byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

            System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
            System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
            System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                            sessionIdBytes.length);

            Get get = new Get(rowKey);
            Result sessionResult = table.get(get);

            if (sessionResult.getRow() != null) {
                NavigableMap<byte[], byte[]> map = sessionResult.getFamilyMap(columnFamily);

                AmphiroSessionDetails session = new AmphiroSessionDetails();
                session.setId(query.getSessionId());

                Integer memberIndex = null;
                EnumMemberSelectionMode memberMode = EnumMemberSelectionMode.AUTO;
                Long memberTimestamp = null;
                
                for (Entry<byte[], byte[]> entry : map.entrySet()) {

                    String qualifier = Bytes.toString(entry.getKey());

                    switch (qualifier) {
                        case "s:t":
                            session.setTimestamp(Bytes.toLong(entry.getValue()));
                            break;
                        case "s:h":
                            session.setHistory(Bytes.toBoolean(entry.getValue()));
                            break;
                        case "s:u":
                            // Ignore
                            break;
                        case "h:t":
                            // Ignore
                            break;
                        case "m:v":
                            session.setVolume(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:e":
                            session.setEnergy(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:d":
                            session.setDuration(Bytes.toInt(entry.getValue()));
                            break;
                        case "m:t":
                            session.setTemperature(Bytes.toFloat(entry.getValue()));
                            break;
                        case "m:f":
                            session.setFlow(Bytes.toFloat(entry.getValue()));
                            break;
                        case "l:i":
                            memberIndex = Bytes.toInt(entry.getValue());
                            break;
                        case "l:m":
                            memberMode = EnumMemberSelectionMode.fromString(new String(entry.getValue(), StandardCharsets.UTF_8));
                            break;
                        case "l:t":
                            memberTimestamp = Bytes.toLong(entry.getValue());
                            break;
                        default:
                            session.addProperty(qualifier, new String(entry.getValue(), StandardCharsets.UTF_8));
                            break;
                    }
                }

                if (memberIndex != null) {
                    session.setMember(new AmphiroSession.Member());
                    
                    session.getMember().setIndex(memberIndex);
                    session.getMember().setMode(memberMode);
                    session.getMember().setTimestamp(memberTimestamp == null ? session.getTimestamp() : memberTimestamp);
                }
                
                if (!query.isExcludeMeasurements()) {
                    session.setMeasurements(this.getSessionMeasurements(query));
                }
                data.setSession(session);
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

    private ArrayList<AmphiroMeasurement> getSessionMeasurements(AmphiroSessionIndexIntervalQuery query) {
        ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableMeasurements);

            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
            byte[] userKeyHash = md.digest(userKey);

            byte[] deviceKey = query.getDeviceKey().toString().getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKey);

            byte[] sessionIdBytes = Bytes.toBytes(Long.MAX_VALUE - query.getSessionId());

            byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

            System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
            System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
            System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                            sessionIdBytes.length);

            Get get = new Get(rowKey);
            Result sessionResult = table.get(get);

            int currentIndex = -1;

            AmphiroMeasurement measurement = null;

            if (sessionResult.getRow() != null) {
                NavigableMap<byte[], byte[]> map = sessionResult.getFamilyMap(columnFamily);

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    int currentColumnIndex = Bytes.toInt(Arrays.copyOfRange(entry.getKey(), 0, 4));

                    if (currentIndex != currentColumnIndex) {
                        if (measurement != null) {
                            measurements.add(measurement);
                        }
                        currentIndex = currentColumnIndex;

                        measurement = new AmphiroMeasurement();
                        measurement.setSessionId(query.getSessionId());
                        measurement.setIndex(currentIndex);
                    }

                    int qualifierLength = Arrays.copyOfRange(entry.getKey(), 4, 5)[0];
                    byte[] qualifierBytes = Arrays.copyOfRange(entry.getKey(), 5, 5 + qualifierLength);
                    String qualifier = Bytes.toString(qualifierBytes);

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
                        case "ts":
                            measurement.setTimestamp(Bytes.toLong(entry.getValue()));
                            break;
                    }
                }
                if (measurement != null) {
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
                                case "s:offset":
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


    @Override
    public void assignMemberToSession(AuthenticatedUser user,  List<MemberAssignmentRequest.Assignment> assignments) throws Exception {
        if (assignments != null) {
            logMemberAssignmentData(user, assignments);
            
            for (MemberAssignmentRequest.Assignment assignment : assignments) {
                AmphiroSessionIndexIntervalQuery query = new AmphiroSessionIndexIntervalQuery();
                
                query.setDeviceKey(assignment.getDeviceKey());
                query.setSessionId(assignment.getSessionId());
                query.setUserKey(user.getKey());
                query.setExcludeMeasurements(true);
                
                AmphiroSessionIndexIntervalQueryResult result = this.getSession(query);
                if(result.getSession() == null) {
                    throw createApplicationException(DataErrorCode.SESSION_NOT_FOUND).set("session", assignment.getSessionId());
                }
                
                assignMeterToSessionInUserTable(user.getKey(), assignment, result.getSession());
                assignMeterToSessionInTimeTable(user.getKey(), assignment, result.getSession());
            }
        }
    }
    
    private void assignMeterToSessionInUserTable(UUID userKey, 
                                                 MemberAssignmentRequest.Assignment assignment, 
                                                 AmphiroSessionDetails session) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] rowKey;

            byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
            byte[] userKeyHash = md.digest(userKeyBytes);

            byte[] deviceKey = assignment.getDeviceKey().toString().getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKey);

            byte[] sessionIdBytes = Bytes.toBytes(Long.MAX_VALUE - assignment.getSessionId());

            // Construct row key
            rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

            System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
            System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
            System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
                            sessionIdBytes.length);

            // Update row
            Put put = new Put(rowKey);
            byte[] column;

            column = Bytes.toBytes("l:i");
            put.addColumn(columnFamily, column, Bytes.toBytes(assignment.getMemberIndex()));

            column = Bytes.toBytes("l:m");
            put.addColumn(columnFamily, column, EnumMemberSelectionMode.MANUAL.toString().getBytes(StandardCharsets.UTF_8));

            if (assignment.getTimestamp() == null) {
                column = Bytes.toBytes("l:t");
                put.addColumn(columnFamily, column, Bytes.toBytes(session.getTimestamp()));
            } else {
                column = Bytes.toBytes("l:t");
                put.addColumn(columnFamily, column, Bytes.toBytes(assignment.getTimestamp()));
            }
            
            table.put(put);
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
    
    private void assignMeterToSessionInTimeTable(UUID userKey, 
                                                 MemberAssignmentRequest.Assignment assignment, 
                                                 AmphiroSessionDetails session) throws Exception {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.amphiroTableSessionByTime);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            long timestamp, offset, timeBucket;

            byte[] partitionBytes, timeBucketBytes, rowKey;

            byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
            byte[] userKeyHash = md.digest(userKeyBytes);

            byte[] deviceKey = assignment.getDeviceKey().toString().getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKey);

            byte[] sessionIdBytes = Bytes.toBytes(assignment.getSessionId());

            short partition = (short) (session.getTimestamp() % this.timePartitions);
            partitionBytes = Bytes.toBytes(partition);

            timestamp = session.getTimestamp() / 1000;
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

            column = Bytes.toBytes("l:i");
            put.addColumn(columnFamily, column, Bytes.toBytes(assignment.getMemberIndex()));

            column = Bytes.toBytes("l:m");
            put.addColumn(columnFamily, column, EnumMemberSelectionMode.MANUAL.toString().getBytes(StandardCharsets.UTF_8));

            if (assignment.getTimestamp() == null) {
                column = Bytes.toBytes("l:t");
                put.addColumn(columnFamily, column, Bytes.toBytes(session.getTimestamp()));
            } else {
                column = Bytes.toBytes("l:t");
                put.addColumn(columnFamily, column, Bytes.toBytes(assignment.getTimestamp()));
            }

            table.put(put);
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

package eu.daiad.web.repository.application;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

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
import eu.daiad.web.model.amphiro.AmphiroSessionDetails;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionUpdate;
import eu.daiad.web.model.amphiro.AmphiroSessionUpdateCollection;
import eu.daiad.web.model.amphiro.IgnoreShowerRequest;
import eu.daiad.web.model.amphiro.MemberAssignmentRequest;
import eu.daiad.web.model.amphiro.SessionVersions;
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

/*
 * Repository for managing amphiro b1 data.
 */
@Primary()
@Repository()
public class HBaseAmphiroRepository extends AbstractAmphiroHBaseRepository implements IAmphiroIndexOrderedRepository {

    /**
     * Prefix for real-time session property names.
     */
    private static final String COLUMN_RT_PROPERTY_PREFIX = "r:s:p:";

    /**
     * Prefix for historical session property names.
     */
    private static final String COLUMN_HIST_PROPERTY_PREFIX = "h:s:p:";

    /**
     * HBase row holding amphiro b1 counters.
     */
    private static final String COUNTER_GROUP = "Amphiro";

    /**
     * HBase counter for measurement index.
     */
    private static final String COUNTER_MEASUREMENT = "MeasurementIndex";

    /**
     * HBase session table columns.
     */
    private static enum EnumSessionColumn {
        COLUMN_UNKNOWN(null),

        COLUMN_RT_SESSION_TIMESTAMP("r:s:timestamp"),
        COLUMN_RT_SESSION_DURATION("r:s:duration"),
        COLUMN_RT_SESSION_VOLUME("r:s:volume"),
        COLUMN_RT_SESSION_ENERGY("r:s:energy"),
        COLUMN_RT_SESSION_TEMPERATURE("r:s:temperature"),
        COLUMN_RT_SESSION_FLOW("r:s:flow"),

        COLUMN_HIST_SESSION_TIMESTAMP("h:s:timestamp"),
        COLUMN_HIST_SESSION_DURATION("h:s:duration"),
        COLUMN_HIST_SESSION_VOLUME("h:s:volume"),
        COLUMN_HIST_SESSION_ENERGY("h:s:energy"),
        COLUMN_HIST_SESSION_TEMPERATURE("h:s:temperature"),
        COLUMN_HIST_SESSION_FLOW("h:s:flow"),

        COLUMN_SHARED_MEMBER_INDEX("s:m:index"),
        COLUMN_SHARED_MEMBER_MODE("s:m:mode"),
        COLUMN_SHARED_MEMBER_TIMESTAMP("h:m:timestamp"),

        COLUMN_SHARED_IGNORE_VALUE("s:i:ignore"),
        COLUMN_SHARED_IGNORE_TIMESTAMP("s:i:timestamp");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumSessionColumn(String value) {
            this.value = value;
        }

        public static final Map<String, EnumSessionColumn> stringToTypeMap = new HashMap<String, EnumSessionColumn>();
        static {
            for (EnumSessionColumn type : EnumSessionColumn.values()) {
                if (stringToTypeMap.containsKey(type.value)) {
                    throw new RuntimeException(String.format("HBase column [%s] already defined.", type.value));
                }
                stringToTypeMap.put(type.value, type);
            }
        }

    };

    /**
     * HBase measurement table columns.
     */
    private static enum EnumMeasurementColumn {
        COLUMN_MEASUREMENT_TIMESTAMP("m:timestamp"),
        COLUMN_MEASUREMENT_VOLUME("m:volume"),
        COLUMN_MEASUREMENT_ENERGY("m:energy"),
        COLUMN_MEASUREMENT_TEMPERATURE("m:temperature");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumMeasurementColumn(String value) {
            this.value = value;
        }

        public static final Map<String, EnumMeasurementColumn> stringToTypeMap = new HashMap<String, EnumMeasurementColumn>();
        static {
            for (EnumMeasurementColumn type : EnumMeasurementColumn.values()) {
                if (stringToTypeMap.containsKey(type.value)) {
                    throw new RuntimeException(String.format("HBase column [%s] already defined.", type.value));
                }
                stringToTypeMap.put(type.value, type);
            }
        }

    };

    /**
     * Returns HBase session table column enumeration value for a given name.
     *
     * @param value the column name.
     * @param throwException throws an exception if the column does not exist in the enumeration.
     * @return the associated enumeration value.
     */
    private EnumSessionColumn getSessionColumn(String value, boolean throwException) {
        EnumSessionColumn type = EnumSessionColumn.stringToTypeMap.get(value);
        if (type == null) {
            if (throwException) {
                throw createApplicationException(DataErrorCode.HBASE_INVALID_COLUMN).set("column", value);
            }
            return EnumSessionColumn.COLUMN_UNKNOWN;
        }
        return type;
    }

    /**
     * Returns HBase measurement table column enumeration value for a given
     * name.
     *
     * @param value the column name.
     * @return the associated enumeration value.
     */
    private EnumMeasurementColumn getMeasurementColumn(String value) {
        EnumMeasurementColumn type = EnumMeasurementColumn.stringToTypeMap.get(value);
        if (type == null) {
            throw createApplicationException(DataErrorCode.HBASE_INVALID_COLUMN).set("column", value);
        }
        return type;
    }

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(HBaseAmphiroRepository.class);

    /**
     * HBase table for storing amphiro b1 real-time session measurements.
     */
    private final String amphiroTableMeasurements = "daiad:amphiro-measurements-v3";

    /**
     * HBase table for storing amphiro b1 sessions indexed by their timestamp.
     */
    private final String amphiroTableSessionByTime = "daiad:amphiro-sessions-by-time-v3";

    /**
     * HBase table for storing amphiro b1 sessions indexed by their id.
     */
    private final String amphiroTableSessionByUser = "daiad:amphiro-sessions-by-user-v3";

    /**
     * Returns the current API version.
     *
     * @return the API version.
     */
    @Override
    protected String getVersion() {
        return "v3";
    }

    /**
     * Stores session and measurement data for an amphiro b1 device.
     *
     * @param user the owner of the device.
     * @param device the device.
     * @param data a collection of amphiro b1 sessions and measurement time series.
     * @return any sessions that have been updated.
     * @throws ApplicationException if saving data has failed.
     */
    @Override
    public AmphiroSessionUpdateCollection store(AuthenticatedUser user, AmphiroDevice device,
                    AmphiroMeasurementCollection data) throws ApplicationException {
        AmphiroSessionUpdateCollection updates = new AmphiroSessionUpdateCollection();

        if ((data == null) || (data.getSessions() == null) || (data.getSessions().isEmpty())) {
            return updates;
        }

        try {
            logStoreOperation(user, device, data);

            preProcessData(user, device, data);

            storeSessionByUser(user.getKey(), data, updates);
            storeSessionByTime(user.getKey(), data);

            if ((data.getMeasurements() != null) && (!data.getMeasurements().isEmpty())) {
                storeMeasurements(user.getKey(), data);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return updates;
    }

    /**
     * Logs amphiro b1 sessions and measurement time series to external files
     * before saving them to HBase.
     *
     * @param user the owner of the device.
     * @param device the device which generated the data.
     * @param data the data to log.
     */
    private void logStoreOperation(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data) {
        for (AmphiroSession session : data.getSessions()) {
            List<String> tokens = new ArrayList<String>();

            tokens.add(getVersion());

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
                    tokens.add("");
                } else {
                    tokens.add(Long.toString(session.getMember().getTimestamp()));
                }
            }

            sessionLogger.info(StringUtils.join(tokens, ";"));
        }

        if (data.getMeasurements() == null) {
            return;
        }

        for (AmphiroMeasurement measurement : data.getMeasurements()) {
            List<String> tokens = new ArrayList<String>();

            tokens.add(getVersion());

            tokens.add(Integer.toString(user.getId()));
            tokens.add(user.getKey().toString());
            tokens.add(user.getUsername());

            tokens.add(Integer.toString(device.getId()));
            tokens.add(device.getKey().toString());

            tokens.add(Long.toString(measurement.getSessionId()));
            tokens.add(Long.toString(measurement.getIndex()));
            tokens.add(Boolean.toString(measurement.isHistory()));
            tokens.add(Long.toString(measurement.getTimestamp()));

            tokens.add(Float.toString(measurement.getVolume()));
            tokens.add(Float.toString(measurement.getEnergy()));
            tokens.add(Float.toString(measurement.getTemperature()));

            sessionMeasurementLogger.info(StringUtils.join(tokens, ";"));
        }
    }

    /**
     * Processes data before inserting it to HBase.
     *
     * @param user the owner of the device.
     * @param device the device which generated the data.
     * @param data the amphiro b1 data to process.
     * @throws NoSuchAlgorithmException if hash algorithm is not supported.
     * @throws UnsupportedEncodingException if encoding is not supported.
     */
    private void preProcessData(final AuthenticatedUser user, final AmphiroDevice device,
                    AmphiroMeasurementCollection data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        try {
            // Ignore any delete operation from the client. We have to set this
            // property to null since the client can always send an invalid
            // delete operation.
            for (int i = 0, count = data.getSessions().size(); i < count; i++) {
                data.getSessions().get(i).setDelete(null);
            }

            // Sort sessions
            List<AmphiroSession> sessions = data.getSessions();

            Collections.sort(sessions, new Comparator<AmphiroSession>() {

                @Override
                public int compare(AmphiroSession s1, AmphiroSession s2) {
                    if (s1.getId() == s2.getId()) {
                        if ((s1.isHistory() && s2.isHistory()) ||
                            (!s1.isHistory() && !s2.isHistory())) {
                            throw createApplicationException(DataErrorCode.DUPLICATE_SESSION_ID)
                                    .set("id", s1.getId())
                                    .set("deviceName", device.getName())
                                    .set("deviceKey", device.getKey().toString())
                                    .set("username", user.getUsername());
                        }
                        return 0;
                    } else if (s1.getId() < s2.getId()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }

            });

            // Sort measurements
            List<AmphiroMeasurement> measurements = data.getMeasurements();

            if ((measurements != null) && (!measurements.isEmpty())) {

                Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {

                    @Override
                    public int compare(AmphiroMeasurement m1, AmphiroMeasurement m2) {
                        if (m1.getSessionId() == m2.getSessionId()) {
                            if (m1.getIndex() == m2.getIndex()) {
                                throw createApplicationException(DataErrorCode.MEASUREMENT_NO_UNIQUE_INDEX)
                                    .set("session", m1.getSessionId())
                                    .set("index", m1.getIndex());
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

                // Set session for every measurement
                for (int m = measurements.size() - 1; m >= 0; m--) {
                    boolean removed = false;

                    // Find session
                    for (AmphiroSession s : sessions) {
                        if (measurements.get(m).getSessionId() == s.getId()) {
                            if (s.isHistory()) {
                                if (strictAmphiroValidation) {
                                    throw createApplicationException(DataErrorCode.HISTORY_SESSION_MEASUREMENT_FOUND)
                                        .set("session", measurements.get(m).getSessionId())
                                        .set("index", measurements.get(m).getIndex());
                                } else {
                                    ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                                        .put("index", measurements.get(m).getIndex())
                                        .put("session", measurements.get(m).getSessionId()).build();

                                    logger.warn(getMessage(DataErrorCode.HISTORY_SESSION_MEASUREMENT_FOUND, properties));

                                    measurements.remove(m);

                                    removed = true;

                                    break;
                                }
                            }

                            measurements.get(m).setSession(s);
                            break;
                        }
                    }

                    // Ignore removed measurements
                    if (removed) {
                        continue;
                    }

                    // Check if measurement has no session
                    if (measurements.get(m).getSession() == null) {
                        if (strictAmphiroValidation) {
                            throw createApplicationException(DataErrorCode.NO_SESSION_FOUND_FOR_MEASUREMENT)
                                .set("session", measurements.get(m).getSessionId())
                                .set("index", measurements.get(m).getIndex());
                        } else {
                            ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                                .put("index", measurements.get(m).getIndex())
                                .put("session", measurements.get(m).getSessionId()).build();

                            logger.warn(getMessage(DataErrorCode.NO_SESSION_FOUND_FOR_MEASUREMENT, properties));

                            measurements.remove(m);
                        }
                    }
                }
            }
        } catch (ApplicationException ex) {
            logger.warn(jsonToString(data));

            throw ex;
        }
    }

    /**
     * Searches for a single session. Optionally, loads the session
     * measurements.
     *
     * @param query a query for selecting the session.
     * @return the session.
     */
    @Override
    public AmphiroSessionIndexIntervalQueryResult getSession(AmphiroSessionIndexIntervalQuery query) {
        AmphiroSessionIndexIntervalQueryResult data = new AmphiroSessionIndexIntervalQueryResult();

        SessionVersions versions = getSessionVersions(query);

        if (versions.realtime == null) {
            data.setSession(versions.historical);
        } else {
            List<AmphiroMeasurement> measurements = versions.realtime.getMeasurements();

            // Sort measurements
            Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {

                @Override
                public int compare(AmphiroMeasurement m1, AmphiroMeasurement m2) {
                    if (m1.getVolume() < m2.getVolume()) {
                        return -1;
                    } else if (m1.getVolume() > m2.getVolume()) {
                        return 1;
                    }

                    if (m1.getEnergy() < m2.getEnergy()) {
                        return -1;
                    } else if (m1.getEnergy() > m2.getEnergy()) {
                        return 1;
                    }

                    if (m1.getTimestamp() < m2.getTimestamp()) {
                        return -1;
                    } else if (m1.getTimestamp() > m2.getTimestamp()) {
                        return 1;
                    }

                    return 0;
                }

            });

            // Compute difference for volume and energy
            for (int i = measurements.size() - 1; i > 0; i--) {
                AmphiroMeasurement current = measurements.get(i);
                AmphiroMeasurement previous = measurements.get(i - 1);

                if (current.getSessionId() == previous.getSessionId()) {
                    // Set volume
                    float diff = current.getVolume() - previous.getVolume();
                    current.setVolume((float) Math.round(diff * 1000f) / 1000f);
                    // Set energy
                    diff = current.getEnergy() - previous.getEnergy();
                    current.setEnergy((float) Math.round(diff * 1000f) / 1000f);
                }
            }

            // Assign new indexes
            for (int i = 0, count = measurements.size(); i < count; i++) {
                measurements.get(i).setIndex(i + 1);
            }

            // Override real-time values from historical session
            if (versions.historical != null) {
                versions.realtime.setVolume(versions.historical.getVolume());
                versions.realtime.setEnergy(versions.historical.getEnergy());
                versions.realtime.setDuration(versions.historical.getDuration());
                versions.realtime.setFlow(versions.historical.getFlow());
                versions.realtime.setTemperature(versions.historical.getTemperature());
            }

            data.setSession(versions.realtime);
        }

        return data;
    }

    /**
     * Creates a HBase row key for an amphiro b1 session.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @param sessionId the session id.
     * @return a valid HBase key.
     * @throws UnsupportedEncodingException if the encoding is not supported.
     * @throws NoSuchAlgorithmException if the hashing algorithm is not supported.
     */
    private byte[] getSessionKey(UUID userKey, UUID deviceKey, long sessionId) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] userKeyHash = md.digest(userKey.toString().getBytes("UTF-8"));
        byte[] deviceKeyHash = md.digest(deviceKey.toString().getBytes("UTF-8"));

        byte[] sessionIdBytes = Bytes.toBytes(Long.MAX_VALUE - sessionId);

        byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

        System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);

        System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);

        System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length), sessionIdBytes.length);

        return rowKey;
    }

    /**
     * Creates a HBase row key for an amphiro b1 session.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @return a valid HBase key.
     * @throws UnsupportedEncodingException if the encoding is not supported.
     * @throws NoSuchAlgorithmException if the hashing algorithm is not supported.
     */
    private byte[] getUserDeviceKey(UUID userKey, UUID deviceKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] userKeyHash = md.digest(userKey.toString().getBytes("UTF-8"));
        byte[] deviceKeyHash = md.digest(deviceKey.toString().getBytes("UTF-8"));

        byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length];

        System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);

        System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);

        return rowKey;
    }

    /**
     * Creates a HBase row key for an amphiro b1 session partitioned by time.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @param sessionId the session id.
     * @param timestamp the session timestamp.
     * @return a valid HBase key.
     * @throws UnsupportedEncodingException if the encoding is not supported.
     * @throws NoSuchAlgorithmException if the hashing algorithm is not supported.
     */
    private byte[] getSessionTimePartitionedKey(UUID userKey, UUID deviceKey, long sessionId, long timestamp)
                    throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        // Time partition
        short partition = (short) (timestamp % timePartitions);
        byte[] partitionBytes = Bytes.toBytes(partition);

        // Time interval
        timestamp = timestamp / 1000;
        long offset = timestamp % EnumTimeInterval.DAY.getValue();
        long timeBucket = timestamp - offset;

        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

        // User and device hashes
        byte[] userKeyHash = md.digest(userKey.toString().getBytes("UTF-8"));
        byte[] deviceKeyHash = md.digest(deviceKey.toString().getBytes("UTF-8"));

        // Session bytes
        byte[] sessionIdBytes = Bytes.toBytes(sessionId);

        byte[] rowKey = new byte[partitionBytes.length + timeBucketBytes.length + userKeyHash.length
                        + deviceKeyHash.length + sessionIdBytes.length];

        System.arraycopy(partitionBytes,
                         0, rowKey,
                         0, partitionBytes.length);

        System.arraycopy(timeBucketBytes,
                         0, rowKey,
                         partitionBytes.length, timeBucketBytes.length);

        System.arraycopy(userKeyHash,
                        0, rowKey,
                        (partitionBytes.length + timeBucketBytes.length), userKeyHash.length);

        System.arraycopy(deviceKeyHash,
                        0, rowKey,
                        (partitionBytes.length + timeBucketBytes.length + userKeyHash.length), deviceKeyHash.length);

        System.arraycopy(sessionIdBytes,
                        0, rowKey,
                        (partitionBytes.length + timeBucketBytes.length + userKeyHash.length + deviceKeyHash.length), sessionIdBytes.length);

        return rowKey;
    }

    /**
     * Searches for the given session id and returns any version found e.g.
     * historical or/and real-time.
     *
     * @param query a query for selecting the session.
     * @return the session versions.
     */
    private SessionVersions getSessionVersions(AmphiroSessionIndexIntervalQuery query) {
        try {
            byte[] rowKey = getSessionKey(query.getUserKey(), query.getDeviceKey(), query.getSessionId());

            return getSessionVersions(rowKey, !query.isExcludeMeasurements());
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Searches for session data given a HBase row key.
     *
     * @param rowKey the row key.
     * @param includeMeasurements fetch measurements if the session is a real-time one.
     * @return the session versions.
     */
    private SessionVersions getSessionVersions(byte[] rowKey, boolean includeMeasurements) {
        Table table = null;
        ResultScanner scanner = null;

        try {
            table = connection.getTable(amphiroTableSessionByUser);

            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            Get get = new Get(rowKey);
            Result result = table.get(get);

            if (result.getRow() != null) {
                NavigableMap<byte[], byte[]> map = result.getFamilyMap(columnFamily);

                long sessionId = Long.MAX_VALUE - Bytes.toLong(Arrays.copyOfRange(rowKey, 32, 40));

                AmphiroSessionDetails historical = new AmphiroSessionDetails();
                historical.setId(sessionId);
                historical.setHistory(true);

                AmphiroSessionDetails realtime = new AmphiroSessionDetails();
                realtime.setId(sessionId);
                realtime.setHistory(false);

                AmphiroSession.Member member = new AmphiroSession.Member();

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    String qualifier = Bytes.toString(entry.getKey());

                    switch (getSessionColumn(qualifier, false)) {
                        case COLUMN_RT_SESSION_TIMESTAMP:
                            realtime.setTimestamp(Bytes.toLong(entry.getValue()));
                            break;
                        case COLUMN_RT_SESSION_VOLUME:
                            realtime.setVolume(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_RT_SESSION_ENERGY:
                            realtime.setEnergy(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_RT_SESSION_DURATION:
                            realtime.setDuration(Bytes.toInt(entry.getValue()));
                            break;
                        case COLUMN_RT_SESSION_TEMPERATURE:
                            realtime.setTemperature(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_RT_SESSION_FLOW:
                            realtime.setFlow(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_HIST_SESSION_TIMESTAMP:
                            historical.setTimestamp(Bytes.toLong(entry.getValue()));
                            break;
                        case COLUMN_HIST_SESSION_VOLUME:
                            historical.setVolume(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_HIST_SESSION_ENERGY:
                            historical.setEnergy(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_HIST_SESSION_DURATION:
                            historical.setDuration(Bytes.toInt(entry.getValue()));
                            break;
                        case COLUMN_HIST_SESSION_TEMPERATURE:
                            historical.setTemperature(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_HIST_SESSION_FLOW:
                            historical.setFlow(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_SHARED_MEMBER_INDEX:
                            member.setIndex(Bytes.toInt(entry.getValue()));
                            break;
                        case COLUMN_SHARED_MEMBER_MODE:
                            member.setMode(EnumMemberSelectionMode.fromString(new String(entry.getValue(),
                                            StandardCharsets.UTF_8)));
                            break;
                        case COLUMN_SHARED_MEMBER_TIMESTAMP:
                            member.setTimestamp(Bytes.toLong(entry.getValue()));
                            break;
                        case COLUMN_SHARED_IGNORE_VALUE:
                            realtime.setIgnored(Bytes.toBoolean(entry.getValue()));
                            historical.setIgnored(Bytes.toBoolean(entry.getValue()));
                            break;
                        case COLUMN_SHARED_IGNORE_TIMESTAMP:
                            // Ignore
                            break;
                        default:
                            if (qualifier.startsWith(COLUMN_HIST_PROPERTY_PREFIX)) {
                                historical.addProperty(StringUtils.substringAfter(qualifier, COLUMN_HIST_PROPERTY_PREFIX),
                                                       new String(entry.getValue(), StandardCharsets.UTF_8));
                            }
                            if (qualifier.startsWith(COLUMN_RT_PROPERTY_PREFIX)) {
                                realtime.addProperty(StringUtils.substringAfter(qualifier, COLUMN_RT_PROPERTY_PREFIX),
                                                     new String(entry.getValue(), StandardCharsets.UTF_8));
                            }
                            break;
                    }
                }

                // Reset objects if no data exist
                if (historical.getTimestamp() == null) {
                    historical = null;
                }

                if (realtime.getTimestamp() == null) {
                    realtime = null;
                } else if (includeMeasurements) {
                    realtime.setMeasurements(getSessionMeasurements(rowKey));
                }

                // Set member
                if (member.getIndex() != null) {
                    if (member.getTimestamp() == null) {
                        if (realtime != null) {
                            member.setTimestamp(realtime.getTimestamp());
                        } else if (historical != null) {
                            member.setTimestamp(historical.getTimestamp());
                        }
                    }

                    if (historical != null) {
                        historical.setMember(member);
                    }
                    if (realtime != null) {
                        realtime.setMember(member);
                    }
                }

                return new SessionVersions(historical, realtime);
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

        return new SessionVersions(null, null);
    }

    /**
     * Returns the time-series of an amphiro b1 session given its row key.
     *
     * @param rowKey the session row key.
     * @return a list of measurements.
     */
    private ArrayList<AmphiroMeasurement> getSessionMeasurements(byte[] rowKey) {
        ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

        Table table = null;
        ResultScanner scanner = null;

        try {
            table = connection.getTable(amphiroTableMeasurements);

            Get get = new Get(rowKey);
            Result sessionResult = table.get(get);

            long currentIndex = -1;

            AmphiroMeasurement measurement = null;

            if (sessionResult.getRow() != null) {
                long sessionId = Long.MAX_VALUE - Bytes.toLong(Arrays.copyOfRange(rowKey, 32, 40));

                byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);
                NavigableMap<byte[], byte[]> map = sessionResult.getFamilyMap(columnFamily);

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    long currentColumnIndex = Bytes.toLong(Arrays.copyOfRange(entry.getKey(), 0, 8));

                    if (currentIndex != currentColumnIndex) {
                        if (measurement != null) {
                            measurements.add(measurement);
                        }
                        currentIndex = currentColumnIndex;

                        measurement = new AmphiroMeasurement();
                        measurement.setSessionId(sessionId);
                        measurement.setIndex(currentIndex);
                    }

                    int qualifierLength = Arrays.copyOfRange(entry.getKey(), 8, 9)[0];
                    byte[] qualifierBytes = Arrays.copyOfRange(entry.getKey(), 9, 9 + qualifierLength);
                    String qualifier = Bytes.toString(qualifierBytes);

                    switch (getMeasurementColumn(qualifier)) {
                        case COLUMN_MEASUREMENT_VOLUME:
                            measurement.setVolume(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_MEASUREMENT_ENERGY:
                            measurement.setEnergy(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_MEASUREMENT_TEMPERATURE:
                            measurement.setTemperature(Bytes.toFloat(entry.getValue()));
                            break;
                        case COLUMN_MEASUREMENT_TIMESTAMP:
                            measurement.setTimestamp(Bytes.toLong(entry.getValue()));
                            break;
                        default:
                            // Ignore
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Stores session time-series.
     *
     * @param userKey the owner of the device.
     * @param data the data to store.
     * @throws Exception if saving data or releasing HBase resources has failed.
     */
    private void storeMeasurements(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        try {
            table = connection.getTable(amphiroTableMeasurements);

            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            // Compute the next valid index value and remove existing
            // measurements from the message.
            for (AmphiroSession s : data.getSessions()) {
                if(s.isHistory()) {
                    continue;
                }
                List<AmphiroMeasurement> measurements = data.getMeasurementsBySessionId(s.getId());

                byte[] rowKey = getSessionKey(userKey, data.getDeviceKey(), s.getId());

                AmphiroSessionDetails realtimeSession = s.getVersions().realtime;

                if (realtimeSession != null) {
                    for (int d = measurements.size() - 1; d >= 0; d--) {
                        for (AmphiroMeasurement oldItem : realtimeSession.getMeasurements()) {
                            if (measurements.get(d).equalByValue(oldItem)) {
                                measurements.remove(d);
                                break;
                            }
                        }
                    }
                }

                if (!measurements.isEmpty()) {
                    for (int i = 0; i < measurements.size(); i++) {
                        AmphiroMeasurement m = measurements.get(i);

                        if (m.getVolume() < 0) {
                            continue;
                        }

                        byte[] indexBytes = Bytes.toBytes((long) increment(COUNTER_GROUP, COUNTER_MEASUREMENT));

                        byte[] column = null;

                        Put p = new Put(rowKey);

                        column = concatenate(indexBytes, appendLength(Bytes.toBytes(EnumMeasurementColumn.COLUMN_MEASUREMENT_TIMESTAMP.getValue())));
                        p.addColumn(columnFamily, column, Bytes.toBytes(m.getTimestamp()));

                        column = concatenate(indexBytes, appendLength(Bytes.toBytes(EnumMeasurementColumn.COLUMN_MEASUREMENT_VOLUME.getValue())));
                        p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

                        column = concatenate(indexBytes, appendLength(Bytes.toBytes(EnumMeasurementColumn.COLUMN_MEASUREMENT_ENERGY.getValue())));
                        p.addColumn(columnFamily, column, Bytes.toBytes(m.getEnergy()));

                        column = concatenate(indexBytes, appendLength(Bytes.toBytes(EnumMeasurementColumn.COLUMN_MEASUREMENT_TEMPERATURE.getValue())));
                        p.addColumn(columnFamily, column, Bytes.toBytes(m.getTemperature()));

                        table.put(p);
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Logs amphiro b1 session assigned household member.
     *
     * @param user the owner of the device.
     * @param assignments a list of member to session assignments.
     */
    private void logAssignmentOperation(AuthenticatedUser user, List<MemberAssignmentRequest.Assignment> assignments) {
        if (assignments != null) {
            for (MemberAssignmentRequest.Assignment assignment : assignments) {
                List<String> tokens = new ArrayList<String>();

                tokens.add(getVersion());

                tokens.add(Integer.toString(user.getId()));
                tokens.add(user.getKey().toString());
                tokens.add(user.getUsername());

                tokens.add(assignment.getDeviceKey().toString());

                tokens.add("MANUAL");
                tokens.add(Long.toString(assignment.getSessionId()));
                tokens.add(Long.toString(assignment.getTimestamp()));
                tokens.add(Integer.toString(assignment.getMemberIndex()));

                sessionMemberLogger.info(StringUtils.join(tokens, ";"));
            }
        }
    }

    /**
     * Assigns one ore more sessions to a specific household members.
     *
     * @param user the device owner.
     * @param assignments the member to session assignments.
     * @throws Exception if update fails.
     */
    @Override
    public void assignMember(AuthenticatedUser user, List<MemberAssignmentRequest.Assignment> assignments) throws Exception {
        if (assignments == null) {
            return;
        }

        logAssignmentOperation(user, assignments);

        for (MemberAssignmentRequest.Assignment assignment : assignments) {
            AmphiroSessionIndexIntervalQuery query = new AmphiroSessionIndexIntervalQuery();

            query.setDeviceKey(assignment.getDeviceKey());
            query.setSessionId(assignment.getSessionId());
            query.setUserKey(user.getKey());
            query.setExcludeMeasurements(true);

            SessionVersions sessions = getSessionVersions(query);
            if (sessions.isEmpty()) {
                throw createApplicationException(DataErrorCode.SESSION_NOT_FOUND)
                    .set("session", assignment.getSessionId());
            }

            assignMemberToSessionInUserTable(user.getKey(), assignment, sessions);
            assignMemberToSessionInTimeTable(user.getKey(), assignment, sessions);
        }
    }

    /**
     * Updates session member in the HBase table indexed by session id.
     *
     * @param userKey user key.
     * @param assignment member assignment information.
     * @param sessions session versions to update.
     * @throws Exception if session does not exists.
     */
    private void assignMemberToSessionInUserTable(UUID userKey,
                                                  MemberAssignmentRequest.Assignment assignment,
                                                  SessionVersions sessions) throws Exception {
        Table table = null;

        try {
            table = connection.getTable(amphiroTableSessionByUser);

            byte[] rowKey = getSessionKey(userKey, assignment.getDeviceKey(), sessions.getSessionId());
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            Put put = new Put(rowKey);
            byte[] column;

            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_INDEX.getValue());
            put.addColumn(columnFamily, column, Bytes.toBytes(assignment.getMemberIndex()));

            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_MODE.getValue());
            put.addColumn(columnFamily, column, EnumMemberSelectionMode.MANUAL.toString().getBytes(StandardCharsets.UTF_8));

            if (assignment.getTimestamp() != null) {
                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_TIMESTAMP.getValue());
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Updates session member in the HBase table indexed by session timestamp.
     *
     * @param userKey user key.
     * @param assignment member assignment information.
     * @param sessions sessions to update.
     * @throws Exception if session does not exists.
     */

    private void assignMemberToSessionInTimeTable(UUID userKey,
                                                  MemberAssignmentRequest.Assignment assignment,
                                                  SessionVersions sessions) throws Exception {
        Table table = null;

        try {
            table = connection.getTable(amphiroTableSessionByTime);

            byte[] rowKey;
            if (sessions.realtime == null) {
                rowKey = getSessionTimePartitionedKey(userKey, assignment.getDeviceKey(), assignment.getSessionId(), sessions.historical.getTimestamp());
            } else {
                rowKey = getSessionTimePartitionedKey(userKey, assignment.getDeviceKey(), assignment.getSessionId(), sessions.realtime.getTimestamp());
            }
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            Put put = new Put(rowKey);
            byte[] column;

            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_INDEX.getValue());
            put.addColumn(columnFamily, column, Bytes.toBytes(assignment.getMemberIndex()));

            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_MODE.getValue());
            put.addColumn(columnFamily, column, EnumMemberSelectionMode.MANUAL.toString().getBytes( StandardCharsets.UTF_8));

            if (assignment.getTimestamp() != null) {
                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_TIMESTAMP.getValue());
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Marks a session as ignored i.e. a session that does not correspond to an
     * actual session.
     *
     * @param user the owner of the device.
     * @param sessions a list of sessions to ignore.
     * @throws Exception if update fails.
     */
    @Override
    public void ignore(AuthenticatedUser user, List<IgnoreShowerRequest.Session> sessions) throws Exception {
        if (sessions == null) {
            return;
        }

        logIgnoreOperation(user, sessions);

        for (IgnoreShowerRequest.Session session : sessions) {
            AmphiroSessionIndexIntervalQuery query = new AmphiroSessionIndexIntervalQuery();

            query.setDeviceKey(session.getDeviceKey());
            query.setSessionId(session.getSessionId());
            query.setUserKey(user.getKey());
            query.setExcludeMeasurements(true);

            SessionVersions versions = getSessionVersions(query);
            if (versions.isEmpty()) {
                throw createApplicationException(DataErrorCode.SESSION_NOT_FOUND)
                    .set("session", session.getSessionId());
            }

            ignoreSessionInUserTable(user.getKey(), session, versions);
            ignoreSessionInTimeTable(user.getKey(), session, versions);
        }
    }

    /**
     * Logs amphiro b1 sessions that are not showers.
     *
     * @param user the owner of the device.
     * @param sessions a list of sessions that are ignored i.e. they are not showers.
     */
    private void logIgnoreOperation(AuthenticatedUser user, List<IgnoreShowerRequest.Session> sessions) {
        for (IgnoreShowerRequest.Session session : sessions) {
            List<String> tokens = new ArrayList<String>();

            tokens.add(getVersion());

            tokens.add(Integer.toString(user.getId()));
            tokens.add(user.getKey().toString());
            tokens.add(user.getUsername());

            tokens.add(session.getDeviceKey().toString());

            tokens.add(Long.toString(session.getSessionId()));
            tokens.add(Long.toString(session.getTimestamp()));

            sessionIgnoreLogger.info(StringUtils.join(tokens, ";"));
        }
    }

    /**
     * Updates session status in the HBase table indexed by session id.
     *
     * @param userKey user key.
     * @param ignore session status information.
     * @param sessions session versions to update.
     * @throws Exception if session does not exists.
     */
    private void ignoreSessionInUserTable(UUID userKey, IgnoreShowerRequest.Session ignore, SessionVersions sessions) throws Exception {
        Table table = null;

        try {
            table = connection.getTable(amphiroTableSessionByUser);

            byte[] rowKey = getSessionKey(userKey, ignore.getDeviceKey(), sessions.getSessionId());
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            Put put = new Put(rowKey);
            byte[] column;

            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_IGNORE_VALUE.getValue());
            put.addColumn(columnFamily, column, Bytes.toBytes(true));

            if (ignore.getTimestamp() != null) {
                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_IGNORE_TIMESTAMP.getValue());
                put.addColumn(columnFamily, column, Bytes.toBytes(ignore.getTimestamp()));
            }

            table.put(put);
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
     * Updates session status in the HBase table indexed by session timestamp.
     *
     * @param userKey user key.
     * @param ignore session status information.
     * @param sessions session versions to update.
     * @throws Exception if session does not exists.
     */
    private void ignoreSessionInTimeTable(UUID userKey, IgnoreShowerRequest.Session ignore, SessionVersions sessions) throws Exception {
        Table table = null;

        try {
            table = connection.getTable(amphiroTableSessionByTime);

            byte[] rowKey;
            if (sessions.realtime == null) {
                rowKey = getSessionTimePartitionedKey(userKey, ignore.getDeviceKey(), ignore.getSessionId(), sessions.historical.getTimestamp());
            } else {
                rowKey = getSessionTimePartitionedKey(userKey, ignore.getDeviceKey(), ignore.getSessionId(), sessions.realtime.getTimestamp());
            }
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);
            byte[] column;

            Put put = new Put(rowKey);

            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_IGNORE_VALUE.getValue());
            put.addColumn(columnFamily, column, Bytes.toBytes(true));

            if (ignore.getTimestamp() != null) {
                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_IGNORE_TIMESTAMP.getValue());
                put.addColumn(columnFamily, column, Bytes.toBytes(ignore.getTimestamp()));
            }

            table.put(put);
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
     * Stores amphiro b1 session indexed by shower id.
     *
     * @param userKey the user key.
     * @param data the data to store.
     * @param updates session that had their timestamps updated.
     * @throws Exception if saving data or releasing HBase resources has failed.
     */
    private void storeSessionByUser(UUID userKey, AmphiroMeasurementCollection data, AmphiroSessionUpdateCollection updates) throws Exception {
        Table table = null;

        try {
            table = connection.getTable(amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            for (int i = data.getSessions().size() - 1; i >= 0; i--) {
                AmphiroSession s = data.getSessions().get(i);

                // Get existing row if any exists and set update properties
                SessionVersions existing = getSessionVersions(getSessionKey(userKey,
                                                                            data.getDeviceKey(),
                                                                            data.getSessions().get(i).getId()), true);

                s.setVersions(existing);

                // Decide update actions
                byte[] rowKey = getSessionKey(userKey, data.getDeviceKey(), s.getId());

                Put put = null;
                byte[] column;

                if (s.getVersions().isEmpty()) {
                    // Insert new row
                    put = new Put(rowKey);

                    if (s.isHistory()) {
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    } else {
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    }
                } else if (s.isHistory()) {
                    /*
                     * Cases when saving a historical session:
                     *
                     * (a) An existing historical session already exists without
                     *     a corresponding real-time one. Moreover the timestamp of
                     *     the new session is less than that of the existing one.
                     *
                     *     In this case we update only the timestamp.
                     *
                     * (b) An existing real-time session already exists without
                     *     a corresponding historical one.
                     *
                     *     The volume, energy, duration, temperature and flow will
                     *     be loaded from the historical session.
                     *
                     * (c) Both, a historical session and a real-time one
                     *     already exist.
                     *
                     *     Ignore the session. Nothing is saved.
                     */
                    if ((s.getVersions().historical != null) && (s.getVersions().realtime == null)) {
                        // Case (a)
                        if (s.getTimestamp() < s.getVersions().historical.getTimestamp()) {
                            put = new Put(rowKey);

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));
                        } else {
                            data.removeSession(i);
                        }
                    } else if ((s.getVersions().historical == null) && (s.getVersions().realtime != null)) {
                        // Case (b)
                        put = new Put(rowKey);

                        // Add historical values
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

                        // A real-time session already exists
                        updates.getUpdates().add(new AmphiroSessionUpdate(data.getDeviceKey(),
                                                                          s.getId(),
                                                                          s.getVersions().realtime.getTimestamp().longValue()));
                    } else {
                        // Case (c)
                        data.removeSession(i);

                        // A real-time session already exists
                        updates.getUpdates().add(new AmphiroSessionUpdate(data.getDeviceKey(),
                                                                          s.getId(),
                                                                          s.getVersions().realtime.getTimestamp().longValue()));
                    }
                } else {
                    /*
                     * Cases when saving a real-time session:
                     *
                     * (a) An existing historical session already exists without
                     *     a corresponding real-time one.
                     *
                     *     Update volume, energy, duration, temperature and flow
                     *     from the historical value.
                     *
                     * (b) An existing real-time session already exists without
                     *     a corresponding historical one.
                     *
                     *     Update volume, energy, duration, temperature and flow
                     *     from the historical value.
                     *
                     * (c) Both, a historical session and a real-time one
                     *     already exist.
                     *
                     *     Ignore the session. Nothing is saved.
                     */
                    if ((s.getVersions().historical != null) && (s.getVersions().realtime == null)) {
                        // Case (a)
                        put = new Put(rowKey);

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    } else if ((s.getVersions().historical == null) && (s.getVersions().realtime != null)) {
                        // A real-time session already exists
                        updates.getUpdates().add(new AmphiroSessionUpdate(data.getDeviceKey(), s.getId(), s.getVersions().realtime.getTimestamp().longValue()));
                    } else {
                        // A real-time session already exists
                        updates.getUpdates().add(new AmphiroSessionUpdate(data.getDeviceKey(), s.getId(), s.getVersions().realtime.getTimestamp().longValue()));
                    }
                }

                if (put != null) {
                    if (s.getMember() != null) {
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_INDEX.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getIndex()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_MODE.getValue());
                        put.addColumn(columnFamily, column, s.getMember().getMode().toString().getBytes(
                                        StandardCharsets.UTF_8));

                        if (s.getMember().getTimestamp() != null) {
                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_TIMESTAMP.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getTimestamp()));
                        }
                    }

                    for (int p = 0, count = s.getProperties().size(); p < count; p++) {
                        String name = s.getProperties().get(p).getKey();

                        if (s.isHistory()) {
                            name = COLUMN_HIST_PROPERTY_PREFIX + name;
                        } else {
                            name = COLUMN_RT_PROPERTY_PREFIX + name;
                        }

                        column = Bytes.toBytes(name);
                        put.addColumn(columnFamily, column, s.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
                    }

                    table.put(put);
                } else {
                    // No session is inserted. Check for additional properties.
                    if ((!s.getVersions().isEmpty()) && (s.getProperties() != null) && (!s.getProperties().isEmpty())) {
                        // There is at least one session (historical or
                        // real-time) already stored and the new session has at
                        // least one property
                        put = new Put(rowKey);

                        for (int p = 0, count = s.getProperties().size(); p < count; p++) {
                            String name = s.getProperties().get(p).getKey();

                            boolean exists = false;
                            if (s.isHistory()) {
                                if((s.getVersions().historical != null) &&
                                   (s.getVersions().historical.getPropertyByKey(name) != null)) {
                                    exists = true;
                                }
                                name = COLUMN_HIST_PROPERTY_PREFIX + name;
                            } else {
                                if ((s.getVersions().realtime != null) &&
                                    (s.getVersions().realtime.getPropertyByKey(name) != null)) {
                                    exists = true;
                                }
                                name = COLUMN_RT_PROPERTY_PREFIX + name;
                            }

                            if (!exists) {
                                column = Bytes.toBytes(name);
                                put.addColumn(columnFamily, column, s.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
                            }
                        }

                        if (put.size() > 0) {
                            table.put(put);
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Stores amphiro b1 session indexed by shower timestamp.
     *
     * @param userKey the user key.
     * @param data the data to store.
     * @throws Exception if saving data or releasing HBase resources has failed.
     */
    private void storeSessionByTime(UUID userKey, AmphiroMeasurementCollection data) throws Exception {
        Table table = null;

        /*
         * When computing the session row key using time partitioning, the
         * real-time session timestamp is always used if it is available.
         * Otherwise, the historical one is used.
         */
        try {
            table = connection.getTable(amphiroTableSessionByTime);
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            for (int i = 0; i < data.getSessions().size(); i++) {
                AmphiroSession s = data.getSessions().get(i);

                // Decide update actions
                Put put = null;

                byte[] column;

                if (s.getVersions().isEmpty()) {
                    // Insert new row
                    put = new Put(getSessionTimePartitionedKey(userKey, data.getDeviceKey(), s.getId(), s.getTimestamp()));

                    if (s.isHistory()) {
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    } else {
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    }
                } else if (s.isHistory()) {
                    /*
                     * Cases when saving a historical session:
                     *
                     * (a) An existing historical session already exists without
                     *     a corresponding real-time one. Moreover the timestamp of
                     *     the new session is less than that of the existing one.
                     *
                     *     In this case we update only the timestamp.
                     *
                     * (b) An existing real-time session already exists without
                     *     a corresponding historical one.
                     *
                     *     The volume, energy, duration, temperature and flow will
                     *     be loaded from the historical session.
                     *
                     * (c) Both, a historical session and a real-time one
                     *     already exist.
                     *
                     *     Ignore the session. Nothing is saved.
                     */
                    if ((s.getVersions().historical != null) && (s.getVersions().realtime == null)) {
                        // Case (a)
                        if (s.getTimestamp() < s.getVersions().historical.getTimestamp()) {
                            // Delete existing record
                            for (short partitionIndex = 0; partitionIndex < timePartitions; partitionIndex++) {
                                byte[] deleteRowKey = getSessionTimePartitionedKey(userKey, data.getDeviceKey(), s.getId(), s.getVersions().historical.getTimestamp());

                                Delete delete = new Delete(deleteRowKey);
                                table.delete(delete);
                            }

                            put = new Put(getSessionTimePartitionedKey(userKey, data.getDeviceKey(), s.getId(), s.getTimestamp()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_VOLUME.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_ENERGY.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_DURATION.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TEMPERATURE.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_FLOW.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

                            // Preserve members and properties
                            if ((s.getMember() == null) && (s.getVersions().historical.getMember() != null)) {
                                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_INDEX.getValue());
                                put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getMember().getIndex()));

                                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_MODE.getValue());
                                put.addColumn(columnFamily, column, s.getVersions().historical.getMember().getMode().toString().getBytes(StandardCharsets.UTF_8));

                                if (s.getVersions().historical.getMember().getTimestamp() != null) {
                                    column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_TIMESTAMP.getValue());
                                    put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getMember().getTimestamp()));
                                }
                            }

                            for (int p = 0, count = s.getVersions().historical.getProperties().size(); p < count; p++) {
                                String name = s.getVersions().historical.getProperties().get(p).getKey();

                                name = COLUMN_HIST_PROPERTY_PREFIX + name;

                                column = Bytes.toBytes(name);
                                put.addColumn(columnFamily, column, s.getVersions().historical.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    } else if ((s.getVersions().historical == null) && (s.getVersions().realtime != null)) {
                        // Case (b)
                        put = new Put(getSessionTimePartitionedKey(userKey, data.getDeviceKey(), s.getId(), s.getVersions().realtime.getTimestamp()));

                        // Add historical values
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    }
                } else {
                    /*
                     * Cases when saving a real-time session:
                     *
                     * (a) An existing historical session already exists without
                     *     a corresponding real-time one.
                     *
                     *     Delete the old row (since we have a real-time session,
                     *     the row key must be computed again). Compute a new row
                     *     key using the real-time timestamp. Copy the existing
                     *     historical values. Add the new real-time values.
                     *
                     * (b) An existing real-time session already exists without
                     *     a corresponding historical one.
                     *
                     *     No update is required.
                     *
                     * (c) Both, a historical session and a real-time one
                     *     already exist.
                     *
                     *     No update is required.
                     */
                    if ((s.getVersions().historical != null) && (s.getVersions().realtime == null)) {
                        // Delete existing record
                        for (short partitionIndex = 0; partitionIndex < timePartitions; partitionIndex++) {
                            byte[] deleteRowKey = getSessionTimePartitionedKey(userKey, data.getDeviceKey(), s.getId(), s.getVersions().historical.getTimestamp());

                            Delete delete = new Delete(deleteRowKey);
                            table.delete(delete);
                        }

                        // Case (a)
                        put = new Put(getSessionTimePartitionedKey(userKey, data.getDeviceKey(), s.getId(), s.getTimestamp()));

                        // Rewrite historical values
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_HIST_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getFlow()));

                        // Preserve members and properties.
                        if ((s.getMember() == null) && (s.getVersions().historical.getMember() != null)) {
                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_INDEX.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getMember().getIndex()));

                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_MODE.getValue());
                            put.addColumn(columnFamily, column, s.getVersions().historical.getMember().getMode().toString().getBytes(StandardCharsets.UTF_8));

                            if (s.getVersions().historical.getMember().getTimestamp() != null) {
                                column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_TIMESTAMP.getValue());
                                put.addColumn(columnFamily, column, Bytes.toBytes(s.getVersions().historical.getMember().getTimestamp()));
                            }
                        }

                        for (int p = 0, count = s.getVersions().historical.getProperties().size(); p < count; p++) {
                            String name = s.getVersions().historical.getProperties().get(p).getKey();

                            name = COLUMN_HIST_PROPERTY_PREFIX + name;

                            column = Bytes.toBytes(name);
                            put.addColumn(columnFamily, column, s.getVersions().historical.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
                        }

                        // Add real-time values
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TIMESTAMP.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTimestamp()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_VOLUME.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_ENERGY.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_DURATION.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_TEMPERATURE.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_RT_SESSION_FLOW.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));
                    }
                }

                // Add member and properties from the request
                if (put != null) {
                    if (s.getMember() != null) {
                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_INDEX.getValue());
                        put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getIndex()));

                        column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_MODE.getValue());
                        put.addColumn(columnFamily, column, s.getMember().getMode().toString().getBytes(StandardCharsets.UTF_8));

                        if (s.getMember().getTimestamp() != null) {
                            column = Bytes.toBytes(EnumSessionColumn.COLUMN_SHARED_MEMBER_TIMESTAMP.getValue());
                            put.addColumn(columnFamily, column, Bytes.toBytes(s.getMember().getTimestamp()));
                        }
                    }

                    for (int p = 0, count = s.getProperties().size(); p < count; p++) {
                        String name = s.getProperties().get(p).getKey();

                        if (s.isHistory()) {
                            name = COLUMN_HIST_PROPERTY_PREFIX + name;
                        } else {
                            name = COLUMN_RT_PROPERTY_PREFIX + name;
                        }

                        column = Bytes.toBytes(name);
                        put.addColumn(columnFamily, column, s.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
                    }

                    table.put(put);
                }
            }
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
     * Searches for amphiro b1 session measurement time series.
     *
     * @param timezone the reference time zone.
     * @param query a query for filtering the measurements.
     * @return a collection of measurements.
     */
    @Override
    public AmphiroMeasurementIndexIntervalQueryResult getMeasurements(DateTimeZone timezone, AmphiroMeasurementIndexIntervalQuery query) {
        AmphiroMeasurementIndexIntervalQueryResult data = new AmphiroMeasurementIndexIntervalQueryResult();

        long startIndex = Long.MAX_VALUE;
        long endIndex = 0L;
        int maxTotalSessions = Integer.MAX_VALUE;

        switch (query.getType()) {
            case ABSOLUTE:
                startIndex = query.getEndIndex();
                endIndex = query.getStartIndex();
                break;
            case SLIDING:
                if (query.getStartIndex() == null) {
                    startIndex = query.getLength();
                    endIndex = 0;
                } else {
                    startIndex = query.getStartIndex() + query.getLength() -1;
                    endIndex = query.getStartIndex();
                }
                maxTotalSessions = query.getLength();
                break;
            default:
                return data;
        }

        Table table = null;
        ResultScanner scanner = null;

        try {
            table = connection.getTable(amphiroTableMeasurements);
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            UUID deviceKeys[] = query.getDeviceKey();

            for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
                AmphiroDataSeries series = new AmphiroDataSeries(deviceKeys[deviceIndex]);

                data.getSeries().add(series);

                if (endIndex > startIndex) {
                    continue;
                }

                Scan scan = new Scan();
                scan.addFamily(columnFamily);
                scan.setStartRow(getSessionKey(query.getUserKey(), deviceKeys[deviceIndex], startIndex));
                scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(getSessionKey(query.getUserKey(),
                                deviceKeys[deviceIndex], endIndex)));

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

                    long currentIndex = -1;
                    eu.daiad.web.model.amphiro.AmphiroDataPoint point = null;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        long currentColumnIndex = Bytes.toLong(Arrays.copyOfRange(entry.getKey(), 0, 8));

                        if (currentIndex != currentColumnIndex) {
                            currentIndex = currentColumnIndex;

                            point = new eu.daiad.web.model.amphiro.AmphiroDataPoint();
                            point.setSessionId(sessionId);
                            point.setIndex(currentIndex);

                            points.add(point);
                        }

                        int qualifierLength = Arrays.copyOfRange(entry.getKey(), 8, 9)[0];
                        byte[] qualifierBytes = Arrays.copyOfRange(entry.getKey(), 9, 9 + qualifierLength);
                        String qualifier = Bytes.toString(qualifierBytes);

                        switch (getMeasurementColumn(qualifier)) {
                            case COLUMN_MEASUREMENT_VOLUME:
                                point.setVolume(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_MEASUREMENT_ENERGY:
                                point.setEnergy(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_MEASUREMENT_TEMPERATURE:
                                point.setTemperature(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_MEASUREMENT_TIMESTAMP:
                                point.setTimestamp(Bytes.toLong(entry.getValue()));
                                break;
                        }
                    }
                }

                scanner.close();
                scanner = null;

                series.setPoints(points, timezone);

                // Sort measurements
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

                        if (m1.getVolume() < m2.getVolume()) {
                            return -1;
                        } else if (m1.getVolume() > m2.getVolume()) {
                            return 1;
                        }

                        if (m1.getEnergy() < m2.getEnergy()) {
                            return -1;
                        } else if (m1.getEnergy() > m2.getEnergy()) {
                            return 1;
                        }

                        if (m1.getTimestamp() < m2.getTimestamp()) {
                            return -1;
                        } else if (m1.getTimestamp() > m2.getTimestamp()) {
                            return 1;
                        }

                        return 0;
                    }

                });

                // Compute difference for volume and energy
                for (int i = series.getPoints().size() - 1; i > 0; i--) {
                    eu.daiad.web.model.amphiro.AmphiroDataPoint current = (eu.daiad.web.model.amphiro.AmphiroDataPoint) series.getPoints().get(i);
                    eu.daiad.web.model.amphiro.AmphiroDataPoint previous = (eu.daiad.web.model.amphiro.AmphiroDataPoint) series.getPoints().get(i - 1);

                    if (current.getSessionId() == previous.getSessionId()) {
                        // Set volume
                        float diff = current.getVolume() - previous.getVolume();
                        current.setVolume((float) Math.round(diff * 1000f) / 1000f);
                        // Set energy
                        diff = current.getEnergy() - previous.getEnergy();
                        current.setEnergy((float) Math.round(diff * 1000f) / 1000f);
                    }
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Searches for sessions for one or more amphiro b1 devices.
     *
     * @param names the names of the devices.
     * @param timezone the reference time zone.
     * @param query a query for filtering sessions.
     * @return a collection of sessions per device.
     */
    @Override
    public AmphiroSessionCollectionIndexIntervalQueryResult getSessions(String[] names,
                                                                        DateTimeZone timezone,
                                                                        AmphiroSessionCollectionIndexIntervalQuery query) {
        AmphiroSessionCollectionIndexIntervalQueryResult data = new AmphiroSessionCollectionIndexIntervalQueryResult();

        long skipSessions = 0;
        long takeSessions = Integer.MAX_VALUE;

        switch (query.getType()) {
            case ABSOLUTE:
                if ((query.getStartIndex() == null) || (query.getEndIndex() == null)) {
                    return data;
                }
                if(query.getStartIndex() > query.getEndIndex()) {
                    return data;
                }
                skipSessions = query.getStartIndex();
                takeSessions = query.getEndIndex() - query.getStartIndex() + 1;
                break;
            case SLIDING:
                if (query.getLength() == null) {
                    return data;
                }
                if (query.getStartIndex() != null) {
                    skipSessions = query.getStartIndex();
                }
                takeSessions = query.getLength();
                break;
            default:
                return data;
        }
        if (takeSessions <= 0) {
            return data;
        }

        Table table = null;
        ResultScanner scanner = null;

        try {
            table = connection.getTable(amphiroTableSessionByUser);
            byte[] columnFamily = Bytes.toBytes(DEFAULT_COLUMN_FAMILY);

            UUID deviceKeys[] = query.getDeviceKey();

            for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
                long index = 0;

                AmphiroSessionCollection collection = new AmphiroSessionCollection(deviceKeys[deviceIndex], names[deviceIndex]);

                data.getDevices().add(collection);

                ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

                Scan scan = new Scan();
                scan.addFamily(columnFamily);
                scan.setStartRow(getUserDeviceKey(query.getUserKey(), deviceKeys[deviceIndex]));
                scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(getUserDeviceKey(query.getUserKey(), deviceKeys[deviceIndex])));
                scanner = table.getScanner(scan);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    if(index < skipSessions) {
                        index++;
                        continue;
                    }
                    if (index >= (skipSessions + takeSessions)) {
                        break;
                    }
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    AmphiroSession historical = new AmphiroSession();
                    historical.setId(Long.MAX_VALUE - Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40)));
                    historical.setHistory(true);

                    AmphiroSession realtime = new AmphiroSession();
                    realtime.setId(Long.MAX_VALUE - Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40)));
                    realtime.setHistory(false);

                    AmphiroSession.Member member = new AmphiroSession.Member();

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        String qualifier = Bytes.toString(entry.getKey());

                        switch (getSessionColumn(qualifier, false)) {
                            case COLUMN_RT_SESSION_TIMESTAMP:
                                realtime.setTimestamp(Bytes.toLong(entry.getValue()));
                                break;
                            case COLUMN_RT_SESSION_VOLUME:
                                realtime.setVolume(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_RT_SESSION_ENERGY:
                                realtime.setEnergy(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_RT_SESSION_DURATION:
                                realtime.setDuration(Bytes.toInt(entry.getValue()));
                                break;
                            case COLUMN_RT_SESSION_TEMPERATURE:
                                realtime.setTemperature(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_RT_SESSION_FLOW:
                                realtime.setFlow(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_HIST_SESSION_TIMESTAMP:
                                historical.setTimestamp(Bytes.toLong(entry.getValue()));
                                break;
                            case COLUMN_HIST_SESSION_VOLUME:
                                historical.setVolume(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_HIST_SESSION_ENERGY:
                                historical.setEnergy(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_HIST_SESSION_DURATION:
                                historical.setDuration(Bytes.toInt(entry.getValue()));
                                break;
                            case COLUMN_HIST_SESSION_TEMPERATURE:
                                historical.setTemperature(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_HIST_SESSION_FLOW:
                                historical.setFlow(Bytes.toFloat(entry.getValue()));
                                break;
                            case COLUMN_SHARED_MEMBER_INDEX:
                                member.setIndex(Bytes.toInt(entry.getValue()));
                                break;
                            case COLUMN_SHARED_MEMBER_MODE:
                                member.setMode(EnumMemberSelectionMode.fromString(new String(entry.getValue(),
                                                StandardCharsets.UTF_8)));
                                break;
                            case COLUMN_SHARED_MEMBER_TIMESTAMP:
                                member.setTimestamp(Bytes.toLong(entry.getValue()));
                                break;
                            case COLUMN_SHARED_IGNORE_VALUE:
                                realtime.setIgnored(Bytes.toBoolean(entry.getValue()));
                                historical.setIgnored(Bytes.toBoolean(entry.getValue()));
                                break;
                            case COLUMN_SHARED_IGNORE_TIMESTAMP:
                                // Ignore
                                break;
                            default:
                                if (qualifier.startsWith(COLUMN_HIST_PROPERTY_PREFIX)) {
                                    historical.addProperty(StringUtils.substringAfter(qualifier, COLUMN_HIST_PROPERTY_PREFIX),
                                                           new String(entry.getValue(), StandardCharsets.UTF_8));
                                }
                                if (qualifier.startsWith(COLUMN_RT_PROPERTY_PREFIX)) {
                                    realtime.addProperty(StringUtils.substringAfter(qualifier, COLUMN_RT_PROPERTY_PREFIX),
                                                         new String(entry.getValue(), StandardCharsets.UTF_8));
                                }
                                break;
                        }
                    }

                    if (index < (skipSessions + takeSessions)) {
                        if (realtime.getTimestamp() != null) {
                            if (member.getIndex() != null) {
                                if (member.getTimestamp() == null) {
                                    member.setTimestamp(realtime.getTimestamp());
                                }
                                realtime.setMember(member);
                            }
                            // Override real-time values from historical session
                            if (historical.getTimestamp() != null) {
                                realtime.setVolume(historical.getVolume());
                                realtime.setEnergy(historical.getEnergy());
                                realtime.setDuration(historical.getDuration());
                                realtime.setFlow(historical.getFlow());
                                realtime.setTemperature(historical.getTemperature());
                            }
                            if (filterMember(realtime.getMember(), query.getMembers())) {
                                sessions.add(realtime);
                                index++;
                            }
                        } else if (historical.getTimestamp() != null) {
                            if (member.getIndex() != null) {
                                if (member.getTimestamp() == null) {
                                    member.setTimestamp(historical.getTimestamp());
                                }
                                historical.setMember(member);
                            }
                            if (filterMember(historical.getMember(), query.getMembers())) {
                                sessions.add(historical);
                                index++;
                            }
                        }
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Checks if the index of the given member belongs to the specified array.
     *
     * @param member member to search.
     * @param indexes
     * @return
     */
    private boolean filterMember(AmphiroSession.Member member, int[] indexes) {
        if ((indexes == null) || (indexes.length == 0)) {
            return true;
        }
        if (member == null) {
            return false;
        }

        return ArrayUtils.contains(indexes, member.getIndex());
    }

    /**
     * Computes aggregates of session values e.g. volume and energy over a time
     * interval for server users.
     *
     * @param query the query to execute.
     * @return a list of data series.
     * @throws ApplicationException if query execution fails.
     */
    @Override
    public ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException {
        Table table = null;
        ResultScanner scanner = null;

        ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        for (ExpandedPopulationFilter filter : query.getGroups()) {
            result.add(new GroupDataSeries(filter.getLabel(), filter.getUsers().size(), filter.getAreaId()));
        }
        try {
            table = connection.getTable(amphiroTableSessionByTime);
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

                        byte[] userHash = Arrays.copyOfRange(r.getRow(), 10, 26);

                        Long timestamp = null;
                        Integer duration = null;
                        Float volume = null, energy = null, temperature = null, flow = null;

                        for (Entry<byte[], byte[]> entry : map.entrySet()) {
                            String qualifier = Bytes.toString(entry.getKey());

                            // Always the historical values override the
                            // real-time ones except for the timestamp
                            switch (getSessionColumn(qualifier, false)) {
                                case COLUMN_RT_SESSION_TIMESTAMP:
                                    timestamp = Bytes.toLong(entry.getValue());
                                    break;
                                case COLUMN_RT_SESSION_VOLUME:
                                    if (volume == null) {
                                        volume = Bytes.toFloat(entry.getValue());
                                    }
                                    break;
                                case COLUMN_RT_SESSION_ENERGY:
                                    if (energy == null) {
                                        energy = Bytes.toFloat(entry.getValue());
                                    }
                                    break;
                                case COLUMN_RT_SESSION_DURATION:
                                    if (duration == null) {
                                        duration = Bytes.toInt(entry.getValue());
                                    }
                                    break;
                                case COLUMN_RT_SESSION_TEMPERATURE:
                                    if (temperature == null) {
                                        temperature = Bytes.toFloat(entry.getValue());
                                    }
                                    break;
                                case COLUMN_RT_SESSION_FLOW:
                                    if (flow == null) {
                                        flow = Bytes.toFloat(entry.getValue());
                                    }
                                    break;
                                case COLUMN_HIST_SESSION_TIMESTAMP:
                                    if (timestamp == null) {
                                        timestamp = Bytes.toLong(entry.getValue());
                                    }
                                    break;
                                case COLUMN_HIST_SESSION_VOLUME:
                                    volume = Bytes.toFloat(entry.getValue());
                                    break;
                                case COLUMN_HIST_SESSION_ENERGY:
                                    energy = Bytes.toFloat(entry.getValue());
                                    break;
                                case COLUMN_HIST_SESSION_DURATION:
                                    duration = Bytes.toInt(entry.getValue());
                                    break;
                                case COLUMN_HIST_SESSION_TEMPERATURE:
                                    temperature = Bytes.toFloat(entry.getValue());
                                    break;
                                case COLUMN_HIST_SESSION_FLOW:
                                    flow = Bytes.toFloat(entry.getValue());
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
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

    /**
     * Removes metrics from a result that are not supported by the query.
     *
     * @param query
     *            the query.
     * @param result
     *            the result.
     */
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

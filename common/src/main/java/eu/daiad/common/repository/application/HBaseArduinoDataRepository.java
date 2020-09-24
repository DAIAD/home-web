package eu.daiad.common.repository.application;

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
import org.springframework.stereotype.Repository;

import eu.daiad.common.hbase.EnumHBaseColumnFamily;
import eu.daiad.common.model.arduino.ArduinoIntervalQuery;
import eu.daiad.common.model.arduino.ArduinoIntervalQueryResult;
import eu.daiad.common.model.arduino.ArduinoMeasurement;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.repository.AbstractHBaseRepository;

@Repository()
public class HBaseArduinoDataRepository extends AbstractHBaseRepository implements IArduinoDataRepository {

    private final String arduinoTableMeasurements = "daiad:arduino-measurements";

    private static final Log logger = LogFactory.getLog(HBaseArduinoDataRepository.class);


    @SuppressWarnings("resource")
    @Override
    public void storeData(String deviceKey, ArrayList<ArduinoMeasurement> data) throws ApplicationException {
        Table table = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(arduinoTableMeasurements);
            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] deviceKeyBytes = deviceKey.getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKeyBytes);

            for (int i = 0; i < data.size(); i++) {
                ArduinoMeasurement m = data.get(i);

                if (m.getVolume() <= 0) {
                    continue;
                }

                long timestamp = (Long.MAX_VALUE - (m.getTimestamp() / 1000));

                long timeSlice = timestamp % 3600;
                byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
                if (timeSliceBytes.length != 2) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                long timeBucket = timestamp - timeSlice;

                byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
                if (timeBucketBytes.length != 8) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                byte[] rowKey = new byte[deviceKeyHash.length + timeBucketBytes.length];
                System.arraycopy(deviceKeyHash, 0, rowKey, 0, deviceKeyHash.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, (deviceKeyHash.length), timeBucketBytes.length);

                Put p = new Put(rowKey);

                byte[] column = concatenate(timeSliceBytes, appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

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

    private byte[] getDeviceTimeRowKey(byte[] deviceKeyHash, long date, EnumTimeInterval interval) throws Exception {

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

        long timestamp = Long.MAX_VALUE - (date / 1000);
        long timeSlice = timestamp % intervalInSeconds;
        long timeBucket = timestamp - timeSlice;
        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

        byte[] rowKey = new byte[deviceKeyHash.length + 8];
        System.arraycopy(deviceKeyHash, 0, rowKey, 0, deviceKeyHash.length);
        System.arraycopy(timeBucketBytes, 0, rowKey, deviceKeyHash.length, timeBucketBytes.length);

        return rowKey;
    }


    @Override
    public ArduinoIntervalQueryResult searchData(ArduinoIntervalQuery query) throws ApplicationException {
        ArduinoIntervalQueryResult data = new ArduinoIntervalQueryResult();

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(arduinoTableMeasurements);
            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] deviceKeyBytes = query.getDeviceKey().getBytes("UTF-8");
            byte[] deviceKeyHash = md.digest(deviceKeyBytes);

            Scan scan = new Scan();
            scan.addFamily(columnFamily);

            byte[] startRow = getDeviceTimeRowKey(deviceKeyHash, query.getTimestampEnd(), EnumTimeInterval.HOUR);

            scan.setStartRow(startRow);

            byte[] stopRow = new byte[startRow.length + 1];
            System.arraycopy(getDeviceTimeRowKey(deviceKeyHash, query.getTimestampStart(), EnumTimeInterval.HOUR),
                            0, stopRow, 0, startRow.length);

            scan.setStopRow(stopRow);

            scanner = table.getScanner(scan);

            boolean isScanCompleted = false;

            for (Result r = scanner.next(); r != null; r = scanner.next()) {
                NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

                    long timestamp = (Long.MAX_VALUE - (timeBucket + (long) offset)) * 1000L;

                    int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                    byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

                    String qualifier = Bytes.toString(slice);
                    if (qualifier.equals("v")) {
                        if (timestamp < query.getTimestampStart()) {
                            isScanCompleted = true;
                            break;
                        }

                        if (timestamp <= query.getTimestampEnd()) {
                            long volume = Bytes.toLong(entry.getValue());

                            data.add(timestamp, volume);
                        }
                    }
                }
                if (isScanCompleted) {
                    break;
                }
            }

            Collections.sort(data.getMeasurements(), new Comparator<ArduinoMeasurement>() {

                @Override
                public int compare(ArduinoMeasurement o1, ArduinoMeasurement o2) {
                    if (o1.getTimestamp() <= o2.getTimestamp()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

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

}
package eu.daiad.web.repository.application;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumRankingType;
import eu.daiad.web.model.query.MeterUserDataPoint;


public class AbstractHBaseMeterAggregateDataRepository extends AbstractHBaseMeterDataRepository {

    public AbstractHBaseMeterAggregateDataRepository() {
        super();
    }

    protected long unixTimestampToLong(long timestamp, DateTimeZone timezone, EnumTimeAggregation granularity) {
        DateTime date = new DateTime(timestamp, timezone);

        long value = 0;

        switch(granularity) {
            case HOUR:
                value = date.getYear() * 1000000 +
                        date.getMonthOfYear() * 10000 +
                        date.getDayOfMonth() * 100 +
                        date.getHourOfDay();
                break;
            case DAY:
                value = date.getYear() * 1000000 +
                        date.getMonthOfYear() * 10000 +
                        date.getDayOfMonth() * 100;
                break;
            case WEEK:
                DateTime monday = date.withDayOfWeek(DateTimeConstants.MONDAY);

                value = monday.getYear() * 1000000 +
                        monday.getMonthOfYear() * 10000 +
                        monday.getDayOfMonth() * 100;
                break;
            case MONTH:
                value = date.getYear() * 1000000 +
                        date.getMonthOfYear() * 10000 + 100;
                break;
            case YEAR:
                value = date.getYear() * 1000000 + 10100;
                break;
            default:
                throw new IllegalArgumentException(String.format("Granularity [%s] is not supported.", granularity .toString()));
        }

        return value;
    }

    protected long longToUnixTimestamp(long time, DateTimeZone timezone, EnumTimeAggregation granularity) {
        int year, month, day, hour;

        hour = (int) (time % 100);
        time /= 100;
        day = (int) (time % 100);
        time /= 100;
        month = (int) (time % 100);
        year = (int) (time / 100);

        if (granularity == EnumTimeAggregation.ALL) {
            throw new IllegalArgumentException("Granularity level not supported.");
        }

        DateTime date = new DateTime(year, month, day, hour, 0, 0, timezone);

        return date.getMillis();
    }

    protected byte[] createAggregateRowKey(MessageDigest md, UUID groupKey, UUID areaKey, EnumTimeAggregation aggregation, long timestamp) throws Exception {
        String key = groupKey.toString();
        if (areaKey != null) {
            key += areaKey.toString();
        }
        byte[] groupHash = md.digest(key.getBytes("UTF-8"));

        if(aggregation == EnumTimeAggregation.ALL) {
            throw new IllegalArgumentException(String.format("Time aggregation level [%s] is not supported.", aggregation.toString()));
        }
        byte granularity = (byte) aggregation.getValue();

        byte[] timestampBytes = Bytes.toBytes(timestamp);

        byte[] rowKey = new byte[groupHash.length + 1 + timestampBytes.length];
        System.arraycopy(groupHash, 0, rowKey, 0, groupHash.length);
        System.arraycopy(new byte[] { granularity }, 0, rowKey, groupHash.length, 1);
        System.arraycopy(timestampBytes, 0, rowKey, (groupHash.length + 1), timestampBytes.length);

        return rowKey;
    }

    protected List<MeterUserDataPoint> parseMeterUserDataPoints(EnumRankingType ranking, long timestamp, String data, int limit) {
        List<MeterUserDataPoint> users = new ArrayList<MeterUserDataPoint>();

        String[] tokens = StringUtils.split(data, ";");
        if (tokens.length % 4 != 0) {
            throw new IllegalArgumentException("Top/Bottom-k user entry number of tokens is not valid.");
        }

        switch (ranking) {
            case TOP:
                for (int i = Math.max(tokens.length / 4 - limit, 0), count = tokens.length / 4; i < count; i++) {
                    MeterUserDataPoint user = new MeterUserDataPoint(UUID.fromString(tokens[i * 4]), tokens[i * 4 + 1]);
                    user.getVolume().put(EnumMetric.SUM, (double) Float.parseFloat(tokens[i * 4 + 3]));

                    users.add(user);
                }
                break;
            case BOTTOM:
                for (int i = 0, count = Math.min(tokens.length / 4, limit); i < count; i++) {
                    MeterUserDataPoint user = new MeterUserDataPoint(UUID.fromString(tokens[i * 4]), tokens[i * 4 + 1]);
                    user.getVolume().put(EnumMetric.SUM, (double) Float.parseFloat(tokens[i * 4 + 3]));

                    users.add(user);
                }
                break;
            default:
                throw new IllegalArgumentException(String.format("Ranking [%s] is not supported.", ranking));
        }

        return users;
    }
}

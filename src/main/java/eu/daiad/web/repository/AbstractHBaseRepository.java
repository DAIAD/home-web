package eu.daiad.web.repository;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.hbase.HConstants;

public class AbstractHBaseRepository extends BaseRepository {

    protected int inArray(ArrayList<byte[]> array, byte[] hash) {
        int index = 0;
        for (byte[] entry : array) {
            if (Arrays.equals(entry, hash)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    protected byte[] concatenate(byte[] a, byte[] b) {
        int lengthA = a.length;
        int lengthB = b.length;
        byte[] concat = new byte[lengthA + lengthB];
        System.arraycopy(a, 0, concat, 0, lengthA);
        System.arraycopy(b, 0, concat, lengthA, lengthB);
        return concat;
    }

    protected byte[] appendLength(byte[] array) throws Exception {
        byte[] length = { (byte) array.length };

        return concatenate(length, array);
    }

    protected byte[] calculateTheClosestNextRowKeyForPrefix(byte[] rowKeyPrefix) {
        // Essentially we are treating it like an 'unsigned very very long' and
        // doing +1 manually.
        // Search for the place where the trailing 0xFFs start
        int offset = rowKeyPrefix.length;
        while (offset > 0) {
            if (rowKeyPrefix[offset - 1] != (byte) 0xFF) {
                break;
            }
            offset--;
        }

        if (offset == 0) {
            // We got an 0xFFFF... (only FFs) stopRow value which is
            // the last possible prefix before the end of the table.
            // So set it to stop at the 'end of the table'
            return HConstants.EMPTY_END_ROW;
        }

        // Copy the right length of the original
        byte[] newStopRow = Arrays.copyOfRange(rowKeyPrefix, 0, offset);
        // And increment the last one
        newStopRow[newStopRow.length - 1]++;
        return newStopRow;
    }

}

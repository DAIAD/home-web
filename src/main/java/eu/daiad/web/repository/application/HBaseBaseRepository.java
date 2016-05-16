package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class HBaseBaseRepository {

	private static final Log logger = LogFactory.getLog(HBaseBaseRepository.class);

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

	protected String jsonToString(Object value) {
		if (value == null) {
			return StringUtils.EMPTY;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
		} catch (Exception ex) {
			logger.warn(String.format("Failed to serialize object of type [%s]", value.getClass().getName()));
		}

		return StringUtils.EMPTY;
	}

}

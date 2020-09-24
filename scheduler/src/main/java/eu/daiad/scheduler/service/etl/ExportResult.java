package eu.daiad.scheduler.service.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the results of an export operation.
 */
public class ExportResult {

    /**
     * Messages generated during the export process.
     */
    private List<Message> messages = new ArrayList<Message>();

    /**
     * List of files to be added to the final archive.
     */
    private List<FileLabelPair> files = new ArrayList<FileLabelPair>();

    /**
     * Total number of exported rows.
     */
    private long totalRows = 0;

    public List<FileLabelPair> getFiles() {
        return files;
    }

    public long getTotalRows() {
        return totalRows;
    }

    /**
     * Increments total number of exported rows by one.
     */
    public void increment() {
        this.increment(1L);
    }

    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Checks if the result is empty.
     * @return true if no file exists or all files have no data rows; Otherwise false
     */
    public boolean isEmpty() {
        if (files.isEmpty()) {
            return true;
        }
        for (FileLabelPair f : files) {
            if (!f.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Increments total number of exported rows by {@code value}.
     *
     * @param value the value to increment the total number of exported rows.
     */
    public void increment(long value) {
        totalRows += value;
    }

    /**
     * Adds a message to the result.
     *
     * @param userKey unique user key.
     * @param username user name.
     * @param deviceKey unique device key.
     * @param message message.
     */
    public void addMessage(UUID userKey, String username, UUID deviceKey, String message) {
        this.messages.add(new Message(userKey, username, deviceKey, message));
    }

    public static class Message {

        private UUID userKey;

        private String username;

        private UUID deviceKey;

        private String message;

        public Message(UUID userKey, String username, UUID deviceKey, String message) {
            this.userKey = userKey;
            this.deviceKey = deviceKey;
            this.username = username;
            this.message = message;
        }

        public UUID getUserKey() {
            return userKey;
        }

        public String getUsername() {
            return username;
        }

        public UUID getDeviceKey() {
            return deviceKey;
        }

        public String getMessage() {
            return message;
        }

    }
}

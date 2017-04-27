package eu.daiad.web.model.query.savings;

import java.util.HashMap;
import java.util.Map;

public class SavingsClusterMonth {

    /**
     * Month index from 1 to 12
     */
    public int index;

    /**
     * Total savings volume in liters for the cluster population
     */
    public double volume;

    /**
     * Total savings percent for the cluster population
     */
    public double percent;

    public Map<String, Consumer> consumers = new HashMap<String, Consumer>();

    public Consumer add(String serial, String waterIq, double deviation) {
        Consumer consumer = new Consumer();
        consumer.serial = serial;
        consumer.waterIq = waterIq;
        consumer.deviation = deviation;

        consumers.put(serial, consumer);

        return consumer;
    }

    public static class Consumer {

        /**
         * Smart water meter unique serial number
         */
        public String serial;

        /**
         * Water IQ score from A to F
         */
        public String waterIq;

        /**
         * Difference of the user total consumption and the average total consumption for the cluster population
         */
        public double deviation;

    }
}
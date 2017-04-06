package eu.daiad.web.model.query.savings;

import java.util.HashMap;
import java.util.Map;

public class SavingsClusterMonth {

    public int index;

    public double volume;

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

        public String serial;

        public String waterIq;

        public double deviation;

    }
}
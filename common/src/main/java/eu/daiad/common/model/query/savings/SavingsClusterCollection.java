package eu.daiad.common.model.query.savings;

import java.util.ArrayList;
import java.util.List;

public class SavingsClusterCollection {

    public List<SavingsCluster> clusters = new ArrayList<SavingsCluster>();

    public SavingsCluster getByName(String name) {
        for (SavingsCluster c : clusters) {
            if (c.name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return add(name);
    }

    public SavingsCluster add(String name) {
        SavingsCluster cluster = new SavingsCluster(name);
        clusters.add(cluster);
        return cluster;
    }

}
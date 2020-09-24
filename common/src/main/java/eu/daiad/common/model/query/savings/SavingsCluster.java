package eu.daiad.common.model.query.savings;

import java.util.HashMap;
import java.util.Map;

public class SavingsCluster {

    public String name;

    public Map<Integer, SavingsClusterMonth> months = new HashMap<Integer, SavingsClusterMonth>();

    public SavingsCluster(String name) {
        this.name = name;
    }

    public SavingsClusterMonth getByIndex(int index) {
        if (months.containsKey(index)) {
            return months.get(index);
        }
        throw new NullPointerException(String.format("Month [%d] does not exist in cluster [%s]", index, name));
    }

    public SavingsClusterMonth add(int index, double volume, double percent) {
        SavingsClusterMonth month = new SavingsClusterMonth();
        month.index = index;
        month.volume = volume;
        month.percent = percent;

        months.put(index, month);

        return month;
    }

}

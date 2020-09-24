package eu.daiad.common.model.query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpandedPopulationFilter {

    private EnumPopulationFilterType type = EnumPopulationFilterType.UNDEFINED;

    private int size;

    private String label;

    private Long areaId;

    private UUID groupKey;

    private UUID areaKey;

    private Ranking ranking;

    private List<UUID> userKeys = new ArrayList<UUID>();

    private List<String> labels = new ArrayList<String>();

    private List<byte[]> userKeyHashes = new ArrayList<byte[]>();

    private List<byte[]> serialHashes = new ArrayList<byte[]>();

    public ExpandedPopulationFilter(PopulationFilter filter, int size) {
        this.size = size;

        type = filter.getType();
        label = filter.getLabel();
        ranking = filter.getRanking();
        switch (filter.getType()) {
            case USER:
                groupKey = null;
                break;
            case GROUP:
                groupKey = ((GroupPopulationFilter) filter).getGroup();
                break;
            case UTILITY:
                groupKey = ((UtilityPopulationFilter) filter).getUtility();
                break;
            default:
                throw new IllegalArgumentException(String.format("Filter type [%s] is not supported.", filter.getType()));
        }
    }

    public ExpandedPopulationFilter(ExpandedPopulationFilter population) {
        type = population.type;
        label = population.label;
        areaId = population.areaId;
        areaKey = population.areaKey;
        ranking = population.ranking;
        groupKey = population.groupKey;
    }

    public ExpandedPopulationFilter(ExpandedPopulationFilter population, long areaId, UUID areaKey) {
        type = population.type;
        label = population.label;
        ranking = population.ranking;
        groupKey = population.groupKey;

        this.areaId = areaId;
        this.areaKey = areaKey;
    }

    public EnumPopulationFilterType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<UUID> getUserKeys() {
        return userKeys;
    }

    public List<byte[]> getUserKeyHashes() {
        return userKeyHashes;
    }

    public List<byte[]> getSerialHashes() {
        return serialHashes;
    }

    public List<String> getLabels() {
        return labels;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public Long getAreaId() {
        return areaId;
    }

    public UUID getGroupKey() {
        return groupKey;
    }

    public UUID getAreaKey() {
        return areaKey;
    }

    public int getSize() {
        if(!userKeys.isEmpty()) {
            return userKeys.size();
        }
        return size;
    }

}

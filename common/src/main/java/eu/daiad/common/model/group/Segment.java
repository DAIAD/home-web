package eu.daiad.common.model.group;

public class Segment extends Group {

    private String cluster;

    @Override
    public EnumGroupType getType() {
        return EnumGroupType.SEGMENT;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}

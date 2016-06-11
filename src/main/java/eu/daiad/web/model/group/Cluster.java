package eu.daiad.web.model.group;

import java.util.ArrayList;
import java.util.List;

public class Cluster extends Group {

    private List<Segment> segments = new ArrayList<Segment>();

    public List<Segment> getSegments() {
        return segments;
    }

    @Override
    public EnumGroupType getType() {
        return EnumGroupType.CLUSTER;
    }
}

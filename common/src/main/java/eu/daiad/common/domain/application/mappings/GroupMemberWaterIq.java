package eu.daiad.common.domain.application.mappings;

import javax.persistence.Basic;
import javax.persistence.Id;

public class GroupMemberWaterIq {

    public GroupMemberWaterIq(int id, double volume, String value) {
        this.id = id;
        this.volume = volume;
        this.value = value;
    }

    @Id()
    @Basic()
    public int id;

    @Basic()
    public double volume;

    @Basic()
    public String value;
}

package eu.daiad.common.domain.application.mappings;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

public class SavingScenarioSegmentEntity {

    public SavingScenarioSegmentEntity(int id,
                                 String clusterName,
                                 UUID clusterKey,
                                 String segmentName,
                                 UUID segmentKey,
                                 double potential,
                                 double consumption) {
        this.id = id;
        this.clusterName = clusterName;
        this.clusterKey = clusterKey;
        this.segmentName = segmentName;
        this.segmentKey = segmentKey;
        this.potential = potential;
        this.consumption = consumption;
    }

    @Id()
    @Basic()
    public int id;

    @Column(name = "cluster_name")
    public String clusterName;

    @Column(name = "cluster_key")
    @Type(type = "pg-uuid")
    public UUID clusterKey;

    @Column(name = "segment_name")
    public String segmentName;

    @Column(name = "segment_key")
    @Type(type = "pg-uuid")
    public UUID segmentKey;

    @Column()
    public double potential;

    @Column()
    public double consumption;

}

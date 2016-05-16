package eu.daiad.web.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.group.EnumGroupType;

@Entity(name = "group_cluster")
@Table(schema = "public", name = "group_cluster")
public class GroupCluster extends Group {

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "cluster_id", nullable = false)
	private Account cluster;

	@Override
	public EnumGroupType getType() {
		return EnumGroupType.CLUSTER;
	}

	public Account getCluster() {
		return cluster;
	}

	public void setCluster(Account cluster) {
		this.cluster = cluster;
	}

}

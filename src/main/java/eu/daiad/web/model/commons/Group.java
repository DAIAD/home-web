package eu.daiad.web.model.commons;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public class Group {

	private UUID key;

	private Utility utility;

	private String name;

	private long createdOn;

	private Geometry geometry;

	private Set<GroupMember> members = new HashSet<GroupMember>();

	private int size;

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

	public Utility getUtility() {
		return utility;
	}

	public void setUtility(Utility utility) {
		this.utility = utility;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Set<GroupMember> getMembers() {
		return members;
	}

	public static class GroupMember {

		private UUID key;

		private String username;

		private long addeOn;

		private String fullname;

		public UUID getKey() {
			return key;
		}

		public void setKey(UUID key) {
			this.key = key;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public long getAddeOn() {
			return addeOn;
		}

		public void setAddeOn(long addeOn) {
			this.addeOn = addeOn;
		}

		public String getFullname() {
			return fullname;
		}

		public void setFullname(String fullname) {
			this.fullname = fullname;
		}

	}
}

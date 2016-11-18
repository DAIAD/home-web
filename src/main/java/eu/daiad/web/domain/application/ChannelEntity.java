package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "channel")
@Table(schema = "public", name = "channel")
public class ChannelEntity {
    
	@Id()
	@Column(name = "id")
	private int id;

    @Basic()
    @Column(name = "name")
    private String name;    

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

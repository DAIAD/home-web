package eu.daiad.web.domain.application;

import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.model.query.DataQuery;

@Entity(name = "data_query")
@Table(schema = "public", name = "data_query")
public class DataQueryEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "data_query_id_seq", name = "data_query_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "data_query_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne()
    @JoinColumn(name = "account_id", nullable = false)
    private Account owner;

    @Basic
    private String name;

    @Basic
    private String query;

    @Column(name = "date_modified")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedOn = new DateTime();

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public long getId() {
        return id;
    }

    public DataQuery toDataQuery() throws JsonParseException, JsonMappingException, IOException {
        if (StringUtils.isBlank(this.query)) {
            return null;
        }

        return (new ObjectMapper()).readValue(query, DataQuery.class);
    }

}

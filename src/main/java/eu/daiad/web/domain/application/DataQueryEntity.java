package eu.daiad.web.domain.application;

import java.io.IOException;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.EnumQueryFavouriteType;

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
    private AccountEntity owner;

    @Basic
    private String name;

    @Basic
    private String query;
 
    @Basic    
    private String tags;
    
    @Column(name = "report_name")
    private String reportName;    

    @Column(name = "time_level")
    private String level; 

    @Column(name = "field")
    private String field; 
    
    @Column(name = "overlap")
    private String overlap;     

    @Column(name = "pinned")
    private boolean pinned; 
    
    @Enumerated(EnumType.STRING)
    private EnumQueryFavouriteType type;

    @Column(name = "date_modified")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedOn = new DateTime();
    
    public EnumQueryFavouriteType getType() {
        return type;
    }

    public void setType(EnumQueryFavouriteType type) {
        this.type = type;
    }
    public AccountEntity getOwner() {
        return owner;
    }

    public void setOwner(AccountEntity owner) {
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

    public List<DataQuery> toDataQuery() throws JsonParseException, JsonMappingException, IOException {
        if (StringUtils.isBlank(this.query)) {
            return null;
        }
        
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JodaModule(), new ShapesAsGeoJSONModule());
        ObjectMapper objectMapper = builder.build();
        List<DataQuery> queries = objectMapper.readValue(query, new TypeReference<List<DataQuery>>(){});

        return queries;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }    

    public String getOverlap() {
        return overlap;
    }

    public void setOverlap(String overlap) {
        this.overlap = overlap;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}

package eu.daiad.common.model.export;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class DataExportFileQuery {

    @JsonIgnore
    private List<Integer> utilities;

    @JsonIgnore
    private Integer days;

    @JsonDeserialize(using = EnumDataExportType.Deserializer.class)
    private EnumDataExportType type;

    private Integer index;

    private Integer size;

    public List<Integer> getUtilities() {
        return utilities;
    }

    public void setUtilities(List<Integer> utilities) {
        this.utilities = utilities;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public EnumDataExportType getType() {
        return type;
    }

    public void setType(EnumDataExportType type) {
        this.type = type;
    }

}

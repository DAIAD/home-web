package eu.daiad.web.model.export;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataExportFileQuery {

    @JsonIgnore
    private int utilityId;

    @JsonIgnore
    private Integer days;

    private int index;

    private int size;

    public int getUtilityId() {
        return utilityId;
    }

    public void setUtilityId(int utilityId) {
        this.utilityId = utilityId;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}

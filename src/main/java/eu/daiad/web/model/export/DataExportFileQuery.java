package eu.daiad.web.model.export;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataExportFileQuery {

    @JsonIgnore
    private List<Integer> utilities;

    @JsonIgnore
    private Integer days;

    private int index;

    private int size;

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

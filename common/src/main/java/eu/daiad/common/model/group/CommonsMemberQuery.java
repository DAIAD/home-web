package eu.daiad.common.model.group;

import java.io.IOException;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CommonsMemberQuery {

    @JsonIgnore
    private UUID groupKey;

    private String name;

    private DateTime joinedOn;

    private Integer pageIndex = 0;

    private Integer pageSize = 10;

    private EnumSortProperty sortBy = EnumSortProperty.FIRSTNAME;

    private boolean sortAscending = true;

    public UUID getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(UUID groupKey) {
        this.groupKey = groupKey;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public EnumSortProperty getSortBy() {
        return sortBy;
    }

    public void setSortBy(EnumSortProperty sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getJoinedOn() {
        return joinedOn;
    }

    public void setJoinedOn(DateTime joinedOn) {
        this.joinedOn = joinedOn;
    }

    public static enum EnumSortProperty {
        FIRSTNAME, LASTNAME, DATE_JOINED;

        public static EnumSortProperty fromString(String value) {
            for (EnumSortProperty item : EnumSortProperty.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EnumSortProperty.FIRSTNAME;
        }

        public static class Deserializer extends JsonDeserializer<EnumSortProperty> {

            @Override
            public EnumSortProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
                return EnumSortProperty.fromString(parser.getValueAsString());
            }
        }
    }
}

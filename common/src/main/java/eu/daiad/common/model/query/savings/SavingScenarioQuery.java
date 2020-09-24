package eu.daiad.common.model.query.savings;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SavingScenarioQuery {

    private Integer pageIndex = 0;

    private Integer pageSize = 10;

    @JsonIgnore()
    private Integer ownerId;

    private String name;

    @JsonDeserialize(using = EnumSavingScenarioStatus.Deserializer.class)
    private EnumSavingScenarioStatus status = EnumSavingScenarioStatus.UNDEFINED;

    private EnumSortProperty sortBy = EnumSortProperty.NAME;

    private boolean sortAscending = true;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumSavingScenarioStatus getStatus() {
        if (status == null) {
            return EnumSavingScenarioStatus.UNDEFINED;
        }
        return status;
    }

    public void setStatus(EnumSavingScenarioStatus status) {
        this.status = status;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
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

    public static enum EnumSortProperty {
        NAME, CREATED_ON, STATUS;

        public static EnumSortProperty fromString(String value) {
            for (EnumSortProperty item : EnumSortProperty.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EnumSortProperty.NAME;
        }

        public static class Deserializer extends JsonDeserializer<EnumSortProperty> {

            @Override
            public EnumSortProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
                return EnumSortProperty.fromString(parser.getValueAsString());
            }
        }
    }

}

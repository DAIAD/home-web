package eu.daiad.web.model.group;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CommonsQuery extends GroupQuery {

    private Integer pageIndex = 0;

    private Integer pageSize = 10;

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
        NAME, SIZE, AREA;

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
            public EnumSortProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException,
                            JsonProcessingException {
                return EnumSortProperty.fromString(parser.getValueAsString());
            }
        }
    }
}

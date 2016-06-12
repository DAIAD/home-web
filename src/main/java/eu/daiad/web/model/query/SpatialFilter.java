package eu.daiad.web.model.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = CustomSpatialFilter.class, name = "CUSTOM"),
                @Type(value = AreaSpatialFilter.class, name = "AREA"),
                @Type(value = GroupSpatialFilter.class, name = "GROUP"),
                @Type(value = ConstraintSpatialFilter.class, name = "CONSTRAINT") })
public abstract class SpatialFilter {

    public abstract EnumSpatialFilterType getType();

}

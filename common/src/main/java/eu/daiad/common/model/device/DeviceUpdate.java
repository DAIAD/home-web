package eu.daiad.common.model.device;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.common.model.KeyValuePair;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = AmphiroDeviceUpdate.class, name = "AMPHIRO") })
public class DeviceUpdate {

    @JsonDeserialize(using = EnumDeviceType.Deserializer.class)
    private EnumDeviceType type;

    private UUID key;

    private List<KeyValuePair> properties = new ArrayList<KeyValuePair>();

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public List<KeyValuePair> getProperties() {
        return this.properties;
    }

    public KeyValuePair getProperty(String key) {
        for (KeyValuePair property : this.properties) {
            if (property.getKey().equals(key)) {
                return property;
            }
        }

        return null;
    }

    public void setProperties(List<KeyValuePair> value) {
        this.properties = value;
    }

    public EnumDeviceType getType() {
        return this.type;
    }

    public void setType(EnumDeviceType value) {
        this.type = value;
    }

}

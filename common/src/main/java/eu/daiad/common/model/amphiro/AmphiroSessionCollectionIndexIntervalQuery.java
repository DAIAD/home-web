package eu.daiad.common.model.amphiro;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.common.model.AuthenticatedRequest;

public class AmphiroSessionCollectionIndexIntervalQuery extends AuthenticatedRequest {

    @JsonIgnore
    private UUID userKey;

    @JsonDeserialize(using = EnumIndexIntervalQuery.Deserializer.class)
    private EnumIndexIntervalQuery type = EnumIndexIntervalQuery.ABSOLUTE;

    private UUID deviceKey[];

    private Long startIndex;

    private Long endIndex;

    private Integer length;

    private int[] members;

    @JsonIgnore
    public UUID getUserKey() {
        return userKey;
    }

    @JsonProperty
    public void setUserKey(UUID userKey) {
        this.userKey = userKey;
    }

    public EnumIndexIntervalQuery getType() {
        return type;
    }

    public void setType(EnumIndexIntervalQuery type) {
        this.type = type;
    }

    public UUID[] getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(UUID[] deviceKey) {
        this.deviceKey = deviceKey;
    }

    public Long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Long startIndex) {
        this.startIndex = startIndex;
    }

    public Long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Long endIndex) {
        this.endIndex = endIndex;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public int[] getMembers() {
        return members;
    }

    public void setMembers(int[] members) {
        this.members = members;
    }

}

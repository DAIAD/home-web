package eu.daiad.web.model.profile;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.EnumGender;

public class HouseholdMember {

    private int index;

    private boolean active;

    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private EnumGender gender;

    private byte photo[];

    @JsonIgnore
    private DateTime createdOn;

    @JsonIgnore
    private DateTime updatedOn;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public EnumGender getGender() {
        return gender;
    }

    public void setGender(EnumGender gender) {
        this.gender = gender;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    @JsonProperty
    public DateTime getCreatedOn() {
        return createdOn;
    }

    @JsonIgnore
    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    @JsonProperty
    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    @JsonIgnore
    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}

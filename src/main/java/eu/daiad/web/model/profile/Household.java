package eu.daiad.web.model.profile;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.domain.application.HouseholdEntity;
import eu.daiad.web.domain.application.HouseholdMemberEntity;
import eu.daiad.web.model.EnumGender;

public class Household {

    private List<HouseholdMember> members = new ArrayList<HouseholdMember>();

    @JsonIgnore
    private DateTime createdOn;

    @JsonIgnore
    private DateTime updatedOn;

    public Household() {

    }

    public Household(HouseholdEntity entity) {
        this.createdOn = entity.getCreatedOn();
        this.updatedOn = entity.getUpdatedOn();

        for (HouseholdMemberEntity m : entity.getMembers()) {
            HouseholdMember member = new HouseholdMember();

            member.setAge(m.getAge());
            member.setGender(m.getGender());
            member.setIndex(m.getIndex());
            member.setName(m.getName());
            member.setPhoto(m.getPhoto());

            member.setCreatedOn(m.getCreatedOn());
            member.setUpdatedOn(m.getUpdatedOn());

            this.members.add(member);
        }
    }

    public List<HouseholdMember> getMembers() {
        return members;
    }

    public void setMembers(List<HouseholdMember> members) {
        this.members = members;
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

    public int getTotalMembers() {
        if (members.isEmpty()) {
            return 0;
        }
        return members.size();
    }

    public int getMaleMembers() {
        if (members.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (HouseholdMember m : members) {
            if (m.getGender() == EnumGender.MALE) {
                count++;
            }
        }
        return count;
    }

    public int getFemaleMembers() {
        if (members.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (HouseholdMember m : members) {
            if (m.getGender() == EnumGender.FEMALE) {
                count++;
            }
        }
        return count;
    }

}

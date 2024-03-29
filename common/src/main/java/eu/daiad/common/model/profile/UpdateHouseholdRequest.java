package eu.daiad.common.model.profile;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.AuthenticatedRequest;

public class UpdateHouseholdRequest extends AuthenticatedRequest {

    private List<HouseholdMember> members = new ArrayList<HouseholdMember>();

    public List<HouseholdMember> getMembers() {
        if (members == null) {
            return new ArrayList<HouseholdMember>();
        }
        return members;
    }

    public void setMembers(List<HouseholdMember> members) {
        this.members = members;
    }

    public HouseholdMember getMember(int index) {
        for (HouseholdMember m : members) {
            if (m.getIndex() == index) {
                return m;
            }
        }

        return null;
    }

}

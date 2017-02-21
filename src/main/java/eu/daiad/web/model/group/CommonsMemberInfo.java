package eu.daiad.web.model.group;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.domain.application.GroupMemberEntity;

public class CommonsMemberInfo {

    private UUID key;

    private String firstname;

    private String lastname;

    private DateTime joinedOn;

    private String ranking;

    public CommonsMemberInfo(GroupMemberEntity member, String ranking) {
        key = member.getAccount().getKey();
        firstname = member.getAccount().getFirstname();
        lastname = member.getAccount().getLastname();
        joinedOn = member.getCreatetOn();
        this.ranking = ranking;
    }

    public UUID getKey() {
        return key;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public DateTime getJoinedOn() {
        return joinedOn;
    }

    public String getRanking() {
        return ranking;
    }

}

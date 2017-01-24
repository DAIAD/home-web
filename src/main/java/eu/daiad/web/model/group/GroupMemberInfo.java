package eu.daiad.web.model.group;

import java.util.UUID;

import eu.daiad.web.domain.application.AccountEntity;

public class GroupMemberInfo {

	private UUID id;

	private String name;

	private String email;

	private long createdOn;

	public GroupMemberInfo(AccountEntity account){
		id = account.getKey();
		if (account.getFirstname() != null || account.getLastname() != null){
			name = account.getFirstname() + " " + account.getLastname();
		} else {
			name = account.getEmail();
		}

		email = account.getEmail();
		createdOn = account.getCreatedOn().getMillis();
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

    public long getCreatedOn() {
        return createdOn;
    }

}
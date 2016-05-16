package eu.daiad.web.model.group;

import java.util.UUID;

import eu.daiad.web.domain.application.Account;

public class GroupMemberInfo {
	
	private UUID id;
	
	private String name;
	
	private String email;
	
	private long registrationDateMils;
	
	public GroupMemberInfo(Account account){
		this.id = account.getKey();
		if (account.getFirstname() != null || account.getLastname() != null){
			this.name = account.getFirstname() + " " + account.getLastname();
		} else {
			this.name = account.getEmail();
		}
		
		this.email = account.getEmail();
		this.registrationDateMils = account.getCreatedOn().getMillis();
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

	public long getRegistrationDateMils() {
		return registrationDateMils;
	}
	
	
}
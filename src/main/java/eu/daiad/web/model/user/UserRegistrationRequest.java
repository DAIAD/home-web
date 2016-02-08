package eu.daiad.web.model.user;

import javax.validation.Valid;


public class UserRegistrationRequest {

	@Valid
	private Account account;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
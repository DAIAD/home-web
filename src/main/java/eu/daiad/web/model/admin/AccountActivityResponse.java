package eu.daiad.web.model.admin;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class AccountActivityResponse extends RestResponse {

	private ArrayList<AccountActivity> accounts = new ArrayList<AccountActivity>();

	public ArrayList<AccountActivity> getAccounts() {
		return accounts;
	}

}

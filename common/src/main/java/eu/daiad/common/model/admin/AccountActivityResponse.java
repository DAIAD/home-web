package eu.daiad.common.model.admin;

import java.util.ArrayList;

import eu.daiad.common.model.RestResponse;

public class AccountActivityResponse extends RestResponse {

	private ArrayList<AccountActivity> accounts = new ArrayList<AccountActivity>();

	public ArrayList<AccountActivity> getAccounts() {
		return accounts;
	}

}

package eu.daiad.web.model.group;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class QueryConsumerResponse extends RestResponse {

	private ArrayList<Group> groups = new ArrayList<Group>();

	private ArrayList<Account> accounts = new ArrayList<Account>();

	public ArrayList<Group> getGroups() {
		return groups;
	}

	public ArrayList<Account> getAccounts() {
		return accounts;
	}

}

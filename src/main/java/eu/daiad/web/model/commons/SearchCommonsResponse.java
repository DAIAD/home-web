package eu.daiad.web.model.commons;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class SearchCommonsResponse extends RestResponse {

	private ArrayList<Community> communties = new ArrayList<Community>();

	public ArrayList<Community> getCommunties() {
		return communties;
	}
}

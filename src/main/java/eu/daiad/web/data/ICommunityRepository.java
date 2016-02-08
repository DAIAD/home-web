package eu.daiad.web.data;

import eu.daiad.web.model.commons.Community;
import eu.daiad.web.model.error.ApplicationException;

public interface ICommunityRepository {

	public abstract void create(Community community) throws ApplicationException;

}

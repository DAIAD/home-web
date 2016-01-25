package eu.daiad.web.data;

import org.joda.time.DateTime;

import eu.daiad.web.model.EnumGender;
import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.security.model.EnumRole;

public interface IUserRepository {

	void createDefaultUser();

	ApplicationUser createUser(String username, String password,
			String firstname, String lastname, EnumGender gender,
			DateTime birthdate, String country, String postalCode,
			String timezone) throws Exception;

	void setPassword(String username, String password) throws Exception;

	void setRole(String username, EnumRole role, boolean set) throws Exception;

	public ApplicationUser getUserByName(String username) throws Exception;

}

package eu.daiad.web.controller.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.data.IUserRepository;
import eu.daiad.web.model.PasswordChangeRequest;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.RoleUpdateRequest;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserRegistrationRequest;
import eu.daiad.web.model.user.UserRegistrationResponse;

@RestController("RestUserController")
public class UserController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(UserController.class);

	@Autowired
	private IUserRepository repository;

	@Autowired
	private org.springframework.validation.Validator validator;

	@Value("${security.white-list}")
	private boolean enforceWhiteListCheck;

	@RequestMapping(value = "/api/v1/user/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse register(@RequestBody UserRegistrationRequest request, BindingResult results) {
		RestResponse response = new RestResponse();

		try {
			if (enforceWhiteListCheck) {
				((OptionalValidatorFactoryBean) validator).validate(request, results, Account.AccountSimpleValidation.class);
			} else {
				((OptionalValidatorFactoryBean) validator).validate(request.getAccount(), results, Account.AccountDefaultValidation.class);
			}

			if (results.hasErrors()) {
				for (FieldError e : results.getFieldErrors()) {
					response.add(this.getError(e));
				}

				return response;
			}
			Account account = request.getAccount();

			Set<String> zones = DateTimeZone.getAvailableIDs();
			if ((!StringUtils.isBlank(account.getCountry()))
							&& ((account.getTimezone() == null) || (!zones.contains(account.getTimezone())))) {
				String country = account.getCountry().toUpperCase();

				Iterator<String> it = zones.iterator();
				while (it.hasNext()) {
					String zone = (String) it.next();
					String[] parts = StringUtils.split(zone, "/");
					if ((parts.length == 1) && (parts[0].toUpperCase().equals(country))) {
						account.setTimezone(zone);
						break;
					}
					if ((parts.length == 2) && (parts[1].toUpperCase().equals(country))) {
						account.setTimezone(zone);
						break;
					}
				}
			}

			if (account.getTimezone() == null) {
				account.setTimezone("Europe/Athens");
			}

			UUID key = repository.createUser(account);

			UserRegistrationResponse registerResponse = new UserRegistrationResponse();
			registerResponse.setUserKey(key.toString());

			return registerResponse;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/user/password", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse changePassword(@RequestBody PasswordChangeRequest data, BindingResult results) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_USER);

			repository.setPassword(data.getCredentials().getUsername(), data.getPassword());

			return new RestResponse();
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/user/role/grant", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse addRole(@RequestBody RoleUpdateRequest data, BindingResult results) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			AuthenticatedUser user = this.repository.getUserByName(data.getUsername());
			if (user == null) {
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put("username", data.getUsername());

				return this.createResponse(UserErrorCode.USERNANE_NOT_FOUND, properties);
			}
			if (data.getRole() == null) {
				logger.warn(String.format("Role does not exists.", data.getRole().toString()));
			} else {
				repository.setRole(data.getUsername(), data.getRole(), true);
			}

			return new RestResponse();
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/user/role/revoke", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse revokeRole(@RequestBody RoleUpdateRequest data, BindingResult results) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			AuthenticatedUser user = this.repository.getUserByName(data.getUsername());
			if (user == null) {
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put("username", data.getUsername());

				return this.createResponse(UserErrorCode.USERNANE_NOT_FOUND, properties);
			}
			if (data.getRole() == null) {
				logger.warn(String.format("Role does not exists.", data.getRole().toString()));
			} else {
				repository.setRole(data.getUsername(), data.getRole(), false);
			}

			return new RestResponse();
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}

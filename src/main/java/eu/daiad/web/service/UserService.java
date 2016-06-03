package eu.daiad.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.logging.MappedDiagnosticContextKeys;
import eu.daiad.web.logging.MappedDiagnosticContextValues;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.admin.AccountWhiteListEntry;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserRegistrationRequest;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;

@Service
public class UserService extends BaseService implements IUserService {

	@Value("${security.white-list}")
	private boolean enforceWhiteListCheck;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Override
	@Transactional("applicationTransactionManager")
	public UUID createUser(UserRegistrationRequest request) throws ApplicationException {
		try {
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

			UUID userKey = userRepository.createUser(account);

			if (enforceWhiteListCheck) {
				AccountWhiteListEntry entry = userRepository.getAccountWhiteListEntry(request.getAccount()
								.getUsername());
				if ((entry != null) && (!StringUtils.isBlank(entry.getMeterSerial()))) {

					Device device = deviceRepository.getWaterMeterDeviceBySerial(entry.getMeterSerial());

					if (device != null) {
						throw createApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id",
										entry.getMeterSerial());
					}

					ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();
					properties.add(new KeyValuePair("white-liest-entry.id", Integer.toString(entry.getId())));
					properties.add(new KeyValuePair("white-liest-entry.registration-date", (entry.getRegisteredOn()
									.toDateTime(DateTimeZone.UTC).toString())));

					deviceRepository.createMeterDevice(entry.getUsername(), entry.getMeterSerial(), properties,
									entry.getMeterLocation());
				}
			}

			return userKey;
		} catch (ApplicationException ex) {
			if (ex.getCode().equals(UserErrorCode.USERNANE_NOT_AVAILABLE)) {
				if (MDC.get(MappedDiagnosticContextKeys.USERNAME)
								.equals(MappedDiagnosticContextValues.UNKNOWN_USERNAME)) {
					MDC.put(MappedDiagnosticContextKeys.USERNAME, request.getAccount().getUsername());
				}
			}
			throw ex;
		} catch (Exception ex) {
			throw wrapApplicationException(ex);
		}
	}

	@Override
	@Transactional("applicationTransactionManager")
	public void setPassword(String username, String password) throws ApplicationException {
		userRepository.setPassword(username, password);
	}

	@Override
	@Transactional("applicationTransactionManager")
	public void setRole(String username, EnumRole role, boolean set) throws ApplicationException {
		AuthenticatedUser user = this.userRepository.getUserByName(username);
		if (user == null) {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("username", username);

			throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
		}

		if (role == null) {
			throw createApplicationException(UserErrorCode.NO_ROLE_SELECTED);
		} else {
			userRepository.setRole(username, role, set);
		}
	}
}

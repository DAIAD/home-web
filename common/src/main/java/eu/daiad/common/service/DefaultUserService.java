package eu.daiad.common.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.common.logging.MappedDiagnosticContextKeys;
import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.KeyValuePair;
import eu.daiad.common.model.admin.AccountWhiteListEntry;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.CaptchaErrorCode;
import eu.daiad.common.model.error.DeviceErrorCode;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.error.UserErrorCode;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.model.security.PasswordResetMailModel;
import eu.daiad.common.model.security.PasswordResetToken;
import eu.daiad.common.model.user.Account;
import eu.daiad.common.model.user.UserRegistrationRequest;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.service.mail.IMailService;
import eu.daiad.common.service.mail.Message;
import eu.daiad.common.service.security.IPasswordValidator;

@Service
public class DefaultUserService extends BaseService implements IUserService {

    private static final Log logger = LogFactory.getLog(DefaultUserService.class);

    private final static String PASSWORD_RESET_MAIL_TEMPLATE_HOME = "password-reset-web";

    private final static String PASSWORD_RESET_MAIL_TEMPLATE_UTILITY = "password-reset-web";

    private final static String PASSWORD_RESET_MAIL_TEMPLATE_MOBILE = "password-reset-mobile";

    @Value("${daiad.url}")
    private String baseSiteUrl;

    @Value("${security.white-list:true}")
    private boolean enforceWhiteListCheck;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IDeviceRepository deviceRepository;

    @Autowired
    private IPasswordValidator passwordValidator;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IGoogleReCaptchaService googleReCaptchaService;

    @Override
    @Transactional
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

                    deviceRepository.createMeterDevice(entry.getUsername(), entry.getMeterSerial(), properties, entry
                                    .getMeterLocation());
                }
            }

            return userKey;
        } catch (ApplicationException ex) {
            if (ex.getCode().equals(UserErrorCode.USERNANE_NOT_AVAILABLE)) {
                if (MDC.get(MappedDiagnosticContextKeys.USERNAME).equals("-")) {
                    MDC.put(MappedDiagnosticContextKeys.USERNAME, request.getAccount().getUsername());
                }
            }
            throw ex;
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Changes a user password.
     *
     * @param remoteAddress the client remote IP address.
     * @param captchaResponse the Google ReCAPTCAH response.
     * @param username the user name .
     * @param password the new password.
     * @throws ApplicationException if the user does not exist.
     */
    @Override
    public void changePassword(String remoteAddress, String captchaResponse, String username, String password) throws ApplicationException {
        if (!googleReCaptchaService.validate(remoteAddress, captchaResponse)) {
            throw createApplicationException(CaptchaErrorCode.CAPTCHA_VERIFICATION_ERROR);
        }

        this.changePassword(username, password);
    }

    @Override
    @Transactional
    public void changePassword(String username, String password) throws ApplicationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        // Check permissions
        if (authenticatedUser.hasRole(EnumRole.ROLE_USER)) {
            if (!authenticatedUser.getUsername().equals(username)) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }
        } else if (!authenticatedUser.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        // Validate request
        AuthenticatedUser user = userRepository.getUserByName(username);

        if (user == null) {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

        if (!authenticatedUser.getUtilities().contains(user.getUtilityId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        List<ErrorCode> errors = passwordValidator.validate(password);
        if (!errors.isEmpty()) {
            throw createApplicationException(errors.get(0));
        }

        userRepository.changePassword(username, password);
    }

    @Override
    @Transactional
    public void grantRole(String username, EnumRole role) throws ApplicationException {
        // Check authenticated user role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        if (!authenticatedUser.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        // Validate request
        if (role == null) {
            throw createApplicationException(UserErrorCode.NO_ROLE_SELECTED);
        }

        AuthenticatedUser user = userRepository.getUserByName(username);

        if (user == null) {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

        if (!authenticatedUser.getUtilities().contains(user.getUtilityId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        // Grant role
        userRepository.grantRole(username, role);
    }

    @Override
    @Transactional
    public void revokeRole(String username, EnumRole role) throws ApplicationException {
        // Check authenticated user role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        if (!authenticatedUser.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        // Validate request
        if (role == null) {
            throw createApplicationException(UserErrorCode.NO_ROLE_SELECTED);
        }

        AuthenticatedUser user = userRepository.getUserByName(username);

        if (user == null) {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

        if (!authenticatedUser.getUtilities().contains(user.getUtilityId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        if(authenticatedUser.getUsername().equals(username)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        // Revoke role
        userRepository.revokeRole(username, role);
    }

    @Override
    public UUID resetPasswordCreateToken(String username, EnumApplication application) throws ApplicationException {
        // Check application
        if ((application == null) || (application == EnumApplication.UNDEFINED)) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_APPLICATION_NOT_SUPPORTED);
        }

        // Find user
        AuthenticatedUser user = userRepository.getUserByName(username);
        if (user == null) {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

        // Administrator password reset must be performed manually
        if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        if (!user.isAllowPasswordReset()) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_NOT_ALLOWED);
        }

        // Generate password reset token, pin and target URL
        PasswordResetToken token = userRepository.createPasswordResetToken(application, username);

        String url = baseSiteUrl + "password/reset/" + token.getToken().toString() + "/";

        // Send email
        Message message = new Message(user.getLocale());

        message.setSubject(messageSource.getMessage("ResetPassword.Mail.Subject", null, "Password Reset", new Locale(user.getLocale())));

        if (StringUtils.isBlank(user.getFullname())) {
            message.setRecipients(user.getUsername());
        } else {
            message.setRecipients(user.getUsername(), user.getFullname());
        }

        message.setLocale(user.getLocale());
        message.getVariables().put("model", new PasswordResetMailModel(user, url, token.getPin()));

        switch(application) {
            case HOME:
                message.setTemplate(PASSWORD_RESET_MAIL_TEMPLATE_HOME);
                break;
            case UTILITY:
                message.setTemplate(PASSWORD_RESET_MAIL_TEMPLATE_UTILITY);
                break;
            case MOBILE:
                message.setTemplate(PASSWORD_RESET_MAIL_TEMPLATE_MOBILE);
                break;
            default:
                throw createApplicationException(UserErrorCode.PASSWORD_RESET_APPLICATION_NOT_SUPPORTED);
        }

        mailService.send(message);

        logger.warn(String.format("Password reset token has been created for user [%s].", username));

        return token.getToken();
    }

    @Override
    public void resetPasswordRedeemToken(UUID token, String pin, String password) throws ApplicationException {
        List<ErrorCode> errors = passwordValidator.validate(password);

        if(!errors.isEmpty()) {
            throw createApplicationException(errors.get(0));
        }

        userRepository.resetPassword(token, pin, password);
    }

    @Override
    public void resetPasswordRedeemToken(String remoteAddress, String captchaResponse, UUID token, String pin, String password) throws ApplicationException {
        if (!googleReCaptchaService.validate(remoteAddress, captchaResponse)) {
            throw createApplicationException(CaptchaErrorCode.CAPTCHA_VERIFICATION_ERROR);
        }

        this.resetPasswordRedeemToken(token, pin, password);
    }

}

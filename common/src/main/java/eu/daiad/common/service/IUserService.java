package eu.daiad.common.service;

import java.util.UUID;

import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.model.user.UserRegistrationRequest;

public interface IUserService {

	/**
	 * Creates a new user.
	 *
	 * @param request the user to create.
	 * @return the new user key.
	 * @throws ApplicationException if the user already exists.
	 */
	UUID createUser(UserRegistrationRequest request) throws ApplicationException;

	/**
	 * Changes a user password.
	 *
	 * @param username the user name .
	 * @param password the new password.
	 * @throws ApplicationException if the user does not exist.
	 */
	void changePassword(String username, String password) throws ApplicationException;

    /**
     * Changes a user password.
     *
     * @param remoteAddress the client remote IP address.
     * @param captchaResponse the Google ReCAPTCAH response.
     * @param username the user name .
     * @param password the new password.
     * @throws ApplicationException if the user does not exist.
     */
    void changePassword(String remoteAddress, String captchaResponse, String username, String password) throws ApplicationException;

	/**
     * Grants a role to a user
     *
     * @param username the user name.
     * @param role the role to grant or revoke.
     * @throws ApplicationException if the user or the role does not exist.
     */
    void grantRole(String username, EnumRole role) throws ApplicationException;

	/**
	 * Revokes a role from a user.
	 *
	 * @param username the user name.
	 * @param role the role to grant or revoke.
	 * @throws ApplicationException if the user or the role does not exist.
	 */
	void revokeRole(String username, EnumRole role) throws ApplicationException;

	/**
	 * Requests a token for reseting a user's password.
	 *
	 * @param username the user for whom the password is reseted.
	 * @param application the application requesting the password reset token.
	 * @return a token for setting the password. The token is valid for a specific time interval.
	 *
	 * @throws ApplicationException if no user is found.
	 */
	UUID resetPasswordCreateToken(String username, EnumApplication application) throws ApplicationException;

	/**
     * Resets a user's password given a valid reset password token.
     *
     * @param token the token created by @{link IUserService#resetPasswordCreateToken} call.
     * @param pin the PIN associated to the specified password reset token.
     * @param password the new password.
     *
     * @throws ApplicationException if token does not exists or it is expired.
     */
    void resetPasswordRedeemToken(UUID token, String pin, String password) throws ApplicationException;

    /**
     * Resets a user's password given a valid reset password token.
     *
     * @param remoteAddress the client remote IP address.
     * @param captchaResponse the Google ReCAPTCAH response.
     * @param token the token created by @{link IUserService#resetPasswordCreateToken} call.
     * @param pin the PIN associated to the specified password reset token.
     * @param password the new password.
     *
     * @throws ApplicationException if token does not exists or it is expired.
     */
    void resetPasswordRedeemToken(String remoteAddress, String captchaResponse, UUID token, String pin, String password) throws ApplicationException;

}

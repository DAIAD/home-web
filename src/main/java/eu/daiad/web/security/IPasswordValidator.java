package eu.daiad.web.security;

import java.util.List;

import eu.daiad.web.model.error.ErrorCode;

public interface IPasswordValidator {

    /**
     * Validates a password.
     *
     * @param password the password to validate.
     * @return a list of {@link ErrorCode} objects.
     */
    List<ErrorCode> validate(String password);

}

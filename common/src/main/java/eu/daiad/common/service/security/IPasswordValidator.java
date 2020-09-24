package eu.daiad.common.service.security;

import java.util.List;

import eu.daiad.common.model.error.ErrorCode;

public interface IPasswordValidator {

    /**
     * Validates a password.
     *
     * @param password the password to validate.
     * @return a list of {@link ErrorCode} objects.
     */
    List<ErrorCode> validate(String password);

}

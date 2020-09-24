package eu.daiad.common.service.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.PasswordErrorCode;

@Service
public class SimplePasswordValidator implements IPasswordValidator {

    private static int MINIMUM_LENGTH = 8;

    /**
     * Validates a password.
     *
     * @param password the password to validate.
     * @return a list of {@link ErrorCode} objects.
     */
    @Override
    public List<ErrorCode> validate(String password) {
        List<ErrorCode> errors = new ArrayList<ErrorCode>();

        if ((StringUtils.isBlank(password)) || (password.length() < MINIMUM_LENGTH)) {
            errors.add(PasswordErrorCode.WEAK_PASSWORD);
        }

        return errors;
    }

}

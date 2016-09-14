package eu.daiad.web.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.PasswordErrorCode;

@Service
public class SimplePasswordValidator implements IPasswordValidator {

    private static int MINIMUM_LENGTH = 8;

    @Override
    public List<ErrorCode> validate(String password) {
        List<ErrorCode> errors = new ArrayList<ErrorCode>();

        if ((StringUtils.isBlank(password)) || (password.length() < MINIMUM_LENGTH)) {
            errors.add(PasswordErrorCode.INVALID_LENGTH);
        }

        return errors;
    }

}

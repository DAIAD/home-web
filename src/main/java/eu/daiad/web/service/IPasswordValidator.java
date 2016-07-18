package eu.daiad.web.service;

import java.util.List;

import eu.daiad.web.model.error.ErrorCode;

public interface IPasswordValidator {

    abstract List<ErrorCode> validate(String password);

}

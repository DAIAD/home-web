package eu.daiad.common.service;

import eu.daiad.common.model.error.ApplicationException;

public interface IGoogleReCaptchaService {

    /**
     * Verify a user's response to a reCAPTCHA challenge.
     *
     * @param remoteAddress the user's IP address.
     * @param response the user response token provided by reCAPTCHA, verifying the user on your site.
     * @return true if the response is valid.
     * @throws ApplicationException if CAPTCHA verification failed.
     */
    boolean validate(String remoteAddress, String response) throws ApplicationException;

}

package eu.daiad.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.CaptchaErrorCode;

@Service
public class GoogleReCaptchaService implements IGoogleReCaptchaService {

    /**
     * Google ReCAPTCHA secret key.
     */
    @Value("${daiad.captcha.google.secret-key}")
    private String secretKey;

    @Value("${daiad.captcha.google.url}")
    private String url;

    /**
     * Verify a user's response to a reCAPTCHA challenge.
     *
     * @param remoteAddressthe user's IP address.
     * @param response the user response token provided by reCAPTCHA, verifying the user on your site.
     * @return true if the response is valid.
     * @throws ApplicationException if CAPTCHA verification failed.
     */
    @Override
    public boolean validate(String remoteAddress, String response) throws ApplicationException {
        RestTemplate restTemplate = new RestTemplate();

        RecaptchaResponse recaptchaResponse;
        try {
            recaptchaResponse = restTemplate.postForEntity(url, createRequest(secretKey, remoteAddress, response), RecaptchaResponse.class)
                                            .getBody();
        } catch (RestClientException e) {
            throw ApplicationException.wrap(e, CaptchaErrorCode.CAPTCHA_SERVICE_ERROR, "Recaptcha API error.");
        }

        return recaptchaResponse.success;
    }

    private MultiValueMap<String, String> createRequest(String secret, String remoteIp, String response) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();

        request.add("secret", secret);
        request.add("remoteip", remoteIp);
        request.add("response", response);

        return request;
    }

    private static class RecaptchaResponse {

        @JsonProperty()
        private boolean success;

        @JsonProperty("error-codes")
        private List<String> errors;

    }
}

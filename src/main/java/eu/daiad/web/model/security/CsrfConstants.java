package eu.daiad.web.model.security;

import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

public class CsrfConstants {

	public static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class
			.getName().concat(".CSRF_TOKEN");

	public static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

	public static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";

	public static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";

	public static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

}

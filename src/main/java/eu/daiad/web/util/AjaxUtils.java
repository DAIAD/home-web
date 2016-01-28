package eu.daiad.web.util;

import org.springframework.web.context.request.WebRequest;

public final class AjaxUtils {

	private AjaxUtils() {

	}

	public static boolean isAjaxRequest(WebRequest req) {
		String requestedWith = req.getHeader("X-Requested-With");

		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith)
				: false;
	}

}
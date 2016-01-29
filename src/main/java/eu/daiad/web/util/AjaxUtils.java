package eu.daiad.web.util;

import javax.servlet.http.HttpServletRequest;

public final class AjaxUtils {

	private AjaxUtils() {

	}

	public static boolean isAjaxRequest(HttpServletRequest request) {
		if (request.getMethod().equals("POST")
				&& "application/json".equals(request.getHeader("Content-Type"))) {
			return true;
		}

		String requestedWith = request.getHeader("X-Requested-With");
		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith)
				: false;
	}
}
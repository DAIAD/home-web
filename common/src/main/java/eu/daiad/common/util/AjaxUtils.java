package eu.daiad.common.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper methods for AJAX
 *
 */
public final class AjaxUtils {

	private AjaxUtils() {

	}

	/**
	 * Checks if the given request is an AJAX one. The detection is based on the
	 * request Content-Type and XMLHttpRequest headers.
	 * 
	 * @param request the request.
	 * @return true if this is an AJAX request; Otherwise false.
	 */
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
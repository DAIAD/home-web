package eu.daiad.web.model.export;

import eu.daiad.web.model.RestResponse;

/**
 * File download response.
 */
public class DownloadFileResponse extends RestResponse {

    /**
     * Unique token for identifying the file to download.
     */
    private String token;

    /**
     * Creates a new response with a valid token.
     *
     * @param token the token for downloading a file.
     */
    public DownloadFileResponse(String token) {
        super();

        this.token = token;
    }

    /**
     * Creates a new response initialized with the given error code and description.
     *
     * @param code the error code.
     * @param description the error description.
     */
    public DownloadFileResponse(String code, String description) {
        super(code, description);
    }

    public String getToken() {
        return token;
    }

}

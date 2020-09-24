package eu.daiad.common.model.export;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

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
    public DownloadFileResponse(ErrorCode code, String description) {
        super(code, description);
    }

    public String getToken() {
        return token;
    }

}

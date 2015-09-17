package eu.daiad.web.controller;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import eu.daiad.web.model.*;
import eu.daiad.web.data.*;

@Controller
public class DataWebApiController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_TYPE_NOT_SUPPORTED = 2;

	private static final int ERROR_UNKNOWN = 100;

	private static final Log logger = LogFactory
			.getLog(DataWebApiController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private ExportService exportService;

	@RequestMapping(value = "/data/export", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public DownloadResponse export(@RequestBody ExportData data,
			BindingResult results) {
		String errorMessage = null;

		try {
			if (results.hasErrors()) {
				return new DownloadResponse(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			}

			switch (data.getType()) {
			case SESSION:
				String token = this.exportService.export(data);

				// Create response
				return new DownloadResponse(token.toString());
			default:
				break;
			}

			return new DownloadResponse(ERROR_TYPE_NOT_SUPPORTED,
					String.format("Export type [%s] is not supported.", data
							.getType().toString()));
		} catch (ExportException eEx) {
			logger.error("Failed to export data.", eEx);
			errorMessage = eEx.getMessage();
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}
		return new DownloadResponse(ERROR_UNKNOWN,
				(errorMessage == null ? "Unhandled exception has occured."
						: errorMessage));
	}

	@RequestMapping(value = "/data/download/{token}", method = RequestMethod.GET)
	@Secured({ "ROLE_ADMIN" })
	public ResponseEntity<InputStreamResource> download(
			@PathVariable("token") String token) {
		try {
			File path = new File(temporaryPath);

			File file = new File(path, token + ".zip");

			if (file.exists()) {
				FileSystemResource fileResource = new FileSystemResource(file);

				HttpHeaders headers = new HttpHeaders();
				headers.add("Cache-Control",
						"no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");

				return ResponseEntity
						.ok()
						.headers(headers)
						.contentLength(fileResource.contentLength())
						.contentType(
								MediaType.parseMediaType("application/zip"))
						.body(new InputStreamResource(fileResource
								.getInputStream()));
			}
		} catch (Exception ex) {
			logger.error(String.format("File [%s] was not found.", token), ex);
		}

		throw new ResourceNotFoundException();
	}
}

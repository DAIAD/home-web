package eu.daiad.web.controller.action;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.ResourceNotFoundException;
import eu.daiad.web.model.error.ActionErrorCode;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.export.DownloadFileResponse;
import eu.daiad.web.model.export.ExportDataRequest;
import eu.daiad.web.service.IExportService;

@Controller
public class DataController extends BaseController {

	private static final Log logger = LogFactory.getLog(DataController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IExportService exportService;

	@RequestMapping(value = "/action/data/export", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public DownloadFileResponse export(@RequestBody ExportDataRequest data) {
		DownloadFileResponse response = new DownloadFileResponse();

		try {
			switch (data.getType()) {
			case SESSION:
				String token = this.exportService.export(data);

				response.setToken(token);
			default:
				throw new ApplicationException(ActionErrorCode.EXPORT_TYPE_NOT_SUPPORTED).set("type", data.getType()
								.toString());
			}
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/data/download/{token}", method = RequestMethod.GET)
	@Secured({ "ROLE_ADMIN" })
	public ResponseEntity<InputStreamResource> download(@PathVariable("token") String token) {
		try {
			File path = new File(temporaryPath);

			File file = new File(path, token + ".zip");

			if (file.exists()) {
				FileSystemResource fileResource = new FileSystemResource(file);

				HttpHeaders headers = new HttpHeaders();
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");

				return ResponseEntity.ok().headers(headers).contentLength(fileResource.contentLength())
								.contentType(MediaType.parseMediaType("application/zip"))
								.body(new InputStreamResource(fileResource.getInputStream()));
			}
		} catch (Exception ex) {
			logger.error(String.format("File [%s] was not found.", token), ex);
		}

		throw new ResourceNotFoundException();
	}
}

package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.data.IAmphiroMeasurementRepository;
import eu.daiad.web.data.IWaterMeterMeasurementRepository;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionQueryResult;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;

@RestController("RestSearchController")
public class SearchController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private IAmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@RequestMapping(value = "/api/v1/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getWaterMeterStatus(@RequestBody WaterMeterStatusQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			query.setUserKey(user.getKey());

			WaterMeterStatusQueryResult data = waterMeterMeasurementRepository.getStatus(query);

			return data;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchWaterMeterMeasurements(@RequestBody WaterMeterMeasurementQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			WaterMeterMeasurementQueryResult data = waterMeterMeasurementRepository.searchMeasurements(query);

			return data;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroMeasurements(@RequestBody AmphiroMeasurementQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			AmphiroMeasurementQueryResult data = amphiroMeasurementRepository.searchMeasurements(query);

			return data;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroSessions(@RequestBody AmphiroSessionCollectionQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			AmphiroSessionCollectionQueryResult data = amphiroMeasurementRepository.searchSessions(query);

			return data;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getAmphiroSession(@RequestBody AmphiroSessionQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			AmphiroSessionQueryResult data = amphiroMeasurementRepository.getSession(query);

			return data;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}

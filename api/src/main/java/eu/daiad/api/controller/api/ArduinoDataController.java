package eu.daiad.api.controller.api;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseController;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.arduino.ArduinoIntervalQuery;
import eu.daiad.common.model.arduino.ArduinoMeasurement;
import eu.daiad.common.repository.application.IArduinoDataRepository;

/**
 * Provides actions for storing and querying smart water meter readings
 * collected by an Arduino controller.
 */
@RestController
public class ArduinoDataController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ArduinoDataController.class);

    /**
     * Repository for accessing Arduino data.
     */
    @Autowired
    private IArduinoDataRepository arduinoDataRepository;

    /**
     * Stores smart water meter data.
     *
     * @param data the data to store.
     * @return an empty string if save operation was successful; Otherwise an error message is returned.
     */
    @PostMapping(value = "/api/v1/arduino/store", consumes = "text/plain", produces = "text/plain")
    public ResponseEntity<String> storeData(@RequestBody String data) {

        String message = "";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        try {
            if (!StringUtils.isBlank(data)) {
                String[] items = StringUtils.split(data, ",");

                if ((items.length < 1) || (((items.length - 1) % 2) != 0)) {
                    return new ResponseEntity<String>("Invalid number of arguments", headers, HttpStatus.BAD_REQUEST);
                }

                String deviceKey = items[0];
                ArrayList<ArduinoMeasurement> measurements = new ArrayList<ArduinoMeasurement>();

                for (int index = 1, count = items.length; index < count; index += 2) {
                    ArduinoMeasurement measurement = new ArduinoMeasurement();
                    measurement.setTimestamp(Long.parseLong(items[index]));
                    measurement.setVolume(Long.parseLong(items[index + 1]));

                    measurements.add(measurement);
                }

                this.arduinoDataRepository.storeData(deviceKey, measurements);
            }
            return new ResponseEntity<String>(message, headers, HttpStatus.OK);
        } catch (Exception ex) {
            message = ex.getMessage();

            logger.error("Failed to insert data from arduino device", ex);
        }

        return new ResponseEntity<String>(message, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Load smart water meter readings based on a query.
     *
     * @param query the query.
     * @return the smart water meter data.
     */
    @PostMapping(value = "/api/v1/arduino/query", consumes = "application/json", produces = "application/json")
    public RestResponse loadData(@RequestBody ArduinoIntervalQuery query) {
        RestResponse response = new RestResponse();

        try {
            return this.arduinoDataRepository.searchData(query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

}

package eu.daiad.web.data;

import java.util.ArrayList;

import eu.daiad.web.model.arduino.ArduinoIntervalQuery;
import eu.daiad.web.model.arduino.ArduinoIntervalQueryResult;
import eu.daiad.web.model.arduino.ArduinoMeasurement;
import eu.daiad.web.model.error.ApplicationException;

public interface IArduinoDataRepository {

	public abstract void storeData(String deviceKey, ArrayList<ArduinoMeasurement> data) throws ApplicationException;

	public abstract ArduinoIntervalQueryResult searchData(ArduinoIntervalQuery query) throws ApplicationException;

}
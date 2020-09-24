package eu.daiad.common.repository.application;

import java.util.ArrayList;

import eu.daiad.common.model.arduino.ArduinoIntervalQuery;
import eu.daiad.common.model.arduino.ArduinoIntervalQueryResult;
import eu.daiad.common.model.arduino.ArduinoMeasurement;
import eu.daiad.common.model.error.ApplicationException;

public interface IArduinoDataRepository {

	public abstract void storeData(String deviceKey, ArrayList<ArduinoMeasurement> data) throws ApplicationException;

	public abstract ArduinoIntervalQueryResult searchData(ArduinoIntervalQuery query) throws ApplicationException;

}
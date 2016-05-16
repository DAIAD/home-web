package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IAmphiroTimeOrderedRepository {

	public void storeData(UUID userKey, AmphiroMeasurementCollection data) throws ApplicationException;

	public abstract AmphiroMeasurementTimeIntervalQueryResult searchMeasurements(DateTimeZone timezone,
					AmphiroMeasurementTimeIntervalQuery query);

	public abstract AmphiroSessionCollectionTimeIntervalQueryResult searchSessions(String[] name,
					DateTimeZone timezone, AmphiroSessionCollectionTimeIntervalQuery query);

	public abstract AmphiroSessionTimeIntervalQueryResult getSession(AmphiroSessionTimeIntervalQuery query);

	public abstract ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}
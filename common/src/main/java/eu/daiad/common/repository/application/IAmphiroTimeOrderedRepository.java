package eu.daiad.common.repository.application;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;

import eu.daiad.common.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.common.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.common.model.device.AmphiroDevice;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.query.ExpandedDataQuery;
import eu.daiad.common.model.query.GroupDataSeries;
import eu.daiad.common.model.security.AuthenticatedUser;

public interface IAmphiroTimeOrderedRepository {

    public void storeData(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data)
                    throws ApplicationException;

    public abstract AmphiroMeasurementTimeIntervalQueryResult searchMeasurements(DateTimeZone timezone,
                    AmphiroMeasurementTimeIntervalQuery query);

    public abstract AmphiroSessionCollectionTimeIntervalQueryResult searchSessions(String[] name,
                    DateTimeZone timezone, AmphiroSessionCollectionTimeIntervalQuery query);

    public abstract AmphiroSessionTimeIntervalQueryResult getSession(AmphiroSessionTimeIntervalQuery query);

    public abstract ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}
package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionUpdateCollection;
import eu.daiad.web.model.amphiro.IgnoreShowerRequest;
import eu.daiad.web.model.amphiro.MemberAssignmentRequest;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.security.AuthenticatedUser;

/**
 * Provides methods for managing amphiro b1 data.
 */
public interface IAmphiroIndexOrderedRepository {

    /**
     * Stores session and measurement data for an amphiro b1 device.
     *
     * @param user the owner of the device.
     * @param device  the device.
     * @param data a collection of amphiro b1 sessions and measurement time series.
     * @return any sessions that have been updated.
     * @throws ApplicationException if saving data has failed.
     */
    AmphiroSessionUpdateCollection store(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data) throws ApplicationException;

    /**
     * Searches for amphiro b1 session measurement time series.
     *
     * @param timezone the reference time zone.
     * @param query a query for filtering the measurements.
     * @return a collection of measurements.
     */
    AmphiroMeasurementIndexIntervalQueryResult getMeasurements(DateTimeZone timezone, AmphiroMeasurementIndexIntervalQuery query);

    /**
     * Searches for sessions for one or more amphiro b1 devices.
     *
     * @param name the names of the devices.
     * @param timezone the reference time zone.
     * @param query a query for filtering sessions.
     * @return a collection of sessions per device.
     */
    AmphiroSessionCollectionIndexIntervalQueryResult getSessions(String[] name, DateTimeZone timezone, AmphiroSessionCollectionIndexIntervalQuery query);

    /**
     * Searches for a single session. Optionally, loads the session measurements.
     *
     * @param query a query for selecting the session.
     * @return the session.
     */
    AmphiroSessionIndexIntervalQueryResult getSession(AmphiroSessionIndexIntervalQuery query);

    /**
     * Computes aggregates of session values e.g. volume and energy over a time
     * interval for server users.
     *
     * @param query the query to execute.
     * @return a list of data series.
     * @throws ApplicationException if query execution fails.
     */
    ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

    /**
     * Assigns one ore more sessions to a specific household members.
     *
     * @param user the device owner.
     * @param assignments the member to session assignments.
     * @throws Exception if update fails.
     */
    void assignMember(AuthenticatedUser user, List<MemberAssignmentRequest.Assignment> assignments) throws Exception;

    /**
     * Marks a session as ignored i.e. a session that does not correspond to an actual session.
     *
     * @param user the owner of the device.
     * @param sessions a list of sessions to ignore.
     * @throws Exception if update fails.
     */
    void ignore(AuthenticatedUser user, List<IgnoreShowerRequest.Session> sessions) throws Exception;

    /**
     * Updates the date time of a historical shower and converts it to a real-time one.
     *
     * @param user the owner of the device.
     * @param device the amphiro b1 device.
     * @param sessionId the per device unique shower Id.
     * @param timestamp the real-time timestamp.
     */
    void toRealTime(AuthenticatedUser user, AmphiroDevice device, long sessionId, long timestamp);

}

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

public interface IAmphiroIndexOrderedRepository {

    AmphiroSessionUpdateCollection storeData(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data) throws ApplicationException;

    AmphiroMeasurementIndexIntervalQueryResult searchMeasurements(DateTimeZone timezone, AmphiroMeasurementIndexIntervalQuery query);

    AmphiroSessionCollectionIndexIntervalQueryResult searchSessions(String[] name, DateTimeZone timezone, AmphiroSessionCollectionIndexIntervalQuery query);

    AmphiroSessionIndexIntervalQueryResult getSession(AmphiroSessionIndexIntervalQuery query);

    ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

    void assignMemberToSession(AuthenticatedUser user, List<MemberAssignmentRequest.Assignment> assignments) throws Exception;

    void ignoreSession(AuthenticatedUser user, List<IgnoreShowerRequest.Session> sessions) throws Exception;

}

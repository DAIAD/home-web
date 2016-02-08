package eu.daiad.web.data;

import java.util.List;

import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionQueryResult;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.export.ExportDataRequest;
import eu.daiad.web.model.export.ExtendedSessionData;
import eu.daiad.web.model.security.AuthenticatedUser;

public interface IAmphiroMeasurementRepository {

	public abstract List<ExtendedSessionData> exportSessions(ExportDataRequest data) throws ApplicationException;

	public abstract void storeData(AuthenticatedUser user, AmphiroDevice device, AmphiroMeasurementCollection data)
					throws ApplicationException;

	public abstract AmphiroMeasurementQueryResult searchMeasurements(AmphiroMeasurementQuery query);

	public abstract AmphiroSessionCollectionQueryResult searchSessions(AmphiroSessionCollectionQuery query);

	public abstract AmphiroSessionQueryResult getSession(AmphiroSessionQuery query);

}
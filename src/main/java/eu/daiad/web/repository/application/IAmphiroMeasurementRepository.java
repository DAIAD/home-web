package eu.daiad.web.repository.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionQueryResult;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IAmphiroMeasurementRepository {

	public void storeData(UUID userKey, AmphiroMeasurementCollection data) throws ApplicationException;

	public abstract AmphiroMeasurementQueryResult searchMeasurements(AmphiroMeasurementQuery query);

	public abstract AmphiroSessionCollectionQueryResult searchSessions(String[] name,
					AmphiroSessionCollectionQuery query);

	public abstract AmphiroSessionQueryResult getSession(AmphiroSessionQuery query);

	public abstract ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

	public abstract void open() throws IOException;

	public abstract void close();

	public abstract boolean isOpen();
}
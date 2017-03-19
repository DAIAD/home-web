package eu.daiad.web.repository.application;

import java.util.List;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.MeterDataStoreStats;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurement;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IMeterDataRepository {

    /**
     * Stores smart water meter data.
     *
     * @param serial the smart water meter unique serial number.
     * @param data a collection of {@link WaterMeterMeasurement}.
     * @return statistics for the insert operations.
     */
    MeterDataStoreStats store(String serial, WaterMeterMeasurementCollection data);

    /**
     * Returns the current status for a set of smart water meters.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @return a collection of {@link WaterMeterStatus}.
     */
    WaterMeterStatusQueryResult getStatus(String serials[]);

    /**
     * Returns the most recent status for a set of smart water meters before the specified timestamp.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param maxDateTime time interval upper limit.
     * @return a collection of {@link WaterMeterStatus}.
     */
    WaterMeterStatusQueryResult getStatusBefore(String serials[], long maxDateTime);

    /**
     * Returns the least recent status for a set of smart water meters after the specified timestamp.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param minDateTime time interval upper limit.
     * @return a collection of {@link WaterMeterStatus}.
     */
    WaterMeterStatusQueryResult getStatusAfter(String serials[], long minDateTime);

    /**
     * Searches for smart water meter readings.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param timezone the time zone of the results.
     * @param query the query for filtering the results.
     * @return a collection of {@link WaterMeterDataSeries}.
     */
    WaterMeterMeasurementQueryResult searchMeasurements(String serials[], DateTimeZone timezone, WaterMeterMeasurementQuery query);

    /**
     * Executes a query for smart water meter data.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    List<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}

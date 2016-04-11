package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

public class DataQueryBuilder {

	private DataQuery query;

	ArrayList<EnumMetric> metrics = new ArrayList<EnumMetric>();

	public DataQueryBuilder() {
		this.query = new DataQuery();
		this.query.setTime(new TimeFilter());
	}

	public DataQueryBuilder reset() {
		this.query = new DataQuery();
		this.query.setTime(new TimeFilter());
		this.metrics = new ArrayList<EnumMetric>();
		return this;
	}

	private void initializeSpatialFilter() {
		if (this.query.getSpatial() == null) {
			this.query.setSpatial(new SpatialFilter());
		}
	}

	public DataQueryBuilder removeSpatialFilter() {
		this.query.setSpatial(null);
		return this;
	}

	public DataQueryBuilder contains(Geometry geometry) {
		initializeSpatialFilter();
		this.query.getSpatial().setType(EnumSpatialFilterType.CONTAINS);
		this.query.getSpatial().setGeometry(geometry);
		return this;
	}

	public DataQueryBuilder intersects(Geometry geometry) {
		initializeSpatialFilter();
		this.query.getSpatial().setType(EnumSpatialFilterType.INTERSECT);
		this.query.getSpatial().setGeometry(geometry);
		return this;
	}

	public DataQueryBuilder distance(Geometry geometry, double distance) {
		initializeSpatialFilter();
		this.query.getSpatial().setType(EnumSpatialFilterType.DISTANCE);
		this.query.getSpatial().setGeometry(geometry);
		this.query.getSpatial().setDistance(distance);
		return this;
	}

	private void initializePopulationFilter() {
		if (this.query.getPopulation() == null) {
			this.query.setPopulation(new PopulationFilter());
		}
	}

	public DataQueryBuilder removePopulationFilter() {
		this.query.setPopulation(new PopulationFilter());
		return this;
	}

	public DataQueryBuilder group(String label, UUID user) {
		initializePopulationFilter();
		this.query.getPopulation().add(label, user);
		return this;
	}

	public DataQueryBuilder group(String label, UUID users[]) {
		initializePopulationFilter();
		this.query.getPopulation().add(label, users);
		return this;
	}

	public DataQueryBuilder absolute(long start, long end) {
		this.query.setTime(new TimeFilter(start, end));

		return this;
	}

	public DataQueryBuilder absolute(long start, long end, EnumTimeUnit graunlarity) {
		this.query.setTime(new TimeFilter(start, end, graunlarity));

		return this;
	}

	public DataQueryBuilder absolute(DateTime start, DateTime end) {
		this.query.setTime(new TimeFilter(start, end));

		return this;
	}

	public DataQueryBuilder absolute(DateTime start, DateTime end, EnumTimeUnit graunlarity) {
		this.query.setTime(new TimeFilter(start, end, graunlarity));

		return this;
	}

	public DataQueryBuilder sliding(long start, int duration) {
		this.query.setTime(new TimeFilter(start, duration));

		return this;
	}

	public DataQueryBuilder sliding(long start, int duration, EnumTimeUnit durationTimeUnit) {
		this.query.setTime(new TimeFilter(start, duration, durationTimeUnit));

		return this;
	}

	public DataQueryBuilder sliding(long start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeUnit granularity) {
		this.query.setTime(new TimeFilter(start, duration, durationTimeUnit, granularity));
		return this;
	}

	public DataQueryBuilder sliding(DateTime start, int duration) {
		this.query.setTime(new TimeFilter(start, duration));

		return this;
	}

	public DataQueryBuilder sliding(int duration) {
		this.query.setTime(new TimeFilter(new DateTime().getMillis(), duration));

		return this;
	}

	public DataQueryBuilder sliding(DateTime start, int duration, EnumTimeUnit durationTimeUnit) {
		this.query.setTime(new TimeFilter(start, duration, durationTimeUnit));

		return this;
	}

	public DataQueryBuilder sliding(int duration, EnumTimeUnit durationTimeUnit) {
		this.query.setTime(new TimeFilter(new DateTime().getMillis(), duration, durationTimeUnit));

		return this;
	}

	public DataQueryBuilder sliding(DateTime start, int duration, EnumTimeUnit durationTimeUnit,
					EnumTimeUnit granularity) {
		this.query.setTime(new TimeFilter(start, duration, durationTimeUnit, granularity));

		return this;
	}

	public DataQueryBuilder sliding(int duration, EnumTimeUnit durationTimeUnit, EnumTimeUnit granularity) {
		this.query.setTime(new TimeFilter(new DateTime().getMillis(), duration, durationTimeUnit, granularity));

		return this;
	}

	public DataQueryBuilder all() {
		this.query.setSource(EnumMeasurementDataSource.BOTH);
		return this;
	}

	public DataQueryBuilder amphiro() {
		this.query.setSource(EnumMeasurementDataSource.AMPHIRO);
		return this;
	}

	public DataQueryBuilder meter() {
		this.query.setSource(EnumMeasurementDataSource.METER);
		return this;
	}

	public DataQueryBuilder sum() {
		if (!this.metrics.contains(EnumMetric.SUM)) {
			this.metrics.add(EnumMetric.SUM);
		}
		return this;
	}

	public DataQueryBuilder min() {
		if (!this.metrics.contains(EnumMetric.MIN)) {
			this.metrics.add(EnumMetric.MIN);
		}
		return this;
	}

	public DataQueryBuilder max() {
		if (!this.metrics.contains(EnumMetric.MAX)) {
			this.metrics.add(EnumMetric.MAX);
		}
		return this;
	}

	public DataQueryBuilder count() {
		if (!this.metrics.contains(EnumMetric.COUNT)) {
			this.metrics.add(EnumMetric.COUNT);
		}
		return this;
	}

	public DataQueryBuilder average() {
		if (!this.metrics.contains(EnumMetric.AVERAGE)) {
			this.metrics.add(EnumMetric.AVERAGE);
		}
		return this;
	}

	public DataQuery build() {
		EnumMetric[] metricArray = new EnumMetric[this.metrics.size()];

		this.query.setMetrics(this.metrics.toArray(metricArray));

		return this.query;
	}
}

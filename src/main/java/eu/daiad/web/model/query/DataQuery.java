package eu.daiad.web.model.query;

import java.util.Arrays;

public class DataQuery {

	TimeFilter time;

	PopulationFilter population;

	SpatialFilter spatial;

	EnumMeasurementDataSource source = EnumMeasurementDataSource.BOTH;

	EnumMetric metrics[] = {};

	public TimeFilter getTime() {
		return time;
	}

	public void setTime(TimeFilter time) {
		this.time = time;
	}

	public PopulationFilter getPopulation() {
		return population;
	}

	public void setPopulation(PopulationFilter population) {
		this.population = population;
	}

	public SpatialFilter getSpatial() {
		return spatial;
	}

	public void setSpatial(SpatialFilter spatial) {
		this.spatial = spatial;
	}

	public EnumMeasurementDataSource getSource() {
		return source;
	}

	public void setSource(EnumMeasurementDataSource source) {
		this.source = source;
	}

	public EnumMetric[] getMetrics() {
		return metrics;
	}

	public void setMetrics(EnumMetric[] metrics) {
		this.metrics = metrics;
	}

	@Override
	public String toString() {
		return "DataQuery [time=" + time + ", population=" + population + ", spatial=" + spatial + ", source=" + source
						+ ", metrics=" + Arrays.toString(metrics) + "]";
	}
}

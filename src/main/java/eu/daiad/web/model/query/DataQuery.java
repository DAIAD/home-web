package eu.daiad.web.model.query;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class DataQuery {

	TimeFilter time;

	private ArrayList<PopulationFilter> population = new ArrayList<PopulationFilter>();

	SpatialFilter spatial;

	@JsonDeserialize(using = EnumMeasurementDataSource.Deserializer.class)
	EnumMeasurementDataSource source = EnumMeasurementDataSource.BOTH;

	EnumMetric[] metrics = {};

	public TimeFilter getTime() {
		return time;
	}

	public void setTime(TimeFilter time) {
		this.time = time;
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

	public ArrayList<PopulationFilter> getPopulation() {
		return population;
	}
}

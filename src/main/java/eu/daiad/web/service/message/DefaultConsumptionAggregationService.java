package eu.daiad.web.service.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.query.AmphiroDataPoint;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumTimeAggregation;
import eu.daiad.web.model.query.EnumTimeUnit;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;

//TODO - define some sanity values for checking the produced results

@Service
public class DefaultConsumptionAggregationService implements IConsumptionAggregationService {

	@Autowired
	IUserRepository userRepository;

	@Autowired
	IDataService dataService;

	@Override
	public ConsumptionAggregateContainer execute(MessageCalculationConfiguration config) {
		ConsumptionAggregateContainer aggregates = new ConsumptionAggregateContainer();

		computeAverageMonthlyConsumptionAmphiro(config, aggregates);
		computeAverageWeeklyConsumptionAmphiro(config, aggregates);
		computeAverageMonthlyConsumptionSWM(config, aggregates);
		computeAverageWeeklyConsumptionSWM(config, aggregates);
		computeTop10MonthlyConsumptionThresholdSWM(config, aggregates);
		computeTop10WeeklyConsumptionThresholdSWM(config, aggregates);
		computeTop25WeeklyConsumptionThresholdSWM(config, aggregates);
		computeTop10MonthlyConsumptionThresholdAmphiro(config, aggregates);
		computeAverageTemperatureAmphiro(config, aggregates);
		computeAverageDurationAmphiro(config, aggregates);
		computeAverageFlowAmphiro(config, aggregates);
		computeAverageSessionConsumptionAmphiro(config, aggregates);

		return aggregates;
	}

	private void computeAverageMonthlyConsumptionAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageMonthlyConsumptionAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}

				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> points = series.getPoints();
					DataPoint dataPoint = points.get(0);
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
					Map<EnumMetric, Double> ma = amphiroDataPoint.getVolume();
					Double sumMonthlyConsumptionAmphiro = ma.get(EnumMetric.SUM);
					container.setAverageMonthlyConsumptionAmphiro(sumMonthlyConsumptionAmphiro / series.getPopulation());
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageMonthlyConsumptionAmphiro(null);
				}
			}
		}
	}

	private void computeAverageWeeklyConsumptionAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageWeeklyConsumptionAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}

				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> points = series.getPoints();
					DataPoint dataPoint = points.get(0);
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
					Map<EnumMetric, Double> ma = amphiroDataPoint.getVolume();
					Double averageWeeklyAmphiro = ma.get(EnumMetric.SUM);

					container.setAverageWeeklyConsumptionAmphiro(averageWeeklyAmphiro / series.getPopulation());
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageWeeklyConsumptionAmphiro(null);
				}
			}
		}
	}

	private void computeAverageWeeklyConsumptionSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageWeeklyConsumptionSWM() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).meter().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

			for (GroupDataSeries series : dataSeriesMeter) {
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> points = series.getPoints();
					DataPoint dataPoint = points.get(0);
					MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
					Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
					container.setAverageWeeklyConsumptionSWM(metricsMap.get(EnumMetric.SUM) / series.getPopulation());
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageWeeklyConsumptionSWM(null);
				}
			}
		}
	}

	private void computeAverageMonthlyConsumptionSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageMonthlyConsumptionSWM() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).meter().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

			for (GroupDataSeries series : dataSeriesMeter) {
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> points = series.getPoints();
					DataPoint dataPoint = points.get(0);
					MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
					Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
					container.setAverageMonthlyConsumptionSWM(ma.get(EnumMetric.SUM) / series.getPopulation());
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageMonthlyConsumptionSWM(null);
				}
			}
		}
	}

	private void computeTop10MonthlyConsumptionThresholdSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		// Compute average for all users.
		// Sort consumptions. The base threshold is the consumption of the last
		// user of the top10%.
		if (container.getTop10BaseMonthThresholdSWM() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).meter().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

			for (GroupDataSeries series : dataSeriesMeter) {
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> userPoints = series.getPoints();
					DataPoint dataPoint = userPoints.get(0);
					MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
					Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
					averageConsumptions.add(metricsMap.get(EnumMetric.SUM) / series.getPopulation());
				}
			}
			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top10BaseIndex = (int) (averageConsumptions.size() * 10) / 100;
				Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
				container.setTop10BaseMonthThresholdSWM(consumptionThresholdTop10);
				container.setLastDateComputed(DateTime.now());
			} else {
				container.setTop10BaseMonthThresholdSWM(null);
			}
		}
	}

	private void computeTop10WeeklyConsumptionThresholdSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getTop10BaseWeekThresholdSWM() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).meter().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

			for (GroupDataSeries series : dataSeriesMeter) {
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> userPoints = series.getPoints();
					DataPoint dataPoint = userPoints.get(0);
					MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
					Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
					averageConsumptions.add(metricsMap.get(EnumMetric.SUM) / series.getPopulation());
				}
			}
			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top10BaseIndex = (int) (averageConsumptions.size() * 10) / 100;
				Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
				container.setTop10BaseWeekThresholdSWM(consumptionThresholdTop10);
				container.setLastDateComputed(DateTime.now());
			} else {
				container.setTop10BaseWeekThresholdSWM(null);
			}
		}
	}

	private void computeTop10MonthlyConsumptionThresholdAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getTop10BaseThresholdAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> userPoints = series.getPoints();
					DataPoint dataPoint = userPoints.get(0);
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
					Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getVolume();
					Double averageMonthlyConsumptionAmphiro = metricsMap.get(EnumMetric.SUM) / series.getPopulation();
					averageConsumptions.add(averageMonthlyConsumptionAmphiro);
				}
			}
			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top10BaseIndex = (int) (averageConsumptions.size() * 10) / 100;
				Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
				container.setTop10BaseThresholdAmphiro(consumptionThresholdTop10);
				container.setLastDateComputed(DateTime.now());
			} else {
				container.setTop10BaseThresholdAmphiro(null);
			}
		}
	}

	private void computeTop25WeeklyConsumptionThresholdSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getTop10BaseWeekThresholdSWM() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).meter().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

			for (GroupDataSeries series : dataSeriesMeter) {
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> userPoints = series.getPoints();
					DataPoint dataPoint = userPoints.get(0);
					MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
					Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
					averageConsumptions.add(metricsMap.get(EnumMetric.SUM) / series.getPopulation());
				}
			}
			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top25BaseIndex = (int) (averageConsumptions.size() * 25) / 100;
				Double consumptionThresholdTop25 = averageConsumptions.get(top25BaseIndex);
				container.setTop25BaseWeekThresholdSWM(consumptionThresholdTop25);
				container.setLastDateComputed(DateTime.now());
			} else {
				container.setTop25BaseWeekThresholdSWM(null);
			}

		}
	}

	private void computeAverageTemperatureAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageTemperatureAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().average();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}

				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> point = series.getPoints();
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
					Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getTemperature();
					Double averageTemperatureAmphiro = metricsMap.get(EnumMetric.AVERAGE);

					container.setAverageTemperatureAmphiro(averageTemperatureAmphiro);
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageTemperatureAmphiro(null);
				}
			}
		}
	}

	private void computeAverageDurationAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageDurationAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().average();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}

				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> point = series.getPoints();
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
					Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getDuration();
					Double averageDurationAmphiro = metricsMap.get(EnumMetric.AVERAGE);

					container.setAverageDurationAmphiro(averageDurationAmphiro);
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageDurationAmphiro(null);
				}
			}
		}
	}

	private void computeAverageFlowAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageFlowAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().average();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}

				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> point = series.getPoints();
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
					Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getFlow();
					Double averageFlowAmphiro = metricsMap.get(EnumMetric.AVERAGE);

					container.setAverageFlowAmphiro(averageFlowAmphiro);
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageFlowAmphiro(null);
				}
			}
		}
	}

	private void computeAverageSessionConsumptionAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageSessionConsumptionAmphiro() == null
						|| container.getLastDateComputed().isBefore(
										DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

			List<UUID> uuidList = userRepository.getUserKeysForUtility(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).amphiro().average();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

			for (GroupDataSeries series : dataSeriesAmphiro) {
				if (series.getPopulation() == 0) {
					return;
				}

				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> point = series.getPoints();
					AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
					Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getVolume();
					Double averageSessionConsumptionAmphiro = metricsMap.get(EnumMetric.AVERAGE);

					container.setAverageTemperatureAmphiro(averageSessionConsumptionAmphiro);
					container.setLastDateComputed(DateTime.now());
				} else {
					container.setAverageTemperatureAmphiro(null);
				}
			}
		}
	}

}

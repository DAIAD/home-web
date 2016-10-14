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
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.aggregates.AverageDurationAmphiro;
import eu.daiad.web.service.message.aggregates.AverageFlowAmphiro;
import eu.daiad.web.service.message.aggregates.AverageMonthlyAmphiro;
import eu.daiad.web.service.message.aggregates.AverageMonthlySWM;
import eu.daiad.web.service.message.aggregates.AverageSessionAmphiro;
import eu.daiad.web.service.message.aggregates.AverageTemperatureAmphiro;
import eu.daiad.web.service.message.aggregates.AverageWeeklyAmphiro;
import eu.daiad.web.service.message.aggregates.AverageWeeklySWM;
import eu.daiad.web.service.message.aggregates.Top10BaseMonthAmphiro;
import eu.daiad.web.service.message.aggregates.Top10BaseMonthSWM;
import eu.daiad.web.service.message.aggregates.Top10BaseWeekSWM;
import eu.daiad.web.service.message.aggregates.Top25BaseWeekSWM;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//TODO - define some sanity values for checking the produced results

@Service
public class DefaultConsumptionAggregationService implements IConsumptionAggregationService {

	@Autowired
	IUserRepository userRepository;

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
	IDataService dataService;
    
    private static final Log logger = LogFactory.getLog(DefaultConsumptionAggregationService.class);

	@Override
	public ConsumptionAggregateContainer execute(MessageCalculationConfiguration config) {
        
		ConsumptionAggregateContainer aggregates = new ConsumptionAggregateContainer();
        aggregates.resetValues();
        aggregates.setUtilityId(config.getUtilityId());     

        computeAverageMonthlyConsumptionSWM(config, aggregates);
		computeAverageWeeklyConsumptionSWM(config, aggregates);       
        computeTop10MonthlyConsumptionThresholdSWM(config, aggregates);
        computeTop10WeeklyConsumptionThresholdSWM(config, aggregates);
        computeTop25WeeklyConsumptionThresholdSWM(config, aggregates);
        
		computeAverageMonthlyConsumptionAmphiro(config, aggregates);
		computeAverageWeeklyConsumptionAmphiro(config, aggregates);					
		computeTop10MonthlyConsumptionThresholdAmphiro(config, aggregates);
		computeAverageTemperatureAmphiro(config, aggregates);
		computeAverageDurationAmphiro(config, aggregates);
		computeAverageFlowAmphiro(config, aggregates);
		computeAverageSessionConsumptionAmphiro(config, aggregates);
        
        logger.info(aggregates.toString());
        
		return aggregates;
	}

	private void computeAverageMonthlyConsumptionSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
        
		if (container.getAverageMonthlySWM() == null 
                || container.getAverageMonthlySWM().getValue() == null
                || container.getAverageMonthlySWM().getLastComputed() == null
                || container.getAverageMonthlySWM().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {
            
            AverageMonthlySWM aggregate = new AverageMonthlySWM();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
			UUID[] userUUIDs = ((List<UUID>) uuidList).toArray(new UUID[uuidList.size()]);

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
			queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
							.users("utility", userUUIDs).meter().sum();

			DataQuery query = queryBuilder.build();
			DataQueryResponse queryResponse = dataService.execute(query);
			ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

			for (GroupDataSeries series : dataSeriesMeter) {
                logger.info("Population(SWM): " + series.getPopulation() + " (utility id " + config.getUtilityId() + ")");
                container.setPopulation(series.getPopulation());
				if (series.getPopulation() == 0) {
					return;
				}
				if (!series.getPoints().isEmpty()) {
					ArrayList<DataPoint> points = series.getPoints();
					DataPoint dataPoint = points.get(0);
					MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
					Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
                                       
                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(ma.get(EnumMetric.SUM) / series.getPopulation());
                    container.setAverageMonthlySWM(aggregate);
				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageMonthlySWM(aggregate);
				}
			}
		}
	}
    
	private void computeAverageWeeklyConsumptionSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
        
		if (container.getAverageWeeklySWM() == null
                || container.getAverageWeeklySWM().getValue() == null
                || container.getAverageWeeklySWM().getLastComputed() == null
				|| container.getAverageWeeklySWM().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageWeeklySWM aggregate = new AverageWeeklySWM();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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
                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(metricsMap.get(EnumMetric.SUM) / series.getPopulation());
                    container.setAverageWeeklySWM(aggregate);                    
				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageWeeklySWM(aggregate);  
				}
			}
		}
	}

	private void computeTop10MonthlyConsumptionThresholdSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		// Compute average for all users.
		// Sort consumptions. The base threshold is the consumption of the last
		// user of the top10%.        
		if (container.getTop10BaseMonthSWM() == null
                || container.getTop10BaseMonthSWM().getValue() == null
                || container.getTop10BaseMonthSWM().getLastComputed() == null
				|| container.getTop10BaseMonthSWM().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            Top10BaseMonthSWM aggregate = new Top10BaseMonthSWM();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
            for(UUID uuid : uuidList){
                queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                                .user("user", uuid).meter().average();    
                DataQuery query = queryBuilder.build();
                DataQueryResponse queryResponse = dataService.execute(query);
                ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters(); 
                
                for (GroupDataSeries series : dataSeriesMeter) {
                    if (!series.getPoints().isEmpty()) {
                        ArrayList<DataPoint> userPoints = series.getPoints();
                        DataPoint dataPoint = userPoints.get(0);

                        MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                        Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                        averageConsumptions.add(metricsMap.get(EnumMetric.AVERAGE));
                    }
                }                
            }            
			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top10BaseIndex = (int) (averageConsumptions.size() * 10) / 100;
				Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);

                aggregate.setLastComputed(DateTime.now());
                aggregate.setValue(consumptionThresholdTop10);
                container.setTop10BaseMonthSWM(aggregate);                
			} else {
                aggregate.setLastComputed(null);
                aggregate.setValue(null);
                container.setTop10BaseMonthSWM(aggregate); 
			}
		}
	}

	private void computeTop10WeeklyConsumptionThresholdSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
        
		if (container.getTop10BaseWeekSWM() == null
                || container.getTop10BaseWeekSWM().getValue() == null
                || container.getTop10BaseWeekSWM().getLastComputed() == null
				|| container.getTop10BaseWeekSWM().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            Top10BaseWeekSWM aggregate = new Top10BaseWeekSWM();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
			List<Double> averageConsumptions = new ArrayList<>();
			DataQueryBuilder queryBuilder = new DataQueryBuilder();
                           
            for(UUID uuid : uuidList){
                queryBuilder.timezone(config.getTimezone()).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                                .user("user", uuid).meter().average();    
                DataQuery query = queryBuilder.build();
                DataQueryResponse queryResponse = dataService.execute(query);
                ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters(); 
                
                for (GroupDataSeries series : dataSeriesMeter) {
                    if (!series.getPoints().isEmpty()) {
                        ArrayList<DataPoint> userPoints = series.getPoints();
                        DataPoint dataPoint = userPoints.get(0);

                        MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                        Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                        averageConsumptions.add(metricsMap.get(EnumMetric.AVERAGE));
                    }
                }                
            }

			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top10BaseIndex = (int) (averageConsumptions.size() * 10) / 100;
				Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
                
                aggregate.setLastComputed(DateTime.now());
                aggregate.setValue(consumptionThresholdTop10);
                container.setTop10BaseWeekSWM(aggregate); 
			} else {
                aggregate.setLastComputed(null);
                aggregate.setValue(null);
                container.setTop10BaseWeekSWM(aggregate);
			}
		}
	}
    
	private void computeTop25WeeklyConsumptionThresholdSWM(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getTop25BaseWeekSWM() == null
                || container.getTop25BaseWeekSWM().getValue() == null
                || container.getTop25BaseWeekSWM().getLastComputed() == null
				|| container.getTop25BaseWeekSWM().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            Top25BaseWeekSWM aggregate = new Top25BaseWeekSWM();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();
            
            for(UUID uuid : uuidList){
                queryBuilder.timezone(config.getTimezone()).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                                .user("user", uuid).meter().average();    
                DataQuery query = queryBuilder.build();
                DataQueryResponse queryResponse = dataService.execute(query);
                ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters(); 
                
                for (GroupDataSeries series : dataSeriesMeter) {
                    if (!series.getPoints().isEmpty()) {
                        ArrayList<DataPoint> userPoints = series.getPoints();
                        DataPoint dataPoint = userPoints.get(0);

                        MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                        Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                        averageConsumptions.add(metricsMap.get(EnumMetric.AVERAGE));
                    }
                }                
            }

			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top25BaseIndex = (int) (averageConsumptions.size() * 25) / 100;
				Double consumptionThresholdTop25 = averageConsumptions.get(top25BaseIndex);
                
                aggregate.setLastComputed(DateTime.now());
                aggregate.setValue(consumptionThresholdTop25);
                container.setTop25BaseWeekSWM(aggregate); 

			} else {
                aggregate.setLastComputed(null);
                aggregate.setValue(null);
                container.setTop25BaseWeekSWM(aggregate); 
			}
		}
	}
    
	private void computeAverageMonthlyConsumptionAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageMonthlyAmphiro() == null
                || container.getAverageMonthlyAmphiro().getValue() == null
                || container.getAverageMonthlyAmphiro().getLastComputed() == null
				|| container.getAverageMonthlyAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageMonthlyAmphiro aggregate = new AverageMonthlyAmphiro();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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

                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(sumMonthlyConsumptionAmphiro / series.getPopulation());
                    container.setAverageMonthlyAmphiro(aggregate);                     

				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageMonthlyAmphiro(aggregate); 
				}
			}
		}
	}

	private void computeAverageWeeklyConsumptionAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageWeeklyAmphiro() == null
                || container.getAverageWeeklyAmphiro().getValue() == null
                || container.getAverageWeeklyAmphiro().getLastComputed() == null
				|| container.getAverageWeeklyAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageWeeklyAmphiro aggregate = new AverageWeeklyAmphiro();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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

                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(averageWeeklyAmphiro / series.getPopulation());
                    container.setAverageWeeklyAmphiro(aggregate);                    
                    
				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageWeeklyAmphiro(aggregate); 
				}
			}
		}
	}
    
	private void computeTop10MonthlyConsumptionThresholdAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getTop10BaseMonthAmphiro() == null
                || container.getTop10BaseMonthAmphiro().getValue() == null
                || container.getTop10BaseMonthAmphiro().getLastComputed() == null
				|| container.getTop10BaseMonthAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            Top10BaseMonthAmphiro aggregate = new Top10BaseMonthAmphiro();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
			List<Double> averageConsumptions = new ArrayList<>();

			DataQueryBuilder queryBuilder = new DataQueryBuilder();   
            
            for(UUID uuid : uuidList){
                queryBuilder.timezone(config.getTimezone()).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                                .user("user", uuid).amphiro().average();    
                DataQuery query = queryBuilder.build();
                DataQueryResponse queryResponse = dataService.execute(query);
                ArrayList<GroupDataSeries> amphiroDataSeries = queryResponse.getDevices(); 
                
                for (GroupDataSeries series : amphiroDataSeries) {
                    if (!series.getPoints().isEmpty()) {
                        ArrayList<DataPoint> userPoints = series.getPoints();
                        DataPoint dataPoint = userPoints.get(0);

                        AmphiroDataPoint meterDataPoint = (AmphiroDataPoint) dataPoint;
                        Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                        averageConsumptions.add(metricsMap.get(EnumMetric.AVERAGE));
                    }
                }                
            }             

			if (!averageConsumptions.isEmpty()) {
				Collections.sort(averageConsumptions);
				int top10BaseIndex = (int) (averageConsumptions.size() * 10) / 100;
				Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
                
                aggregate.setLastComputed(DateTime.now());
                aggregate.setValue(consumptionThresholdTop10);
                container.setTop10BaseMonthAmphiro(aggregate);                 

			} else {
                aggregate.setLastComputed(null);
                aggregate.setValue(null);
                container.setTop10BaseMonthAmphiro(aggregate); 
			}
		}
	}

	private void computeAverageTemperatureAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageTemperatureAmphiro() == null
                || container.getAverageTemperatureAmphiro().getValue() == null
                || container.getAverageTemperatureAmphiro().getLastComputed() == null
				|| container.getAverageTemperatureAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageTemperatureAmphiro aggregate = new AverageTemperatureAmphiro();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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
                    
                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(averageTemperatureAmphiro);
                    container.setAverageTemperatureAmphiro(aggregate);
				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageTemperatureAmphiro(aggregate); 
				}
			}
		}
	}

	private void computeAverageSessionConsumptionAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageSessionAmphiro() == null
                || container.getAverageSessionAmphiro().getValue() == null
                || container.getAverageSessionAmphiro().getLastComputed() == null
				|| container.getAverageSessionAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageSessionAmphiro aggregate = new AverageSessionAmphiro();
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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

                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(averageSessionConsumptionAmphiro);
                    container.setAverageSessionAmphiro(aggregate);                    
                    
				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageSessionAmphiro(aggregate); 
				}
			}
		}
	}
    
	private void computeAverageFlowAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageFlowAmphiro() == null
                || container.getAverageFlowAmphiro().getValue() == null
                || container.getAverageFlowAmphiro().getLastComputed() == null
				|| container.getAverageFlowAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageFlowAmphiro aggregate = new AverageFlowAmphiro();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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

                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(averageFlowAmphiro);
                    container.setAverageFlowAmphiro(aggregate);                     

				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageFlowAmphiro(aggregate);
				}
			}
		}
	}
    
	private void computeAverageDurationAmphiro(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer container) {
		if (container.getAverageDurationAmphiro() == null
                || container.getAverageDurationAmphiro().getValue() == null
                || container.getAverageDurationAmphiro().getLastComputed() == null
				|| container.getAverageDurationAmphiro().getLastComputed().isBefore(
						DateTime.now().minusDays(config.getAggregateComputationInterval()))) {

            AverageDurationAmphiro aggregate = new AverageDurationAmphiro();
            
			List<UUID> uuidList = groupRepository.getUtilityByIdMemberKeys(config.getUtilityId());
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

                    aggregate.setLastComputed(DateTime.now());
                    aggregate.setValue(averageDurationAmphiro);
                    container.setAverageDurationAmphiro(aggregate);                     
				} else {
                    aggregate.setLastComputed(null);
                    aggregate.setValue(null);
                    container.setAverageDurationAmphiro(aggregate);
				}
			}
		}
	}

}

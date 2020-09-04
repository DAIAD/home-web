package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Period;

import eu.daiad.web.domain.application.UtilityStatisticsEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.query.EnumMeasurementField;

public interface IUtilityStatisticsRepository
{
    List<UtilityStatisticsEntity> findBy(
        UUID utilityKey, DateTime refDate);

    List<UtilityStatisticsEntity> findBy(
        Group group, DateTime refDate);

    List<UtilityStatisticsEntity> findBy(
        UUID utilityKey, DateTime refDate, Period period);

    List<UtilityStatisticsEntity> findBy(
        Group group, DateTime refDate, Period period);

    List<UtilityStatisticsEntity> findBy(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field);

    List<UtilityStatisticsEntity> findBy(
        Group group, DateTime refDate, Period period, EnumMeasurementField field);

    UtilityStatisticsEntity findOne(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);

    UtilityStatisticsEntity findOne(
        Group group, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);

    UtilityStatisticsEntity save(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic,
        ComputedNumber n);

    UtilityStatisticsEntity save(
        Group group, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic,
        ComputedNumber n);

    void delete(UtilityStatisticsEntity e);
}

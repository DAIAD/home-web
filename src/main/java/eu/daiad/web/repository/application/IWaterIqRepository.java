package eu.daiad.web.repository.application;

import java.util.UUID;

import eu.daiad.web.model.profile.ComparisonRanking;

public interface IWaterIqRepository {

    void clean(int days);

    void update(UUID userKey,
                String from,
                String to,
                ComparisonRanking.WaterIq user,
                ComparisonRanking.WaterIq similar,
                ComparisonRanking.WaterIq nearest,
                ComparisonRanking.WaterIq all,
                ComparisonRanking.MonthlyConsumtpion last1Month,
                ComparisonRanking.MonthlyConsumtpion last6Month);

    ComparisonRanking getWaterIqByUserId(int userId);
}

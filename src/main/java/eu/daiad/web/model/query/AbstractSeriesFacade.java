package eu.daiad.web.model.query;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

public abstract class AbstractSeriesFacade implements SeriesFacade
{
    @Override
    public boolean isEmpty()
    {
        return (size() > 0);
    }

    /**
     * Aggregate values over points.
     * 
     * Note that this implementation clears any internal state on aggregator.
     */
    @Override
    public Double aggregateValues(
        EnumDataField field, EnumMetric metric, StorelessUnivariateStatistic aggregator)
    {
        aggregator.clear();
        for (Point p: iterPoints(field, metric))
            aggregator.increment(p.getValue());
        
        long n = aggregator.getN();
        return (n > 0)? aggregator.getResult() : null;
    }

}

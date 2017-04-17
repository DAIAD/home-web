package eu.daiad.web.model.query;

import org.apache.commons.collections4.Predicate;
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
    public Double aggregate(
        EnumDataField field, EnumMetric metric, StorelessUnivariateStatistic aggregator)
    {
        aggregator.clear();
        for (Point p: iterPoints(field, metric))
            if (p.getValue() != null) 
                aggregator.increment(p.getValue());
        
        long n = aggregator.getN();
        return (n > 0)? aggregator.getResult() : null;
    }
    
    /**
     * Aggregate values over a subset of points (satisfying a predicate).
     * 
     * Note that this implementation clears any internal state on aggregator.
     */
    @Override
    public Double aggregate(
        EnumDataField field, EnumMetric metric, Predicate<Point> pred, StorelessUnivariateStatistic aggregator)
    {
        aggregator.clear();
        for (Point p: iterPoints(field, metric))
            if (p.getValue() != null && pred.evaluate(p)) 
                aggregator.increment(p.getValue());
        
        long n = aggregator.getN();
        return (n > 0)? aggregator.getResult() : null;
    }

    @Override
    public int count(EnumDataField field, EnumMetric metric, Predicate<Point> pred)
    {
        int n = 0;
        for (Point p: iterPoints(field, metric))
            if (p.getValue() != null && pred.evaluate(p))
                n++;
        return n;
    }
}

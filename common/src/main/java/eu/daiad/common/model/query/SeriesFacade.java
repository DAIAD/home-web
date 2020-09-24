package eu.daiad.common.model.query;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

public interface SeriesFacade
{
    /**
     * Get size (i.e. number of points) for this series. 
     */
    int size();
    
    /**
     * Does this series contain any points?
     */
    boolean isEmpty();
 
    /**
     * Get the actual size of the population involved in this series.
     */
    int getPopulationCount();
    
    /**
     * Get a label for this series (optional).
     */
    String getLabel();
    
    /**
     * Get an iterator on (time, value) pairs for this series.
     * 
     * @return an iterable of points
     */
    Iterable<Point> iterPoints(EnumDataField field, EnumMetric metric);
    
    /**
     * Get a scalar value (a number) from the single point contained in this series.
     * 
     * @return a number when this series contains a single point (i.e {@code size() == 1}}),
     *     or null otherwise.
     */
    Double get(EnumDataField field, EnumMetric metric);
    
    /**
     * Get a scalar value (a number) from the single point that satisfies a predicate.
     * 
     * @return a number when such a point is found, or null otherwise.
     */
    Double get(EnumDataField field, EnumMetric metric, Predicate<Point> pred);
    
    /**
     * Aggregate values over points and produce a scalar result.
     */
    Double aggregate(EnumDataField field, EnumMetric metric, StorelessUnivariateStatistic a);
    
    /**
     * Aggregate values over a subset of points (satisfying a predicate) and produce a scalar result.
     */
    Double aggregate(EnumDataField field, EnumMetric metric, Predicate<Point> pred, StorelessUnivariateStatistic a);
    
    /**
     * Count points satisfying a predicate
     */
    int count(EnumDataField field, EnumMetric metric, Predicate<Point> pred);
}

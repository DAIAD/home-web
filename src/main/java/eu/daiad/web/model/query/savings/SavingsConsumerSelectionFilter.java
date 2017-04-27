package eu.daiad.web.model.query.savings;

import java.util.ArrayList;

import eu.daiad.web.model.query.SpatialFilter;

public class SavingsConsumerSelectionFilter {

    protected ArrayList<SavingsPopulationFilter> population = new ArrayList<SavingsPopulationFilter>();

    protected ArrayList<SpatialFilter> spatial = new ArrayList<SpatialFilter>();

    public ArrayList<SavingsPopulationFilter> getPopulation() {
        return population;
    }

    public void setPopulation(ArrayList<SavingsPopulationFilter> population) {
        this.population = population;
    }

    public ArrayList<SpatialFilter> getSpatial() {
        return spatial;
    }

    public void setSpatial(ArrayList<SpatialFilter> spatial) {
        this.spatial = spatial;
    }

}

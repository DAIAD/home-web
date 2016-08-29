package eu.daiad.web.model.weather;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.utility.UtilityInfo;

public class WeatherService {

    private int id;

    private String name;

    private List<UtilityInfo> utilities = new ArrayList<UtilityInfo>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UtilityInfo> getUtilities() {
        return utilities;
    }

}

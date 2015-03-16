package eu.daiad.web.model;

import org.springframework.stereotype.*;

@Component("prototype")
public class Query {

    private String name;

    private int volume;
   
    public Query() {
    }

    public Query(String name, int volume) {
        this.name = name;
        this.volume = volume;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

}

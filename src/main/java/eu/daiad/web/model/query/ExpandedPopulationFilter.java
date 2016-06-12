package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

public class ExpandedPopulationFilter {

    private String label;

    private Long areaId;

    private ArrayList<UUID> users = new ArrayList<UUID>();

    private ArrayList<String> labels = new ArrayList<String>();

    private ArrayList<byte[]> hashes = new ArrayList<byte[]>();

    private ArrayList<byte[]> serials = new ArrayList<byte[]>();

    private Ranking ranking;

    public ExpandedPopulationFilter() {

    }

    public ExpandedPopulationFilter(String label) {
        this.label = label;
    }

    public ExpandedPopulationFilter(String label, Ranking ranking) {
        this.label = label;
        this.ranking = ranking;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ArrayList<UUID> getUsers() {
        return users;
    }

    public ArrayList<byte[]> getHashes() {
        return hashes;
    }

    public ArrayList<byte[]> getSerials() {
        return serials;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

}

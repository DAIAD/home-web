package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpandedPopulationFilter {

    private String label;

    private Long areaId;

    private List<UUID> users = new ArrayList<UUID>();

    private List<String> labels = new ArrayList<String>();

    private List<byte[]> hashes = new ArrayList<byte[]>();

    private List<byte[]> serials = new ArrayList<byte[]>();

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

    public List<UUID> getUsers() {
        return users;
    }

    public List<byte[]> getHashes() {
        return hashes;
    }

    public List<byte[]> getSerials() {
        return serials;
    }

    public List<String> getLabels() {
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

package eu.daiad.common.model.meter;

public class MeterDataStoreStats {

    private int updated = 0;

    private int created = 0;

    public void update() {
        updated++;
    }

    public void create() {
        created++;
    }

    public int getUpdated() {
        return updated;
    }

    public int getCreated() {
        return created;
    }

}

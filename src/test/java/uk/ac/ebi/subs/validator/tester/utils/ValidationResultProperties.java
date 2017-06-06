package uk.ac.ebi.subs.validator.tester.utils;

/**
 * Created by karoly on 24/05/2017.
 */
public class ValidationResultProperties {

    private String entityUuid;
    private int version;
    private boolean updated;

    public ValidationResultProperties() {
    }

    public ValidationResultProperties(String entityUuid) {
        this.entityUuid = entityUuid;
        setUpdated(false);
        setVersion(1);
    }

    public String getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(String entityUuid) {
        this.entityUuid = entityUuid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public void incrementVersion() {
        this.version++;
        setUpdated(true);
    }

    @Override
    public String toString() {
        return "{ entityUuid = " + this.entityUuid +
                ", version = " + this.version +
                ", updated = " + this.updated +
                " }";
    }
}

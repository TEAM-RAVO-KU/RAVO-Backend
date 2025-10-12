package ravo.ravobackend.global.constants;

public enum TargetDB {

    ACTIVE, STANDBY, UNKNOWN;

    @Override
    public String toString() {
        return name();
    }
}
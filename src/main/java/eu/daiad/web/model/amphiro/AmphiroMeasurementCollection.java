package eu.daiad.web.model.amphiro;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.device.EnumDeviceType;

public class AmphiroMeasurementCollection extends DeviceMeasurementCollection {

    private List<AmphiroSession> sessions;

    private List<AmphiroMeasurement> measurements;

    public void setSessions(List<AmphiroSession> value) {
        sessions = value;
    }

    public List<AmphiroSession> getSessions() {
        return sessions;
    }

    public void setMeasurements(List<AmphiroMeasurement> value) {
        measurements = value;
    }

    public List<AmphiroMeasurement> getMeasurements() {
        return measurements;
    }

    @Override
    public EnumDeviceType getType() {
        return EnumDeviceType.AMPHIRO;
    }

    public void removeSession(int index) {
        removeSession(index, true);
    }

    public void removeSession(int index, boolean removeMeasurments) {
        if ((removeMeasurments) && (measurements != null)) {
            for (int i = measurements.size() - 1; i >= 0; i--) {
                if (measurements.get(i).getSessionId() == sessions.get(index).getId()) {
                    measurements.remove(i);
                }
            }
        }
        sessions.remove(index);
    }

    public List<AmphiroMeasurement> getMeasurementsBySessionId(long id) {
        List<AmphiroMeasurement> result = new ArrayList<AmphiroMeasurement>();

        for (AmphiroMeasurement m : measurements) {
            if (m.getSessionId() == id) {
                result.add(m);
            }
        }

        return result;
    }
}

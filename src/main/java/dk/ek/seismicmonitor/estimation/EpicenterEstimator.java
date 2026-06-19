package dk.ek.seismicmonitor.estimation;

import java.util.List;

public interface EpicenterEstimator {

    Location estimate(List<LocationWithDistance> measurements);
}

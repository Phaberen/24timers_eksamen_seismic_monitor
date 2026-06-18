package dk.ek.seismicmonitor.estimation;

import java.util.List;

public interface EpicenterEstimator {

    // Estimates the epicenter from the given measurements.
    // Implementations may throw IllegalArgumentException when no stable result exists.
    Location estimate(List<LocationWithDistance> measurements);
}

package dk.ek.seismicmonitor.estimation;

import java.util.List;

public interface MagnitudeEstimator {

    // Estimates the overall magnitude from the contributing measurements.
    double estimate(List<MagnitudeSample> samples);
}

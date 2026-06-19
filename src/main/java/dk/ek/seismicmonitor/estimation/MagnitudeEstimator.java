package dk.ek.seismicmonitor.estimation;

import java.util.List;

public interface MagnitudeEstimator {

    double estimate(List<MagnitudeSample> samples);
}

package dk.ek.seismicmonitor.estimation;

import java.util.List;

// ESTIMATION LAYER — the contract for working out how strong a quake was.
// Like EpicenterEstimator, it's an interface so the calculation can be swapped (e.g. a plain
// average vs a weighted one) without changing AlertService.
public interface MagnitudeEstimator {

    // Given the samples (each a magnitude + its distance), return the estimated overall magnitude.
    double estimate(List<MagnitudeSample> samples);
}

package dk.ek.seismicmonitor.estimation;

import org.springframework.stereotype.Component;

import java.util.List;

// ESTIMATION LAYER — the simplest MagnitudeEstimator: just the average of the readings' magnitudes.
// A weighted version (closer readings count more) could replace this without touching AlertService.
@Component
public class AverageMagnitudeEstimator implements MagnitudeEstimator {

    @Override
    public double estimate(List<MagnitudeSample> samples) {
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Der skal være mindst én måling.");
        }
        // Pull out each magnitude and average them. orElse(0.0) is just a safety fallback
        // (the empty case is already handled above).
        return samples.stream()
                .mapToDouble(MagnitudeSample::magnitude)
                .average()
                .orElse(0.0);
    }
}
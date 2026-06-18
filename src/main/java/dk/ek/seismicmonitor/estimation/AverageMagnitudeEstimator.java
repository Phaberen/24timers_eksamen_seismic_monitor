package dk.ek.seismicmonitor.estimation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AverageMagnitudeEstimator implements MagnitudeEstimator {

    @Override
    public double estimate(List<MagnitudeSample> samples) {
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Der skal være mindst én måling.");
        }
        return samples.stream()
                .mapToDouble(MagnitudeSample::magnitude)
                .average()
                .orElse(0.0);
    }
}
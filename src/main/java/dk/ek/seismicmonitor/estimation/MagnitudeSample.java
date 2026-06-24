package dk.ek.seismicmonitor.estimation;

// ESTIMATION LAYER — one input to the magnitude math: a single reading's strength and its distance.
// The distance is carried along so a future weighted estimator could give closer readings more say.
public record MagnitudeSample(double magnitude, double distanceToEpicenterKm) { }
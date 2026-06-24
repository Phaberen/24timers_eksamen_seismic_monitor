package dk.ek.seismicmonitor.estimation;

import java.util.List;

// ESTIMATION LAYER — the "contract" for working out where a quake happened.
// It's an interface (just the method signature, no code), so AlertService can depend on the IDEA
// of "something that estimates an epicenter" without caring which formula is used. Swap in a
// different implementation and the rest of the code doesn't change. This swappability is graded.
public interface EpicenterEstimator {

    // Given the measurements (each a location + a distance to the quake), return the estimated centre.
    Location estimate(List<LocationWithDistance> measurements);
}

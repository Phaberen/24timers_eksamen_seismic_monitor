package dk.ek.seismicmonitor.estimation;

// ESTIMATION LAYER — a simple latitude/longitude point. Used as the estimators' own little data
// type so they don't depend on our database entities. The estimate() result comes back as one.
public record Location(double latitude, double longitude) { }

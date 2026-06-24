package dk.ek.seismicmonitor.estimation;

// ESTIMATION LAYER — one input to the epicenter math: a sensor's location plus how far it thinks
// the quake was (the radius of its "circle"). The epicenter estimator takes a list of these.
public record LocationWithDistance(Location location, double distance) { }

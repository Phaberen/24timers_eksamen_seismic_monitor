package dk.ek.seismicmonitor.model;

import dk.ek.seismicmonitor.exception.IllegalAlertTransitionException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// MODEL LAYER (entity) — an earthquake alert, created when 3 valid readings point to one quake.
// Maps to the "earthquake_alert" table. It also owns the rule for how its status may change.
@Entity
@Table(name = "earthquake_alert")
public class EarthquakeAlert {

    // Auto-generated database key.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Where the alert is in its lifecycle. @Enumerated(STRING) stores the name ("ACTIVE") in the
    // database instead of a number, so the column stays readable and safe to reorder the enum.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    private double epicenterLatitude;   // estimated centre of the quake
    private double epicenterLongitude;
    private double magnitude;           // estimated strength

    private String area;                // human-readable place name (from geocoding); may be null

    private LocalDateTime createdAt;

    // Required empty constructor for JPA.
    protected EarthquakeAlert() { }

    // Used when our code creates a fresh alert. Every new alert starts as UNDER_REVIEW
    // (not yet shown to regular users) and stamps its own creation time.
    public EarthquakeAlert(double epicenterLatitude, double epicenterLongitude,
                           double magnitude, String area) {
        this.status = AlertStatus.UNDER_REVIEW;
        this.epicenterLatitude = epicenterLatitude;
        this.epicenterLongitude = epicenterLongitude;
        this.magnitude = magnitude;
        this.area = area;
        this.createdAt = LocalDateTime.now();
    }

    // The ONLY way to change the status. The rule lives here on the object itself, so an illegal
    // jump is impossible no matter who calls it: ask the current status if the move is allowed,
    // and throw if it isn't.
    public void changeStatusTo(AlertStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalAlertTransitionException(status, target);
        }
        this.status = target;
    }

    public Long getId() { return id; }
    public AlertStatus getStatus() { return status; }
    public double getEpicenterLatitude() { return epicenterLatitude; }
    public double getEpicenterLongitude() { return epicenterLongitude; }
    public double getMagnitude() { return magnitude; }
    public String getArea() { return area; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
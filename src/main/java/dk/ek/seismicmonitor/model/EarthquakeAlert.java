package dk.ek.seismicmonitor.model;

import dk.ek.seismicmonitor.exception.IllegalAlertTransitionException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "earthquake_alert")
public class EarthquakeAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stored as the enum name (e.g. "UNDER_REVIEW") rather than an ordinal int.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    private double epicenterLatitude;
    private double epicenterLongitude;
    private double magnitude;

    // Reverse-geocoded area for the epicenter (city/region/country); null if unknown.
    private String area;

    private LocalDateTime createdAt;

    protected EarthquakeAlert() { } // required by JPA

    // New alerts always start UNDER_REVIEW.
    public EarthquakeAlert(double epicenterLatitude, double epicenterLongitude,
                           double magnitude, String area) {
        this.status = AlertStatus.UNDER_REVIEW;
        this.epicenterLatitude = epicenterLatitude;
        this.epicenterLongitude = epicenterLongitude;
        this.magnitude = magnitude;
        this.area = area;
        this.createdAt = LocalDateTime.now();
    }

    // Moves the alert to a new status, enforcing the state machine in one place.
    // Rejects any transition AlertStatus.canTransitionTo does not allow.
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
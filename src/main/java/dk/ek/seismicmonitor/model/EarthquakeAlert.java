package dk.ek.seismicmonitor.model;

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

    private LocalDateTime createdAt;

    protected EarthquakeAlert() { } // required by JPA

    // New alerts always start UNDER_REVIEW.
    public EarthquakeAlert(double epicenterLatitude, double epicenterLongitude, double magnitude) {
        this.status = AlertStatus.UNDER_REVIEW;
        this.epicenterLatitude = epicenterLatitude;
        this.epicenterLongitude = epicenterLongitude;
        this.magnitude = magnitude;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AlertStatus getStatus() { return status; }
    public double getEpicenterLatitude() { return epicenterLatitude; }
    public double getEpicenterLongitude() { return epicenterLongitude; }
    public double getMagnitude() { return magnitude; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
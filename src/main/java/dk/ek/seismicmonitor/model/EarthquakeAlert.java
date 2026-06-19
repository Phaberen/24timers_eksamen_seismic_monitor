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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    private double epicenterLatitude;
    private double epicenterLongitude;
    private double magnitude;

    private String area;

    private LocalDateTime createdAt;

    protected EarthquakeAlert() { }

    public EarthquakeAlert(double epicenterLatitude, double epicenterLongitude,
                           double magnitude, String area) {
        this.status = AlertStatus.UNDER_REVIEW;
        this.epicenterLatitude = epicenterLatitude;
        this.epicenterLongitude = epicenterLongitude;
        this.magnitude = magnitude;
        this.area = area;
        this.createdAt = LocalDateTime.now();
    }

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
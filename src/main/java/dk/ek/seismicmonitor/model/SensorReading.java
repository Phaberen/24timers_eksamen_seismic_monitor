package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_reading")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Business identifier from the payload, e.g. "READ-001".
    @Column(nullable = false, unique = true)
    private String readingId;

    @ManyToOne
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alert_id")
    private EarthquakeAlert alert;

    // Sensor location at the time of the reading (copied from the payload).
    private double latitude;
    private double longitude;

    private double estimatedDistanceToEpicenterKm;
    private double estimatedMagnitude;
    private LocalDateTime recordedAt;

    protected SensorReading() { } // required by JPA

    public SensorReading(String readingId, Sensor sensor,
                         double latitude, double longitude,
                         double estimatedDistanceToEpicenterKm,
                         double estimatedMagnitude,
                         LocalDateTime recordedAt) {
        this.readingId = readingId;
        this.sensor = sensor;
        this.latitude = latitude;
        this.longitude = longitude;
        this.estimatedDistanceToEpicenterKm = estimatedDistanceToEpicenterKm;
        this.estimatedMagnitude = estimatedMagnitude;
        this.recordedAt = recordedAt;
    }

    public void assignToAlert(EarthquakeAlert alert) {
        this.alert = alert;
    }

    public Long getId() { return id; }
    public String getReadingId() { return readingId; }
    public Sensor getSensor() { return sensor; }
    public EarthquakeAlert getAlert() { return alert; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getEstimatedDistanceToEpicenterKm() { return estimatedDistanceToEpicenterKm;}
    public double getEstimatedMagnitude() { return estimatedMagnitude; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
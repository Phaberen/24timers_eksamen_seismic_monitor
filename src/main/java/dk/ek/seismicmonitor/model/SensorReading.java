package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// MODEL LAYER (entity) — a single measurement reported by a sensor at one moment.
// Maps to the "sensor_reading" table. Many readings can come from one sensor, and (later)
// several readings can be tied to one alert.
@Entity
@Table(name = "sensor_reading")
public class SensorReading {

    // Auto-generated database key.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The reading's own id from the data (e.g. "READ-001"). unique = used to spot resends/duplicates.
    @Column(nullable = false, unique = true)
    private String readingId;

    // Each reading belongs to exactly one sensor (many readings -> one sensor).
    // optional = false means a reading can't exist without a sensor.
    // @JoinColumn = this link is stored as a "sensor_id" foreign-key column on this table.
    @ManyToOne(optional = false)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    // The alert this reading helped create, if any. No "optional = false" here on purpose:
    // most readings never lead to an alert, so this stays null until 3 valid readings form one.
    @ManyToOne
    @JoinColumn(name = "alert_id")
    private EarthquakeAlert alert;

    // The sensor's location, copied onto the reading.
    private double latitude;
    private double longitude;

    private double estimatedDistanceToEpicenterKm; // how far the sensor thinks the quake was (km)
    private double estimatedMagnitude;             // how strong the sensor thinks it was
    private LocalDateTime recordedAt;              // when it was measured (no timezone, just date+time)

    // Required empty constructor for JPA.
    protected SensorReading() { }

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

    // Links this reading to the alert it helped create (called once the alert exists).
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

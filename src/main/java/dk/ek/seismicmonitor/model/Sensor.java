package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sensor")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sensorId;

    private double latitude;
    private double longitude;

    protected Sensor() { }

    public Sensor(String sensorId, double latitude, double longitude) {
        this.sensorId = sensorId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getSensorId() { return sensorId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}

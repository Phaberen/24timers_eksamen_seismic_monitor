package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;

// MODEL LAYER (entity) — a physical seismic sensor placed somewhere in the world.
// @Entity + @Table tell JPA this class maps to a database table called "sensor":
// one object here = one row there.
@Entity
@Table(name = "sensor")
public class Sensor {

    // Database primary key. IDENTITY = let the database auto-generate the number on insert.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The sensor's own id from the incoming data (e.g. "SEN-001"). unique = no two rows share it,
    // which is how we recognise a sensor we've already seen.
    @Column(nullable = false, unique = true)
    private String sensorId;

    // Where the sensor sits. Its location is fixed once we first record it.
    private double latitude;
    private double longitude;

    // JPA needs a no-argument constructor to rebuild objects it loads from the database.
    // It's "protected" so our own code doesn't use it by accident.
    protected Sensor() { }

    // The constructor our code actually uses when creating a brand-new sensor.
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

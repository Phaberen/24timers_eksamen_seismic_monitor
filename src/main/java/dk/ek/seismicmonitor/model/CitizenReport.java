package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_report")
public class CitizenReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alert_id")
    private EarthquakeAlert alert;

    // Felt strength on a 1–10 scale.
    @Column(nullable = false)
    private int intensity;

    private LocalDateTime createdAt;

    protected CitizenReport() { } // required by JPA

    public CitizenReport(EarthquakeAlert alert, int intensity) {
        this.alert = alert;
        this.intensity = intensity;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public EarthquakeAlert getAlert() { return alert; }
    public int getIntensity() { return intensity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "citizen_report",
        // One report per user per alert, enforced in the database.
        uniqueConstraints = @UniqueConstraint(
                name = "uk_report_per_user_per_alert",
                columnNames = {"alert_id", "reported_by"})
)
public class CitizenReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alert_id")
    private EarthquakeAlert alert;

    @Column(name = "reported_by", nullable = false)
    private String reportedBy;

    // Felt strength on a 1–10 scale.
    @Column(nullable = false)
    private int intensity;

    private LocalDateTime createdAt;

    protected CitizenReport() { } // required by JPA

    public CitizenReport(EarthquakeAlert alert, String reportedBy, int intensity) {
        this.alert = alert;
        this.reportedBy = reportedBy;
        this.intensity = intensity;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public EarthquakeAlert getAlert() { return alert; }
    public String getReportedBy() { return reportedBy; }
    public int getIntensity() { return intensity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
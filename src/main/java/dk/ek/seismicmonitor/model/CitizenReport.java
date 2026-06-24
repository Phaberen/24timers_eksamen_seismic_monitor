package dk.ek.seismicmonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// MODEL LAYER (entity) — a citizen saying "I felt this quake, and this is how strong".
// Maps to the "citizen_report" table.
// The uniqueConstraint says the pair (alert_id, reported_by) must be unique, i.e. one user can
// file at most ONE report per alert. The database enforces this even if our code missed it.
@Entity
@Table(
        name = "citizen_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_report_per_user_per_alert",
                columnNames = {"alert_id", "reported_by"})
)
public class CitizenReport {

    // Auto-generated database key.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The alert this report is about. A report can't exist without an alert (optional = false).
    @ManyToOne(optional = false)
    @JoinColumn(name = "alert_id")
    private EarthquakeAlert alert;

    // Who filed it — the logged-in user's name. Part of the "one report per user per alert" rule.
    @Column(name = "reported_by", nullable = false)
    private String reportedBy;

    @Column(nullable = false)
    private int intensity; // how strongly they felt it (1–10)

    private LocalDateTime createdAt;

    // Required empty constructor for JPA.
    protected CitizenReport() { }

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

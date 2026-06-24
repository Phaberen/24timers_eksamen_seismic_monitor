# SeismicMonitor — Exam Plan & Knowledge Base

> Full distillation of the 24-hour exam assignment (Programmering 2, 3. semester Datamatiker, EK Guldbergsgade).
> This is the "everything I understood from reading the assignment" reference. Companion file: `PROGRESS.md` (decisions + checklist + oral cheat-sheet).

---

## 1. The exam at a glance
- **Build:** a full-stack REST web application.
- **Backend:** Spring Boot + MySQL, with unit tests of the functionality.
- **Frontend:** HTML/CSS + JavaScript, communicating with the backend via the REST API.
- **Structure:** 5 feature-divided delopgaver; each can span JPA/database, backend/REST, business logic, and frontend.
- **Handed out:** Thu 2026-06-18 09:00. **Due (Wiseflow):** Fri 2026-06-19 09:00.

### Hard rules (violating these can invalidate the submission)
- Work **alone**. Sharing code or ideas with others = exam cheating.
- **No commits to `main` after the deadline** (Fri 09:00). New branches are allowed and encouraged for post-deadline polish (refactoring, bug-fixing, small extensions) — but grading is based on what existed in `main` at the deadline.
- **Commit & push continuously** — don't wait until the last minutes. Verify that submitting actually works.
- AI tools (ChatGPT, Claude, Copilot) **are allowed**. BUT at the oral exam you must add corrections/extensions to your code using only IntelliJ, no other aids → **you must genuinely understand all the code.**
- Grading priority: a feature wired all the way **JPA → backend → frontend (working buttons)** beats having all subpages exist. Build something on both frontend and backend for every part. Use unit tests.
- Post-deadline: don't invent new unrelated features; instead review your own code for parts that could be more solid, elegant, or flexible.

---

## 2. The case — SeismicMonitor
An international emergency-response organization wants a digital system to monitor earth tremors. Seismic sensors are placed in earthquake-risk areas. Sensors record **raw measurements**, but a single measurement doesn't necessarily mean an earthquake occurred. The system must take measurements from **several sensors** and decide whether, *together*, they indicate an earthquake.

When the system judges an earthquake has occurred, it creates an **earthquake alert** (jordskælvsvarsel) with an estimated **epicenter** and estimated **magnitude**.

- **Regular users** can view active alerts and register whether they felt tremors.
- **Admins** can additionally view raw sensor measurements, follow which measurements led to an alert, and set an alert to inactive when it no longer applies.

---

## 3. Sensor data (the input)
A Docker container simulates sensors and **POSTs** measurements to your app at `localhost:8080`.

### Endpoint
`POST /api/sensor-data` — your Spring app must listen here. The container assumes the app runs on `localhost:8080`.

### Payload (JSON array of 1–3 measurements per request)
```json
[
  {
    "readingId": "READ-001",
    "sensorId": "SEN-001",
    "sensorLocation": { "latitude": 55.61880, "longitude": 11.13720 },
    "estimatedDistanceToEpicenterKm": 47.31,
    "estimatedMagnitude": 3.4,
    "recordedAt": "2026-05-20T10:15:30"
  }
]
```
- A request carries **1 to 3** measurements.
- `recordedAt` has no zone/offset → parse as `LocalDateTime`.
- No "user" concept exists in this data — auth users are a separate concern.

### Docker setup (the simulator)
`compose.yml`:
```yaml
services:
  sensors:
    image: ghcr.io/osman-butt/exam-seismic-simulator:latest
    environment:
      STUDENT_APP_BASE_URL: "http://host.docker.internal:8080"
      # SIMULATOR_INTERVAL_MS: 15000
    extra_hosts:
      - "host.docker.internal:host-gateway"
```
- Start: `docker compose up -d`
- Sends simulated measurements every **15 s** (configurable via `SIMULATOR_INTERVAL_MS`).
- Inspect: `docker compose logs sensors`
- If it can't reach your app, the log shows: `Could not send sensor readings to student app: I/O error on POST request for "http://host.docker.internal:8080/api/sensor-data"`.

---

## 4. Estimating epicenter & magnitude
Simplified model — not seismologically correct. The algorithm is a **black box**: just use it, don't re-derive it. **Graded:** the solution must allow swapping to a different calculation method **without changing the calling code** → strategy/interface seam.

### Epicenter (trilateration)
Each measurement gives an estimated *distance* to the epicenter but not a direction; one measurement = a circle. **Three** measurements (three circles) intersect at the estimated epicenter. Given Java method (use as-is):
```java
public Location estimate(List<LocationWithDistance> measurements) {
    if (measurements.size() < 3)
        throw new IllegalArgumentException("Der skal være mindst tre målinger.");

    Location ref = measurements.getFirst().location();
    double refLatRad = Math.toRadians(ref.latitude());
    double refLonRad = Math.toRadians(ref.longitude());
    double earthRadius = 6371.00; // KM

    double[] x = new double[3], y = new double[3], d = new double[3];
    for (int i = 0; i < 3; i++) {
        Location loc = measurements.get(i).location();
        double latRad = Math.toRadians(loc.latitude());
        double lonRad = Math.toRadians(loc.longitude());
        x[i] = earthRadius * (lonRad - refLonRad) * Math.cos(refLatRad);
        y[i] = earthRadius * (latRad - refLatRad);
        d[i] = measurements.get(i).distance(); // km
    }
    double A = 2 * (x[1] - x[0]);
    double B = 2 * (y[1] - y[0]);
    double C = d[0]*d[0] - d[1]*d[1] - x[0]*x[0] + x[1]*x[1] - y[0]*y[0] + y[1]*y[1];
    double D = 2 * (x[2] - x[1]);
    double E = 2 * (y[2] - y[1]);
    double F = d[1]*d[1] - d[2]*d[2] - x[1]*x[1] + x[2]*x[2] - y[1]*y[1] + y[2]*y[2];
    double denominator = A * E - B * D;
    if (Math.abs(denominator) < 1e-12)
        throw new IllegalArgumentException("Målepunkterne giver ingen stabil løsning.");

    double epicenterX = (C * E - B * F) / denominator;
    double epicenterY = (A * F - C * D) / denominator;
    double epicenterLatRad = refLatRad + epicenterY / earthRadius;
    double epicenterLonRad = refLonRad + epicenterX / (earthRadius * Math.cos(refLatRad));
    return new Location(Math.toDegrees(epicenterLatRad), Math.toDegrees(epicenterLonRad));
}
public record Location(double latitude, double longitude) {}
public record LocationWithDistance(Location location, double distance) {}
```
Note: it can throw `IllegalArgumentException` (< 3 measurements, or unstable/degenerate geometry) — must be caught so readings are still saved without an alert.

### Magnitude
Simple **average** of the measurements' magnitudes is acceptable. Optional: **weighted** average where measurements closer to the epicenter weigh more.

---

## 5. Business rules
A request carries 1–3 measurements (only measurements **in the same request** are combined — no cross-request combination required).

**A measurement is valid iff:**
- `sensorLocation` has both `latitude` and `longitude`
- `estimatedDistanceToEpicenterKm` > 0
- `estimatedMagnitude` > 0
- `recordedAt` parses as date/time

**Alert-creation logic:**
- **< 3 valid measurements** → save the measurements, **do NOT** create an alert (epicenter not uniquely determinable).
- **Exactly 3 valid measurements** → attempt to compute epicenter + magnitude.
- **Epicenter calc fails** → still save measurements, **no** alert.
- **3 valid measurements indicate a possible quake** → create alert with status **`UNDER_REVIEW`**, and **link the measurements** that produced it.

**Alert state machine** (`UNDER_REVIEW` is not yet visible to regular users as active):
- `UNDER_REVIEW → ACTIVE` — admin judges the alert valid.
- `UNDER_REVIEW → FALSE_ALARM` — admin judges it a false alarm.
- `ACTIVE → NOT_ACTIVE` — admin judges the danger is over.
- `FALSE_ALARM` and `NOT_ACTIVE` are **terminal** (cannot change back).

---

## 6. The 5 delopgaver (requirements)

### Delopgave 1 — Receive & store sensor data
- Receive `POST /api/sensor-data`.
- Store sensors **and** sensor measurements in the database.
- Handle requests with 1–3 measurements.
- Store measurements even when no alert can be created.
- Allow viewing raw sensor measurements.

### Delopgave 2 — Create earthquake alerts
- Decide whether a request has enough measurements for an alert.
- Compute estimated epicenter.
- Compute estimated magnitude.
- Create an alert with status `UNDER_REVIEW`.
- Link the relevant measurements to the alert.

### Delopgave 3 — Alerts & citizen reports (REST + frontend)
- Show **active** earthquake alerts.
- Show **all** earthquake alerts.
- Create a citizen report with **intensity**.
- Show the **number** of citizen reports per alert.
- Show the citizen reports for an alert.
- Change an alert's status (respecting transition rules): `FALSE_ALARM`, `ACTIVE`, `NOT_ACTIVE`.
- Show which sensor measurements led to an alert.

### Delopgave 4 — Reverse geocoding with OpenStreetMap
- Determine a geographic area for an alert from the epicenter's coordinates (city, region, or country).
- Use an external API, e.g. OpenStreetMap reverse geocoding:
  `GET https://nominatim.openstreetmap.org/reverse?format=json&lat=55.6761&lon=12.5683`
- **Graded:** must be possible to swap OpenStreetMap for another provider → interface seam.

### Delopgave 5 — Security (Spring Security)
- Protect prior features with user roles; at least two roles: **USER**, **ADMIN**.
- **USER** can: see active alerts; create **one** citizen report per alert.
- **ADMIN** can: see raw measurements; see all alerts; change alert status; see citizen reports; see which measurements led to an alert.
- **`POST /api/sensor-data` must stay PUBLIC** (the Docker container sends measurements without login).

---

## 7. Implementation strategy (derived)
Order chosen to always keep something runnable end-to-end:
1. **Setup** — Spring Boot 4.1.x (Web, JPA, Validation, Security, MySQL, H2), MySQL up, project boots on :8080.
2. **Delopgave 1** — entities `Sensor`/`SensorReading`, ingest endpoint, validity check, raw-readings view. Verify against the live Docker simulator.
3. **Delopgave 2** — `EpicenterEstimator` + `MagnitudeEstimator` strategy interfaces, alert creation, link readings, failure handling.
4. **Delopgave 3** — REST + plain JS frontend: alert lists, citizen reports, report counts, status transitions, readings-behind-alert.
5. **Delopgave 4** — `ReverseGeocoder` interface + Nominatim impl; resolve area on alert creation; non-fatal on failure.
6. **Delopgave 5** — Spring Security: in-memory USER/ADMIN, role rules, keep `/api/sensor-data` public.

**Cross-cutting (graded) emphases:** strategy interfaces for the two swappable algorithms + geocoding; unit tests of the core business rules (validity, exactly-3, state transitions, one-report-per-user); working frontend buttons over breadth.

**Key risks / gotchas:**
- Epicenter algorithm throws on degenerate input → must catch and still persist readings.
- `recordedAt` is `LocalDateTime` (no offset).
- State transitions must reject illegal moves (enforce in service layer).
- Security is added last and wraps everything — keep `/api/sensor-data` public; don't break parts 1–4.
- Nominatim requires a descriptive `User-Agent` and has rate limits.
- Docker reaches the host via `host.docker.internal` — app must bind on :8080.

---

## 8. Tech stack & deliverables
- Spring Boot (Spring Web, JPA, H2 and/or MySQL), JUnit tests.
- Frontend: JavaScript, HTML, CSS (same project or separate — we chose **same project**, served as static resources).

### Submission (Wiseflow document)
- Full name + EK-email.
- Link to the **private** GitHub repository (must be private to be valid).
- Short status description of the solution.
- Note any unfinished parts; optionally describe how thoroughly the backend is unit-tested.

### Collaborators to add (private repo) — **before deadline**
**Your class: DATA-GBG-F25B → add `erlmek` and `mnyborg`** (ERLM@ek.dk, MANY@ek.dk).

(Other classes, for reference: F25A → osman-butt, AnikoZs · F25C → per-kiil, BjornChristensen, JarlTuxen.)

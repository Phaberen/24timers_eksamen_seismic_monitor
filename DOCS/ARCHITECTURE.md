# SeismicMonitor — Architecture Guide

A plain-language tour of how the app is built: what each layer does, how a request travels through
it, and where the important rules live. Pair this with the inline comments in the code.

---

## The big idea

Seismic sensors send in measurements. The app stores them, decides when a group of measurements
together means an earthquake, raises an **alert** with an estimated centre and strength, lets
citizens report that they felt it, and shows everything through a small website — with logins so
that regular users and admins see different things.

It's a **layered** Spring Boot app. Each layer has one job and only talks to the layer just below it:

```
            Browser (HTML + JavaScript)
                      │  HTTP + JSON
                      ▼
   Controller  ──►  Service  ──►  Repository  ──►  Database
   (the web)       (the rules)    (data access)    (MySQL)
                      │
                      ├─► Estimation (epicenter + magnitude)
                      └─► Geocoding  (coordinates → place name)
```

Why layers? Each piece is easy to find, test, and change on its own. The web layer doesn't know
about the database; the rules don't know about HTTP.

---

## The layers, top to bottom

### Controller — the web layer (`controller/`)
The "front desk". Receives HTTP requests, hands the work to a service, and returns the result as
JSON. Controllers are deliberately **thin** — no business rules here.
- `SensorDataController` — `POST /api/sensor-data`, the public entry point for sensors.
- `AlertController` — list alerts, change a status, see the readings behind an alert.
- `CitizenReportController` — file a report, count reports, list reports (nested under an alert).
- `SensorReadingController` — admin list of all raw readings.
- `MeController` — `GET /api/me`, tells the frontend who is logged in.

### Service — the business-rules layer (`service/`)
Where the actual decisions happen. Marked `@Transactional` so a batch of database changes either
all succeed or all get undone together.
- `SensorDataService` — saves every reading, dedupes resends, finds-or-creates sensors, and passes
  the valid readings on.
- `AlertService` — the core rule: exactly 3 valid readings → estimate centre + strength → create an
  alert. Also lists alerts and changes their status.
- `CitizenReportService` — creates/counts/lists reports and enforces one report per user per alert.

### Repository — the data-access layer (`repository/`)
One interface per entity, extending `JpaRepository`. Spring Data **writes the implementation for
us** at runtime, including custom finders inferred from the method name (e.g.
`findBySensorId`, `existsByReadingId`). No SQL by hand.

### Model — the domain layer (`model/`)
The data itself, as JPA entities (each maps to a database table), plus the rules that belong to the
data:
- `Sensor`, `SensorReading`, `EarthquakeAlert`, `CitizenReport` — the four tables.
- `AlertStatus` — the four states **and** the rules for moving between them (`canTransitionTo`).
- The status rule lives on `EarthquakeAlert.changeStatusTo` + `AlertStatus`, so an illegal status
  change is impossible no matter who calls it.

### DTO — the data-shape layer (`dto/`)
Small `record` classes describing the JSON going in and out. Entities never leave the service layer;
they're converted to/from DTOs (via `from(...)` mappers) so the public JSON is decoupled from the
database structure. Incoming DTOs also carry the validation (`@NotNull`, `@Min`/`@Max`, `isValid()`).

### Estimation — swappable algorithms (`estimation/`)
The maths, hidden behind interfaces so it can be replaced without touching `AlertService`:
- `EpicenterEstimator` → `TrilaterationEpicenterEstimator` (given black-box trilateration).
- `MagnitudeEstimator` → `AverageMagnitudeEstimator` (simple average).
- Plus tiny value types: `Location`, `LocationWithDistance`, `MagnitudeSample`.

### Geocoding — swappable provider (`geocoding/`)
- `ReverseGeocoder` → `NominatimReverseGeocoder` (calls OpenStreetMap to name the area).
- Best-effort: if the call fails, the alert is still created, just without an area name.

### Exception — error handling (`exception/`)
Domain errors are plain `RuntimeException`s (`AlertNotFoundException`, `DuplicateReportException`,
`IllegalAlertTransitionException`). `GlobalExceptionHandler` catches them in one place and turns
each into the right HTTP status (404 / 409). Services just throw; controllers stay clean.

### Config — security (`config/`)
`SecurityConfig` sets up logins (two in-memory accounts) and the rules for who may call what. Stateless
HTTP Basic; `POST /api/sensor-data` stays public so the sensor simulator (which can't log in) works.

### Frontend — the website (`resources/static/`)
Plain HTML/CSS/JavaScript, no framework, served by the same app (so no cross-site issues).
- `index.html` — the page skeleton (mostly empty placeholders).
- `js/api.js` — the one place that calls the backend (adds the login header, handles errors).
- `js/render.js` — builds the HTML for cards and lists.
- `js/map.js` — the Leaflet map of epicenters.
- `js/app.js` — ties it together: login, loading alerts, button clicks.
- `styles.css` — the styling.

---

## Following one request all the way through

**A sensor sends data** — `POST /api/sensor-data` with 1–3 measurements:
1. `SensorDataController` receives the JSON (as `SensorDataRequest` DTOs) and calls the service.
2. `SensorDataService.ingest` saves **every** reading (skipping any it has already seen), and keeps
   the **valid** ones in a list.
3. It calls `AlertService.createAlertIfWarranted` with that list.
4. `AlertService`: if there are exactly 3 valid readings, it estimates the epicenter (and, if that
   doesn't fail on bad geometry) the magnitude, looks up the area, saves a new alert as
   `UNDER_REVIEW`, and links the 3 readings to it. Otherwise it does nothing — the readings still
   stay saved.
5. The controller returns `201 Created`.

**An admin confirms an alert** — `PATCH /api/alerts/5/status` with `{"status":"ACTIVE"}`:
1. Security checks the caller is an admin (else 401/403).
2. `AlertController` calls `AlertService.changeStatus`.
3. The service loads the alert (404 if missing) and calls `alert.changeStatusTo(ACTIVE)`.
4. The alert asks `AlertStatus.canTransitionTo` — legal, so the status changes; an illegal move
   would throw and become a 409. No explicit save is needed: inside the transaction JPA notices the
   change and writes it automatically.

---

## Why the design choices (quick reference for the oral)

- **Layered** — separation of concerns; each layer is simple and replaceable.
- **DTOs, not entities, on the API** — the JSON shape is decoupled from the database; entities stay
  inside the service layer.
- **Strategy interfaces** for estimation + geocoding — the algorithm/provider can be swapped without
  changing the caller (an explicit grading requirement).
- **Rules in the domain** — alert status changes are guarded on the entity itself, so they can't be
  bypassed.
- **Central exception handling** — domain exceptions → HTTP codes in one place.
- **Stateless HTTP Basic security** added as a thin outer layer; ingest stays public.
- **No-framework frontend** — plain ES modules, split by job (network / rendering / map / glue).

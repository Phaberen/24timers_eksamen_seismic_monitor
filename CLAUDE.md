# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

SeismicMonitor — a full-stack Spring Boot REST app built for a 24-hour Programmering 2 exam (Datamatiker, 3. semester). An international org monitors earth tremors: simulated seismic sensors POST raw measurements, the app decides when a group of measurements together indicates an earthquake, creates an alert with an estimated epicenter/magnitude, and exposes alerts + citizen reports through a role-secured REST API and a vanilla-JS frontend.

The assignment, plan, and progress notes live one directory up: `../assignment.txt`, `../PLAN.md`, `../progress.md`. **Read `../PLAN.md` first** — it is the authoritative spec (the 5 delopgaver, business rules, the given epicenter algorithm, exam constraints). Don't re-derive requirements from code.

## Build / run / test

Uses the Maven wrapper (`mvnw` / `mvnw.cmd`). Java 26, Spring Boot 4.1.0.

```bash
# Start MySQL (required for the app; app expects it on localhost:3306)
docker compose up -d mysql

# Run the app on :8080
./mvnw spring-boot:run

# Start the sensor simulator (POSTs 1-3 measurements every 15s to host :8080)
docker compose up -d sensors
docker compose logs -f sensors        # verify it reaches the app

# Full test suite (uses in-memory H2, no MySQL/Docker needed)
./mvnw test

# Single test class / method
./mvnw test -Dtest=AlertCreationTest
./mvnw test -Dtest=AlertStatusTransitionTest#someMethod

# Package
./mvnw clean package
```

iJava 26 is required for `mvnw` builds — point `JAVA_HOME` at a JDK 26 install (it is usually not the system default).

The app **must** run on `localhost:8080` — the Dockerized simulator reaches the host via `host.docker.internal:8080` and POSTs to `/api/sensor-data`. Tests run against H2 (`src/test/resources/application-test.properties`), production against MySQL (`src/main/resources/application.properties`, ddl-auto=update).

## Architecture

Layered Spring MVC: `controller` (thin, maps HTTP ↔ DTOs) → `service` (business rules, `@Transactional`) → `repository` (Spring Data JPA) → `model` (JPA entities). DTOs are Java records; entities never leave the service layer (responses map via `XxxResponse.from(...)`).

**Ingest flow (the core pipeline):** `SensorDataController` → `SensorDataService.ingest()` → `AlertService.createAlertIfWarranted()`.
- `SensorDataService` saves **every** reading (idempotently — skips duplicates by `readingId`), finds-or-creates the `Sensor`, then passes only the *valid* readings onward. Readings are persisted even when no alert results.
- `AlertService` creates an alert **only when exactly 3 valid readings** are present, computes epicenter + magnitude, links the 3 readings to the alert, and starts it in status `UNDER_REVIEW`.

**Swappable algorithms (graded — keep the seams).** Three strategy interfaces let implementations be replaced without touching calling code:
- `estimation.EpicenterEstimator` → `TrilaterationEpicenterEstimator` (the given black-box trilateration math; do not re-derive it).
- `estimation.MagnitudeEstimator` → `AverageMagnitudeEstimator`.
- `geocoding.ReverseGeocoder` → `NominatimReverseGeocoder` (OpenStreetMap). Tests use `NoopReverseGeocoder`.
`AlertService` depends on the interfaces only. Preserve this — collapsing a strategy into its caller breaks a grading requirement.

**Failure handling is deliberate, not incidental.** The epicenter math throws `IllegalArgumentException` on <3 readings or degenerate geometry; `AlertService` catches it and returns `Optional.empty()` so readings are still saved with no alert. Reverse geocoding failure is non-fatal (area becomes null). Don't "fix" these into hard errors.

**Alert state machine.** Transitions are enforced in the domain, not the controller: `AlertStatus.canTransitionTo()` defines legal moves (`UNDER_REVIEW→ACTIVE|FALSE_ALARM`, `ACTIVE→NOT_ACTIVE`; `FALSE_ALARM` and `NOT_ACTIVE` are terminal). `EarthquakeAlert.changeStatusTo()` throws `IllegalAlertTransitionException` on illegal moves. Add new transition rules in `AlertStatus`, never by bypassing this check.

**Exceptions → HTTP.** Domain exceptions (`AlertNotFoundException`, `DuplicateReportException`, `IllegalAlertTransitionException`) are translated to status codes centrally in `exception.GlobalExceptionHandler`. Throw the domain exception from the service; don't return `ResponseEntity` error bodies from controllers.

**Security.** `config.SecurityConfig` — stateless HTTP Basic, in-memory users `user/user` (ROLE_USER) and `admin/admin` (ROLE_ADMIN), CSRF disabled. Authorization is URL-pattern based in the filter chain (not method annotations). Two invariants: `POST /api/sensor-data` **must stay public** (the simulator has no login), and admin-only endpoints (raw `/api/readings`, all alerts, status changes, reports list, readings-behind-alert) must stay `hasRole("ADMIN")`. USER may view active alerts and file one citizen report per alert.

**Frontend.** Plain HTML/CSS/JS served as static resources from `src/main/resources/static/` (`index.html`, `js/api.js`, `js/app.js`, `js/render.js`, `js/map.js`). No build step. Talks to the REST API with Basic auth.

## Key API surface

- `POST /api/sensor-data` — public ingest (JSON array, 1–3 measurements; `recordedAt` is a zoneless `LocalDateTime`).
- `GET /api/readings` — admin: all raw readings.
- `GET /api/alerts` (admin, all) · `GET /api/alerts/active` (user+admin) · `PATCH /api/alerts/{id}/status` (admin) · `GET /api/alerts/{id}/readings` (admin).
- `POST /api/alerts/{alertId}/reports` (user+admin) · `GET .../reports/count` (user+admin) · `GET .../reports` (admin).
- `GET /api/me` — current authenticated user.

## Gotchas

- The simulator can resend the same `readingId`; ingest dedupes on it — keep that idempotency.
- Only measurements **within the same request** are combined into an alert (no cross-request aggregation).
- Static-resource paths in `SecurityConfig` permit `/Js/**` and `/styles.css`, but JS files live under `static/js/` (lowercase). If JS fails to load due to auth, reconcile the `permitAll` matcher with the actual path.
- Exam rule: nothing graded may be committed to `main` after the deadline — branch for any post-deadline work.

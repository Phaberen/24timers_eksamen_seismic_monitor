# SeismicMonitor — Plan & Progress

> 24-hour exam · Programmering 2 · 3. semester Datamatiker, EK Guldbergsgade
> **Handed out:** Thu 2026-06-18 09:00 · **Deadline (Wiseflow + no more commits to `main`):** Fri 2026-06-19 09:00
> Kept one level above the `SeismicMonitor` repo (outside git) as a personal planning doc — intentionally NOT part of the submission.

> **STATUS (2026-06-18): all 5 delopgaver complete** (Setup + 1–5), wired JPA → backend → frontend, secured. Remaining items are optional polish + a few extra tests + the pre-deadline submission steps (private repo, collaborators, Wiseflow). See §4 for the per-item checklist and §6 for the log.

---

## 0. How to use this file
- This is the single source of truth for *what we're building, what's decided, and what's left*.
- Check items off (`[x]`) as they're completed and verified (compiles + tested).
- The **Architecture decisions & tradeoffs** section (below) is your **oral-exam cheat-sheet** — keep it honest and current.
- Priority rule from the brief: **a working JPA→backend→frontend feature beats many empty pages.** Always have something runnable.

---

## 1. Locked decisions
| # | Decision | Choice | Rationale |
|---|----------|--------|-----------|
| 1 | Repo layout | Single repo | Less git/collaborator overhead, no CORS, faster in 24h. Brief allows "samme projekt". |
| 2 | Frontend serving | Spring static resources (`src/main/resources/static`) | Same origin → `fetch('/api/...')` just works, no CORS. |
| 3 | CORS | Built on `feature/cors-demo` branch only | Demonstrational artifact for the oral; not needed for same-origin. |
| 4 | Database | MySQL (app) + H2 (tests) | Brief allows "H2 og/eller MySQL". H2 keeps tests fast & isolated. |
| 5 | Security | HTTP Basic + in-memory USER/ADMIN on `main` | Auth carries no domain data (sensor JSON has no user) → separate, low-risk concern. DB-users = optional stretch branch. |
| 6 | Architecture | Layered (controller/service/repository/model/dto/config) | Conventional, easiest to navigate and defend orally. |
| 7 | Swappable algorithms | Strategy interfaces for epicenter/magnitude AND geocoding | **Explicitly graded** ("skifte til en anden beregningsmetode/API uden at koden ændres"). |
| 8 | Magnitude | Simple average (default impl) | Brief allows simple average; weighted average = optional second strategy impl. |
| 9 | Stack | Spring Boot 4.1.0, Java 26 | Most recent. Note: pulls Spring Security 7 (config DSL differs slightly from 3.x examples). |

---

## 2. Architecture decisions & tradeoffs (ORAL CHEAT-SHEET)
Short, honest answers to rehearse. Expand as we build.

**Single repo vs two repos.** Chose single repo: frontend served as static resources from Spring Boot, same origin as the API. Pros: no CORS, one push target, one set of collaborators, less to manage in a 24h timed exam. Two-repo tradeoff: cleaner separation and independent deployment (frontend → static host/CDN, backend → app server), but it costs CORS config and double the git overhead with no benefit that's graded here. To show I understand the split, I added a `feature/cors-demo` branch with a real `CorsConfigurationSource` allowing a hypothetical separate frontend origin.

**Swappable algorithms (strategy pattern).** Epicenter estimation sits behind an `EpicenterEstimator` interface; magnitude behind a `MagnitudeEstimator` interface; reverse geocoding behind a `ReverseGeocoder` interface. Each has one concrete impl wired by Spring. To swap algorithm/provider I change only the bean wiring, not the calling code — that's the "uden at koden ændres" requirement.

**Security.** HTTP Basic with two in-memory accounts (USER, ADMIN). The sensor payload contains no user concept, so auth is a thin cross-cutting layer added last without touching parts 1–4. `POST /api/sensor-data` stays public (the simulator can't log in). Tradeoff: Basic sends credentials per request (only safe over TLS, which we don't have locally) and users aren't persisted/registerable. A DB-backed `UserDetailsService` (the stretch branch) would integrate with JPA and support many distinct citizen accounts, but adds entities + CSRF considerations for the final hours.

**recordedAt parsing / validity.** `recordedAt` has no zone/offset → parsed as `LocalDateTime`. A reading is valid iff lat+lon present, distance > 0, magnitude > 0, and `recordedAt` parses.

**Alert state machine.** Transitions enforced in the service layer (not just the controller): `UNDER_REVIEW→ACTIVE`, `UNDER_REVIEW→FALSE_ALARM`, `ACTIVE→NOT_ACTIVE`. `FALSE_ALARM` and `NOT_ACTIVE` are terminal; any other transition throws. The rule lives on the **entity** (`EarthquakeAlert.changeStatusTo` → `AlertStatus.canTransitionTo`, an exhaustive `switch` expression), so an illegal jump is impossible regardless of caller; the service just loads/saves and the advice maps the exception to **409**.

### Considerations & deliberations made during the build (extra oral cheat-sheet)
Honest record of *why* things are the way they are — and what was deliberately left out.

- **Slim-first, defer to the right delopgave.** Built the bare minimum each delopgave required, rather than front-loading. Concretely: the citizen report's **user identity + one-report-per-user** rule was deferred from Delopgave 3 to **Delopgave 5**, because that's where a real authenticated principal exists. In D3 a report was just `{alert, intensity}`; in D5 it gained `reportedBy` (from `Authentication.getName()`) + the unique `(alert, reportedBy)` constraint + `DuplicateReportException`(409). Defending the *non*-choice matters as much as the choice.
- **Public ingest vs admin read.** `POST /api/sensor-data` is the simulator pushing data *in* → must stay public (it can't log in). `GET /api/readings` is a human reading data *out* → ADMIN. Same data, opposite directions, different access — a deliberate distinction, not an oversight.
- **Security shape.** Stateless HTTP Basic + in-memory `user`/`admin`, `{noop}` passwords. Chose `{noop}` over BCrypt on purpose: in-memory exam users, no real secrets; "production would hash" is the honest one-liner. CSRF disabled because it's a stateless token-per-request API, not a session+cookie app.
- **Geocoding is best-effort.** A Nominatim failure returns `Optional.empty()` and never blocks alert creation; the test profile swaps a `NoopReverseGeocoder` (`@Profile`) for the real impl — which both keeps tests offline *and* demonstrates the swap seam. Known smell: the HTTP call currently sits inside the create `@Transactional` (network I/O in a txn) — flagged, low impact at 15s simulator cadence, left as polish.
- **DRY.** `EarthquakeAlertRepository.getByIdOrThrow` (a Spring Data `default` method) is the single home for "find by id or 404", removing the same lookup that was written three times across two services. Trade-off accepted: the repository now imports the domain exception.
- **DTOs as records + `from` mappers.** Entities never leave the service layer; every endpoint returns a record DTO, mapped in one place, so the JSON shape is decoupled from the JPA mapping.
- **Frontend: vanilla ES modules, no framework.** Split by responsibility — `api.js` (fetch), `render.js` (DOM), `app.js` (orchestration), `map.js` (Leaflet). Rejected a SPA framework on purpose: build tooling is harder to defend in an IntelliJ-only oral, the brief asks for plain HTML/CSS/JS, and there's only one view (two filters ≠ routing). In-place card updates avoid a full re-render (no scroll-jump). Role-aware UI is driven by `GET /api/me`.
- **CSS as a small design system.** `:root` custom-property tokens (palette, status hues, fonts, radii); badge/edge shades **derived** from one per-status hue via `color-mix()` (12 hand-tuned rgba values → 4 hue vars); status-colored card edge via the `:has()` selector — pure CSS, no JS change.
- **Map scope call.** Considered drawing the **trilateration circles** (sensor location = centre, `estimatedDistanceToEpicenterKm` = radius; the three intersect at the epicenter — exactly the assignment's model). Left it out: readings are ADMIN-only, so it'd be an on-click per-alert fetch, not a general view. Rejected a magnitude-sized "halo" circle outright — it implies a felt-radius the model doesn't define and would be misleading. Map ships as clean epicenter markers.
- **Test strategy.** `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional` on H2 (not `@DataJpaTest` — fails to load context under Boot 4.1). Tests are feature-named (`AlertCreationTest`, `CitizenReportTest`, …). Pure logic (state-machine) tested without Spring; security tested with MockMvc + `@WithMockUser`.
- **Known improvements left for post-deadline branches (in the Wiseflow note):** escape backend strings before `innerHTML` (XSS via the public ingest endpoint); fold `reportCount` into `AlertResponse` to kill the per-card N+1; make `togglePanel` actually toggle closed; split `auth.js` out of `app.js`; move geocoding outside the transaction.

---

## 3. Domain model
```
Sensor (1) ──< SensorReading (N) >── (1) EarthquakeAlert (1) ──< CitizenReport (N)
```
- **Sensor** — `sensorId` (natural key, upsert find-or-create), `latitude`, `longitude` (sensor placement).
- **SensorReading** — `readingId` (natural key, unique → idempotency/dedupe), FK→Sensor, `estimatedDistanceToEpicenterKm`, `estimatedMagnitude`, `recordedAt` (`LocalDateTime`), FK→EarthquakeAlert (nullable; set when 3 readings produce an alert).
- **EarthquakeAlert** — epicenter `latitude`/`longitude`, `estimatedMagnitude`, `status` (enum), `area` (part 4, nullable), `createdAt`. Has the readings that produced it.
- **CitizenReport** — FK→EarthquakeAlert, `intensity`, `reportedBy` (principal username), `createdAt`. Unique (alert, reportedBy) → one report per user per alert.
- **AlertStatus** enum — `UNDER_REVIEW`, `ACTIVE`, `FALSE_ALARM`, `NOT_ACTIVE`.

### Proposed package layout (`dk.ek.seismicmonitor`)
```
config/        SecurityConfig, (WebConfig)
controller/    SensorDataController, AlertController, CitizenReportController
service/       SensorDataService, AlertService, CitizenReportService
repository/    SensorRepository, SensorReadingRepository, EarthquakeAlertRepository, CitizenReportRepository
model/         Sensor, SensorReading, EarthquakeAlert, CitizenReport, AlertStatus
dto/           SensorReadingRequest, SensorLocationDto, AlertResponse, CitizenReportRequest, ...
estimation/    EpicenterEstimator (interface) + TrilaterationEpicenterEstimator,
               MagnitudeEstimator (interface) + AverageMagnitudeEstimator,
               Location (record), LocationWithDistance (record)
geocoding/     ReverseGeocoder (interface) + NominatimReverseGeocoder
exception/     GlobalExceptionHandler, domain exceptions
```

---

## 4. Work breakdown & progress

### Setup
- [x] Initialize Spring Boot 4.1.x project (Web, Data JPA, Validation, Security, MySQL driver, H2)
- [x] `application.properties` → MySQL datasource; `application-test.properties` → H2
- [x] MySQL running (Docker `compose.yml`) + schema created
- [x] Verify project boots cleanly on :8080 (runs from IntelliJ on JDK 26)
- [ ] Make repo private + add F25B collaborators (`erlmek`, `mnyborg`) — **before deadline**

### Delopgave 1 — Receive & store sensor data ✅ DONE + verified
- [x] `Sensor`, `SensorReading` entities + repositories
- [x] DTOs for the incoming JSON (`SensorDataRequest`, `SensorLocationDto`, `SensorReadingResponse`)
- [x] `POST /api/sensor-data` controller (accepts list of 1–3 readings)
- [x] `SensorDataService`: upsert sensors (find-or-create, fixed location); save readings; skip duplicate readingId
- [x] Save readings even when no alert is created
- [x] Endpoint to view raw readings (`GET /api/readings`)
- [x] **Tests:** 1–3 readings stored; find-or-create sensor; duplicate readingId skipped; DTO view (5 tests green)

### Delopgave 2 — Create earthquake alerts ✅ DONE + verified (10 tests green)
- [x] `EpicenterEstimator` interface + `TrilaterationEpicenterEstimator` (given algorithm, black-box)
- [x] `MagnitudeEstimator` interface + `AverageMagnitudeEstimator` (takes `MagnitudeSample` = magnitude + distance, so a weighted impl can swap in)
- [x] `EarthquakeAlert` entity + repository + `AlertStatus` enum; `SensorReading.alert` link (nullable)
- [x] `SensorDataRequest.isValid()` (lat/lon present, distance>0, magnitude>0, recordedAt present)
- [x] On epicenter failure → still save readings, no alert (`AlertService` catches `IllegalArgumentException`)
- [x] Create alert `UNDER_REVIEW`; link the 3 readings to it (`AlertService.createAlertIfWarranted`)
- [x] Logic wiring: `SensorDataService.ingest` collects valid persisted readings → calls `AlertService` (file 14 done). Note: `findByReadingId` NOT needed — trio = valid readings persisted in *this* request; resends are dedup-skipped so a full re-send yields an empty list → no duplicate alert.
- [x] **Tests:** exactly-3 triggers alert (status UNDER_REVIEW + readings linked); <3 → no alert but saved; invalid-in-trio → no alert; estimator failure → no alert but saved (`AlertCreationTest`, 4 tests green)

### Delopgave 3 — Alerts, reports & frontend ✅ DONE
- [x] `GET /api/alerts/active` active alerts only (status=ACTIVE; UNDER_REVIEW not public) — `findByStatusOrderByCreatedAtDesc`
- [x] `GET /api/alerts` all alerts (admin) — `findAllByOrderByCreatedAtDesc`
- [x] `CitizenReport` entity + repository (unique `(alert, reportedBy)`)
- [x] `POST /api/alerts/{id}/reports` create citizen report with intensity (1–10)
- [x] `GET /api/alerts/{id}/reports/count` + `GET /api/alerts/{id}/reports` (count + list per alert)
- [x] `PATCH /api/alerts/{id}/status` change alert status with **state-transition validation** (rule on `AlertStatus.canTransitionTo` + `EarthquakeAlert.changeStatusTo`; illegal→409, unknown id→404 via `GlobalExceptionHandler`)
- [x] `GET /api/alerts/{id}/readings` readings that led to an alert (`SensorReadingResponse.from` shared mapper)
- [x] Frontend: list active/all alerts, expandable detail (readings + reports), submit report, admin status buttons — vanilla **ES modules** (`Js/api.js` fetch, `Js/render.js` DOM, `Js/app.js` orchestration), in-place card updates (no scroll-jump)
- [x] **Tests:** `CitizenReportTest` — report created/counted/listed; one-report-per-user (duplicate→409); unknown alert→404
- [x] **Tests:** `AlertStatusTransitionTest` — pure logic: legal moves succeed, illegal jumps + terminal states throw `IllegalAlertTransitionException`
- [x] Frontend: **Leaflet map** of alert epicenters (`Js/map.js`; markers respect the active/all filter, popup shows id/status/area/magnitude)

### Delopgave 4 — Reverse geocoding (OpenStreetMap) ✅ DONE
- [x] `ReverseGeocoder` interface + `NominatimReverseGeocoder` impl (`RestClient`, parses city→town→village→state→country→display_name)
- [x] On alert creation, resolve epicenter → area; store on alert (`AlertService` injects geocoder, sets `area`)
- [x] Handle Nominatim failure gracefully (catches all → `Optional.empty()`; alert still created, area null)
- [x] Respect Nominatim usage policy (descriptive `User-Agent` header)
- [x] Show area in frontend (each card shows `Area: ...` / "Unknown")
- [x] **Swap seam proven:** test profile activates a `NoopReverseGeocoder` (`@Profile("test")`) instead of the Nominatim impl (`@Profile("!test")`) — tests stay offline *and* demonstrate the provider is swappable without touching callers
- [x] **`area` length guarded:** `NominatimReverseGeocoder` truncates the resolved area to 255 chars before returning, so a long `display_name` can't break alert creation on MySQL.

### Delopgave 5 — Security ✅ DONE
- [x] `SecurityConfig` (Spring Security 7 DSL): stateless HTTP Basic, CSRF off, in-memory `user`/`user` (USER) + `admin`/`admin` (ADMIN), `{noop}` passwords
- [x] `POST /api/sensor-data` → `permitAll` (+ static assets `/`, `/index.html`, `/styles.css`, `/Js/**`)
- [x] USER: see active alerts, create one report per alert (identity from `Authentication.getName()`; one-per-user enforced)
- [x] ADMIN: raw readings, all alerts, change status, see reports, see readings behind an alert
- [x] `GET /api/me` → `{username, admin}` so the frontend renders role-appropriate UI
- [x] Frontend login bar (Basic via `btoa`), `Js/api.js` attaches `Authorization`; USER sees active-only (All tab hidden), ADMIN sees all + admin buttons; logout clears auth
a- [x] **Tests:** `SecurityAccessTest` (MockMvc + `@WithMockUser`) — `POST /api/sensor-data` public (201); `GET /api/alerts` 401 anonymous / 403 for USER / 200 for ADMIN; USER 200 on `/api/alerts/active`

### Stretch / branches (post-main, allowed after deadline on non-main branches)
- [ ] `feature/cors-demo` — `CorsConfigurationSource` for a separate frontend origin (oral artifact)
- [ ] `feature/db-users` — DB-backed `UserDetailsService` (User/Role entities, form login)
- [ ] Weighted-average magnitude as a second `MagnitudeEstimator` impl

### Submission
- [ ] Final commit + push to `main` **before Fri 09:00**
- [ ] Repo is **private** + collaborators added
- [ ] Wiseflow doc: full name + EK-email, private repo link, status description, note unfinished parts + test coverage

---

## 5. Suggested time budget (24h window, includes sleep)
| Block | Focus |
|-------|-------|
| Thu 09–11 | Setup, MySQL, data model, project boots |
| Thu 11–14 | Delopgave 1 (ingest + store + tests) |
| Thu 14–18 | Delopgave 2 (epicenter/magnitude strategies + alert + tests) |
| Thu 18–22 | Delopgave 3 (REST + reports + state machine + frontend) |
| Thu 22–23 | Commit/push checkpoint; sleep |
| Fri 06–07 | Delopgave 4 (geocoding) |
| Fri 07–08 | Delopgave 5 (security) |
| Fri 08–09 | Buffer, final tests, push, Wiseflow submission |

> Commit & push **after every working feature** — never wait until the end. Verify the push lands on GitHub.

---

## 6. Status log
- 2026-06-18 — Decisions locked, plan written. Project not yet initialized.
- 2026-06-18 — Setup + Delopgave 1 complete and verified end-to-end against the live Docker simulator (5 unit tests green). Note: `@DataJpaTest` fails to load context under Spring Boot 4.1 (autoconfigure relocated) → tests use `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional` on H2.
- 2026-06-18 — Delopgave 3 started: alert status **state machine** complete end-to-end (9 classes). `AlertStatus.canTransitionTo` (exhaustive switch) → `EarthquakeAlert.changeStatusTo` (throws on illegal) → `AlertService.changeStatus(id, target)` (load/404, dirty-checked flush) → `PATCH /api/alerts/{id}/status` (`AlertController`). New `exception/` package: `IllegalAlertTransitionException`(409), `AlertNotFoundException`(404), `GlobalExceptionHandler` (`@RestControllerAdvice`, `ProblemDetail`). New DTOs: `ChangeAlertStatusRequest`(@NotNull status), `AlertResponse`(+`from` mapper). NOT yet tested/committed. **NEXT: read endpoints (repo `findByStatus`, service `getAllAlerts`/`getActiveAlerts`, GET `/api/alerts` + `/api/alerts/active`), then citizen reports, then transition tests, then frontend.**
- 2026-06-18 — Delopgave 2 DONE + verified (10 tests green). Added `AlertCreationTest` (4 tests, drives real ingest→AlertService flow on H2). Fixed 3 issues found while testing: (1) `SensorReading.alert` was `@ManyToOne(optional=false)` → made nullable (readings must save without an alert); (2) tightened `SensorReading.sensor` to `optional=false` (a reading always has a sensor); (3) `SeismicMonitorApplicationTests` (Initializr scaffold) had bare `@SpringBootTest` with no profile → booted on default MySQL profile and failed contextLoads when Docker down → added `@ActiveProfiles("test")`. Whole suite now H2/Docker-independent.
- 2026-06-18 — Delopgave 2 wiring done (file 14): `SensorDataService.ingest` now collects this request's valid persisted readings and calls `AlertService.createAlertIfWarranted`. Decided AGAINST `findByReadingId` (cross-request combining not required by brief; avoids duplicate alerts on resends). **Next: Delopgave 2 unit tests (file 15), then Delopgave 3.**
- 2026-06-18 — Delopgave 2 in progress: estimation package built (EpicenterEstimator/MagnitudeEstimator interfaces + Trilateration/Average impls; Location/LocationWithDistance/MagnitudeSample records), EarthquakeAlert entity + AlertStatus enum + repository, SensorReading→alert link, SensorDataRequest.isValid(), and AlertService done. **Next: wire SensorDataService.ingest → AlertService (file 14), then Delopgave 2 tests.** Divergences from earlier plan: stack is Java 26 (not 21); incoming DTO named `SensorDataRequest`; `isValid()` on the DTO; MagnitudeEstimator takes `MagnitudeSample` to keep weighted-average swap possible.
- 2026-06-18 — Delopgave 3 COMPLETE. Read endpoints (`GET /api/alerts`, `/active`). Citizen reports built slim first (no user identity), nested under `/api/alerts/{id}/reports` (POST create, GET count, GET list). Readings-behind-alert (`GET /api/alerts/{id}/readings`) — lifted reading→DTO mapping into `SensorReadingResponse.from`. `CitizenReportTest` added. Frontend built (vanilla JS) then refactored into ES modules `Js/{api,render,app}.js` + in-place card updates to kill the scroll-jump. Decision: a framework SPA was rejected as over-engineering for the brief; native ES modules give clean separation with zero tooling.
- 2026-06-18 — Delopgave 4 COMPLETE. `geocoding/ReverseGeocoder` interface + `NominatimReverseGeocoder` (`RestClient`, User-Agent, best-effort → empty on failure). `area` added to `EarthquakeAlert` (4-arg ctor) + `AlertResponse` + frontend card. Test profile uses `NoopReverseGeocoder` (`@Profile`) so tests stay offline and the swap seam is demonstrated. Confirmed against the brief: Delopgave 4 wants an area NAME string, **not** a map; a Leaflet map remains optional post-required polish.
- 2026-06-18 — Delopgave 5 COMPLETE → **all 5 delopgaver done.** Stateless HTTP Basic, in-memory user/admin, role rules per the brief; `POST /api/sensor-data` stays public; `GET /api/readings` is ADMIN-only. Report identity now from the authenticated principal → `reportedBy` + unique `(alert, reported_by)` + `DuplicateReportException`(409) restored (these were deliberately deferred from Delopgave 3 to here). `GET /api/me` + frontend login bar with role-aware UI. `CitizenReportTest` updated (+ duplicate-report test).
- 2026-06-18 — Infra clarified: `compose.yml` runs **both** MySQL 8.4 (production DB, persisted in named volume `mysql-data`) **and** the sensors simulator. Tests use H2 `MODE=MySQL`. Note for MySQL verification: `ddl-auto=update` won't retrofit the new NOT NULL `reported_by` / unique constraint onto the old volume schema → use `docker compose down -v` to rebuild fresh. Volumes are host-local (do not follow you to another device / Docker login). **Remaining optional polish:** guard `area` length vs VARCHAR(255); dedicated transition + role-access tests; Leaflet epicenter map; BCrypt over `{noop}`.
- 2026-06-19 — MySQL run confirmed (compose up/down seamless, app compiles). Closed the optional polish (except BCrypt, intentionally skipped): (1) `area` truncated to 255 in the geocoder; (2) `AlertStatusTransitionTest` (pure logic) + (3) `SecurityAccessTest` (MockMvc role rules) added; (4) Leaflet epicenter map (`Js/map.js`, 4th ES module) wired into the load flow — markers per alert respecting the active/all filter. Frontend now 4 modules: api/render/app/map. Leaflet loaded from CDN (needs internet). IntelliJ shows a harmless "missed locally stored library" warning on the CDN `<script>` — ignore.

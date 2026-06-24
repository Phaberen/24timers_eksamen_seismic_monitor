# SeismicMonitor — Oral Exam Prep Tracker

> Concept inventory for the IntelliJ-only oral (you defend/extend the code with no AI).
> Companion to `PLAN.md` (spec) and `progress.md` (decisions + cheat-sheet).

## Legend
- `[ ]` not yet rehearsed out loud · `[x]` rehearsed & comfortable
- ✍️ = a written defense already exists in `progress.md` §2 (drafted, but still rehearse it aloud)
- 🔲 = no written answer yet — needs both an answer AND rehearsal
- ⚡ = high-likelihood probe / "extend the code live" candidate
- 📖 = explained with Claude in a session (see session log at bottom) — you've heard it, now say it back solo

> NOTE: 📖 means "covered, but not yet rehearsed by you". Flip 📖 → `[x]` once you can explain it
> out loud without notes. The whole codebase now also has plain-English comments to study from.

---

## ⭐ Exam format & what the examiner wants (from the F25B email, received just before the exam)

**Logistics:** 3 days of oral exams. Morning slots start 09:00; each exam is **30 min incl.
assessment**. Roughly: slot #5 ≈ 11:00. Be ready ~15 min before your slot.

**Before the exam — have ready:**
- IDE + project open, **including the Docker container** running.
- A way to **view the database** (MySQL Workbench, terminal, or IntelliJ's database tool). ← ties
  directly to the "does this really use SQL?" walkthrough below — be able to open the DB and show rows.
- **AI assistance turned OFF in the IDE.**

**During the exam:**
1. **Start with a ~5 min demo** of the running app.
2. Then questions. You must **know your project incl. its weaknesses**, and understand a **full
   flow Frontend → Backend → DB**.
3. **Explain what the code does AND argue why** you did it that way. Any layer is fair game —
   frontend / backend / db, **including tests**. Topics span **Spring (+ the dependencies we used),
   Java, and DB**. What counts most is **ownership**: you understand your code and use correct
   **technical terminology (fagtermer)**.
4. **Live coding, no AI.** Be able to: add a **new endpoint**, **fix a bug**, add a **small feature
   end-to-end** (JS call → controller), wire **JS events**, do a **JS fetch with async/await**,
   **write a test**, etc. — basically anything from this semester.

> Takeaway for prep priority: the live-coding list above maps onto the "Extend the code live"
> section at the bottom — rehearse those hands-on. And practise narrating one full
> Frontend → Backend → DB flow end to end.

---

## 🗄️ Viewing the live database (for the demo + the "is it real SQL?" question)

Real data lives in **MySQL** in the `mysql` Docker container. Start it: `docker compose up -d mysql`.
Connection details (from `compose.yml` / `application.properties`):

| Field | Value |
|---|---|
| Host | `localhost` (or `127.0.0.1`) |
| Port | `3306` |
| Database | `seismicmonitor` |
| User / Password | `seismic` / `seismic` (or `root` / `root`) |

Tables: `sensor`, `sensor_reading`, `earthquake_alert`, `citizen_report`.

**Option A — IntelliJ Database tool (easiest; IDE is already open):**
1. Right edge **Database** tab → **+** → **Data Source → MySQL**.
2. Host `localhost`, Port `3306`, Database `seismicmonitor`, User `seismic`, Password `seismic`.
3. Click **download driver** if prompted → **Test Connection** → **OK**.
4. Expand `seismicmonitor → tables`, double-click a table, or run in a query console:
   `SELECT * FROM earthquake_alert;` / `SELECT * FROM sensor_reading;`

**Option B — MySQL Workbench (step by step):**
1. Start the DB container: `docker compose up -d mysql`.
2. Workbench home → click the **⊕** next to "MySQL Connections".
3. Connection Name: anything · Hostname: `127.0.0.1` · Port: `3306` · Username: `seismic` ·
   Password: **Store in Vault…** → `seismic`.
4. **Test Connection** → should succeed → **OK**.
5. Double-click the connection tile to open it.
6. Run (⚡ lightning button):
   `USE seismicmonitor; SELECT * FROM earthquake_alert; SELECT * FROM sensor_reading;`
   Or use the left **Schemas** panel → expand `seismicmonitor → Tables` → hover a table → click the
   little **grid icon** to see its rows.
7. If `seismic`/`seismic` fails, use **root** / `root` (both exist per `compose.yml`).

**Option C — terminal (quick proof):**
`docker exec -it seismic-mysql mysql -useismic -pseismic seismicmonitor -e "SELECT id, status, area, magnitude FROM earthquake_alert;"`

**Demo trick — prove it does real SQL:** `application.properties` has `spring.jpa.show-sql=true` +
`format_sql=true`, so when a sensor POST arrives, the actual `insert into ...` prints in the IntelliJ
run console. Show Hibernate generating the SQL, then refresh the table in the Database tool to show
the new row appear. (Tests use in-memory **H2** instead, which disappears when the test ends.)

### When does something *technically* become an alert — and where it shows in MySQL
Created at exactly one place: `AlertService.createAlertIfWarranted(...)`. Flow:
1. `POST /api/sensor-data` arrives with 1–3 measurements (simulator sends a random batch every 15s).
2. `SensorDataService` saves **every** reading, collects the **valid** ones (location present,
   `distance > 0`, `magnitude > 0`, `recordedAt` present).
3. An alert is born **only if** there are **exactly 3 valid readings in that one request** AND the
   trilateration math succeeds (it throws on degenerate geometry → no alert, readings still saved).
4. On success: a new `earthquake_alert` row with **status `UNDER_REVIEW`** + epicenter/magnitude/area,
   and the **3 readings get linked** to it. (Most readings never form an alert.)

Where to see it (two tables tell the story):
- `earthquake_alert` — one row per alert; a new one is `status = 'UNDER_REVIEW'`.
- `sensor_reading` — has an **`alert_id`** FK column: **filled** for the 3 readings that formed an
  alert, **NULL** for readings that didn't.
```sql
SELECT * FROM earthquake_alert;
SELECT * FROM sensor_reading WHERE alert_id IS NOT NULL;   -- readings that made alerts
SELECT * FROM sensor_reading WHERE alert_id IS NULL;       -- readings that didn't
-- clearest proof of the "exactly 3 readings -> 1 alert" rule (each alert should show 3):
SELECT a.id, a.status, a.magnitude, a.area, COUNT(r.id) AS linked_readings
FROM earthquake_alert a
LEFT JOIN sensor_reading r ON r.alert_id = a.id
GROUP BY a.id;
```
Note: an alert only becomes visible to regular **users** on the frontend once an **admin** sets it
`UNDER_REVIEW → ACTIVE` — but it *technically exists in the DB* the moment the `UNDER_REVIEW` row is written.

---

> SELF-REPORTED (2026-06-25): user is "more or less" comfortable with repositories, services,
> controllers, models, DTOs (requests/responses), and config. Still rehearse aloud, but these are
> not the priority. Priority gaps the user named: estimation, geocoding, exception (all 📖 below now).

## A. Architecture & design choices  (your strongest material — mostly ✍️)
- [ ] ✍️ Layered architecture: controller → service → repository → model; why each layer, what belongs where
- [ ] ✍️ Why entities never leave the service layer; DTOs as records + `from()` mappers
- [ ] ✍️ Single repo vs two repos (static frontend, no CORS); the `feature/cors-demo` artifact
- [ ] 📖 ✍️ ⚡ Strategy pattern / interface seam — `EpicenterEstimator`, `MagnitudeEstimator`, `ReverseGeocoder`; how you'd swap an impl "uden at koden ændres" (walked the estimation + geocoding packages this session)
- [ ] ✍️ Slim-first / defer-to-the-right-delopgave (report identity deferred D3→D5)
- [ ] ✍️ DRY: `getByIdOrThrow` default repo method; trade-off (repo imports domain exception)

## B. The core domain pipeline  (know it cold — it's the whole point)
- [ ] ⚡ End-to-end ingest flow: `SensorDataController` → `SensorDataService.ingest()` → `AlertService.createAlertIfWarranted()`
- [ ] Validity rule: lat+lon present, distance>0, magnitude>0, `recordedAt` parses
- [ ] Alert-creation logic: <3 valid → save, no alert · exactly 3 → attempt · calc fails → save, no alert
- [ ] Idempotency / dedup on `readingId`; why a full re-send yields no duplicate alert
- [ ] Only same-request measurements combine (no cross-request aggregation) — and why that's enough per the brief
- [ ] 📖 ✍️ Why geocoding failure is non-fatal (best-effort, `Optional.empty()`, area null)
- [ ] 📖 ⚡ The black-box trilateration algorithm: what it does (3 circles intersect), why it can throw, that you catch it
- [ ] ⭐ **REQUESTED walkthrough — `SensorDataService` line-by-line** (the file the user keeps open):
  `ingest()` — null guard, the loop, dedupe via `existsByReadingId`, `findOrCreateSensor`, map DTO→entity,
  save EVERY reading, collect the valid ones, then one call to `AlertService.createAlertIfWarranted`;
  plus the null-safe `latOf`/`lonOf`/`orZero` helpers and `getAllReadings()`.
  > Key precision to nail (corrected this session): batches are **1–3**, not always 3. Validity
  > (`isValid()`) and trilateration are **two separate steps** — validity counts the good readings;
  > trilateration only runs on 3 valid ones and can still fail on bad geometry (→ no alert, readings
  > still saved). One-liner to rehearse: *"Each request brings 1–3 measurements. I save them all,
  > keep the valid ones, and if a request has exactly 3 valid readings I run trilateration for the
  > epicenter + an averaging estimator for magnitude. On success I create an UNDER_REVIEW alert for
  > an admin to confirm; if the geometry can't be solved, readings still save but no alert is made."*

## C. Domain modeling & the state machine
- [ ] ✍️ ⚡ Alert state machine: legal transitions, terminal states; rule on the **entity** (`AlertStatus.canTransitionTo` → `changeStatusTo`), not the controller
- [ ] Why enforce in the domain/entity vs the service or controller (encapsulation — illegal jump impossible regardless of caller)
- [ ] Domain model relationships: Sensor 1—N SensorReading N—1 EarthquakeAlert 1—N CitizenReport
- [ ] Natural keys (`sensorId`, `readingId`) vs surrogate PK; unique `(alert, reportedBy)` constraint

## D. Spring core
- [ ] 📖 ⚡ IoC / Dependency Injection — what a bean is, constructor injection, why DI (testability, swap impls). Also covered: ApplicationContext / the container, `SpringApplication.run` returns it, interface-vs-concrete field type
- [ ] 🔲 Auto-configuration & starters — what Spring Boot does for you
- [ ] 📖 `@Component`/`@Service`/`@Repository`/`@RestController`/`@Configuration` — what each marks and why it matters (touched while explaining DI + SecurityConfig; rehearse the differences)
- [ ] 📖 `@Bean` factory methods (you have two in `SecurityConfig`) vs component scanning
- [ ] 🔲 `@Profile` — how the test profile swaps `NoopReverseGeocoder` for the Nominatim impl

## E. REST & web layer
- [ ] 🔲 What makes an API RESTful; resources, HTTP verbs, statelessness
- [ ] HTTP status codes you return: 200/201, 400, 401 vs 403, 404, 409
- [ ] 📖 ✍️ Exception → HTTP: domain exception thrown in service → `@RestControllerAdvice` `GlobalExceptionHandler` → `ProblemDetail` (walked the whole `exception/` package this session: 3 RuntimeExceptions → 404/409, why unchecked, auto-rollback)
- [ ] 🔲 `@PathVariable` / `@RequestBody` / validation (`@Valid`, `@NotNull`) — where validation runs
- [ ] Nested resource design: `/api/alerts/{id}/reports`, `/{id}/readings`, `/{id}/status`

## F. JPA / persistence
- [ ] 🔲 ⚡ Entity ↔ table mapping; `@Entity`, `@Id`, `@GeneratedValue`
- [ ] 🔲 ⚡ Relationships: `@ManyToOne` / `@OneToMany`, owning side / FK, `optional=false` vs nullable (the `SensorReading.alert` nullable fix)
- [ ] 🔲 Lazy vs eager fetching; the N+1 problem (you have a known `reportCount` N+1)
- [ ] 🔲 `@Transactional` — what the persistence context / dirty checking does (status change flushes without explicit save)
- [ ] 🔲 `ddl-auto=update` — what it does and its limits (won't retrofit new NOT NULL / unique onto an old schema)
- [ ] 🔲 Spring Data JPA derived queries (`findByStatusOrderByCreatedAtDesc`) — how method names become SQL
- [ ] ⭐ **REQUESTED walkthrough — "does this actually do SQL, or is it all just the Docker volume?"**
  Short answer to verify and rehearse: **Yes, it does real SQL.** The app talks to a **MySQL**
  database (running in the `mysql` Docker container, on `localhost:3306`). JPA/Hibernate generates
  real SQL (`INSERT`/`SELECT`/`UPDATE`) against it — so in **MySQL Workbench** you can connect and
  see the actual tables (`sensor`, `sensor_reading`, `earthquake_alert`, `citizen_report`) and rows.
  The **Docker volume (`mysql-data`)** is *not* a separate store — it's just where MySQL keeps its
  data files on disk so they survive a container restart. So it's not either/or: **MySQL is the
  database; the volume is its persistence.** (Tests are different — they use in-memory **H2**, which
  vanishes when the test ends.) To-do before exam: open Workbench, connect, run a `SELECT` to prove it.

## G. Spring Security  (walked through the whole class this session 📖)
- [ ] 📖 ⚡ Authentication vs authorization — the distinction in your own words (covered via the 401-vs-403 flow: not-logged-in vs logged-in-but-not-allowed)
- [ ] 📖 ✍️ Stateless HTTP Basic; why `SessionCreationPolicy.STATELESS`; credentials per request (TLS caveat)
- [ ] 📖 ⚡ The filter chain & URL-pattern authorization; order matters (specific before `anyRequest`)
- [ ] 📖 Why `POST /api/sensor-data` is `permitAll` but `GET /api/readings` is ADMIN (data in vs data out)
- [ ] 📖 ✍️ Why CSRF disabled (stateless token-per-request API, not session+cookie)
- [ ] 📖 ✍️ `{noop}` vs BCrypt — honest "production would hash" answer; `PasswordEncoder` role
- [ ] 📖 `UserDetailsService` / `InMemoryUserDetailsManager`; roles vs authorities (`ROLE_` prefix, `hasRole` vs `hasAuthority`)
- [ ] 🔲 ⚡ **Live-extend candidate:** add a new role/endpoint rule, or DB-backed users (`feature/db-users`)

## H. Testing
- [ ] ✍️ `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional` on H2 (why not `@DataJpaTest` — fails under Boot 4.1)
- [ ] Unit vs integration; pure-logic state-machine test (no Spring) vs full-flow ingest test
- [ ] 🔲 MockMvc + `@WithMockUser` for the security access test
- [ ] 🔲 ⚡ **Live-extend candidate:** write a new test for a given rule
- [ ] Why H2 for tests, MySQL for prod (`MODE=MySQL`); isolation & speed

## I. External integration
- [ ] 📖 `RestClient` call to Nominatim; descriptive `User-Agent`, rate limits
- [ ] 📖 ✍️ Best-effort failure; known smell: HTTP I/O inside `@Transactional` (flagged as polish)
- [ ] 📖 `area` truncated to 255 — why (VARCHAR(255), MySQL would reject longer)
- [ ] 📖 `@Profile("!test")` swaps the real Nominatim impl for an offline `NoopReverseGeocoder` in tests (keeps tests offline AND proves the provider is swappable)

## J. Frontend
- [ ] ✍️ Vanilla ES modules split by responsibility (api/render/app/map); why no SPA framework (IntelliJ-only oral)
- [ ] Same-origin → `fetch('/api/...')` with Basic auth via `btoa`; role-aware UI from `GET /api/me`
- [ ] In-place card updates (avoid full re-render / scroll-jump)
- [ ] 🔲 Known XSS smell: backend strings into `innerHTML` (post-deadline branch)
- [ ] ⭐ **REQUESTED walkthrough — how `render.js` actually works** (line by line): `buildCard`
  (creating elements + `innerHTML` + `dataset.alertId`), `buildActions` (admin buttons from the
  `NEXT_STATUSES` map, the "Felt it?" form, passing `handlers` down), `makeButton`
  (`addEventListener`), `renderReadings`/`renderReports` (`.map().join("")` → HTML string). How it
  fits with `app.js` (app fetches data → render builds DOM → app inserts it).

## K. General Java / CS (Datamatiker fundamentals — often probed)
- [ ] 🔲 ⚡ Records — what they generate, immutability, why used for DTOs & value objects (`Location`)
- [ ] 🔲 `Optional` — why `createAlertIfWarranted` returns `Optional<>` instead of null
- [ ] 🔲 Switch expressions (the exhaustive `canTransitionTo` switch)
- [ ] 🔲 Enums with behavior (methods on `AlertStatus`)
- [ ] 🔲 Interfaces & polymorphism (the strategy seams are the concrete example)
- [ ] 🔲 Collections / streams used in validity filtering & magnitude averaging
- [ ] 🔲 Checked vs unchecked exceptions (your domain exceptions extend `RuntimeException` — why)

---

## Highest-priority gaps to close (the 🔲 + ⚡ overlap)
These have NO written defense yet AND are likely probes — do these first:
1. **JPA mapping, relationships, `@Transactional`, lazy/N+1** (F) — biggest 🔲 cluster, now the top gap
2. **Records / Optional / enums-with-behavior / interfaces** (K) — Datamatiker staples, easy points
3. **REST fundamentals & where validation runs** (E)
4. **The core domain pipeline** (B) — know the ingest flow cold; it's the whole point
5. **Auto-configuration & starters, `@Profile`** (D) — the remaining Spring-core 🔲 items

> Spring DI (D) and Spring Security (G) are now 📖 (covered this session) — next pass is YOU
> explaining them back without notes, then they flip to `[x]`.

## "Extend the code live" rehearsal (⚡ — practice in IntelliJ, no notes)
- Add a new `MagnitudeEstimator` (weighted average) and wire it — proves the strategy seam
- Add an alert-status transition rule or a new role-restricted endpoint
- Write one new unit test for a stated rule
- Walk the ingest pipeline from the controller to the saved alert, file by file

---

## Session log
- **2026-06-25 — Session 1.** Covered **Spring DI** (what DI is via `AlertService`'s constructor,
  the 3 payoffs: swappability/testability/no-manual-wiring, IoC vs DI, beans, the
  ApplicationContext and how `SpringApplication.run` returns it, why fields are typed to the
  interface, constructor vs field injection). Then walked the **whole `SecurityConfig` class**
  (filter chain, CSRF off, stateless sessions, URL-pattern rules + order, public ingest vs admin
  reads, HTTP Basic, `{noop}`, `UserDetailsService`, roles/`ROLE_` prefix, 401 vs 403).
  Also: added plain-English study comments across the entire codebase + `DOCS/ARCHITECTURE.md`.
  → Marked the above 📖.
  → Logged the **examiner's pre-exam email** (see the ⭐ Exam format section at the top).
  → Flagged two **requested walkthroughs** (⭐): **`render.js` line-by-line** (section J) and
    **"real SQL vs Docker volume"** (section F).
  Next time: do the requested `render.js` walkthrough, quiz me on JPA/persistence (F), or have me
  explain DI/Security back out loud to flip them to `[x]`. Also: rehearse one full
  Frontend → Backend → DB narration and the live-coding drills (the examiner will test those).
- **2026-06-25 — Session 1 (cont.).** Walked the three packages the user flagged as gaps:
  **`estimation/`** (interfaces + Trilateration/Average impls + the 3 records, strategy seam),
  **`geocoding/`** (ReverseGeocoder + Nominatim, best-effort/`Optional`, `@Profile` test swap),
  **`exception/`** (3 RuntimeExceptions → 404/409 via `@RestControllerAdvice` + `ProblemDetail`).
  → Marked the related items 📖 across sections A/B/E/I. Also documented **how to view the live DB**
  (new 🗄️ section: IntelliJ Database tool / Workbench / terminal + the `show-sql` demo trick).
  User self-reports comfort with repo/service/controller/model/dto/config (see note under section A).
  Also clarified the alert-trigger precision (batches 1–3; validity vs trilateration are separate
  steps) and added the MySQL "when does it become an alert + where to see it" detail (🗄️ section).
  **NEXT SESSION (requested rehearsals, in order):** (1) `SensorDataService` line-by-line,
  (2) `render.js` line-by-line, then (3) quiz on JPA/persistence (F).
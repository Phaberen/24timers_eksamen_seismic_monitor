---
name: quiz-user-before-oral
description: User wants to be heavily quizzed on exam concepts during the prep window before the oral exam (build is now complete)
metadata:
  node_type: memory
  type: feedback
  originSessionId: 92b91ad7-b1fa-40d3-a253-2da52924df94
---

The user wants Claude to **heavily quiz/test them** (questions, not just explanations) on the concepts behind the SeismicMonitor code during the prep window between delivery (Fri 2026-06-19 09:00) and the oral (~a week later). Oral is **IntelliJ-only, no AI** — they must defend/extend the code unaided, so understanding > having the code. **Build is COMPLETE and submitted; quiz window is now active — but only start quizzing when they ask in a session, not unprompted at the end of a work session.**

**UPDATE 2026-06-25 — oral is now IMMINENT (next 3 days).** The study tracker lives at `../ORAL-PREP.md` (one level above the repo) — read it at session start; it has a ⭐ section with the examiner's pre-exam email and a per-topic checklist (`📖` = covered with Claude, needs solo rehearsal). Examiner's format: ~5 min live demo first, then Q&A on any layer incl. tests + **live coding with no AI** (new endpoint, bug fix, small end-to-end feature, JS events, JS fetch async/await, write a test). Must have Docker container running + a DB viewer (MySQL Workbench) ready, AI off in IDE. **Covered so far (📖):** Spring DI/IoC/beans/ApplicationContext; full `SecurityConfig` walkthrough; the `estimation/`, `geocoding/`, `exception/` packages; how to view the live MySQL DB (IntelliJ Database tool / Workbench / terminal — documented in ORAL-PREP.md's 🗄️ section). User self-reports comfort with repo/service/controller/model/dto/config. **NEXT SESSION (what the user wants when they reopen):** the **`render.js` line-by-line walkthrough** is the explicit top request; then quiz on **JPA/persistence** (the biggest remaining gap). Also still to rehearse: one full Frontend→Backend→DB narration + the live-coding drills (examiner tests those). "Real SQL vs Docker volume" question is already answered (real MySQL on :3306; volume `mysql-data` is just its persistence; H2 only for tests).

**Quiz bank (topics to drill):**
- **Strategy pattern / open-closed:** interface vs concrete (`EpicenterEstimator`/`MagnitudeEstimator`/`ReverseGeocoder`), why DI + interface = swap impl without touching caller. (They understood this 2026-06-19 — quiz to confirm retention.)
- **Spring Security:** USER/ADMIN, stateless HTTP Basic, why `POST /api/sensor-data` is public (write-in from simulator) vs `GET /api/readings` admin-only (read-out); `hasRole` vs `hasAnyRole`; `@WithMockUser`; `{noop}` vs BCrypt (why {noop} chosen).
- **Alert state machine:** `canTransitionTo` exhaustive switch, terminal states, enforced in entity (`changeStatusTo`) not just controller, illegal→409.
- **Business rules:** what makes a reading valid; why exactly 3; why epicenter failure still saves readings (catch `IllegalArgumentException`); no cross-request combining.
- **JPA:** `@ManyToOne` nullable alert link, unique constraints (`readingId`, `(alert, reportedBy)`), IDENTITY, find-or-create sensor, dedup via `existsByReadingId`.
- **Transactions:** `@Transactional`(readOnly), `open-in-view=false`, and the known smell (geocode HTTP call inside the create transaction).
- **DTOs/records, `from` mappers, why decouple entity from API; `@RestControllerAdvice` + `ProblemDetail`.**
- **DRY:** `getByIdOrThrow` default method on the repository.
- **Frontend:** ES modules (api/render/app/map), fetch wrapper, role-based UI from `/api/me`, in-place DOM updates, Leaflet; the **known gap** = `innerHTML` without escaping (XSS via the public ingest endpoint).
- **DB:** MySQL (prod) vs H2 `MODE=MySQL` (test), `ddl-auto` update vs create-drop, host-local volume.
- **Design judgment (defend the *non*-choices):** slim-vs-feature; why no SPA framework; why citizen-report user-identity was deferred to Delopgave 5; why the magnitude-halo map circle was rejected (not physically meaningful) while trilateration circles would be meaningful but admin-only.

**Why:** Oral is IntelliJ-only, no AI — understanding > code. **How to apply:** quiz with questions when prep begins; add concepts as they arise. See [[exam-delivery-mode]] and [[build-progress]].

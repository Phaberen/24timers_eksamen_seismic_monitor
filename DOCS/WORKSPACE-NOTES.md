# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this directory is

This is the **exam workspace** for a 24-hour Programmering 2 exam (Datamatiker, 3. semester, EK Guldbergsgade). It is *not* itself a git repository. It contains:

- `SeismicMonitor/` — the actual deliverable: a full-stack Spring Boot REST app, its own git repo. **All code work happens here.** It has its own `CLAUDE.md` with the authoritative build/run/test commands and architecture — read `SeismicMonitor/CLAUDE.md` before touching code.
- `PLAN.md` — the full distillation of the assignment: the 5 delopgaver, business rules, the given black-box epicenter (trilateration) algorithm, validity rules, the alert state machine, exam constraints. **The authoritative spec — read it before re-deriving any requirement.**
- `progress.md` — decisions log + per-item checklist + the **oral-exam cheat-sheet** (honest record of *why* each choice was made and what was deliberately left out/deferred). All 5 delopgaver are complete as of 2026-06-19.
- `assignment.txt` / `24-timers-seismicmonitor.pdf` — the original handed-out brief (Danish).
- `Dokument m. oplysninger.odt`/`.pdf` — the Wiseflow submission document (name, EK-email, private repo link, status).

These planning docs live one level **above** the git repo on purpose: they are a personal knowledge base and are intentionally **not part of the submission**. Don't move them into `SeismicMonitor/` or commit them there.

## Working here

- For anything involving the application — building, running, tests, the ingest pipeline, security, frontend — `cd SeismicMonitor` and follow `SeismicMonitor/CLAUDE.md`. Quick reference: `./mvnw spring-boot:run` (app on :8080, needs MySQL via `docker compose up -d mysql`), `./mvnw test` (H2, no Docker needed). Requires JDK 26.
- Edits to `PLAN.md` / `progress.md` are knowledge-base maintenance, not code. Keep `progress.md`'s oral cheat-sheet honest — it explains the reasoning behind design decisions and is what the user is graded on defending in an IntelliJ-only oral.

## Exam constraints that govern all work here

- **Nothing graded may be committed to `main` after the Fri 2026-06-19 09:00 deadline.** Grading is on what existed in `main` at the deadline; post-deadline polish must go on a new branch. (The deadline has passed — assume any further code work is post-deadline branch work unless told otherwise.)
- The user commits themselves — do not run `git commit`/`push`.
- At the oral, the user must extend the code using only IntelliJ, no AI. Solutions must be genuinely understandable and defensible, not just working. Favor the standard, well-reasoned approach over clever or over-engineered ones.
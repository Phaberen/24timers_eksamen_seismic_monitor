# DOCS — project + prep context (additions branch)

Everything needed to keep working on this project (and to prep for the oral exam) from any machine
that clones this repo. Pulled in here so the branch is self-contained.

> These are study/dev aids on the `additions` branch — **not** graded deliverables. None of it is on
> `master`. Note: pushing this repo (even a private one) puts these files on GitHub.

## Contents
- **ARCHITECTURE.md** — plain-language tour of every layer + two request walk-throughs.
- **ORAL-PREP.md** — the oral-exam study tracker: examiner's email, the per-topic checklist
  (`📖` = covered, needs solo rehearsal), the DB-viewing guide, and the session log. **Start here.**
- **PLAN.md** — the authoritative spec (the 5 delopgaver, business rules, the given epicenter
  algorithm, exam constraints). *Snapshot copy; original lived one dir above the repo.*
- **progress.md** — decisions log + the oral cheat-sheet (why each choice was made). *Snapshot copy.*
- **WORKSPACE-NOTES.md** — the parent-folder CLAUDE.md (how the exam workspace was laid out).
- **ai-memory/** — snapshot of the local Claude Code memory (working preferences + study state).
  `MEMORY.md` is the index. These are normally machine-local and don't travel with git, so they're
  copied here for continuity on another device.

## Continuing on another laptop
1. Clone the repo, check out `additions`.
2. Have JDK 26 set as `JAVA_HOME` for `mvnw` builds (see `ai-memory/jdk-26-build-setup.md`).
3. Open `DOCS/ORAL-PREP.md` and read the latest **Session log** entry — it says exactly what's next.
4. If using Claude Code there: the memory won't auto-load (it's a fresh machine), so point it at
   `DOCS/ai-memory/MEMORY.md` to restore the working context.

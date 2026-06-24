---
name: slimmest-solution-philosophy
description: Default to the slimmest solution that satisfies the current delopgave; defer later-delopgave concerns; user corrects as we go
metadata: 
  node_type: memory
  type: feedback
  originSessionId: ed401af7-3e51-47c6-949e-9596951dd094
---

The user wants the **bare-minimum** solution that satisfies the assignment, exercising concepts they already know, so it's easy to defend at the oral. Concretely:
- Don't front-load a later delopgave's requirements into an earlier one. Build only what the current delopgave asks; add the rest when its delopgave arrives.
- Prefer fewer files / less machinery when it still satisfies the task. When you propose something beyond minimum, say so and give the simpler alternative.
- The user reviews and corrects as we go — keep producing, don't stall.
- If something is generally cumbersome/awkward in the design, jot a note in memory rather than blocking.
- Always keep the **whole** assignment in view so we get through all 5 delopgaver — slim per-step, complete overall.

**Concrete decision (Delopgave 3 citizen reports):** built SLIM — `CitizenReport { id, alert, intensity, createdAt }`, no `reportedBy`, no unique constraint, no duplicate check, no `DuplicateReportException`. The user identity + "one report per user per alert" rule belong to **Delopgave 5** (where a real authenticated principal exists); H2 ddl-auto recreate makes the later retrofit cheap. Alert-not-found in the report flow REUSES `AlertNotFoundException`.

**User did want to KEEP the exceptions package** (custom domain exceptions + @RestControllerAdvice mapping) for separation of concerns — that stays. See [[exam-delivery-mode]] and [[delivery-brevity-and-comments]].

---
name: no-claude-traces-copy-paste-workflow
description: "SUPERSEDED — Claude now edits project files directly; copy-paste-only workflow no longer applies. Git boundary still in force."
metadata:
  node_type: memory
  type: feedback
  originSessionId: 31e368a0-86be-4931-8c86-ae4a405e754f
---

**SUPERSEDED 2026-06-18.** The user explicitly overruled the copy-paste-only workflow: "YOU CODE THE CHANGES. YOU MAKE THE CHANGES TO PROJECT; I WILL NOT COPY ANYMORE MYSELF." Frustration had built up from delivering paste-blocks. **Claude now uses Write/Edit on project source files directly.**

Current workflow:
- **Edit project files directly** with Write/Edit. No more copy-paste blocks.
- After a change, give a brief note of what changed (see [[delivery-brevity-and-comments]] — 1-2 lines, plain comments, no exam framing). The user reviews/confirms in their IDE.
- **Still DO NOT run git** (no commit/staging/push). The user commits manually under their own name; never add `Co-Authored-By` or any AI marker. (This boundary was NOT lifted — keep it unless the user says otherwise.)
- Reading project files and the assignment yourself is expected — see [[read-assignment-and-files]].
- Running Docker / MySQL / the app / tests: the user has been doing this themselves; ask before assuming, but editing files is now yours.

**Still relevant guidance that carried over:** [[slimmest-solution-philosophy]] (bare-minimum per delopgave), [[delivery-brevity-and-comments]], [[exam-delivery-mode]]. The "one file at a time / explain method-by-method / ask-don't-look" rules from the old workflow are all retired.

**Why the original rule existed:** exam integrity + oral defense (repo should read as the user's own work, and they must understand every line for the IntelliJ-only oral). The user has decided direct edits are acceptable for their workflow now.

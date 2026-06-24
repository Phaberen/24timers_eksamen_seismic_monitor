---
name: read-assignment-and-files
description: "User wants Claude to READ the assignment + project files itself before asking — overrides the old ask-don't-look clause"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: ed401af7-3e51-47c6-949e-9596951dd094
---

The user corrected the "never look for context yourself — ASK and the user pastes it" clause of [[no-claude-traces-copy-paste-workflow]]. Twice: "look at the assignment always before asking such questions" and "no you need to be able to read the file."

**How to apply:**
- ALWAYS read the assignment before asking scope/requirements questions. Don't ask the user something the brief already answers (e.g. "active = ACTIVE only" is stated explicitly in the brief).
- Reading project source files to extend them correctly is fine — read them yourself instead of asking the user to paste. The cost concern is overruled.
- The assignment PDF: `C:\Programmering\3.Semester\24timers_eksamen\24-timers-seismicmonitor.pdf`. Extracted text at `...\assignment.txt`. To re-extract: `"/c/Program Files/Git/mingw64/bin/pdftotext.exe" -layout <pdf> <txt>` (pdftotext ships with Git; not on default bash PATH).

**Why:** Asking for things the brief/code already contains wasted the user's time and frustrated them.

**Still in force from [[no-claude-traces-copy-paste-workflow]]:** do NOT Write/Edit project source, do NOT run git/docker/tests. Deliver code as paste-ready blocks, one file at a time, explained method-by-method. Only the "don't read" part is relaxed.

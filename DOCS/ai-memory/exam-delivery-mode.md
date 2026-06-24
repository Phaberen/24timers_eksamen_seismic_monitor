---
name: exam-delivery-mode
description: "Working style for the 24h exam — brief explanations, strictly in scope, standard good-practice solutions, no over-engineering"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 92b91ad7-b1fa-40d3-a253-2da52924df94
---

For the 24-hour SeismicMonitor exam build, the user wants speed + a defensible product, not exhaustive teaching:

- **Brief** explanations — just enough to understand and defend at the oral. No long essays unless asked.
- **Strictly in scope.** Build only what the assignment/delopgave asks. Do NOT add fields, methods, or behavior the spec doesn't require (e.g. the invented `updateLocation` on Sensor was caught and removed).
- **Standard, acknowledged good-practice solutions** that fit the stack (Spring Boot 4.1 + MySQL + plain JS/HTML/CSS), each with short reasoning so it's the "intuitive correct" choice.
- The user does deep concept/alternatives prep themselves in the 6 days between delivery (Fri 2026-06-19) and the oral exam. Claude's job during the 24h is a correct, in-scope, defensible product fast.

**Why:** Only 24h to deliver; over-engineering and long explanations waste the budget. Grading rewards features wired JPA→backend→frontend with working buttons over breadth.

**How to apply:** When tempted to add robustness/flexibility beyond the spec, stop and confirm it's required. Default to the simplest correct design. See [[no-claude-traces-copy-paste-workflow]].

**Code comment style:** Keep comments professional and brief — explain WHAT a snippet is for / why it exists, never narrate HOW it works (line-by-line how-comments are bad convention). NEVER reference exam meta-language in code (no "Delopgave 2", "this part of the assignment", etc.) — comments must read like normal production code. Exam/delopgave framing belongs only in the chat prose, not in delivered code.

**Delivery format nuance:** For a COMPLETE file the user will paste, give ONE single code block — the whole file, no prose interleaved between code sections. Any explanation goes either before or after the block, not inside it. If something must be noted inline, write it as a real pastable Java comment (`// ...`), never as markdown prose between snippets. (The code-alongside-explanation method-by-method style is for walking through / teaching a file; the single-block style is for handing over a finished file to paste.)

---
name: comment-style-plain
description: "Educational code comments must be plain-English // comments, no Javadoc markup"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 0658a167-9b09-464c-91a8-6318b8e7d487
---

When adding educational/explanatory code comments, keep them simple and layman-ish. Use plain `//` comments in everyday language.

**Why:** User rejected Javadoc-style blocks with `{@code ...}`, `<p>`, and jargon like "dirty-checking/flushing" — too formal/technical for their study comments.

**How to apply:** No `/** */` Javadoc with `{@code}`/`<p>`/HTML tags. Explain terms in plain words (e.g. "readOnly = true just tells Spring we're only reading, not changing anything"). Short, clear, professional — never exam-framed. See [[delivery-brevity-and-comments]]. Context: the `additions` branch educational-commenting pass over the whole project.

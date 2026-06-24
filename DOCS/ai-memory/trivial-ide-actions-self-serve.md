---
name: trivial-ide-actions-self-serve
description: "User handles trivial IDE/filesystem housekeeping themselves; don't spend tool calls on it"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 31e368a0-86be-4931-8c86-ae4a405e754f
---

For trivial IDE/filesystem housekeeping — deleting leftover empty packages/dirs after a refactor, renames, and similar one-click IntelliJ actions — the user does these themselves.

**Why:** Not worth spending tokens/turns on; the user is at the keyboard in IntelliJ and these are faster for them to do directly. Also reinforces their own IntelliJ fluency for the oral exam.

**How to apply:** Don't run cleanup commands for empty dirs or other trivial housekeeping. Just point out what needs doing in one line and let the user do it. Focus tool calls on substantive work (code, config, builds, tests).

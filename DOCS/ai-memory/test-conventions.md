---
name: test-conventions
description: "Test classes: feature-named (not DelopgaveNTest), match AlertCreationTest style, keep few + clean"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: ed401af7-3e51-47c6-949e-9596951dd094
---

Test classes follow the existing `AlertCreationTest` pattern exactly:
- `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional`, AssertJ (`assertThat`), field injection (`@Autowired SomeService x;`).
- Set up state through the **real flow** where practical (e.g. `sensorDataService.ingest(...)` with a `reading(...)` helper) rather than hand-building entities.
- **Name by feature, NOT by delopgave.** e.g. `CitizenReportTest`, `AlertCreationTest` — never `Delopgave3Test`. Naming a test after a delopgave annoyed the user ("hallucinating, not listening").
- Keep it **minimal and clean**: a few focused tests covering the most basic functionality, not exhaustive coverage. See [[slimmest-solution-philosophy]].

**Why:** matches the repo's existing convention and the user's slim, defend-it-at-the-oral preference.

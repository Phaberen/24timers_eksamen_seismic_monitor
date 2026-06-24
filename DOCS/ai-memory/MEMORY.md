# Memory Index

- [JDK 26 build setup](jdk-26-build-setup.md) — user wants latest Java; export JAVA_HOME to openjdk-26.0.1 for terminal mvnw builds (system default is 21)
- [Trivial IDE actions self-serve](trivial-ide-actions-self-serve.md) — user deletes empty packages / does trivial IDE housekeeping themselves; don't spend tool calls on it
- [No Claude traces / copy-paste workflow](no-claude-traces-copy-paste-workflow.md) — SUPERSEDED: Claude now edits project files directly; copy-paste retired. Still DO NOT run git (user commits themselves)
- [Exam delivery mode](exam-delivery-mode.md) — brief explanations, strictly in scope, standard good-practice solutions, no over-engineering
- [Use @SpringBootTest not @DataJpaTest](use-springboottest-not-datajpatest.md) — @DataJpaTest fails to load context in this Boot 4.1 project; use @SpringBootTest + @ActiveProfiles("test") + @Transactional
- [Quiz user before oral](quiz-user-before-oral.md) — heavy quizzing before IntelliJ-only oral (now IMMINENT, next 3 days from 2026-06-25); study tracker at ../ORAL-PREP.md w/ examiner email + checklist
- [Build progress](build-progress.md) — Delopgave 1 done+tested; Delopgave 2 in progress, next = file 14 (wire SensorDataService→AlertService); divergences from PLAN noted
- [Read assignment and files](read-assignment-and-files.md) — READ the brief + project files yourself before asking; pdftotext at Git bin; ask-don't-look clause overruled (no-edit still in force)
- [Delivery brevity and comments](delivery-brevity-and-comments.md) — per-snippet explanation = 1-2 lines, user asks if more; code comments plain/professional, never exam-framed
- [Slimmest solution philosophy](slimmest-solution-philosophy.md) — build bare-minimum for the current delopgave; defer later-delopgave concerns (citizen reports built slim, user/uniqueness → Delopgave 5)
- [Test conventions](test-conventions.md) — feature-named test classes (never DelopgaveNTest), match AlertCreationTest style, keep few + clean
- [Frontend static structure](frontend-static-structure.md) — JS in static/js/ as ES modules (api/render/app); index.html + styles.css at static root; no framework SPA
- [Plain comment style](comment-style-plain.md) — educational comments must be plain // English, no Javadoc {@code}/<p>/jargon; for the `additions` branch commenting pass

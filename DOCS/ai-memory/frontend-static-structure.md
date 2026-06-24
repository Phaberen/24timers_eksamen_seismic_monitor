---
name: frontend-static-structure
description: "Frontend layout: JS in static/js/, index.html + styles.css at static root; ES modules"
metadata: 
  node_type: memory
  type: project
  originSessionId: ed401af7-3e51-47c6-949e-9596951dd094
---

Frontend lives in `src/main/resources/static/`:
- **`index.html`** and **`styles.css`** stay at the **static root**.
- **All JS goes in a `Js/` subfolder** (note: capital J as the user actually named it) — `static/Js/app.js`, `api.js`, `render.js`, `map.js`.
- `map.js` = Leaflet epicenter map (4th module). Leaflet loads from CDN `<script>`/`<link>` in index.html (global `L`); needs internet. `#map` div lives inside `#app`; `initMap()` runs after login (container visible) and `showAlerts()` refreshes markers on every load, respecting the active/all filter.
- `index.html` references the entry module as `<script type="module" src="Js/app.js">` (folder casing and src casing must match; they do).
- JS is split by responsibility as native **ES modules** (no bundler): `api.js` = fetch wrapper, `render.js` = pure DOM building (actions delegated via a handlers object), `app.js` = orchestration/state/event wiring + in-place card updates. The user values this separation as good convention (do NOT suggest a framework SPA — over-engineering; see [[slimmest-solution-philosophy]]).
- Import paths between JS modules are relative to the importing file (`./api.js`), so they're unaffected by which folder the js/ package sits in — only `index.html`'s `src` needs the `js/` prefix.
- ES modules only load over http:// (served by Spring at :8080), not file://.

The user moves files around themselves (trivial IDE housekeeping — see [[trivial-ide-actions-self-serve]]).

// FRONTEND (render module) — builds the HTML pieces (cards, lists). No network calls here; it just
// turns data into elements/markup. app.js fetches the data and passes it in.

// For each status, which buttons an admin should see (target status + button label).
// Mirrors the backend's allowed transitions; empty list = a final status with no further moves.
const NEXT_STATUSES = {
    UNDER_REVIEW: [["ACTIVE", "Mark active"], ["FALSE_ALARM", "False alarm"]],
    ACTIVE: [["NOT_ACTIVE", "Mark not active"]],
    FALSE_ALARM: [],
    NOT_ACTIVE: []
};

// Build the whole card element for one alert: a header, a line of details, the action buttons,
// and an (initially hidden) panel that readings/reports get loaded into.
export function buildCard(alert, reportCount, handlers, isAdmin) {
    const card = document.createElement("div");
    card.className = "alert-card";
    card.dataset.alertId = alert.id;
    card.innerHTML = `
        <div class="alert-head">
            <strong>Alert #${alert.id}</strong>
            <span class="badge ${alert.status}">${alert.status}</span>
        </div>
        <p class="alert-meta">
            Area: ${alert.area || "Unknown"}
            &nbsp;|&nbsp; Epicenter: ${alert.epicenterLatitude.toFixed(4)}, ${alert.epicenterLongitude.toFixed(4)}
            &nbsp;|&nbsp; Magnitude: ${alert.magnitude.toFixed(2)}
            &nbsp;|&nbsp; Reports: <span class="report-count">${reportCount}</span>
        </p>`;

    card.appendChild(buildActions(alert, handlers, isAdmin));
    const panel = document.createElement("div");
    panel.className = "panel";
    panel.style.display = "none";
    card.appendChild(panel);
    return card;
}

// Build the row of buttons for a card. Admins get status + readings/reports buttons; everyone gets
// the "Felt it?" report form.
function buildActions(alert, handlers, isAdmin) {
    const actions = document.createElement("div");
    actions.className = "actions";

    if (isAdmin) {
        // One status button per allowed transition for this alert's current status.
        for (const [target, label] of NEXT_STATUSES[alert.status]) {
            actions.appendChild(makeButton(label, () => handlers.changeStatus(alert.id, target)));
        }
        actions.appendChild(makeButton("Readings", () => handlers.showReadings(alert.id)));
        actions.appendChild(makeButton("Reports", () => handlers.showReports(alert.id)));
    }

    const form = document.createElement("span");
    form.className = "report-form";
    const intensity = document.createElement("input");
    intensity.type = "number";
    intensity.min = "1";
    intensity.max = "10";
    intensity.placeholder = "1-10";
    form.appendChild(document.createTextNode("Felt it? "));
    form.appendChild(intensity);
    form.appendChild(makeButton("Report", () => handlers.submitReport(alert.id, intensity.value)));
    actions.appendChild(form);

    return actions;
}

// Small helper: make a button with a label that runs the given function when clicked.
function makeButton(label, handler) {
    const button = document.createElement("button");
    button.textContent = label;
    button.addEventListener("click", handler);
    return button;
}

// Build the HTML list of readings shown in a card's panel (or a message if there are none).
export function renderReadings(readings) {
    if (readings.length === 0) return `<span class="empty">No readings linked.</span>`;
    const items = readings.map(r =>
        `<li>${r.readingId} (${r.sensorId}) — magnitude ${r.estimatedMagnitude.toFixed(1)}, ${r.estimatedDistanceToEpicenterKm.toFixed(1)} km</li>`).join("");
    return `<strong>Readings behind this alert</strong><ul>${items}</ul>`;
}

// Build the HTML list of citizen reports shown in a card's panel.
export function renderReports(reports) {
    if (reports.length === 0) return `<span class="empty">No citizen reports yet.</span>`;
    const items = reports.map(r =>
        `<li>Intensity ${r.intensity} — ${new Date(r.createdAt).toLocaleString()}</li>`).join("");
    return `<strong>Citizen reports</strong><ul>${items}</ul>`;
}

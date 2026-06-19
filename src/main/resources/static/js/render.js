// Pure rendering: turns alert/report/reading data into DOM. No network calls and
// no app state; user actions are delegated through the passed-in handlers.

// Which status transitions an admin may trigger from the current status.
const NEXT_STATUSES = {
    UNDER_REVIEW: [["ACTIVE", "Mark active"], ["FALSE_ALARM", "False alarm"]],
    ACTIVE: [["NOT_ACTIVE", "Mark not active"]],
    FALSE_ALARM: [],
    NOT_ACTIVE: []
};

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

function buildActions(alert, handlers, isAdmin) {
    const actions = document.createElement("div");
    actions.className = "actions";

    // Admin-only: change status and inspect the alert's reports/readings.
    if (isAdmin) {
        for (const [target, label] of NEXT_STATUSES[alert.status]) {
            actions.appendChild(makeButton(label, () => handlers.changeStatus(alert.id, target)));
        }
        actions.appendChild(makeButton("Readings", () => handlers.showReadings(alert.id)));
        actions.appendChild(makeButton("Reports", () => handlers.showReports(alert.id)));
    }

    // Citizen report form: intensity 1-10 + submit.
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

function makeButton(label, handler) {
    const button = document.createElement("button");
    button.textContent = label;
    button.addEventListener("click", handler);
    return button;
}

export function renderReadings(readings) {
    if (readings.length === 0) return `<span class="empty">No readings linked.</span>`;
    const items = readings.map(r =>
        `<li>${r.readingId} (${r.sensorId}) — magnitude ${r.estimatedMagnitude.toFixed(1)}, ${r.estimatedDistanceToEpicenterKm.toFixed(1)} km</li>`).join("");
    return `<strong>Readings behind this alert</strong><ul>${items}</ul>`;
}

export function renderReports(reports) {
    if (reports.length === 0) return `<span class="empty">No citizen reports yet.</span>`;
    const items = reports.map(r =>
        `<li>Intensity ${r.intensity} — ${new Date(r.createdAt).toLocaleString()}</li>`).join("");
    return `<strong>Citizen reports</strong><ul>${items}</ul>`;
}
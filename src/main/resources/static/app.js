const alertsEl = document.getElementById("alerts");
const messageEl = document.getElementById("status-message");

// Which status transitions an admin may trigger from the current status.
const NEXT_STATUSES = {
    UNDER_REVIEW: [["ACTIVE", "Mark active"], ["FALSE_ALARM", "False alarm"]],
    ACTIVE: [["NOT_ACTIVE", "Mark not active"]],
    FALSE_ALARM: [],
    NOT_ACTIVE: []
};

let showActiveOnly = false;

document.getElementById("show-all").addEventListener("click", () => switchView(false));
document.getElementById("show-active").addEventListener("click", () => switchView(true));
document.getElementById("refresh").addEventListener("click", loadAlerts);

function switchView(activeOnly) {
    showActiveOnly = activeOnly;
    document.getElementById("show-all").classList.toggle("active", !activeOnly);
    document.getElementById("show-active").classList.toggle("active", activeOnly);
    loadAlerts();
}

// Small fetch wrapper that surfaces backend errors in the status bar.
async function api(path, options) {
    const response = await fetch(path, options);
    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        throw new Error(problem.detail || `Request failed (${response.status})`);
    }
    return response.status === 204 ? null : response.json();
}

function showError(error) {
    messageEl.textContent = error.message;
}

async function loadAlerts() {
    messageEl.textContent = "";
    try {
        const alerts = await api(showActiveOnly ? "/api/alerts/active" : "/api/alerts");
        alertsEl.innerHTML = "";
        if (alerts.length === 0) {
            alertsEl.innerHTML = `<p class="empty">No alerts to show.</p>`;
            return;
        }
        for (const alert of alerts) {
            alertsEl.appendChild(await renderAlert(alert));
        }
    } catch (error) {
        showError(error);
    }
}

async function renderAlert(alert) {
    const count = await api(`/api/alerts/${alert.id}/reports/count`);

    const card = document.createElement("div");
    card.className = "alert-card";
    card.dataset.alertId = alert.id;
    card.innerHTML = `
        <div class="alert-head">
            <strong>Alert #${alert.id}</strong>
            <span class="badge ${alert.status}">${alert.status}</span>
        </div>
        <p class="alert-meta">
            Epicenter: ${alert.epicenterLatitude.toFixed(4)}, ${alert.epicenterLongitude.toFixed(4)}
            &nbsp;|&nbsp; Magnitude: ${alert.magnitude.toFixed(2)}
            &nbsp;|&nbsp; Reports: ${count}
        </p>`;

    card.appendChild(buildActions(alert));
    const panel = document.createElement("div");
    panel.className = "panel";
    panel.style.display = "none";
    card.appendChild(panel);
    return card;
}

function buildActions(alert) {
    const actions = document.createElement("div");
    actions.className = "actions";

    // Status-change buttons allowed from the current status.
    for (const [target, label] of NEXT_STATUSES[alert.status]) {
        const button = document.createElement("button");
        button.textContent = label;
        button.addEventListener("click", () => changeStatus(alert.id, target));
        actions.appendChild(button);
    }

    actions.appendChild(makeButton("Readings", () =>
        togglePanel(alert.id, `/api/alerts/${alert.id}/readings`, renderReadings)));
    actions.appendChild(makeButton("Reports", () =>
        togglePanel(alert.id, `/api/alerts/${alert.id}/reports`, renderReports)));

    // Citizen report form: intensity 1-10 + submit.
    const intensity = document.createElement("input");
    intensity.type = "number";
    intensity.min = "1";
    intensity.max = "10";
    intensity.placeholder = "1-10";
    actions.appendChild(intensity);
    actions.appendChild(makeButton("Report felt", () => submitReport(alert.id, intensity.value)));

    return actions;
}

function makeButton(label, handler) {
    const button = document.createElement("button");
    button.textContent = label;
    button.addEventListener("click", handler);
    return button;
}

async function changeStatus(alertId, target) {
    messageEl.textContent = "";
    try {
        await api(`/api/alerts/${alertId}/status`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status: target })
        });
        loadAlerts();
    } catch (error) {
        showError(error);
    }
}

async function submitReport(alertId, value) {
    messageEl.textContent = "";
    const intensity = Number(value);
    if (!Number.isInteger(intensity) || intensity < 1 || intensity > 10) {
        showError(new Error("Intensity must be a whole number from 1 to 10."));
        return;
    }
    try {
        await api(`/api/alerts/${alertId}/reports`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ intensity })
        });
        loadAlerts();
    } catch (error) {
        showError(error);
    }
}

// Opens (or refreshes) the detail panel under an alert with fetched data.
async function togglePanel(alertId, path, renderer) {
    messageEl.textContent = "";
    const card = alertsEl.querySelector(`.alert-card[data-alert-id="${alertId}"]`);
    const target = card.querySelector(".panel");
    try {
        const data = await api(path);
        target.innerHTML = renderer(data);
        target.style.display = "block";
    } catch (error) {
        showError(error);
    }
}

function renderReadings(readings) {
    if (readings.length === 0) return `<span class="empty">No readings linked.</span>`;
    const items = readings.map(r =>
        `<li>${r.readingId} (${r.sensorId}) — magnitude ${r.estimatedMagnitude.toFixed(1)}, ${r.estimatedDistanceToEpicenterKm.toFixed(1)} km</li>`).join("");
    return `<strong>Readings behind this alert</strong><ul>${items}</ul>`;
}

function renderReports(reports) {
    if (reports.length === 0) return `<span class="empty">No citizen reports yet.</span>`;
    const items = reports.map(r =>
        `<li>Intensity ${r.intensity} — ${new Date(r.createdAt).toLocaleString()}</li>`).join("");
    return `<strong>Citizen reports</strong><ul>${items}</ul>`;
}

loadAlerts();

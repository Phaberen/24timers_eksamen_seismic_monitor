// FRONTEND (app module) — the "orchestrator". It wires up buttons, handles login, loads alerts,
// and reacts to clicks. It leans on the other modules: api.js (network), render.js (building the
// HTML), map.js (the Leaflet map).
import { api, setAuth } from "./api.js";
import { buildCard, renderReadings, renderReports } from "./render.js";
import { initMap, showAlerts } from "./map.js";

// Grab the page elements we need to read from or update (looked up once, by their HTML id).
const alertsEl = document.getElementById("alerts");
const messageEl = document.getElementById("status-message");
const appEl = document.getElementById("app");
const whoEl = document.getElementById("who");
const usernameEl = document.getElementById("username");
const passwordEl = document.getElementById("password");
const loginEl = document.getElementById("login");
const logoutEl = document.getElementById("logout");
const showAllEl = document.getElementById("show-all");

let showActiveOnly = false;   // which tab we're on: active-only vs all alerts
let currentUser = null;       // the logged-in user (name + admin flag), or null

// The actions we pass down to each card's buttons, so render.js doesn't need to know the details.
const handlers = {
    changeStatus,
    submitReport,
    showReadings: (id) => togglePanel(id, `/api/alerts/${id}/readings`, renderReadings),
    showReports: (id) => togglePanel(id, `/api/alerts/${id}/reports`, renderReports)
};

// Connect buttons/inputs to functions ("when this is clicked, run that").
loginEl.addEventListener("click", login);
logoutEl.addEventListener("click", logout);

// Let the user press Enter in the username/password fields to log in.
for (const field of [usernameEl, passwordEl]) {
    field.addEventListener("keydown", (event) => {
        if (event.key === "Enter") login();
    });
}
showAllEl.addEventListener("click", () => switchView(false));
document.getElementById("show-active").addEventListener("click", () => switchView(true));
document.getElementById("refresh").addEventListener("click", loadAlerts);

// Show a problem message to the user.
function showError(error) {
    messageEl.textContent = error.message;
}

async function login() {
    messageEl.textContent = "";
    // Build the Basic auth header: base64 of "username:password" (btoa does the base64 encoding).
    setAuth("Basic " + btoa(`${usernameEl.value}:${passwordEl.value}`));
    try {
        // Ask the backend who we are. If this succeeds, the credentials were valid.
        currentUser = await api("/api/me");
        onLoggedIn();
    } catch (error) {
        // Bad credentials: clear the header and show the error.
        setAuth(null);
        currentUser = null;
        showError(new Error("Login failed: " + error.message));
    }
}

// Clear the login and reset the page back to the logged-out state.
function logout() {
    setAuth(null);
    currentUser = null;
    passwordEl.value = "";
    appEl.hidden = true;
    whoEl.textContent = "";
    logoutEl.hidden = true;
    loginEl.hidden = false;
    usernameEl.hidden = false;
    passwordEl.hidden = false;
    alertsEl.innerHTML = "";
    messageEl.textContent = "";
}

// Shown after a successful login: swap the login bar for the app, and tailor the UI to the role.
function onLoggedIn() {
    whoEl.textContent = `${currentUser.username} (${currentUser.admin ? "ADMIN" : "USER"})`;
    loginEl.hidden = true;
    usernameEl.hidden = true;
    passwordEl.hidden = true;
    logoutEl.hidden = false;
    appEl.hidden = false;

    initMap();

    // Only admins get the "All" tab. Regular users start on (and are limited to) active alerts.
    showAllEl.hidden = !currentUser.admin;
    switchView(!currentUser.admin);
}

// Switch between the "Active" and "All" tabs, highlight the chosen one, and reload the list.
function switchView(activeOnly) {
    showActiveOnly = activeOnly;
    showAllEl.classList.toggle("active", !activeOnly);
    document.getElementById("show-active").classList.toggle("active", activeOnly);
    loadAlerts();
}

// Find an alert's card element on the page by its id.
function findCard(alertId) {
    return alertsEl.querySelector(`.alert-card[data-alert-id="${alertId}"]`);
}

// Build one alert card, fetching its report count first.
async function cardFor(alert) {
    const reportCount = await api(`/api/alerts/${alert.id}/reports/count`);
    return buildCard(alert, reportCount, handlers, currentUser.admin);
}

// Fetch the alerts for the current tab and (re)draw the list and the map.
async function loadAlerts() {
    messageEl.textContent = "";
    try {
        const alerts = await api(showActiveOnly ? "/api/alerts/active" : "/api/alerts");
        showAlerts(alerts);          // update the map markers
        alertsEl.innerHTML = "";     // clear the old list
        if (alerts.length === 0) {
            alertsEl.innerHTML = `<p class="empty">No alerts to show.</p>`;
            return;
        }
        for (const alert of alerts) {
            alertsEl.appendChild(await cardFor(alert));
        }
    } catch (error) {
        showError(error);
    }
}

// After a status change, swap just that one card instead of redrawing everything (avoids the page
// jumping). If we're on the active-only tab and it's no longer active, remove the card instead.
async function replaceCard(alertId, updatedAlert) {
    const oldCard = findCard(alertId);
    if (!oldCard) return;
    if (showActiveOnly && updatedAlert.status !== "ACTIVE") {
        oldCard.remove();
        if (!alertsEl.querySelector(".alert-card")) {
            alertsEl.innerHTML = `<p class="empty">No alerts to show.</p>`;
        }
        return;
    }
    oldCard.replaceWith(await cardFor(updatedAlert));
}

// Admin action: PATCH the alert's status, then refresh just that card.
async function changeStatus(alertId, target) {
    messageEl.textContent = "";
    try {
        const updated = await api(`/api/alerts/${alertId}/status`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status: target })
        });
        await replaceCard(alertId, updated);
    } catch (error) {
        showError(error);
    }
}

// File a citizen report. Check the 1–10 range here for a quick message; the backend checks it too.
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
        await refreshReports(alertId);
    } catch (error) {
        showError(error);
    }
}

// After filing a report, update the count on the card and, if the reports panel is open, its list.
async function refreshReports(alertId) {
    const card = findCard(alertId);
    if (!card) return;
    card.querySelector(".report-count").textContent =
        await api(`/api/alerts/${alertId}/reports/count`);

    const panel = card.querySelector(".panel");
    if (panel && panel.style.display !== "none") {
        const reports = await api(`/api/alerts/${alertId}/reports`);
        panel.innerHTML = renderReports(reports);
    }
}

// Open the expandable panel under a card and fill it using the given renderer (readings or reports).
async function togglePanel(alertId, path, renderer) {
    messageEl.textContent = "";
    const panel = findCard(alertId).querySelector(".panel");
    try {
        const data = await api(path);
        panel.innerHTML = renderer(data);
        panel.style.display = "block";
    } catch (error) {
        showError(error);
    }
}

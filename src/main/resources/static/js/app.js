import { api, setAuth } from "./api.js";
import { buildCard, renderReadings, renderReports } from "./render.js";
import { initMap, showAlerts } from "./map.js";

const alertsEl = document.getElementById("alerts");
const messageEl = document.getElementById("status-message");
const appEl = document.getElementById("app");
const whoEl = document.getElementById("who");
const usernameEl = document.getElementById("username");
const passwordEl = document.getElementById("password");
const loginEl = document.getElementById("login");
const logoutEl = document.getElementById("logout");
const showAllEl = document.getElementById("show-all");

let showActiveOnly = false;
let currentUser = null;

const handlers = {
    changeStatus,
    submitReport,
    showReadings: (id) => togglePanel(id, `/api/alerts/${id}/readings`, renderReadings),
    showReports: (id) => togglePanel(id, `/api/alerts/${id}/reports`, renderReports)
};

loginEl.addEventListener("click", login);
logoutEl.addEventListener("click", logout);

for (const field of [usernameEl, passwordEl]) {
    field.addEventListener("keydown", (event) => {
        if (event.key === "Enter") login();
    });
}
showAllEl.addEventListener("click", () => switchView(false));
document.getElementById("show-active").addEventListener("click", () => switchView(true));
document.getElementById("refresh").addEventListener("click", loadAlerts);

function showError(error) {
    messageEl.textContent = error.message;
}

async function login() {
    messageEl.textContent = "";
    setAuth("Basic " + btoa(`${usernameEl.value}:${passwordEl.value}`));
    try {
        currentUser = await api("/api/me");
        onLoggedIn();
    } catch (error) {
        setAuth(null);
        currentUser = null;
        showError(new Error("Login failed: " + error.message));
    }
}

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

function onLoggedIn() {
    whoEl.textContent = `${currentUser.username} (${currentUser.admin ? "ADMIN" : "USER"})`;
    loginEl.hidden = true;
    usernameEl.hidden = true;
    passwordEl.hidden = true;
    logoutEl.hidden = false;
    appEl.hidden = false;

    initMap();

    showAllEl.hidden = !currentUser.admin;
    switchView(!currentUser.admin);
}

function switchView(activeOnly) {
    showActiveOnly = activeOnly;
    showAllEl.classList.toggle("active", !activeOnly);
    document.getElementById("show-active").classList.toggle("active", activeOnly);
    loadAlerts();
}

function findCard(alertId) {
    return alertsEl.querySelector(`.alert-card[data-alert-id="${alertId}"]`);
}

async function cardFor(alert) {
    const reportCount = await api(`/api/alerts/${alert.id}/reports/count`);
    return buildCard(alert, reportCount, handlers, currentUser.admin);
}

async function loadAlerts() {
    messageEl.textContent = "";
    try {
        const alerts = await api(showActiveOnly ? "/api/alerts/active" : "/api/alerts");
        showAlerts(alerts);
        alertsEl.innerHTML = "";
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

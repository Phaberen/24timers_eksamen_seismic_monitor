// FRONTEND (map module) — the Leaflet map of alert epicenters. "L" is the Leaflet library, loaded
// from a CDN in index.html.

let map = null;          // the map, created once
let markerLayer = null;  // a group holding the pins, so we can clear them all at once

// Create the map the first time. Starts zoomed out on the whole world with a dark map background.
export function initMap() {
    if (map) return; // already set up
    map = L.map("map").setView([20, 0], 2);
    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap contributors © CARTO",
        maxZoom: 19
    }).addTo(map);
    markerLayer = L.layerGroup().addTo(map);
}

// Redraw the pins for the given alerts (called whenever the alert list changes).
export function showAlerts(alerts) {
    if (!map) return;
    map.invalidateSize();      // fix sizing in case the map was hidden when created
    markerLayer.clearLayers(); // remove the old pins

    const points = [];
    for (const alert of alerts) {
        const latlng = [alert.epicenterLatitude, alert.epicenterLongitude];
        points.push(latlng);
        // Drop a pin at the epicenter with a popup showing the key facts.
        L.marker(latlng)
            .bindPopup(
                `<strong>Alert #${alert.id}</strong><br>` +
                `${alert.status}<br>` +
                `${alert.area || "Unknown area"}<br>` +
                `Magnitude ${alert.magnitude.toFixed(2)}`)
            .addTo(markerLayer);
    }

    // Zoom/pan so all the pins fit on screen.
    if (points.length > 0) {
        map.fitBounds(points, { padding: [30, 30], maxZoom: 8 });
    }
}

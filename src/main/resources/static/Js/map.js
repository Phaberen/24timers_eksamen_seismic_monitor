// Leaflet map of alert epicenters. Leaflet is loaded globally (as `L`) from a CDN
// <script> in index.html, so this module just uses the global.

let map = null;
let markerLayer = null;

// Builds the map once. Must run after its container is visible and sized.
export function initMap() {
    if (map) return;
    map = L.map("map").setView([20, 0], 2);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap contributors",
        maxZoom: 19
    }).addTo(map);
    markerLayer = L.layerGroup().addTo(map);
}

// Replaces all markers with one per alert and fits the view to them.
export function showAlerts(alerts) {
    if (!map) return;
    map.invalidateSize(); // container may have just become visible
    markerLayer.clearLayers();

    const points = [];
    for (const alert of alerts) {
        const latlng = [alert.epicenterLatitude, alert.epicenterLongitude];
        points.push(latlng);
        L.marker(latlng)
            .bindPopup(
                `<strong>Alert #${alert.id}</strong><br>` +
                `${alert.status}<br>` +
                `${alert.area || "Unknown area"}<br>` +
                `Magnitude ${alert.magnitude.toFixed(2)}`)
            .addTo(markerLayer);
    }

    if (points.length > 0) {
        map.fitBounds(points, { padding: [30, 30], maxZoom: 8 });
    }
}

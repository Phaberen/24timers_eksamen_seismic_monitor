let map = null;
let markerLayer = null;

export function initMap() {
    if (map) return;
    map = L.map("map").setView([20, 0], 2);
    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap contributors © CARTO",
        maxZoom: 19
    }).addTo(map);
    markerLayer = L.layerGroup().addTo(map);
}

export function showAlerts(alerts) {
    if (!map) return;
    map.invalidateSize();
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

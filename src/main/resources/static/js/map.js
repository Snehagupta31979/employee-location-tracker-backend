/**
 * Small helper around Leaflet to create maps and manage markers by employee id.
 */
const TrackerMap = (() => {

    function createMap(elementId, center = [28.6139, 77.2090], zoom = 12) {
        const map = L.map(elementId).setView(center, zoom);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors',
            maxZoom: 19
        }).addTo(map);
        return map;
    }

    function statusColor(status) {
        switch (status) {
            case 'ONLINE': return '#22c55e';
            case 'MOVING': return '#3b82f6';
            case 'STOPPED': return '#f59e0b';
            default: return '#9ca3af';
        }
    }

    function upsertMarker(map, markerStore, key, lat, lng, label, status) {
        const color = statusColor(status);
        const icon = L.divIcon({
            className: '',
            html: `<div style="background:${color};width:16px;height:16px;border-radius:50%;border:2px solid #fff;box-shadow:0 0 4px rgba(0,0,0,0.4);"></div>`,
            iconSize: [16, 16],
            iconAnchor: [8, 8]
        });

        if (markerStore[key]) {
            markerStore[key].setLatLng([lat, lng]);
            markerStore[key].setIcon(icon);
            markerStore[key].setPopupContent(label);
        } else {
            markerStore[key] = L.marker([lat, lng], { icon }).addTo(map).bindPopup(label);
        }
        return markerStore[key];
    }
	function upsertDirectionalMarker(map, markerStore, key, lat, lng, label, headingDeg = 0) {
	        if (markerStore[key] && markerStore[key]._isDirectional) {
	            markerStore[key].setLatLng([lat, lng]);
	            markerStore[key].setPopupContent(label);
	            const el = markerStore[key].getElement();
	            if (el) {
	                const arrow = el.querySelector('.direction-arrow-inner');
	                if (arrow) arrow.style.transform = `rotate(${headingDeg}deg)`;
	            }
	            return markerStore[key];
	        }

	        const icon = L.divIcon({
	            className: '',
	            html: `
	                <div style="width:34px;height:34px;display:flex;align-items:center;justify-content:center;">
	                    <div class="direction-arrow-inner" style="transform:rotate(${headingDeg}deg); transition: transform 0.2s ease; width:0;height:0;
	                        border-left:9px solid transparent;border-right:9px solid transparent;
	                        border-bottom:20px solid #2563eb; filter: drop-shadow(0 0 2px rgba(0,0,0,0.5));"></div>
	                </div>`,
	            iconSize: [34, 34],
	            iconAnchor: [17, 17]
	        });

	        markerStore[key] = L.marker([lat, lng], { icon }).addTo(map).bindPopup(label);
	        markerStore[key]._isDirectional = true;
	        return markerStore[key];
	    }

    return { createMap, upsertMarker, upsertDirectionalMarker, statusColor };
})();

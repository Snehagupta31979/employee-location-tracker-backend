if (window.Chart) {
    Chart.defaults.font.family = "'Segoe UI', system-ui, -apple-system, sans-serif";
    Chart.defaults.font.size = 12;
    Chart.defaults.color = '#5a6270';
    Chart.defaults.plugins.legend.labels.usePointStyle = true;
    Chart.defaults.plugins.legend.labels.boxWidth = 8;
    Chart.defaults.plugins.legend.labels.padding = 14;
    Chart.defaults.plugins.tooltip.backgroundColor = '#1e2a44';
    Chart.defaults.plugins.tooltip.padding = 10;
    Chart.defaults.plugins.tooltip.cornerRadius = 8;
    Chart.defaults.plugins.tooltip.titleFont = { weight: '600' };
}

const CHART_PALETTE = ['#2563eb', '#f59e0b', '#22c55e', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899'];
let currentUser = null;
let employeeMap, adminMap;
let officeGeofences = [];
let geofenceCircles = {};
const adminMarkers = {};
let watchIntervalId = null;
let currentHeading = 0;
let lastKnownLat = null;
let lastKnownLng = null;

function calculateBearing(lat1, lon1, lat2, lon2) {
    const toRad = (v) => (v * Math.PI) / 180;
    const toDeg = (v) => (v * 180) / Math.PI;

    const dLon = toRad(lon2 - lon1);
    const y = Math.sin(dLon) * Math.cos(toRad(lat2));
    const x = Math.cos(toRad(lat1)) * Math.sin(toRad(lat2)) -
              Math.sin(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.cos(dLon);
    let bearing = toDeg(Math.atan2(y, x));
    return (bearing + 360) % 360;
}

function updateHeadingFromMovement(newLat, newLng) {
    if (lastKnownLat !== null && lastKnownLng !== null) {
        const moved = haversineMeters(lastKnownLat, lastKnownLng, newLat, newLng);
        if (moved > 3) { // 3 meter se zyada move hua tabhi direction update karo (GPS noise avoid karne ke liye)
            currentHeading = calculateBearing(lastKnownLat, lastKnownLng, newLat, newLng);
            console.log(`[bearing] moved=${moved.toFixed(1)}m heading=${currentHeading.toFixed(1)}°`);
        }
    }
    lastKnownLat = newLat;
    lastKnownLng = newLng;
}
let trackingActive = false;

document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('logoutBtn').addEventListener('click', logout);

    try {
        currentUser = await Api.get('/api/auth/me');
    } catch (e) {
        window.location.href = 'index.html';
        return;
    }

    document.getElementById('welcomeText').textContent = `Welcome, ${currentUser.fullName}`;

    initEmployeeDashboard();

    if (currentUser.role === 'ADMIN') {
        document.getElementById('adminSection').classList.remove('d-none');
        initAdminDashboard();
    }
});

/* =========================================================
   EMPLOYEE DASHBOARD
   ========================================================= */

function initEmployeeDashboard() {
    employeeMap = TrackerMap.createMap('employeeMap');
	loadGeofencesOnMap();
    const employeeMarkerStore = {};

    document.getElementById('refreshLocationBtn').addEventListener('click', () => captureAndSendLocation(employeeMarkerStore));
	document.getElementById('nearMeBtn').addEventListener('click', () => showNearbyPlaces());
	document.getElementById('viewNearbyBtn').addEventListener('click', () => {
	        const list = document.getElementById('nearbyPlacesList');
	        const showing = list.style.display === 'none';
	        list.style.display = showing ? 'block' : 'none';

	        nearbyMarkerRefs.forEach(marker => {
	            if (showing) marker.openTooltip();
	            else marker.closeTooltip();
	        });
	    });

	// Data sirf refresh hogi, location capture ab Start button dabane par hi shuru hogi
	    refreshEmployeeData(employeeMarkerStore);
		

	    document.getElementById('startTrackingBtn').addEventListener('click', () => startTracking(employeeMarkerStore));
	    document.getElementById('stopTrackingBtn').addEventListener('click', () => stopTracking());

    // Refresh the read-only stats every 30 seconds so status/distance stay current
    setInterval(() => refreshEmployeeData(employeeMarkerStore), 30 * 1000);
}
async function startTracking(markerStore) {
    try {
        await Api.post('/api/location/tracking/start');
    } catch (err) {
        console.error('Failed to start tracking', err);
    }
    trackingActive = true;
    document.getElementById('startTrackingBtn').classList.add('d-none');
    document.getElementById('stopTrackingBtn').classList.remove('d-none');

    captureAndSendLocation(markerStore);
    if (watchIntervalId) clearInterval(watchIntervalId);
    watchIntervalId = setInterval(() => captureAndSendLocation(markerStore), 2 * 60 * 1000); // har 2 min
    refreshEmployeeData(markerStore);
}

async function stopTracking() {
    try {
        await Api.post('/api/location/tracking/stop');
    } catch (err) {
        console.error('Failed to stop tracking', err);
    }
    trackingActive = false;
    if (watchIntervalId) clearInterval(watchIntervalId);
    document.getElementById('stopTrackingBtn').classList.add('d-none');
    document.getElementById('startTrackingBtn').classList.remove('d-none');
}

function captureAndSendLocation(markerStore) {
    if (!navigator.geolocation) {
        alert('Geolocation is not supported by this browser.');
        return;
    }

    navigator.geolocation.getCurrentPosition(async (position) => {
        const { latitude, longitude, accuracy } = position.coords;
        try {
            await Api.post('/api/location/save', { latitude, longitude, accuracy });
            await refreshEmployeeData(markerStore);
        } catch (err) {
            console.error('Failed to save location', err);
        }
    }, (error) => {
        console.warn('Geolocation error:', error.message);
    }, { enableHighAccuracy: true, timeout: 15000, maximumAge: 60000 });
}

async function refreshEmployeeData(markerStore) {
    try {
        const [current, distance, activities, stops] = await Promise.all([
            Api.get('/api/location/current').catch(() => null),
            Api.get('/api/location/distance').catch(() => ({ distanceKm: 0 })),
            Api.get('/api/location/activities').catch(() => []),
            Api.get('/api/location/stops').catch(() => [])
        ]);

        if (current) {
            renderStatusBadge(document.getElementById('statusBadge'), current.status);
            document.getElementById('coordsValue').textContent =
                `${current.latitude.toFixed(5)}, ${current.longitude.toFixed(5)}`;
            document.getElementById('lastUpdatedValue').textContent = formatTime(current.recordedAt);

			updateHeadingFromMovement(current.latitude, current.longitude);
			            TrackerMap.upsertDirectionalMarker(employeeMap, markerStore, 'me',
			                current.latitude, current.longitude, `${current.employeeName}<br>${current.status}`, currentHeading);
            employeeMap.setView([current.latitude, current.longitude], employeeMap.getZoom() || 14);
			updateGeofenceColors(current.latitude, current.longitude);
        }

        document.getElementById('distanceValue').textContent = `${distance.distanceKm.toFixed(2)} km`;

        renderTimeline(activities);
        renderStops(stops);
    } catch (err) {
        console.error('Failed to refresh employee dashboard', err);
    }
}
async function loadGeofencesOnMap() {
    try {
        officeGeofences = await Api.get('/api/geofences');
        console.log('Geofences loaded:', officeGeofences);

        if (!officeGeofences || officeGeofences.length === 0) {
            console.warn('No active geofences returned from /api/geofences — check backend seeding.');
            return;
        }

        officeGeofences.forEach(g => {
            geofenceCircles[g.id] = L.circle([g.latitude, g.longitude], {
                radius: g.radiusMeters,
                color: '#ef4444',
                fillColor: '#ef4444',
                fillOpacity: 0.15,
                weight: 2
            }).addTo(employeeMap).bindPopup(`${g.name} (${g.type}) - ${g.radiusMeters}m`);
        });

        employeeMap.setView([officeGeofences[0].latitude, officeGeofences[0].longitude], 15);

        const radiusSelect = document.getElementById('geofenceRadiusSelect');
        if (radiusSelect) {
            radiusSelect.addEventListener('change', () => {
                const newRadius = parseInt(radiusSelect.value, 10);
                Object.values(geofenceCircles).forEach(circle => circle.setRadius(newRadius));
            });
        }
    } catch (err) {
        console.error('Failed to load geofences from /api/geofences:', err);
    }
}

function updateGeofenceColors(lat, lng) {
    if (!officeGeofences || officeGeofences.length === 0) return;

    let insideAny = false;
    officeGeofences.forEach(g => {
        const circle = geofenceCircles[g.id];
        const activeRadius = circle ? circle.getRadius() : g.radiusMeters;
        const dist = haversineMeters(lat, lng, g.latitude, g.longitude);
        const inside = dist <= activeRadius;
        if (inside) insideAny = true;
        if (circle) {
            circle.setStyle({ color: inside ? '#22c55e' : '#ef4444', fillColor: inside ? '#22c55e' : '#ef4444' });
        }
    });

    const badge = document.getElementById('geofenceStatusBadge');
    if (badge) {
        badge.textContent = insideAny ? 'Inside Geofence' : 'Outside Geofence';
        badge.className = 'badge ms-2 ' + (insideAny ? 'bg-success' : 'bg-danger');
    }
}
function showNearbyPlaces() {
    if (!navigator.geolocation) {
        alert('Geolocation is not supported by this browser.');
        return;
    }
    const panel = document.getElementById('nearbyPlacesPanel');
    const list = document.getElementById('nearbyPlacesList');
    panel.style.display = 'block';
    list.innerHTML = '<div class="text-muted small p-2">Finding nearby places...</div>';

    navigator.geolocation.getCurrentPosition(async (position) => {
        const { latitude, longitude } = position.coords;
        try {
            const places = await fetchNearbyPlaces(latitude, longitude);
            renderNearbyPlaces(places, latitude, longitude);
        } catch (err) {
            console.error('Failed to fetch nearby places', err);
            list.innerHTML = '<div class="text-danger small p-2">Could not load nearby places.</div>';
        }
    }, () => {
        list.innerHTML = '<div class="text-danger small p-2">Location permission denied.</div>';
    }, { enableHighAccuracy: true, timeout: 15000 });
}

async function fetchNearbyPlaces(lat, lng, radiusMeters = 1000) {
    const query = `
        [out:json][timeout:25];
        (
          node["amenity"~"hospital|restaurant|atm|pharmacy|fuel|bank|police|cafe"](around:${radiusMeters},${lat},${lng});
        );
        out body 30;
    `;
    const res = await fetch('https://overpass-api.de/api/interpreter', {
        method: 'POST',
        body: query
    });
    const data = await res.json();

    return (data.elements || [])
        .filter(e => e.tags && e.tags.name)
        .map(e => ({
            name: e.tags.name,
            type: e.tags.amenity,
            lat: e.lat,
            lng: e.lon,
            distanceM: Math.round(haversineMeters(lat, lng, e.lat, e.lon))
        }))
        .sort((a, b) => a.distanceM - b.distanceM);
}

function haversineMeters(lat1, lon1, lat2, lon2) {
    const R = 6371000;
    const toRad = (v) => (v * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function placeIcon(type) {
    const icons = {
        hospital: '🏥', restaurant: '🍽️', atm: '🏧', pharmacy: '💊',
        fuel: '⛽', bank: '🏦', police: '🚓', cafe: '☕'
    };
    return icons[type] || '📍';
}

let lastFetchedPlaces = [];

let nearbyMarkerRefs = [];

function renderNearbyPlaces(places, lat, lng) {
    lastFetchedPlaces = places;
    const list = document.getElementById('nearbyPlacesList');
    list.style.display = 'none';

    if (!places.length) {
        list.innerHTML = '<div class="text-muted small p-2">No nearby places found within 1 km.</div>';
    } else {
        list.innerHTML = places.slice(0, 20).map(p => `
            <div class="list-group-item d-flex justify-content-between align-items-center">
                <span>${placeIcon(p.type)} ${p.name} <span class="text-muted small">(${p.type})</span></span>
                <span class="badge bg-light text-dark">${p.distanceM < 1000 ? p.distanceM + ' m' : (p.distanceM / 1000).toFixed(1) + ' km'}</span>
            </div>
        `).join('');
    }

    nearbyMarkerRefs = [];
    places.slice(0, 20).forEach((p, i) => {
        const marker = TrackerMap.upsertMarker(employeeMap, {}, 'nearby-' + i, p.lat, p.lng, `${placeIcon(p.type)} ${p.name}`, 'STOPPED');
        marker.bindTooltip(`${placeIcon(p.type)} ${p.name}`, {
            permanent: false,
            direction: 'right',
            offset: [10, 0],
            className: 'nearby-place-label'
        });
        nearbyMarkerRefs.push(marker);
    });
}

function renderStatusBadge(el, status) {
    const classMap = {
        ONLINE: 'badge-online',
        MOVING: 'badge-moving',
        STOPPED: 'badge-stopped',
        OFFLINE: 'badge-offline'
    };
    el.className = `badge ${classMap[status] || 'bg-secondary'}`;
    el.textContent = status;
}

function renderTimeline(activities) {
    const container = document.getElementById('activityTimeline');
    if (!activities || activities.length === 0) {
        container.innerHTML = '<p class="text-muted small">No activity yet today.</p>';
        return;
    }
    container.innerHTML = activities.slice().reverse().map(a => `
        <div class="timeline-item">
            <div>${activityLabel(a.activityType)} ${a.description ? '- ' + escapeHtml(a.description) : ''}</div>
            <div class="time">${formatTime(a.activityTime)}</div>
        </div>
    `).join('');
}

function activityLabel(type) {
    const labels = {
        LOGIN: '🔓 Login',
        LOGOUT: '🔒 Logout',
        LOCATION_UPDATE: '📍 Location Update',
        STOP_START: '⏸️ Stop Started',
        STOP_END: '▶️ Stop Ended',
		TRACKING_START: '▶️ Tracking Started',
		        TRACKING_END: '⏹️ Tracking Stopped',
    };
    return labels[type] || type;
}

function renderStops(stops) {
    const container = document.getElementById('stopsList');
    if (!stops || stops.length === 0) {
        container.innerHTML = '<p class="text-muted small">No stops detected yet today.</p>';
        return;
    }
    container.innerHTML = stops.slice().reverse().map(s => `
        <div class="timeline-item">
            <div>⏸️ Stop ${s.ongoing ? '(ongoing)' : ''} - ${s.durationMinutes ?? 0} min</div>
            <div class="time">${formatTime(s.startTime)} ${s.endTime ? '→ ' + formatTime(s.endTime) : ''}</div>
        </div>
    `).join('');
}

/* =========================================================
   ADMIN DASHBOARD
   ========================================================= */

function initAdminDashboard() {
    adminMap = TrackerMap.createMap('adminMap');

    document.getElementById('refreshAdminBtn').addEventListener('click', refreshAdminData);
    document.getElementById('reportForm').addEventListener('submit', onViewReport);
    document.getElementById('exportReportBtn').addEventListener('click', onExportReport);
    document.getElementById('printReportBtn').addEventListener('click', () => window.print());
    document.getElementById('reportDate').valueAsDate = new Date();

    refreshAdminData();
	const adminEvents = new EventSource('/api/admin/events');
	    adminEvents.addEventListener('status-change', () => {
	        refreshAdminData();
	    });
		adminEvents.addEventListener('geofence-event', (e) => {
		        const data = JSON.parse(e.data);
		        showGeofenceToast(data.message);
		    });
    setInterval(refreshAdminData, 30 * 1000);
}

async function refreshAdminData() {
    try {
        const [summary, employees, liveLocations] = await Promise.all([
            Api.get('/api/admin/summary'),
            Api.get('/api/admin/employees'),
            Api.get('/api/admin/live-locations')
        ]);

        document.getElementById('totalCount').textContent = summary.totalEmployees;
        document.getElementById('onlineCount').textContent = summary.onlineEmployees;
        document.getElementById('offlineCount').textContent = summary.offlineEmployees;

        renderEmployeeTable(employees);
        renderReportEmployeeSelect(employees);
        renderLiveLocations(liveLocations);
    } catch (err) {
        console.error('Failed to refresh admin dashboard', err);
    }
}

function renderEmployeeTable(employees) {
    const tbody = document.getElementById('employeeTableBody');
    if (!employees || employees.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted small">No employees found.</td></tr>';
        return;
    }

    tbody.innerHTML = employees.map(e => `
        <tr>
            <td>${escapeHtml(e.fullName)}</td>
            <td><span class="badge ${badgeClass(e.status)}">${e.status}</span></td>
            <td>${e.todayDistanceKm.toFixed(2)} km</td>
            <td class="small">${e.lastUpdated ? formatTime(e.lastUpdated) : '--'}</td>
            <td><button class="btn btn-sm btn-outline-primary" onclick="focusEmployee(${e.id})">View</button></td>
        </tr>
    `).join('');
}

function badgeClass(status) {
    const map = { ONLINE: 'badge-online', MOVING: 'badge-moving', STOPPED: 'badge-stopped', OFFLINE: 'badge-offline' };
    return map[status] || 'bg-secondary';
}

function renderReportEmployeeSelect(employees) {
    const select = document.getElementById('reportEmployeeSelect');
    const previousValue = select.value;
    select.innerHTML = employees.map(e => `<option value="${e.id}">${escapeHtml(e.fullName)}</option>`).join('');
    if (previousValue) select.value = previousValue;
}

function renderLiveLocations(locations) {
    locations.forEach(loc => {
        TrackerMap.upsertMarker(adminMap, adminMarkers, loc.userId,
            loc.latitude, loc.longitude, `${loc.employeeName}<br>${loc.status}`, loc.status);
    });
}

function focusEmployee(employeeId) {
    const marker = adminMarkers[employeeId];
    if (marker) {
        adminMap.setView(marker.getLatLng(), 15);
        marker.openPopup();
    }
    document.getElementById('reportEmployeeSelect').value = employeeId;
}

async function onViewReport(e) {
    e.preventDefault();
    const employeeId = document.getElementById('reportEmployeeSelect').value;
    const date = document.getElementById('reportDate').value;
    if (!employeeId) return;

    try {
        const report = await Api.get(`/api/admin/report?employeeId=${employeeId}${date ? '&date=' + date : ''}`);
        renderReport(report);
    } catch (err) {
        alert(err.message || 'Failed to load report');
    }
}
function isSameLocation(a, b) {
    return Math.abs(a.latitude - b.latitude) < 0.0002 && Math.abs(a.longitude - b.longitude) < 0.0002;
}

function computeStoppageMinutes(locations, index) {
    const current = locations[index];
    const next = locations[index + 1];

    if (next && isSameLocation(current, next)) {
        return Math.round((new Date(next.recordedAt) - new Date(current.recordedAt)) / 60000);
    }

    if (index === locations.length - 1) {
        const prev = locations[index - 1];
        if (prev && isSameLocation(current, prev)) {
            return Math.round((new Date() - new Date(current.recordedAt)) / 60000);
        }
    }

    return 0;
}

function renderReport(report) {
    document.getElementById('reportResult').classList.remove('d-none');
    document.getElementById('reportEmployeeName').textContent = report.employeeName;
    document.getElementById('reportDateLabel').textContent = report.date;
    document.getElementById('reportDistance').textContent = report.totalDistanceKm.toFixed(2);
	document.getElementById('reportStartCount').textContent = report.trackingStartCount ?? 0;
	    document.getElementById('reportStopCount').textContent = report.trackingStopCount ?? 0;
	    document.getElementById('reportFirstStart').textContent = report.firstStartTime ? formatTime(report.firstStartTime) : '--';
		document.getElementById('reportLoginTime').textContent = report.loginTime ? formatTime(report.loginTime) : '--';
		    document.getElementById('reportLogoutTime').textContent = report.logoutTime ? formatTime(report.logoutTime) : '--';

		    document.getElementById('reportSessionsBody').innerHTML = (report.trackingSessions || []).map((s, i) => `
		        <tr><td>${i + 1}</td><td>${formatTime(s.startTime)}</td><td>${s.endTime ? formatTime(s.endTime) : 'Ongoing'}</td><td>${s.durationMinutes ?? 0}</td></tr>
		    `).join('') || '<tr><td colspan="4" class="text-muted small">No tracking sessions recorded.</td></tr>';
			document.getElementById('reportGeofenceBody').innerHTML = (report.geofenceSessions || []).map((s, i) => `
			        <tr><td>${i + 1}</td><td>${s.geofenceName}</td><td>${s.geofenceType}</td>
			        <td>${formatTime(s.entryTime)}</td><td>${s.exitTime ? formatTime(s.exitTime) : 'Ongoing'}</td>
			        <td>${s.durationMinutes ?? 0}</td></tr>
			    `).join('') || '<tr><td colspan="6" class="text-muted small">No geofence visits recorded.</td></tr>';
				loadReportCharts(report.userId, report.date, report);

    

				const locs = report.locations || [];
				    document.getElementById('reportLocationsBody').innerHTML = locs.map((l, i) => {
				        const mins = computeStoppageMinutes(locs, i);
				        const stoppageText = mins > 0 ? `${mins} min` : '--';
				        return `<tr><td>${i + 1}</td><td id="addr-${i}" class="text-muted small">Loading address...</td><td>${formatTime(l.recordedAt)}</td><td>${stoppageText}</td></tr>`;
				    }).join('') || '<tr><td colspan="4" class="text-muted small">No location points recorded.</td></tr>';

				    loadAddressesForLocations(locs);
}

async function onExportReport() {
    const employeeId = document.getElementById('reportEmployeeSelect').value;
    const date = document.getElementById('reportDate').value;
    if (!employeeId) return;

    try {
        await Api.download(
            `/api/admin/report/export?employeeId=${employeeId}${date ? '&date=' + date : ''}`,
            `report_${employeeId}_${date || 'today'}.xlsx`
        );
    } catch (err) {
        alert(err.message || 'Failed to export report');
    }
}

/* =========================================================
   UTILITIES
   ========================================================= */

function formatTime(isoString) {
    if (!isoString) return '--';
    const date = new Date(isoString);
    return date.toLocaleString();
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, (c) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[c]);
}
/* ==========================
   LOGOUT
========================== */

async function logout() {
    try {
        await Api.post('/api/auth/logout');
    } catch (e) {
        console.error(e);
    }
    window.location.href = 'index.html';
}
function showGeofenceToast(message) {
    const container = document.getElementById('geofenceToastContainer');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'alert alert-info shadow-sm mb-2';
    toast.style.minWidth = '280px';
    toast.textContent = '🔔 ' + message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 8000);
}
let dailyDistanceChartInstance = null;
let lastDailyDistanceData = [];
let sessionDurationChartInstance = null;
let currentTimeDistType = 'pie';
let currentSessionType = 'bar';
let currentStoppageType = 'bar';
let lastTimeDistData = null;
let lastSessionData = [];
let lastStoppageData = [];
let addressCache = {};
let stoppageChartInstance = null;
let currentDailyChartType = 'line';
let timeDistributionChartInstance = null;

async function loadReportCharts(userId, dateStr, report) {
	try {
	        lastDailyDistanceData = await Api.get(`/api/analytics/daily-distance?employeeId=${userId}&date=${dateStr}`);
	        renderDailyDistanceChart(currentDailyChartType);
	    } catch (err) {
	        console.error('Failed to load daily distance chart', err);
	    }

		try {
		        const dist = await Api.get(`/api/analytics/time-distribution?employeeId=${userId}&date=${dateStr}`);
		        lastTimeDistData = {
		            labels: ['Moving', 'Stopped', 'Offline'],
		            values: [dist.movingMinutes, dist.stoppedMinutes, dist.offlineMinutes]
		        };
		        renderTimeDistributionChartByType(currentTimeDistType);
		    } catch (err) {
		        console.error('Failed to load time distribution chart', err);
		    }
	// Chart 3: Tracking Session Durations
	try {
	        const sessions = report.trackingSessions || [];
	        lastSessionData = sessions.map((s, i) => ({ label: `Session ${i + 1}`, minutes: s.durationMinutes ?? 0 }));
	        renderSessionDurationChartByType(currentSessionType);
	    } catch (err) {
	        console.error('Failed to load session duration chart', err);
	    }

	    // Chart 4: Location Stoppage Time
		try {
		        const locs = report.locations || [];
		        lastStoppageData = locs.map((l, i) => ({ label: `#${i + 1}`, minutes: computeStoppageMinutes(locs, i) }));
		        renderStoppageChartByType(currentStoppageType);
		    } catch (err) {
		        console.error('Failed to load stoppage chart', err);
		    }
}
function renderDailyDistanceChart(type) {
    const daily = lastDailyDistanceData;
    const dCtx = document.getElementById('reportDailyDistanceChart');
    if (dailyDistanceChartInstance) dailyDistanceChartInstance.destroy();

	const isShape = type === 'pie';
	    const baseDataset = {
	        label: 'Distance (KM)',
	        data: daily.map(d => d.distanceKm),
	        borderColor: type === 'line' ? '#2563eb' : 'transparent',
	        backgroundColor: isShape || type === 'bar' ? CHART_PALETTE : 'rgba(37,99,235,0.12)',
	        borderWidth: type === 'line' ? 2.5 : 0,
	        borderRadius: type === 'bar' ? 8 : 0,
	        maxBarThickness: 40,
	        tension: 0.35,
	        fill: type === 'line',
	        pointRadius: type === 'line' ? 4 : 0,
	        pointBackgroundColor: '#2563eb',
	        pointBorderColor: '#fff',
	        pointBorderWidth: 2,
	        hoverOffset: isShape ? 10 : 0
	    };

    dailyDistanceChartInstance = new Chart(dCtx, {
        type: type,
        data: { labels: daily.map(d => d.label), datasets: [baseDataset] },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: isShape, position: 'bottom' },
                tooltip: { callbacks: { label: (ctx) => ` ${ctx.formattedValue} km` } }
            },
            scales: isShape ? {} : {
                y: { beginAtZero: true, title: { display: true, text: 'KM' }, grid: { color: '#eef0f4' } },
                x: { grid: { display: false } }
            }
        }
    });
}

document.querySelectorAll('.chart-type-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const group = btn.dataset.chartGroup;
        const type = btn.dataset.chartType;

        document.querySelectorAll(`.chart-type-btn[data-chart-group="${group}"]`).forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        if (group === 'daily') { currentDailyChartType = type; renderDailyDistanceChart(type); }
        else if (group === 'time') { currentTimeDistType = type; renderTimeDistributionChartByType(type); }
        else if (group === 'session') { currentSessionType = type; renderSessionDurationChartByType(type); }
        else if (group === 'stoppage') { currentStoppageType = type; renderStoppageChartByType(type); }
    });
});

document.getElementById('dailyChartDownloadBtn').addEventListener('click', () => {
    if (!dailyDistanceChartInstance) return;
    const link = document.createElement('a');
    link.download = 'daily-distance-chart.png';
    link.href = dailyDistanceChartInstance.toBase64Image();
    link.click();
});
document.getElementById('timeChartDownloadBtn').addEventListener('click', () => {
    if (!timeDistributionChartInstance) return;
    const link = document.createElement('a');
    link.download = 'time-distribution-chart.png';
    link.href = timeDistributionChartInstance.toBase64Image();
    link.click();
});

document.getElementById('sessionChartDownloadBtn').addEventListener('click', () => {
    if (!sessionDurationChartInstance) return;
    const link = document.createElement('a');
    link.download = 'tracking-session-durations-chart.png';
    link.href = sessionDurationChartInstance.toBase64Image();
    link.click();
});

document.getElementById('stoppageChartDownloadBtn').addEventListener('click', () => {
    if (!stoppageChartInstance) return;
    const link = document.createElement('a');
    link.download = 'location-stoppage-chart.png';
    link.href = stoppageChartInstance.toBase64Image();
    link.click();
});
function renderTimeDistributionChartByType(type) {
    if (!lastTimeDistData) return;
    const tCtx = document.getElementById('reportTimeDistributionChart');
    if (timeDistributionChartInstance) timeDistributionChartInstance.destroy();

    const isShape = type === 'pie';
    const colors = ['#2563eb', '#f59e0b', '#cbd5e1'];
    timeDistributionChartInstance = new Chart(tCtx, {
        type: type,
        data: {
            labels: lastTimeDistData.labels,
            datasets: [{
                label: 'Minutes',
                data: lastTimeDistData.values,
                backgroundColor: isShape || type === 'bar' ? colors : 'rgba(37,99,235,0.12)',
                borderColor: type === 'line' ? '#2563eb' : 'transparent',
                borderWidth: type === 'line' ? 2.5 : 0,
                borderRadius: type === 'bar' ? 8 : 0,
                maxBarThickness: 40,
                tension: 0.35,
                fill: type === 'line',
                pointRadius: type === 'line' ? 4 : 0,
                hoverOffset: isShape ? 10 : 0
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: {
                legend: { display: isShape, position: 'bottom' },
                tooltip: { callbacks: { label: (c) => ` ${c.label ? c.label + ': ' : ''}${c.formattedValue} min` } }
            },
            scales: isShape ? {} : { y: { beginAtZero: true, grid: { color: '#eef0f4' } }, x: { grid: { display: false } } }
        }
    });
}

function renderSessionDurationChartByType(type) {
    const sCtx = document.getElementById('reportSessionDurationChart');
    if (sessionDurationChartInstance) sessionDurationChartInstance.destroy();

    const isShape = type === 'pie';
    sessionDurationChartInstance = new Chart(sCtx, {
        type: type,
        data: {
            labels: lastSessionData.map(d => d.label),
            datasets: [{
                label: 'Duration (min)',
                data: lastSessionData.map(d => d.minutes),
                backgroundColor: isShape || type === 'bar' ? CHART_PALETTE : 'rgba(34,197,94,0.12)',
                borderColor: type === 'line' ? '#22c55e' : 'transparent',
                borderWidth: type === 'line' ? 2.5 : 0,
                borderRadius: type === 'bar' ? 8 : 0,
                maxBarThickness: 48,
                tension: 0.35,
                fill: type === 'line',
                pointRadius: type === 'line' ? 4 : 0,
                hoverOffset: isShape ? 10 : 0
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: {
                legend: { display: isShape, position: 'bottom' },
                tooltip: { callbacks: { label: (c) => ` ${c.formattedValue} min` } }
            },
            scales: isShape ? {} : { y: { beginAtZero: true, title: { display: true, text: 'Minutes' }, grid: { color: '#eef0f4' } }, x: { grid: { display: false } } }
        }
    });
}

function renderStoppageChartByType(type) {
    const stCtx = document.getElementById('reportStoppageChart');
    if (stoppageChartInstance) stoppageChartInstance.destroy();

    const isShape = type === 'pie';
    stoppageChartInstance = new Chart(stCtx, {
        type: type,
        data: {
            labels: lastStoppageData.map(d => d.label),
            datasets: [{
                label: 'Stoppage (min)',
                data: lastStoppageData.map(d => d.minutes),
                backgroundColor: isShape || type === 'bar' ? CHART_PALETTE : 'rgba(245,158,11,0.12)',
                borderColor: type === 'line' ? '#f59e0b' : 'transparent',
                borderWidth: type === 'line' ? 2.5 : 0,
                borderRadius: type === 'bar' ? 8 : 0,
                maxBarThickness: 30,
                tension: 0.35,
                fill: type === 'line',
                pointRadius: type === 'line' ? 4 : 0,
                hoverOffset: isShape ? 10 : 0
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: {
                legend: { display: isShape, position: 'bottom' },
                tooltip: { callbacks: { label: (c) => ` ${c.formattedValue} min` } }
            },
            scales: isShape ? {} : { y: { beginAtZero: true, title: { display: true, text: 'Minutes' }, grid: { color: '#eef0f4' } }, x: { grid: { display: false } } }
        }
    });
}
async function reverseGeocode(lat, lng) {
    const key = `${lat.toFixed(4)},${lng.toFixed(4)}`;
    if (addressCache[key]) return addressCache[key];

    try {
        const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`);
        const data = await res.json();
        const addr = data.address || {};
        const area = addr.suburb || addr.neighbourhood || addr.village || addr.town || addr.city_district || addr.city || 'Unknown area';
        const pincode = addr.postcode || 'N/A';
        const fullAddress = data.display_name || 'Address unavailable';

        const result = { fullAddress, area, pincode };
        addressCache[key] = result;
        return result;
    } catch (err) {
        console.error('Reverse geocoding failed', err);
        return { fullAddress: 'Address unavailable', area: 'N/A', pincode: 'N/A' };
    }
}

function renderAddressCell(cell, addressObj) {
    cell.classList.remove('text-muted');
    cell.innerHTML = `
        <div style="max-width:280px;">
            <div style="font-weight:500; white-space:normal;">${addressObj.fullAddress}</div>
            <div class="text-muted small">Area: ${addressObj.area} | Pincode: ${addressObj.pincode}</div>
        </div>
    `;
}

async function loadAddressesForLocations(locs) {
    for (let i = 0; i < locs.length; i++) {
        const cell = document.getElementById(`addr-${i}`);
        if (!cell) continue;

        const key = `${locs[i].latitude.toFixed(4)},${locs[i].longitude.toFixed(4)}`;
        if (addressCache[key]) {
            renderAddressCell(cell, addressCache[key]);
            continue;
        }

        const addressObj = await reverseGeocode(locs[i].latitude, locs[i].longitude);
        renderAddressCell(cell, addressObj);

        await new Promise(resolve => setTimeout(resolve, 1000)); // Nominatim rate-limit ke liye 1 sec gap
    }
}
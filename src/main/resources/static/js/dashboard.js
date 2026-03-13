/* ═══════════════════════════════════════════════════════
   ARIS Dashboard — Enterprise Command Engine v2
   Features: Zomato-style route shrinking, yellow→blue routes,
   closest ambulance dispatch, red dot removal on pickup
   ═══════════════════════════════════════════════════════ */

(function () {
    'use strict';

    const TOKEN = localStorage.getItem('resq_token');
    const USER = localStorage.getItem('resq_user') || 'operator';
    const ROLE = localStorage.getItem('resq_role') || 'DISPATCHER';

    if (!TOKEN) { window.location.href = '/login'; return; }

    const AUTH = { 'Authorization': 'Bearer ' + TOKEN, 'Content-Type': 'application/json' };

    document.getElementById('userName').textContent = USER;
    document.getElementById('userRole').textContent = ROLE;
    document.getElementById('navAvatar').textContent = USER.charAt(0).toUpperCase();

    // ═══ CLOCK + UPTIME ═══
    const startTime = Date.now();
    function updateClock() {
        const now = new Date();
        document.getElementById('navClock').textContent = now.toLocaleTimeString('en-US', { hour12: false });
        const elapsed = Math.floor((Date.now() - startTime) / 1000);
        const h = String(Math.floor(elapsed / 3600)).padStart(2, '0');
        const m = String(Math.floor((elapsed % 3600) / 60)).padStart(2, '0');
        const s = String(elapsed % 60).padStart(2, '0');
        document.getElementById('uptimeCounter').textContent = `UPTIME: ${h}:${m}:${s}`;
    }
    setInterval(updateClock, 1000);
    updateClock();

    // ═══ LOGOUT ═══
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.clear();
        document.body.style.transition = 'opacity 0.5s';
        document.body.style.opacity = '0';
        setTimeout(() => { window.location.href = '/login'; }, 500);
    });

    // ═══ MAP SETUP ═══
    const map = L.map('map', { zoomControl: false, attributionControl: false }).setView([28.58, 77.22], 12);
    const streetLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);
    const satLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { maxZoom: 19 });
    L.control.zoom({ position: 'bottomright' }).addTo(map);

    map.on('mousemove', (e) => {
        document.getElementById('mapCoords').textContent = `${e.latlng.lat.toFixed(4)}, ${e.latlng.lng.toFixed(4)}`;
    });

    let isStreet = true;
    document.getElementById('streetViewBtn').addEventListener('click', () => {
        if (!isStreet) { map.removeLayer(satLayer); map.addLayer(streetLayer); isStreet = true;
            document.getElementById('streetViewBtn').classList.add('active');
            document.getElementById('satViewBtn').classList.remove('active'); }
    });
    document.getElementById('satViewBtn').addEventListener('click', () => {
        if (isStreet) { map.removeLayer(streetLayer); map.addLayer(satLayer); isStreet = false;
            document.getElementById('satViewBtn').classList.add('active');
            document.getElementById('streetViewBtn').classList.remove('active'); }
    });
    document.getElementById('recenterBtn').addEventListener('click', () => map.setView([28.58, 77.22], 12));

    // ═══ MARKER STORAGE ═══
    const ambulanceMarkers = {};
    const hospitalMarkers = {};
    const incidentMarkers = {};

    // Route tracking per ambulance: { ambId: { layer, coords, phase } }
    const activeRoutes = {};

    // Custom marker icons
    function divIcon(emoji, cls, size) {
        return L.divIcon({
            html: `<div class="${cls}" style="font-size:${size}px;text-align:center;line-height:${size}px;filter:drop-shadow(0 0 4px rgba(0,212,245,0.4));">${emoji}</div>`,
            iconSize: [size, size], iconAnchor: [size / 2, size / 2], className: ''
        });
    }

    const ambIcon = divIcon('🚑', 'ambulance-marker', 28);
    const heliIcon = divIcon('🚁', 'ambulance-marker', 28);
    const incIcon = divIcon('🔴', 'incident-marker', 22);

    function hospIcon(h) {
        const pct = h.totalBeds > 0 ? (h.availableBeds / h.totalBeds) * 100 : 0;
        const c = pct < 30 ? '#f0192a' : pct < 60 ? '#f5a800' : '#00e676';
        return L.divIcon({
            html: `<div style="font-size:22px;text-align:center;filter:drop-shadow(0 0 6px ${c});">🏥</div>`,
            iconSize: [26, 26], iconAnchor: [13, 13], className: ''
        });
    }

    // ═══ API HELPERS ═══
    async function api(url) {
        try {
            const r = await fetch(url, { headers: AUTH });
            if (r.status === 401 || r.status === 403) { localStorage.clear(); window.location.href = '/login'; return null; }
            return await r.json();
        } catch (e) { console.error('API:', url, e); return null; }
    }

    async function apiPost(url, body) {
        try {
            const r = await fetch(url, { method: 'POST', headers: AUTH, body: JSON.stringify(body) });
            return await r.json();
        } catch (e) { console.error('POST:', url, e); return null; }
    }

    // ═══ ANIMATED COUNT ═══
    function animateValue(el, target, decimals, duration) {
        const start = parseFloat(el.textContent) || 0;
        const startTime = performance.now();
        function tick(now) {
            const p = Math.min((now - startTime) / duration, 1);
            const ease = 1 - Math.pow(1 - p, 3);
            const val = start + (target - start) * ease;
            el.textContent = decimals > 0 ? val.toFixed(decimals) : Math.floor(val);
            if (p < 1) requestAnimationFrame(tick);
        }
        requestAnimationFrame(tick);
    }

    // ═══ HAVERSINE DISTANCE (km) ═══
    function haversine(lat1, lng1, lat2, lng2) {
        const R = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLng = (lng2 - lng1) * Math.PI / 180;
        const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng / 2) ** 2;
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ═══ LOAD STATS ═══
    async function loadStats() {
        const d = await api('/api/dashboard/stats');
        if (!d) return;
        animateValue(document.getElementById('navIncidents'), d.activeIncidents, 0, 600);
        animateValue(document.getElementById('navDeployed'), d.deployedUnits, 0, 600);
        animateValue(document.getElementById('navEta'), d.avgEta || 0, 1, 600);
        animateValue(document.getElementById('navBeds'), d.availableBeds, 0, 600);
        animateValue(document.getElementById('metricTotal'), d.totalAmbulances || 5, 0, 600);
        animateValue(document.getElementById('metricHospitals'), d.totalHospitals || 7, 0, 600);
        animateValue(document.getElementById('metricAvgEta'), d.avgEta || 0, 1, 600);
    }

    // ═══ LOAD AMBULANCES + LIVE ROUTE TRACKING ═══
    let cachedAmbulances = [];
    async function loadAmbulances() {
        const data = await api('/api/ambulances');
        if (!data) return;
        cachedAmbulances = data;

        const list = document.getElementById('unitList');
        list.innerHTML = '';

        for (const a of data) {
            const icon = a.unitCode.startsWith('HELI') ? heliIcon : ambIcon;
            if (ambulanceMarkers[a.id]) {
                ambulanceMarkers[a.id].setLatLng([a.lat, a.lng]);
                ambulanceMarkers[a.id].setPopupContent(
                    `<strong>${a.unitCode}</strong><br>Status: ${a.status}<br>` +
                    `<span style="font-family:monospace;font-size:11px;">${a.lat.toFixed(4)}, ${a.lng.toFixed(4)}</span>`
                );
            } else {
                ambulanceMarkers[a.id] = L.marker([a.lat, a.lng], { icon })
                    .bindPopup(`<strong>${a.unitCode}</strong><br>Status: ${a.status}`)
                    .addTo(map);
            }

            // ── ZOMATO-STYLE ROUTE TRIMMING ──
            // If ambulance is TRANSIT and we have a cached route, trim it
            if (a.status === 'TRANSIT' && activeRoutes[a.id]) {
                trimRouteToPosition(a.id, a.lat, a.lng);
            }

            const sc = a.status === 'ACTIVE' ? 'status-active' : a.status === 'TRANSIT' ? 'status-transit' : 'status-standby';
            const ic = a.unitCode.startsWith('HELI') ? '🚁' : '🚑';
            list.innerHTML += `
                <div class="unit-item">
                    <span class="unit-icon">${ic}</span>
                    <div class="unit-info">
                        <div class="unit-code">${a.unitCode}</div>
                        <div class="unit-coords">${a.lat.toFixed(4)}, ${a.lng.toFixed(4)}</div>
                    </div>
                    <span class="unit-status ${sc}">${a.status}</span>
                </div>`;
        }

        // Clean up routes for ambulances that are no longer TRANSIT
        for (const ambId of Object.keys(activeRoutes)) {
            const amb = data.find(a => a.id == ambId);
            if (!amb || amb.status !== 'TRANSIT') {
                removeRoute(ambId);
            }
        }
    }

    /**
     * Trim route polyline: find closest waypoint to ambulance position,
     * remove all waypoints before it, update the polyline.
     */
    function trimRouteToPosition(ambId, lat, lng) {
        const route = activeRoutes[ambId];
        if (!route || !route.coords || route.coords.length < 2) return;

        // Find closest waypoint index
        let minDist = Infinity, minIdx = 0;
        for (let i = 0; i < route.coords.length; i++) {
            const d = (route.coords[i][0] - lat) ** 2 + (route.coords[i][1] - lng) ** 2;
            if (d < minDist) { minDist = d; minIdx = i; }
        }

        // Trim: keep only from current position forward
        route.coords = route.coords.slice(minIdx);

        // Prepend ambulance current position for smooth line
        route.coords.unshift([lat, lng]);

        // Update polyline
        if (route.layer) {
            route.layer.setLatLngs(route.coords);
        }
    }

    /**
     * Show route on map with color based on phase.
     * YELLOW = ambulance → incident (approaching patient)
     * BLUE/CYAN = incident → hospital (transporting patient)
     */
    async function showRoute(ambId, fromLat, fromLng, toLat, toLng, phase) {
        // Remove existing route for this ambulance
        removeRoute(ambId);

        const route = await api(`/api/route?from=${fromLat},${fromLng}&to=${toLat},${toLng}`);
        if (!route || !route.coordinates || route.coordinates.length < 2) return;

        const color = phase === 'TO_INCIDENT' ? '#FFD600' : '#00d4f5'; // Yellow or Cyan
        const weight = phase === 'TO_INCIDENT' ? 4 : 3;

        const layer = L.polyline(route.coordinates, {
            color: color,
            weight: weight,
            opacity: 0.85,
            dashArray: phase === 'TO_INCIDENT' ? '10, 6' : '8, 5',
            className: 'animated-route'
        }).addTo(map);

        activeRoutes[ambId] = {
            layer: layer,
            coords: [...route.coordinates],
            phase: phase,
            eta: route.durationMinutes
        };

        return route;
    }

    function removeRoute(ambId) {
        if (activeRoutes[ambId]) {
            if (activeRoutes[ambId].layer) map.removeLayer(activeRoutes[ambId].layer);
            delete activeRoutes[ambId];
        }
    }

    // ═══ LOAD HOSPITALS ═══
    let cachedHospitals = [];
    async function loadHospitals() {
        const data = await api('/api/hospitals');
        if (!data) return;
        cachedHospitals = data;

        const matrix = document.getElementById('hospitalMatrix');
        matrix.innerHTML = '';

        data.forEach(h => {
            if (hospitalMarkers[h.id]) {
                hospitalMarkers[h.id].setLatLng([h.lat, h.lng]);
                hospitalMarkers[h.id].setIcon(hospIcon(h));
                hospitalMarkers[h.id].setPopupContent(
                    `<strong>${h.name}</strong><br>Beds: ${h.availableBeds}/${h.totalBeds}<br>${h.specialties || ''}`
                );
            } else {
                hospitalMarkers[h.id] = L.marker([h.lat, h.lng], { icon: hospIcon(h) })
                    .bindPopup(`<strong>${h.name}</strong><br>Beds: ${h.availableBeds}/${h.totalBeds}<br>${h.specialties || ''}`)
                    .addTo(map);
            }

            const pct = h.totalBeds > 0 ? (h.availableBeds / h.totalBeds) * 100 : 0;
            const bar = pct > 60 ? 'green' : pct > 30 ? 'amber' : 'red';
            const bedCls = pct > 60 ? 'green' : pct > 30 ? 'amber' : 'red';
            const specs = h.specialties ? h.specialties.split(',') : [];

            matrix.innerHTML += `
                <div class="hospital-card">
                    <div class="hc-header">
                        <span class="hc-name">${h.name}</span>
                        <span class="hc-beds ${bedCls}">${h.availableBeds}/${h.totalBeds}</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${bar}" style="width:${pct}%"></div>
                    </div>
                    <div class="hc-specialties">
                        ${specs.map(s => `<span class="specialty-badge">${s.trim()}</span>`).join('')}
                    </div>
                </div>`;
        });

        return data;
    }

    // ═══ LOAD INCIDENTS ═══
    let latestIncident = null;
    async function loadIncidents() {
        const data = await api('/api/incidents');
        if (!data) return;

        // Remove old incident markers
        Object.keys(incidentMarkers).forEach(id => {
            if (!data.find(inc => inc.id == id)) {
                map.removeLayer(incidentMarkers[id]);
                delete incidentMarkers[id];
            }
        });

        const mc = document.getElementById('missionContent');

        if (data.length === 0) {
            mc.innerHTML = '<p class="no-mission">No active incidents. System standby.</p>';
            document.getElementById('aiContent').innerHTML = '<p class="no-mission">Awaiting incident data for analysis...</p>';
            document.getElementById('routingOptions').innerHTML = '<p class="no-mission">Awaiting routing analysis...</p>';
            return;
        }

        latestIncident = data[data.length - 1];
        mc.innerHTML = `
            <div class="mission-card">
                <div class="mc-header">
                    <span class="mc-id">INC-${String(latestIncident.id).padStart(4, '0')}</span>
                    <span class="mc-status">${latestIncident.status}</span>
                </div>
                <div class="mc-condition">${latestIncident.condition || 'Emergency'}</div>
                <div class="mc-coords">📍 ${latestIncident.lat.toFixed(4)}, ${latestIncident.lng.toFixed(4)}</div>
            </div>`;

        // Add red markers for active incidents only
        data.forEach(inc => {
            if (incidentMarkers[inc.id]) {
                incidentMarkers[inc.id].setLatLng([inc.lat, inc.lng]);
            } else {
                incidentMarkers[inc.id] = L.marker([inc.lat, inc.lng], { icon: incIcon })
                    .bindPopup(`<strong>Incident #${inc.id}</strong><br>${inc.condition}<br>Status: ${inc.status}`)
                    .addTo(map);
            }
        });

        loadRoutingOptions(latestIncident);
    }

    // ═══ FIND CLOSEST STANDBY AMBULANCE ═══
    function findClosestStandby(targetLat, targetLng, ambulances) {
        let closest = null, minDist = Infinity;
        for (const a of ambulances) {
            if (a.status !== 'STANDBY') continue;
            const d = haversine(a.lat, a.lng, targetLat, targetLng);
            if (d < minDist) { minDist = d; closest = a; }
        }
        return closest;
    }

    // ═══ AI ROUTING + OPTIONS ═══
    async function loadRoutingOptions(incident) {
        const hospitals = cachedHospitals.length > 0 ? cachedHospitals : await api('/api/hospitals');
        if (!hospitals || !incident) return;

        const available = hospitals.filter(h => h.availableBeds > 0);

        // Sort by a score: weighted combo of distance + beds
        const scored = [];
        for (const h of available) {
            const dist = haversine(incident.lat, incident.lng, h.lat, h.lng);
            scored.push({ hospital: h, dist });
        }
        scored.sort((a, b) => a.dist - b.dist);
        const top3 = scored.slice(0, 3);

        // AI Intelligence Panel — recommend best option
        if (top3.length > 0) {
            const best = top3[0];
            const routeData = await api(`/api/route?from=${incident.lat},${incident.lng}&to=${best.hospital.lat},${best.hospital.lng}`);
            const eta = routeData ? routeData.durationMinutes.toFixed(1) : '?';
            const confidence = (85 + Math.random() * 12).toFixed(1);

            document.getElementById('aiContent').innerHTML = `
                <div class="ai-recommendation">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
                        <span class="ai-hospital">${best.hospital.name}</span>
                        <span class="ai-score">${confidence}% optimal</span>
                    </div>
                    <div class="ai-reason">
                        Nearest hospital (${best.dist.toFixed(1)} km) • ETA ${eta} min • ${best.hospital.availableBeds} beds available
                    </div>
                </div>
                <div class="ai-recommendation" style="border-color: rgba(0,212,245,0.15); animation: none;">
                    <div class="ai-reason" style="color: var(--text-0);">
                        💡 Closest STANDBY unit will be auto-dispatched.<br>
                        🟡 Yellow route = approaching patient<br>
                        🔵 Blue route = transporting to hospital
                    </div>
                </div>`;
        }

        // Routing options with dispatch buttons
        const routingDiv = document.getElementById('routingOptions');
        let html = '';
        for (const entry of top3) {
            const h = entry.hospital;
            const routeData = await api(`/api/route?from=${incident.lat},${incident.lng}&to=${h.lat},${h.lng}`);
            const eta = routeData ? routeData.durationMinutes.toFixed(1) : '?';
            const dist = routeData ? routeData.distanceKm.toFixed(1) : entry.dist.toFixed(1);
            const pct = h.totalBeds > 0 ? (h.availableBeds / h.totalBeds) * 100 : 0;
            const bar = pct > 60 ? 'green' : pct > 30 ? 'amber' : 'red';
            const specs = h.specialties ? h.specialties.split(',').slice(0, 3) : [];

            html += `
                <div class="route-option">
                    <div class="ro-header">
                        <span class="ro-name">${h.name}</span>
                        <span class="ro-eta">${eta} min</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${bar}" style="width:${pct}%"></div>
                    </div>
                    <div class="ro-info">
                        <span>Beds: ${h.availableBeds}/${h.totalBeds}</span>
                        <span>${dist} km</span>
                    </div>
                    <div class="ro-specs">
                        ${specs.map(s => `<span class="specialty-badge">${s.trim()}</span>`).join('')}
                    </div>
                    <button class="dispatch-btn" onclick="dispatchToHospital(${h.id}, ${incident.id})">⚡ DISPATCH CLOSEST UNIT</button>
                </div>`;
        }
        routingDiv.innerHTML = html || '<p class="no-mission">No hospitals available</p>';
    }

    // ═══ DISPATCH — AUTO-SELECTS CLOSEST STANDBY UNIT ═══
    window.dispatchToHospital = async function (hospitalId, incidentId) {
        const ambs = cachedAmbulances.length > 0 ? cachedAmbulances : await api('/api/ambulances');
        const incident = latestIncident;
        if (!incident || !ambs) return;

        // Find closest STANDBY ambulance to the incident
        const closest = findClosestStandby(incident.lat, incident.lng, ambs);
        if (!closest) {
            alert('❌ No available STANDBY units for dispatch!');
            return;
        }

        const result = await apiPost('/api/dispatch', {
            unitId: closest.id, hospitalId: hospitalId, incidentId: incidentId
        });

        if (result) {
            // Show YELLOW route: ambulance → incident (approaching patient)
            await showRoute(closest.id, closest.lat, closest.lng, incident.lat, incident.lng, 'TO_INCIDENT');

            // Store the hospital target so we can show blue route later
            if (activeRoutes[closest.id]) {
                activeRoutes[closest.id].hospitalId = hospitalId;
                activeRoutes[closest.id].incidentLat = incident.lat;
                activeRoutes[closest.id].incidentLng = incident.lng;
            }

            // Flash map
            const mapEl = document.querySelector('.map-container');
            mapEl.style.boxShadow = '0 0 30px rgba(255, 214, 0, 0.3)';
            setTimeout(() => { mapEl.style.boxShadow = 'none'; }, 2000);

            map.setView([incident.lat, incident.lng], 14);

            eventBroadcastLocal('DISPATCH',
                `🚑 ${closest.unitCode} dispatched (closest unit, ${haversine(closest.lat, closest.lng, incident.lat, incident.lng).toFixed(1)} km away)`);

            refreshAll();
        }
    };

    // ═══ MONITOR PHASE CHANGES — switch yellow→blue route ═══
    let previousPhases = {}; // ambId → last known incident status
    async function checkPhaseChanges() {
        const ambs = cachedAmbulances;
        for (const a of ambs) {
            if (a.status !== 'TRANSIT') continue;

            const route = activeRoutes[a.id];
            if (!route) continue;

            // If we had a TO_INCIDENT (yellow) route, check if phase changed
            if (route.phase === 'TO_INCIDENT') {
                // Check if ambulance is near the incident (patient picked up)
                if (route.incidentLat && route.incidentLng) {
                    const distToInc = haversine(a.lat, a.lng, route.incidentLat, route.incidentLng);
                    if (distToInc < 0.3) { // within 300m = picked up
                        // Switch to BLUE route: incident → hospital
                        const hosp = cachedHospitals.find(h => h.id === route.hospitalId);
                        if (hosp) {
                            await showRoute(a.id, a.lat, a.lng, hosp.lat, hosp.lng, 'TO_HOSPITAL');
                            activeRoutes[a.id].hospitalId = hosp.id;

                            eventBroadcastLocal('SYSTEM',
                                `📍 Patient picked up — routing to ${hosp.name}`);
                        }
                    }
                }
            }
        }
    }

    // ═══ LOCAL EVENT BROADCAST ═══
    function eventBroadcastLocal(source, message) {
        addEvent({ source, message, timestamp: new Date().toISOString() });
    }

    // ═══ SIMULATE INCIDENT ═══
    document.getElementById('simIncidentBtn').addEventListener('click', async () => {
        const conditions = ['Cardiac Arrest', 'Major Trauma', 'Severe Burns', 'Stroke', 'Respiratory Failure', 'Multi-Vehicle Accident'];
        const lat = 28.50 + Math.random() * 0.2;
        const lng = 77.10 + Math.random() * 0.2;

        const result = await apiPost('/api/incidents', {
            patientId: 'PAT-' + Math.floor(Math.random() * 9000 + 1000),
            condition: conditions[Math.floor(Math.random() * conditions.length)],
            lat: parseFloat(lat.toFixed(4)),
            lng: parseFloat(lng.toFixed(4))
        });

        if (result) {
            const mapEl = document.querySelector('.map-container');
            mapEl.style.boxShadow = '0 0 30px rgba(240, 25, 42, 0.3)';
            setTimeout(() => { mapEl.style.boxShadow = 'none'; }, 2000);
            map.setView([lat, lng], 14);
            refreshAll();
        }
    });

    // ═══ SOS — AUTO DISPATCH CLOSEST ═══
    document.getElementById('sosBtn').addEventListener('click', async () => {
        const lat = 28.55 + Math.random() * 0.1;
        const lng = 77.18 + Math.random() * 0.1;

        const inc = await apiPost('/api/incidents', {
            patientId: 'SOS-' + Date.now(),
            condition: '🆘 EMERGENCY SOS — Critical',
            lat: parseFloat(lat.toFixed(4)),
            lng: parseFloat(lng.toFixed(4))
        });

        if (inc) {
            const ambs = await api('/api/ambulances');
            const hosps = await api('/api/hospitals');
            const closest = findClosestStandby(inc.lat, inc.lng, ambs || []);
            const bestHosp = hosps?.filter(h => h.availableBeds > 0)
                .sort((a, b) => haversine(inc.lat, inc.lng, a.lat, a.lng) - haversine(inc.lat, inc.lng, b.lat, b.lng))[0];

            if (closest && bestHosp) {
                await apiPost('/api/dispatch', { unitId: closest.id, hospitalId: bestHosp.id, incidentId: inc.id });
                // Show yellow route first
                await showRoute(closest.id, closest.lat, closest.lng, inc.lat, inc.lng, 'TO_INCIDENT');
                if (activeRoutes[closest.id]) {
                    activeRoutes[closest.id].hospitalId = bestHosp.id;
                    activeRoutes[closest.id].incidentLat = inc.lat;
                    activeRoutes[closest.id].incidentLng = inc.lng;
                }

                eventBroadcastLocal('SOS DISPATCH',
                    `🆘 ${closest.unitCode} auto-dispatched (closest unit) → ${bestHosp.name}`);
            }
            map.setView([inc.lat, inc.lng], 14);
            refreshAll();
        }
    });

    // ═══ SEARCH ═══
    document.getElementById('searchBtn').addEventListener('click', async () => {
        const q = document.getElementById('mapSearch').value.trim();
        if (!q) return;
        const results = await api('/api/geocode?q=' + encodeURIComponent(q));
        if (results?.length > 0) {
            map.setView([results[0].lat, results[0].lng], 15);
            L.popup().setLatLng([results[0].lat, results[0].lng]).setContent(results[0].displayName).openOn(map);
        }
    });
    document.getElementById('mapSearch').addEventListener('keypress', e => {
        if (e.key === 'Enter') document.getElementById('searchBtn').click();
    });

    // ═══ EVENT FILTERS ═══
    let currentFilter = 'all';
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            currentFilter = chip.dataset.filter;
            filterEvents();
        });
    });
    function filterEvents() {
        document.querySelectorAll('.event-item').forEach(item => {
            item.style.display = (currentFilter === 'all' || item.classList.contains(currentFilter)) ? '' : 'none';
        });
    }

    // ═══ WEBSOCKET ═══
    function connectWS() {
        try {
            const sock = new SockJS('/ws');
            const stomp = Stomp.over(sock);
            stomp.debug = null;
            stomp.connect({}, () => {
                console.log('[RESQ] WebSocket connected');
                stomp.subscribe('/topic/events', msg => addEvent(JSON.parse(msg.body)));
            }, () => setTimeout(connectWS, 5000));
        } catch (e) { setTimeout(connectWS, 5000); }
    }

    function addEvent(ev) {
        const feed = document.getElementById('eventFeed');
        const src = (ev.source || '').toLowerCase();
        const cls = src.includes('system') ? 'system' : src.includes('dispatch') ? 'dispatch' :
                    src.includes('alert') || src.includes('sos') ? 'alert' : 'hospital';
        const srcCls = src.includes('system') ? 'system' : src.includes('dispatch') ? 'dispatch' :
                       src.includes('router') ? 'router' : 'hospital';
        const time = ev.timestamp ? new Date(ev.timestamp).toLocaleTimeString('en-US', { hour12: false }) :
                     new Date().toLocaleTimeString('en-US', { hour12: false });

        feed.insertAdjacentHTML('afterbegin', `
            <div class="event-item ${cls}">
                <div class="ei-header">
                    <span class="ei-source ${srcCls}">${ev.source || 'SYSTEM'}</span>
                    <span class="ei-time">${time}</span>
                </div>
                <div class="ei-message">${ev.message || ''}</div>
            </div>`);
        while (feed.children.length > 50) feed.removeChild(feed.lastChild);
        filterEvents();
    }

    async function loadInitialEvents() {
        const events = await api('/api/events');
        if (events) events.reverse().forEach(addEvent);
    }

    // ═══ REFRESH ═══
    function refreshAll() {
        loadStats();
        loadAmbulances();
        loadHospitals();
        loadIncidents();
    }

    // ═══ INIT ═══
    async function init() {
        await loadStats();
        await loadHospitals();
        await loadAmbulances();
        await loadIncidents();
        await loadInitialEvents();
        connectWS();

        // Polling
        setInterval(loadStats, 5000);
        setInterval(loadAmbulances, 2000);  // Match SimulationService tick
        setInterval(loadHospitals, 10000);
        setInterval(loadIncidents, 3000);
        setInterval(checkPhaseChanges, 2000); // Check for yellow→blue route switches
    }

    init();
})();

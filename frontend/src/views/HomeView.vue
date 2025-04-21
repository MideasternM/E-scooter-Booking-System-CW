<template>
  <div class="home">
    <div class="hero">
      <h1>Welcome to E-Scooter Booking System</h1>
      <p class="subtitle">Your Green Transportation Solution</p>
      <div class="cta-buttons">
        <router-link v-if="!isLoggedIn" to="/login" class="btn primary">Login</router-link>
        <router-link v-if="!isLoggedIn" to="/register" class="btn secondary">Register</router-link>
        <router-link v-if="isLoggedIn" to="/scooters" class="btn primary">Find & Book a Scooter</router-link>
        <button v-if="isLoggedIn" @click="logout" class="btn danger">Logout</button>
      </div>
    </div>

    <!-- START: Add Map Section -->
    <div class="map-section">
      <h2>Find Scooters Near You</h2>
      <div v-if="isLoadingMap" class="loading-message">Loading map...</div>
      <div v-else-if="mapError" class="error-message">{{ mapError }}</div>
      <div id="home-map-container">
        <div id="home-map"></div>
      </div>
    </div>
    <!-- END: Add Map Section -->

    <div class="features">
      <div class="feature-card">
        <h3>Easy Booking</h3>
        <p>Book your scooter in just a few clicks</p>
      </div>
      <div class="feature-card">
        <h3>Eco-Friendly</h3>
        <p>Zero emissions, better for our environment</p>
      </div>
      <div class="feature-card">
        <h3>Affordable</h3>
        <p>Competitive rates for your daily commute</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'
// START: Import Leaflet and API
import { adminApi } from '../services/api' // Using adminApi temporarily, might need a public endpoint
// @ts-ignore 
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
// END: Import Leaflet and API

// Interface for Scooter data needed by map
interface HomeScooter {
    id: number;
    status: string;
    isAvailable: boolean;
    batteryLevel: number;
    location: string;
    latitude?: number | null;
    longitude?: number | null;
}

// --- START: Map and Location State ---
const mapInstance = ref<L.Map | null>(null);
const scooterMarkersLayer = ref<L.LayerGroup | null>(null);
const userMarker = ref<L.Marker | null>(null);
const homeScooters = ref<HomeScooter[]>([]);
const isLoadingMap = ref(true);
const mapError = ref<string | null>(null);

// Define a fixed virtual user location (e.g., near SWJTU South Gate)
// Slightly adjusted coordinates to reduce overlap chance
const virtualUserLocation = L.latLng(30.7488, 103.9783);

// Define Scooter Icon (Emoji)
const scooterDivIcon = L.divIcon({
    html: '🛴', 
    className: 'leaflet-emoji-icon', 
    iconSize: [30, 30], 
    iconAnchor: [15, 30],
    popupAnchor: [0, -30]
});

// Define User Icon (Emoji)
const userDivIcon = L.divIcon({
    html: '📍', // Location pin emoji
    className: 'leaflet-user-icon', // Custom class for styling
    iconSize: [30, 30],
    iconAnchor: [15, 30],
    popupAnchor: [0, -30]
});
// --- END: Map and Location State ---

const router = useRouter()
const authStore = useAuthStore()
const { isAuthenticated: isLoggedIn } = storeToRefs(authStore)

const logout = () => {
  authStore.userLogout()
  router.push('/login')
}

// --- START: Map Functions ---
const initMap = () => {
    if (mapInstance.value) return; // Already initialized
    try {
        mapInstance.value = L.map('home-map').setView(virtualUserLocation, 14); // Center on user

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(mapInstance.value);

        scooterMarkersLayer.value = L.layerGroup().addTo(mapInstance.value);
        
        // Add user marker
        userMarker.value = L.marker(virtualUserLocation, { icon: userDivIcon })
            .addTo(mapInstance.value)
            .bindPopup("Your approximate location");

    } catch (error) {
        console.error("Failed to initialize map:", error);
        mapError.value = "Could not load the map.";
    }
};

const updateMapMarkers = () => {
    if (!mapInstance.value || !scooterMarkersLayer.value) return;

    scooterMarkersLayer.value.clearLayers();
    const validCoords: L.LatLngExpression[] = [virtualUserLocation]; // Include user location

    homeScooters.value.forEach(scooter => {
        if (scooter.latitude != null && scooter.longitude != null && !isNaN(scooter.latitude) && !isNaN(scooter.longitude)) {
            
            const coords: L.LatLngExpression = [scooter.latitude, scooter.longitude];
            validCoords.push(coords);
            
            const marker = L.marker(coords, { icon: scooterDivIcon });
            
            // Determine display status based on the boolean isAvailable flag
            const displayStatus = scooter.isAvailable === true ? 'Available' : 'Unavailable'; // Use isAvailable

            marker.bindPopup(`<b>Scooter #${scooter.id}</b><br>Status: ${displayStatus}<br>Battery: ${scooter.batteryLevel != null ? scooter.batteryLevel + '%' : 'N/A'}`);
            scooterMarkersLayer.value?.addLayer(marker);
        }
    });

    // Adjust map view
    if (validCoords.length > 1 && mapInstance.value) { // More than just the user marker
         try {
             mapInstance.value.flyToBounds(L.latLngBounds(validCoords), { padding: [50, 50] });
         } catch(e) {
             console.error("Error adjusting map bounds:", e);
         }
    }
};

const fetchHomeScooters = async () => {
    isLoadingMap.value = true;
    mapError.value = null;
    try {
        const response = await adminApi.getAllScooters(); 
        homeScooters.value = response.data.map((s: any): HomeScooter => ({
            id: s.id,
            latitude: s.latitude,
            longitude: s.longitude,
            status: s.status,
            batteryLevel: s.batteryLevel,
            isAvailable: s.available,
            location: s.location
        }));
        console.log('Fetched home scooters data:', homeScooters.value); // Add log to check data
        await nextTick();
        updateMapMarkers();
    } catch (error: any) {  
        console.error('Error fetching scooters for home map:', error);
        mapError.value = `Failed to load scooter locations: ${error.message || 'Unknown error'}`;
        homeScooters.value = []; 
    } finally {
        isLoadingMap.value = false;
    }
};
// --- END: Map Functions ---

onMounted(async () => {
  await nextTick();
  initMap();
  if (mapInstance.value) {
      fetchHomeScooters();
  }
});

</script>

<style scoped>
.home {
  width: 100%;
  max-width: 100%;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.hero {
  text-align: center;
  padding: 6rem 2rem;
  background: linear-gradient(135deg, #42b983 0%, #2c3e50 100%);
  color: white;
  border-radius: 0;
  margin-bottom: 0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/images/scooter-1.jpg') center center;
  background-size: cover;
  opacity: 0.1;
  z-index: 1;
}

.hero > * {
  position: relative;
  z-index: 2;
}

.hero h1 {
  font-size: 3.5rem;
  margin-bottom: 1.2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 1.4rem;
  margin-bottom: 3rem;
  opacity: 0.95;
  font-weight: 300;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.cta-buttons {
  display: flex;
  gap: 1.2rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 1rem 2rem;
  border-radius: 50px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  font-size: 1.1rem;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.btn.primary {
  background-color: white;
  color: #42b983;
}

.btn.primary:hover {
  background-color: #f8f9fa;
  color: #3aa876;
}

.btn.secondary {
  background-color: transparent;
  border: 2px solid white;
  color: white;
}

.btn.secondary:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.btn.danger {
  background-color: #ff4444;
  color: white;
}

.btn.danger:hover {
  background-color: #ff3333;
}

.btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
}

.map-section {
  margin: 0;
  padding: 4rem 2rem;
  text-align: center;
  background-color: white;
}

.map-section h2 {
  color: #2c3e50;
  margin-bottom: 2rem;
  font-size: 2.2rem;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.map-section h2::after {
  content: '';
  display: block;
  width: 80px;
  height: 4px;
  background-color: #42b983;
  margin: 1rem auto 0;
  border-radius: 2px;
}

#home-map-container {
  height: 550px;
  width: 100%;
  margin: 0 auto 3rem;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  max-width: 1600px;
}

#home-map {
  height: 100%;
  width: 100%;
}

.loading-message, .error-message {
  padding: 2rem;
  text-align: center;
  font-size: 1.1rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.loading-message {
  color: #606f7b;
  background-color: #f8f9fa;
  border: 1px solid #e0e0e0;
}

.error-message {
  color: #e74c3c;
  background-color: #fceded;
  border: 1px solid #e74c3c;
}

.leaflet-emoji-icon, .leaflet-user-icon {
  font-size: 28px;
  text-align: center;
  line-height: 30px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  padding: 4rem 2rem;
  max-width: 1600px;
  margin: 0 auto;
}

.feature-card {
  background-color: white;
  padding: 3rem 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.4s ease;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background-color: #42b983;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.12);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card h3 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  font-size: 1.6rem;
  font-weight: 600;
}

.feature-card p {
  color: #505a66;
  line-height: 1.7;
  font-size: 1.1rem;
}

@media (max-width: 1024px) {
  .features {
    grid-template-columns: repeat(2, 1fr);
    padding: 3rem 1.5rem;
  }
  
  .map-section {
    padding: 3rem 1.5rem;
  }
  
  .hero {
    padding: 5rem 1.5rem;
  }
  
  .hero h1 {
    font-size: 2.8rem;
  }
  
  #home-map-container {
    height: 450px;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 4rem 1rem;
  }

  .hero h1 {
    font-size: 2.2rem;
  }
  
  .subtitle {
    font-size: 1.1rem;
    margin-bottom: 2rem;
  }
  
  .btn {
    padding: 0.9rem 1.5rem;
    font-size: 1rem;
  }

  .features {
    grid-template-columns: 1fr;
    padding: 2rem 1rem;
  }
  
  .map-section {
    padding: 2rem 1rem;
  }
  
  .map-section h2 {
    font-size: 1.8rem;
  }
  
  #home-map-container {
    height: 350px;
    margin-bottom: 2rem;
  }
  
  .feature-card {
    padding: 2rem 1.5rem;
  }
}
</style>


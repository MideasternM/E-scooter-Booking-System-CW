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
import { ref, onMounted, nextTick, computed, watch, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'
// START: Import Leaflet, new MapStore and types
import { useMapStore } from '../stores/mapStore'; // Import the new map store
import type { MapItem, ScooterMapItem, StoreMapItem } from '../types/MapItem'; // Import map item types
// @ts-ignore 
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
// END: Import Leaflet, new MapStore and types

// Remove or comment out old HomeScooter interface
// interface HomeScooter { ... }

// --- START: Map and Location State ---
const mapInstance = ref<L.Map | null>(null);
const markersLayer = ref<L.LayerGroup | null>(null); // Rename scooterMarkersLayer
const userMarker = ref<L.Marker | null>(null);
const selectedStoreMarker = ref<L.Marker | null>(null);
const isDirectMapAccess = ref(false); // 标记是否是直接从站点列表访问地图

// 使用route获取查询参数
const route = useRoute();

// Use the map store
const mapStore = useMapStore();
const { mapItems, isLoading: isLoadingMap, error: mapError } = storeToRefs(mapStore); // Get state from map store

// Define a fixed virtual user location (e.g., near SWJTU South Gate)
const virtualUserLocation = L.latLng(30.7488, 103.9783);

// Define Scooter Icon (Emoji)
const scooterDivIcon = L.divIcon({
    html: '🛴', 
    className: 'leaflet-emoji-icon scooter-icon', // Add specific class
    iconSize: [30, 30],
    iconAnchor: [15, 30],
    popupAnchor: [0, -30]
});

// Define Store Icon (Emoji)
const storeDivIcon = L.divIcon({
    html: '🏢', // Building emoji
    className: 'leaflet-emoji-icon store-icon', // Add specific class
    iconSize: [30, 30], 
    iconAnchor: [15, 30],
    popupAnchor: [0, -30]
});

// Define Selected Store Icon (Highlighted)
const selectedStoreDivIcon = L.divIcon({
    html: '🏪', // Store emoji (different from regular store)
    className: 'leaflet-emoji-icon selected-store-icon', // Add specific class
    iconSize: [40, 40], // Larger size
    iconAnchor: [20, 40],
    popupAnchor: [0, -40]
});

// Define User Icon (Emoji)
const userDivIcon = L.divIcon({
    html: '📍', // Location pin emoji
    className: 'leaflet-user-icon',
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

// 重置选定的站点和地图状态
const resetSelectedStore = () => {
  // 移除之前的标记（如果有）
  if (selectedStoreMarker.value) {
    selectedStoreMarker.value.remove();
    selectedStoreMarker.value = null;
  }
  
  // 清除localStorage中的站点数据
  localStorage.removeItem('selectedStoreToView');
  
  // 重置地图视图（如果已经加载）
  if (mapInstance.value && markersLayer.value) {
    const validCoords: L.LatLngExpression[] = [virtualUserLocation];
    
    // 收集所有有效的坐标
    mapItems.value.forEach(item => {
      if (item.latitude != null && item.longitude != null && !isNaN(item.latitude) && !isNaN(item.longitude)) {
        validCoords.push([item.latitude, item.longitude]);
      }
    });
    
    // 调整地图视图
    if (validCoords.length > 1) {
      try {
        mapInstance.value.flyToBounds(L.latLngBounds(validCoords), { padding: [50, 50] });
      } catch(e) {
        console.error("Error adjusting map bounds:", e);
      }
    }
  }
};

// --- START: Map Functions ---
const initMap = () => {
    if (mapInstance.value) return; // Already initialized
    try {
        const map = L.map('home-map').setView(virtualUserLocation, 14); // Create map instance first
        mapInstance.value = map; // Assign to ref

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map); // Add tile layer to the map instance

        // Create layer group
        const layerGroup = L.layerGroup();
        layerGroup.addTo(map); // Add layer group to the map instance
        markersLayer.value = layerGroup; // Assign to ref
        
        // Create user marker
        const userMarkerInstance = L.marker(virtualUserLocation, { icon: userDivIcon })
            .bindPopup("Your approximate location");
        userMarkerInstance.addTo(map); // Add user marker to the map instance
        userMarker.value = userMarkerInstance; // Assign to ref

        // 检查URL查询参数以确定是否需要显示特定站点
        checkForSelectedStore();
    } catch (error) {
        console.error("Failed to initialize map:", error);
        // Error ref is now from mapStore
    }
};

// 检查是否有选中的站点需要显示
const checkForSelectedStore = () => {
    // 先检查是否应该显示详情
    if (!isDirectMapAccess.value) {
        // 如果不是直接从站点列表访问地图，则返回
        return;
    }
    
    // 先检查URL参数
    const showStoreId = route.query.showStore as string;
    const lat = route.query.lat as string;
    const lng = route.query.lng as string;
    
    if (showStoreId && lat && lng) {
        const storePosition = L.latLng(parseFloat(lat), parseFloat(lng));
        focusOnSelectedStore(parseInt(showStoreId), storePosition);
        return;
    }
    
    // 如果URL没有参数，检查localStorage
    const storedData = localStorage.getItem('selectedStoreToView');
    if (storedData) {
        try {
            const storeData = JSON.parse(storedData);
            const storePosition = L.latLng(storeData.latitude, storeData.longitude);
            focusOnSelectedStore(storeData.id, storePosition, storeData.name, storeData.code);
            // 使用后清除localStorage
            localStorage.removeItem('selectedStoreToView');
        } catch (error) {
            console.error('Error parsing stored store data:', error);
        }
    }
};

// 聚焦并显示选中的站点
const focusOnSelectedStore = (storeId: number, position: L.LatLng, storeName?: string, storeCode?: string) => {
    if (!mapInstance.value) return;
    
    // 移除之前的标记（如果有）
    if (selectedStoreMarker.value) {
        selectedStoreMarker.value.remove();
    }
    
    // 查找地图项目中的完整店铺信息
    const storeItem = mapItems.value.find(item => 
        item.type === 'store' && item.id === storeId
    ) as StoreMapItem | undefined;
    
    // 准备弹出内容
    let popupContent = '';
    if (storeItem) {
        // 使用地图项目中的详细信息
        popupContent = `<b>Store: ${storeItem.name} (${storeItem.code})</b><br>Status: ${storeItem.status}<br>Available Scooters: ${storeItem.availableScooterCount}`;
        if(storeItem.address) {
            popupContent += `<br>Address: ${storeItem.address}`;
        }
    } else if (storeName && storeCode) {
        // 使用传递的基本信息
        popupContent = `<b>Store: ${storeName} (${storeCode})</b>`;
    } else {
        popupContent = `<b>Store #${storeId}</b>`;
    }
    
    // 创建高亮标记
    const marker = L.marker(position, { icon: selectedStoreDivIcon });
    
    // 使用类型断言解决TypeScript错误
    (marker.addTo(mapInstance.value as any) as L.Marker)
        .bindPopup(popupContent)
        .openPopup();
    
    selectedStoreMarker.value = marker;
    
    // 放大到这个位置
    mapInstance.value.flyTo(position, 16);
};

const updateMapMarkers = () => {
    if (!mapInstance.value || !markersLayer.value) return;

    markersLayer.value.clearLayers();
    const validCoords: L.LatLngExpression[] = [virtualUserLocation]; // Include user location

    // Use mapItems from the store
    mapItems.value.forEach(item => {
        if (item.latitude != null && item.longitude != null && !isNaN(item.latitude) && !isNaN(item.longitude)) {
            
            const coords: L.LatLngExpression = [item.latitude, item.longitude];
            validCoords.push(coords);
            
            let marker: L.Marker;
            let popupContent: string;

            if (item.type === 'scooter') {
                const scooter = item as ScooterMapItem;
                marker = L.marker(coords, { icon: scooterDivIcon });
                const displayStatus = scooter.status; // Use status directly
                popupContent = `<b>Scooter #${scooter.id} (${scooter.model || 'N/A'})</b><br>Status: ${displayStatus}<br>Battery: ${scooter.batteryLevel != null ? scooter.batteryLevel + '%' : 'N/A'}`;
                 // Add Book button if available and logged in
                 if (scooter.status === 'Available' && isLoggedIn.value) {
                    popupContent += `<br><button class="map-book-button" data-scooter-id="${scooter.id}">Book Now</button>`;
                }
            } else if (item.type === 'store') {
                const store = item as StoreMapItem;
                marker = L.marker(coords, { icon: storeDivIcon });
                popupContent = `<b>Store: ${store.name} (${store.code})</b><br>Status: ${store.status}<br>Available Scooters: ${store.availableScooterCount}`; 
                 if(store.address) {
                     popupContent += `<br>Address: ${store.address}`;
                 }
            } else {
                 // Skip unknown type
                 return;
            }

            marker.bindPopup(popupContent);
            markersLayer.value?.addLayer(marker);
        } else {
             console.warn(`Map item ID ${item.id} (Type: ${item.type}) has invalid or missing coordinates.`);
        }
    });

    // 检查是否需要显示特定站点（在加载标记后再检查）
    nextTick(() => {
        checkForSelectedStore();
    });

    // Adjust map view if no specific store is selected
    if (!selectedStoreMarker.value && validCoords.length > 1 && mapInstance.value) {
        try {
            mapInstance.value.flyToBounds(L.latLngBounds(validCoords), { padding: [50, 50] });
        } catch(e) {
            console.error("Error adjusting map bounds:", e);
        }
    }
};

// Modify the fetch function to use the store
const fetchMapData = async () => {
    await mapStore.fetchMapItems(); // Call store action
    if (!mapError.value) { // Check for errors from store
        await nextTick();
        updateMapMarkers();
    }
};

// Add event delegation for book button clicks
const setupMapEventListeners = () => {
    if (!mapInstance.value) return;
    mapInstance.value.on('popupopen', (e) => {
        const content = e.popup.getContent();
        if (typeof content === 'string' && content.includes('map-book-button')) {
            const container = e.popup.getElement();
            const button = container?.querySelector('.map-book-button');
            if (button) {
                button.addEventListener('click', () => {
                    const scooterId = (button as HTMLElement).dataset.scooterId;
                    if (scooterId) {
                        console.log('Booking scooter:', scooterId);
                        router.push({ name: 'booking-create', params: { scooterId } });
                    }
                });
            }
        }
    });
};

// --- END: Map Functions ---

// 检查当前路由是否有查询参数或localStorage中有站点数据，决定是否显示详情
const checkIfDirectAccess = () => {
    // 检查是否有查询参数
    const hasQueryParams = route.query.showStore || localStorage.getItem('selectedStoreToView');
    isDirectMapAccess.value = !!hasQueryParams;
    
    // 如果从其他页面返回（没有查询参数），重置地图状态
    if (!hasQueryParams) {
        resetSelectedStore();
    }
};

onMounted(async () => {
  // 检查是否是从站点列表直接访问
  checkIfDirectAccess();
  
  await nextTick();
  initMap();
  if (mapInstance.value) {
      fetchMapData(); // Call the updated fetch function
      setupMapEventListeners(); // Add listener setup
  }
});

// 在组件卸载前清理
onBeforeUnmount(() => {
  // 如果不是通过查询参数直接访问，则清除localStorage中的站点信息
  if (!route.query.showStore) {
    localStorage.removeItem('selectedStoreToView');
  }
});

// Watch for changes in mapItems to update markers
watch(mapItems, updateMapMarkers);

// 监听路由变化，以便在URL查询参数变化时更新地图
watch(() => route.query, (newQuery, oldQuery) => {
    // 如果新的查询参数中有showStore，则表示是从站点列表直接访问
    if (newQuery.showStore) {
      isDirectMapAccess.value = true;
    } else if (oldQuery.showStore) {
      // 如果旧的查询参数中有showStore，但新的没有，则表示已离开直接访问状态
      isDirectMapAccess.value = false;
      // 重置选定的站点
      resetSelectedStore();
    }
    
    if (mapInstance.value) {
        checkForSelectedStore();
    }
}, { deep: true });

// 监听路由路径变化（用于检测从其他页面返回主页）
watch(() => route.path, (newPath, oldPath) => {
    if (newPath === '/' && oldPath !== '/') {
        // 从其他页面返回主页时，重置状态
        isDirectMapAccess.value = false;
        resetSelectedStore();
    }
}, { immediate: true });
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
  position: relative; /* Needed if adding legends/controls */
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

.leaflet-emoji-icon {
  font-size: 28px;
  text-align: center;
  line-height: 30px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.scooter-icon { /* Optional: specific styles */
}
.store-icon { /* Optional: specific styles */
}
.leaflet-user-icon {
    font-size: 28px;
    text-align: center;
    line-height: 30px;
}

.map-book-button {
    padding: 5px 10px;
    font-size: 0.9em;
    color: white;
    background-color: #4CAF50; /* Green */
    border: none;
    border-radius: 4px;
    cursor: pointer;
    margin-top: 8px;
    display: inline-block;
    transition: background-color 0.2s;
}
.map-book-button:hover {
    background-color: #45a049;
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

/* 添加高亮站点的样式 */
:deep(.selected-store-icon) {
  font-size: 40px;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.7);
  z-index: 1000 !important;
}
</style>


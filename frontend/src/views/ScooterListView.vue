<template>
  <div class="scooter-list-container">
    <div class="header">
      <h1>Available Scooters</h1>
      <div class="filters">
        <div class="search-box">
          <input v-model="searchQuery" type="text" placeholder="Search by ID or location" />
        </div>
        <select v-model="filterStatus">
          <option value="all">All Status</option>
          <option value="available">Available</option>
          <option value="in-use">In Use</option>
          <option value="maintenance">Maintenance</option>
        </select>
        <select v-model="sortBy">
          <option value="id">Sort by ID</option>
          <option value="battery">Sort by Battery</option>
          <option value="distance">Sort by Distance</option>
        </select>
      </div>
    </div>

    <div class="scooter-grid">
      <div v-for="scooter in filteredScooters" :key="scooter.id" class="scooter-card"
        :class="{ 'unavailable': !scooter.available }">
        <div class="scooter-image">
          <img :src="scooter.imageUrl" alt="Scooter" />
          <div class="status-badge" :class="scooter.available ? 'available' : 'unavailable'">
            {{ scooter.available ? 'Available' : 'Unavailable' }}
          </div>
        </div>
        <div class="scooter-info">
          <h3>Scooter #{{ scooter.id }}</h3>
          <div class="scooter-details">
            <p><strong>Model:</strong> {{ scooter.model || 'Standard' }}</p>
            <p><strong>Location:</strong> {{ scooter.location }}</p>
            <p><strong>Distance:</strong> {{ getDistanceToScooter(scooter) }}</p>
          </div>
          <div class="battery-indicator">
            <div class="battery-bar">
              <div :style="{ width: scooter.batteryLevel + '%' }" :class="getBatteryClass(scooter.batteryLevel)">
              </div>
            </div>
            <span>{{ scooter.batteryLevel }}%</span>
          </div>
          <button @click="bookScooter(scooter)" :disabled="!scooter.available" class="book-button">
            {{ scooter.available ? 'Book Now' : 'Unavailable' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../services/api'
import { bookingApi } from '../services/api'
// @ts-ignore 
import L from 'leaflet';
import { rentalStoreService } from '../services/rentalStoreService';

interface Scooter {
  id: number;
  model?: string;
  imageUrl?: string;
  location: string;
  latitude?: number | null; 
  longitude?: number | null;
  batteryLevel: number;
  available: boolean;
  storeId?: number | null;
  scooterCode?: string;
}

const scooters = ref<Scooter[]>([])
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('id')
const router = useRouter()
const storeScooterCodes = ref<string[]>([])

// Define a fixed virtual user location (e.g., near SWJTU South Gate)
const virtualUserLocation = L.latLng(30.7485, 103.9780);

// Helper to get distance in meters for sorting
const getDistanceMeters = (scooter: Scooter): number => {
    if (scooter.latitude != null && scooter.longitude != null && !isNaN(scooter.latitude) && !isNaN(scooter.longitude)) {
        try {
            const scooterLatLng = L.latLng(scooter.latitude, scooter.longitude);
            return virtualUserLocation.distanceTo(scooterLatLng);
        } catch (e) {
            return Infinity; // Return a large number on error
        }
    } else {
        return Infinity; // Return a large number if no coordinates
    }
};

// Function to get formatted distance string for display
const getDistanceToScooter = (scooter: Scooter): string => {
    const distanceMeters = getDistanceMeters(scooter); // Reuse meter calculation
    if (distanceMeters === Infinity) {
         return 'N/A';
    }
    if (distanceMeters < 1000) {
        return `${Math.round(distanceMeters)} m`;
    } else {
        return `${(distanceMeters / 1000).toFixed(1)} km`;
    }
};

onMounted(async () => {
  try {
    // 首先，获取所有租赁门店以获取其滑板车代码
    const stores = await rentalStoreService.getAllStores();
    
    // 创建一个包含所有门店滑板车代码的列表
    storeScooterCodes.value = stores.flatMap(store => store.availableScooters || []);
    console.log("门店中的滑板车代码:", storeScooterCodes.value);
    
    // 然后获取所有滑板车
    const response = await adminApi.getAllScooters()
    console.log("API返回的原始滑板车数据:", response.data);
    
    scooters.value = response.data.map((s: any): Scooter => {
      // 记录每个原始滑板车数据以查看其属性
      console.log("原始滑板车对象:", s);
      
      // 分配滑板车图片URL，根据ID选择不同图片
      const imageIndex = (s.id % 4) + 1; // 从4张图片中选择(scooter-1.jpg到scooter-4.jpg)
      const imageUrl = `/images/scooter-${imageIndex}.jpg`;
      
      // 确保获取正确的scooterCode字段名
      const code = s.scooterCode || s.code || String(s.id);
      
      return {
        id: s.id,
        model: s.model,
        imageUrl: imageUrl, // 使用本地图片URL
        location: s.location,
        latitude: s.latitude,
        longitude: s.longitude,
        batteryLevel: s.batteryLevel ?? 0,
        available: s.available ?? (s.status === 'Available'),
        storeId: s.storeId ?? null, 
        scooterCode: code, // 保存滑板车代码
      };
    });
    
    console.log("从API映射后的滑板车:", scooters.value.map(s => ({id: s.id, code: s.scooterCode})));
  } catch (error) {
    console.error('获取滑板车失败', error)
  }
})

const filteredScooters = computed(() => {
  let result = [...scooters.value]
  
  // 过滤掉那些代码在任何门店的availableScooters列表中的滑板车
  result = result.filter(scooter => {
    // 确保我们以一致的方式处理scooterCode，考虑不同的可能格式
    const scooterCode = scooter.scooterCode || String(scooter.id);
    
    // 检查这个滑板车代码是否在任何门店的列表中
    const isInStore = storeScooterCodes.value.some(storeCode => {
      // 尝试不同的比较方式，包括直接比较和数字ID比较
      return storeCode === scooterCode || 
             storeCode === String(scooter.id) ||
             (Number(storeCode) === scooter.id);
    });
    
    if (isInStore) {
      console.log(`过滤掉滑板车 #${scooter.id}，代码 ${scooterCode}，因为它在门店中`);
    }
    return !isInStore;
  });
  
  console.log("按门店过滤后 - 剩余滑板车数量:", result.length);

  // 应用搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(scooter =>
      scooter.id.toString().includes(query) ||
      scooter.location.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (filterStatus.value !== 'all') {
    const targetAvailability = filterStatus.value === 'available'
    if (filterStatus.value === 'available') {
      result = result.filter(scooter => scooter.available === targetAvailability)
    } else if (filterStatus.value !== 'all') {
      result = result.filter(scooter => !scooter.available)
    }
  }

  // Apply sorting
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'battery':
        return b.batteryLevel - a.batteryLevel
      case 'distance':
        // Use the helper function to get meters for comparison
        return getDistanceMeters(a) - getDistanceMeters(b);
      default:
        return a.id - b.id
    }
  })

  return result
})

const getBatteryClass = (level: number) => {
  if (level > 70) return 'high'
  if (level > 30) return 'medium'
  return 'low'
}

const bookScooter = (scooter: Scooter) => {
  router.push(`/booking/create/${scooter.id}`)
}
</script>

<style scoped>
.scooter-list-container {
  max-width: 100%;
  margin: 0;
  padding: 0;
  background-color: #f8f9fa;
}

.header {
  background-color: #ffffff;
  margin-bottom: 1.5rem;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 1.2rem;
  font-weight: 600;
  font-size: 1.8rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 0;
  flex-wrap: wrap;
}

.search-box input,
.filters select {
  padding: 0.7rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.search-box input:focus,
.filters select:focus {
  border-color: #42b983;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15);
  outline: none;
}

.scooter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  padding: 0 2rem 2rem;
}

.scooter-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.scooter-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.scooter-card.unavailable {
  opacity: 0.7;
}

.scooter-image {
  position: relative;
  height: 180px;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.scooter-image img {
  width: 85%;
  height: 85%;
  object-fit: contain;
  transition: transform 0.4s ease;
}

.scooter-card:hover .scooter-image img {
  transform: scale(1.08);
}

.status-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 30px;
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.status-badge.available {
  background-color: #42b983;
}

.status-badge.unavailable {
  background-color: #e74c3c;
}

.scooter-info {
  padding: 1.5rem;
}

.scooter-info h3 {
  margin: 0 0 1rem;
  color: #2c3e50;
  font-weight: 600;
}

.scooter-details {
  margin-bottom: 1.2rem;
}

.scooter-details p {
  margin: 0.5rem 0;
  color: #505a66;
}

.battery-indicator {
  margin-bottom: 1.5rem;
}

.battery-bar {
  height: 10px;
  background: #eee;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.battery-bar>div {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 10px;
}

.battery-bar .high {
  background-color: #42b983;
}

.battery-bar .medium {
  background-color: #f39c12;
}

.battery-bar .low {
  background-color: #e74c3c;
}

.book-button {
  width: 100%;
  padding: 0.9rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.book-button:hover:not(:disabled) {
  background-color: #3aa876;
  box-shadow: 0 4px 10px rgba(66, 185, 131, 0.3);
}

.book-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }

  .scooter-grid {
    grid-template-columns: 1fr;
    padding: 0 1rem 1rem;
  }
  
  .header {
    padding: 1rem;
  }
}
</style>

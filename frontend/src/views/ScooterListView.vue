<template>
  <div class="scooter-list-container">
    <div class="header">
      <h1>Available Scooters</h1>
      <div class="filters">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Search by ID or location"
          />
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
      <div v-for="scooter in filteredScooters" 
           :key="scooter.id" 
           class="scooter-card"
           :class="{ 'unavailable': !scooter.isAvailable }">
        <div class="scooter-image">
          <img :src="scooter.imageUrl" alt="Scooter" />
          <div class="status-badge" :class="scooter.status.toLowerCase()">
            {{ scooter.status }}
          </div>
        </div>
        <div class="scooter-info">
          <h3>Scooter #{{ scooter.id }}</h3>
          <div class="battery-indicator">
            <div class="battery-bar">
              <div :style="{ width: scooter.batteryLevel + '%' }" 
                   :class="getBatteryClass(scooter.batteryLevel)">
              </div>
            </div>
            <span>{{ scooter.batteryLevel }}%</span>
          </div>
          <p><strong>Location:</strong> {{ scooter.location }}</p>
          <p><strong>Distance:</strong> {{ scooter.distance }}km away</p>
          <button 
            @click="bookScooter(scooter)" 
            :disabled="!scooter.isAvailable"
            class="book-button">
            {{ scooter.isAvailable ? 'Book Now' : 'Unavailable' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

interface Scooter {
  id: number
  status: string
  batteryLevel: number
  location: string
  distance: number
  isAvailable: boolean
  imageUrl: string
}

// Mock data
const mockScooters: Scooter[] = [
  {
    id: 1001,
    status: 'Available',
    batteryLevel: 95,
    location: 'Central Park',
    distance: 0.5,
    isAvailable: true,
    imageUrl: '/images/scooter-1.jpg'
  },
  {
    id: 1002,
    status: 'In Use',
    batteryLevel: 60,
    location: 'Main Street',
    distance: 1.2,
    isAvailable: false,
    imageUrl: '/images/scooter-2.jpg'
  },
  {
    id: 1003,
    status: 'Available',
    batteryLevel: 85,
    location: 'City Square',
    distance: 0.8,
    isAvailable: true,
    imageUrl: '/images/scooter-1.jpg'
  },
  {
    id: 1004,
    status: 'Maintenance',
    batteryLevel: 30,
    location: 'West Station',
    distance: 2.1,
    isAvailable: false,
    imageUrl: '/images/scooter-2.jpg'
  },
  {
    id: 1005,
    status: 'Available',
    batteryLevel: 75,
    location: 'Shopping Mall',
    distance: 1.5,
    isAvailable: true,
    imageUrl: '/images/scooter-1.jpg'
  },
  {
    id: 1006,
    status: 'Available',
    batteryLevel: 90,
    location: 'University Campus',
    distance: 0.3,
    isAvailable: true,
    imageUrl: '/images/scooter-2.jpg'
  }
]

const scooters = ref<Scooter[]>(mockScooters)
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('id')
const router = useRouter()

const filteredScooters = computed(() => {
  let result = [...scooters.value]

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(scooter => 
      scooter.id.toString().includes(query) ||
      scooter.location.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (filterStatus.value !== 'all') {
    result = result.filter(scooter => 
      scooter.status.toLowerCase() === filterStatus.value
    )
  }

  // Apply sorting
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'battery':
        return b.batteryLevel - a.batteryLevel
      case 'distance':
        return a.distance - b.distance
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
  router.push(`/booking/${scooter.id}`)
}
</script>

<style scoped>
.scooter-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  margin-bottom: 2rem;
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.search-box input,
.filters select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.scooter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.scooter-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s ease;
}

.scooter-card:hover {
  transform: translateY(-5px);
}

.scooter-card.unavailable {
  opacity: 0.7;
}

.scooter-image {
  position: relative;
  height: 150px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.scooter-image img {
  width: 80%;
  height: 80%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.scooter-card:hover .scooter-image img {
  transform: scale(1.1);
}

.status-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  color: white;
  font-weight: bold;
  font-size: 0.875rem;
}

.status-badge.available {
  background-color: #42b983;
}

.status-badge.in-use {
  background-color: #f39c12;
}

.status-badge.maintenance {
  background-color: #e74c3c;
}

.scooter-info {
  padding: 1.5rem;
}

.scooter-info h3 {
  margin: 0 0 1rem;
  color: #2c3e50;
}

.battery-indicator {
  margin-bottom: 1rem;
}

.battery-bar {
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.battery-bar > div {
  height: 100%;
  transition: width 0.3s ease;
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
  padding: 0.8rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.book-button:hover:not(:disabled) {
  background-color: #3aa876;
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
  }
}
</style>

<template>
  <div class="booking-list-container">
    <div class="header">
      <h1>My Bookings</h1>
      <div class="filters">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Search by booking ID or location"
          />
        </div>
        <select v-model="filterStatus">
          <option value="all">All Status</option>
          <option value="active">Active</option>
          <option value="completed">Completed</option>
          <option value="cancelled">Cancelled</option>
        </select>
        <select v-model="sortBy">
          <option value="date">Sort by Date</option>
          <option value="duration">Sort by Duration</option>
          <option value="cost">Sort by Cost</option>
        </select>
      </div>
    </div>

    <div class="booking-grid">
      <div v-for="booking in filteredBookings" 
           :key="booking.id" 
           class="booking-card"
           :class="booking.status.toLowerCase()">
        <div class="booking-header">
          <h3>Booking #{{ booking.id }}</h3>
          <div class="status-badge" :class="booking.status.toLowerCase()">
            {{ booking.status }}
          </div>
        </div>
        
        <div class="booking-details">
          <div class="scooter-info">
            <img :src="booking.scooter.imageUrl" alt="Scooter" />
            <div>
              <p><strong>Scooter ID:</strong> #{{ booking.scooter.id }}</p>
              <p><strong>Location:</strong> {{ booking.location }}</p>
            </div>
          </div>

          <div class="time-info">
            <div class="time-block">
              <p class="label">Start Time</p>
              <p class="value">{{ formatDate(booking.startTime) }}</p>
            </div>
            <div class="time-block">
              <p class="label">End Time</p>
              <p class="value">{{ formatDate(booking.endTime) }}</p>
            </div>
            <div class="time-block">
              <p class="label">Duration</p>
              <p class="value">{{ calculateDuration(booking) }}</p>
            </div>
          </div>

          <div class="cost-info">
            <div class="cost-details">
              <p><strong>Base Rate:</strong> ${{ booking.baseRate.toFixed(2) }}</p>
              <p><strong>Additional Charges:</strong> ${{ booking.additionalCharges.toFixed(2) }}</p>
              <p class="total-cost"><strong>Total:</strong> ${{ (booking.baseRate + booking.additionalCharges).toFixed(2) }}</p>
            </div>
          </div>

          <div class="actions">
            <button 
              v-if="booking.status === 'Active'" 
              @click="endRide(booking)"
              class="action-button end">
              End Ride
            </button>
            <button 
              v-if="booking.status === 'Active'"
              @click="reportIssue(booking)"
              class="action-button report">
              Report Issue
            </button>
            <button 
              v-if="booking.status === 'Completed'"
              @click="viewReceipt(booking)"
              class="action-button receipt">
              View Receipt
            </button>
          </div>
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
  imageUrl: string
}

interface Booking {
  id: number
  status: string
  startTime: Date
  endTime: Date
  location: string
  baseRate: number
  additionalCharges: number
  scooter: Scooter
}

// Mock data
const mockBookings: Booking[] = [
  {
    id: 2001,
    status: 'Active',
    startTime: new Date(Date.now() - 30 * 60000), // 30 minutes ago
    endTime: new Date(Date.now() + 30 * 60000), // 30 minutes from now
    location: 'Central Park',
    baseRate: 10.00,
    additionalCharges: 0,
    scooter: {
      id: 1001,
      imageUrl: '/images/scooter-1.jpg'
    }
  },
  {
    id: 2002,
    status: 'Completed',
    startTime: new Date(Date.now() - 24 * 60 * 60000), // 1 day ago
    endTime: new Date(Date.now() - 23 * 60 * 60000), // 23 hours ago
    location: 'Main Street',
    baseRate: 10.00,
    additionalCharges: 2.50,
    scooter: {
      id: 1002,
      imageUrl: '/images/scooter-2.jpg'
    }
  },
  {
    id: 2003,
    status: 'Cancelled',
    startTime: new Date(Date.now() - 2 * 24 * 60 * 60000), // 2 days ago
    endTime: new Date(Date.now() - 2 * 24 * 60 * 60000), // 2 days ago
    location: 'City Square',
    baseRate: 10.00,
    additionalCharges: 5.00,
    scooter: {
      id: 1003,
      imageUrl: '/images/scooter-1.jpg'
    }
  },
  {
    id: 2004,
    status: 'Completed',
    startTime: new Date(Date.now() - 3 * 24 * 60 * 60000), // 3 days ago
    endTime: new Date(Date.now() - 3 * 24 * 60 * 60000 + 2 * 60 * 60000), // 3 days ago + 2 hours
    location: 'Shopping Mall',
    baseRate: 10.00,
    additionalCharges: 0,
    scooter: {
      id: 1004,
      imageUrl: '/images/scooter-2.jpg'
    }
  }
]

const bookings = ref<Booking[]>(mockBookings)
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('date')
const router = useRouter()

const filteredBookings = computed(() => {
  let result = [...bookings.value]

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(booking => 
      booking.id.toString().includes(query) ||
      booking.location.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (filterStatus.value !== 'all') {
    result = result.filter(booking => 
      booking.status.toLowerCase() === filterStatus.value
    )
  }

  // Apply sorting
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'duration':
        return (b.endTime.getTime() - b.startTime.getTime()) - 
               (a.endTime.getTime() - a.startTime.getTime())
      case 'cost':
        return (b.baseRate + b.additionalCharges) - 
               (a.baseRate + a.additionalCharges)
      default: // date
        return b.startTime.getTime() - a.startTime.getTime()
    }
  })

  return result
})

const formatDate = (date: Date) => {
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const calculateDuration = (booking: Booking) => {
  const duration = booking.endTime.getTime() - booking.startTime.getTime()
  const hours = Math.floor(duration / (1000 * 60 * 60))
  const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}h ${minutes}m`
}

const endRide = (booking: Booking) => {
  // Implement end ride logic
  console.log('End ride for booking:', booking.id)
}

const reportIssue = (booking: Booking) => {
  router.push(`/report-issue/${booking.id}`)
}

const viewReceipt = (booking: Booking) => {
  router.push(`/receipt/${booking.id}`)
}
</script>

<style scoped>
.booking-list-container {
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

.booking-grid {
  display: grid;
  gap: 1.5rem;
}

.booking-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.booking-header h3 {
  margin: 0;
  color: #2c3e50;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  color: white;
  font-size: 0.875rem;
}

.status-badge.active {
  background-color: #42b983;
}

.status-badge.completed {
  background-color: #606f7b;
}

.status-badge.cancelled {
  background-color: #e74c3c;
}

.booking-details {
  padding: 1.5rem;
}

.scooter-info {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.scooter-info img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.time-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.time-block .label {
  color: #606f7b;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.time-block .value {
  font-weight: bold;
  color: #2c3e50;
}

.cost-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.cost-details p {
  margin: 0.5rem 0;
}

.total-cost {
  font-size: 1.1rem;
  color: #42b983;
}

.actions {
  display: flex;
  gap: 1rem;
}

.action-button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-button.end {
  background-color: #42b983;
  color: white;
}

.action-button.report {
  background-color: #f39c12;
  color: white;
}

.action-button.receipt {
  background-color: #606f7b;
  color: white;
}

.action-button:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }

  .time-info {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
}
</style> 
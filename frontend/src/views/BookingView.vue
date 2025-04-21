<template>
    <div class="booking-view">
        <h2>My Bookings</h2>
        <div v-if="loading" class="loading-indicator">Loading...</div>
        <div v-else-if="error" class="error-message">
            {{ error }}
            <button @click="fetchBookings" class="retry-button">Retry</button>
        </div>
        <div v-else>
            <div v-if="bookings.length === 0" class="no-bookings-message">No bookings found</div>
            <div v-else>
                <div v-for="booking in bookings" :key="booking.id" class="booking-item">
                    <h3>Booking #{{ booking.id }}</h3>
                    <p><strong>Scooter ID:</strong> #{{ booking.scooterId }}</p>
                    <p><strong>Start Time:</strong> {{ formatDate(booking.startTime) }}</p>
                    <p><strong>End Time:</strong> {{ formatDate(booking.endTime) }}</p>
                    <p><strong>Status:</strong> <span class="status-badge" :class="booking.status.toLowerCase()">{{ booking.status }}</span></p>
                    <button v-if="booking.status === 'ACTIVE'" @click="cancelBooking(booking.id)" class="cancel-button">
                        Cancel Booking
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

export default {
    setup() {
        const bookings = ref([])
        const loading = ref(true)
        const error = ref('')
        const authStore = useAuthStore()

        const fetchBookings = async () => {
            loading.value = true
            error.value = ''
            try {
                const userId = authStore.getCurrentUserId()
                if (!userId) {
                    error.value = 'Please log in to view your bookings'
                    loading.value = false
                    return
                }
                
                const response = await axios.get(`/api/bookings/user/${userId}`)
                bookings.value = response.data
            } catch (err) {
                console.error('Failed to fetch bookings:', err)
                error.value = 'Failed to load bookings. Please try again.'
            } finally {
                loading.value = false
            }
        }

        const cancelBooking = async (bookingId: number) => {
            try {
                await axios.delete(`/api/bookings/${bookingId}`)
                alert('Booking cancelled successfully!')
                fetchBookings()
            } catch (err) {
                console.error('Failed to cancel booking:', err)
                alert('Failed to cancel booking. Please try again.')
            }
        }

        const formatDate = (dateString: string) => {
            return new Date(dateString).toLocaleString()
        }

        onMounted(() => {
            fetchBookings()
        })

        return {
            bookings,
            loading,
            error,
            cancelBooking,
            formatDate,
            fetchBookings
        }
    }
}
</script>

<style scoped>
.booking-view {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.booking-item {
    border: 1px solid #ddd;
    padding: 15px;
    margin-bottom: 15px;
    border-radius: 5px;
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cancel-button {
    padding: 8px 16px;
    background-color: #ff4444;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.cancel-button:hover {
    background-color: #ff0000;
}

.loading-indicator {
    text-align: center;
    padding: 20px;
    color: #666;
}

.error-message {
    color: #ff4444;
    padding: 15px;
    border: 1px solid #ffdddd;
    background-color: #fff6f6;
    border-radius: 5px;
    margin-bottom: 20px;
}

.retry-button {
    margin-left: 10px;
    padding: 5px 10px;
    background-color: #4285f4;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.no-bookings-message {
    text-align: center;
    padding: 30px;
    color: #666;
    font-style: italic;
}

.status-badge {
    display: inline-block;
    padding: 3px 8px;
    border-radius: 12px;
    font-size: 0.85em;
    font-weight: bold;
    color: white;
}

.status-badge.active {
    background-color: #4caf50;
}

.status-badge.completed {
    background-color: #2196f3;
}

.status-badge.cancelled {
    background-color: #f44336;
}

.status-badge.fault_reported {
    background-color: #ff9800;
}
</style>

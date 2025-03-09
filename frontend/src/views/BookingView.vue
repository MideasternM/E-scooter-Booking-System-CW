<template>
    <div class="booking-view">
        <h2>My Bookings</h2>
        <div v-if="loading">Loading...</div>
        <div v-else>
            <div v-if="bookings.length === 0">No bookings found</div>
            <div v-else>
                <div v-for="booking in bookings" :key="booking.id" class="booking-item">
                    <h3>Scooter ID: {{ booking.scooterId }}</h3>
                    <p>Start Time: {{ formatDate(booking.startTime) }}</p>
                    <p>End Time: {{ formatDate(booking.endTime) }}</p>
                    <p>Status: {{ booking.status }}</p>
                    <button v-if="booking.status === 'ACTIVE'" @click="cancelBooking(booking.id)">
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

export default {
    setup() {
        const bookings = ref([])
        const loading = ref(true)

        const fetchBookings = async () => {
            try {
                const response = await axios.get('/api/bookings/user/1') // TODO: Replace with actual user ID
                bookings.value = response.data
            } catch (error) {
                console.error('Failed to fetch bookings:', error)
            } finally {
                loading.value = false
            }
        }

        const cancelBooking = async (bookingId: number) => {
            try {
                await axios.delete(`/api/bookings/${bookingId}`)
                alert('Booking cancelled successfully!')
                fetchBookings()
            } catch (error) {
                console.error('Failed to cancel booking:', error)
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
            cancelBooking,
            formatDate
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
}

button {
    padding: 8px 16px;
    background-color: #ff4444;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}
</style>

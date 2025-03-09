<template>
    <div class="scooter-list">
        <h2>Available Scooters</h2>
        <div v-if="loading">Loading...</div>
        <div v-else>
            <div v-for="scooter in scooters" :key="scooter.id" class="scooter-item">
                <h3>{{ scooter.model }}</h3>
                <p>Location: {{ scooter.location }}</p>
                <p>Battery: {{ scooter.batteryLevel }}%</p>
                <button @click="bookScooter(scooter.id)">Book Now</button>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
    setup() {
        const scooters = ref([])
        const loading = ref(true)

        const fetchScooters = async () => {
            try {
                const response = await axios.get('/api/scooters/available')
                scooters.value = response.data
            } catch (error) {
                console.error('Failed to fetch scooters:', error)
            } finally {
                loading.value = false
            }
        }

        const bookScooter = async (scooterId: number) => {
            try {
                await axios.post('/api/bookings', {
                    scooterId,
                    startTime: new Date().toISOString(),
                    endTime: new Date(Date.now() + 3600 * 1000).toISOString() // 1 hour booking
                })
                alert('Scooter booked successfully!')
                fetchScooters()
            } catch (error) {
                console.error('Booking failed:', error)
            }
        }

        onMounted(() => {
            fetchScooters()
        })

        return {
            scooters,
            loading,
            bookScooter
        }
    }
}
</script>

<style scoped>
.scooter-list {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.scooter-item {
    border: 1px solid #ddd;
    padding: 15px;
    margin-bottom: 15px;
    border-radius: 5px;
}

button {
    padding: 8px 16px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}
</style>

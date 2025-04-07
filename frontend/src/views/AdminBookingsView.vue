<template>
    <div class="admin-bookings">
        <div class="page-header">
            <h1>Booking Management</h1>
        </div>

        <div class="filters">
            <div class="search-box">
                <input v-model="searchQuery" type="text" placeholder="Search by ID, user or location" />
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

        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>User</th>
                        <th>Scooter</th>
                        <th>Start Time</th>
                        <th>End Time</th>
                        <th>Status</th>
                        <th>Cost</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="booking in filteredBookings" :key="booking.id">
                        <td>#{{ booking.id }}</td>
                        <td>{{ booking.userName }}</td>
                        <td>Scooter #{{ booking.scooterId }}</td>
                        <td>{{ formatDate(booking.startTime) }}</td>
                        <td>{{ booking.endTime ? formatDate(booking.endTime) : '-' }}</td>
                        <td>
                            <span class="status-badge" :class="booking.status.toLowerCase()">
                                {{ booking.status }}
                            </span>
                        </td>
                        <td>${{ booking.amount.toFixed(2) }}</td>
                        <td class="actions-cell">
                            <button class="action-btn view" @click="viewBookingDetails(booking.id)">
                                View
                            </button>
                            <button v-if="booking.status === 'Active'" class="action-btn cancel"
                                @click="confirmCancel(booking)">
                                Cancel
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- View Booking Details Modal -->
        <div v-if="showDetailsModal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>Booking Details #{{ selectedBooking.id }}</h3>
                    <button class="close-btn" @click="showDetailsModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="booking-details">
                        <div class="detail-section">
                            <h4>User Information</h4>
                            <p><strong>Name:</strong> {{ selectedBooking.userName }}</p>
                            <p><strong>User ID:</strong> {{ selectedBooking.userId }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Scooter Information</h4>
                            <p><strong>Scooter ID:</strong> #{{ selectedBooking.scooterId }}</p>
                            <p><strong>Location:</strong> {{ selectedBooking.location }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Booking Information</h4>
                            <p><strong>Start Time:</strong> {{ formatDateTime(selectedBooking.startTime) }}</p>
                            <p><strong>End Time:</strong> {{ selectedBooking.endTime ?
                    formatDateTime(selectedBooking.endTime) : 'Not ended yet' }}</p>
                            <p><strong>Duration:</strong> {{ calculateDuration(selectedBooking) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge" :class="selectedBooking.status.toLowerCase()">
                                    {{ selectedBooking.status }}
                                </span>
                            </p>
                        </div>

                        <div class="detail-section">
                            <h4>Payment Information</h4>
                            <p><strong>Base Rate:</strong> ${{ selectedBooking.baseRate?.toFixed(2) || '0.00' }}</p>
                            <p><strong>Additional Charges:</strong> ${{ selectedBooking.additionalCharges?.toFixed(2) ||
                    '0.00' }}</p>
                            <p><strong>Total Amount:</strong> ${{ selectedBooking.amount.toFixed(2) }}</p>
                            <p><strong>Payment Status:</strong> {{ selectedBooking.paymentStatus || 'Not paid' }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Cancel Booking Confirmation Modal -->
        <div v-if="showCancelModal" class="modal-overlay">
            <div class="modal-container delete-modal">
                <div class="modal-header">
                    <h3>Confirm Cancellation</h3>
                    <button class="close-btn" @click="showCancelModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <p>Are you sure you want to cancel Booking #{{ cancellingBooking.id }}?</p>
                    <p class="warning">This action cannot be undone.</p>
                    <div class="form-actions">
                        <button type="button" class="cancel-btn" @click="showCancelModal = false">No, Keep
                            Booking</button>
                        <button type="button" class="delete-btn" @click="cancelBooking">Yes, Cancel Booking</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

interface Booking {
    id: number
    userId: number
    userName: string
    scooterId: number
    location: string
    startTime: Date
    endTime?: Date
    status: string
    baseRate?: number
    additionalCharges?: number
    amount: number
    paymentStatus?: string
}

// State variables
const bookings = ref<Booking[]>([])
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('date')

// Modal state
const showDetailsModal = ref(false)
const showCancelModal = ref(false)

// Selected booking for details or actions
const selectedBooking = ref<Booking>({
    id: 0,
    userId: 0,
    userName: '',
    scooterId: 0,
    location: '',
    startTime: new Date(),
    status: '',
    amount: 0
})

const cancellingBooking = ref<Booking>({
    id: 0,
    userId: 0,
    userName: '',
    scooterId: 0,
    location: '',
    startTime: new Date(),
    status: '',
    amount: 0
})

const router = useRouter()

// Mock data
const mockBookings: Booking[] = [
    {
        id: 1001,
        userId: 101,
        userName: 'John Smith',
        scooterId: 5001,
        location: 'Central Park',
        startTime: new Date(Date.now() - 3600000),
        status: 'Active',
        baseRate: 10.00,
        additionalCharges: 5.50,
        amount: 15.50,
        paymentStatus: 'Pending'
    },
    {
        id: 1000,
        userId: 102,
        userName: 'Emma Johnson',
        scooterId: 5003,
        location: 'City Square',
        startTime: new Date(Date.now() - 7200000),
        status: 'Active',
        baseRate: 10.00,
        additionalCharges: 2.75,
        amount: 12.75,
        paymentStatus: 'Pending'
    },
    {
        id: 999,
        userId: 103,
        userName: 'Michael Brown',
        scooterId: 5010,
        location: 'Main Street',
        startTime: new Date(Date.now() - 86400000),
        endTime: new Date(Date.now() - 82800000),
        status: 'Completed',
        baseRate: 5.00,
        additionalCharges: 3.25,
        amount: 8.25,
        paymentStatus: 'Paid'
    },
    {
        id: 998,
        userId: 104,
        userName: 'Sarah Davis',
        scooterId: 5002,
        location: 'University Campus',
        startTime: new Date(Date.now() - 172800000),
        endTime: new Date(Date.now() - 169200000),
        status: 'Completed',
        baseRate: 7.50,
        additionalCharges: 3.00,
        amount: 10.50,
        paymentStatus: 'Paid'
    },
    {
        id: 997,
        userId: 105,
        userName: 'David Wilson',
        scooterId: 5007,
        location: 'Downtown',
        startTime: new Date(Date.now() - 259200000),
        endTime: new Date(Date.now() - 255600000),
        status: 'Cancelled',
        baseRate: 7.50,
        additionalCharges: 2.25,
        amount: 9.75,
        paymentStatus: 'Refunded'
    }
]

// Computed properties
const filteredBookings = computed(() => {
    let result = [...bookings.value]

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(booking =>
            booking.id.toString().includes(query) ||
            booking.userName.toLowerCase().includes(query) ||
            booking.location.toLowerCase().includes(query)
        )
    }

    // Apply status filter
    if (filterStatus.value !== 'all') {
        const statusMap: Record<string, string> = {
            'active': 'Active',
            'completed': 'Completed',
            'cancelled': 'Cancelled'
        }
        result = result.filter(booking =>
            booking.status === statusMap[filterStatus.value]
        )
    }

    // Apply sorting
    result.sort((a, b) => {
        if (sortBy.value === 'date') {
            return new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
        } else if (sortBy.value === 'duration') {
            const aDuration = a.endTime ? new Date(a.endTime).getTime() - new Date(a.startTime).getTime() : Infinity
            const bDuration = b.endTime ? new Date(b.endTime).getTime() - new Date(b.startTime).getTime() : Infinity
            return bDuration - aDuration
        } else if (sortBy.value === 'cost') {
            return b.amount - a.amount
        }
        return 0
    })

    return result
})

// Methods
const formatDate = (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date)
}

const formatDateTime = (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    }).format(date)
}

const calculateDuration = (booking: Booking) => {
    if (!booking.endTime) {
        return 'Ongoing'
    }

    const start = new Date(booking.startTime).getTime()
    const end = new Date(booking.endTime).getTime()
    const durationMs = end - start

    const hours = Math.floor(durationMs / (1000 * 60 * 60))
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60))

    return `${hours}h ${minutes}m`
}

const viewBookingDetails = (id: number) => {
    const booking = bookings.value.find(b => b.id === id)
    if (booking) {
        selectedBooking.value = { ...booking }
        showDetailsModal.value = true
    }
}

const confirmCancel = (booking: Booking) => {
    cancellingBooking.value = { ...booking }
    showCancelModal.value = true
}

const cancelBooking = async () => {
    try {
        // In a real app, you would call the API
        // await axios.put(`/api/bookings/${cancellingBooking.value.id}/cancel`)

        // For now, update the local state
        const index = bookings.value.findIndex(b => b.id === cancellingBooking.value.id)
        if (index !== -1) {
            bookings.value[index].status = 'Cancelled'
            bookings.value[index].endTime = new Date()
        }

        showCancelModal.value = false
    } catch (error) {
        console.error('Failed to cancel booking:', error)
    }
}

onMounted(async () => {
    try {
        // In a real app, you would fetch the bookings from the API
        // const response = await axios.get('/api/admin/bookings')
        // bookings.value = response.data

        // For now, use mock data
        bookings.value = mockBookings
    } catch (error) {
        console.error('Failed to load bookings:', error)
    }
})
</script>

<style scoped>
.admin-bookings {
    padding: 1rem;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

h1 {
    color: #2c3e50;
    margin: 0;
}

.filters {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.search-box input,
.filters select {
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
}

.search-box input {
    min-width: 250px;
}

.table-container {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
}

.data-table th,
.data-table td {
    padding: 1rem;
    text-align: left;
}

.data-table th {
    background-color: #f8f9fa;
    color: #606f7b;
    font-weight: 500;
}

.data-table tr:not(:last-child) {
    border-bottom: 1px solid #eee;
}

.status-badge {
    display: inline-block;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.875rem;
    font-weight: 500;
    color: white;
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

.actions-cell {
    display: flex;
    gap: 0.5rem;
}

.action-btn {
    padding: 0.25rem 0.5rem;
    border: none;
    border-radius: 4px;
    font-size: 0.875rem;
    cursor: pointer;
    transition: background-color 0.3s;
}

.action-btn.view {
    background-color: #3498db;
    color: white;
}

.action-btn.cancel {
    background-color: #e74c3c;
    color: white;
}

.action-btn:hover {
    opacity: 0.9;
}

/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-container {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.delete-modal {
    max-width: 450px;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #eee;
}

.modal-header h3 {
    margin: 0;
    color: #2c3e50;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #606f7b;
}

.modal-body {
    padding: 1.5rem;
}

.booking-details {
    display: grid;
    gap: 1.5rem;
}

.detail-section h4 {
    margin: 0 0 0.5rem;
    color: #42b983;
    font-size: 1.1rem;
}

.detail-section p {
    margin: 0.25rem 0;
    color: #2c3e50;
}

.warning {
    color: #e74c3c;
    font-weight: 500;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1.5rem;
}

.cancel-btn {
    padding: 0.5rem 1rem;
    background-color: #f8f9fa;
    border: 1px solid #ddd;
    border-radius: 4px;
    color: #606f7b;
    cursor: pointer;
}

.delete-btn {
    padding: 0.5rem 1rem;
    background-color: #e74c3c;
    border: none;
    border-radius: 4px;
    color: white;
    cursor: pointer;
}

.submit-btn {
    padding: 0.5rem 1rem;
    background-color: #42b983;
    border: none;
    border-radius: 4px;
    color: white;
    cursor: pointer;
}

@media (max-width: 768px) {
    .filters {
        flex-direction: column;
        gap: 0.5rem;
    }

    .search-box input {
        min-width: auto;
        width: 100%;
    }

    .data-table th,
    .data-table td {
        padding: 0.75rem 0.5rem;
        font-size: 0.875rem;
    }

    .actions-cell {
        flex-direction: column;
        gap: 0.25rem;
    }

    .action-btn {
        width: 100%;
    }
}
</style>
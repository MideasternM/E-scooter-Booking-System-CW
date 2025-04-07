<template>
    <div class="admin-scooters">
        <div class="page-header">
            <h1>Scooter Management</h1>
            <button class="add-button" @click="showAddModal = true">
                Add New Scooter
            </button>
        </div>

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
                <option value="location">Sort by Location</option>
            </select>
        </div>

        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Status</th>
                        <th>Battery</th>
                        <th>Location</th>
                        <th>Last Maintenance</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="scooter in filteredScooters" :key="scooter.id">
                        <td>#{{ scooter.id }}</td>
                        <td>
                            <span class="status-badge" :class="scooter.status.toLowerCase()">
                                {{ scooter.status }}
                            </span>
                        </td>
                        <td>
                            <div class="battery-indicator">
                                <div class="battery-bar">
                                    <div :style="{ width: scooter.batteryLevel + '%' }"
                                        :class="getBatteryClass(scooter.batteryLevel)">
                                    </div>
                                </div>
                                <span>{{ scooter.batteryLevel }}%</span>
                            </div>
                        </td>
                        <td>{{ scooter.location }}</td>
                        <td>{{ formatDate(scooter.lastMaintenance) }}</td>
                        <td class="actions-cell">
                            <button class="action-btn edit" @click="editScooter(scooter)">
                                Edit
                            </button>
                            <button class="action-btn maintenance" @click="setMaintenance(scooter)">
                                {{ scooter.status === 'Maintenance' ? 'Set Available' : 'Set Maintenance' }}
                            </button>
                            <button class="action-btn delete" @click="confirmDelete(scooter)">
                                Delete
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Add Scooter Modal -->
        <div v-if="showAddModal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>Add New Scooter</h3>
                    <button class="close-btn" @click="showAddModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <form @submit.prevent="addScooter">
                        <div class="form-group">
                            <label>Scooter ID:</label>
                            <input v-model="newScooter.id" type="number" required />
                        </div>
                        <div class="form-group">
                            <label>Status:</label>
                            <select v-model="newScooter.status" required>
                                <option value="Available">Available</option>
                                <option value="In Use">In Use</option>
                                <option value="Maintenance">Maintenance</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Battery Level (%):</label>
                            <input v-model="newScooter.batteryLevel" type="number" min="0" max="100" required />
                        </div>
                        <div class="form-group">
                            <label>Location:</label>
                            <input v-model="newScooter.location" type="text" required />
                        </div>
                        <div class="form-group">
                            <label>Last Maintenance:</label>
                            <input v-model="newScooter.lastMaintenance" type="date" required />
                        </div>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="showAddModal = false">Cancel</button>
                            <button type="submit" class="submit-btn">Add Scooter</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Edit Scooter Modal -->
        <div v-if="showEditModal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>Edit Scooter #{{ editingScooter.id }}</h3>
                    <button class="close-btn" @click="showEditModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <form @submit.prevent="updateScooter">
                        <div class="form-group">
                            <label>Status:</label>
                            <select v-model="editingScooter.status" required>
                                <option value="Available">Available</option>
                                <option value="In Use">In Use</option>
                                <option value="Maintenance">Maintenance</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Battery Level (%):</label>
                            <input v-model="editingScooter.batteryLevel" type="number" min="0" max="100" required />
                        </div>
                        <div class="form-group">
                            <label>Location:</label>
                            <input v-model="editingScooter.location" type="text" required />
                        </div>
                        <div class="form-group">
                            <label>Last Maintenance:</label>
                            <input v-model="editingScooter.lastMaintenance" type="date" required />
                        </div>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="showEditModal = false">Cancel</button>
                            <button type="submit" class="submit-btn">Update Scooter</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Delete Confirmation Modal -->
        <div v-if="showDeleteModal" class="modal-overlay">
            <div class="modal-container delete-modal">
                <div class="modal-header">
                    <h3>Confirm Delete</h3>
                    <button class="close-btn" @click="showDeleteModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <p>Are you sure you want to delete Scooter #{{ deletingScooter.id }}?</p>
                    <p class="warning">This action cannot be undone.</p>
                    <div class="form-actions">
                        <button type="button" class="cancel-btn" @click="showDeleteModal = false">Cancel</button>
                        <button type="button" class="delete-btn" @click="deleteScooter">Delete</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

interface Scooter {
    id: number
    status: string
    batteryLevel: number
    location: string
    lastMaintenance: string
    isAvailable?: boolean
}

// State variables
const scooters = ref<Scooter[]>([])
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('id')

// Modal state
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

// Form data
const newScooter = ref<Scooter>({
    id: 0,
    status: 'Available',
    batteryLevel: 100,
    location: '',
    lastMaintenance: new Date().toISOString().split('T')[0]
})

const editingScooter = ref<Scooter>({
    id: 0,
    status: '',
    batteryLevel: 0,
    location: '',
    lastMaintenance: ''
})

const deletingScooter = ref<Scooter>({
    id: 0,
    status: '',
    batteryLevel: 0,
    location: '',
    lastMaintenance: ''
})

// Mock data
const mockScooters: Scooter[] = [
    {
        id: 5001,
        status: 'Available',
        batteryLevel: 95,
        location: 'Central Park',
        lastMaintenance: '2023-05-15'
    },
    {
        id: 5002,
        status: 'In Use',
        batteryLevel: 60,
        location: 'Main Street',
        lastMaintenance: '2023-04-20'
    },
    {
        id: 5003,
        status: 'Available',
        batteryLevel: 85,
        location: 'City Square',
        lastMaintenance: '2023-05-10'
    },
    {
        id: 5004,
        status: 'Maintenance',
        batteryLevel: 30,
        location: 'Workshop',
        lastMaintenance: '2023-06-01'
    },
    {
        id: 5005,
        status: 'Available',
        batteryLevel: 100,
        location: 'University Campus',
        lastMaintenance: '2023-05-25'
    }
]

// Computed properties
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
        const statusMap: Record<string, string> = {
            'available': 'Available',
            'in-use': 'In Use',
            'maintenance': 'Maintenance'
        }
        result = result.filter(scooter =>
            scooter.status === statusMap[filterStatus.value]
        )
    }

    // Apply sorting
    result.sort((a, b) => {
        if (sortBy.value === 'id') {
            return a.id - b.id
        } else if (sortBy.value === 'battery') {
            return b.batteryLevel - a.batteryLevel
        } else if (sortBy.value === 'location') {
            return a.location.localeCompare(b.location)
        }
        return 0
    })

    return result
})

// Methods
const getBatteryClass = (level: number) => {
    if (level > 70) return 'high'
    if (level > 30) return 'medium'
    return 'low'
}

const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    }).format(date)
}

const editScooter = (scooter: Scooter) => {
    editingScooter.value = { ...scooter }
    showEditModal.value = true
}

const confirmDelete = (scooter: Scooter) => {
    deletingScooter.value = { ...scooter }
    showDeleteModal.value = true
}

const setMaintenance = async (scooter: Scooter) => {
    try {
        const newStatus = scooter.status === 'Maintenance' ? 'Available' : 'Maintenance'
        // In a real app, you would call the API
        // await axios.put(`/api/scooters/${scooter.id}/status`, { status: newStatus })

        // For now, update the local state
        const index = scooters.value.findIndex(s => s.id === scooter.id)
        if (index !== -1) {
            scooters.value[index].status = newStatus
        }
    } catch (error) {
        console.error('Failed to update scooter status:', error)
    }
}

const addScooter = async () => {
    try {
        // In a real app, you would call the API
        // const response = await axios.post('/api/scooters', newScooter.value)
        // const addedScooter = response.data

        // For now, add to the local state
        const addedScooter = { ...newScooter.value }
        scooters.value.push(addedScooter)

        // Reset form and close modal
        newScooter.value = {
            id: 0,
            status: 'Available',
            batteryLevel: 100,
            location: '',
            lastMaintenance: new Date().toISOString().split('T')[0]
        }
        showAddModal.value = false
    } catch (error) {
        console.error('Failed to add scooter:', error)
    }
}

const updateScooter = async () => {
    try {
        // In a real app, you would call the API
        // await axios.put(`/api/scooters/${editingScooter.value.id}`, editingScooter.value)

        // For now, update the local state
        const index = scooters.value.findIndex(s => s.id === editingScooter.value.id)
        if (index !== -1) {
            scooters.value[index] = { ...editingScooter.value }
        }
        showEditModal.value = false
    } catch (error) {
        console.error('Failed to update scooter:', error)
    }
}

const deleteScooter = async () => {
    try {
        // In a real app, you would call the API
        // await axios.delete(`/api/scooters/${deletingScooter.value.id}`)

        // For now, update the local state
        scooters.value = scooters.value.filter(s => s.id !== deletingScooter.value.id)
        showDeleteModal.value = false
    } catch (error) {
        console.error('Failed to delete scooter:', error)
    }
}

// Initialize data
onMounted(() => {
    // In a real app, you would fetch data from the API
    // const response = await axios.get('/api/scooters')
    // scooters.value = response.data

    // For now, use mock data
    scooters.value = mockScooters
})
</script>

<style scoped>
.admin-scooters {
    padding: 20px;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-button {
    background-color: #4CAF50;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.filters {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
}

.search-box input,
.filters select {
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.table-container {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
}

.data-table th,
.data-table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

.status-badge {
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 0.9em;
}

.status-badge.available {
    background-color: #4CAF50;
    color: white;
}

.status-badge.in-use {
    background-color: #2196F3;
    color: white;
}

.status-badge.maintenance {
    background-color: #FFC107;
    color: black;
}

.battery-indicator {
    display: flex;
    align-items: center;
    gap: 10px;
}

.battery-bar {
    width: 100px;
    height: 20px;
    background-color: #eee;
    border-radius: 10px;
    overflow: hidden;
}

.battery-bar>div {
    height: 100%;
    transition: width 0.3s ease;
}

.battery-bar .high {
    background-color: #4CAF50;
}

.battery-bar .medium {
    background-color: #FFC107;
}

.battery-bar .low {
    background-color: #f44336;
}

.actions-cell {
    display: flex;
    gap: 8px;
}

.action-btn {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9em;
}

.action-btn.edit {
    background-color: #2196F3;
    color: white;
}

.action-btn.maintenance {
    background-color: #FFC107;
    color: black;
}

.action-btn.delete {
    background-color: #f44336;
    color: white;
}

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
}

.modal-container {
    background-color: white;
    padding: 20px;
    border-radius: 8px;
    min-width: 400px;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5em;
    cursor: pointer;
}

.form-group {
    margin-bottom: 15px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
}

.cancel-btn {
    background-color: #9e9e9e;
    color: white;
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.submit-btn {
    background-color: #4CAF50;
    color: white;
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.delete-btn {
    background-color: #f44336;
    color: white;
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.warning {
    color: #f44336;
    margin-top: 10px;
}
</style>
<template>
    <div class="admin-users">
        <div class="page-header">
            <h1>User Management</h1>
        </div>

        <div class="filters">
            <div class="search-box">
                <input v-model="searchQuery" type="text" placeholder="Search by ID, name or email" />
            </div>
            <select v-model="filterStatus">
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
                <option value="suspended">Suspended</option>
            </select>
            <select v-model="sortBy">
                <option value="id">Sort by ID</option>
                <option value="name">Sort by Name</option>
                <option value="date">Sort by Join Date</option>
            </select>
        </div>

        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Join Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="user in filteredUsers" :key="user.id">
                        <td>#{{ user.id }}</td>
                        <td>{{ user.name }}</td>
                        <td>{{ user.email }}</td>
                        <td>{{ user.phone }}</td>
                        <td>{{ formatDate(user.joinDate) }}</td>
                        <td>
                            <span class="status-badge" :class="user.status.toLowerCase()">
                                {{ user.status }}
                            </span>
                        </td>
                        <td class="actions-cell">
                            <button class="action-btn view" @click="viewUserDetails(user.id)">
                                View
                            </button>
                            <button v-if="user.status !== 'Suspended'" class="action-btn suspend"
                                @click="confirmSuspend(user)">
                                Suspend
                            </button>
                            <button v-if="user.status === 'Suspended'" class="action-btn activate"
                                @click="activateUser(user.id)">
                                Activate
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- View User Details Modal -->
        <div v-if="showDetailsModal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>User Details #{{ selectedUser.id }}</h3>
                    <button class="close-btn" @click="showDetailsModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="user-details">
                        <div class="detail-section">
                            <h4>Personal Information</h4>
                            <p><strong>Name:</strong> {{ selectedUser.name }}</p>
                            <p><strong>Email:</strong> {{ selectedUser.email }}</p>
                            <p><strong>Phone:</strong> {{ selectedUser.phone }}</p>
                            <p><strong>Address:</strong> {{ selectedUser.address || 'Not provided' }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Account Information</h4>
                            <p><strong>User ID:</strong> #{{ selectedUser.id }}</p>
                            <p><strong>Username:</strong> {{ selectedUser.username }}</p>
                            <p><strong>Join Date:</strong> {{ formatDateTime(selectedUser.joinDate) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge" :class="selectedUser.status.toLowerCase()">
                                    {{ selectedUser.status }}
                                </span>
                            </p>
                            <p v-if="selectedUser.lastLogin"><strong>Last Login:</strong> {{
                    formatDateTime(selectedUser.lastLogin) }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Activity Summary</h4>
                            <p><strong>Total Bookings:</strong> {{ selectedUser.bookingCount || 0 }}</p>
                            <p><strong>Completed Rides:</strong> {{ selectedUser.completedRides || 0 }}</p>
                            <p><strong>Cancelled Bookings:</strong> {{ selectedUser.cancelledBookings || 0 }}</p>
                            <p><strong>Total Spent:</strong> ${{ selectedUser.totalSpent?.toFixed(2) || '0.00' }}</p>
                            <p><strong>Reported Issues:</strong> {{ selectedUser.reportedIssues || 0 }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Notes</h4>
                            <textarea v-model="userNotes" placeholder="Add administrative notes here..."
                                rows="4"></textarea>
                            <button class="save-notes-btn" @click="saveUserNotes">Save Notes</button>
                        </div>

                        <div class="form-actions">
                            <button v-if="selectedUser.status !== 'Suspended'" class="action-btn suspend"
                                @click="confirmSuspend(selectedUser)">
                                Suspend User
                            </button>
                            <button v-if="selectedUser.status === 'Suspended'" class="action-btn activate"
                                @click="activateUser(selectedUser.id)">
                                Activate User
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Suspend User Confirmation Modal -->
        <div v-if="showSuspendModal" class="modal-overlay">
            <div class="modal-container delete-modal">
                <div class="modal-header">
                    <h3>Confirm Suspension</h3>
                    <button class="close-btn" @click="showSuspendModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <p>Are you sure you want to suspend user {{ suspendingUser.name }}?</p>
                    <p>This will prevent the user from making new bookings or logging in.</p>
                    <div class="form-group">
                        <label>Reason for suspension:</label>
                        <textarea v-model="suspensionReason" placeholder="Enter reason for suspension..." rows="3"
                            required></textarea>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="cancel-btn" @click="showSuspendModal = false">Cancel</button>
                        <button type="button" class="delete-btn" @click="suspendUser">Suspend User</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

interface User {
    id: number
    username: string
    name: string
    email: string
    phone: string
    address?: string
    joinDate: Date
    status: string
    lastLogin?: Date
    bookingCount?: number
    completedRides?: number
    cancelledBookings?: number
    totalSpent?: number
    reportedIssues?: number
    notes?: string
}

// State variables
const users = ref<User[]>([])
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('id')

// Modal state
const showDetailsModal = ref(false)
const showSuspendModal = ref(false)
const selectedUser = ref<User>({
    id: 0,
    username: '',
    name: '',
    email: '',
    phone: '',
    joinDate: new Date(),
    status: 'active'
})
const userNotes = ref('')
const suspendingUser = ref<User | null>(null)
const suspensionReason = ref('')

// Computed properties
const filteredUsers = computed(() => {
    let result = [...users.value]

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(user =>
            user.id.toString().includes(query) ||
            user.name.toLowerCase().includes(query) ||
            user.email.toLowerCase().includes(query)
        )
    }

    // Apply status filter
    if (filterStatus.value !== 'all') {
        result = result.filter(user => user.status.toLowerCase() === filterStatus.value)
    }

    // Apply sorting
    result.sort((a, b) => {
        switch (sortBy.value) {
            case 'name':
                return a.name.localeCompare(b.name)
            case 'date':
                return new Date(b.joinDate).getTime() - new Date(a.joinDate).getTime()
            default:
                return a.id - b.id
        }
    })

    return result
})

// Methods
const fetchUsers = async () => {
    try {
        const response = await axios.get('/api/admin/users')
        users.value = response.data
    } catch (error) {
        console.error('Error fetching users:', error)
        // TODO: Add error notification
    }
}

const viewUserDetails = async (userId: number) => {
    try {
        const response = await axios.get(`/api/admin/users/${userId}`)
        selectedUser.value = response.data
        userNotes.value = response.data.notes || ''
        showDetailsModal.value = true
    } catch (error) {
        console.error('Error fetching user details:', error)
        // TODO: Add error notification
    }
}

const confirmSuspend = (user: User) => {
    suspendingUser.value = user
    suspensionReason.value = ''
    showSuspendModal.value = true
}

const suspendUser = async () => {
    if (!suspendingUser.value || !suspensionReason.value.trim()) {
        return
    }

    try {
        await axios.post(`/api/admin/users/${suspendingUser.value.id}/suspend`, {
            reason: suspensionReason.value
        })

        // Update local state
        const user = users.value.find(u => u.id === suspendingUser.value?.id)
        if (user) {
            user.status = 'Suspended'
        }

        showSuspendModal.value = false
        // TODO: Add success notification
    } catch (error) {
        console.error('Error suspending user:', error)
        // TODO: Add error notification
    }
}

const activateUser = async (userId: number) => {
    try {
        await axios.post(`/api/admin/users/${userId}/activate`)

        // Update local state
        const user = users.value.find(u => u.id === userId)
        if (user) {
            user.status = 'Active'
        }

        // TODO: Add success notification
    } catch (error) {
        console.error('Error activating user:', error)
        // TODO: Add error notification
    }
}

const saveUserNotes = async () => {
    if (!selectedUser.value) return

    try {
        await axios.post(`/api/admin/users/${selectedUser.value.id}/notes`, {
            notes: userNotes.value
        })
        // TODO: Add success notification
    } catch (error) {
        console.error('Error saving user notes:', error)
        // TODO: Add error notification
    }
}

const formatDate = (date: Date) => {
    return new Date(date).toLocaleDateString()
}

const formatDateTime = (date: Date) => {
    return new Date(date).toLocaleString()
}

// Lifecycle hooks
onMounted(() => {
    fetchUsers()
})
</script>

<style scoped>
.admin-users {
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

.status-badge.inactive {
    background-color: #606f7b;
}

.status-badge.suspended {
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

.action-btn.suspend {
    background-color: #e74c3c;
    color: white;
}

.action-btn.activate {
    background-color: #42b983;
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

.user-details {
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

.detail-section textarea {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    margin-top: 0.5rem;
    resize: vertical;
}

.save-notes-btn {
    margin-top: 0.5rem;
    padding: 0.5rem 1rem;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.875rem;
}

.save-notes-btn:hover {
    opacity: 0.9;
}

.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    color: #2c3e50;
    font-weight: 500;
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

@media (max-width: 768px) {
    .filters {
        flex-direction: column;
    }

    .data-table th,
    .data-table td {
        padding: 0.75rem 0.5rem;
        font-size: 0.875rem;
    }

    .actions-cell {
        flex-direction: column;
    }
}
</style>
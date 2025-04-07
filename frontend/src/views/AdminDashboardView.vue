<template>
    <div class="admin-dashboard">
        <h1>Admin Dashboard</h1>

        <div class="stats-grid">
            <div class="stat-card">
                <h3>Total Scooters</h3>
                <div class="stat-value">{{ stats.totalScooters }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item">
                        <span class="label">Available:</span>
                        <span class="value">{{ stats.availableScooters }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="label">In Use:</span>
                        <span class="value">{{ stats.inUseScooters }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="label">Maintenance:</span>
                        <span class="value">{{ stats.maintenanceScooters }}</span>
                    </div>
                </div>
            </div>

            <div class="stat-card">
                <h3>Total Users</h3>
                <div class="stat-value">{{ stats.totalUsers }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item">
                        <span class="label">Active:</span>
                        <span class="value">{{ stats.activeUsers }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="label">New Today:</span>
                        <span class="value">{{ stats.newUsers }}</span>
                    </div>
                </div>
            </div>

            <div class="stat-card">
                <h3>Bookings</h3>
                <div class="stat-value">{{ stats.totalBookings }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item">
                        <span class="label">Active:</span>
                        <span class="value">{{ stats.activeBookings }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="label">Completed:</span>
                        <span class="value">{{ stats.completedBookings }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="label">Today:</span>
                        <span class="value">{{ stats.todayBookings }}</span>
                    </div>
                </div>
            </div>

            <div class="stat-card">
                <h3>Revenue</h3>
                <div class="stat-value">${{ stats.totalRevenue.toFixed(2) }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item">
                        <span class="label">Today:</span>
                        <span class="value">${{ stats.todayRevenue.toFixed(2) }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="label">This Week:</span>
                        <span class="value">${{ stats.weekRevenue.toFixed(2) }}</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="recent-section">
            <h2>Recent Bookings</h2>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>User</th>
                            <th>Scooter</th>
                            <th>Start Time</th>
                            <th>Status</th>
                            <th>Amount</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="booking in recentBookings" :key="booking.id">
                            <td>#{{ booking.id }}</td>
                            <td>{{ booking.userName }}</td>
                            <td>Scooter #{{ booking.scooterId }}</td>
                            <td>{{ formatDate(booking.startTime) }}</td>
                            <td>
                                <span class="status-badge" :class="booking.status.toLowerCase()">
                                    {{ booking.status }}
                                </span>
                            </td>
                            <td>${{ booking.amount.toFixed(2) }}</td>
                            <td>
                                <button class="action-btn view" @click="viewBookingDetails(booking.id)">
                                    View
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="view-all">
                <router-link to="/admin/bookings" class="view-all-link">View All Bookings</router-link>
            </div>
        </div>

        <div class="recent-section">
            <h2>Recent Issues</h2>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Scooter</th>
                            <th>Reported By</th>
                            <th>Issue Type</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="issue in recentIssues" :key="issue.id">
                            <td>#{{ issue.id }}</td>
                            <td>Scooter #{{ issue.scooterId }}</td>
                            <td>{{ issue.reportedBy }}</td>
                            <td>{{ issue.issueType }}</td>
                            <td>{{ formatDate(issue.reportDate) }}</td>
                            <td>
                                <span class="status-badge" :class="issue.status.toLowerCase()">
                                    {{ issue.status }}
                                </span>
                            </td>
                            <td>
                                <button class="action-btn view" @click="viewIssueDetails(issue.id)">
                                    View
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="view-all">
                <router-link to="/admin/issues" class="view-all-link">View All Issues</router-link>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// Mock data for dashboard stats
const stats = ref({
    totalScooters: 120,
    availableScooters: 78,
    inUseScooters: 32,
    maintenanceScooters: 10,
    totalUsers: 450,
    activeUsers: 320,
    newUsers: 15,
    totalBookings: 1250,
    activeBookings: 32,
    completedBookings: 1218,
    todayBookings: 45,
    totalRevenue: 8750.50,
    todayRevenue: 450.75,
    weekRevenue: 2340.25
})

// Mock data for recent bookings
const recentBookings = ref([
    {
        id: 1001,
        userName: 'John Smith',
        scooterId: 5001,
        startTime: new Date(Date.now() - 3600000),
        status: 'Active',
        amount: 15.50
    },
    {
        id: 1000,
        userName: 'Emma Johnson',
        scooterId: 5003,
        startTime: new Date(Date.now() - 7200000),
        status: 'Active',
        amount: 12.75
    },
    {
        id: 999,
        userName: 'Michael Brown',
        scooterId: 5010,
        startTime: new Date(Date.now() - 10800000),
        status: 'Completed',
        amount: 8.25
    },
    {
        id: 998,
        userName: 'Sarah Davis',
        scooterId: 5002,
        startTime: new Date(Date.now() - 14400000),
        status: 'Completed',
        amount: 10.50
    },
    {
        id: 997,
        userName: 'David Wilson',
        scooterId: 5007,
        startTime: new Date(Date.now() - 18000000),
        status: 'Completed',
        amount: 9.75
    }
])

// Mock data for recent issues
const recentIssues = ref([
    {
        id: 501,
        scooterId: 5010,
        reportedBy: 'Michael Brown',
        issueType: 'Battery',
        reportDate: new Date(Date.now() - 86400000),
        status: 'Pending'
    },
    {
        id: 500,
        scooterId: 5015,
        reportedBy: 'Lisa Taylor',
        issueType: 'Brakes',
        reportDate: new Date(Date.now() - 172800000),
        status: 'In Progress'
    },
    {
        id: 499,
        scooterId: 5003,
        reportedBy: 'James Anderson',
        issueType: 'Wheels',
        reportDate: new Date(Date.now() - 259200000),
        status: 'Resolved'
    },
    {
        id: 498,
        scooterId: 5022,
        reportedBy: 'Emily White',
        issueType: 'Lights',
        reportDate: new Date(Date.now() - 345600000),
        status: 'Resolved'
    }
])

const formatDate = (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date)
}

const viewBookingDetails = (id: number) => {
    router.push(`/admin/bookings/${id}`)
}

const viewIssueDetails = (id: number) => {
    router.push(`/admin/issues/${id}`)
}

onMounted(async () => {
    // In a real application, you would fetch the dashboard data from the API
    try {
        // const response = await axios.get('/api/admin/dashboard')
        // stats.value = response.data.stats
        // recentBookings.value = response.data.recentBookings
        // recentIssues.value = response.data.recentIssues
    } catch (error) {
        console.error('Failed to load dashboard data:', error)
    }
})
</script>

<style scoped>
.admin-dashboard {
    padding: 1rem;
}

h1 {
    color: #2c3e50;
    margin-bottom: 2rem;
}

h2 {
    color: #2c3e50;
    margin: 2rem 0 1rem;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.stat-card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 1.5rem;
}

.stat-card h3 {
    margin: 0 0 1rem;
    color: #606f7b;
    font-size: 1rem;
}

.stat-value {
    font-size: 2rem;
    font-weight: bold;
    color: #2c3e50;
    margin-bottom: 1rem;
}

.stat-breakdown {
    border-top: 1px solid #eee;
    padding-top: 1rem;
}

.stat-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
}

.stat-item .label {
    color: #606f7b;
}

.stat-item .value {
    font-weight: 500;
    color: #2c3e50;
}

.table-container {
    overflow-x: auto;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.status-badge.pending {
    background-color: #f39c12;
}

.status-badge.cancelled {
    background-color: #e74c3c;
}

.status-badge.resolved {
    background-color: #3498db;
}

.status-badge.in.progress {
    background-color: #9b59b6;
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

.action-btn:hover {
    opacity: 0.9;
}

.view-all {
    text-align: right;
    margin-top: 1rem;
}

.view-all-link {
    color: #3498db;
    text-decoration: none;
    font-weight: 500;
}

.view-all-link:hover {
    text-decoration: underline;
}

@media (max-width: 768px) {
    .stats-grid {
        grid-template-columns: 1fr;
    }

    .data-table th,
    .data-table td {
        padding: 0.75rem 0.5rem;
        font-size: 0.875rem;
    }
}
</style>
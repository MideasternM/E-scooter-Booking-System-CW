<template>
    <div class="admin-issues">
        <div class="page-header">
            <h1>Issue Management</h1>
        </div>

        <div class="filters">
            <div class="search-box">
                <input v-model="searchQuery" type="text" placeholder="Search by ID, scooter or issue type" />
            </div>
            <select v-model="filterStatus">
                <option value="all">All Status</option>
                <option value="pending">Pending</option>
                <option value="in-progress">In Progress</option>
                <option value="resolved">Resolved</option>
            </select>
            <select v-model="sortBy">
                <option value="date">Sort by Date</option>
                <option value="scooter">Sort by Scooter ID</option>
                <option value="type">Sort by Issue Type</option>
            </select>
        </div>

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
                    <tr v-for="issue in filteredIssues" :key="issue.id">
                        <td>#{{ issue.id }}</td>
                        <td>Scooter #{{ issue.scooterId }}</td>
                        <td>{{ issue.reportedBy }}</td>
                        <td>{{ issue.issueType }}</td>
                        <td>{{ formatDate(issue.reportDate) }}</td>
                        <td>
                            <span class="status-badge" :class="issue.status.toLowerCase().replace(' ', '-')">
                                {{ issue.status }}
                            </span>
                        </td>
                        <td class="actions-cell">
                            <button class="action-btn view" @click="viewIssueDetails(issue.id)">
                                View
                            </button>
                            <button v-if="issue.status === 'Pending'" class="action-btn progress"
                                @click="updateIssueStatus(issue.id, 'In Progress')">
                                Start Progress
                            </button>
                            <button v-if="issue.status === 'In Progress'" class="action-btn resolve"
                                @click="updateIssueStatus(issue.id, 'Resolved')">
                                Mark Resolved
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- View Issue Details Modal -->
        <div v-if="showDetailsModal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>Issue Details #{{ selectedIssue.id }}</h3>
                    <button class="close-btn" @click="showDetailsModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="issue-details">
                        <div class="detail-section">
                            <h4>Scooter Information</h4>
                            <p><strong>Scooter ID:</strong> #{{ selectedIssue.scooterId }}</p>
                            <p><strong>Location:</strong> {{ selectedIssue.location }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Reporter Information</h4>
                            <p><strong>Reported By:</strong> {{ selectedIssue.reportedBy }}</p>
                            <p><strong>User ID:</strong> {{ selectedIssue.userId }}</p>
                            <p><strong>Contact:</strong> {{ selectedIssue.contactInfo || 'Not provided' }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Issue Information</h4>
                            <p><strong>Issue Type:</strong> {{ selectedIssue.issueType }}</p>
                            <p><strong>Report Date:</strong> {{ formatDateTime(selectedIssue.reportDate) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge"
                                    :class="selectedIssue.status.toLowerCase().replace(' ', '-')">
                                    {{ selectedIssue.status }}
                                </span>
                            </p>
                        </div>

                        <div class="detail-section">
                            <h4>Description</h4>
                            <p class="issue-description">{{ selectedIssue.description }}</p>
                        </div>

                        <div v-if="selectedIssue.photos && selectedIssue.photos.length > 0" class="detail-section">
                            <h4>Photos</h4>
                            <div class="photo-gallery">
                                <div v-for="(photo, index) in selectedIssue.photos" :key="index" class="photo-item">
                                    <img :src="photo" :alt="`Issue photo ${index + 1}`" />
                                </div>
                            </div>
                        </div>

                        <div class="detail-section">
                            <h4>Resolution Notes</h4>
                            <textarea v-model="resolutionNotes" placeholder="Enter resolution notes here..."
                                :disabled="selectedIssue.status === 'Resolved'" rows="4"></textarea>
                        </div>

                        <div class="form-actions" v-if="selectedIssue.status !== 'Resolved'">
                            <button v-if="selectedIssue.status === 'Pending'" class="action-btn progress"
                                @click="updateIssueWithNotes('In Progress')">
                                Start Progress
                            </button>
                            <button v-if="selectedIssue.status === 'In Progress'" class="action-btn resolve"
                                @click="updateIssueWithNotes('Resolved')">
                                Mark Resolved
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

interface Issue {
    id: number
    scooterId: number
    userId: number
    reportedBy: string
    contactInfo?: string
    issueType: string
    description: string
    location: string
    reportDate: Date
    status: string
    photos?: string[]
    resolutionNotes?: string
}

// State variables
const issues = ref<Issue[]>([])
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('date')

// Modal state
const showDetailsModal = ref(false)
const selectedIssue = ref<Issue>({
    id: 0,
    scooterId: 0,
    userId: 0,
    reportedBy: '',
    issueType: '',
    description: '',
    location: '',
    reportDate: new Date(),
    status: ''
})
const resolutionNotes = ref('')

// Mock data
const mockIssues: Issue[] = [
    {
        id: 501,
        scooterId: 5010,
        userId: 103,
        reportedBy: 'Michael Brown',
        contactInfo: 'michael.brown@example.com',
        issueType: 'Battery',
        description: 'Scooter battery drains very quickly, only lasted about 10 minutes on a full charge.',
        location: 'Main Street',
        reportDate: new Date(Date.now() - 86400000),
        status: 'Pending',
        photos: ['/images/issue-1.jpg']
    },
    {
        id: 500,
        scooterId: 5015,
        userId: 105,
        reportedBy: 'Lisa Taylor',
        contactInfo: 'lisa.taylor@example.com',
        issueType: 'Brakes',
        description: 'Brakes are not responding well, takes too long to stop.',
        location: 'Central Park',
        reportDate: new Date(Date.now() - 172800000),
        status: 'In Progress',
        photos: ['/images/issue-2.jpg', '/images/issue-3.jpg'],
        resolutionNotes: 'Technician assigned to check brake system.'
    },
    {
        id: 499,
        scooterId: 5003,
        userId: 102,
        reportedBy: 'James Anderson',
        contactInfo: 'james.anderson@example.com',
        issueType: 'Wheels',
        description: 'Front wheel wobbles while riding, feels unsafe.',
        location: 'City Square',
        reportDate: new Date(Date.now() - 259200000),
        status: 'Resolved',
        resolutionNotes: 'Front wheel axle tightened and aligned. Test ride confirmed issue is fixed.'
    },
    {
        id: 498,
        scooterId: 5022,
        userId: 107,
        reportedBy: 'Emily White',
        contactInfo: 'emily.white@example.com',
        issueType: 'Lights',
        description: 'Headlight not working at night, difficult to see the road.',
        location: 'University Campus',
        reportDate: new Date(Date.now() - 345600000),
        status: 'Resolved',
        resolutionNotes: 'Replaced headlight bulb and checked wiring. All lights functioning properly now.'
    },
    {
        id: 497,
        scooterId: 5008,
        userId: 110,
        reportedBy: 'Daniel Lee',
        contactInfo: 'daniel.lee@example.com',
        issueType: 'Throttle',
        description: 'Throttle sticks occasionally, causing unexpected acceleration.',
        location: 'Downtown',
        reportDate: new Date(Date.now() - 432000000),
        status: 'Pending'
    }
]

// Computed properties
const filteredIssues = computed(() => {
    let result = [...issues.value]

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(issue =>
            issue.id.toString().includes(query) ||
            issue.scooterId.toString().includes(query) ||
            issue.issueType.toLowerCase().includes(query) ||
            issue.reportedBy.toLowerCase().includes(query)
        )
    }

    // Apply status filter
    if (filterStatus.value !== 'all') {
        const statusMap: Record<string, string> = {
            'pending': 'Pending',
            'in-progress': 'In Progress',
            'resolved': 'Resolved'
        }
        result = result.filter(issue =>
            issue.status === statusMap[filterStatus.value]
        )
    }

    // Apply sorting
    result.sort((a, b) => {
        if (sortBy.value === 'date') {
            return new Date(b.reportDate).getTime() - new Date(a.reportDate).getTime()
        } else if (sortBy.value === 'scooter') {
            return a.scooterId - b.scooterId
        } else if (sortBy.value === 'type') {
            return a.issueType.localeCompare(b.issueType)
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
        year: 'numeric'
    }).format(date)
}

const formatDateTime = (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date)
}

const viewIssueDetails = (id: number) => {
    const issue = issues.value.find(i => i.id === id)
    if (issue) {
        selectedIssue.value = { ...issue }
        resolutionNotes.value = issue.resolutionNotes || ''
        showDetailsModal.value = true
    }
}

const updateIssueStatus = async (id: number, newStatus: string) => {
    try {
        // In a real app, you would call the API
        // await axios.put(`/api/issues/${id}/status`, { status: newStatus })

        // For now, update the local state
        const index = issues.value.findIndex(i => i.id === id)
        if (index !== -1) {
            issues.value[index].status = newStatus
            if (newStatus === 'In Progress' && !issues.value[index].resolutionNotes) {
                issues.value[index].resolutionNotes = 'Issue being investigated.'
            }
        }
    } catch (error) {
        console.error('Failed to update issue status:', error)
    }
}

const updateIssueWithNotes = async (newStatus: string) => {
    try {
        // In a real app, you would call the API
        // await axios.put(`/api/issues/${selectedIssue.value.id}`, { 
        //   status: newStatus,
        //   resolutionNotes: resolutionNotes.value 
        // })

        // For now, update the local state
        const index = issues.value.findIndex(i => i.id === selectedIssue.value.id)
        if (index !== -1) {
            issues.value[index].status = newStatus
            issues.value[index].resolutionNotes = resolutionNotes.value
        }

        showDetailsModal.value = false
    } catch (error) {
        console.error('Failed to update issue:', error)
    }
}

onMounted(async () => {
    try {
        // In a real app, you would fetch the issues from the API
        // const response = await axios.get('/api/admin/issues')
        // issues.value = response.data

        // For now, use mock data
        issues.value = mockIssues
    } catch (error) {
        console.error('Failed to load issues:', error)
    }
})
</script>

<style scoped>
.admin-issues {
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

.status-badge.pending {
    background-color: #f39c12;
}

.status-badge.in-progress {
    background-color: #3498db;
}

.status-badge.resolved {
    background-color: #42b983;
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

.action-btn.progress {
    background-color: #f39c12;
    color: white;
}

.action-btn.resolve {
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

.issue-details {
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

.issue-description {
    background-color: #f8f9fa;
    padding: 1rem;
    border-radius: 4px;
    white-space: pre-line;
}

.photo-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 0.5rem;
    margin-top: 0.5rem;
}

.photo-item img {
    width: 100%;
    height: 100px;
    object-fit: cover;
    border-radius: 4px;
    cursor: pointer;
    transition: transform 0.3s;
}

.photo-item img:hover {
    transform: scale(1.05);
}

textarea {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    resize: vertical;
    font-family: inherit;
}

textarea:focus {
    border-color: #42b983;
    outline: none;
}

textarea:disabled {
    background-color: #f8f9fa;
    cursor: not-allowed;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1rem;
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

    .photo-gallery {
        grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    }

    .photo-item img {
        height: 80px;
    }
}
</style>
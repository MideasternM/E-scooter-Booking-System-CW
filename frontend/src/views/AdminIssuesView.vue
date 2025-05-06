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
                <option value="severity">Sort by Severity</option>
            </select>
        </div>

        <!-- Add Loading/Error/Empty States -->
        <div v-if="isLoading" class="loading-message">Loading issues...</div>
        <div v-else-if="apiError" class="error-message">{{ apiError }}</div>
        <div v-else-if="issues.length === 0" class="empty-message">No issues found.</div>

        <div v-else class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Scooter</th>
                        <th>Reported By</th>
                        <th>Issue Type</th>
                        <th>Severity</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Iterate over processed issues -->
                    <tr v-for="issue in filteredIssues" :key="issue.id">
                        <td>#{{ issue.id }}</td>
                        <td>Scooter #{{ issue.scooterId || 'N/A' }}</td>
                        <td>{{ issue.reportedByName || 'N/A' }}</td>
                        <td>{{ issue.issueType }}</td>
                        <td>
                            <span class="severity-badge" :class="(issue.severity || '').toLowerCase()">
                                {{ issue.severity || 'Unknown' }}
                            </span>
                        </td>
                        <td>{{ formatDate(issue.reportDate) }}</td>
                        <td>
                            <!-- Safe status access -->
                            <span class="status-badge" :class="(issue.status || '').toLowerCase().replace(' ', '-')">
                                {{ issue.status || 'Unknown' }}
                            </span>
                        </td>
                        <td class="actions-cell">
                            <button class="action-btn view" @click="viewIssueDetails(issue.id)">
                                View
                            </button>
                             <!-- Pass ProcessedIssue object -->
                            <button v-if="issue.status === 'Pending'" class="action-btn progress"
                                @click="updateIssueStatus(issue, 'In Progress')">
                                Start Progress
                            </button>
                            <button v-if="issue.status === 'In Progress'" class="action-btn resolve"
                                @click="updateIssueStatus(issue, 'Resolved')">
                                Mark Resolved
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- View Issue Details Modal -->
        <div v-if="showDetailsModal && selectedIssue" class="modal-overlay" @click="closeIssueDetailsModal">
            <div class="modal-container" @click.stop @mousedown.stop @mouseup.stop @touchstart.stop @touchend.stop>
                <div class="modal-header">
                    <h3>Issue Details #{{ selectedIssue.id }}</h3>
                    <button class="close-btn" @click="closeIssueDetailsModal">&times;</button>
                </div>
                <div class="modal-body">
                     <!-- Add v-if check -->
                    <div v-if="selectedIssue" class="issue-details">
                        <div class="detail-section">
                            <h4>Scooter Information</h4>
                            <p><strong>Scooter ID:</strong> #{{ selectedIssue.scooterId || 'N/A' }}</p>
                            <p><strong>Location:</strong> {{ selectedIssue.location || 'N/A' }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Reporter Information</h4>
                            <p><strong>Reported By:</strong> {{ selectedIssue.reportedByName || 'N/A' }}</p>
                            <p><strong>User ID:</strong> #{{ selectedIssue.userId || 'N/A'}}</p>
                            <!-- <p><strong>Contact:</strong> {{ selectedIssue.contactInfo || 'Not provided' }}</p> -->
                        </div>

                        <div class="detail-section">
                            <h4>Issue Information</h4>
                            <p><strong>Issue Type:</strong> {{ selectedIssue.issueType }}</p>
                            <p><strong>Severity:</strong> 
                                <span class="severity-badge" :class="(selectedIssue.severity || '').toLowerCase()">
                                    {{ selectedIssue.severity || 'Unknown' }}
                                </span>
                            </p>
                            <p><strong>Report Date:</strong> {{ formatDateTime(selectedIssue.reportDate) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge"
                                    :class="(selectedIssue.status || '').toLowerCase().replace(' ', '-')">
                                    {{ selectedIssue.status || 'Unknown' }}
                                </span>
                            </p>
                        </div>

                        <div class="detail-section">
                            <h4>Description</h4>
                            <p class="issue-description">{{ selectedIssue.description }}</p>
                        </div>

                        <!-- Photos - Assuming photos are URLs from backend -->
                         <div v-if="selectedIssue.photos && selectedIssue.photos.length > 0" class="detail-section">
                             <h4>Photos</h4>
                             <div class="photo-gallery">
                                 <div v-for="(photoUrl, index) in selectedIssue.photos" :key="index" class="photo-item">
                                     <img :src="photoUrl" :alt="`Issue photo ${index + 1}`" />
                                 </div>
                             </div>
                         </div>

                        <div class="detail-section">
                            <h4>Resolution Notes</h4>
                            <textarea v-model="resolutionNotes" placeholder="Enter resolution notes here..."
                                :disabled="selectedIssue.status === 'Resolved'" rows="4"></textarea>
                        </div>

                        <div class="form-actions" v-if="selectedIssue.status !== 'Resolved'">
                             <!-- Pass ProcessedIssue object -->
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
import { ref, computed, onMounted, watch } from 'vue' // Import watch
import { adminApi } from '../services/api'
import { parseISO, format as formatDateFn } from 'date-fns'; // Import date-fns

// Interface for API Response (based on FaultReport entity)
interface ApiIssueResponse {
    id: number;
    scooter?: { id: number; location?: string; }; // Nested scooter
    reportedBy?: { id: number; username?: string; name?: string; }; // Nested user
    faultType?: string; // Changed from issueType based on FaultReport entity
    description?: string;
    location?: string; // Location where issue occurred
    reportedAt?: string | null; // Changed from reportDate
    status?: string | null;
    resolution?: string | null; // Resolution notes from backend
    staffNotes?: string | null; // Staff notes from backend
    photos?: string[] | null; // Assuming backend provides photo URLs
    // Add other potentially relevant fields from FaultReport
    severity?: string;
    assignedStaff?: { id: number; username?: string; };
}

// Interface for internal component data
interface ProcessedIssue {
    id: number;
    scooterId?: number;
    userId?: number;
    reportedByName?: string;
    issueType: string;
    description: string;
    location?: string; // Location from scooter or report?
    reportDate: Date | null; // Store as Date object
    status: string; // Ensure always string
    severity?: string; // Added severity field
    photos?: string[] | null;
    resolutionNotes?: string | null;
    // Keep raw data if needed
    rawIssueData?: ApiIssueResponse;
}

// State variables
const issues = ref<ProcessedIssue[]>([]) // Store processed issues
const isLoading = ref(true);
const apiError = ref<string | null>(null);
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('date')

// Modal state
const showDetailsModal = ref(false)
const selectedIssue = ref<ProcessedIssue | null>(null) // Use ProcessedIssue | null
const resolutionNotes = ref('')

// Watch selectedIssue to update resolutionNotes
watch(selectedIssue, (newVal) => {
    if (newVal) {
        // Prefer staffNotes from backend if available, otherwise resolution
        resolutionNotes.value = newVal.rawIssueData?.staffNotes || newVal.rawIssueData?.resolution || '';
    } else {
        resolutionNotes.value = '';
    }
});

// Computed properties
const filteredIssues = computed(() => {
    let result = [...issues.value];

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(issue =>
            issue.id.toString().includes(query) ||
            issue.scooterId?.toString().includes(query) ||
            issue.issueType.toLowerCase().includes(query) ||
            (issue.reportedByName || '').toLowerCase().includes(query) ||
            (issue.severity || '').toLowerCase().includes(query)
        );
    }

    if (filterStatus.value !== 'all') {
        // Map filter values if needed (e.g., pending -> Pending)
        const targetStatus = filterStatus.value.replace('-', ' '); // Convert in-progress to In Progress
         result = result.filter(issue =>
             (issue.status || '').toLowerCase() === targetStatus.toLowerCase()
         );
    }

    result.sort((a, b) => {
        if (sortBy.value === 'date') {
            return (b.reportDate?.getTime() ?? 0) - (a.reportDate?.getTime() ?? 0);
        } else if (sortBy.value === 'scooter') {
            return (a.scooterId ?? 0) - (b.scooterId ?? 0);
        } else if (sortBy.value === 'type') {
            return a.issueType.localeCompare(b.issueType);
        } else if (sortBy.value === 'severity') {
            // Sort severity: HIGH > MEDIUM > LOW
            const severityOrder: Record<string, number> = { 'high': 3, 'medium': 2, 'low': 1, 'unknown': 0 };
            const severityA = a.severity?.toLowerCase() || 'unknown';
            const severityB = b.severity?.toLowerCase() || 'unknown';
            return (severityOrder[severityB] || 0) - (severityOrder[severityA] || 0);
        }
        return 0;
    });

    return result;
});

// --- API Interaction --- 
const fetchIssues = async () => {
    isLoading.value = true;
    apiError.value = null;
    try {
        const response = await adminApi.getAllIssues();
        issues.value = response.data.map((raw: ApiIssueResponse): ProcessedIssue => {
            let parsedDate: Date | null = null;
            try { parsedDate = raw.reportedAt ? parseISO(raw.reportedAt) : null; } catch (e) { console.error("Error parsing reportedAt:", raw.reportedAt, e); }

            return {
                id: raw.id,
                scooterId: raw.scooter?.id,
                userId: raw.reportedBy?.id,
                reportedByName: raw.reportedBy?.username || raw.reportedBy?.name || 'Unknown User',
                issueType: raw.faultType || 'Unknown Type', // Map faultType to issueType
                description: raw.description || 'No description',
                location: raw.location || raw.scooter?.location || 'Unknown Location',
                reportDate: parsedDate,
                status: raw.status || 'Unknown',
                severity: raw.severity || 'Unknown', // Map severity from API
                photos: raw.photos,
                resolutionNotes: raw.resolution || raw.staffNotes,
                rawIssueData: raw
            };
        });
    } catch (error: any) {
        console.error('Failed to fetch issues:', error);
        apiError.value = `Failed to load issues: ${error.message || 'Unknown error'}`;
        issues.value = [];
    } finally {
        isLoading.value = false;
    }
};

const updateIssueStatus = async (issue: ProcessedIssue, newStatus: string, notes?: string) => {
    try {
        await adminApi.updateIssueStatus(issue.id, newStatus, notes);
        // Update local state
        const index = issues.value.findIndex(i => i.id === issue.id);
        if (index !== -1) {
            issues.value[index].status = newStatus;
            // Optionally update notes if provided
            if(notes) issues.value[index].resolutionNotes = notes;
            if (issues.value[index].rawIssueData) {
                 issues.value[index].rawIssueData!.status = newStatus;
                 if(notes) issues.value[index].rawIssueData!.staffNotes = notes; // Assuming notes map to staffNotes
            }
        }
        // Also update selected issue if it's the one being modified
        if (selectedIssue.value && selectedIssue.value.id === issue.id) {
             selectedIssue.value.status = newStatus;
             if(notes) selectedIssue.value.resolutionNotes = notes;
             if (selectedIssue.value.rawIssueData) {
                  selectedIssue.value.rawIssueData.status = newStatus;
                  if(notes) selectedIssue.value.rawIssueData.staffNotes = notes;
             }
        }
        alert(`Issue #${issue.id} status updated to ${newStatus}.`);
    } catch (error: any) {
        console.error(`Failed to update issue ${issue.id} status:`, error);
        alert(`Failed to update status: ${error.response?.data?.message || error.message}`);
    }
};

// Called from modal when updating with notes
const updateIssueWithNotes = async (newStatus: string) => {
    if (!selectedIssue.value) return;
    await updateIssueStatus(selectedIssue.value, newStatus, resolutionNotes.value);
    closeIssueDetailsModal();
};

// --- Modal Control --- 
const viewIssueDetails = (issueId: number) => {
    try {
        const issue = issues.value.find(i => i.id === issueId);
        if (issue) {
            // 创建副本以避免引用问题
            selectedIssue.value = JSON.parse(JSON.stringify(issue));
            
            // 阻止页面滚动
            document.body.classList.add('modal-open');
            showDetailsModal.value = true;
        } else {
            console.warn(`在当前列表中未找到问题 ${issueId} 的详细信息。`);
            alert('无法找到问题详情。');
        }
    } catch (error) {
        console.error('查看问题详情时出错:', error);
        alert('无法显示问题详情。');
    }
};

// 添加关闭模态框的函数
const closeIssueDetailsModal = () => {
    showDetailsModal.value = false;
    document.body.classList.remove('modal-open');
};

// --- Formatting --- 
const formatDate = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    try {
        return formatDateFn(dateObj, 'MMM d, yyyy');
    } catch (error) { return 'Error'; }
};

const formatDateTime = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    try {
        return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm a');
    } catch (error) { return 'Error'; }
};

// Lifecycle hook
onMounted(() => {
    fetchIssues();
});

</script>

<style scoped>
/* Add styles for loading/error/empty messages */
.loading-message,
.error-message,
.empty-message {
    padding: 2rem;
    text-align: center;
    color: #606f7b;
    background-color: #f8f9fa;
    border-radius: 8px;
    margin-bottom: 1.5rem;
}
.error-message {
    color: #e74c3c;
    background-color: #fceded;
    border: 1px solid #e74c3c;
}

/* General Styles */
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
    flex-wrap: wrap;
    gap: 1rem;
    margin-bottom: 1.5rem;
    padding: 1rem;
    background-color: white;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
}

.filters .search-box,
.filters select {
    flex-grow: 1;
    min-width: 150px;
}

.table-container {
    width: 100%;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    border: 1px solid var(--border-color, #eee);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    margin-bottom: 1rem;
}

.data-table {
    width: 100%;
    min-width: 750px; /* Adjust based on columns */
    border-collapse: collapse;
    background-color: var(--card-bg, white);
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

/* Status Badge Styles */
.status-badge {
    display: inline-block;
    padding: 0.3rem 0.6rem;
    border-radius: 1rem;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    background-color: #e2e8f0;
    color: #64748b;
}

.status-badge.pending { background-color: #feb2b2; color: #e53e3e; }
.status-badge.in-progress { background-color: #bfdbfe; color: #3b82f6; }
.status-badge.resolved { background-color: #bbf7d0; color: #22c55e; }

/* Severity Badge Styles */
.severity-badge {
    display: inline-block;
    padding: 0.25rem 0.5rem;
    border-radius: 0.5rem;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    text-align: center;
    min-width: 5rem;
}

.severity-badge.high {
    background-color: #fee2e2;
    color: #dc2626;
    border: 1px solid #ef4444;
}

.severity-badge.medium {
    background-color: #fef3c7;
    color: #d97706;
    border: 1px solid #f59e0b;
}

.severity-badge.low {
    background-color: #dcfce7;
    color: #16a34a;
    border: 1px solid #22c55e;
}

.severity-badge.unknown {
    background-color: #e2e8f0;
    color: #64748b;
    border: 1px solid #94a3b8;
}

.actions-cell {
    display: flex;
    gap: 0.5rem;
    white-space: nowrap;
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
    background-color: #2ecc71;
    color: white;
}

.action-btn.resolve {
    background-color: #1abc9c;
    color: white;
}

.action-btn:hover {
    opacity: 0.9;
}

/* Modal Styles (Copied from AdminDashboardView) */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999; /* Keep high z-index */
    visibility: visible;
    opacity: 1;
    padding: 1rem;
}

.modal-container {
    width: 100%;
    max-width: 700px; /* Allow wider for issue details */
    max-height: 90vh;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 10000; /* Higher than overlay */
    visibility: visible;
    opacity: 1;
    margin: auto;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #f1f1f1;
    color: #2c3e50;
    flex-shrink: 0;
}
.modal-header h3 { margin: 0; font-weight: 600; }
.close-btn {
    background: none; border: none; font-size: 1.5rem; cursor: pointer;
    color: #606f7b; padding: 0; line-height: 1; transition: color 0.2s;
}
.close-btn:hover { color: #ef4444; }

.modal-body {
    padding: 1.25rem;
    color: #2c3e50;
    overflow-y: auto; /* Enable vertical scroll within body */
    flex-grow: 1;
}

/* Specific details styling */
.issue-details {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

.detail-section {
    background-color: #f9fafb;
    padding: 1rem;
    border-radius: var(--radius-md);
}

.detail-section h4 {
    margin-bottom: 0.75rem;
}

.detail-section p {
    margin: 0.4rem 0;
    line-height: 1.6;
}

.issue-description {
    white-space: pre-wrap; /* Allow description to wrap */
    background-color: var(--neutral-100);
    padding: 0.75rem;
    border-radius: var(--radius-sm);
    font-size: 0.9rem;
}

.photo-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 0.75rem;
    margin-top: 0.5rem;
}

.photo-item img {
    width: 100%;
    height: auto;
    aspect-ratio: 1 / 1;
    object-fit: cover;
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color, #eee);
}

.detail-section textarea {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid var(--border-color, #eee);
    border-radius: var(--radius-md);
    resize: vertical;
    min-height: 80px;
}

.issue-details .form-actions {
    grid-column: 1 / -1;
    justify-content: flex-start;
    border-top: 1px solid var(--border-color, #eee);
    padding-top: 1rem;
    margin-top: 0.5rem;
}

/* Responsive Adjustments */
@media (max-width: 992px) {
    .filters {
        padding: 0.75rem;
    }
    .data-table {
        min-width: 650px;
    }
    .modal-container {
        max-width: 600px;
    }
    .issue-details {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .page-header h1 {
        font-size: 1.5rem;
    }
    .filters {
        padding: 0.5rem;
        gap: 0.5rem;
    }
    .filters .search-box input,
    .filters select {
        font-size: 0.9rem;
        padding: 0.5rem 0.75rem;
    }

    .table-container {
        margin-bottom: 1rem;
    }
    .data-table {
        min-width: 550px;
    }
    .data-table th,
    .data-table td {
        padding: 0.6rem 0.5rem;
        font-size: 0.85rem;
    }
    .status-badge {
        font-size: 0.75rem;
        padding: 0.2rem 0.5rem;
    }
    .actions-cell .action-btn {
        padding: 0.3rem 0.6rem;
        font-size: 0.75rem;
        margin-right: 0.25rem;
    }

    .modal-header h3 {
        font-size: 1.1rem;
    }
    .modal-body {
        padding: 1rem;
    }
    .issue-details {
        gap: 1rem;
    }
    .detail-section {
        padding: 0.75rem;
    }
    .detail-section h4 {
        font-size: 1rem;
    }
    .detail-section p {
        font-size: 0.9rem;
    }
    .photo-gallery {
        grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
        gap: 0.5rem;
    }
    .form-actions {
        padding: 0.75rem;
        gap: 0.5rem;
    }
    .form-actions button {
        font-size: 0.85rem;
    }
}

@media (max-width: 576px) {
    .filters {
        flex-direction: column;
        align-items: stretch;
    }
    .filters .search-box,
    .filters select {
        min-width: 100%;
    }
    .data-table th,
    .data-table td {
        white-space: nowrap;
    }
}
</style>
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

        <!-- Add Loading/Error/Empty States -->
        <div v-if="isLoading" class="loading-message">Loading users...</div>
        <div v-else-if="apiError" class="error-message">{{ apiError }}</div>
        <div v-else-if="users.length === 0" class="empty-message">No users found.</div>

        <div v-else class="table-container">
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
                    <!-- Iterate over processed users -->
                    <tr v-for="user in filteredUsers" :key="user.id">
                        <td>#{{ user.id }}</td>
                        <!-- Use ?? for potentially missing name -->
                        <td>{{ user.name ?? 'N/A' }}</td>
                        <td>{{ user.email }}</td>
                        <td>{{ user.phone ?? 'N/A' }}</td>
                        <td>{{ formatUserJoinDate(user.joinDate) }}</td>
                        <td>
                             <!-- Safe access to status -->
                            <span class="status-badge" :class="(user.status || '').toLowerCase()">
                                {{ user.status || 'Unknown' }}
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
        <!-- Add check for selectedUser -->
        <div v-if="showDetailsModal && selectedUser" class="modal-overlay" @click="closeUserDetailsModal">
            <div class="modal-container" @click.stop @mousedown.stop @mouseup.stop @touchstart.stop @touchend.stop>
                <div class="modal-header">
                    <h3>User Details #{{ selectedUser.id }}</h3>
                    <button class="close-btn" @click="closeUserDetailsModal">&times;</button>
                </div>
                <div class="modal-body">
                    <!-- Wrap content in v-if -->
                    <div v-if="selectedUser" class="user-details">
                        <div class="detail-section">
                            <h4>Personal Information</h4>
                            <p><strong>Name:</strong> {{ selectedUser.name ?? 'N/A' }}</p>
                            <p><strong>Email:</strong> {{ selectedUser.email }}</p>
                            <p><strong>Phone:</strong> {{ selectedUser.phone ?? 'N/A' }}</p>
                            <p><strong>Address:</strong> {{ selectedUser.address || 'Not provided' }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Account Information</h4>
                            <p><strong>User ID:</strong> #{{ selectedUser.id }}</p>
                            <p><strong>Username:</strong> {{ selectedUser.username }}</p>
                            <p><strong>Join Date:</strong> {{ formatDateTime(selectedUser.joinDate) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge" :class="(selectedUser.status || '').toLowerCase()">
                                    {{ selectedUser.status || 'Unknown' }}
                                </span>
                            </p>
                            <p v-if="selectedUser.lastLogin"><strong>Last Login:</strong> {{
                                formatDateTime(selectedUser.lastLogin) }}</p>
                        </div>

                        <!-- Activity Summary - Use processed data or raw data safely -->
                        <div class="detail-section">
                            <h4>Activity Summary</h4>
                            <p><strong>Total Bookings:</strong> {{ selectedUser.rawUserData?.bookingCount || 0 }}</p>
                            <p><strong>Completed Rides:</strong> {{ selectedUser.rawUserData?.completedRides || 0 }}</p>
                            <p><strong>Cancelled Bookings:</strong> {{ selectedUser.rawUserData?.cancelledBookings || 0 }}</p>
                            <p><strong>Total Spent:</strong> ${{ selectedUser.rawUserData?.totalSpent?.toFixed(2) || '0.00' }}</p>
                            <p><strong>Reported Issues:</strong> {{ selectedUser.rawUserData?.reportedIssues || 0 }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Notes</h4>
                            <!-- Use v-model safely -->
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
        <!-- Add check for suspendingUser -->
        <div v-if="showSuspendModal && suspendingUser" class="modal-overlay">
            <div class="modal-container delete-modal">
                <div class="modal-header">
                    <h3>Confirm Suspension</h3>
                    <button class="close-btn" @click="showSuspendModal = false">&times;</button>
                </div>
                <div class="modal-body">
                     <!-- Wrap content in v-if -->
                    <div v-if="suspendingUser">
                        <p>Are you sure you want to suspend user {{ suspendingUser.name ?? suspendingUser.username }}?</p>
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
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../services/api'
import { parseISO, format as formatDateFn } from 'date-fns'; // Import date-fns

// Interface for API response (adjust based on actual API)
interface ApiUserResponse {
    id: number;
    username: string;
    name?: string | null; // Name might be optional
    email: string;
    phoneNumber?: string | null; // Use actual field name from API
    address?: string | null;
    createdAt: string; // Expect string from API
    status?: string | null;
    lastLogin?: string | null;
    notes?: string | null;
    // Fields likely needed for detail view / activity summary
    bookingCount?: number;
    completedRides?: number;
    cancelledBookings?: number;
    totalSpent?: number;
    reportedIssues?: number;
}

// Interface for component's internal state
interface ProcessedUser {
    id: number;
    username: string;
    name?: string | null;
    email: string;
    phone?: string | null;
    address?: string | null;
    joinDate: Date | null; // Store as Date object
    status: string; // Ensure status is always string (e.g., 'Unknown')
    lastLogin?: Date | null;
    notes?: string | null;
    // Keep raw data for details modal
    rawUserData?: ApiUserResponse;
}

// State variables
const users = ref<ProcessedUser[]>([]) // Store processed users
const isLoading = ref(true);
const apiError = ref<string | null>(null);
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('id')

// Modal state
const showDetailsModal = ref(false)
const showSuspendModal = ref(false)
const selectedUser = ref<ProcessedUser | null>(null) // Use ProcessedUser | null
const userNotes = ref('')
const suspendingUser = ref<ProcessedUser | null>(null) // Use ProcessedUser | null
const suspensionReason = ref('')

// Computed properties
const filteredUsers = computed(() => {
    let result = [...users.value];

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(user =>
            user.id.toString().includes(query) ||
            (user.name || '').toLowerCase().includes(query) || // Safe access
            user.email.toLowerCase().includes(query)
        );
    }

    if (filterStatus.value !== 'all') {
        // Ensure status exists before filtering
        result = result.filter(user => (user.status || '').toLowerCase() === filterStatus.value);
    }

    result.sort((a, b) => {
        switch (sortBy.value) {
            case 'name':
                // Safe compare for potentially null names
                return (a.name ?? '').localeCompare(b.name ?? '');
            case 'date':
                // Safe compare for potentially null dates
                return (b.joinDate?.getTime() ?? 0) - (a.joinDate?.getTime() ?? 0);
            default: // id
                return a.id - b.id;
        }
    });

    return result;
});

// Methods
const fetchUsers = async () => {
    isLoading.value = true;
    apiError.value = null;
    try {
        const response = await adminApi.getAllUsers();
        users.value = response.data.map((rawUser: ApiUserResponse): ProcessedUser => {
            let parsedJoinDate: Date | null = null;
            let parsedLastLogin: Date | null = null;
            try { parsedJoinDate = rawUser.createdAt ? parseISO(rawUser.createdAt) : null; } catch(e) { console.error("Error parsing joinDate:", rawUser.createdAt, e); }
            try { parsedLastLogin = rawUser.lastLogin ? parseISO(rawUser.lastLogin) : null; } catch(e) { console.error("Error parsing lastLogin:", rawUser.lastLogin, e); }

            return {
                id: rawUser.id,
                username: rawUser.username,
                name: rawUser.name,
                email: rawUser.email,
                phone: rawUser.phoneNumber, // Assuming API returns phoneNumber
                address: rawUser.address,
                joinDate: parsedJoinDate,
                status: rawUser.status || 'Unknown', // Provide default status
                lastLogin: parsedLastLogin,
                notes: rawUser.notes,
                rawUserData: rawUser // Store raw data
            };
        });
    } catch (error: any) {
        console.error('Error fetching users:', error);
        apiError.value = `Failed to load users: ${error.message || 'Unknown error'}`;
        users.value = [];
    } finally {
        isLoading.value = false;
    }
};

const viewUserDetails = async (userId: number) => {
    try {
        // 首先找到已处理的用户数据
        const user = users.value.find(u => u.id === userId);
        if (user) {
            // 使用已处理的数据
            selectedUser.value = {...user}; // 创建一个副本以防止引用问题
            userNotes.value = user.notes || '' // 从处理后的数据中获取备注
            
            // 显示模态框并防止页面滚动
            document.body.classList.add('modal-open');
            showDetailsModal.value = true;
        } else {
            // 可选: 如果在列表中未找到（例如，过滤后），则直接获取详细信息
            console.log(`获取列表中未找到的用户 ${userId} 的详细信息。`);
            const response = await adminApi.getUserDetails(userId);
            let parsedJoinDate: Date | null = null;
            let parsedLastLogin: Date | null = null;
            try { 
                parsedJoinDate = response.data.createdAt ? parseISO(response.data.createdAt) : null; 
            } catch(e) { 
                console.error("解析joinDate出错:", response.data.createdAt, e); 
            }
            try { 
                parsedLastLogin = response.data.lastLogin ? parseISO(response.data.lastLogin) : null; 
            } catch(e) { 
                console.error("解析lastLogin出错:", response.data.lastLogin, e); 
            }

            selectedUser.value = {
                id: response.data.id,
                username: response.data.username,
                name: response.data.name,
                email: response.data.email,
                phone: response.data.phoneNumber,
                address: response.data.address,
                joinDate: parsedJoinDate,
                status: response.data.status || 'Unknown',
                lastLogin: parsedLastLogin,
                notes: response.data.notes,
                rawUserData: response.data // 存储原始数据
            };
            userNotes.value = selectedUser.value?.notes || '';
            
            // 显示模态框并防止页面滚动
            document.body.classList.add('modal-open');
            showDetailsModal.value = true;
        }
    } catch (error) {
        console.error('获取用户详细信息时出错:', error);
        alert('无法加载用户详细信息。');
    }
};

const saveUserNotes = async () => {
    if (!selectedUser.value) return;
    try {
        await adminApi.saveUserNotes(selectedUser.value.id, userNotes.value);
        // Update local data if necessary
        const index = users.value.findIndex(u => u.id === selectedUser.value!.id)
        if (index !== -1) {
            users.value[index].notes = userNotes.value;
            if (users.value[index].rawUserData) {
                users.value[index].rawUserData!.notes = userNotes.value;
            }
        }
        alert('Notes saved successfully.')
    } catch (error) {
        console.error('Error saving user notes:', error)
        alert('Failed to save notes.')
    }
};

const confirmSuspend = (user: ProcessedUser) => {
    suspendingUser.value = user;
    suspensionReason.value = '' // Clear previous reason
    showSuspendModal.value = true;
};

const suspendUser = async () => {
    if (!suspendingUser.value || !suspensionReason.value) {
        alert('Please provide a reason for suspension.')
        return;
    }
    try {
        await adminApi.suspendUser(suspendingUser.value.id, suspensionReason.value);
        // Update user status locally
        const index = users.value.findIndex(u => u.id === suspendingUser.value!.id)
        if (index !== -1) {
            users.value[index].status = 'Suspended';
             if (users.value[index].rawUserData) {
                users.value[index].rawUserData!.status = 'Suspended';
            }
        }
        showSuspendModal.value = false;
        alert('User suspended successfully.')
    } catch (error) {
        console.error('Error suspending user:', error)
        alert('Failed to suspend user.')
    }
};

const activateUser = async (userId: number) => {
    try {
        await adminApi.activateUser(userId);
        // Update user status locally
        const index = users.value.findIndex(u => u.id === userId)
        if (index !== -1) {
            users.value[index].status = 'Active'; // Or whatever the active status is called
            if (users.value[index].rawUserData) {
                users.value[index].rawUserData!.status = 'Active';
            }
        }
         // If the activated user is the selected user in modal, update modal too
        if (selectedUser.value && selectedUser.value.id === userId) {
            selectedUser.value.status = 'Active';
        }
        alert('User activated successfully.')
    } catch (error) {
        console.error('Error activating user:', error)
        alert('Failed to activate user.')
    }
};

// Rename formatDate specifically for the table
const formatUserJoinDate = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    try {
        return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm a');
    } catch (error) {
        console.error('Error formatting date:', dateObj, error);
        return 'Error';
    }
};

// Keep formatDateTime for the modal
const formatDateTime = (dateObj: Date | null | undefined) => {
     if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm:ss a');
};

// 添加一个新函数用于关闭模态框
const closeUserDetailsModal = () => {
    showDetailsModal.value = false;
    document.body.classList.remove('modal-open');
};

// Lifecycle hook
onMounted(() => {
    fetchUsers();
});

</script>

<style scoped>
/* Add styles for loading/error/empty messages if needed */
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

/* Rest of the styles from previous component, adjust as needed */
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
    background-color: #bdc3c7;
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
    background-color: #e67e22;
    color: white;
}

.action-btn.activate {
    background-color: #2ecc71;
    color: white;
}

.action-btn:hover {
    opacity: 0.9;
}

/* Modal Styles (Copied from AdminDashboardView & AdminScootersView) */
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
}

.modal-container {
    width: 90%;
    max-width: 600px; /* Standard width for details */
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
    /* overflow: hidden; */ /* Let modal-body handle scroll */
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

/* Specific User Details styling */
.user-details {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
}

.detail-section h4 {
    margin: 0 0 0.75rem;
    color: #8b5cf6; /* Example color for users */
    font-size: 1.1rem;
    font-weight: 600;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.5rem;
}

.detail-section p {
    margin: 0.4rem 0;
    line-height: 1.6;
}

.detail-section textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    box-sizing: border-box;
    font-size: 1rem;
    margin-top: 0.5rem;
    transition: border-color 0.2s, box-shadow 0.2s;
}
.detail-section textarea:focus {
    outline: none;
    border-color: var(--primary-500);
    box-shadow: 0 0 0 3px rgba(var(--primary-500-rgb), 0.2);
}

.save-notes-btn {
    padding: 0.5rem 1rem;
    background-color: var(--primary-500);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    margin-top: 0.5rem;
    transition: background-color 0.2s;
}
.save-notes-btn:hover {
    background-color: var(--primary-600);
}


/* Styles for suspend confirmation modal */
.delete-modal .modal-header {
    background-color: #fee2e2; /* Light red header */
    color: #b91c1c; /* Darker red text */
}

.delete-modal .modal-body p {
    margin-bottom: 0.75rem;
}

.delete-modal .form-group {
    margin-bottom: 1rem;
}

.delete-modal .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1.5rem;
}

.delete-modal .cancel-btn {
    background-color: #e5e7eb;
    color: #374151;
    padding: 0.6rem 1.2rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}
.delete-modal .cancel-btn:hover {
    background-color: #d1d5db;
}

.delete-modal .delete-btn {
    background-color: #ef4444;
    color: white;
    padding: 0.6rem 1.2rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}
.delete-modal .delete-btn:hover {
    background-color: #dc2626;
}

.form-actions {
    display: flex;
    justify-content: flex-end; /* Align actions to the right */
    gap: 0.75rem;
    margin-top: 1.5rem;
    padding-top: 1rem;
    border-top: 1px solid #eee;
}

.action-btn.suspend {
    background-color: #f59e0b; /* Orange */
    color: white;
}

.action-btn.suspend:hover {
    background-color: #d97706;
}

.action-btn.activate {
    background-color: #10b981; /* Green */
    color: white;
}

.action-btn.activate:hover {
    background-color: #059669;
}
/* --- End Modal Styles --- */

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
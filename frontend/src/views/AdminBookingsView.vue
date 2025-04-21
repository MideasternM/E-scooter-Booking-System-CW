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

        <!-- Add Loading/Error/Empty States -->
        <div v-if="isLoading" class="loading-message">Loading bookings...</div>
        <div v-else-if="apiError" class="error-message">{{ apiError }}</div>
        <div v-else-if="bookings.length === 0" class="empty-message">No bookings found.</div>

        <div v-else class="table-container">
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
                        <td>#{{ booking.scooterId }}</td>
                        <td>{{ formatDate(booking.startTime) }}</td>
                        <td>{{ booking.endTime ? formatDate(booking.endTime) : '-' }}</td>
                        <td>
                            <span class="status-badge" :class="(booking.status || '').toLowerCase()">
                                {{ booking.status || 'Unknown' }}
                            </span>
                        </td>
                        <td>${{ (booking.amount ?? 0).toFixed(2) }}</td>
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
        <div v-if="showDetailsModal && selectedBooking" class="modal-overlay" @click="closeDetailsModal">
            <div class="modal-container" @click.stop>
                <div class="modal-header">
                    <h3>Booking Details #{{ selectedBooking.id }}</h3>
                    <button class="close-btn" @click="closeDetailsModal">&times;</button>
                </div>
                <div class="modal-body">
                    <div v-if="selectedBooking" class="booking-details">
                        <div class="detail-section">
                            <h4>User Information</h4>
                            <p><strong>Name:</strong> {{ selectedBooking.userName }}</p>
                            <p><strong>User ID:</strong> {{ selectedBooking.userId }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Scooter Information</h4>
                            <p><strong>ID:</strong> #{{ selectedBooking.scooterId }}</p>
                            <p><strong>Location:</strong> {{ selectedBooking.location }}</p>
                        </div>

                        <div class="detail-section">
                            <h4>Booking Information</h4>
                            <p><strong>Start Time:</strong> {{ formatDateTime(selectedBooking.startTime) }}</p>
                            <p><strong>End Time:</strong> {{ selectedBooking.endTime ?
                                formatDateTime(selectedBooking.endTime) : 'Not ended yet' }}</p>
                            <p><strong>Duration:</strong> {{ calculateDuration(selectedBooking) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge" :class="(selectedBooking.status || '').toLowerCase()">
                                    {{ selectedBooking.status || 'Unknown' }}
                                </span>
                            </p>
                        </div>

                        <div class="detail-section">
                            <h4>Payment Information</h4>
                            <p><strong>Base Rate:</strong> ${{ selectedBooking.rawBookingData?.payment?.baseRate?.toFixed(2) || 'N/A' }}</p>
                            <p><strong>Additional Charges:</strong> ${{ selectedBooking.rawBookingData?.payment?.additionalCharges?.toFixed(2) || 'N/A' }}</p>
                            <p><strong>Total Amount:</strong> ${{ selectedBooking.amount.toFixed(2) }}</p>
                            <p><strong>Payment Status:</strong> {{ selectedBooking.rawBookingData?.payment?.status || 'N/A' }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Cancel Booking Confirmation Modal -->
        <div v-if="showCancelModal && cancellingBooking" class="modal-overlay" @click="closeCancelModal">
            <div class="modal-container delete-modal" @click.stop>
                <div class="modal-header">
                    <h3>Confirm Cancellation</h3>
                    <button class="close-btn" @click="closeCancelModal">&times;</button>
                </div>
                <div class="modal-body">
                    <div v-if="cancellingBooking">
                        <p>Are you sure you want to cancel Booking #{{ cancellingBooking.id }}?</p>
                        <p class="warning">This action cannot be undone.</p>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="closeCancelModal">No, Keep
                                Booking</button>
                            <button type="button" class="delete-btn" @click="cancelBooking">Yes, Cancel Booking</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../services/api'
import { parseISO, format as formatDateFn } from 'date-fns'

// Define a more accurate interface based on expected API response
interface ApiBookingResponse {
    id: number;
    status: string;
    startTime: string; // Expect string from API
    endTime?: string | null; // Expect string from API
    createdAt: string;
    user?: { id: number; username?: string; name?: string; };
    scooter?: { id: number; scooterCode?: string; location?: string };
    payment?: {
        amount?: number;
        status?: string;
        baseRate?: number;
        additionalCharges?: number;
        completedAt?: string | Date;
    };
}

// Define the structure used within the component
interface ProcessedBooking {
    id: number;
    userId: number;
    userName: string;
    scooterId: number;
    location: string;
    startTime: Date | null; // Store as Date object
    endTime?: Date | null; // Store as Date object
    status: string;
    amount: number;
    // Keep other relevant raw data if needed for modals?
    rawBookingData?: ApiBookingResponse;
}

// State variables
const bookings = ref<ProcessedBooking[]>([]) // Store processed bookings
const isLoading = ref(true); // Loading state
const apiError = ref<string | null>(null); // Error state
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('date')

// Modal state
const showDetailsModal = ref(false)
const showCancelModal = ref(false)
const selectedBooking = ref<ProcessedBooking | null>(null) // Use ProcessedBooking
const cancellingBooking = ref<ProcessedBooking | null>(null)

const router = useRouter()

// Computed properties
const filteredBookings = computed(() => {
    let result = [...bookings.value];

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(booking =>
            booking.id.toString().includes(query) ||
            booking.userName.toLowerCase().includes(query) ||
            booking.location.toLowerCase().includes(query)
        )
    }

    if (filterStatus.value !== 'all') {
        result = result.filter(booking =>
            (booking.status || '').toLowerCase() === filterStatus.value
        );
    }

    result.sort((a, b) => {
        const startTimeA = a.startTime?.getTime() ?? 0;
        const startTimeB = b.startTime?.getTime() ?? 0;
        const endTimeA = a.endTime?.getTime() ?? 0;
        const endTimeB = b.endTime?.getTime() ?? 0;

        if (sortBy.value === 'date') {
            return startTimeB - startTimeA;
        } else if (sortBy.value === 'duration') {
            const durationA = endTimeA && startTimeA ? endTimeA - startTimeA : -1; // Handle nulls
            const durationB = endTimeB && startTimeB ? endTimeB - startTimeB : -1;
            // Sort ongoing (-1) last, then by duration descending
            if (durationA === -1 && durationB === -1) return 0;
            if (durationA === -1) return 1;
            if (durationB === -1) return -1;
            return durationB - durationA;
        } else if (sortBy.value === 'cost') {
            return (b.amount ?? 0) - (a.amount ?? 0);
        }
        return 0;
    });

    return result;
});

// Functions related to API interaction
const fetchBookings = async () => {
    isLoading.value = true;
    apiError.value = null;
    try {
        const response = await adminApi.getAllBookings();
        // Process the raw API data
        bookings.value = response.data.map((rawBooking: ApiBookingResponse): ProcessedBooking => {
            let parsedStartTime: Date | null = null;
            let parsedEndTime: Date | null = null;
            try { parsedStartTime = rawBooking.startTime ? parseISO(rawBooking.startTime) : null; } catch(e) { console.error("Error parsing start time:", rawBooking.startTime, e);}
            try { parsedEndTime = rawBooking.endTime ? parseISO(rawBooking.endTime) : null; } catch(e) { console.error("Error parsing end time:", rawBooking.endTime, e); }

            return {
                id: rawBooking.id,
                userId: rawBooking.user?.id ?? 0,
                userName: rawBooking.user?.username || rawBooking.user?.name || 'Unknown User',
                scooterId: rawBooking.scooter?.id ?? 0,
                location: rawBooking.scooter?.location || 'Unknown Location',
                startTime: parsedStartTime,
                endTime: parsedEndTime,
                status: rawBooking.status || 'Unknown',
                amount: rawBooking.payment?.amount ?? 0,
                rawBookingData: rawBooking // Keep raw data if needed by modals
            };
        });
    } catch (error: any) {
        console.error('Failed to fetch bookings:', error);
        apiError.value = `Failed to load bookings: ${error.message || 'Unknown error'}`;
        // alert('Failed to load bookings.'); // Replaced by error message display
        bookings.value = []; // Clear bookings on error
    } finally {
        isLoading.value = false;
    }
};

// Methods
const formatDate = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    try {
        return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm a');
    } catch (error) {
        console.error('Error formatting date:', dateObj, error);
        return 'Error';
    }
};

const formatDateTime = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    // Use a more detailed format for the modal?
    return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm:ss a');
};

const calculateDuration = (booking: ProcessedBooking | null): string => {
    if (!booking || !booking.endTime || !booking.startTime) return 'N/A';
    try {
        // Using date-fns formatDistanceStrict might be better here too
        const durationMs = booking.endTime.getTime() - booking.startTime.getTime();
        if (durationMs < 0) return 'Invalid';
        const hours = Math.floor(durationMs / (1000 * 60 * 60))
        const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60))
        return `${hours}h ${minutes}m`;
    } catch (error) {
        return 'Error';
    }
};

const viewBookingDetails = (bookingId: number) => {
    try {
        // 创建选定预订的副本，避免引用问题
        selectedBooking.value = JSON.parse(JSON.stringify(
            bookings.value.find(b => b.id === bookingId)
        )) || null;
        
        if (selectedBooking.value) {
            // 添加body类以防止滚动
            document.body.classList.add('modal-open');
            console.log("为模态框选择的预订:", selectedBooking.value);
            showDetailsModal.value = true;
        } else {
            alert('找不到预订详情。');
        }
    } catch (error) {
        console.error('查看预订详情时出错:', error);
        alert('无法显示预订详情。');
    }
};

const confirmCancel = (booking: ProcessedBooking) => {
    // 创建副本
    cancellingBooking.value = JSON.parse(JSON.stringify(booking));
    document.body.classList.add('modal-open');
    showCancelModal.value = true;
};

// 添加用于关闭模态框的函数
const closeDetailsModal = () => {
    showDetailsModal.value = false;
    document.body.classList.remove('modal-open');
};

const closeCancelModal = () => {
    showCancelModal.value = false;
    document.body.classList.remove('modal-open');
};

const cancelBooking = async () => {
    if (!cancellingBooking.value) return;
    try {
        await adminApi.cancelBooking(cancellingBooking.value.id);
        alert(`预订 #${cancellingBooking.value.id} 已成功取消。`);
        closeCancelModal();
        cancellingBooking.value = null;
        fetchBookings(); // 刷新列表
    } catch (error: any) {
        console.error('取消预订失败:', error);
        alert(`取消预订失败: ${error.message || '未知错误'}`);
    }
};

// Lifecycle hook
onMounted(() => {
    fetchBookings();
});
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
}

.modal-container {
    width: 90%;
    max-width: 700px; /* Wider modal for details */
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

/* Specific details styling */
.booking-details {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
}

.detail-section h4 {
    margin: 0 0 0.75rem;
    color: #3b82f6; /* Example color for bookings */
    font-size: 1.1rem;
    font-weight: 600;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.5rem;
}

.detail-section p {
    margin: 0.4rem 0;
    line-height: 1.6;
}

/* Styles for delete confirmation modal */
.delete-modal .modal-header {
    background-color: #fee2e2; /* Light red header */
    color: #b91c1c; /* Darker red text */
}

.delete-modal .modal-body p {
    margin-bottom: 0.5rem;
}

.delete-modal .warning {
    color: #ef4444; /* Red warning text */
    font-weight: 600;
    margin-top: 0.25rem;
    margin-bottom: 1rem;
}

.delete-modal .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}

.delete-modal .cancel-btn {
    background-color: #e5e7eb; /* Light gray */
    color: #374151;
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.delete-modal .cancel-btn:hover {
    background-color: #d1d5db;
}

.delete-modal .delete-btn {
    background-color: #ef4444; /* Red */
    color: white;
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.delete-modal .delete-btn:hover {
    background-color: #dc2626;
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
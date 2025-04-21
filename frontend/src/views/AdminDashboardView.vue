<template>
    <div class="admin-dashboard">
        <h1>Admin Dashboard</h1>

        <!-- Dashboard Statistics Grid -->
        <div v-if="dashboardStats" class="stats-grid">
            <div class="stat-card">
                <h3>Total Scooters</h3>
                <div class="stat-value">{{ dashboardStats.totalScooters }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item"><span class="label">Available:</span><span class="value">{{ dashboardStats.availableScooters }}</span></div>
                    <div class="stat-item"><span class="label">In Use:</span><span class="value">{{ dashboardStats.inUseScooters }}</span></div>
                    <div class="stat-item"><span class="label">Maintenance:</span><span class="value">{{ dashboardStats.maintenanceScooters }}</span></div>
                </div>
            </div>
            <div class="stat-card">
                <h3>Total Users</h3>
                <div class="stat-value">{{ dashboardStats.totalUsers }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item"><span class="label">Active:</span><span class="value">{{ dashboardStats.activeUsers }}</span></div>
                    <div class="stat-item"><span class="label">New Today:</span><span class="value">{{ dashboardStats.newUsers }}</span></div>
                </div>
            </div>
            <div class="stat-card">
                <h3>Bookings</h3>
                <div class="stat-value">{{ dashboardStats.totalBookings }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item"><span class="label">Active:</span><span class="value">{{ dashboardStats.activeBookings }}</span></div>
                    <div class="stat-item"><span class="label">Completed:</span><span class="value">{{ dashboardStats.completedBookings }}</span></div>
                    <div class="stat-item"><span class="label">Today:</span><span class="value">{{ dashboardStats.todayBookings }}</span></div>
                </div>
            </div>
            <div class="stat-card">
                <h3>Revenue</h3>
                <div class="stat-value">${{ dashboardStats.totalRevenue.toFixed(2) }}</div>
                <div class="stat-breakdown">
                    <div class="stat-item"><span class="label">Today:</span><span class="value">${{ dashboardStats.todayRevenue.toFixed(2) }}</span></div>
                    <div class="stat-item"><span class="label">This Week:</span><span class="value">${{ dashboardStats.weekRevenue.toFixed(2) }}</span></div>
                </div>
            </div>
        </div>
        <div v-else>
            Loading dashboard statistics...
        </div>

        <!-- Recent Bookings Section -->
        <div class="recent-section section-card">
            <h2>Recent Bookings</h2>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th><th>User</th><th>Scooter</th><th>Start Time</th><th>Status</th><th>Amount</th><th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="booking in recentBookings" :key="booking.id">
                            <td>#{{ booking.id }}</td>
                            <td>{{ booking.userName }}</td>
                            <td>Scooter #{{ booking.scooterId }}</td>
                            <td>{{ formatDate(booking.startTime) }}</td>
                            <td><span class="status-badge" :class="(booking.status || '').toLowerCase().replace(' ', '-')">{{ booking.status }}</span></td>
                            <td>${{ (booking.amount ?? 0).toFixed(2) }}</td>
                            <td><button class="action-btn view" @click="viewBookingDetails(booking.id)">View</button></td>
                        </tr>
                         <tr v-if="!recentBookings || recentBookings.length === 0">
                            <td colspan="7" class="no-data">No recent bookings found.</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="view-all">
                <router-link to="/admin/bookings" class="view-all-link">View All Bookings</router-link>
            </div>
        </div>

        <!-- Recent Issues Section -->
        <div class="recent-section section-card">
            <h2>Recent Issues</h2>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th><th>Scooter</th><th>Reported By</th><th>Issue Type</th><th>Date</th><th>Status</th><th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="issue in recentIssues" :key="issue.id">
                            <td>#{{ issue.id }}</td>
                            <td>Scooter #{{ issue.scooterId || 'N/A' }}</td>
                            <td>{{ issue.reportedByName || 'N/A' }}</td>
                            <td>{{ issue.issueType }}</td>
                            <td>{{ formatDate(issue.reportDate) }}</td>
                            <td><span class="status-badge" :class="(issue.status || '').toLowerCase().replace(' ', '-')">{{ issue.status || 'Unknown' }}</span></td>
                            <td><button class="action-btn view" @click="viewIssueDetails(issue.id)">View</button></td>
                        </tr>
                         <tr v-if="!recentIssues || recentIssues.length === 0">
                            <td colspan="7" class="no-data">No recent issues found.</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="view-all">
                <router-link to="/admin/issues" class="view-all-link">View All Issues</router-link>
            </div>
        </div>

        <!-- View Booking Details Modal -->
        <div v-if="showBookingDetailsModal && selectedBookingForModal" class="modal-overlay" @click="closeBookingDetailsModal" style="display: flex !important;">
            <div class="modal-container" @click.stop @mousedown.stop @mouseup.stop @touchstart.stop @touchend.stop>
                <div class="modal-header">
                    <h3>Booking Details #{{ selectedBookingForModal.id }}</h3>
                    <button class="close-btn" @click="closeBookingDetailsModal">&times;</button>
                </div>
                <div class="modal-body">
                     <div v-if="selectedBookingForModal" class="booking-details">
                        <div class="detail-section"><h4>User Information</h4><p><strong>Name:</strong> {{ selectedBookingForModal.userName }}</p><p><strong>User ID:</strong> {{ selectedBookingForModal.userId || 'N/A'}}</p></div>
                        <div class="detail-section"><h4>Scooter Information</h4><p><strong>ID:</strong> #{{ selectedBookingForModal.scooterId }}</p><p><strong>Location:</strong> {{ selectedBookingForModal.location }}</p></div>
                        <div class="detail-section"><h4>Booking Information</h4><p><strong>Start Time:</strong> {{ formatDateTime(selectedBookingForModal.startTime) }}</p><p><strong>End Time:</strong> {{ selectedBookingForModal.endTime ? formatDateTime(selectedBookingForModal.endTime) : 'Not ended yet' }}</p><p><strong>Duration:</strong> {{ calculateDuration(selectedBookingForModal) }}</p><p><strong>Status:</strong> <span class="status-badge" :class="(selectedBookingForModal.status || '').toLowerCase().replace(' ', '-')">{{ selectedBookingForModal.status || 'Unknown' }}</span></p></div>
                        <div class="detail-section"><h4>Payment Information</h4><p><strong>Base Rate:</strong> ${{ selectedBookingForModal.rawBookingData?.payment?.baseRate?.toFixed(2) || 'N/A' }}</p><p><strong>Additional Charges:</strong> ${{ selectedBookingForModal.rawBookingData?.payment?.additionalCharges?.toFixed(2) || 'N/A' }}</p><p><strong>Total Amount:</strong> ${{ selectedBookingForModal.amount.toFixed(2) }}</p><p><strong>Payment Status:</strong> {{ selectedBookingForModal.rawBookingData?.payment?.status || 'N/A' }}</p></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- START: View Issue Details Modal -->
        <div v-if="showIssueDetailsModal && selectedIssueForModal" class="modal-overlay" @click="closeIssueDetailsModal">
            <div class="modal-container" @click.stop>
                <div class="modal-header">
                    <h3>Issue Details #{{ selectedIssueForModal.id }}</h3>
                    <button class="close-btn" @click="closeIssueDetailsModal">&times;</button>
                </div>
                <div class="modal-body">
                     <div v-if="selectedIssueForModal" class="issue-details">
                         <div class="detail-section">
                            <h4>Scooter Information</h4>
                            <p><strong>Scooter ID:</strong> #{{ selectedIssueForModal.scooterId || 'N/A' }}</p>
                            <!-- Add location if available in ProcessedIssueForDashboard -->
                            <!-- <p><strong>Location:</strong> {{ selectedIssueForModal.location || 'N/A' }}</p> -->
                        </div>
                        <div class="detail-section">
                            <h4>Reporter Information</h4>
                            <p><strong>Reported By:</strong> {{ selectedIssueForModal.reportedByName || 'N/A' }}</p>
                            <!-- Add User ID if available -->
                            <!-- <p><strong>User ID:</strong> #{{ selectedIssueForModal.userId || 'N/A' }}</p> -->
                        </div>
                        <div class="detail-section">
                            <h4>Issue Information</h4>
                            <p><strong>Issue Type:</strong> {{ selectedIssueForModal.issueType }}</p>
                            <p><strong>Report Date:</strong> {{ formatDateTime(selectedIssueForModal.reportDate) }}</p>
                            <p><strong>Status:</strong>
                                <span class="status-badge" :class="(selectedIssueForModal.status || '').toLowerCase().replace(' ', '-')">
                                    {{ selectedIssueForModal.status || 'Unknown' }}
                                </span>
                            </p>
                        </div>
                        <!-- Add description if available -->
                        <!-- 
                        <div class="detail-section" style="grid-column: 1 / -1;"> 
                            <h4>Description</h4>
                            <p>{{ selectedIssueForModal.description || 'No description provided.' }}</p>
                        </div> 
                        -->
                    </div>
                </div>
            </div>
        </div>
        <!-- END: View Issue Details Modal -->

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../services/api' // Only need adminApi now
import { parseISO, format as formatDateFn, intervalToDuration, formatDuration } from 'date-fns'

// --- Interfaces needed for Dashboard Stats & Recent Items --- 
interface ApiUser {
    id: number;
    status?: string;
    createdAt: string | Date;
}
interface ApiScooter {
    id: number;
    available?: boolean;
}
interface ApiBookingResponse {
    id: number;
    status: string;
    startTime: string; 
    endTime?: string | null; 
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
    userName?: string;
    scooterId?: number;
    amount?: number;
}
interface ApiIssue {
    id: number;
    status?: string | null;
    reportedAt?: string | null;
    scooter?: { id: number; }; 
    reportedBy?: { id: number; username?: string; name?: string; };
    faultType?: string;
}
interface ProcessedBookingForDashboard {
    id: number;
    userId?: number;
    userName?: string;
    scooterId?: number;
    location?: string;
    startTime: Date | null; 
    endTime?: Date | null;
    status: string;
    amount: number;
    rawBookingData?: ApiBookingResponse;
}
interface ProcessedIssueForDashboard {
    id: number;
    scooterId?: number;
    reportedByName?: string;
    issueType: string;
    reportDate: Date | null;
    status: string;
}
// --- End Interfaces ---

const router = useRouter()

// --- State for Dashboard --- 
const dashboardStats = ref({
    totalScooters: 0, availableScooters: 0, inUseScooters: 0, maintenanceScooters: 0,
    totalUsers: 0, activeUsers: 0, newUsers: 0,
    totalBookings: 0, activeBookings: 0, completedBookings: 0, todayBookings: 0,
    totalRevenue: 0, todayRevenue: 0, weekRevenue: 0,
    pendingIssues: 0,
})
const recentBookings = ref<ProcessedBookingForDashboard[]>([])
const recentIssues = ref<ProcessedIssueForDashboard[]>([])
const showBookingDetailsModal = ref(false);
const selectedBookingForModal = ref<ProcessedBookingForDashboard | null>(null);

// Add Issue Modal State
const showIssueDetailsModal = ref(false);
const selectedIssueForModal = ref<ProcessedIssueForDashboard | null>(null);
// --- End State --- 

// --- Utility Functions --- 
const formatDate = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    try { return formatDateFn(dateObj, 'MMM d, hh:mm a'); } catch(e) { return 'Error'}
}
const formatDateTime = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
    try { return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm a'); } catch (error) { return 'Error'; }
};
const calculateDuration = (booking: ProcessedBookingForDashboard) => {
    if (!booking.startTime || !booking.endTime) return 'N/A'; 
    try {
        const duration = intervalToDuration({ start: booking.startTime, end: booking.endTime });
        return formatDuration(duration, { format: ['hours', 'minutes'] }) || '0 minutes';
    } catch (error) { console.error("Error calculating duration:", error); return "Error"; }
};
const viewBookingDetails = (id: number) => {
    try {
        const bookingToShow = recentBookings.value.find(b => b.id === id);
        console.log("找到的预订:", bookingToShow);
        
        if (bookingToShow) {
            // 创建副本以避免引用问题
            selectedBookingForModal.value = {...bookingToShow}; // 使用展开运算符代替JSON.parse
            console.log("为模态框设置的预订:", selectedBookingForModal.value);
            
            // 阻止页面滚动
            document.body.classList.add('modal-open');
            showBookingDetailsModal.value = true;
            console.log("模态框状态:", showBookingDetailsModal.value);
            
            // 确保在DOM更新后检查元素
            setTimeout(() => {
                const modalElement = document.querySelector('.modal-overlay');
                console.log("模态框元素:", modalElement);
                if (modalElement) {
                    console.log("模态框可见性:", window.getComputedStyle(modalElement).visibility);
                    console.log("模态框显示:", window.getComputedStyle(modalElement).display);
                    console.log("模态框z-index:", window.getComputedStyle(modalElement).zIndex);
                }
            }, 100);
        } else { 
            console.warn(`找不到预订 ${id}。`); 
            alert('找不到预订详情。');
        }
    } catch (error) {
        console.error('查看预订详情时出错:', error);
        alert('无法显示预订详情。');
    }
}
const viewIssueDetails = (id: number) => {
    try {
        const issueToShow = recentIssues.value.find(i => i.id === id);
        console.log("找到的问题:", issueToShow);
        if (issueToShow) {
            selectedIssueForModal.value = { ...issueToShow }; // Create copy
            console.log("为模态框设置的问题:", selectedIssueForModal.value);
            document.body.classList.add('modal-open');
            showIssueDetailsModal.value = true;
            console.log("问题模态框状态:", showIssueDetailsModal.value);
        } else {
            console.warn(`找不到问题 ${id}。`);
            alert('找不到问题详情。');
        }
    } catch (error) {
        console.error('查看问题详情时出错:', error);
        alert('无法显示问题详情。');
    }
}
const closeBookingDetailsModal = () => {
    console.log("关闭预订模态框");
    showBookingDetailsModal.value = false;
    document.body.classList.remove('modal-open');
};
const closeIssueDetailsModal = () => {
    console.log("关闭问题模态框");
    showIssueDetailsModal.value = false;
    document.body.classList.remove('modal-open');
};
// --- End Utility Functions ---

// --- Data Fetching --- 
const fetchDashboardData = async () => {
    try {
        const [usersRes, scootersRes, bookingsRes, issuesRes] = await Promise.all([
            adminApi.getAllUsers(),
            adminApi.getAllScooters(),
            adminApi.getAllBookings(),
            adminApi.getAllIssues()
        ]);

        const users: ApiUser[] = usersRes.data;
        const scooters: ApiScooter[] = scootersRes.data;
        const bookingsRaw: ApiBookingResponse[] = bookingsRes.data;
        const issuesRaw: ApiIssue[] = issuesRes.data;

        const processedBookings = bookingsRaw.map((b: ApiBookingResponse): ProcessedBookingForDashboard => {
            let parsedStartTime: Date | null = null;
            try { parsedStartTime = b.startTime ? parseISO(b.startTime) : null; } catch(e) {}
            let parsedEndTime: Date | null = null;
            try { parsedEndTime = b.endTime ? parseISO(b.endTime) : null; } catch(e) { }
            const finalAmount = b.payment?.amount ?? b.amount ?? 0;
            return {
                id: b.id, userId: b.user?.id, userName: b.user?.username || b.userName || 'N/A',
                scooterId: b.scooter?.id || b.scooterId, location: b.scooter?.location || 'Unknown',
                startTime: parsedStartTime, endTime: parsedEndTime, status: b.status,
                amount: finalAmount, rawBookingData: b
            };
        });

        const processedIssues = issuesRaw.map((rawIssue: ApiIssue): ProcessedIssueForDashboard => {
            let parsedDate: Date | null = null;
            try { parsedDate = rawIssue.reportedAt ? parseISO(rawIssue.reportedAt) : null; } catch (e) { }
            return {
                id: rawIssue.id, scooterId: rawIssue.scooter?.id,
                reportedByName: rawIssue.reportedBy?.username || rawIssue.reportedBy?.name || 'Unknown',
                issueType: rawIssue.faultType || 'Unknown Type', reportDate: parsedDate, status: rawIssue.status || 'Unknown'
            };
        });

        const now = new Date();
        const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const weekStartCalc = new Date(); 
        weekStartCalc.setDate(weekStartCalc.getDate() - weekStartCalc.getDay());
        weekStartCalc.setHours(0, 0, 0, 0);
        const weekStart = weekStartCalc;

        let totalRevenue = 0, todayRevenue = 0, weekRevenue = 0;
        bookingsRaw.forEach((b: ApiBookingResponse) => { 
            if (b.payment?.status?.toLowerCase() === 'completed') {
                const amount = b.payment.amount || 0;
                totalRevenue += amount;
                try {
                    const completedDate = b.payment.completedAt ? new Date(b.payment.completedAt) : null;
                    if (completedDate && !isNaN(completedDate.getTime())) {
                         if (completedDate >= todayStart) { todayRevenue += amount; }
                         if (completedDate >= weekStart) { weekRevenue += amount; }
                    }
                } catch (e) { }
            }
        });

        dashboardStats.value = {
            totalScooters: scooters.length, availableScooters: scooters.filter(s => s.available === true).length,
            inUseScooters: scooters.filter(s => s.available === false).length, maintenanceScooters: 0,
            totalUsers: users.length, activeUsers: users.filter(u => u.status?.toLowerCase() === 'active').length,
            newUsers: users.filter(u => { try { return new Date(u.createdAt) >= todayStart; } catch { return false; } }).length,
            totalBookings: bookingsRaw.length, activeBookings: bookingsRaw.filter(b => b.status?.toLowerCase() === 'active').length,
            completedBookings: bookingsRaw.filter(b => b.status?.toLowerCase() === 'completed').length,
            todayBookings: bookingsRaw.filter(b => { try { return new Date(b.createdAt) >= todayStart; } catch { return false; } }).length,
            totalRevenue: totalRevenue, todayRevenue: todayRevenue, weekRevenue: weekRevenue,
            pendingIssues: issuesRaw.filter(i => i.status?.toLowerCase() === 'pending').length,
        };

        recentBookings.value = processedBookings.sort((a, b) => (b.startTime?.getTime() ?? 0) - (a.startTime?.getTime() ?? 0)).slice(0, 5);
        recentIssues.value = processedIssues.sort((a, b) => (b.reportDate?.getTime() ?? 0) - (a.reportDate?.getTime() ?? 0)).slice(0, 5);

    } catch (error) {
        console.error('Failed to load dashboard data:', error);
        alert('Failed to load dashboard data.');
    }
}
// --- End Data Fetching ---

onMounted(async () => {
    fetchDashboardData();
})
</script>

<style scoped>
/* Basic Layout & Global Styles */
.admin-dashboard { 
  padding: 0; 
  background-color: #f5f7fa;
  min-height: 100vh;
}
h1 { 
  color: #2c3e50; 
  margin-bottom: 1.5rem; 
  font-weight: 600;
  padding: 1rem;
}
h2 { 
  color: #2c3e50; 
  margin: 1.5rem 0 0.75rem; 
  font-weight: 600;
}
.section-card { 
  background-color: #ffffff; 
  padding: 1.25rem; 
  border-radius: 10px; 
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08); 
  margin-bottom: 1.5rem; 
}
.no-data { 
  text-align: center; 
  color: #6c757d; 
  padding: 0.75rem; 
  font-style: italic; 
}

/* Stats Grid */
.stats-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); 
  gap: 1.25rem; 
  margin-bottom: 1.5rem; 
  padding: 0 1rem;
}
.stat-card { 
  background: white; 
  border-radius: 10px; 
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08); 
  padding: 1.25rem; 
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.12);
}
.stat-card h3 { 
  margin: 0 0 0.75rem; 
  color: #4a5568; 
  font-size: 1.1rem; 
  font-weight: 600;
}
.stat-value { 
  font-size: 2.25rem; 
  font-weight: bold; 
  color: #2c3e50; 
  margin-bottom: 0.75rem; 
}
.stat-breakdown { 
  border-top: 1px solid #eee; 
  padding-top: 0.75rem; 
}
.stat-item { 
  display: flex; 
  justify-content: space-between; 
  margin-bottom: 0.4rem; 
}
.stat-item .label { 
  color: #606f7b; 
}
.stat-item .value { 
  font-weight: 600; 
  color: #2c3e50; 
}

/* Recent Items Table */
.recent-section {
  margin: 0 1rem 1.5rem 1rem;
}
.table-container { 
  overflow-x: auto; 
  border-radius: 8px;
}
.data-table { 
  width: 100%; 
  border-collapse: collapse; 
}
.data-table th, .data-table td { 
  padding: 0.8rem 1rem; 
  text-align: left; 
}
.data-table th { 
  background-color: #f1f5f9; 
  color: #4a5568; 
  font-weight: 600; 
}
.data-table tr:hover {
  background-color: #f8fafc;
}
.data-table tr:not(:last-child) { 
  border-bottom: 1px solid #f1f1f1; 
}
.status-badge { 
  display: inline-block; 
  padding: 0.2rem 0.6rem; 
  border-radius: 20px; 
  font-size: 0.8rem; 
  font-weight: 600; 
  color: white; 
}
.status-badge.active { background-color: #10b981; }
.status-badge.completed { background-color: #475569; }
.status-badge.pending { background-color: #f59e0b; }
.status-badge.cancelled { background-color: #ef4444; }
.status-badge.resolved { background-color: #3b82f6; }
.status-badge.in-progress { background-color: #8b5cf6; }
.status-badge.new { background-color: #f97316; }
.status-badge.assigned { background-color: #eab308; }
.status-badge.unknown { background-color: #94a3b8; }
.action-btn { 
  padding: 0.25rem 0.6rem; 
  border: none; 
  border-radius: 6px; 
  font-size: 0.8rem; 
  font-weight: 600;
  cursor: pointer; 
  transition: all 0.2s; 
}
.action-btn.view { 
  background-color: #3b82f6; 
  color: white; 
}
.action-btn:hover { 
  opacity: 0.9; 
  transform: translateY(-2px);
}
.view-all { 
  text-align: right; 
  margin-top: 0.75rem; 
}
.view-all-link { 
  color: #3b82f6; 
  text-decoration: none; 
  font-weight: 600; 
  transition: color 0.2s;
}
.view-all-link:hover { 
  color: #2563eb;
  text-decoration: underline; 
}

/* Modal Styles */
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
    z-index: 9999;
    visibility: visible;
    opacity: 1;
}

.modal-container {
    /* 响应式尺寸 */
    width: 90%;
    max-width: 700px;
    max-height: 90vh;

    /* 基本外观和 Flex 布局 */
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;

    /* z-index 和可见性 */
    position: relative;
    z-index: 10000;
    visibility: visible;
    opacity: 1;

    /* 保持容器 overflow-y 注释掉 */
    /* overflow-y: auto; */
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
.modal-header h3 { margin: 0; color: #2c3e50; font-weight: 600; }
.close-btn {
    background: none; border: none; font-size: 1.5rem; cursor: pointer;
    color: #606f7b; padding: 0; line-height: 1; transition: color 0.2s;
}
.close-btn:hover { color: #ef4444; }

.modal-body {
    padding: 1.25rem;
    color: #2c3e50;
    overflow-y: auto;
    flex-grow: 1;
}

.booking-details,
.issue-details {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.25rem;
}
.detail-section h4 {
    margin: 0 0 0.6rem;
    color: #10b981;
    font-size: 1.1rem;
    font-weight: 600;
    border-bottom: 1px solid #f1f1f1;
    padding-bottom: 0.4rem;
}
.detail-section p {
    margin: 0.3rem 0;
    color: #2c3e50;
    line-height: 1.5;
}

@media (max-width: 768px) {
  .stats-grid { 
    grid-template-columns: 1fr; 
    padding: 0 0.75rem;
  }
  .recent-section {
    margin: 0 0.75rem 1.25rem 0.75rem;
  }
  .data-table th, .data-table td { 
    padding: 0.6rem 0.4rem; 
    font-size: 0.875rem; 
  }
  .modal-container { 
    max-width: 95%; 
  }
}
</style>
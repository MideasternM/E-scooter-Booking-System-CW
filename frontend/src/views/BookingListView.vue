<template>
  <div class="booking-list-container">
    <div class="header">
      <h1>My Bookings</h1>
      <div class="filters">
        <div class="search-box">
          <input v-model="searchQuery" type="text" placeholder="Search by booking ID or location" />
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
    </div>

    <div v-if="bookings.length > 0" class="booking-grid">
      <div v-for="booking in filteredBookings" :key="booking.id" class="booking-card"
        :class="[booking.status.toLowerCase(), { 'expiring-soon': isBookingExpiringSoon(booking) }]">
        <div class="booking-header">
          <h3>Booking #{{ booking.id }}</h3>
          <div class="status-badge" :class="booking.status.toLowerCase()">
            {{ booking.status }}
            <span v-if="isBookingExpiringSoon(booking)" class="expiry-warning-badge">
              Expiring Soon!
            </span>
          </div>
        </div>

        <div class="booking-details">
          <div class="scooter-info">
            <img :src="booking.scooter.imageUrl || '/placeholder-scooter.png'" alt="Scooter" />
            <div>
              <p><strong>Scooter ID:</strong> #{{ booking.scooter.id }}</p>
              <p><strong>Location:</strong> {{ booking.location }}</p>
            </div>
          </div>

          <div class="time-info">
            <div class="time-block">
              <p class="label">Start Time</p>
              <p class="value">{{ formatDate(booking.startTime) }}</p>
            </div>
            <div class="time-block">
              <p class="label">End Time</p>
              <p class="value">{{ formatDate(booking.endTime) }}</p>
            </div>
            <div class="time-block">
              <p class="label">Duration</p>
              <p class="value">{{ calculateDuration(booking) }}</p>
            </div>
          </div>

          <div class="cost-info">
            <div class="cost-details">
              <p v-if="booking.status !== 'Completed'">Payment pending completion</p>
              <p v-else-if="booking.status === 'Completed' && !booking.payment">Ready to pay</p>
              <p v-else-if="booking.status === 'Completed' && booking.payment">Booking completed</p>
              <p v-if="booking.payment">
                <strong>Paid Amount:</strong> {{ formatAmountDisplay(booking.payment.amount) }}
                <span v-if="booking.hasDiscount" class="discount-tag">20% Discount Applied</span>
              </p>
            </div>
          </div>

          <div class="actions">
            <button @click="viewDetails(booking)" class="action-button view">
              View Details
            </button>
            <button v-if="booking.status === 'Active'" @click="endRide(booking)"
              :disabled="completingBookingId === booking.id" class="action-button end">
              {{ completingBookingId === booking.id ? 'Ending...' : 'End Ride' }}
            </button>
            <button v-if="booking.status === 'Active'" @click="openExtendModal(booking)" class="action-button extend">
                Extend
            </button>
            <button v-if="booking.status === 'Active'" @click="reportIssue(booking)" class="action-button report">
              Report Issue
            </button>
            <button v-if="booking.status === 'Completed' && !booking.payment" @click="goToPayment(booking)"
              class="action-button pay">
              Pay Now
            </button>
            <button v-if="booking.status === 'Completed' && booking.payment" @click="viewReceipt(booking)"
              class="action-button receipt">
              View Receipt
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="no-bookings">
        <p>You have no bookings yet.</p>
        <router-link to="/scooters" class="find-scooter-link">Find a Scooter</router-link>
    </div>

    <div v-if="showExtendModal && selectedBookingForExtend" class="modal-overlay">
      <div class="modal-container extend-modal">
        <div class="modal-header">
          <h3>Extend Booking #{{ selectedBookingForExtend.id }}</h3>
          <button class="close-btn" @click="closeExtendModal">&times;</button>
        </div>
        <div class="modal-body">
          <p><strong>Current End Time:</strong> {{ formatDate(selectedBookingForExtend.endTime) }}</p>
          <div class="form-group">
            <label for="newDuration">Select New Total Duration:</label>
            <select id="newDuration" v-model="selectedNewDurationLabel" class="duration-select">
              <option disabled value="">-- Select duration --</option>
              <option v-for="option in availableExtensionOptions" :key="option.label" :value="option.label">
                {{ option.label }}
              </option>
            </select>
          </div>
          <p v-if="newCalculatedEndTime"><strong>New Estimated End Time:</strong> {{ formatDate(newCalculatedEndTime) }}</p>
          <div v-if="extendError" class="error api-error">{{ extendError }}</div>
        </div>
        <div class="modal-footer">
          <button @click="closeExtendModal" class="modal-button cancel">Cancel</button>
          <button @click="confirmExtendBooking" :disabled="extendingBooking || !selectedNewDurationLabel" class="modal-button confirm">
            {{ extendingBooking ? 'Extending...' : 'Confirm Extension' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDetailsModal && selectedBookingForDetails" class="modal-overlay" @click="closeDetailsModal">
      <div class="modal-container view-details-modal" @click.stop>
        <div class="modal-header">
          <h3>Booking Details #{{ selectedBookingForDetails.id }}</h3>
          <button class="close-btn" @click="closeDetailsModal">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedBookingForDetails" class="booking-details-modal-content">
            <div class="detail-section">
              <h4>Scooter Information</h4>
              <p><strong>Scooter ID:</strong> #{{ selectedBookingForDetails.scooter.id }}</p>
              <p><strong>Location:</strong> {{ selectedBookingForDetails.location }}</p>
            </div>
            <div class="detail-section">
              <h4>Booking Information</h4>
              <p><strong>Start Time:</strong> {{ formatDate(selectedBookingForDetails.startTime) }}</p>
              <p><strong>End Time:</strong> {{ formatDate(selectedBookingForDetails.endTime) }}</p>
              <p><strong>Duration:</strong> {{ calculateDuration(selectedBookingForDetails) }}</p>
              <p><strong>Status:</strong> 
                <span class="status-badge" :class="selectedBookingForDetails.status.toLowerCase()">
                  {{ selectedBookingForDetails.status }}
                </span>
              </p>
            </div>
            <div class="detail-section">
              <h4>Payment Information</h4>
              <p v-if="selectedBookingForDetails.payment">
                <strong>Amount Paid:</strong> {{ formatAmountDisplay(selectedBookingForDetails.payment.amount) }}
                <span v-if="selectedBookingForDetails.hasDiscount" class="discount-tag">20% Discount Applied</span>
              </p>
              <p v-else>Payment not yet completed.</p>
            </div>
          </div>
          <div v-else>
            Loading booking details...
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeDetailsModal" class="modal-button cancel">Close</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookingApi } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { parseISO, format as formatDateFn, formatDistanceStrict, addMinutes, differenceInMinutes } from 'date-fns'

interface Scooter {
  id: number
  imageUrl: string
}

interface Booking {
  id: number
  status: string
  startTime: Date | null
  endTime?: Date | null
  location: string
  scooter: Scooter
  payment?: {
    amount: number;
    discountAmount?: number;
  } | null;
  rawStartTime?: string;
  rawEndTime?: string;
  selectedDurationLabel?: string;
  hasDiscount?: boolean;
}

interface DurationOption {
    label: string;
    value: number;
}
const durationOptions: DurationOption[] = [
  { label: '1 Hour', value: 60 },
  { label: '4 Hours', value: 240 },
  { label: '1 Day', value: 1440 },
  { label: '1 Week', value: 10080 }
];

const bookings = ref<Booking[]>([])
const completingBookingId = ref<number | null>(null);
const apiError = ref('');
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('date')
const router = useRouter()
const auth = useAuthStore()

const showExtendModal = ref(false);
const selectedBookingForExtend = ref<Booking | null>(null);
const selectedNewDurationLabel = ref<string>('');
const extendingBooking = ref(false);
const extendError = ref('');

const showDetailsModal = ref(false);
const selectedBookingForDetails = ref<Booking | null>(null);

const isBookingExpiringSoon = (booking: Booking): boolean => {
    if (booking.status !== 'Active' || !booking.endTime || !(booking.endTime instanceof Date) || isNaN(booking.endTime.getTime())) {
        return false;
    }
    const now = Date.now();
    const endTimeMs = booking.endTime.getTime();
    const fifteenMinutesMs = 15 * 60 * 1000;
    return endTimeMs > now && endTimeMs <= now + fifteenMinutesMs;
};

const formatDate = (dateObj: Date | null | undefined) => {
  if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A';
  try { return formatDateFn(dateObj, 'MMM d, yyyy, hh:mm a'); }
  catch (error) { console.error('Error formatting date:', dateObj, error); return 'Error'; }
}

const calculateDuration = (booking: Booking): string => {
  if (!booking.startTime || !booking.endTime || !(booking.startTime instanceof Date) || !(booking.endTime instanceof Date) || isNaN(booking.startTime.getTime()) || isNaN(booking.endTime.getTime())) {
    return 'N/A';
  }
  try { return formatDistanceStrict(booking.endTime, booking.startTime); }
  catch (error) { console.error('Error calculating duration:', booking, error); return 'Error'; }
}

const fetchBookings = async () => {
  try {
    const userId = auth.getCurrentUserId()
    if (!userId) {
      // Handle unauthenticated
      console.error('No logged in user found')
      return
    }

    const response = await bookingApi.getUserBookings(userId)
    console.log('Fetched bookings:', response.data)
    
    bookings.value = response.data.map((booking: any) => {
      // Parse dates and add default values
      try {
        // Extract date strings if available
        const rawStartTime = booking.startTime
        const rawEndTime = booking.endTime
        // Assign the imageUrl based on the scooter ID
        const scooterId = booking.scooter?.id || 0
        const imageIndex = (scooterId % 4) + 1 // 从4张图片中选择(scooter-1.jpg到scooter-4.jpg)
        const imageUrl = `/images/scooter-${imageIndex}.jpg`
        
        return {
          ...booking,
          rawStartTime,
          rawEndTime,
          // Parse dates - handle both ISO strings and date objects
          startTime: typeof booking.startTime === 'string' ? parseISO(booking.startTime) : booking.startTime,
          endTime: typeof booking.endTime === 'string' ? parseISO(booking.endTime) : booking.endTime,
          // Ensure scooter object has imageUrl
          scooter: {
            ...booking.scooter,
            imageUrl: imageUrl
          }
        }
      } catch (e) {
        console.error('Error processing booking data:', e)
        return booking // Return original on error
      }
    })
  } catch (error: any) {
    console.error('Failed to fetch bookings', error)
    apiError.value = error.response?.data?.message || error.message || 'Failed to load bookings'
  }
}

const filteredBookings = computed(() => {
  let result = [...bookings.value]
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(booking => booking.id.toString().includes(query) || booking.location.toLowerCase().includes(query))
  }
  if (filterStatus.value !== 'all') {
    result = result.filter(booking => booking.status.toLowerCase() === filterStatus.value)
  }
  result.sort((a, b) => {
    const startTimeA = a.startTime?.getTime() ?? 0;
    const startTimeB = b.startTime?.getTime() ?? 0;
    const endTimeA = a.endTime?.getTime() ?? 0;
    const endTimeB = b.endTime?.getTime() ?? 0;
    const amountA = a.payment?.amount ?? 0;
    const amountB = b.payment?.amount ?? 0;
    switch (sortBy.value) {
      case 'duration':
        if (endTimeA > startTimeA && endTimeB > startTimeB) return (endTimeB - startTimeB) - (endTimeA - startTimeA);
        return 0;
      case 'cost': return amountB - amountA;
      default: return startTimeB - startTimeA;
    }
  })
  return result
})

const availableExtensionOptions = computed(() => {
    if (!selectedBookingForExtend.value?.startTime || !selectedBookingForExtend.value?.endTime) {
        return [];
    }
    try {
        const currentDurationMinutes = differenceInMinutes(selectedBookingForExtend.value.endTime, selectedBookingForExtend.value.startTime);
        return durationOptions.filter(option => option.value > currentDurationMinutes);
    } catch (e) {
        console.error("Error calculating current duration for filtering options:", e);
        return durationOptions;
    }
});

const newCalculatedEndTime = computed(() => {
  if (!selectedBookingForExtend.value?.startTime || !selectedNewDurationLabel.value) {
    return null;
  }
  const selectedOption = durationOptions.find(opt => opt.label === selectedNewDurationLabel.value);
  if (!selectedOption) return null;
  try { return addMinutes(selectedBookingForExtend.value.startTime, selectedOption.value); }
  catch (e) { console.error("Error calculating new end time:", e); return null; }
});

const endRide = async (booking: Booking) => {
  if (completingBookingId.value === booking.id) return;
  completingBookingId.value = booking.id;
  apiError.value = '';
  try {
    const response = await bookingApi.completeBooking(booking.id);
    const index = bookings.value.findIndex(b => b.id === booking.id);
    if (index !== -1) {
      bookings.value[index].status = response.data.status || 'Completed';
      bookings.value[index].endTime = response.data.endTime ? new Date(response.data.endTime) : new Date();
    }
    alert('Ride ended successfully!');
  } catch (err: any) { 
    apiError.value = err.response?.data?.message || err.message || 'Failed to end ride.';
    alert(`Error ending ride: ${apiError.value}`);
  } finally { completingBookingId.value = null; }
}

const reportIssue = (booking: Booking) => {
  router.push(`/report-issue/${booking.scooter.id}?bookingId=${booking.id}`);
}

const goToPayment = (booking: Booking) => {
  router.push(`/payment/${booking.id}`);
}

const viewReceipt = (booking: Booking) => {
  if (booking.payment) { router.push(`/receipt/${booking.id}`); }
  else { alert('Payment information is not available.'); }
}

const openExtendModal = (booking: Booking) => {
  console.log("Opening extend modal for booking:", booking);
  selectedBookingForExtend.value = booking;
  console.log("selectedBookingForExtend set to:", selectedBookingForExtend.value);
  selectedNewDurationLabel.value = '';
  extendError.value = '';
  showExtendModal.value = true;
  console.log("showExtendModal set to:", showExtendModal.value);
};

const closeExtendModal = () => {
  showExtendModal.value = false;
  selectedBookingForExtend.value = null;
  selectedNewDurationLabel.value = '';
  extendingBooking.value = false;
};

const confirmExtendBooking = async () => {
  if (!selectedBookingForExtend.value || !selectedNewDurationLabel.value || extendingBooking.value) return;
  extendingBooking.value = true;
  extendError.value = '';
  try {
    const response = await bookingApi.extendBooking(selectedBookingForExtend.value.id, selectedNewDurationLabel.value);
    const index = bookings.value.findIndex(b => b.id === selectedBookingForExtend.value!.id);
    if (index !== -1) {
      bookings.value[index].endTime = response.data.endTime ? new Date(response.data.endTime) : null;
      bookings.value[index].selectedDurationLabel = response.data.selectedDurationLabel;
    }
    closeExtendModal();
    alert('Booking extended successfully!');
  } catch (err: any) {
    extendError.value = err.response?.data?.message || err.message || 'Failed to extend booking.';
  } finally { extendingBooking.value = false; }
};

const viewDetails = (booking: Booking) => {
  selectedBookingForDetails.value = { ...booking }; // Create a copy
  document.body.classList.add('modal-open');
  showDetailsModal.value = true;
};

const closeDetailsModal = () => {
  showDetailsModal.value = false;
  selectedBookingForDetails.value = null;
  document.body.classList.remove('modal-open');
};

// Helper function to format amount display
const formatAmountDisplay = (amount: number | null | undefined): string => {
    if (amount === null || amount === undefined) return '$0.00';
    const numAmount = Number(amount);
    if (isNaN(numAmount)) return '$?.??';
    if (numAmount < 0.01 && numAmount > 0) {
        return `$${numAmount.toFixed(4)}`; // Show more decimals for small values
    }
    return `$${numAmount.toFixed(2)}`;
};

// Call fetchBookings when component mounts
onMounted(() => {
  fetchBookings()
})
</script>

<style scoped>
.booking-list-container {
  max-width: 100%;
  margin: 0;
  padding: 0;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.header {
  background-color: #ffffff;
  padding: 1.5rem 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 1.2rem;
  font-weight: 600;
  font-size: 1.8rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 0;
  flex-wrap: wrap;
}

.search-box input,
.filters select {
  padding: 0.7rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.search-box input:focus,
.filters select:focus {
  border-color: #42b983;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15);
  outline: none;
}

.booking-grid {
  display: grid;
  gap: 1.5rem;
  padding: 0 2rem 2rem;
}

.booking-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.booking-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.booking-header h3 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
}

.status-badge {
  padding: 0.35rem 0.85rem;
  border-radius: 30px;
  color: white;
  font-size: 0.875rem;
  position: relative;
  padding-right: 10px;
  font-weight: 600;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
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

.booking-details {
  padding: 1.5rem;
}

.scooter-info {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.8rem;
  align-items: center;
}

.scooter-info img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.scooter-info img:hover {
  transform: scale(1.05);
}

.scooter-info p {
  margin: 0.5rem 0;
  color: #505a66;
}

.scooter-info strong {
  color: #2c3e50;
  font-weight: 600;
}

.time-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.8rem;
  padding: 1.2rem;
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.03);
}

.time-block .label {
  color: #606f7b;
  font-size: 0.875rem;
  margin-bottom: 0.4rem;
  font-weight: 500;
}

.time-block .value {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.05rem;
}

.cost-info {
  margin-bottom: 1.8rem;
  padding: 1.2rem;
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.03);
}

.cost-details p {
  margin: 0.5rem 0;
  color: #505a66;
}

.cost-details strong {
  color: #2c3e50;
  font-weight: 600;
}

.total-cost {
  font-size: 1.1rem;
  color: #42b983;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-button {
  padding: 0.7rem 1.2rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  flex: 1;
  min-width: 100px;
  text-align: center;
}

.action-button.end {
  background-color: #42b983;
  color: white;
}

.action-button.extend {
  background-color: #3498db;
  color: white;
}

.action-button.report {
  background-color: #f39c12;
  color: white;
}

.action-button.pay {
  background-color: #3498db;
  color: white;
}

.action-button.receipt {
  background-color: #606f7b;
  color: white;
}

.action-button.view {
  background-color: var(--info);
  color: white;
}

.action-button.view:hover {
  background-color: var(--primary-600);
}

.action-button:hover {
  opacity: 0.9;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.no-bookings {
  text-align: center;
  padding: 3rem 1rem;
  background: white;
  border-radius: 12px;
  margin: 0 2rem;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.no-bookings p {
  margin-bottom: 1.5rem;
  color: #606f7b;
  font-size: 1.1rem;
}

.find-scooter-link {
  display: inline-block;
  padding: 0.8rem 1.5rem;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 2px 5px rgba(66, 185, 131, 0.3);
}

.find-scooter-link:hover {
  background-color: #3aa876;
  box-shadow: 0 4px 10px rgba(66, 185, 131, 0.4);
  transform: translateY(-2px);
}

.booking-card.expiring-soon {
  border-left: 5px solid #e67e22;
  box-shadow: 0 4px 15px rgba(230, 126, 34, 0.2);
}

.expiry-warning-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 3px 8px;
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
  background-color: #e67e22;
  border-radius: 20px;
  vertical-align: middle;
  box-shadow: 0 2px 5px rgba(230, 126, 34, 0.2);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

/* --- Modal Styles --- */
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
  z-index: 1000; 
  backdrop-filter: blur(3px);
}

.modal-container { 
  background-color: white; 
  border-radius: 12px; 
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2); 
  width: 90%; 
  max-width: 500px; 
  max-height: 90vh; 
  display: flex; 
  flex-direction: column;
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  padding: 1.2rem 1.5rem; 
  border-bottom: 1px solid #eee; 
}

.modal-header h3 { 
  margin: 0; 
  color: #2c3e50; 
  font-size: 1.3rem;
  font-weight: 600;
}

.close-btn { 
  background: none; 
  border: none; 
  font-size: 1.8rem; 
  cursor: pointer; 
  color: #606f7b; 
  padding: 0; 
  line-height: 1;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #e74c3c;
}

.modal-body { 
  padding: 1.5rem; 
  overflow-y: auto; 
}

.modal-body p { 
  margin-bottom: 1.2rem; 
  font-size: 1rem;
  color: #505a66;
}

.modal-body strong { 
  font-weight: 600;
  color: #2c3e50;
}

.form-group { 
  margin-bottom: 1.5rem; 
}

.form-group label { 
  display: block; 
  margin-bottom: 0.7rem; 
  font-weight: 600; 
  font-size: 1rem;
  color: #2c3e50;
}

.duration-select { 
  width: 100%; 
  padding: 0.9rem; 
  border: 1px solid #e0e0e0; 
  border-radius: 6px; 
  font-size: 1rem; 
  box-sizing: border-box;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.duration-select:focus {
  border-color: #42b983;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15);
  outline: none;
}

.api-error { 
  color: #e74c3c; 
  margin-top: 1rem; 
  font-size: 0.95rem;
  background-color: #fceded;
  padding: 0.8rem;
  border-radius: 6px;
  border: 1px solid #fadbd8;
}

.modal-footer { 
  display: flex; 
  justify-content: flex-end; 
  gap: 1rem; 
  padding: 1.2rem 1.5rem; 
  border-top: 1px solid #eee; 
}

.modal-button { 
  padding: 0.7rem 1.5rem; 
  border-radius: 6px; 
  border: none; 
  cursor: pointer; 
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.3s;
}

.modal-button.cancel { 
  background-color: #f1f1f1; 
  color: #505a66;
}

.modal-button.cancel:hover {
  background-color: #e0e0e0;
}

.modal-button.confirm { 
  background-color: #42b983; 
  color: white;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.modal-button.confirm:hover {
  background-color: #3aa876;
  box-shadow: 0 4px 10px rgba(66, 185, 131, 0.3);
}

.modal-button:disabled { 
  background-color: #ccc; 
  cursor: not-allowed;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .header, .booking-grid {
    padding: 1rem;
  }
  
  .filters {
    flex-direction: column;
  }

  .time-info {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
  
  .no-bookings {
    margin: 0 1rem;
  }
}

/* Import Modal Styles (Similar to Admin Views) */
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
    transition: opacity 0.3s ease;
}

.modal-container {
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
    width: 90%;
    max-width: 550px; /* Adjust as needed */
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 10000;
    visibility: visible;
    opacity: 1;
    transform: scale(1);
    transition: transform 0.3s ease;
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
.modal-header h3 { margin: 0; font-weight: 600; font-size: 1.2rem; }
.close-btn {
    background: none; border: none; font-size: 1.5rem; cursor: pointer;
    color: #606f7b; padding: 0; line-height: 1; transition: color 0.2s;
}
.close-btn:hover { color: #ef4444; }

.modal-body {
    padding: 1.5rem;
    color: #2c3e50;
    overflow-y: auto; 
    flex-grow: 1;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    padding: 1rem 1.5rem;
    border-top: 1px solid #f1f1f1;
    background-color: #f9fafb;
    border-bottom-left-radius: 12px;
    border-bottom-right-radius: 12px;
}

.modal-button {
    padding: 0.6rem 1.2rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 0.95rem;
}

.modal-button.cancel {
    background-color: #e5e7eb;
    color: #374151;
}
.modal-button.cancel:hover { background-color: #d1d5db; }

.modal-button.confirm {
    background-color: var(--primary-600);
    color: white;
}
.modal-button.confirm:hover { background-color: var(--primary-700); }
.modal-button:disabled { background-color: var(--neutral-300); cursor: not-allowed; }

/* Specific styles for view details modal */
.view-details-modal {
    max-width: 650px; /* Slightly wider for details */
}

.booking-details-modal-content {
     display: grid;
     grid-template-columns: 1fr; /* Single column layout */
     gap: 1.5rem;
}

.detail-section h4 {
    margin: 0 0 0.75rem;
    color: var(--primary-700);
    font-size: 1.1rem;
    font-weight: 600;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.5rem;
}

.detail-section p {
    margin: 0.4rem 0;
    line-height: 1.6;
    color: var(--text-secondary);
}
.detail-section p strong {
    color: var(--text-primary);
    margin-right: 0.5em;
}

/* Styles for extend modal specifics */
.extend-modal {
    max-width: 500px;
}

.form-group {
    margin-bottom: 1.25rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #4a5568;
}

.duration-select {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    font-size: 1rem;
    background-color: white;
}

.api-error {
    color: var(--danger-500);
    background-color: var(--danger-100);
    padding: 0.75rem;
    border-radius: var(--radius-md);
    margin-top: 1rem;
    border: 1px solid var(--danger-200);
    text-align: center;
    font-size: 0.9rem;
}

/* Add styles for view details button if needed */
.action-button.view {
    background-color: var(--info);
    color: white;
}
.action-button.view:hover {
    background-color: var(--primary-600);
}

.discount-tag {
  display: inline-block;
  background-color: #4caf50;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.7rem;
  margin-left: 6px;
  font-weight: 600;
}

</style>
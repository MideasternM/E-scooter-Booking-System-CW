<template>
  <div class="receipt-container">
    <div class="header">
      <router-link to="/bookings" class="back-button">
        <span>&larr;</span> Back to Booking List
      </router-link>
      <h1>Receipt</h1>
      <p class="subtitle">Booking #{{ bookingId }}</p>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="receipt" class="receipt-card">
      <div class="company-info">
        <h2>E-Scooter Booking System</h2>
        <p>123 Main Street</p>
        <p>City, Country</p>
        <p>support@escooter.com</p>
      </div>

      <div class="receipt-details">
        <div class="receipt-header">
          <div>
            <h3>Receipt #{{ receiptId }}</h3>
            <p>Date: {{ formatDate(receipt.date) }}</p>
          </div>
          <div class="receipt-status">PAID</div>
        </div>

        <div class="customer-info">
          <h3>Customer Details</h3>
          <p><strong>Name:</strong> {{ receipt.customerName }}</p>
          <p><strong>Email:</strong> {{ receipt.customerEmail }}</p>
        </div>

        <div class="booking-info">
          <h3>Booking Details</h3>
          <p><strong>Scooter ID:</strong> #{{ receipt.scooterId || 'N/A' }}</p>
          <p><strong>Scooter Model:</strong> {{ receipt.scooterModel || 'N/A' }}</p>
          <p><strong>Start Time:</strong> {{ formatDate(receipt.startTime) }}</p>
          <p><strong>End Time:</strong> {{ formatDate(receipt.endTime) }}</p>
          <p><strong>Duration:</strong> {{ receipt.durationText }}</p>
          <p><strong>Location:</strong> {{ receipt.location || 'N/A' }}</p>
        </div>

        <div class="cost-breakdown">
          <h3>Cost Breakdown</h3>
          <div class="cost-table">
            <div class="cost-row" v-if="receipt.discountApplied && receipt.discountAmount !== undefined">
              <span>Discount Applied</span>
              <span class="discount-value">-{{ formatAmountDisplay(receipt.discountAmount) }}</span>
            </div>
            <div class="cost-row total">
              <span>Total Amount Paid</span>
              <span>{{ formatAmountDisplay(receipt.totalAmountPaid) }}</span>
            </div>
          </div>
        </div>

        <div class="payment-info">
          <h3>Payment Information</h3>
          <p><strong>Payment Method:</strong> {{ receipt.paymentMethod }}</p>
          <p><strong>Transaction ID:</strong> {{ receipt.transactionId }}</p>
          <p><strong>Payment Date:</strong> {{ formatDate(receipt.paymentDate) }}</p>
        </div>

        <div class="receipt-footer">
          <p>Thank you for choosing our service!</p>
          <p class="support-text">For any questions, please contact our support team.</p>
        </div>
      </div>

      <!-- <div class="actions">
        <button @click="downloadReceipt" class="action-button download">
          Download Receipt (PDF)
        </button>
        <button @click="emailReceipt" class="action-button email">
          Email Receipt
        </button>
      </div> -->
    </div>
    <div v-else class="error">Could not load receipt data.</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { bookingApi, api, paymentApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const bookingId = Number(route.params.id)
const loading = ref(true)
const error = ref('')
const receiptId = ref('')

interface Booking {
  id: number
  startTime: Date
  endTime: Date
  location: string
  scooter?: { id: number; model?: string };
  user?: { username?: string; email?: string };
  hasDiscount?: boolean;
}

interface Payment {
  id: number;
  amount: number;
  paymentMethod: string;
  transactionId: string;
  completedAt: Date | string; // Backend might send string
  discountAmount?: number;
  hasDiscount?: boolean;
}

// Update Receipt interface to align with fetched data
interface Receipt {
  bookingId: number;
  date: Date; // Date of receipt generation (or payment completion)
  customerName: string;
  customerEmail: string;
  scooterId?: number;
  scooterModel?: string;
  startTime: Date | null;
  endTime: Date | null;
  location: string;
  durationText: string; // Formatted duration string
  totalAmountPaid: number;
  paymentMethod: string;
  transactionId: string;
  paymentDate: Date | null;
  discountApplied: boolean;
  discountAmount?: number;
}

// Receipt data state
const receipt = ref<Receipt | null>(null);

const formatDate = (dateString: string | Date | null | undefined) => {
  if (!dateString) return ''; // Handle null or undefined dates gracefully
  try {
    // Explicitly create a Date object from the input string/Date
    const date = new Date(dateString);
    // Check if the date is valid after parsing
    if (isNaN(date.getTime())) {
      console.error('[ReceiptView] Invalid date value received:', dateString);
      return 'Invalid Date';
    }
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date); // Format the Date object
  } catch (error) {
    console.error('[ReceiptView] Error formatting date:', dateString, error);
    return 'Error';
  }
}

const calculateDurationText = (startTime: Date | null, endTime: Date | null): string => {
    if (!startTime || !endTime) return 'N/A';
    const start = new Date(startTime);
    const end = new Date(endTime);
    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 'Invalid Date Range';
    
    const durationMs = end.getTime() - start.getTime();
    if (durationMs < 0) return 'Invalid Duration';
    
    const totalMinutes = Math.ceil(durationMs / (1000 * 60));
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return `${hours}h ${minutes}m (${totalMinutes} min total)`;
}

// Helper function to format amount display (consistent with BookingListView)
const formatAmountDisplay = (amount: number | null | undefined): string => {
    if (amount === null || amount === undefined) return '$0.00';
    const numAmount = Number(amount);
    if (isNaN(numAmount)) return '$?.??';
    if (numAmount < 0.01 && numAmount > 0) {
        return `$${numAmount.toFixed(4)}`; // Show more decimals for small values
    }
    return `$${numAmount.toFixed(2)}`;
};

onMounted(async () => {
  loading.value = true;
  error.value = '';
  try {
    // Fetch Booking details first
    const bookingRes = await bookingApi.getBookingById(bookingId);
    const bookingData: Booking = bookingRes.data;
    if (!bookingData) {
        throw new Error('Booking not found.');
    }

    // Fetch associated Payment details
    const paymentsRes = await paymentApi.getPaymentsByBooking(bookingId); // Use paymentApi
    const payments: Payment[] = paymentsRes.data;
    
    if (!payments || payments.length === 0) {
      throw new Error('Payment details not found for this booking.');
    }
    // Assuming one payment per completed booking for now
    const paymentData: Payment = payments[0]; 

    // Populate the receipt ref using fetched data
    receipt.value = {
        bookingId: bookingData.id,
        date: new Date(), // Use current date for receipt generation date
        customerName: bookingData.user?.username || 'N/A',
        customerEmail: bookingData.user?.email || 'N/A',
        scooterId: bookingData.scooter?.id,
        scooterModel: bookingData.scooter?.model || 'Standard',
        startTime: bookingData.startTime ? new Date(bookingData.startTime) : null,
        endTime: bookingData.endTime ? new Date(bookingData.endTime) : null,
        location: bookingData.location,
        durationText: calculateDurationText(bookingData.startTime, bookingData.endTime),
        totalAmountPaid: paymentData.amount, // Use the amount from the payment record!
        paymentMethod: paymentData.paymentMethod,
        transactionId: paymentData.transactionId,
        paymentDate: paymentData.completedAt ? new Date(paymentData.completedAt) : null,
        discountApplied: paymentData.hasDiscount || false,
        discountAmount: paymentData.discountAmount
    };
    receiptId.value = `RCPT-${bookingId}-${paymentData.id}`;

  } catch (err: any) { 
    console.error('Failed to load receipt data:', err);
    error.value = err.message || 'Could not load receipt data.';
    receipt.value = null;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.receipt-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  margin-bottom: 2rem;
}

.back-button {
  display: inline-flex;
  align-items: center;
  color: #606f7b;
  text-decoration: none;
  margin-bottom: 1rem;
}

.back-button:hover {
  color: #2c3e50;
}

.back-button span {
  margin-right: 0.5rem;
}

h1 {
  color: #2c3e50;
  margin: 0 0 0.5rem;
}

.subtitle {
  color: #606f7b;
  margin: 0;
}

.loading,
.error {
  text-align: center;
  padding: 2rem;
  color: #606f7b;
}

.error {
  color: #e74c3c;
}

.receipt-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.company-info {
  background: #2c3e50;
  color: white;
  padding: 2rem;
  text-align: center;
}

.company-info h2 {
  margin: 0 0 1rem;
}

.company-info p {
  margin: 0.25rem 0;
  opacity: 0.9;
}

.receipt-details {
  padding: 2rem;
}

.receipt-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.receipt-header h3 {
  margin: 0 0 0.5rem;
  color: #2c3e50;
}

.receipt-status {
  background: #42b983;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-weight: bold;
}

.customer-info,
.booking-info,
.cost-breakdown,
.payment-info {
  margin-bottom: 2rem;
}

h3 {
  color: #2c3e50;
  margin: 0 0 1rem;
  font-size: 1.25rem;
}

p {
  margin: 0.5rem 0;
  color: #606f7b;
}

.cost-table {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 1rem;
}

.cost-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  color: #2c3e50;
}

.cost-row.subtotal {
  border-top: 1px solid #ddd;
  margin-top: 0.5rem;
  padding-top: 1rem;
}

.cost-row.tax {
  color: #606f7b;
  font-size: 0.9rem;
}

.cost-row.total {
  border-top: 1px solid #ddd;
  margin-top: 0.5rem;
  padding-top: 1rem;
  font-weight: bold;
  font-size: 1.1rem;
  color: #42b983;
}

.receipt-footer {
  text-align: center;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.support-text {
  color: #606f7b;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  gap: 1rem;
  padding: 2rem;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.action-button {
  flex: 1;
  padding: 0.875rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-button.download {
  background-color: #42b983;
  color: white;
}

.action-button.email {
  background-color: #606f7b;
  color: white;
}

.action-button:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .receipt-container {
    padding: 1rem;
  }

  .receipt-header {
    flex-direction: column;
    gap: 1rem;
  }

  .actions {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
}
</style>
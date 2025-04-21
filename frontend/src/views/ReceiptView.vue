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
    <div v-else class="receipt-card">
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
          <p><strong>Scooter ID:</strong> #{{ receipt.scooterId }}</p>
          <p><strong>Start Time:</strong> {{ formatDate(receipt.startTime) }}</p>
          <p><strong>End Time:</strong> {{ formatDate(receipt.endTime) }}</p>
          <p><strong>Duration:</strong> {{ calculateDuration(receipt) }}</p>
          <p><strong>Location:</strong> {{ receipt.location }}</p>
        </div>

        <div class="cost-breakdown">
          <h3>Cost Breakdown</h3>
          <div class="cost-table">
            <div class="cost-row">
              <span>Base Rate ({{ receipt.duration }} minutes)</span>
              <span>${{ receipt.baseRate.toFixed(2) }}</span>
            </div>
            <div class="cost-row" v-if="receipt.additionalCharges > 0">
              <span>Additional Charges</span>
              <span>${{ receipt.additionalCharges.toFixed(2) }}</span>
            </div>
            <div class="cost-row subtotal">
              <span>Subtotal</span>
              <span>${{ (receipt.baseRate + receipt.additionalCharges).toFixed(2) }}</span>
            </div>
            <div class="cost-row tax">
              <span>Tax ({{ (receipt.taxRate * 100).toFixed(0) }}%)</span>
              <span>${{ calculateTax(receipt).toFixed(2) }}</span>
            </div>
            <div class="cost-row total">
              <span>Total</span>
              <span>${{ calculateTotal(receipt).toFixed(2) }}</span>
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

      <div class="actions">
        <button @click="downloadReceipt" class="action-button download">
          Download Receipt (PDF)
        </button>
        <button @click="emailReceipt" class="action-button email">
          Email Receipt
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { bookingApi, api } from '../services/api'

const route = useRoute()
const router = useRouter()
const bookingId = Number(route.params.id)
const loading = ref(true)
const error = ref('')
const receiptId = ref('')

interface Receipt {
  date: Date
  customerName: string
  customerEmail: string
  scooterId: number
  startTime: Date
  endTime: Date
  location: string
  duration: number
  baseRate: number
  additionalCharges: number
  taxRate: number
  paymentMethod: string
  transactionId: string
  paymentDate: Date
}

// 收据数据
const receipt = ref<Receipt>({
  date: new Date(),
  customerName: '',
  customerEmail: '',
  scooterId: 0,
  startTime: new Date(),
  endTime: new Date(),
  location: '',
  duration: 0,
  baseRate: 0,
  additionalCharges: 0,
  taxRate: 0,
  paymentMethod: '',
  transactionId: '',
  paymentDate: new Date()
})

// Apply the same robust date formatting as in BookingListView
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

const calculateDuration = (receipt: Receipt) => {
  // Ensure startTime and endTime are Date objects before calculation
  const startTime = new Date(receipt.startTime);
  const endTime = new Date(receipt.endTime);
  if (isNaN(startTime.getTime()) || isNaN(endTime.getTime())) {
    return 'Invalid Date Range';
  }
  const duration = endTime.getTime() - startTime.getTime()
  const hours = Math.floor(duration / (1000 * 60 * 60))
  const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}h ${minutes}m`
}

const calculateTax = (receipt: Receipt) => {
  const subtotal = receipt.baseRate + receipt.additionalCharges
  return subtotal * receipt.taxRate
}

const calculateTotal = (receipt: Receipt) => {
  const subtotal = receipt.baseRate + receipt.additionalCharges
  return subtotal * (1 + receipt.taxRate)
}

const downloadReceipt = () => {
  // Implement PDF download logic
  console.log('Downloading receipt...')
}

const emailReceipt = () => {
  // Implement email sending logic
  console.log('Sending receipt by email...')
}

onMounted(async () => {
  try {
    // 获取预订详情
    const bookingRes = await bookingApi.getBookingById(bookingId)
    const booking = bookingRes.data
    // 获取付款记录
    const paymentsRes = await api.get(`/api/payments/booking/${bookingId}`)
    const payments = paymentsRes.data
    const payment = payments.length > 0 ? payments[0] : null

    if (!payment) {
      throw new Error('未找到对应的付款记录')
    }

    // 构建收据数据
    receipt.value = {
      date: payment.completedAt || payment.createdAt,
      customerName: booking.user.name,
      customerEmail: booking.user.email,
      scooterId: booking.scooter.id,
      startTime: new Date(booking.startTime),
      endTime: new Date(booking.endTime),
      location: booking.scooter.location,
      duration: Math.floor((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000),
      baseRate: parseFloat(payment.amount) - (payment.discountAmount ? parseFloat(payment.discountAmount) : 0),
      additionalCharges: payment.discountAmount ? parseFloat(payment.discountAmount) : 0,
      taxRate: 0,
      paymentMethod: payment.paymentMethod,
      transactionId: payment.transactionId,
      paymentDate: payment.completedAt ? new Date(payment.completedAt) : new Date(payment.createdAt)
    }
    receiptId.value = `R${payment.id}`
  } catch (err: any) {
    error.value = err.message || '加载收据失败'
  } finally {
    loading.value = false
  }
})
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
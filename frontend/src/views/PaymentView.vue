<template>
    <div class="payment-container">
        <h1>Confirm Payment</h1>
        <div v-if="loadingBooking" class="loading">Loading booking details...</div>
        <div v-else-if="loadError" class="error">{{ loadError }}</div>
        <div v-else-if="booking" class="payment-details">
            <h2>Booking #{{ booking.id }}</h2>
            <div class="info-section">
                <h3>Ride Details</h3>
                <p><strong>Scooter ID:</strong> #{{ booking.scooter?.id || 'N/A' }}</p>
                <p><strong>Start Time:</strong> {{ formatDate(booking.startTime) }}</p>
                <p><strong>End Time:</strong> {{ formatDate(booking.endTime) }}</p>
                <p><strong>Duration:</strong> {{ calculatedDuration }}</p>
            </div>

            <div class="info-section cost-section">
                <h3>Payment Summary</h3>
                <p><strong>Rate:</strong> ${{ RATE_PER_MINUTE.toFixed(2) }} per minute</p>
                <p><strong>Calculated Amount:</strong> ${{ calculatedAmount.toFixed(2) }}</p>
                <!-- 可以添加税费、折扣等 -->
                <p class="total-amount"><strong>Total Due:</strong> ${{ calculatedAmount.toFixed(2) }}</p>
            </div>

            <div v-if="paymentError" class="error payment-error">{{ paymentError }}</div>

            <button @click="confirmPayment" :disabled="isPaying || paymentSuccess" class="confirm-button">
                {{ isPaying ? 'Processing...' : (paymentSuccess ? 'Payment Successful' : 'Confirm Payment') }}
            </button>

            <router-link v-if="!paymentSuccess" to="/bookings" class="back-button">
                Cancel and Go Back
            </router-link>
            <router-link v-if="paymentSuccess" :to="`/receipt/${booking.id}`" class="view-receipt-button">
                View Receipt
            </router-link>
        </div>
        <div v-else class="error">Could not load booking details.</div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { bookingApi, paymentApi } from '../services/api'
import { Decimal } from 'decimal.js'; // <-- 修改导入名称

// --- Interfaces (可以考虑提取到公共文件) ---
interface Scooter {
    id: number;
}
interface Booking {
    id: number;
    startTime: string | Date;
    endTime: string | Date;
    status: string;
    scooter?: Scooter; // Scooter 可能需要从 booking 详情 API 获取
}
interface Payment {
    id: number;
    amount: number;
    // ... 其他支付字段
}
// --- ---

const props = defineProps<{
    bookingId: string // 来自路由 props: true
}>()

const route = useRoute()
const router = useRouter()

const loadingBooking = ref(true)
const loadError = ref('')
const booking = ref<Booking | null>(null)
const isPaying = ref(false)
const paymentError = ref('')
const paymentSuccess = ref(false)

// 费率常量 (与后端保持一致)
const RATE_PER_MINUTE = new Decimal("0.15"); // <-- 使用 Decimal

// --- 计算属性 ---
const calculatedDuration = computed(() => {
    if (!booking.value || !booking.value.startTime || !booking.value.endTime) return 'N/A';
    try {
        const start = new Date(booking.value.startTime);
        const end = new Date(booking.value.endTime);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return 'Invalid Date';
        const durationMs = end.getTime() - start.getTime();
        if (durationMs < 0) return 'Invalid Duration';
        const durationMinutes = Math.max(1, Math.ceil(durationMs / (1000 * 60))); // 向上取整并最少1分钟
        const hours = Math.floor(durationMinutes / 60);
        const minutes = durationMinutes % 60;
        return `${hours}h ${minutes}m (${durationMinutes} min total)`;
    } catch (e) {
        return 'Calculation Error';
    }
});

const calculatedAmount = computed(() => {
    if (!booking.value || !booking.value.startTime || !booking.value.endTime) return new Decimal(0); // <-- 使用 Decimal
    try {
        const start = new Date(booking.value.startTime);
        const end = new Date(booking.value.endTime);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return new Decimal(0); // <-- 使用 Decimal
        const durationMs = end.getTime() - start.getTime();
        if (durationMs < 0) return new Decimal(0); // <-- 使用 Decimal
        const durationMinutes = Math.max(1, Math.ceil(durationMs / (1000 * 60))); // 向上取整并最少1分钟
        return RATE_PER_MINUTE.times(durationMinutes); // <-- 使用 Decimal
    } catch (e) {
        return new Decimal(0); // <-- 使用 Decimal
    }
});

// --- 方法 ---
const formatDate = (dateString: string | Date | null | undefined) => {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return 'Invalid Date';
        return new Intl.DateTimeFormat('en-US', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        }).format(date);
    } catch (error) {
        return 'Error';
    }
};

const fetchBookingDetails = async () => {
    loadingBooking.value = true;
    loadError.value = '';
    try {
        const response = await bookingApi.getBookingById(Number(props.bookingId));
        if (response.data && response.data.status === 'Completed') {
            booking.value = response.data;
            // 检查是否已支付 (需要后端 API 支持，暂时跳过)
            // const paymentRes = await paymentApi.getPaymentForBooking(Number(props.bookingId));
            // if (paymentRes.data) { paymentSuccess.value = true; }
        } else if (response.data) {
            loadError.value = `Booking status is ${response.data.status}. Payment can only be made for completed bookings.`;
            booking.value = null; // 清空 booking
        } else {
            throw new Error('Booking details not found.');
        }
    } catch (err: any) {
        console.error('Failed to load booking details:', err);
        loadError.value = err.response?.data?.message || err.message || 'Failed to load booking details.';
        booking.value = null;
    } finally {
        loadingBooking.value = false;
    }
};

const confirmPayment = async () => {
    if (isPaying.value || paymentSuccess.value || !booking.value) return;

    isPaying.value = true;
    paymentError.value = '';

    try {
        console.log(`Attempting to create payment for booking ${props.bookingId}`);
        const response = await paymentApi.createPaymentForBooking(Number(props.bookingId));
        console.log('Payment creation successful:', response.data);
        paymentSuccess.value = true;
        alert('Payment successful!');

    } catch (err: any) {
        console.error('Payment failed:', err);
        paymentError.value = err.response?.data?.message || err.message || 'Payment failed. Please try again.';
        alert(`Payment Error: ${paymentError.value}`);
    } finally {
        isPaying.value = false;
    }
};

// --- 生命周期钩子 ---
onMounted(() => {
    if (props.bookingId) {
        fetchBookingDetails();
    } else {
        loadError.value = 'Booking ID is missing.';
        loadingBooking.value = false;
    }
});
</script>

<style scoped>
.payment-container {
    max-width: 600px;
    margin: 2rem auto;
    padding: 2rem;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h1,
h2 {
    color: #2c3e50;
    text-align: center;
    margin-bottom: 1.5rem;
}

h2 {
    text-align: left;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.5rem;
    margin-bottom: 1rem;
}

.loading,
.error {
    text-align: center;
    padding: 2rem;
    color: #606f7b;
}

.error {
    color: #e74c3c;
    background-color: #fceded;
    border: 1px solid #e74c3c;
    border-radius: 4px;
    padding: 1rem;
    margin-bottom: 1.5rem;
}

.payment-error {
    margin-top: 1rem;
}

.payment-details {
    margin-top: 1rem;
}

.info-section {
    margin-bottom: 2rem;
    padding: 1.5rem;
    background-color: #f8f9fa;
    border-radius: 6px;
    border: 1px solid #eee;
}

.info-section h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    color: #34495e;
}

.info-section p {
    margin: 0.5rem 0;
    color: #555;
}

.cost-section p {
    display: flex;
    justify-content: space-between;
}

.cost-section .total-amount {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid #ddd;
    font-size: 1.2em;
    font-weight: bold;
    color: #42b983;
}

.confirm-button {
    display: block;
    width: 100%;
    padding: 0.875rem 1.5rem;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1.1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease, opacity 0.3s ease;
    margin-top: 1rem;
}

.confirm-button:hover:not(:disabled) {
    background-color: #36a374;
}

.confirm-button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
    opacity: 0.7;
}

.back-button,
.view-receipt-button {
    display: block;
    text-align: center;
    margin-top: 1.5rem;
    color: #606f7b;
    text-decoration: none;
}

.back-button:hover,
.view-receipt-button:hover {
    color: #2c3e50;
    text-decoration: underline;
}

.view-receipt-button {
    color: #42b983;
    font-weight: bold;
}
</style>
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
                <p><strong>Rate:</strong> ${{ formattedRate }} per minute</p>
                <p><strong>Calculated Amount:</strong> ${{ formattedAmount }}</p>
                <!-- 可以添加税费、折扣等 -->
                <p class="total-amount"><strong>Total Due:</strong> ${{ formattedAmount }}</p>
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
import { bookingApi, paymentApi, pricingApi } from '../services/api'
import { Decimal } from 'decimal.js'; // <-- 修改导入名称

// --- Interfaces (可以考虑提取到公共文件) ---
interface Scooter {
    id: number;
    model: string;
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
// Instead of hardcoding the rate, we'll get it from the back-end based on the scooter model
const RATE_PER_MINUTE = ref(new Decimal("0.15")); // Default, will be updated based on scooter model

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
    if (!booking.value || !booking.value.startTime || !booking.value.endTime) return new Decimal(0);
    try {
        const start = new Date(booking.value.startTime);
        const end = new Date(booking.value.endTime);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) return new Decimal(0);
        const durationMs = end.getTime() - start.getTime();
        if (durationMs < 0) return new Decimal(0);
        const durationMinutes = Math.max(1, Math.ceil(durationMs / (1000 * 60))); // 向上取整并最少1分钟
        return RATE_PER_MINUTE.value.times(durationMinutes);
    } catch (e) {
        return new Decimal(0);
    }
});

// For display with appropriate decimals
const formattedAmount = computed(() => {
    const amount = calculatedAmount.value;
    // Show more decimal places for very small amounts
    if (amount.lessThan(0.01)) {
        return amount.toFixed(4);
    }
    return amount.toFixed(2);
});

// For display of the rate with appropriate decimals
const formattedRate = computed(() => {
    return RATE_PER_MINUTE.value.toString();
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

const loadBookingDetails = async () => {
    loadingBooking.value = true
    loadError.value = ''
    try {
        // Fetch booking details
        const response = await bookingApi.getBookingById(Number(props.bookingId))
        console.log('Booking details:', response.data)
        if (response.data) {
            booking.value = response.data
            // Get the scooter model rate
            if (booking.value && booking.value.scooter && booking.value.scooter.model) {
                try {
                    const priceResponse = await pricingApi.getPriceForModel(booking.value.scooter.model);
                    if (priceResponse.data && typeof priceResponse.data.pricePerMinute === 'number') {
                        RATE_PER_MINUTE.value = new Decimal(priceResponse.data.pricePerMinute.toString());
                        console.log(`Rate set to: ${RATE_PER_MINUTE.value}`);
                    }
                } catch (priceErr) {
                    console.error('Failed to fetch price for model:', 
                                 booking.value && booking.value.scooter ? booking.value.scooter.model : 'unknown', 
                                 priceErr);
                    // Continue with default rate
                }
            }
        } else {
            throw new Error('Booking data not found in response')
        }
    } catch (err) {
        console.error('Failed to load booking details:', err)
        loadError.value = 'Could not load booking details. Please try again later.'
        booking.value = null
    } finally {
        loadingBooking.value = false
    }
}

const confirmPayment = async () => {
    if (isPaying.value || paymentSuccess.value || !booking.value) return;

    isPaying.value = true;
    paymentError.value = '';

    try {
        console.log(`Attempting to create payment for booking ${props.bookingId}`);
        // 步骤1: 创建支付记录
        const createResponse = await paymentApi.createPaymentForBooking(Number(props.bookingId));
        console.log('Payment creation successful:', createResponse.data);
        
        // 步骤2: 使用支付ID处理支付
        const paymentId = createResponse.data.id;
        if (!paymentId) {
            throw new Error('Created payment is missing ID');
        }
        
        // 确保使用数值类型的支付ID
        const processResponse = await paymentApi.processPayment(Number(paymentId));
        console.log('Payment processing successful:', processResponse.data);
        
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
        loadBookingDetails();
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
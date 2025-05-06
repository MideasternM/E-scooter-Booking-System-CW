<template>
    <div class="booking-create-container">
        <h1>Create Booking</h1>
        <div v-if="loading" class="loading">Loading scooter details...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="scooter" class="booking-form">
            <div class="scooter-details-section">
                <h2 class="section-title">
                    <span class="icon-wrapper">🛴</span>
                    Scooter Details
                </h2>
                <div class="scooter-info">
                    <div class="scooter-image-container">
                        <img :src="scooter.imageUrl || '/placeholder-scooter.png'" alt="Scooter Image" class="scooter-image" />
                        <div class="scooter-badge">
                            #{{ scooter.id }}
                        </div>
                    </div>
                    <div class="scooter-specs">
                        <div class="spec-item">
                            <div class="spec-icon">📍</div>
                            <div class="spec-content">
                                <div class="spec-label">Location</div>
                                <div class="spec-value">{{ scooter.location }}</div>
                            </div>
                        </div>
                        <div class="spec-item">
                            <div class="spec-icon">🔋</div>
                            <div class="spec-content">
                                <div class="spec-label">Battery</div>
                                <div class="spec-value">
                                    <div class="battery-bar">
                                        <div class="battery-level" :style="`width: ${scooter.batteryLevel}%`" 
                                             :class="getBatteryClass(scooter.batteryLevel)"></div>
                                    </div>
                                    <span>{{ scooter.batteryLevel }}%</span>
                                </div>
                            </div>
                        </div>
                        <div class="spec-item">
                            <div class="spec-icon">🏷️</div>
                            <div class="spec-content">
                                <div class="spec-label">Model</div>
                                <div class="spec-value">{{ scooter.model || 'Standard' }}</div>
                            </div>
                        </div>
                        <div class="spec-item">
                            <div class="spec-icon">📊</div>
                            <div class="spec-content">
                                <div class="spec-label">Status</div>
                                <div class="spec-value">
                                    <span class="status-badge" :class="scooter.available ? 'status-available' : 'status-unavailable'">
                                        {{ scooter.available ? 'Available' : 'Unavailable' }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <form @submit.prevent="handleBookingConfirm">
                <h2 class="section-title">
                    <span class="icon-wrapper">📝</span>
                    Booking Details
                </h2>
                <div class="form-group">
                    <label for="duration">Booking Duration:</label>
                    <select id="duration" v-model.number="bookingDurationMinutes" required class="duration-select">
                        <option v-for="option in durationOptions" :key="option.value" :value="option.value">
                            {{ option.label }}
                        </option>
                    </select>
                    <p v-if="bookingDurationMinutes" class="time-estimate">
                        <span class="info-icon">⏱️</span> Estimated End Time: <span class="highlight">{{ estimatedEndTime }}</span>
                    </p>
                    <p v-if="bookingDurationMinutes" class="estimated-price">
                        <span class="info-icon">💰</span> Estimated Price: 
                        <span v-if="priceLoading" class="loading-price">Calculating...</span>
                        <span v-else-if="priceError" class="error-text">{{ priceError }}</span>
                        <span v-else class="highlight">{{ estimatedPrice }}</span>
                    </p>
                    <!-- Display discount eligibility info -->
                    <div v-if="discountInfo && discountInfo.eligibleForDiscount" class="discount-info">
                        <span class="discount-badge">🎁 20% Discount Applied!</span>
                        <p class="discount-details">You've used the service for {{ discountInfo.weeklyUsageHours.toFixed(1) }} hours this week.</p>
                    </div>
                    <div v-else-if="discountInfo && !discountInfo.eligibleForDiscount" class="discount-progress">
                        <p>Use the service for {{ (discountInfo.requiredHoursForDiscount - discountInfo.weeklyUsageHours).toFixed(1) }} more hours this week to get a 20% discount!</p>
                        <div class="progress-bar">
                            <div class="progress-fill" :style="`width: ${(discountInfo.weeklyUsageHours / discountInfo.requiredHoursForDiscount) * 100}%`"></div>
                        </div>
                    </div>
                </div>

                <div v-if="apiError" class="error api-error">{{ apiError }}</div>

                <button type="submit" :disabled="!scooter?.available || bookingInProgress || priceLoading || !!priceError" class="confirm-button">
                    {{ confirmButtonText }}
                </button>
            </form>
        </div>
        <router-link to="/scooters" class="back-button">
            <span>&larr;</span> Back to Scooter List
        </router-link>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { api, bookingApi, pricingApi } from '../services/api' // Remove scooterApi, keep api and bookingApi
import { jwtDecode } from 'jwt-decode'; // Import jwt-decode

// Define interfaces locally if not globally available
interface Scooter {
    id: number;
    batteryLevel: number;
    location: string;
    available: boolean;
    imageUrl?: string;
    model?: string; // Add model field
}

interface DiscountInfo {
    eligibleForDiscount: boolean;
    discountRate: number;
    weeklyUsageHours: number;
    requiredHoursForDiscount: number;
}

interface DecodedToken {
    userId: number;
    // Add other expected token payload fields if necessary
}

// Define duration options
const durationOptions = [
  { label: '1 Hour', value: 60 },
  { label: '4 Hours', value: 240 },
  { label: '1 Day', value: 1440 }, 
  { label: '1 Week', value: 10080 } 
];

const props = defineProps<{
    scooterId: string | number // Received from router (props: true)
}>()

const router = useRouter()

const loading = ref(true)
const error = ref('')
const apiError = ref('')
const scooter = ref<Scooter | null>(null)
const bookingDurationMinutes = ref(durationOptions[0].value) // Default duration
const bookingInProgress = ref(false)

// --- Add state for fetched price --- 
const pricePerMinute = ref<number | null>(null);
const priceLoading = ref(false);
const priceError = ref('');

// --- Add state for discount ---
const discountInfo = ref<DiscountInfo | null>(null);
const loadingDiscount = ref(false);
const discountError = ref('');

// --- ADD COMPUTED PROPERTY FOR BUTTON TEXT START ---
const confirmButtonText = computed(() => {
    if (bookingInProgress.value) {
        return 'Booking...';
    }
    if (priceLoading.value) {
        return 'Getting Price...'; // Indicate price loading
    }
    if (priceError.value) {
        return 'Cannot Book (Price Error)'; // Indicate price error
    }
    // Use optional chaining `?.` in case scooter is null initially
    if (scooter.value?.available) {
        return 'Confirm Booking';
    }
    return 'Scooter Unavailable';
});
// --- ADD COMPUTED PROPERTY FOR BUTTON TEXT END ---

// --- ADD COMPUTED PROPERTY FOR ESTIMATED PRICE START ---
const estimatedPrice = computed(() => {
    if (!bookingDurationMinutes.value || bookingDurationMinutes.value <= 0) return 'N/A';

    const rate = pricePerMinute.value; 
    if (rate === null || rate < 0) return 'N/A'; // If rate is invalid/not fetched
    
    let calculatedPrice = bookingDurationMinutes.value * rate;
    
    // Apply discount if eligible
    if (discountInfo.value?.eligibleForDiscount) {
        calculatedPrice *= discountInfo.value.discountRate;
        // Always show at least 2 decimal places, but show up to 4 for small values
        if (calculatedPrice < 0.01) {
            return `$${calculatedPrice.toFixed(4)} (20% discount applied!)`;
        }
        return `$${calculatedPrice.toFixed(2)} (20% discount applied!)`;
    }

    // Always show at least 2 decimal places, but show up to 4 for small values
    if (calculatedPrice < 0.01) {
        return `$${calculatedPrice.toFixed(4)}`;
    }
    return `$${calculatedPrice.toFixed(2)}`;
});
// --- ADD COMPUTED PROPERTY FOR ESTIMATED PRICE END ---

const getUserIdFromToken = (): number | null => {
    // --- RESTORE START ---
    const token = localStorage.getItem('token'); // Or wherever the token is stored
    if (!token) {
        console.error('Authentication token not found.');
        // Optionally redirect to login or show error
        error.value = 'User not authenticated. Please login.';
        return null;
    }
    try {
        // Decode the token to get user information (ensure jwt-decode is installed: npm install jwt-decode)
        const decoded = jwtDecode<DecodedToken>(token);
        console.log('Decoded token:', decoded); // Debugging: Log the decoded token
        if (decoded && typeof decoded.userId === 'number') {
            return decoded.userId;
        } else {
            console.error('User ID not found or invalid in token payload:', decoded);
            error.value = 'Invalid authentication token.';
            return null;
        }

    } catch (e) {
        console.error('Error decoding token:', e);
        error.value = 'Invalid authentication token.';
        // Handle invalid token (e.g., redirect to login)
        return null;
    }
    // --- RESTORE END ---
    // return 1; // Remove dummy return
};

// Add a method to check discount eligibility
const checkDiscountEligibility = async () => {
    const userId = getUserIdFromToken();
    if (!userId) return;
    
    loadingDiscount.value = true;
    discountError.value = '';
    
    try {
        const response = await bookingApi.getUserDiscountEligibility(userId);
        discountInfo.value = response.data;
        console.log('Discount info:', discountInfo.value);
    } catch (err: any) {
        console.error('Failed to get discount info:', err);
        discountError.value = 'Could not check for discounts';
    } finally {
        loadingDiscount.value = false;
    }
};

const fetchScooterDetails = async () => {
    loading.value = true
    error.value = ''
    priceLoading.value = false; // Reset price state
    priceError.value = '';
    pricePerMinute.value = null;

    try {
        // Use the base 'api' instance directly
        const response = await api.get(`/api/scooters/${Number(props.scooterId)}`); // Change this line
        console.log('Fetched scooter details:', response.data); // Debugging: Log fetched data
        if (response.data) {
            // 分配滑板车图片URL，根据ID选择不同图片
            const imageIndex = (response.data.id % 4) + 1; // 从4张图片中选择(scooter-1.jpg到scooter-4.jpg)
            const imageUrl = `/images/scooter-${imageIndex}.jpg`;
            
            scooter.value = {
                ...response.data,
                imageUrl: imageUrl // 使用本地图片URL
            };
            
            if (!scooter.value?.available) {
                error.value = 'This scooter is currently unavailable for booking.';
            } else {
                 // --- Fetch price based on model --- 
                 if (scooter.value.model) {
                     priceLoading.value = true;
                     try {
                         console.log(`Fetching price for model: ${scooter.value.model}`); // Debug log
                         const priceResponse = await pricingApi.getPriceForModel(scooter.value.model);
                         console.log('Price response:', priceResponse.data); // Debug log
                         if (priceResponse.data && typeof priceResponse.data.pricePerMinute === 'number') {
                             pricePerMinute.value = priceResponse.data.pricePerMinute;
                             console.log(`Price set to: ${pricePerMinute.value}`); // Debug log
                         } else {
                              console.warn('Invalid price data received:', priceResponse.data);
                              priceError.value = 'Could not get price info.';
                              pricePerMinute.value = -1; // Indicate error state for computed prop
                         }
                     } catch (priceErr: any) {
                         console.error('Failed to fetch price for model:', scooter.value.model, priceErr);
                         priceError.value = 'Could not fetch price.';
                         pricePerMinute.value = -1; // Indicate error state
                     } finally {
                         priceLoading.value = false;
                     }
                 } else {
                     console.warn('Scooter model is missing, cannot fetch specific price.');
                     priceError.value = 'Price info unavailable (no model).'; 
                     pricePerMinute.value = -1; // Indicate error state
                 }
            }
        } else {
            throw new Error('Scooter data not found in response');
        }

    } catch (err: any) {
        console.error('Failed to fetch scooter details:', err)
        error.value = err.response?.data?.message || err.message || 'Failed to load scooter details.'
        scooter.value = null // Ensure scooter is null on error
    } finally {
        loading.value = false
    }
}

const estimatedEndTime = computed(() => {
    if (!bookingDurationMinutes.value) return '';
    const startTime = new Date();
    const endTime = new Date(startTime.getTime() + bookingDurationMinutes.value * 60000); // Add minutes
    return endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
});


const handleBookingConfirm = async () => {
    if (!scooter.value || !scooter.value.available || bookingInProgress.value || priceLoading.value || !!priceError.value) {
        return
    }

    bookingInProgress.value = true
    apiError.value = ''

    const userId = getUserIdFromToken();
    if (userId === null) {
        apiError.value = 'Could not verify user. Please log in again.';
        bookingInProgress.value = false; // Reset loading state here on early exit
        return;
    }

    const startTime = new Date()
    const endTime = new Date(startTime.getTime() + bookingDurationMinutes.value * 60000)
    const selectedOption = durationOptions.find(opt => opt.value === bookingDurationMinutes.value);
    const selectedDurationLabel = selectedOption ? selectedOption.label : `${bookingDurationMinutes.value} minutes`;

    const bookingData = {
        scooterId: Number(props.scooterId),
        userId: userId,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        selectedDurationLabel: selectedDurationLabel
    }

    try {
        const response = await bookingApi.createBooking(bookingData)
        console.log('Booking successful:', response.data);
        
        // Reset loading state BEFORE alert/navigation
        bookingInProgress.value = false 
        
        alert('Booking successful!'); 
        router.push('/bookings') 
    } catch (err: any) {
        console.error('Booking failed:', err)
        console.error('Error details:', err.response?.data || err.message);
        
        // Reset loading state BEFORE setting error
        bookingInProgress.value = false 
        apiError.value = err.response?.data?.message || err.message || 'An error occurred during booking.'
    } 
    // Keep finally block as a safety net, although it might be redundant now
    // finally {
    //    bookingInProgress.value = false 
    // }
}

// Fetch scooter details when the component mounts
onMounted(() => {
    console.log('BookingCreateView mounted with scooterId:', props.scooterId); // Debugging: Log mount and prop
    // --- RESTORE START ---
    if (props.scooterId) {
        fetchScooterDetails();
        checkDiscountEligibility(); // Add this to check for discounts
    } else {
        error.value = 'Scooter ID is missing.';
        loading.value = false;
    }
    // --- RESTORE END ---
    // console.log("fetchScooterDetails() call in onMounted is temporarily disabled."); // Remove debug log
    // Manually set loading to false and maybe provide dummy scooter data for UI testing // Remove debug comment
    // loading.value = false; // Remove debug assignment
    // Optional: Provide dummy data to render the form part // Remove debug comment
    /* // Remove dummy data assignment
    scooter.value = {
        id: Number(props.scooterId),
        location: "Dummy Location",
        batteryLevel: 75,
        available: true,
        imageUrl: '/placeholder-scooter.png'
    };
    */
});

// 添加电池状态样式计算函数
const getBatteryClass = (level: number): string => {
  if (level > 70) return 'high';
  if (level > 30) return 'medium';
  return 'low';
};
</script>

<style scoped>
.booking-create-container {
    max-width: 100%;
    margin: 0;
    padding: 0;
    background-color: #f8f9fa;
    min-height: 100vh;
}

h1,
h2 {
    color: #2c3e50;
    margin-bottom: 1.5rem;
    font-weight: 600;
}

h1 {
    padding: 1.5rem 2rem;
    background-color: #ffffff;
    margin: 0 0 1.5rem 0;
    font-size: 1.8rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.loading,
.error {
    text-align: center;
    padding: 2rem;
    color: #606f7b;
    font-size: 1.1rem;
}

.error {
    color: #e74c3c;
    background-color: #fceded;
    border: 1px solid #e74c3c;
    border-radius: 8px;
    padding: 1rem;
    margin: 1.5rem 2rem;
}

.api-error {
    margin-top: 1rem;
}

.booking-form {
    padding: 0 2rem 2rem;
}

.scooter-details-section {
    margin-bottom: 2rem;
}

.section-title {
    display: flex;
    align-items: center;
    margin-bottom: 1.2rem;
    font-size: 1.5rem;
}

.icon-wrapper {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    margin-right: 10px;
    font-size: 1.3rem;
}

.scooter-info {
    background-color: #ffffff;
    padding: 1.5rem;
    border-radius: 12px;
    border: 1px solid #eee;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: row;
    gap: 2rem;
    align-items: center;
}

.scooter-image-container {
    position: relative;
    flex: 0 0 auto;
    width: 240px;
}

.scooter-image {
    width: 100%;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    object-fit: cover;
    height: auto;
    max-height: 240px;
}

.scooter-image:hover {
    transform: scale(1.03);
}

.scooter-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 4px 8px;
    border-radius: 4px;
    font-weight: bold;
    font-size: 0.9rem;
}

.scooter-specs {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.spec-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 1.2rem;
}

.spec-icon {
    flex: 0 0 36px;
    font-size: 1.3rem;
    margin-right: 10px;
}

.spec-content {
    flex: 1;
}

.spec-label {
    font-size: 0.9rem;
    color: #7b8996;
    margin-bottom: 4px;
}

.spec-value {
    font-size: 1.15rem;
    font-weight: 500;
    color: #2c3e50;
}

.battery-bar {
    width: 100%;
    height: 10px;
    background-color: #e9eef2;
    border-radius: 5px;
    margin-bottom: 5px;
    overflow: hidden;
}

.battery-level {
    height: 100%;
    background-color: #4caf50;
    border-radius: 5px;
}

.battery-level.high {
    background-color: #4caf50;
}

.battery-level.medium {
    background-color: #ff9800;
}

.battery-level.low {
    background-color: #f44336;
}

.status-badge {
    display: inline-block;
    padding: 5px 10px;
    border-radius: 50px;
    font-size: 0.9rem;
    font-weight: 500;
}

.status-available {
    background-color: rgba(76, 175, 80, 0.1);
    color: #4caf50;
}

.status-unavailable {
    background-color: rgba(244, 67, 54, 0.1);
    color: #f44336;
}

.time-estimate, .estimated-price {
    display: flex;
    align-items: center;
    margin-top: 1rem;
    font-size: 1rem;
    color: #505a66;
}

.info-icon {
    margin-right: 8px;
    font-size: 1.1rem;
}

.highlight {
    color: #2c3e50;
    font-weight: 600;
    margin-left: 4px;
}

.estimated-price .highlight {
    color: #4caf50;
}

.loading-price {
    font-style: italic;
    color: #7b8996;
}

form {
    background-color: #ffffff;
    padding: 1.5rem;
    border-radius: 12px;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
    border: 1px solid #eee;
}

.form-group {
    margin-bottom: 1.8rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.7rem;
    font-weight: 600;
    color: #2c3e50;
    font-size: 1.05rem;
}

.duration-select {
    width: 100%;
    padding: 0.9rem;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    font-size: 1rem;
    box-sizing: border-box;
    background-color: white;
    cursor: pointer;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    transition: all 0.2s ease;
}

.duration-select:focus {
    border-color: #42b983;
    box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15);
    outline: none;
}

.form-group p {
    margin-top: 0.8rem;
    font-size: 1rem;
    color: #505a66;
}

.estimated-price {
    font-weight: 600;
    font-size: 1.1rem !important;
    color: #2c3e50 !important;
}

.estimated-price span {
    color: #42b983;
    font-weight: 700;
}

.confirm-button {
    display: block;
    width: 100%;
    padding: 1rem 1.5rem;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 1.1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.confirm-button:hover:not(:disabled) {
    background-color: #36a374;
    box-shadow: 0 4px 10px rgba(66, 185, 131, 0.3);
}

.confirm-button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
    opacity: 0.7;
}

.back-button {
    display: inline-flex;
    align-items: center;
    color: #505a66;
    text-decoration: none;
    margin: 1.5rem 2rem;
    font-size: 1rem;
    font-weight: 500;
    transition: color 0.2s ease;
}

.back-button:hover {
    color: #2c3e50;
}

.back-button span {
    margin-right: 0.5rem;
    font-size: 1.2rem;
}

.error-text {
    color: #e74c3c;
    font-weight: normal;
    font-size: 0.9em;
}

.discount-info {
    margin-top: 1rem;
    background-color: rgba(76, 175, 80, 0.1);
    padding: 0.8rem;
    border-radius: 8px;
    border-left: 4px solid #4caf50;
}

.discount-badge {
    display: inline-block;
    font-weight: 600;
    color: #4caf50;
    margin-bottom: 0.5rem;
    font-size: 1.1rem;
}

.discount-details {
    color: #2c3e50;
    margin: 0;
    font-size: 0.9rem;
}

.discount-progress {
    margin-top: 1rem;
    background-color: rgba(255, 152, 0, 0.1);
    padding: 0.8rem;
    border-radius: 8px;
    border-left: 4px solid #ff9800;
}

.discount-progress p {
    color: #2c3e50;
    margin: 0 0 0.5rem 0;
    font-size: 0.9rem;
}

.progress-bar {
    height: 8px;
    background-color: #e0e0e0;
    border-radius: 4px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background-color: #ff9800;
    border-radius: 4px;
}

@media (max-width: 768px) {
    .scooter-info {
        flex-direction: column;
    }
    
    .scooter-image-container {
        width: 100%;
        margin-bottom: 1.5rem;
    }
    
    h1 {
        padding: 1rem;
    }
    
    .booking-form {
        padding: 0 1rem 1rem;
    }
    
    .back-button {
        margin: 1rem;
    }
    
    .error {
        margin: 1rem;
    }
}
</style>
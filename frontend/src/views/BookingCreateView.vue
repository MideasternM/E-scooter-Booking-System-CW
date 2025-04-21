<template>
    <div class="booking-create-container">
        <h1>Create Booking</h1>
        <div v-if="loading" class="loading">Loading scooter details...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="scooter" class="booking-form">
            <h2>Scooter Details</h2>
            <div class="scooter-info">
                <p><strong>ID:</strong> #{{ scooter.id }}</p>
                <p><strong>Location:</strong> {{ scooter.location }}</p>
                <p><strong>Battery:</strong> {{ scooter.batteryLevel }}%</p>
                <p><strong>Model:</strong> {{ scooter.model || 'Standard' }}</p>
                <p><strong>Status:</strong> {{ scooter.available ? 'Available' : 'Unavailable' }}</p>
                <img :src="scooter.imageUrl || '/placeholder-scooter.png'" alt="Scooter Image" class="scooter-image" />
            </div>

            <form @submit.prevent="handleBookingConfirm">
                <h2>Booking Details</h2>
                <div class="form-group">
                    <label for="duration">Booking Duration:</label>
                    <select id="duration" v-model.number="bookingDurationMinutes" required class="duration-select">
                        <option v-for="option in durationOptions" :key="option.value" :value="option.value">
                            {{ option.label }}
                        </option>
                    </select>
                    <p v-if="bookingDurationMinutes">
                        Estimated End Time: {{ estimatedEndTime }}
                    </p>
                    <p v-if="bookingDurationMinutes" class="estimated-price">
                        Estimated Price: 
                        <span v-if="priceLoading">Calculating...</span>
                        <span v-else-if="priceError" class="error-text">{{ priceError }}</span>
                        <span v-else>{{ estimatedPrice }}</span>
                    </p>
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
    
    const calculatedPrice = bookingDurationMinutes.value * rate;

    // Format as currency (e.g., $4.50)
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

.scooter-info {
    background-color: #ffffff;
    padding: 1.5rem;
    border-radius: 12px;
    margin-bottom: 2rem;
    border: 1px solid #eee;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 1.5rem;
}

.scooter-info p {
    margin: 0.7rem 0;
    color: #505a66;
    font-size: 1.05rem;
}

.scooter-info strong {
    color: #2c3e50;
}

.scooter-image {
    grid-column: 2;
    grid-row: span 5;
    display: block;
    max-width: 180px;
    height: auto;
    margin: 0;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
}

.scooter-image:hover {
    transform: scale(1.05);
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

@media (max-width: 768px) {
    .scooter-info {
        grid-template-columns: 1fr;
    }
    
    .scooter-image {
        grid-column: 1;
        grid-row: auto;
        margin: 1rem auto;
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
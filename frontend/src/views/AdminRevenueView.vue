<template>
  <div class="admin-revenue-view">
    <h1>Revenue & Statistics</h1>

    <!-- START: Weekly Income Section -->
    <div class="weekly-income-section section-card">
        <h2>Weekly Income by Selected Duration</h2>
        <div v-if="loadingWeeklyIncome" class="loading-indicator">Loading income data...</div>
        <div v-else-if="weeklyIncomeError" class="error-message">{{ weeklyIncomeError }}</div>
        <div v-else-if="weeklyIncomeData" class="income-breakdown">
            <div class="income-item">
                <span class="label">1 Hour:</span>
                <span class="value">${{ weeklyIncomeData['1 Hour']?.toFixed(2) ?? '0.00' }}</span>
            </div>
            <div class="income-item">
                <span class="label">4 Hours:</span>
                <span class="value">${{ weeklyIncomeData['4 Hours']?.toFixed(2) ?? '0.00' }}</span>
            </div>
            <div class="income-item">
                <span class="label">1 Day:</span>
                <span class="value">${{ weeklyIncomeData['1 Day']?.toFixed(2) ?? '0.00' }}</span>
            </div>
            <div class="income-item">
                <span class="label">1 Week:</span>
                <span class="value">${{ weeklyIncomeData['1 Week']?.toFixed(2) ?? '0.00' }}</span>
            </div>
            <div class="income-item">
                <span class="label">Other/Unknown:</span>
                <span class="value">${{ weeklyIncomeData['Other']?.toFixed(2) ?? '0.00' }}</span>
            </div>
        </div>
        <div v-else class="no-data">No weekly income data available.</div>
    </div>
    <!-- END: Weekly Income Section -->

    <!-- START: Daily Income Section -->
    <div class="daily-income-section section-card">
        <h2>Daily Income (Current Week)</h2>
        <div v-if="loadingDailyIncome" class="loading-indicator">Loading daily income...</div>
        <div v-else-if="dailyIncomeError" class="error-message">{{ dailyIncomeError }}</div>
        <div v-else-if="dailyIncomeData" class="daily-income-list">
            <div class="day-item"><strong>Mon:</strong> ${{ dailyIncomeData.mondayIncome?.toFixed(2) ?? '0.00' }}</div>
            <div class="day-item"><strong>Tue:</strong> ${{ dailyIncomeData.tuesdayIncome?.toFixed(2) ?? '0.00' }}</div>
            <div class="day-item"><strong>Wed:</strong> ${{ dailyIncomeData.wednesdayIncome?.toFixed(2) ?? '0.00' }}</div>
            <div class="day-item"><strong>Thu:</strong> ${{ dailyIncomeData.thursdayIncome?.toFixed(2) ?? '0.00' }}</div>
            <div class="day-item"><strong>Fri:</strong> ${{ dailyIncomeData.fridayIncome?.toFixed(2) ?? '0.00' }}</div>
            <div class="day-item"><strong>Sat:</strong> ${{ dailyIncomeData.saturdayIncome?.toFixed(2) ?? '0.00' }}</div>
            <div class="day-item"><strong>Sun:</strong> ${{ dailyIncomeData.sundayIncome?.toFixed(2) ?? '0.00' }}</div>
        </div>
         <div v-else class="no-data">No daily income data available.</div>
    </div>
    <!-- END: Daily Income Section -->

    <!-- START: Duration Popularity Section -->
    <div class="duration-popularity-section section-card">
        <h2>Booking Duration Popularity (All Time)</h2>
        <div v-if="loadingPopularity" class="loading-indicator">Loading popularity data...</div>
        <div v-else-if="popularityError" class="error-message">{{ popularityError }}</div>
        <div v-else-if="durationPopularityData && Object.keys(durationPopularityData.durationCounts).length > 0" class="popularity-list">
             <!-- Iterate over the map entries -->
             <div v-for="(count, label) in durationPopularityData.durationCounts" :key="label" class="popularity-item">
                 <span class="label">{{ label }}:</span>
                 <span class="value">{{ count }} bookings</span>
             </div>
        </div>
        <div v-else class="no-data">No popularity data available.</div>
    </div>
    <!-- END: Duration Popularity Section -->

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from '../services/api'; // Use the base api instance

// --- Interfaces (Moved from Dashboard) ---
interface WeeklyIncomeBreakdown {
    '1 Hour': number;
    '4 Hours': number;
    '1 Day': number;
    '1 Week': number;
    'Other': number;
}

interface DailyIncomeData {
    mondayIncome: number;
    tuesdayIncome: number;
    wednesdayIncome: number;
    thursdayIncome: number;
    fridayIncome: number;
    saturdayIncome: number;
    sundayIncome: number;
}

interface DurationPopularityData {
    durationCounts: { [key: string]: number };
}

// --- State Variables (Moved from Dashboard) ---
const weeklyIncomeData = ref<WeeklyIncomeBreakdown | null>(null);
const loadingWeeklyIncome = ref(false);
const weeklyIncomeError = ref('');

const dailyIncomeData = ref<DailyIncomeData | null>(null);
const loadingDailyIncome = ref(false);
const dailyIncomeError = ref('');

const durationPopularityData = ref<DurationPopularityData | null>(null);
const loadingPopularity = ref(false);
const popularityError = ref('');

// --- Fetch Functions (Moved from Dashboard) ---
const fetchWeeklyIncome = async () => {
    loadingWeeklyIncome.value = true;
    weeklyIncomeError.value = '';
    try {
        console.log('Fetching weekly income breakdown from API...');
        const response = await api.get('/api/admin/statistics/weekly-income-breakdown');
        if (response.data) {
            weeklyIncomeData.value = {
                '1 Hour': response.data.income1Hour ?? 0,
                '4 Hours': response.data.income4Hours ?? 0,
                '1 Day': response.data.income1Day ?? 0,
                '1 Week': response.data.income1Week ?? 0,
                'Other': response.data.incomeOther ?? 0
            };
            console.log('Received weekly income breakdown:', weeklyIncomeData.value);
        } else {
            throw new Error('No data received from weekly income API');
        }
    } catch (err: any) {
        console.error('Failed to fetch weekly income breakdown:', err);
        weeklyIncomeError.value = err.response?.data?.message || err.message || 'Could not load weekly income data.';
        weeklyIncomeData.value = null;
    } finally {
        loadingWeeklyIncome.value = false;
    }
};

const fetchDailyIncome = async () => {
    loadingDailyIncome.value = true;
    dailyIncomeError.value = '';
    try {
        console.log('Fetching daily income from API...');
        const response = await api.get('/api/admin/statistics/daily-income-current-week');
        if (response.data) {
            dailyIncomeData.value = response.data;
            console.log('Received daily income:', dailyIncomeData.value);
        } else {
            throw new Error('No data received from daily income API');
        }
    } catch (err: any) {
        console.error('Failed to fetch daily income:', err);
        dailyIncomeError.value = err.response?.data?.message || err.message || 'Could not load daily income data.';
        dailyIncomeData.value = null;
    } finally {
        loadingDailyIncome.value = false;
    }
};

const fetchDurationPopularity = async () => {
    loadingPopularity.value = true;
    popularityError.value = '';
    try {
        console.log('Fetching booking duration popularity from API...');
        const response = await api.get('/api/admin/statistics/booking-duration-popularity');
        if (response.data && response.data.durationCounts) {
            durationPopularityData.value = response.data;
            console.log('Received duration popularity:', durationPopularityData.value);
        } else {
            throw new Error('Invalid data received from duration popularity API');
        }
    } catch (err: any) {
        console.error('Failed to fetch duration popularity:', err);
        popularityError.value = err.response?.data?.message || err.message || 'Could not load popularity data.';
        durationPopularityData.value = null;
    } finally {
        loadingPopularity.value = false;
    }
};

// --- onMounted Hook ---
onMounted(() => {
  fetchWeeklyIncome();
  fetchDailyIncome();
  fetchDurationPopularity();
});
</script>

<style scoped>
.admin-revenue-view {
  padding: 1rem;
}
h1 {
  color: #2c3e50;
  margin-bottom: 2rem;
}

/* General card style for sections */
.section-card {
  background-color: #ffffff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

/* Styles for loading/error/no-data */
.loading-indicator,
.error-message,
.no-data {
    padding: 1rem;
    text-align: center;
    color: #6c757d;
    font-style: italic;
}

.error-message {
    color: #e74c3c;
    font-weight: 500;
}

/* Styles moved from Dashboard */
/* Weekly Income Section Styles */
.weekly-income-section h2,
.daily-income-section h2,
.duration-popularity-section h2 {
    margin-bottom: 1rem;
    font-size: 1.4rem;
    color: #34495e;
}

.income-breakdown {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1rem;
    padding: 1rem 0;
}

.income-item {
    background-color: #f8f9fa;
    padding: 1rem;
    border-radius: 6px;
    border: 1px solid #e9ecef;
    text-align: center;
}

.income-item .label {
    display: block;
    font-size: 0.9rem;
    color: #6c757d;
    margin-bottom: 0.5rem;
}

.income-item .value {
    display: block;
    font-size: 1.3rem;
    font-weight: 600;
    color: #2c3e50;
}

/* Daily Income Section Styles */
.daily-income-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 0.75rem;
    padding: 1rem 0;
}

.day-item {
    background-color: #f8f9fa;
    padding: 0.75rem 1rem;
    border-radius: 6px;
    border: 1px solid #e9ecef;
    text-align: center;
    font-size: 0.95rem;
    color: #2c3e50;
}

.day-item strong {
    color: #6c757d;
    margin-right: 0.5rem;
}

/* Duration Popularity Section Styles */
.popularity-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 0.75rem;
    padding: 1rem 0;
}

.popularity-item {
    background-color: #f8f9fa;
    padding: 1rem;
    border-radius: 6px;
    border: 1px solid #e9ecef;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.popularity-item .label {
    font-weight: 500;
    color: #34495e;
}

.popularity-item .value {
    font-size: 1.1rem;
    font-weight: 600;
    color: #42b983;
}
</style> 
<template>
    <div class="register-container">
        <div class="register-box">
            <h2>Admin Registration</h2>
            <div v-if="error" class="error-message">
                {{ error }}
            </div>
            <form @submit.prevent="handleRegister">
                <div class="form-group">
                    <label>Staff Number:</label>
                    <input v-model="staffNumber" type="text" required :disabled="loading"
                        placeholder="Enter your staff number" />
                </div>
                <div class="form-group">
                    <label>Full Name:</label>
                    <input v-model="fullName" type="text" required :disabled="loading"
                        placeholder="Enter your full name" />
                </div>
                <div class="form-group">
                    <label>Position:</label>
                    <select v-model="position" required :disabled="loading">
                        <option value="">Select your position</option>
                        <option value="Manager">Manager</option>
                        <option value="Supervisor">Supervisor</option>
                        <option value="Technician">Technician</option>
                        <option value="Customer Support">Customer Support</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Email:</label>
                    <input v-model="email" type="email" required :disabled="loading" placeholder="Enter your email" />
                </div>
                <div class="form-group">
                    <label>Phone Number:</label>
                    <input v-model="phoneNumber" type="text" required :disabled="loading"
                        placeholder="Enter your phone number" />
                </div>
                <div class="form-group">
                    <label>Password:</label>
                    <input v-model="password" type="password" required :disabled="loading"
                        placeholder="Enter your password" />
                </div>
                <div class="form-group">
                    <label>Confirm Password:</label>
                    <input v-model="confirmPassword" type="password" required :disabled="loading"
                        placeholder="Confirm your password" />
                </div>
                <button type="submit" :disabled="loading" class="register-button">
                    {{ loading ? 'Registering...' : 'Register' }}
                </button>
                <div class="links">
                    <router-link to="/admin/login">Already have an account? Login</router-link>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../services/api'

const staffNumber = ref('')
const phoneNumber = ref('')
const fullName = ref('')
const position = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()

const passwordsMatch = computed(() => {
    return password.value === confirmPassword.value
})

const handleRegister = async () => {
    // Reset error
    error.value = ''

    // Validate passwords match
    if (!passwordsMatch.value) {
        error.value = 'Passwords do not match'
        return
    }

    loading.value = true

    try {
        // Call the API to register admin
        await api.post('/api/staff/register', {
            staffNumber: staffNumber.value,
            phoneNumber: staffNumber.value,
            name: fullName.value,
            position: position.value,
            email: email.value,
            password: password.value
        })

        // Redirect to admin login page after successful registration
        router.push('/admin/login')
    } catch (err: any) {
        error.value = err.response?.data?.message || 'Registration failed, please try again'
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f5f5f5;
}

.register-box {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
}

h2 {
    text-align: center;
    color: #2c3e50;
    margin-bottom: 2rem;
}

.form-group {
    margin-bottom: 1.5rem;
}

label {
    display: block;
    margin-bottom: 0.5rem;
    color: #2c3e50;
}

input,
select {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    transition: border-color 0.3s;
}

input:focus,
select:focus {
    border-color: #42b983;
    outline: none;
}

.register-button {
    width: 100%;
    padding: 0.8rem;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.3s;
}

.register-button:hover {
    background-color: #3aa876;
}

.register-button:disabled {
    background-color: #a8d5c2;
    cursor: not-allowed;
}

.error-message {
    background-color: #ffe6e6;
    color: #ff4444;
    padding: 0.8rem;
    border-radius: 4px;
    margin-bottom: 1rem;
    text-align: center;
}

.links {
    text-align: center;
    margin-top: 1rem;
}

.links a {
    color: #42b983;
    text-decoration: none;
}

.links a:hover {
    text-decoration: underline;
}

/* Responsive Adjustments */
@media (max-width: 480px) {
    .register-box {
        padding: 1.5rem;
        margin: 0 1rem; /* Add some horizontal margin */
    }
    h2 {
        font-size: 1.5rem;
        margin-bottom: 1.5rem;
    }
    .form-group {
        margin-bottom: 1rem;
    }
    input,
    select {
        padding: 0.7rem;
        font-size: 0.95rem;
    }
    .register-button {
        padding: 0.7rem;
        font-size: 0.95rem;
    }
}
</style>
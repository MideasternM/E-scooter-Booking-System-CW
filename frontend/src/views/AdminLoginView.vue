<template>
    <div class="login-container">
        <div class="login-box">
            <h2>Admin Login</h2>
            <div v-if="error" class="error-message">
                {{ error }}
            </div>
            <form @submit.prevent="handleLogin">
                <div class="form-group">
                    <label>Staff Number:</label>
                    <input v-model="staffNumber" type="text" required :disabled="loading"
                        placeholder="Enter your staff number" />
                </div>
                <div class="form-group">
                    <label>Password:</label>
                    <input v-model="password" type="password" required :disabled="loading"
                        placeholder="Enter your password" />
                </div>
                <button type="submit" :disabled="loading" class="login-button">
                    {{ loading ? 'Logging in...' : 'Login' }}
                </button>
                <div class="links">
                    <router-link to="/">Back to Home</router-link>
                    <span class="link-divider">|</span>
                    <router-link to="/admin/register">Register as Admin</router-link>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../services/api'

const staffNumber = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()

const handleLogin = async () => {
    loading.value = true
    error.value = ''

    try {
        const response = await api.post('/api/staff/login', {
            staffNumber: staffNumber.value,
            password: password.value
        })
        localStorage.setItem('adminToken', response.data.token)
        localStorage.setItem('isAdmin', 'true')
        router.push('/admin/dashboard')
    } catch (err: any) {
        error.value = err.response?.data?.message || 'Login failed, please try again'
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f5f5f5;
}

.login-box {
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

input {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    transition: border-color 0.3s;
}

input:focus {
    border-color: #42b983;
    outline: none;
}

.login-button {
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

.login-button:hover {
    background-color: #3aa876;
}

.login-button:disabled {
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

.link-divider {
    margin: 0 0.5rem;
    color: #999;
}
</style>
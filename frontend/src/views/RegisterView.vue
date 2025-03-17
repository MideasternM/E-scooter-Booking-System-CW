<template>
    <div class="register-container">
        <div class="register-box">
            <h2>User Registration</h2>
            <div v-if="error" class="error-message">
                {{ error }}
            </div>
            <form @submit.prevent="handleRegister">
                <div class="form-group">
                    <label>Username:</label>
                    <input 
                        v-model="username" 
                        type="text" 
                        required 
                        :disabled="loading"
                        placeholder="Enter your username"
                    />
                </div>
                <div class="form-group">
                    <label>Password:</label>
                    <input 
                        v-model="password" 
                        type="password" 
                        required 
                        :disabled="loading"
                        placeholder="Enter your password"
                    />
                </div>
                <div class="form-group">
                    <label>Email:</label>
                    <input 
                        v-model="email" 
                        type="email" 
                        required 
                        :disabled="loading"
                        placeholder="Enter your email"
                    />
                </div>
                <div class="form-group">
                    <label>Phone Number:</label>
                    <input 
                        v-model="phoneNumber" 
                        type="tel" 
                        required 
                        :disabled="loading"
                        placeholder="Enter your phone number"
                    />
                </div>
                <button type="submit" :disabled="loading" class="register-button">
                    {{ loading ? 'Registering...' : 'Register' }}
                </button>
                <div class="links">
                    <router-link to="/login">Already have an account? Login</router-link>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('')
const password = ref('')
const email = ref('')
const phoneNumber = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()

const handleRegister = async () => {
    loading.value = true
    error.value = ''
    
    try {
        await axios.post('/api/users/register', {
            username: username.value,
            password: password.value,
            email: email.value,
            phoneNumber: phoneNumber.value
        })
        router.push('/login')
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
</style>

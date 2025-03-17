<template>
    <div class="login-container">
        <div class="login-box">
            <h2>User Login</h2>
            <div v-if="error" class="error-message">
                {{ error }}
            </div>
            <form @submit.prevent="handleLogin">
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
                <button type="submit" :disabled="loading" class="login-button">
                    {{ loading ? 'Logging in...' : 'Login' }}
                </button>
                <div class="links">
                    <router-link to="/register">No account? Register now</router-link>
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
const loading = ref(false)
const error = ref('')
const router = useRouter()

const handleLogin = async () => {
    loading.value = true
    error.value = ''
    
    try {
        const response = await axios.post('/api/users/login', {
            username: username.value,
            password: password.value
        })
        localStorage.setItem('token', response.data.token)
        router.push('/')
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
</style>

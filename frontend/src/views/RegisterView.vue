<template>
    <div class="register">
        <h2>Register</h2>
        <form @submit.prevent="register">
            <div>
                <label>Username:</label>
                <input v-model="username" type="text" required />
            </div>
            <div>
                <label>Password:</label>
                <input v-model="password" type="password" required />
            </div>
            <div>
                <label>Email:</label>
                <input v-model="email" type="email" required />
            </div>
            <div>
                <label>Phone Number:</label>
                <input v-model="phoneNumber" type="tel" required />
            </div>
            <button type="submit">Register</button>
        </form>
    </div>
</template>

<script lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

export default {
    setup() {
        const username = ref('')
        const password = ref('')
        const email = ref('')
        const phoneNumber = ref('')
        const router = useRouter()

        const register = async () => {
            try {
                await axios.post('/api/users/register', {
                    username: username.value,
                    password: password.value,
                    email: email.value,
                    phoneNumber: phoneNumber.value
                })
                router.push('/login')
            } catch (error) {
                console.error('Registration failed:', error)
            }
        }

        return {
            username,
            password,
            email,
            phoneNumber,
            register
        }
    }
}
</script>

<style scoped>
.register {
    max-width: 400px;
    margin: 0 auto;
    padding: 20px;
}

form div {
    margin-bottom: 15px;
}

label {
    display: block;
    margin-bottom: 5px;
}

input {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
}

button {
    padding: 10px 20px;
    background-color: #42b983;
    color: white;
    border: none;
    cursor: pointer;
}
</style>

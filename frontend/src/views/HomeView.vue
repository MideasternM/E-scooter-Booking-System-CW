<template>
  <div class="home">
    <div class="hero">
      <h1>Welcome to E-Scooter Booking System</h1>
      <p class="subtitle">Your Green Transportation Solution</p>
      <div class="cta-buttons">
        <router-link v-if="!isLoggedIn" to="/login" class="btn primary">Login</router-link>
        <router-link v-if="!isLoggedIn" to="/register" class="btn secondary">Register</router-link>
        <router-link v-if="isLoggedIn" to="/scooters" class="btn primary">Book a Scooter</router-link>
        <button v-if="isLoggedIn" @click="logout" class="btn danger">Logout</button>
      </div>
    </div>
    <div class="features">
      <div class="feature-card">
        <h3>Easy Booking</h3>
        <p>Book your scooter in just a few clicks</p>
      </div>
      <div class="feature-card">
        <h3>Eco-Friendly</h3>
        <p>Zero emissions, better for our environment</p>
      </div>
      <div class="feature-card">
        <h3>Affordable</h3>
        <p>Competitive rates for your daily commute</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const isLoggedIn = ref(false)
const router = useRouter()

const checkLoginStatus = () => {
  isLoggedIn.value = !!localStorage.getItem('token')
}

const logout = () => {
  localStorage.removeItem('token')
  isLoggedIn.value = false
  router.push('/')
}

onMounted(() => {
  checkLoginStatus()
})
</script>

<style scoped>
.home {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

@media (min-width: 1024px) {
  .home {
    padding: 40px;
  }
}

@media (min-width: 1440px) {
  .home {
    padding: 60px;
  }
}

@media (max-width: 1024px) {
  .home {
    padding: 20px;
  }
  .features {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
  }
}

.hero {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #42b983 0%, #2c3e50 100%);
  color: white;
  border-radius: 12px;
  margin-bottom: 3rem;
}

.hero h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 0.8rem 1.5rem;
  border-radius: 6px;
  font-weight: bold;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  font-size: 1rem;
}

.btn.primary {
  background-color: white;
  color: #42b983;
}

.btn.secondary {
  background-color: transparent;
  border: 2px solid white;
  color: white;
}

.btn.danger {
  background-color: #ff4444;
  color: white;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  margin-top: 3rem;
}

@media (max-width: 1024px) {
  .features {
    grid-template-columns: repeat(2, 1fr);
  }
}

.feature-card {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card h3 {
  color: #42b983;
  margin-bottom: 1rem;
}

.feature-card p {
  color: #666;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .hero {
    padding: 2rem 1rem;
  }

  .hero h1 {
    font-size: 2rem;
  }

  .features {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 1024px) {
  .hero {
    padding: 6rem 4rem;
  }

  .hero h1 {
    font-size: 3rem;
  }

  .subtitle {
    font-size: 1.5rem;
  }
}
</style>


<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from './stores/auth'
import ThemeToggle from './components/ThemeToggle.vue'

// Use the auth store
const authStore = useAuthStore()

// Get reactive state from the store
const { isAuthenticated: isLoggedIn } = storeToRefs(authStore)

// Get current route
const route = useRoute()

// Check if the current route is an admin route
const isAdminRoute = computed(() => {
  // Check if the route path starts with /admin/ and is not the admin login page itself
  return route.path.startsWith('/admin/') && route.path !== '/admin/login';
})
</script>

<template>
  <div class="app-container" :class="{ 'admin-layout-active': isAdminRoute }">
    <header class="app-header glass" :class="{ 'non-sticky-header': isAdminRoute }">
      <div class="container">
        <nav class="main-nav">
          <RouterLink to="/" class="logo hover-scale">
            <span class="logo-icon">🛴</span>
            <span class="logo-text text-gradient">E-Scooter</span>
          </RouterLink>
          
          <div class="nav-links">
            <RouterLink to="/scooters" class="nav-link" v-slot="{ isActive }">
              <div class="nav-link-inner" :class="{ 'active': isActive }">
                <span class="nav-icon">🔍</span>
                <span class="nav-text">Scooters</span>
              </div>
            </RouterLink>
            
            <RouterLink to="/rental-stores" class="nav-link" v-slot="{ isActive }">
              <div class="nav-link-inner" :class="{ 'active': isActive }">
                <span class="nav-icon">🏪</span>
                <span class="nav-text">Stores</span>
              </div>
            </RouterLink>
            
            <RouterLink v-if="isLoggedIn" to="/bookings" class="nav-link" v-slot="{ isActive }">
              <div class="nav-link-inner" :class="{ 'active': isActive }">
                <span class="nav-icon">📋</span>
                <span class="nav-text">Bookings</span>
              </div>
            </RouterLink>
            
            <RouterLink v-if="!isLoggedIn" to="/login" class="nav-link" v-slot="{ isActive }">
              <div class="nav-link-inner" :class="{ 'active': isActive }">
                <span class="nav-icon">👤</span>
                <span class="nav-text">Login</span>
              </div>
            </RouterLink>
            
            <RouterLink v-if="!isLoggedIn" to="/register" class="nav-link register-link" v-slot="{ isActive }">
              <div class="nav-link-inner btn btn-primary" :class="{ 'active': isActive }">
                <span class="nav-text">Register</span>
              </div>
            </RouterLink>
            
            <RouterLink to="/admin/login" class="nav-link admin-link" v-slot="{ isActive }">
              <div class="nav-link-inner" :class="{ 'active': isActive }">
                <span class="nav-icon">⚙️</span>
                <span class="nav-text">Admin</span>
              </div>
            </RouterLink>
            
            <div class="theme-toggle-wrapper">
              <ThemeToggle />
            </div>
          </div>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <RouterView v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    
    <footer class="app-footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-logo">
            <span class="logo-icon">🛴</span>
            <span class="logo-text">E-Scooter</span>
          </div>
          <p class="copyright">© {{ new Date().getFullYear() }} E-Scooter Booking System. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--body-bg);
  position: relative;
}

/* Background gradient */
.app-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 500px;
  background: linear-gradient(135deg, rgba(var(--primary-500-rgb), 0.12), rgba(var(--secondary-500-rgb), 0.08));
  z-index: -1;
  pointer-events: none;
}

/* Header styles */
.app-header {
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* Override position when admin route is active */
.app-header.non-sticky-header {
  position: relative;
}

.main-nav {
  height: 4.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 1.5rem;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  text-decoration: none;
  transition: transform var(--transition-fast) var(--easing-standard);
}

.logo:hover {
  text-decoration: none;
}

.logo-icon {
  margin-right: 0.5rem;
  font-size: 1.75rem;
}

.nav-links {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.nav-link {
  text-decoration: none;
}

.nav-link-inner {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  font-weight: var(--font-weight-medium);
  position: relative;
}

.nav-link-inner:hover {
  background-color: rgba(var(--primary-500-rgb), 0.08);
  color: var(--primary-600);
}

.nav-link-inner.active {
  background-color: rgba(var(--primary-500-rgb), 0.12);
  color: var(--primary-600);
  font-weight: var(--font-weight-semibold);
}

.nav-icon {
  margin-right: 0.5rem;
  transition: transform var(--transition-fast);
}

.nav-link-inner:hover .nav-icon {
  transform: scale(1.1);
}

.register-link .nav-link-inner {
  background-color: var(--primary-600);
  color: white;
  padding: 0.5rem 1.25rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.register-link .nav-link-inner:hover {
  background-color: var(--primary-700);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.admin-link .nav-link-inner {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-md);
}

.admin-link .nav-link-inner:hover {
  background-color: rgba(0, 0, 0, 0.15);
}

.theme-toggle-wrapper {
  display: flex;
  align-items: center;
  margin-left: 0.5rem;
  border-left: 1px solid rgba(var(--primary-500-rgb), 0.2);
  padding-left: 0.75rem;
}

/* Main content area */
.app-main {
  flex: 1;
  box-sizing: border-box;
  width: 100%;
  margin: 0 auto;
  padding: var(--space-md) 0; /* Vertical padding only */
  /* Ensure no horizontal padding is applied here */
  /* Remove transition if it was for padding-left */
  /* transition: padding-left 0.3s ease; */
}

/* Footer */
.app-footer {
  background-color: var(--neutral-900);
  color: var(--neutral-300);
  padding: var(--space-xl) 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-lg);
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.25rem;
  font-weight: var(--font-weight-semibold);
}

.copyright {
  font-size: 0.875rem;
  color: var(--neutral-500);
}

/* Page Transitions */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .main-nav {
    flex-direction: column;
    height: auto;
    padding: 0.75rem 0;
    gap: var(--space-md);
  }
  
  .nav-links {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .nav-link-inner {
    font-size: var(--font-size-sm);
    padding: 0.4rem 0.6rem;
  }
  
  .nav-icon {
    margin-right: 0.25rem;
  }
  
  .theme-toggle-wrapper {
    margin-left: 0;
    border-left: none;
    padding-left: 0;
    margin-top: 0.5rem;
  }
  
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--space-md);
  }
}
</style>

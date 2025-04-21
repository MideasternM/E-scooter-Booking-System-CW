<template>
    <nav class="admin-navbar">
        <div class="navbar-brand">
            <RouterLink to="/admin/dashboard" class="brand-logo hover-scale">
                <span class="brand-icon">🛴</span>
                <span class="brand-text text-gradient">E-Scooter Admin</span>
            </RouterLink>
            <button class="mobile-toggle" @click="toggleMobileMenu" aria-label="Toggle menu">
                <div class="hamburger-icon" :class="{ 'is-active': mobileMenuOpen }">
                    <span></span><span></span><span></span>
                </div>
            </button>
        </div>
        
        <div class="navbar-container" :class="{ 'mobile-open': mobileMenuOpen }">
            <div class="navbar-user glass">
                <div class="user-avatar">A</div>
                <div class="user-info">
                    <div class="user-name">Admin</div>
                    <div class="user-role">Staff</div>
                </div>
            </div>
            
            <div class="navbar-links">
                <RouterLink to="/admin/dashboard" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">📊</span>
                        <span class="nav-text">Dashboard</span>
                    </div>
                </RouterLink>
                
                <RouterLink to="/admin/scooters" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">🛴</span>
                        <span class="nav-text">Scooters</span>
                    </div>
                </RouterLink>
                
                <RouterLink to="/admin/bookings" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">📋</span>
                        <span class="nav-text">Bookings</span>
                    </div>
                </RouterLink>
                
                <RouterLink to="/admin/users" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">👥</span>
                        <span class="nav-text">Users</span>
                    </div>
                </RouterLink>
                
                <RouterLink to="/admin/issues" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">⚠️</span>
                        <span class="nav-text">Issues</span>
                    </div>
                </RouterLink>
                
                <RouterLink to="/admin/revenue" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">💰</span>
                        <span class="nav-text">Revenue</span>
                    </div>
                </RouterLink>
                
                <RouterLink to="/admin/pricing" class="nav-item" v-slot="{ isActive }">
                    <div class="nav-link" :class="{ 'active': isActive }">
                        <span class="nav-icon">💲</span>
                        <span class="nav-text">Pricing</span>
                    </div>
                </RouterLink>
            </div>
            
            <div class="navbar-footer">
                <button class="logout-button hover-lift" @click="logout">
                    <span class="logout-icon">🚪</span>
                    <span>Logout</span>
                </button>
            </div>
        </div>
    </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const mobileMenuOpen = ref(false)

const toggleMobileMenu = () => {
    mobileMenuOpen.value = !mobileMenuOpen.value
}

const logout = () => {
    authStore.logout()
    router.push('/admin/login')
}
</script>

<style scoped>
.admin-navbar {
    background: linear-gradient(180deg, var(--primary-900) 0%, var(--primary-800) 100%);
    color: var(--neutral-100);
    width: 240px;
    height: 100vh;
    display: flex;
    flex-direction: column;
    box-shadow: var(--shadow-lg);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 900;
    transition: width var(--transition-normal) var(--easing-standard);
}

.navbar-brand {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 1.25rem;
    height: 4.5rem;
    background-color: rgba(0, 0, 0, 0.2);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    flex-shrink: 0;
}

.brand-logo {
    display: flex;
    align-items: center;
    text-decoration: none;
    color: white;
    font-weight: var(--font-weight-bold);
    font-size: 1.25rem;
    transition: transform var(--transition-fast) var(--easing-standard);
}

.brand-logo:hover {
    text-decoration: none;
}

.brand-icon {
    font-size: 1.5rem;
    margin-right: 0.75rem;
}

.mobile-toggle {
    display: none;
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    padding: 0.5rem;
}

.hamburger-icon {
    width: 24px;
    height: 18px;
    position: relative;
    transform: rotate(0deg);
    transition: 0.5s ease-in-out;
}

.hamburger-icon span {
    display: block;
    position: absolute;
    height: 2px;
    width: 100%;
    background: white;
    border-radius: 9px;
    opacity: 1;
    left: 0;
    transform: rotate(0deg);
    transition: .25s ease-in-out;
}

.hamburger-icon span:nth-child(1) {
    top: 0px;
}

.hamburger-icon span:nth-child(2) {
    top: 8px;
}

.hamburger-icon span:nth-child(3) {
    top: 16px;
}

.hamburger-icon.is-active span:nth-child(1) {
    top: 8px;
    transform: rotate(135deg);
}

.hamburger-icon.is-active span:nth-child(2) {
    opacity: 0;
    left: -60px;
}

.hamburger-icon.is-active span:nth-child(3) {
    top: 8px;
    transform: rotate(-135deg);
}

.navbar-container {
    display: flex;
    flex-direction: column;
    flex: 1;
    overflow-y: auto;
    padding-bottom: 1rem;
}

.navbar-user {
    display: flex;
    align-items: center;
    padding: 1.5rem 1.25rem;
    margin: 1rem;
    border-radius: var(--radius-lg);
}

.user-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--primary-400), var(--primary-600));
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: var(--font-weight-bold);
    margin-right: 0.75rem;
    font-size: var(--font-size-lg);
    box-shadow: var(--shadow-md);
}

.user-name {
    font-weight: var(--font-weight-semibold);
    font-size: var(--font-size-md);
    margin-bottom: 0.25rem;
}

.user-role {
    font-size: var(--font-size-sm);
    opacity: 0.7;
}

.navbar-links {
    padding: 0.5rem 0.75rem;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.nav-item {
    text-decoration: none;
    color: inherit;
    display: block;
    transition: transform var(--transition-fast);
}

.nav-item:hover {
    transform: translateX(2px);
}

.nav-link {
    display: flex;
    align-items: center;
    padding: 0.85rem 1.25rem;
    margin: 0.1rem 0;
    border-radius: var(--radius-md);
    color: var(--neutral-300);
    transition: all var(--transition-fast);
    font-weight: var(--font-weight-medium);
    position: relative;
}

.nav-link:hover {
    background-color: rgba(var(--primary-300-rgb), 0.15);
    color: white;
    transform: translateX(3px);
}

.nav-link.active {
    background-color: var(--primary-600);
    color: white;
    font-weight: var(--font-weight-semibold);
    box-shadow: 0 4px 10px rgba(var(--primary-500-rgb), 0.3);
}

.nav-link.active::before {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 60%;
    background-color: white;
    border-radius: 0 4px 4px 0;
}

.nav-icon {
    margin-right: 1rem;
    font-size: 1.1rem;
    width: 20px;
    text-align: center;
    transition: transform var(--transition-fast);
}

.nav-link:hover .nav-icon {
    transform: scale(1.1);
}

.navbar-footer {
    padding: 1rem 1.25rem;
    margin-top: auto;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-button {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 0.75rem 1.25rem;
    background-color: rgba(255, 255, 255, 0.05);
    color: var(--neutral-300);
    border: none;
    border-radius: var(--radius-md);
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-medium);
    cursor: pointer;
    text-align: left;
    transition: all var(--transition-fast);
}

.logout-button:hover {
    background-color: rgba(255, 255, 255, 0.1);
    color: white;
}

.logout-icon {
    margin-right: 0.75rem;
    font-size: 1.1rem;
}

@media (max-width: 768px) {
    .admin-navbar {
        width: 100%;
        height: auto;
        min-height: 0;
        position: sticky;
        top: 4.5rem;
        z-index: 800;
    }
    
    .navbar-brand {
        height: auto;
        padding: 1rem 1.25rem;
    }
    
    .mobile-toggle {
        display: block;
    }
    
    .navbar-container {
        position: absolute;
        top: 100%;
        left: 0;
        width: 100%;
        background: linear-gradient(180deg, var(--primary-900) 0%, var(--primary-800) 100%);
        box-shadow: var(--shadow-lg);
        max-height: 0;
        overflow: hidden;
        transition: max-height 0.3s ease-out;
        z-index: 799;
    }
    
    .navbar-container.mobile-open {
        max-height: calc(100vh - 4.5rem - 3.5rem);
        overflow-y: auto;
    }
    
    .navbar-user {
        margin: 0.75rem;
    }
    
    .nav-link {
        padding: 0.75rem 1rem;
    }
}

@media (min-width: 769px) {
    .navbar-container {
        display: flex !important;
        max-height: none !important;
    }
    
    .admin-navbar:hover {
        width: 300px;
    }
}

@media (prefers-color-scheme: dark) {
    .admin-navbar {
        background: linear-gradient(180deg, var(--neutral-900) 0%, var(--neutral-800) 100%);
    }
}

/* Scrollbar styling for the navbar */
.navbar-container::-webkit-scrollbar {
    width: 6px;
}

.navbar-container::-webkit-scrollbar-track {
    background: transparent;
}

.navbar-container::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-full);
}

.navbar-container::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 255, 255, 0.3);
}
</style>
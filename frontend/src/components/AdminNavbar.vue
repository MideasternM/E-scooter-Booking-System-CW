<template>
    <div class="admin-navbar">
        <div class="navbar-header">
            <h1 class="navbar-title">Admin Panel</h1>
        </div>
        <nav class="navbar-nav">
            <router-link to="/admin/dashboard" class="nav-item">
                <span class="nav-icon">?</span>
                <span class="nav-text">Dashboard</span>
            </router-link>
            <router-link to="/admin/scooters" class="nav-item">
                <span class="nav-icon">?</span>
                <span class="nav-text">Scooters</span>
            </router-link>
            <router-link to="/admin/bookings" class="nav-item">
                <span class="nav-icon">?</span>
                <span class="nav-text">Bookings</span>
            </router-link>
            <router-link to="/admin/users" class="nav-item">
                <span class="nav-icon">?</span>
                <span class="nav-text">Users</span>
            </router-link>
            <router-link to="/admin/issues" class="nav-item">
                <span class="nav-icon">??</span>
                <span class="nav-text">Issues</span>
            </router-link>
        </nav>
        <div class="navbar-footer">
            <button @click="logout" class="logout-button">
                <span class="nav-icon">?</span>
                <span class="nav-text">Logout</span>
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const logout = () => {
    localStorage.removeItem('adminToken')
    localStorage.removeItem('isAdmin')
    router.push('/admin/login')
}
</script>

<style scoped>
.admin-navbar {
    display: flex;
    flex-direction: column;
    width: 250px;
    height: 100vh;
    background-color: #2c3e50;
    color: white;
    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}

.navbar-header {
    padding: 1.5rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.navbar-title {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
    color: #42b983;
}

.navbar-nav {
    flex: 1;
    padding: 1rem 0;
    overflow-y: auto;
}

.nav-item {
    display: flex;
    align-items: center;
    padding: 0.75rem 1.5rem;
    color: rgba(255, 255, 255, 0.8);
    text-decoration: none;
    transition: all 0.3s ease;
}

.nav-item:hover,
.nav-item.router-link-active {
    background-color: rgba(66, 185, 131, 0.1);
    color: #42b983;
}

.nav-icon {
    margin-right: 0.75rem;
    font-size: 1.25rem;
}

.navbar-footer {
    padding: 1rem 1.5rem;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-button {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 0.75rem;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.8);
    cursor: pointer;
    transition: color 0.3s ease;
    text-align: left;
    font-size: 1rem;
}

.logout-button:hover {
    color: #e74c3c;
}

@media (max-width: 768px) {
    .admin-navbar {
        width: 60px;
    }

    .navbar-title,
    .nav-text {
        display: none;
    }

    .nav-icon {
        margin-right: 0;
        font-size: 1.5rem;
    }

    .nav-item,
    .logout-button {
        justify-content: center;
        padding: 1rem;
    }

    .navbar-header {
        padding: 1rem;
        display: flex;
        justify-content: center;
    }
}
</style>
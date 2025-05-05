<template>
    <div class="admin-layout">
        <AdminNavbar class="admin-sidebar" />
        <div class="admin-content">
            <div class="admin-header">
                <div class="user-info">
                    <span class="user-greeting">Welcome, Admin</span>
                    <span class="user-role">Staff</span>
                </div>
            </div>
            <div class="admin-main">
                <RouterView />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import AdminNavbar from '../components/AdminNavbar.vue'
</script>

<style scoped>
.admin-layout {
    display: flex;
    min-height: 100vh;
    background-color: #f5f7fa;
}

.admin-sidebar {
    flex-shrink: 0;
    position: sticky;
    top: 0;
    height: 100vh;
}

.admin-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
}

.admin-header {
    height: 64px;
    background-color: white;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 2rem;
}

.user-info {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
}

.user-greeting {
    font-weight: 500;
    color: #2c3e50;
}

.user-role {
    font-size: 0.875rem;
    color: #606f7b;
}

.admin-main {
    flex: 1;
    padding: 2rem;
    overflow-y: auto;
}

/* Responsive adjustments */
@media (max-width: 992px) { /* Adjust breakpoint if needed */
    /* Potential styles if sidebar width changes */
}

@media (max-width: 768px) {
    .admin-layout {
        flex-direction: column; /* Stack sidebar and content */
    }

    .admin-sidebar {
        position: relative; /* Unstick for stacking */
        height: auto;      /* Adjust height */
        width: 100%;       /* Full width when stacked */
        /* Add box-shadow or border if needed for separation */
        border-bottom: 1px solid var(--border-color, #eee);
    }

    .admin-content {
        /* No specific changes needed here if sidebar stacks above */
    }

    .admin-header {
        padding: 0 1rem;
        /* Consider adding a menu toggle button here if needed */
    }

    .admin-main {
        padding: 1rem;
    }
}
</style>
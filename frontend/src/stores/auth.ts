import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../services/api'

// Define User and Admin interfaces (adjust based on actual API responses)
interface User {
    id: number;
    username: string;
    name?: string;
    email?: string;
    // Add other relevant user fields
}

interface AdminUser {
    id: number;
    username: string;
    role?: string;
    // Add other relevant admin fields
}

export const useAuthStore = defineStore('auth', () => {
    // --- User State --- 
    const currentUser = ref<User | null>(null)
    const isUserAuthenticated = ref(false)
    const userToken = ref<string | null>(null)

    // --- Admin State --- 
    const currentAdmin = ref<AdminUser | null>(null)
    const isAdminAuthenticated = ref(false)
    const adminToken = ref<string | null>(null)

    // --- Computed property for App.vue (backward compatible) --- 
    // This provides the `isAuthenticated` name that App.vue expects, mapping to user auth.
    const isAuthenticated = computed(() => isUserAuthenticated.value)

    // --- Initialization --- 
    function init() {
        console.log('[Auth Init] Initializing auth state...');
        // Load User State
        const storedUserToken = localStorage.getItem('token')
        const storedUser = localStorage.getItem('user')
        if (storedUserToken) {
            userToken.value = storedUserToken
            isUserAuthenticated.value = true
            console.log('[Auth Init] User token loaded.');
        }
        if (storedUser) {
            try {
                currentUser.value = JSON.parse(storedUser)
                isUserAuthenticated.value = true; // Ensure auth if user exists
                console.log('[Auth Init] User data parsed.');
            } catch (error) {
                console.error('[Auth Init] Failed to parse user data:', error)
                localStorage.removeItem('user');
                currentUser.value = null;
                // Only de-authenticate if token is also missing
                if (!userToken.value) isUserAuthenticated.value = false;
            }
        }

        // Load Admin State
        const storedAdminToken = localStorage.getItem('adminToken')
        const storedAdmin = localStorage.getItem('adminUser')
        if (storedAdminToken) {
            adminToken.value = storedAdminToken
            isAdminAuthenticated.value = true
            console.log('[Auth Init] Admin token loaded.');
        }
        if (storedAdmin) {
            try {
                currentAdmin.value = JSON.parse(storedAdmin)
                isAdminAuthenticated.value = true;
                console.log('[Auth Init] Admin data parsed.');
            } catch (error) {
                console.error('[Auth Init] Failed to parse admin data:', error)
                localStorage.removeItem('adminUser');
                currentAdmin.value = null;
                if (!adminToken.value) isAdminAuthenticated.value = false;
            }
        }
    }

    // --- User Actions --- 
    async function userLogin(username: string, password: string) {
        try {
            const response = await api.post('/api/users/login', { username, password })
            const responseData = response.data;
            const userData = responseData.user || responseData;
            const authToken = responseData.token;

            if (!userData || typeof userData !== 'object' || !authToken) {
                throw new Error('Invalid user login response structure.');
            }

            currentUser.value = userData
            userToken.value = authToken
            isUserAuthenticated.value = true

            localStorage.setItem('token', authToken)
            localStorage.setItem('user', JSON.stringify(userData))
            console.log('[Auth UserLogin] Success.');
            return { success: true }
        } catch (error) {
            console.error('[Auth UserLogin] Failed:', error)
            userLogout(); // Clear state on failure
            return { success: false, error }
        }
    }

    function userLogout() {
        console.log('[Auth UserLogout] Logging out user...');
        currentUser.value = null
        userToken.value = null
        isUserAuthenticated.value = false
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    // --- Admin Actions --- 
    async function adminLogin(username: string, password: string) {
        try {
            // Use the standard 'api' instance but with the admin login endpoint
            const response = await api.post('/api/admins/login', { username, password })
            const responseData = response.data;
            const adminData = responseData.admin || responseData;
            const authToken = responseData.token;

            if (!adminData || typeof adminData !== 'object' || !authToken) {
                throw new Error('Invalid admin login response structure.');
            }

            currentAdmin.value = adminData
            adminToken.value = authToken
            isAdminAuthenticated.value = true

            localStorage.setItem('adminToken', authToken)
            localStorage.setItem('adminUser', JSON.stringify(adminData))
            console.log('[Auth AdminLogin] Success.');
            return { success: true }
        } catch (error) {
            console.error('[Auth AdminLogin] Failed:', error)
            adminLogout(); // Clear state on failure
            return { success: false, error }
        }
    }

    function adminLogout() {
        console.log('[Auth AdminLogout] Logging out admin...');
        currentAdmin.value = null
        adminToken.value = null
        isAdminAuthenticated.value = false
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminUser')
    }

    // --- Getters --- 
    function getCurrentUserId() {
        return currentUser.value?.id || null
    }
    function getCurrentAdminId() {
        return currentAdmin.value?.id || null
    }

    // --- Initialize --- 
    init()

    return {
        // User State & Actions
        currentUser,
        isUserAuthenticated,
        userToken,
        userLogin,
        userLogout,
        getCurrentUserId,
        isAuthenticated, // Keep this computed property for App.vue

        // Admin State & Actions
        currentAdmin,
        isAdminAuthenticated,
        adminToken,
        adminLogin,
        adminLogout,
        getCurrentAdminId,
    }
})
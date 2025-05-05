import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AdminPricingView from '../views/AdminPricingView.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/scooters',
    name: 'scooters',
    component: () => import('../views/ScooterListView.vue')
  },
  {
    path: '/rental-stores',
    name: 'rental-stores',
    component: () => import('../views/RentalStoreListView.vue')
  },
  {
    path: '/bookings',
    name: 'bookings',
    component: () => import('../views/BookingListView.vue')
  },
  {
    path: '/booking/create/:scooterId',
    name: 'booking-create',
    component: () => import('../views/BookingCreateView.vue'),
    props: true
  },
  {
    path: '/report-issue/:scooterId',
    name: 'report-issue',
    component: () => import('../views/ReportIssueView.vue')
  },
  {
    path: '/receipt/:id',
    name: 'receipt',
    component: () => import('../views/ReceiptView.vue')
  },
  {
    path: '/payment/:bookingId',
    name: 'payment',
    component: () => import('../views/PaymentView.vue'),
    props: true
  },
  // Admin routes
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('../views/AdminLoginView.vue')
  },
  {
    path: '/admin/register',
    name: 'admin-register',
    component: () => import('../views/AdminRegisterView.vue')
  },
  {
    path: '/admin',
    component: () => import('../views/AdminLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('../views/AdminDashboardView.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'scooters',
        name: 'admin-scooters',
        component: () => import('../views/AdminScootersView.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'bookings',
        name: 'admin-bookings',
        component: () => import('../views/AdminBookingsView.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('../views/AdminUsersView.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'issues',
        name: 'admin-issues',
        component: () => import('../views/AdminIssuesView.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'pricing',
        name: 'admin-pricing',
        component: AdminPricingView,
        meta: { requiresAdmin: true }
      },
      {
        path: 'rental-stores',
        name: 'admin-rental-stores',
        component: () => import('../views/AdminRentalStoreListView.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'revenue',
        name: 'admin-revenue',
        component: () => import('../views/AdminRevenueView.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (!localStorage.getItem('adminToken')) {
      next({ name: 'admin-login' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router

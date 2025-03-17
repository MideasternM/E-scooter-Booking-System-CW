import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

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
    path: '/bookings',
    name: 'bookings',
    component: () => import('../views/BookingListView.vue')
  },
  {
    path: '/booking/:id',
    name: 'booking-detail',
    component: () => import('../views/BookingDetailView.vue')
  },
  {
    path: '/report-issue/:id',
    name: 'report-issue',
    component: () => import('../views/ReportIssueView.vue')
  },
  {
    path: '/receipt/:id',
    name: 'receipt',
    component: () => import('../views/ReceiptView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router

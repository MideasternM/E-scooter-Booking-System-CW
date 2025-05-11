import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios';
import type { MapItem } from '../types/MapItem'; // Import the new types

// --- ADD INTERFACE DEFINITIONS START ---
// Define expected response structure for pricing
interface PriceResponse {
    pricePerMinute: number;
}

interface ModelPricesResponse {
    [modelName: string]: number;
}
// --- ADD INTERFACE DEFINITIONS END ---

// axios实例
const api = axios.create({
    baseURL: 'http://localhost:8080', // 服务器地址
    timeout: 10000, // 请求超时时间
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
api.interceptors.request.use(
    config => {
        const adminToken = localStorage.getItem('adminToken'); // Check for admin token first
        const userToken = localStorage.getItem('token');      // Check for user token

        if (adminToken) {
            // If admin token exists, use it
            config.headers.Authorization = `Bearer ${adminToken}`;
        } else if (userToken) {
            // Otherwise, if user token exists, use that
            config.headers.Authorization = `Bearer ${userToken}`;
        }
        // If neither token exists, no Authorization header is added

        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
api.interceptors.response.use(
    response => {
        return response
    },
    error => {
        // 处理API响应错误
        if (error.response) {
            // 处理服务器返回的状态码错误
            console.error('API错误:', error.response.status, error.response.data)

            // 处理401未授权错误
            if (error.response.status === 401) {
                // 清除本地存储的token
                localStorage.removeItem('token')
                // 重定向到登录页面
                window.location.href = '/login'
            }
        } else if (error.request) {
            // 处理客户端请求错误
            console.error('请求错误:', error.request)
        } else {
            // 处理请求超时错误
            console.error('请求超时:', error.message)
        }
        return Promise.reject(error)
    }
)

// 管理员API
const adminApi = {
    // 管理员登录
    login: (username: string, password: string) => {
        return api.post('/api/staff/login', { username, password })
    },
    // 管理员注册
    register: (staffData: any) => {
        return api.post('/api/staff/register', staffData)
    },
    // 获取所有用户
    getAllUsers: () => {
        return api.get('/api/users')
    },
    // 获取用户详情
    getUserDetails: (id: number) => {
        return api.get(`/api/users/${id}`)
    },
    // 暂停用户账号
    suspendUser: (id: number, reason: string) => {
        return api.post(`/api/users/${id}/suspend`, { reason })
    },
    // 激活用户账号
    activateUser: (id: number) => {
        return api.post(`/api/users/${id}/activate`)
    },
    // 保存用户备注
    saveUserNotes: (id: number, notes: string) => {
        return api.post(`/api/users/${id}/notes`, { notes })
    },
    // 获取所有预约
    getAllBookings: () => {
        return api.get('/api/bookings')
    },
    // 取消预约
    cancelBooking: (id: number) => {
        return api.delete(`/api/bookings/${id}`)
    },
    // 获取所有问题报告
    getAllIssues: () => {
        return api.get('/api/issues')
    },
    // 更新问题状态
    updateIssueStatus: (id: number, status: string, notes?: string) => {
        return api.put(`/api/issues/${id}/status`, { status, notes })
    },
    // 获取所有运行中的电动滑板车
    getAllScooters: () => {
        return api.get('/api/scooters')
    },
    // 更新新电动滑板车状态
    updateScooterStatus: (id: number, status: string) => {
        return api.put(`/api/scooters/${id}/status`, { status })
    },
    // 添加新电动滑板车
    addScooter: (scooterData: any) => {
        return api.post('/api/scooters', scooterData)
    },
    // 更新新电动滑板车信息
    updateScooter: (id: number, scooterData: any) => {
        return api.put(`/api/scooters/${id}`, scooterData)
    },
    // 删除电动滑板车
    deleteScooter: (id: number) => {
        return api.delete(`/api/scooters/${id}`)
    },
    // --- START: Add method for staff creating guest booking ---
    createGuestBooking: (bookingData: { scooterId: number; selectedDurationLabel: string; guestEmail: string; guestName?: string }) => {
        return api.post('/api/bookings/staff/guest', bookingData);
    },
    // --- END: Add method for staff creating guest booking ---
    // --- Pricing Config (Admin) ---
    getAllModelPrices: (): Promise<AxiosResponse<ModelPricesResponse>> => {
        return api.get('/api/admin/pricing/models');
    },
    updateModelPrice: (modelName: string, pricePerMinute: number): Promise<AxiosResponse<void>> => {
        // Encode modelName in case it contains special characters
        const encodedModelName = encodeURIComponent(modelName);
        return api.put(`/api/admin/pricing/models/${encodedModelName}`, { pricePerMinute: pricePerMinute.toString() });
    },
    // Update scooter's coordinates
    updateScooterCoordinates: (id: number, latitude: number, longitude: number) => {
        return api.put(`/api/scooters/${id}/coordinates`, { latitude, longitude });
    },
    // Update scooter's model
    updateScooterModel: (id: number, model: string) => {
        return api.put(`/api/scooters/${id}/model`, { model });
    },
}

// 预约API
const bookingApi = {
    // 获取用户预约
    getUserBookings: (userId: number) => {
        return api.get(`/api/bookings/user/${userId}`)
    },
    // 创建预约
    createBooking: (bookingData: any) => {
        return api.post('/api/bookings', bookingData)
    },
    // 取消预约
    cancelBooking: (id: number) => {
        return api.delete(`/api/bookings/${id}`)
    },
    // 添加：根据 ID 获取单个预订详情
    getBookingById: (id: number) => {
        return api.get(`/api/bookings/${id}`)
    },
    // --- 添加 completeBooking 方法 START ---
    completeBooking: (id: number) => {
        // 调用后端新添加的 PUT /api/bookings/{id}/complete 端点
        return api.put(`/api/bookings/${id}/complete`)
    },
    // --- 添加 completeBooking 方法 END ---

    // --- START: Add extendBooking method ---
    extendBooking: (id: number, newDurationLabel: string) => {
        return api.put(`/api/bookings/${id}/extend`, { newDurationLabel });
    },
    // --- END: Add extendBooking method ---

    // --- START: Add getUserDiscountEligibility method ---
    getUserDiscountEligibility: (userId: number) => {
        return api.get(`/api/bookings/user/${userId}/discount`);
    }
    // --- END: Add getUserDiscountEligibility method ---
}

// 问题API
const issueApi = {
    // 
    reportIssue: (issueData: any) => {
        return api.post('/api/issues', issueData)
    },
    // 获取用户所有问题
    getUserIssues: (userId: number) => {
        return api.get(`/api/issues/user/${userId}`)
    }
}

// --- 添加 paymentApi START ---
const paymentApi = {
    // 为指定的 Booking 创建支付记录
    createPaymentForBooking: (bookingId: number) => {
        return api.post(`/api/payments/booking/${bookingId}`);
    },
    // 处理支付并更新订单状态
    processPayment: (paymentId: number) => {
        return api.post(`/api/payments/${paymentId}/process`);
    },
    // Add method to get payments for a booking
    getPaymentsByBooking: (bookingId: number) => {
        return api.get(`/api/payments/booking/${bookingId}`);
    }
};
// --- 添加 paymentApi END ---

// Add pricingApi for public pricing endpoint
const pricingApi = {
    getPriceForModel: (modelName: string): Promise<AxiosResponse<PriceResponse>> => {
        const encodedModelName = encodeURIComponent(modelName);
        return api.get(`/api/pricing/models/${encodedModelName}`);
    }
};

// --- Add Map API START ---
const mapApi = {
    getMapItems: (): Promise<AxiosResponse<MapItem[]>> => {
        return api.get('/api/map-items'); // Call the new backend endpoint
    }
};
// --- Add Map API END ---

// Export the new mapApi along with others
export { api, adminApi, bookingApi, issueApi, paymentApi, pricingApi, mapApi }
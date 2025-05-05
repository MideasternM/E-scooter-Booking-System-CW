import { api as apiClient } from './api'; // 使用命名导入并重命名为 apiClient
import type { RentalStore } from '../types/RentalStore'; // 使用相对路径
import type { RentalStoreDTO } from '../types/RentalStoreDTO'; // 使用相对路径

const BASE_URL = '/api/rental-stores';

export const rentalStoreService = {
    async getAllStores(): Promise<RentalStore[]> {
        const response = await apiClient.get(BASE_URL);
        return response.data;
    },

    async getStoreById(id: number): Promise<RentalStore> {
        const response = await apiClient.get(`${BASE_URL}/${id}`);
        return response.data;
    },

    async getStoreByCode(code: string): Promise<RentalStore> {
        const response = await apiClient.get(`${BASE_URL}/by-code/${code}`);
        return response.data;
    },

    async createStore(storeData: RentalStoreDTO): Promise<RentalStore> {
        const response = await apiClient.post(BASE_URL, storeData);
        return response.data;
    },

    async updateStore(id: number, storeData: RentalStoreDTO): Promise<RentalStore> {
        const response = await apiClient.put(`${BASE_URL}/${id}`, storeData);
        return response.data;
    },

    async deleteStore(id: number): Promise<void> {
        return apiClient.delete(`${BASE_URL}/${id}`);
    },

    async addScooterToStore(storeId: number, scooterCode: string): Promise<RentalStore> {
        const response = await apiClient.post(`${BASE_URL}/${storeId}/scooters`, { scooterCode });
        return response.data;
    },

    async removeScooterFromStore(storeId: number, scooterCode: string): Promise<RentalStore> {
        const response = await apiClient.delete(`${BASE_URL}/${storeId}/scooters/${scooterCode}`);
        return response.data;
    }
}; 
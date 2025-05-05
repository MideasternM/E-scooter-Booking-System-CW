import axios from 'axios';
import type { Scooter } from '@/types/Scooter';

// API Base URL
const API_URL = '/api/scooters';

export const scooterService = {
    /**
     * Get all scooters
     */
    async getAllScooters(): Promise<Scooter[]> {
        try {
            const response = await axios.get(API_URL);
            return response.data;
        } catch (error) {
            console.error('Error fetching scooters:', error);
            throw new Error('Failed to fetch scooters');
        }
    },

    /**
     * Get a specific scooter by ID
     */
    async getScooterById(id: number): Promise<Scooter> {
        try {
            const response = await axios.get(`${API_URL}/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching scooter ${id}:`, error);
            throw new Error(`Failed to fetch scooter ${id}`);
        }
    },

    /**
     * Update a scooter's status
     */
    async updateScooterStatus(id: number, status: string): Promise<Scooter> {
        try {
            const response = await axios.patch(`${API_URL}/${id}/status`, { status });
            return response.data;
        } catch (error) {
            console.error(`Error updating scooter ${id} status:`, error);
            throw new Error(`Failed to update scooter ${id} status`);
        }
    },

    /**
     * Get all available scooters (not assigned to any store)
     */
    async getAvailableScooters(): Promise<Scooter[]> {
        try {
            const response = await axios.get(`${API_URL}/available`);
            return response.data;
        } catch (error) {
            console.error('Error fetching available scooters:', error);
            throw new Error('Failed to fetch available scooters');
        }
    }
}; 
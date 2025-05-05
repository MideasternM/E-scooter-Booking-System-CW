import { defineStore } from 'pinia';
import { ref } from 'vue';
import { mapApi } from '@/services/api'; // Import the mapApi
import type { MapItem } from '@/types/MapItem';

export const useMapStore = defineStore('mapStore', () => {
    // State
    const mapItems = ref<MapItem[]>([]);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    // Actions
    async function fetchMapItems() {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await mapApi.getMapItems();
            mapItems.value = response.data;
            console.log('Fetched map items:', mapItems.value);
        } catch (err: any) {
            error.value = err.response?.data?.message || err.message || 'Failed to fetch map items';
            console.error('Error fetching map items:', err);
            mapItems.value = []; // Clear items on error
        } finally {
            isLoading.value = false;
        }
    }

    return {
        // State
        mapItems,
        isLoading,
        error,
        // Actions
        fetchMapItems,
    };
}); 
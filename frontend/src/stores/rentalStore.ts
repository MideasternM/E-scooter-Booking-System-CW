import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { rentalStoreService } from '@/services/rentalStoreService';
import type { RentalStore } from '@/types/RentalStore';
import type { RentalStoreDTO } from '@/types/RentalStoreDTO';

export const useRentalStore = defineStore('rentalStore', () => {
    // State
    const stores = ref<RentalStore[]>([]);
    const currentStore = ref<RentalStore | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    // Getters (Computed properties)
    const storeCount = computed(() => stores.value.length);

    // Actions
    async function fetchStores() {
        isLoading.value = true;
        error.value = null;
        try {
            const data = await rentalStoreService.getAllStores();
            stores.value = data;
        } catch (err: any) {
            error.value = err.message || 'Failed to fetch rental stores';
            console.error(error.value);
            // Optionally use a toast notification service
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchStoreById(id: number) {
        isLoading.value = true;
        error.value = null;
        currentStore.value = null;
        try {
            const data = await rentalStoreService.getStoreById(id);
            currentStore.value = data;
        } catch (err: any) {
            error.value = err.message || `Failed to fetch rental store ${id}`;
            console.error(error.value);
        } finally {
            isLoading.value = false;
        }
    }

    async function createStore(storeData: RentalStoreDTO): Promise<RentalStore | null> {
        isLoading.value = true;
        error.value = null;
        try {
            const newStore = await rentalStoreService.createStore(storeData);
            // Add to local state immediately or refetch list
            stores.value.push(newStore);
            // Or: await fetchStores();
            return newStore;
        } catch (err: any) {
            error.value = err.message || 'Failed to create rental store';
            console.error(error.value);
            return null;
        } finally {
            isLoading.value = false;
        }
    }

    async function updateStore(id: number, storeData: RentalStoreDTO): Promise<RentalStore | null> {
        isLoading.value = true;
        error.value = null;
        try {
            const updatedStore = await rentalStoreService.updateStore(id, storeData);
            // Update local state
            const index = stores.value.findIndex(s => s.id === id);
            if (index !== -1) {
                stores.value[index] = updatedStore;
            }
            if (currentStore.value?.id === id) {
                currentStore.value = updatedStore;
            }
            return updatedStore;
        } catch (err: any) {
            error.value = err.message || `Failed to update rental store ${id}`;
            console.error(error.value);
            return null;
        } finally {
            isLoading.value = false;
        }
    }

    async function deleteStore(id: number): Promise<boolean> {
        isLoading.value = true;
        error.value = null;
        try {
            await rentalStoreService.deleteStore(id);
            // Remove from local state
            stores.value = stores.value.filter(s => s.id !== id);
            if (currentStore.value?.id === id) {
                currentStore.value = null;
            }
            return true;
        } catch (err: any) {
            error.value = err.message || `Failed to delete rental store ${id}`;
            console.error(error.value);
            return false;
        } finally {
            isLoading.value = false;
        }
    }

    // --- Scooter Management Actions ---
    async function addScooter(storeId: number, scooterCode: string): Promise<RentalStore | null> {
        isLoading.value = true;
        error.value = null;
        try {
            const updatedStore = await rentalStoreService.addScooterToStore(storeId, scooterCode);
            // Update local state for the specific store
            const index = stores.value.findIndex(s => s.id === storeId);
            if (index !== -1) {
                stores.value[index] = updatedStore;
            }
            if (currentStore.value?.id === storeId) {
                currentStore.value = updatedStore;
            }
            return updatedStore;
        } catch (err: any) {
            error.value = err.message || `Failed to add scooter ${scooterCode} to store ${storeId}`;
            console.error(error.value);
            return null;
        } finally {
            isLoading.value = false;
        }
    }

    async function removeScooter(storeId: number, scooterCode: string): Promise<RentalStore | null> {
        isLoading.value = true;
        error.value = null;
        try {
            const updatedStore = await rentalStoreService.removeScooterFromStore(storeId, scooterCode);
            // Update local state
            const index = stores.value.findIndex(s => s.id === storeId);
            if (index !== -1) {
                stores.value[index] = updatedStore;
            }
            if (currentStore.value?.id === storeId) {
                currentStore.value = updatedStore;
            }
            return updatedStore;
        } catch (err: any) {
            error.value = err.message || `Failed to remove scooter ${scooterCode} from store ${storeId}`;
            console.error(error.value);
            return null;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        // State
        stores,
        currentStore,
        isLoading,
        error,
        // Getters
        storeCount,
        // Actions
        fetchStores,
        fetchStoreById,
        createStore,
        updateStore,
        deleteStore,
        addScooter,
        removeScooter
    };
}); 
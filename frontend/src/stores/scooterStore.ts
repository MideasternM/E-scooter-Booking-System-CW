import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { scooterService } from '@/services/scooterService';
import type { Scooter } from '@/types/Scooter';

export const useScooterStore = defineStore('scooterStore', () => {
    // State
    const scooters = ref<Scooter[]>([]);
    const currentScooter = ref<Scooter | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    // Getters
    const scooterCount = computed(() => scooters.value.length);
    const availableScooters = computed(() =>
        scooters.value.filter(scooter => scooter.available)
    );

    // Actions
    async function fetchScooters() {
        isLoading.value = true;
        error.value = null;
        try {
            const data = await scooterService.getAllScooters();
            scooters.value = data;
        } catch (err: any) {
            error.value = err.message || 'Failed to fetch scooters';
            console.error(error.value);
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchScooterById(id: number) {
        isLoading.value = true;
        error.value = null;
        currentScooter.value = null;
        try {
            const data = await scooterService.getScooterById(id);
            currentScooter.value = data;
        } catch (err: any) {
            error.value = err.message || `Failed to fetch scooter ${id}`;
            console.error(error.value);
        } finally {
            isLoading.value = false;
        }
    }

    async function updateScooterStatus(id: number, status: string): Promise<Scooter | null> {
        isLoading.value = true;
        error.value = null;
        try {
            const updatedScooter = await scooterService.updateScooterStatus(id, status);
            // Update local state
            const index = scooters.value.findIndex(s => s.id === id);
            if (index !== -1) {
                scooters.value[index] = updatedScooter;
            }
            if (currentScooter.value?.id === id) {
                currentScooter.value = updatedScooter;
            }
            return updatedScooter;
        } catch (err: any) {
            error.value = err.message || `Failed to update scooter ${id} status`;
            console.error(error.value);
            return null;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        // State
        scooters,
        currentScooter,
        isLoading,
        error,
        // Getters
        scooterCount,
        availableScooters,
        // Actions
        fetchScooters,
        fetchScooterById,
        updateScooterStatus
    };
}); 
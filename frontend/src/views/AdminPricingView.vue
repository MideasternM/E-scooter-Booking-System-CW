<template>
    <div class="admin-pricing">
        <h1>Manage Model Pricing</h1>
        <p>Configure the price per minute for different scooter models.</p>

        <div v-if="isLoading" class="loading-message">Loading pricing info...</div>
        <div v-else-if="apiError" class="error-message">{{ apiError }}</div>
        
        <div v-else-if="Object.keys(modelPrices).length === 0" class="empty-message">
            No scooter models found or no prices configured. Add scooters with models first.
        </div>

        <div v-else class="pricing-table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Scooter Model</th>
                        <th>Current Price Per Minute ($)</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(price, model) in modelPrices" :key="model as string">
                        <td>{{ model }}</td>
                        <td>
                            <span v-if="!isEditing(model as string)">{{ price.toFixed(2) }}</span>
                            <input v-else 
                                   type="number" 
                                   v-model.number="editPriceForm[model as string]" 
                                   min="0.01" 
                                   step="0.01"
                                   class="price-input" />
                        </td>
                        <td class="actions-cell">
                            <button v-if="!isEditing(model as string)" 
                                    @click="startEditing(model as string, price)" 
                                    class="action-btn edit">Edit</button>
                            <template v-else>
                                <button @click="savePrice(model as string)" 
                                        class="action-btn save" 
                                        :disabled="saveInProgress[model as string]">
                                    {{ saveInProgress[model as string] ? 'Saving...' : 'Save' }}
                                </button>
                                <button @click="cancelEditing(model as string)" 
                                        class="action-btn cancel"
                                        :disabled="saveInProgress[model as string]">Cancel</button>
                            </template>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { adminApi } from '../services/api';

// Type for the prices fetched from API
interface ModelPrices {
    [modelName: string]: number;
}

// State
const isLoading = ref(true);
const apiError = ref<string | null>(null);
const modelPrices = ref<ModelPrices>({});

// State for inline editing
const editingModel = ref<string | null>(null);
const editPriceForm = reactive<{ [key: string]: number }>({});
const saveInProgress = reactive<{ [key: string]: boolean }>({});

// Fetch all model prices on component mount
const fetchModelPrices = async () => {
    isLoading.value = true;
    apiError.value = null;
    try {
        const response = await adminApi.getAllModelPrices();
        modelPrices.value = response.data || {};
        // Initialize form and saving state for each model
        Object.keys(modelPrices.value).forEach(model => {
            if (editPriceForm[model] === undefined) { // Avoid overwriting during edit
                 editPriceForm[model] = modelPrices.value[model];
            }
            saveInProgress[model] = false;
        });
    } catch (err: any) {
        console.error("Failed to fetch model prices:", err);
        apiError.value = `Error loading prices: ${err.response?.data?.message || err.message}`;
        modelPrices.value = {};
    } finally {
        isLoading.value = false;
    }
};

// Functions for inline editing
const isEditing = (model: string): boolean => {
    return editingModel.value === model;
};

const startEditing = (model: string, currentPrice: number) => {
    // Reset any previous edits
    if (editingModel.value && editingModel.value !== model) {
        editPriceForm[editingModel.value] = modelPrices.value[editingModel.value];
    }
    editPriceForm[model] = currentPrice; // Ensure form starts with current price
    editingModel.value = model;
};

const cancelEditing = (model: string) => {
    if (editingModel.value === model) {
        editPriceForm[model] = modelPrices.value[model]; // Reset value on cancel
        editingModel.value = null;
    }
};

const savePrice = async (model: string) => {
    const newPrice = editPriceForm[model];
    if (newPrice === undefined || newPrice <= 0) {
        alert("Please enter a valid positive price.");
        return;
    }

    saveInProgress[model] = true;
    apiError.value = null; // Clear previous errors

    try {
        await adminApi.updateModelPrice(model, newPrice);
        // Update local state on success
        modelPrices.value[model] = newPrice;
        editingModel.value = null; // Exit editing mode
        alert(`Price for model '${model}' updated successfully.`);
    } catch (err: any) {
        console.error(`Failed to save price for model ${model}:`, err);
        apiError.value = `Error saving price for ${model}: ${err.response?.data?.message || err.message}`;
        // Optionally reset the form value to the original price on error
        // editPriceForm[model] = modelPrices.value[model]; 
    } finally {
        saveInProgress[model] = false;
    }
};


onMounted(() => {
    fetchModelPrices();
});

</script>

<style scoped>
.admin-pricing {
    padding: 2rem;
}

h1 {
    color: #2c3e50;
    margin-bottom: 0.5rem;
}

p {
    color: #606f7b;
    margin-bottom: 2rem;
}

.loading-message,
.error-message,
.empty-message {
    padding: 2rem;
    text-align: center;
    color: #606f7b;
    background-color: #f8f9fa;
    border-radius: 8px;
    margin-bottom: 1.5rem;
}

.error-message {
    color: #e74c3c;
    background-color: #fceded;
    border: 1px solid #e74c3c;
}

.pricing-table-container {
    background: white;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    border: 1px solid var(--border-color, #eee);
}

.data-table {
    width: 100%;
    min-width: 500px; /* Adjust based on columns */
    border-collapse: collapse;
}

.data-table th,
.data-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #eee;
}

.data-table th {
    background-color: #f8f9fa;
    color: #606f7b;
    font-weight: 500;
}

.price-input {
    padding: 0.4rem 0.6rem;
    border: 1px solid #ccc;
    border-radius: 4px;
    max-width: 100px; /* Adjust width as needed */
}

.actions-cell {
    display: flex;
    gap: 0.5rem;
    white-space: nowrap;
}

.action-btn {
    padding: 0.4rem 0.8rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9em;
    transition: background-color 0.2s;
}

.action-btn.edit {
    background-color: #3498db;
    color: white;
}
.action-btn.edit:hover {
    background-color: #2980b9;
}

.action-btn.save {
    background-color: #2ecc71;
    color: white;
}
.action-btn.save:hover:not(:disabled) {
    background-color: #27ae60;
}

.action-btn.cancel {
    background-color: #95a5a6;
    color: white;
}
.action-btn.cancel:hover:not(:disabled) {
    background-color: #7f8c8d;
}

.action-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
    .admin-pricing {
        padding: 1rem;
    }
    h1 {
        font-size: 1.5rem;
    }
    p {
        margin-bottom: 1rem;
        font-size: 0.9rem;
    }
    .loading-message,
    .error-message,
    .empty-message {
        padding: 1rem;
        font-size: 0.9rem;
    }

    .data-table {
        min-width: 400px;
    }
    .data-table th,
    .data-table td {
        padding: 0.75rem 0.5rem;
        font-size: 0.85rem;
    }
    .price-input {
        padding: 0.3rem 0.5rem;
        font-size: 0.85rem;
        max-width: 80px;
    }
    .actions-cell .action-btn {
        padding: 0.3rem 0.6rem;
        font-size: 0.8rem;
    }
}

@media (max-width: 576px) {
     /* Further adjustments if needed, e.g., stacking table cells */
    .data-table th,
    .data-table td {
        white-space: nowrap;
    }
}

</style>
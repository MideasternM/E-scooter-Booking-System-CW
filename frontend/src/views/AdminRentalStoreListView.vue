<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRentalStore } from '@/stores/rentalStore';
import type { RentalStore } from '@/types/RentalStore';
import type { RentalStoreDTO } from '@/types/RentalStoreDTO'; 
import { StoreStatus } from '@/types/RentalStore';
import RentalStoreForm from '@/components/RentalStoreForm.vue';
import StoreScooterManager from '@/components/StoreScooterManager.vue';
// Assuming Base components exist and are globally registered or imported elsewhere
// import BaseCard from '@/components/BaseCard.vue'; 
// import BaseButton from '@/components/BaseButton.vue';
// import Modal from '@/components/Modal.vue';
// import Spinner from '@/components/Spinner.vue';
// import Alert from '@/components/Alert.vue';

const rentalStore = useRentalStore();

// State refs for modals and selected item
const isModalOpen = ref(false);
const isManageScootersModalOpen = ref(false);
const selectedStore = ref<RentalStore | null>(null);

// Computed properties to get state from store
const stores = computed(() => rentalStore.stores);
const isLoading = computed(() => rentalStore.isLoading);
const error = computed(() => rentalStore.error);

// Fetch stores when component mounts
onMounted(() => {
  rentalStore.fetchStores();
});

// --- Modal Handling Functions --- 
function openAddModal() {
  selectedStore.value = null;
  isModalOpen.value = true;
}

function openEditModal(store: RentalStore) {
  selectedStore.value = { ...store };
  isModalOpen.value = true;
}

function closeModal() {
  isModalOpen.value = false;
  selectedStore.value = null;
}

function openManageScootersModal(store: RentalStore) {
  selectedStore.value = { ...store };
  isManageScootersModalOpen.value = true;
}

function closeManageScootersModal() {
  isManageScootersModalOpen.value = false;
  selectedStore.value = null;
}

// --- Action Handlers --- 
async function handleSave(storeData: RentalStoreDTO) {
  console.log('Store data to save:', storeData);
  console.log('Status value:', storeData.status);
  
  let success = false;
  if (selectedStore.value && selectedStore.value.id) {
    success = !!(await rentalStore.updateStore(selectedStore.value.id, storeData));
  } else {
    success = !!(await rentalStore.createStore(storeData));
  }
  
  if (success) {
    closeModal();
  } else {
    // Use a more integrated notification system if available
    alert(`Failed to save store: ${rentalStore.error || 'Unknown error'}`); 
  }
}

async function confirmDelete(store: RentalStore) {
  // Use a confirmation dialog component if available
  if (confirm(`Are you sure you want to delete store "${store.name}" (ID: ${store.id})? This action cannot be undone.`)) {
    const deleted = await rentalStore.deleteStore(store.id);
    if (!deleted) {
      alert(`Failed to delete store: ${rentalStore.error || 'Unknown error'}`);
    }
  }
}

// Handle store update from scooter manager
function handleStoreUpdate(updatedStore: RentalStore) {
  if (selectedStore.value) {
    selectedStore.value = updatedStore;
  }
  // Potentially find and update the store in the main list as well
  const index = stores.value.findIndex(s => s.id === updatedStore.id);
  if (index !== -1) {
    stores.value[index] = updatedStore; 
  }
}

// --- Helper Functions ---
// Status styling (adjust classes based on your design system/Tailwind config)
function getStatusClass(status: StoreStatus): string {
  switch (status) {
    case StoreStatus.OPERATIONAL:
      return 'bg-green-100 text-green-800';
    case StoreStatus.CLOSED_TEMPORARY:
      return 'bg-yellow-100 text-yellow-800';
    case StoreStatus.CLOSED_PERMANENT:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

function getStatusText(status: StoreStatus): string {
  switch (status) {
    case StoreStatus.OPERATIONAL:
      return 'Operational';
    case StoreStatus.CLOSED_TEMPORARY:
      return 'Temporarily Closed';
    case StoreStatus.CLOSED_PERMANENT:
      return 'Permanently Closed';
    default:
      return 'Unknown';
  }
}
</script>

<template>
    <div class="admin-scooters">
        <div class="page-header">
            <h1>Manage Rental Stores</h1>
            <button class="add-button" @click="openAddModal">
                <span class="icon">+</span>
                <span>Add Store</span>
            </button>
        </div>

        <div v-if="error" class="alert alert-danger">
            <strong>Error!</strong> {{ error }}
        </div>
        
        <!-- Loading state -->
        <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <span>Loading stores...</span>
        </div>

        <!-- Data table -->
        <div v-else-if="stores.length > 0" class="data-table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Code</th>
                        <th>Address</th>
                        <th>Status</th>
                        <th>Scooters</th>
                        <th class="actions-column">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="store in stores" :key="store.id" class="data-row">
                        <td>
                            <div class="store-info">
                                <div class="store-icon">
                                    <span class="store-emoji">🏪</span>
                                </div>
                                <div class="store-details">
                                    <span class="store-name">{{ store.name }}</span>
                                    <span class="store-date">Created: {{ new Date(store.createdAt).toLocaleDateString() }}</span>
                                </div>
                            </div>
                        </td>
                        <td>{{ store.code }}</td>
                        <td>
                            <div class="address-info">
                                <div>{{ store.address }}</div>
                                <small>{{ store.openingHours }}</small>
                            </div>
                        </td>
                        <td>
                            <span :class="['status-badge', getStatusClass(store.status)]">
                                {{ getStatusText(store.status) }}
                            </span>
                        </td>
                        <td class="scooter-count">
                            {{ store.availableScooters?.length ?? 0 }}
                        </td>
                        <td class="actions-cell">
                            <div class="action-buttons-container">
                                <button class="action-btn edit" @click="openEditModal(store)">
                                    Edit
                                </button>
                                <button class="action-btn manage" @click="openManageScootersModal(store)">
                                    Scooters
                                </button>
                                <button class="action-btn delete" @click="confirmDelete(store)">
                                    Delete
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        
        <!-- Empty state -->
        <div v-else class="empty-state">
            <div class="empty-icon">🏪</div>
            <h3>No Rental Stores Found</h3>
            <p>There are currently no rental stores to display. Start by adding your first store.</p>
            <button class="btn btn-primary" @click="openAddModal">Add Your First Store</button>
        </div>
        
        <!-- Add/Edit Modal -->
        <teleport to="body">
            <div v-if="isModalOpen" class="modal-overlay active">
                <div class="modal-container">
                    <RentalStoreForm
                        :store-to-edit="selectedStore"
                        @save="handleSave"
                        @cancel="closeModal"
                    />
                </div>
            </div>
        </teleport>
        
        <!-- Manage Scooters Modal -->
        <teleport to="body">
            <div v-if="isManageScootersModalOpen" class="modal-overlay active">
                <div class="modal-container">
                    <StoreScooterManager 
                        :store="selectedStore"
                        @close="closeManageScootersModal"
                        @update="handleStoreUpdate"
                    />
                </div>
            </div>
        </teleport>
    </div>
</template>

<style scoped>
.admin-scooters {
    padding: 0 0 2rem 0;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.page-header h1 {
    margin-bottom: 0;
    font-size: 1.75rem;
    font-weight: 600;
    color: var(--text-primary);
}

.add-button {
    display: flex;
    align-items: center;
    padding: 0.6rem 1.2rem;
    background-color: var(--primary-600);
    color: white;
    border: none;
    border-radius: var(--radius-md);
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
}

.add-button:hover {
    background-color: var(--primary-700);
}

.add-button .icon {
    font-size: 1.2rem;
    margin-right: 0.5rem;
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 0;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 3px solid rgba(var(--primary-500-rgb), 0.2);
    border-top-color: var(--primary-500);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1rem;
}

.data-table-container {
    background-color: var(--card-bg);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    overflow: hidden;
    border: 1px solid var(--border-color);
}

.data-table {
    width: 100%;
    border-collapse: collapse;
}

.data-table th {
    text-align: left;
    padding: 1rem;
    background-color: rgba(0, 0, 0, 0.02);
    font-weight: 600;
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-secondary);
    border-bottom: 1px solid var(--border-color);
}

.data-table td {
    padding: 1rem;
    border-bottom: 1px solid var(--border-color);
    vertical-align: middle;
}

.data-row:hover {
    background-color: rgba(0, 0, 0, 0.02);
}

.store-info {
    display: flex;
    align-items: center;
}

.store-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: var(--primary-100);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 0.75rem;
}

.store-emoji {
    font-size: 1.2rem;
}

.store-details {
    display: flex;
    flex-direction: column;
}

.store-name {
    font-weight: 500;
    color: var(--text-primary);
}

.store-date {
    font-size: 0.75rem;
    color: var(--text-muted);
}

.address-info {
    display: flex;
    flex-direction: column;
}

.address-info small {
    font-size: 0.75rem;
    color: var(--text-muted);
    margin-top: 0.25rem;
}

.status-badge {
    display: inline-flex;
    align-items: center;
    padding: 0.25rem 0.75rem;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 500;
}

.status-badge.bg-green-100 {
    background-color: var(--success-light);
    color: var(--success);
}

.status-badge.bg-yellow-100 {
    background-color: var(--warning-light);
    color: var(--warning);
}

.status-badge.bg-red-100 {
    background-color: var(--error-light);
    color: var(--error);
}

.status-badge::before {
    content: "";
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 0.5rem;
}

.status-badge.bg-green-100::before {
    background-color: var(--success);
}

.status-badge.bg-yellow-100::before {
    background-color: var(--warning);
}

.status-badge.bg-red-100::before {
    background-color: var(--error);
}

.scooter-count {
    font-weight: 500;
    text-align: center;
}

.actions-column {
    text-align: center;
    width: 310px;
}

.actions-cell {
    padding: 0.5rem !important;
}

.action-buttons-container {
    display: flex;
    justify-content: space-between;
    width: 100%;
}

.action-btn {
    flex: 0 0 auto;
    width: 31%;
    padding: 0.5rem 0;
    border: none;
    border-radius: var(--radius-md);
    font-size: 0.8rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    text-align: center;
}

.empty-state {
    background-color: var(--card-bg);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    padding: 3rem 2rem;
    text-align: center;
    border: 1px solid var(--border-color);
}

.empty-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.empty-state h3 {
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.empty-state p {
    color: var(--text-secondary);
    max-width: 400px;
    margin: 0 auto 1.5rem auto;
}

/* Modal styles updated to match Modal.vue component */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: flex-start;
    justify-content: center;
    z-index: var(--z-modal);
    overflow-y: auto;
    padding: var(--space-lg) 0;
    opacity: 0;
    visibility: hidden;
    transition: opacity var(--transition-normal);
}

.modal-overlay.active {
    opacity: 1;
    visibility: visible;
}

.modal-container {
    background-color: var(--card-bg);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-xl);
    width: 100%;
    margin: 0 var(--space-lg);
    max-width: 800px; /* Match with other modals' size */
    max-height: calc(100vh - 2 * var(--space-lg));
    overflow-y: auto;
    transform: scale(0.95);
    opacity: 0;
    transition: transform var(--transition-normal), opacity var(--transition-normal);
    display: flex;
    flex-direction: column;
}

.modal-overlay.active .modal-container {
    transform: scale(1);
    opacity: 1;
}

/* Alert styles */
.alert {
    padding: 1rem;
    border-radius: var(--radius-md);
    margin-bottom: 1.5rem;
}

.alert-danger {
    background-color: var(--error-light);
    color: var(--error);
    border-left: 4px solid var(--error);
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

@media (max-width: 768px) {
    .actions-cell {
        padding: 0.5rem !important;
    }
    
    .action-buttons-container {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .action-btn {
        display: block;
        width: 100%;
    }
}

.action-btn.edit {
    background-color: var(--info-light);
    color: var(--info);
}

.action-btn.edit:hover {
    background-color: var(--info);
    color: white;
}

.action-btn.manage {
    background-color: var(--secondary-100);
    color: var(--secondary-600);
}

.action-btn.manage:hover {
    background-color: var(--secondary-600);
    color: white;
}

.action-btn.delete {
    background-color: var(--error-light);
    color: var(--error);
}

.action-btn.delete:hover {
    background-color: var(--error);
    color: white;
}
</style> 
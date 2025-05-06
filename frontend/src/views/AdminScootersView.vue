<template>
    <div class="admin-scooters">
        <div class="page-header">
            <h1>Scooter Management</h1>
            <button class="add-button" @click="showAddModal = true">
                Add New Scooter
            </button>
        </div>

        <div class="filters">
            <div class="search-box">
                <input v-model="searchQuery" type="text" placeholder="Search by ID or location" />
            </div>
            <select v-model="filterStatus">
                <option value="all">All Status</option>
                <option value="available">Available</option>
                <option value="in-use">In Use</option>
                <option value="maintenance">Maintenance</option>
            </select>
            <select v-model="sortBy">
                <option value="id">Sort by ID</option>
                <option value="battery">Sort by Battery</option>
                <option value="location">Sort by Location</option>
            </select>
        </div>

        <!-- Map Container -->
        <div class="map-container">
             <div id="scooter-map"></div>
        </div>

        <!-- Add Loading/Error/Empty States -->
        <div v-if="isLoading" class="loading-message">Loading scooters...</div>
        <div v-else-if="apiError" class="error-message">{{ apiError }}</div>
        <div v-else-if="scooters.length === 0" class="empty-message">No scooters found.</div>

        <div v-else class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Model</th>
                        <th>Status</th>
                        <th>Battery</th>
                        <th>Location</th>
                        <th>Last Maintenance</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="scooter in filteredScooters" :key="scooter.id">
                        <td>#{{ scooter.id }}</td>
                        <td>{{ scooter.model || 'N/A' }}</td>
                        <td>
                            <span class="status-badge" :class="(scooter.status || '').toLowerCase().replace(' ', '-')">
                                {{ scooter.status || 'Unknown' }}
                            </span>
                        </td>
                        <td>
                            <div class="battery-indicator">
                                <div class="battery-bar">
                                    <div :style="{ width: scooter.batteryLevel + '%' }"
                                        :class="getBatteryClass(scooter.batteryLevel)">
                                    </div>
                                </div>
                                <span>{{ scooter.batteryLevel }}%</span>
                            </div>
                        </td>
                        <td>{{ scooter.location }}</td>
                        <td>{{ formatDate(scooter.lastMaintenanceDate) }}</td>
                        <td class="actions-cell">
                            <button class="action-btn edit" @click="editScooter(scooter)">
                                Edit
                            </button>
                            <button class="action-btn maintenance" @click="toggleMaintenance(scooter)">
                                {{ scooter.status === 'Maintenance' ? 'Set Available' : 'Set Maintenance' }}
                            </button>
                            <button v-if="scooter.status === 'Available'" 
                                    class="action-btn book-guest" 
                                    @click="openGuestBookingModal(scooter)">
                                Book for Guest
                            </button>
                            <button class="action-btn delete" @click="confirmDelete(scooter)">
                                Delete
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Add Scooter Modal -->
        <div v-if="showAddModal" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>Add New Scooter</h3>
                    <button class="close-btn" @click="showAddModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <form @submit.prevent="addScooter">
                        <div class="form-group">
                            <label>Scooter Code:</label>
                            <input v-model="newScooterForm.scooterCode" type="text" required />
                        </div>
                        <div class="form-group">
                            <label>Model:</label>
                            <input v-model="newScooterForm.model" type="text" />
                        </div>
                        <div class="form-group">
                            <label>Status:</label>
                            <select v-model="newScooterForm.status" required>
                                <option value="Available">Available</option>
                                <option value="In Use">In Use</option>
                                <option value="Maintenance">Maintenance</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Battery Level (%):</label>
                            <input v-model="newScooterForm.batteryLevel" type="number" min="0" max="100" required />
                        </div>
                        <div class="form-group">
                            <label>Latitude:</label>
                            <input v-model="newScooterForm.latitude" type="number" step="0.000001" required placeholder="e.g. 40.7128" @change="handleNewScooterCoordinateChange" />
                        </div>
                        <div class="form-group">
                            <label>Longitude:</label>
                            <input v-model="newScooterForm.longitude" type="number" step="0.000001" required placeholder="e.g. -74.0060" @change="handleNewScooterCoordinateChange" />
                        </div>
                        <div class="form-group">
                            <label>Location:</label>
                            <div class="location-input-group">
                            <input v-model="newScooterForm.location" type="text" required />
                                <button type="button" class="location-btn" @click="getNewScooterLocationFromCoordinates" :disabled="isNewLocationLoading">
                                    {{ isNewLocationLoading ? 'Loading...' : 'Get from Coordinates' }}
                                </button>
                            </div>
                            <small v-if="newLocationError" class="error-text">{{ newLocationError }}</small>
                        </div>
                        <div class="form-group">
                            <label>Last Maintenance:</label>
                            <input v-model="newScooterForm.lastMaintenance" type="date" required />
                        </div>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="showAddModal = false">Cancel</button>
                            <button type="submit" class="submit-btn">Add Scooter</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Edit Scooter Modal -->
        <div v-if="showEditModal && editingScooter" class="modal-overlay">
            <div class="modal-container">
                <div class="modal-header">
                    <h3>Edit Scooter #{{ editingScooter.id }}</h3>
                    <button class="close-btn" @click="showEditModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <form v-if="editingScooter" @submit.prevent="updateScooter">
                        <div class="form-group">
                            <label>Model:</label>
                            <input v-model="editScooterForm.model" type="text" placeholder="e.g. Standard, Premium, etc." />
                        </div>
                        <div class="form-group">
                            <label>Status:</label>
                            <select v-model="editScooterForm.status" required>
                                <option value="Available">Available</option>
                                <option value="In Use">In Use</option>
                                <option value="Maintenance">Maintenance</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Battery Level (%):</label>
                            <input v-model="editScooterForm.batteryLevel" type="number" min="0" max="100" required />
                        </div>
                        <div class="form-group">
                            <label>Location:</label>
                            <div class="location-input-group">
                            <input v-model="editScooterForm.location" type="text" required />
                                <button type="button" class="location-btn" @click="getLocationFromCoordinates" :disabled="isLocationLoading">
                                    {{ isLocationLoading ? 'Loading...' : 'Get from Coordinates' }}
                                </button>
                            </div>
                            <small v-if="locationError" class="error-text">{{ locationError }}</small>
                        </div>
                        <div class="form-group">
                            <label>Latitude:</label>
                            <input v-model="editScooterForm.latitude" type="number" step="0.000001" required placeholder="e.g. 40.7128" @change="handleCoordinateChange" />
                        </div>
                        <div class="form-group">
                            <label>Longitude:</label>
                            <input v-model="editScooterForm.longitude" type="number" step="0.000001" required placeholder="e.g. -74.0060" @change="handleCoordinateChange" />
                        </div>
                        <div class="form-group">
                            <label>Last Maintenance:</label>
                            <input v-model="editScooterForm.lastMaintenance" type="date" required />
                        </div>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="showEditModal = false">Cancel</button>
                            <button type="submit" class="submit-btn">Update Scooter</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Delete Confirmation Modal -->
        <div v-if="showDeleteModal && deletingScooter" class="modal-overlay">
            <div class="modal-container delete-modal">
                <div class="modal-header">
                    <h3>Confirm Delete</h3>
                    <button class="close-btn" @click="showDeleteModal = false">&times;</button>
                </div>
                <div class="modal-body">
                    <div v-if="deletingScooter">
                        <p>Are you sure you want to delete Scooter #{{ deletingScooter.id }}?</p>
                        <p class="warning">This action cannot be undone.</p>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="showDeleteModal = false">Cancel</button>
                            <button type="button" class="delete-btn" @click="deleteScooter">Delete</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- START: Guest Booking Modal -->
        <div v-if="showGuestBookingModal && scooterForGuestBooking" class="modal-overlay">
            <div class="modal-container guest-booking-modal">
                <div class="modal-header">
                    <h3>Book Scooter #{{ scooterForGuestBooking.id }} for Guest</h3>
                    <button class="close-btn" @click="closeGuestBookingModal">&times;</button>
                </div>
                <div class="modal-body">
                    <form @submit.prevent="submitGuestBooking">
                        <div class="form-group">
                            <label>Guest Email:</label>
                            <input v-model="guestBookingForm.guestEmail" type="email" required placeholder="guest@example.com" />
                            <span v-if="guestBookingForm.guestEmail && !isValidEmail(guestBookingForm.guestEmail)" class="validation-error">Please enter a valid email address.</span>
                        </div>
                        <div class="form-group">
                            <label>Guest Name (Optional):</label>
                            <input v-model="guestBookingForm.guestName" type="text" placeholder="Guest Name"/>
                        </div>
                        <div class="form-group">
                            <label>Booking Duration:</label>
                            <select v-model="guestBookingForm.selectedDurationLabel" required>
                                <option disabled value="">-- Select duration --</option>
                                <option v-for="option in durationOptions" :key="option.value" :value="option.label">
                                    {{ option.label }}
                                </option>
                            </select>
                        </div>
                         <div v-if="guestBookingError" class="error api-error">{{ guestBookingError }}</div>
                        <div class="form-actions">
                            <button type="button" class="cancel-btn" @click="closeGuestBookingModal">Cancel</button>
                            <button type="submit" class="submit-btn" :disabled="isGuestBookingLoading || !isValidGuestBookingForm">
                                {{ isGuestBookingLoading ? 'Booking...' : 'Confirm Booking' }}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <!-- END: Guest Booking Modal -->
    </div>
</template>

<script setup lang="ts">
// Import necessary functions and types
import { ref, computed, onMounted, reactive, watch, nextTick } from 'vue'
import { adminApi, api } from '../services/api'
import { parseISO, format as formatDateFn, isValid as isDateValid } from 'date-fns'
// Change Leaflet import back to default import and ignore TS error
// @ts-ignore 
import L from 'leaflet'; 
import 'leaflet/dist/leaflet.css' // Import Leaflet CSS

// --- START: Define Custom DivIcon using Emoji ---
const scooterDivIcon = L.divIcon({
    html: '🛴', // The scooter emoji
    className: 'leaflet-emoji-icon', // Custom class for potential styling
    iconSize: [30, 30], // Approximate size (adjust as needed)
    iconAnchor: [15, 30], // Anchor point (half width, full height - centers bottom)
    popupAnchor: [0, -30] // Popup position relative to anchor
});
// --- END: Define Custom DivIcon using Emoji ---

// Interface for API Response
interface ApiScooterResponse {
    id: number;
    scooterCode?: string; // Assuming API might return scooterCode
    model?: string;
    status?: string | null;
    batteryLevel: number;
    location: string;
    lastMaintenance?: string | null; // Expect string from API
    available?: boolean; // API might return this instead of status?
    latitude?: number | null;
    longitude?: number | null;
}

// Interface for internal component data
interface ProcessedScooter {
    id: number;
    scooterCode?: string;
    model?: string;
    status: string; // Ensure always string
    batteryLevel: number;
    location: string;
    lastMaintenanceDate: Date | null; // Store as Date object
    latitude?: number | null;
    longitude?: number | null;
}

// Type for the add/edit form (uses string for date)
interface ScooterForm {
    id?: number | null;
    scooterCode?: string;
    model?: string;
    status: string;
    batteryLevel: number;
    location: string;
    latitude?: number | null;
    longitude?: number | null;
    lastMaintenance: string; // YYYY-MM-DD format for input type=date
}

// Duration Options (Copied from BookingListView/CreateView logic)
interface DurationOption {
    label: string;
    value: number; // minutes (though value not directly used here, useful for consistency)
}
const durationOptions: DurationOption[] = [
    { label: '1 Hour', value: 60 },
    { label: '4 Hours', value: 240 },
    { label: '1 Day', value: 1440 },
    { label: '1 Week', value: 10080 }
];

// State variables
const scooters = ref<ProcessedScooter[]>([])
const isLoading = ref(true)
const apiError = ref<string | null>(null)
const searchQuery = ref('')
const filterStatus = ref('all')
const sortBy = ref('id')

// Modal state
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const showGuestBookingModal = ref(false)

// Form data - Use reactive for objects
const newScooterForm = reactive<ScooterForm>({
    scooterCode: '',
    model: 'Standard',
    status: 'Available',
    batteryLevel: 100,
    location: '',
    latitude: 0,
    longitude: 0,
    lastMaintenance: new Date().toISOString().split('T')[0] // Current date in YYYY-MM-DD
})

const editScooterForm = reactive<ScooterForm>({
    id: null,
    scooterCode: '',
    model: '',
    status: '',
    batteryLevel: 0,
    location: '',
    lastMaintenance: ''
})

const editingScooter = ref<ProcessedScooter | null>(null)
const deletingScooter = ref<ProcessedScooter | null>(null)
const scooterForGuestBooking = ref<ProcessedScooter | null>(null)

// Guest Booking state
const guestBookingForm = reactive({
    guestEmail: '',
    guestName: '',
    selectedDurationLabel: ''
})
const isGuestBookingLoading = ref(false)
const guestBookingError = ref<string | null>(null)

// Map state
const mapInstance = ref<L.Map | null>(null)
const scooterMarkersLayer = ref<L.LayerGroup | null>(null)

// 添加地理编码相关的状态变量
const isLocationLoading = ref(false)
const locationError = ref<string | null>(null)

// 添加用于新滑板车表单的地理编码状态
const isNewLocationLoading = ref(false)
const newLocationError = ref<string | null>(null)

// Helper function to format Date to YYYY-MM-DD
const formatDateToInput = (date: Date | null): string => {
    if (!date || isNaN(date.getTime())) return ''
    return date.toISOString().split('T')[0]
}

// Watcher to update edit form when editingScooter changes
watch(editingScooter, (newVal) => {
    if (newVal) {
        editScooterForm.id = newVal.id
        editScooterForm.scooterCode = newVal.scooterCode || ''
        editScooterForm.model = newVal.model || ''
        editScooterForm.status = newVal.status
        editScooterForm.batteryLevel = newVal.batteryLevel
        editScooterForm.location = newVal.location
        editScooterForm.latitude = newVal.latitude || 0
        editScooterForm.longitude = newVal.longitude || 0
        editScooterForm.lastMaintenance = formatDateToInput(newVal.lastMaintenanceDate)
    } else {
        // Reset form if needed
        Object.assign(editScooterForm, { id: null, scooterCode: '', model: '', status: '', batteryLevel: 0, location: '', latitude: 0, longitude: 0, lastMaintenance: '' })
    }
})

// Computed properties
const filteredScooters = computed(() => {
    let result = [...scooters.value]

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(scooter =>
            scooter.id.toString().includes(query) ||
            scooter.location.toLowerCase().includes(query) ||
            (scooter.scooterCode || '').toLowerCase().includes(query)
        )
    }

    if (filterStatus.value !== 'all') {
        const statusMap: Record<string, string> = {
            'available': 'Available',
            'in-use': 'In Use',
            'maintenance': 'Maintenance'
        }
        result = result.filter(scooter =>
            (scooter.status || '').toLowerCase().replace(' ', '-') === filterStatus.value
        )
    }

    result.sort((a, b) => {
        switch (sortBy.value) {
            case 'battery':
                return b.batteryLevel - a.batteryLevel
            case 'location':
                return a.location.localeCompare(b.location)
            default: // id
                return a.id - b.id
        }
    })

    return result
})

// Functions related to API interaction
const fetchScooters = async () => {
    isLoading.value = true
    apiError.value = null
    try {
        const response = await adminApi.getAllScooters()
        console.log('Received scooters from API:', response.data) // Add debugging log
        scooters.value = response.data.map((raw: ApiScooterResponse): ProcessedScooter => {
            let parsedDate: Date | null = null
            try { parsedDate = raw.lastMaintenance ? parseISO(raw.lastMaintenance) : null } catch(e) { console.error("Error parsing lastMaintenance:", raw.lastMaintenance, e) }

            // Determine status (handle potential 'available' field)
            let status = raw.status || 'Unknown'
            if(raw.available === true && status === 'Unknown') status = 'Available'
            if(raw.available === false && status === 'Unknown') status = 'Maintenance' // Changed from 'In Use' to 'Maintenance'

            return {
                id: raw.id,
                scooterCode: raw.scooterCode,
                model: raw.model,
                status: status,
                batteryLevel: raw.batteryLevel,
                location: raw.location,
                lastMaintenanceDate: parsedDate,
                latitude: raw.latitude !== undefined ? raw.latitude : null,
                longitude: raw.longitude !== undefined ? raw.longitude : null
            }
        })
        updateMapMarkers()
    } catch (error: any) {
        console.error('Error fetching scooters:', error)
        apiError.value = `Failed to load scooters: ${error.message || 'Unknown error'}`
        scooters.value = []
    } finally {
        isLoading.value = false
    }
}

const addScooter = async () => {
    try {
        // Prepare data for API submission
        const apiData: Omit<ScooterForm, 'id'> = {
            scooterCode: newScooterForm.scooterCode,
            model: newScooterForm.model,
            status: newScooterForm.status,
            batteryLevel: newScooterForm.batteryLevel,
            location: newScooterForm.location,
            latitude: newScooterForm.latitude,
            longitude: newScooterForm.longitude,
            lastMaintenance: newScooterForm.lastMaintenance
        };
        
        await adminApi.addScooter(apiData)
        alert('Scooter added successfully!')
        showAddModal.value = false
        // Reset form
        Object.assign(newScooterForm, { 
            scooterCode: '', 
            model: 'Standard', 
            status: 'Available', 
            batteryLevel: 100, 
            location: '', 
            latitude: 0, 
            longitude: 0,
            lastMaintenance: new Date().toISOString().split('T')[0] 
        })
        fetchScooters() // Refresh list
    } catch (error: any) {
        console.error('Error adding scooter:', error)
        alert(`Failed to add scooter: ${error.response?.data?.message || error.message}`)
    }
}

const updateScooter = async () => {
    if (!editScooterForm.id) return
    try {
        // Prepare data for API (might need adjustments based on backend)
        const apiData = { ...editScooterForm }
        console.log('Sending scooter update data:', apiData)
        
        // First, update the scooter with the standard API
        await adminApi.updateScooter(editScooterForm.id, apiData)
        
        // Check if we need to update coordinates separately
        if (apiData.latitude !== undefined && apiData.longitude !== undefined) {
            try {
                // Use the dedicated API method for coordinates
                await adminApi.updateScooterCoordinates(
                    editScooterForm.id, 
                    Number(apiData.latitude), 
                    Number(apiData.longitude)
                )
                console.log(`Updated coordinates for scooter #${editScooterForm.id} to:`, {
                    latitude: apiData.latitude, 
                    longitude: apiData.longitude
                })
            } catch (coordError) {
                console.error('Error updating coordinates:', coordError)
                // Don't fail the entire operation if coordinates update fails
            }
        }
        
        alert('Scooter updated successfully!')
        showEditModal.value = false
        editingScooter.value = null // Clear selection
        fetchScooters() // Refresh list
    } catch (error: any) {
        console.error('Error updating scooter:', error)
        alert(`Failed to update scooter: ${error.response?.data?.message || error.message}`)
    }
}

const deleteScooter = async () => {
    if (!deletingScooter.value) return
    try {
        await adminApi.deleteScooter(deletingScooter.value.id)
        alert('Scooter deleted successfully!')
        showDeleteModal.value = false
        deletingScooter.value = null
        fetchScooters() // Refresh list
    } catch (error: any) {
        console.error('Error deleting scooter:', error)
        alert(`Failed to delete scooter: ${error.response?.data?.message || error.message}`)
    }
}

const toggleMaintenance = async (scooter: ProcessedScooter) => {
    const newStatus = scooter.status === 'Maintenance' ? 'Available' : 'Maintenance'
    try {
        await adminApi.updateScooterStatus(scooter.id, newStatus)
        // 不在此处更新本地状态，而是通过fetchScooters完全刷新数据
        alert(`Scooter status updated to ${newStatus}.`)
        // 重新获取所有滑板车数据，确保获取到正确的状态
        await fetchScooters()
    } catch (error: any) {
        console.error('Error updating scooter status:', error)
        alert(`Failed to update status: ${error.response?.data?.message || error.message}`)
    }
}

const editScooter = (scooter: ProcessedScooter) => {
    editingScooter.value = scooter // Set the scooter to be edited
    showEditModal.value = true
}

const confirmDelete = (scooter: ProcessedScooter) => {
    deletingScooter.value = scooter
    showDeleteModal.value = true
}

const formatDate = (dateObj: Date | null | undefined) => {
    if (!dateObj || !(dateObj instanceof Date) || isNaN(dateObj.getTime())) return 'N/A'
    try {
        return formatDateFn(dateObj, 'MMM d, yyyy') // Simpler format for date only
    } catch (error) { return 'Error' }
}

const getBatteryClass = (level: number) => {
    if (level > 70) return 'high'
    if (level > 30) return 'medium'
    return 'low'
}

// Map functions
const initMap = () => {
    if (mapInstance.value) return;

    try {
        const initialCoords: L.LatLngExpression = [40.7128, -74.0060]; // Example: New York
        const initialZoom = 10;

        mapInstance.value = L.map('scooter-map').setView(initialCoords, initialZoom);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(mapInstance.value);

        scooterMarkersLayer.value = L.layerGroup().addTo(mapInstance.value);

        if (scooters.value.length > 0) {
            updateMapMarkers();
        }

    } catch (error) {
        console.error("Failed to initialize map:", error);
        apiError.value = "Could not load the map.";
    }
};

const updateMapMarkers = () => {
    if (!mapInstance.value || !scooterMarkersLayer.value) return;

    scooterMarkersLayer.value.clearLayers();

    const validCoords: L.LatLngExpression[] = [];

    scooters.value.forEach(scooter => {
        if (scooter.latitude != null && scooter.longitude != null && !isNaN(scooter.latitude) && !isNaN(scooter.longitude)) {
            const coords: L.LatLngExpression = [scooter.latitude, scooter.longitude];
            validCoords.push(coords);

            // Use the custom divIcon when creating the marker
            const marker = L.marker(coords, { icon: scooterDivIcon });

            marker.bindPopup(`
                <b>Scooter #${scooter.id}</b><br>
                Code: ${scooter.scooterCode || 'N/A'}<br>
                Status: ${scooter.status}<br>
                Battery: ${scooter.batteryLevel}%<br>
                Location: ${scooter.location}
            `);

            scooterMarkersLayer.value?.addLayer(marker);
        } else {
            console.warn(`Scooter #${scooter.id} has invalid or missing coordinates.`);
        }
    });

    if (validCoords.length > 0 && mapInstance.value) {
        const mapSize = mapInstance.value.getSize();
        if (mapSize.x > 0 && mapSize.y > 0) {
             try {
                 mapInstance.value.flyToBounds(L.latLngBounds(validCoords), { padding: [50, 50], maxZoom: 15 });
             } catch(e) {
                 console.error("Error adjusting map bounds:", e);
             }
        } else {
             console.warn("Map size is invalid, cannot fit bounds yet.");
        }
    }
};

// Guest Booking methods
const openGuestBookingModal = (scooter: ProcessedScooter) => {
    scooterForGuestBooking.value = scooter;
    // Reset form fields
    guestBookingForm.guestEmail = '';
    guestBookingForm.guestName = '';
    guestBookingForm.selectedDurationLabel = '';
    guestBookingError.value = null;
    isGuestBookingLoading.value = false;
    showGuestBookingModal.value = true;
};

const closeGuestBookingModal = () => {
    showGuestBookingModal.value = false;
    scooterForGuestBooking.value = null;
};

const submitGuestBooking = async () => {
    if (!scooterForGuestBooking.value || !isValidGuestBookingForm.value || isGuestBookingLoading.value) {
        return;
    }
    isGuestBookingLoading.value = true;
    guestBookingError.value = null;

    try {
        const payload = {
            scooterId: scooterForGuestBooking.value.id,
            selectedDurationLabel: guestBookingForm.selectedDurationLabel,
            guestEmail: guestBookingForm.guestEmail,
            guestName: guestBookingForm.guestName || undefined // Send undefined if empty
        };
        await adminApi.createGuestBooking(payload);
        alert(`Booking created successfully for guest ${guestBookingForm.guestEmail}! Confirmation email sent.`);
        closeGuestBookingModal();
        fetchScooters(); // Refresh scooter list (status might change to 'In Use')
    } catch (error: any) {
        console.error("Failed to create guest booking:", error);
        guestBookingError.value = error.response?.data?.message || error.message || 'Failed to create booking for guest.';
        // Keep modal open on error
    } finally {
        isGuestBookingLoading.value = false;
    }
};

// Simple email validation regex
const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

// Computed property to check if the guest booking form is valid for submission
const isValidGuestBookingForm = computed(() => {
    return isValidEmail(guestBookingForm.guestEmail) && guestBookingForm.selectedDurationLabel !== '';
});

// 添加一个通用的反向地理编码函数
const reverseGeocode = async (lat: number, lng: number): Promise<string | null> => {
    try {
        // 使用OpenStreetMap的Nominatim服务进行反向地理编码
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`, {
            headers: {
                'Accept-Language': 'en-US,en', // 请求英文结果
                'User-Agent': 'E-Scooter-Booking-System' // 按Nominatim API要求提供应用名称
            }
        })
        
        if (!response.ok) {
            throw new Error(`Geocoding failed with status: ${response.status}`)
        }
        
        const data = await response.json()
        
        if (data && data.display_name) {
            // 获取到了地址，返回处理后的地址
            const addressParts = data.display_name.split(',')
            // 取地址的前几部分作为location值，避免过长
            return addressParts.slice(0, 3).join(', ')
        }
        return null
    } catch (error) {
        console.error('Error in reverseGeocode:', error)
        throw error
    }
}

// 现有滑板车的地理编码功能
const getLocationFromCoordinates = async () => {
    // 检查是否有有效的经纬度
    const lat = Number(editScooterForm.latitude)
    const lng = Number(editScooterForm.longitude)
    
    if (isNaN(lat) || isNaN(lng) || !lat || !lng) {
        locationError.value = 'Please enter valid latitude and longitude values'
        return
    }
    
    isLocationLoading.value = true
    locationError.value = null
    
    try {
        const locationValue = await reverseGeocode(lat, lng)
        if (locationValue) {
            editScooterForm.location = locationValue
            console.log('Location set from coordinates:', locationValue)
        } else {
            locationError.value = 'No address found for these coordinates'
        }
    } catch (error: any) {
        console.error('Error fetching location data:', error)
        locationError.value = error.message || 'Failed to get location from coordinates'
    } finally {
        isLocationLoading.value = false
    }
}

// 新滑板车的地理编码功能
const getNewScooterLocationFromCoordinates = async () => {
    // 检查是否有有效的经纬度
    const lat = Number(newScooterForm.latitude)
    const lng = Number(newScooterForm.longitude)
    
    if (isNaN(lat) || isNaN(lng) || !lat || !lng) {
        newLocationError.value = 'Please enter valid latitude and longitude values'
        return
    }
    
    isNewLocationLoading.value = true
    newLocationError.value = null
    
    try {
        const locationValue = await reverseGeocode(lat, lng)
        if (locationValue) {
            newScooterForm.location = locationValue
            console.log('New scooter location set from coordinates:', locationValue)
        } else {
            newLocationError.value = 'No address found for these coordinates'
        }
    } catch (error: any) {
        console.error('Error fetching location data for new scooter:', error)
        newLocationError.value = error.message || 'Failed to get location from coordinates'
    } finally {
        isNewLocationLoading.value = false
    }
}

// 坐标变化时清除错误
const handleCoordinateChange = () => {
    locationError.value = null
}

// 新滑板车坐标变化处理程序
const handleNewScooterCoordinateChange = () => {
    newLocationError.value = null
}

// Lifecycle hook
onMounted(async () => {
    await nextTick();
    initMap();
    fetchScooters();
});

watch(scooters, updateMapMarkers, { deep: true });
</script>

<style scoped>
.admin-scooters {
    padding: 20px;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-button {
    background-color: #4CAF50;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.filters {
    display: flex;
    flex-wrap: wrap; /* Allow filters to wrap */
    gap: 1rem;
    margin-bottom: 1.5rem;
    padding: 1rem;
    background-color: white;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
}

.filters .search-box,
.filters select {
    flex-grow: 1; /* Allow elements to grow */
    min-width: 150px; /* Minimum width before wrapping */
}

.map-container {
    height: 400px; /* Adjust as needed */
    margin-bottom: 1.5rem;
    border-radius: var(--radius-lg);
    overflow: hidden;
    box-shadow: var(--shadow-md);
}

#scooter-map {
    height: 100%;
    width: 100%;
    z-index: 1; /* Ensure map is below modals */
}

.table-container {
    width: 100%;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    border: 1px solid var(--border-color, #eee);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
}

.data-table {
    width: 100%;
    min-width: 800px; /* Adjust based on columns */
    border-collapse: collapse;
    background-color: var(--card-bg, white);
}

.data-table th,
.data-table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

.status-badge {
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 0.9em;
}

.status-badge.available {
    background-color: #4CAF50;
    color: white;
}

.status-badge.in-use {
    background-color: #2196F3;
    color: white;
}

.status-badge.maintenance {
    background-color: #FFC107;
    color: black;
}

.battery-indicator {
    display: flex;
    align-items: center;
    gap: 10px;
}

.battery-bar {
    width: 100px;
    height: 20px;
    background-color: #eee;
    border-radius: 10px;
    overflow: hidden;
}

.battery-bar>div {
    height: 100%;
    transition: width 0.3s ease;
}

.battery-bar .high {
    background-color: #4CAF50;
}

.battery-bar .medium {
    background-color: #FFC107;
}

.battery-bar .low {
    background-color: #f44336;
}

.actions-cell {
    display: flex;
    gap: 8px;
    white-space: nowrap; /* Prevent action buttons from wrapping */
}

.action-btn {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9em;
}

.action-btn.edit {
    background-color: #2196F3;
    color: white;
}

.action-btn.maintenance {
    background-color: #FFC107;
    color: black;
}

.action-btn.delete {
    background-color: #f44336;
    color: white;
}

.action-btn.book-guest {
    background-color: #9b59b6;
    color: white;
}

.actions-cell .action-btn {
    margin-right: 0.5rem;
}
.actions-cell .action-btn:last-child {
    margin-right: 0;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1050; /* Ensure modals are above map */
    visibility: visible;
    opacity: 1;
    padding: 1rem;
}

.modal-container {
    width: 100%; /* Full width on small screens */
    max-width: 500px;
    max-height: 90vh;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 10000; /* Higher than overlay */
    visibility: visible;
    opacity: 1;
    margin: auto;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #f1f1f1;
    color: #2c3e50;
    flex-shrink: 0;
}
.modal-header h3 { margin: 0; font-weight: 600; }
.close-btn {
    background: none; border: none; font-size: 1.5rem; cursor: pointer;
    color: #606f7b; padding: 0; line-height: 1; transition: color 0.2s;
}
.close-btn:hover { color: #ef4444; }

.modal-body {
    padding: 1.5rem; /* More padding for forms */
    color: #2c3e50;
    overflow-y: auto; /* Enable vertical scroll within body */
    flex-grow: 1;
}

/* Form Styles inside Modal */
.form-group {
    margin-bottom: 1.25rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #4a5568;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="email"],
.form-group input[type="date"],
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    box-sizing: border-box;
    font-size: 1rem;
    transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
    outline: none;
    border-color: var(--primary-500);
    box-shadow: 0 0 0 3px rgba(var(--primary-500-rgb), 0.2);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1.5rem;
    padding-top: 1rem;
    border-top: 1px solid #eee;
}

.cancel-btn,
.submit-btn,
.delete-btn {
    padding: 0.6rem 1.2rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.cancel-btn {
    background-color: #e5e7eb;
    color: #374151;
}
.cancel-btn:hover {
    background-color: #d1d5db;
}

.submit-btn {
    background-color: var(--primary-600);
    color: white;
}
.submit-btn:hover {
    background-color: var(--primary-700);
    transform: translateY(-1px);
}

.delete-btn {
    background-color: #ef4444;
    color: white;
}
.delete-btn:hover {
    background-color: #dc2626;
}

/* Styles for delete confirmation modal */
.delete-modal .modal-header {
    background-color: #fee2e2; /* Light red header */
    color: #b91c1c; /* Darker red text */
}
.delete-modal .modal-body p {
    margin-bottom: 0.5rem;
}
.delete-modal .warning {
    color: #ef4444;
    font-weight: 600;
    margin-top: 0.25rem;
    margin-bottom: 1rem;
}

/* Guest Booking Modal Specifics */
.guest-booking-modal {
     max-width: 450px;
}
.validation-error {
    color: var(--danger-500);
    font-size: 0.8rem;
    margin-top: 0.25rem;
}
.api-error {
    color: var(--danger-500);
    background-color: var(--danger-100);
    padding: 0.75rem;
    border-radius: var(--radius-md);
    margin-bottom: 1rem;
    border: 1px solid var(--danger-200);
    text-align: center;
}
/* --- End Modal Styles --- */

/* Map Container Styles */
.map-container {
    margin-bottom: 20px;
    border: 1px solid #ddd;
    border-radius: 8px;
}

#scooter-map {
    height: 400px;
    width: 100%;
    border-radius: 8px;
}

/* Ensure Leaflet popups look okay */
.leaflet-popup-content-wrapper {
    border-radius: 4px;
}

.leaflet-popup-content p {
    margin: 0.5em 0;
}

.leaflet-popup-content b {
   color: #333;
}

@media (max-width: 992px) {
    .filters {
        padding: 0.75rem;
    }
    .map-container {
        height: 350px;
    }
    .data-table {
        min-width: 700px;
    }
}

@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
        margin-bottom: 1rem;
    }
    .page-header h1 {
        font-size: 1.5rem;
    }
    .page-header .add-button {
        font-size: 0.85rem;
        padding: 0.4rem 0.8rem;
    }
    
    .filters {
        padding: 0.5rem;
        gap: 0.5rem;
    }

    .filters .search-box input,
    .filters select {
        font-size: 0.9rem;
        padding: 0.5rem 0.75rem;
    }

    .map-container {
        height: 300px;
        margin-bottom: 1rem;
    }

    .table-container {
        margin-bottom: 1rem;
    }
    .data-table {
        min-width: 600px; /* Further reduce min-width if needed */
    }
    .data-table th,
    .data-table td {
        padding: 0.6rem 0.5rem;
        font-size: 0.85rem;
    }
    .status-badge {
        font-size: 0.75rem;
        padding: 0.2rem 0.5rem;
    }
    .battery-indicator span {
        font-size: 0.8rem;
    }

    .actions-cell .action-btn {
        padding: 0.3rem 0.6rem;
        font-size: 0.75rem;
        margin-right: 0.25rem;
    }
    
    .modal-header h3 {
        font-size: 1.1rem;
    }
    .modal-body {
        padding: 1rem;
    }
    .form-group label {
        font-size: 0.9rem;
    }
    .form-group input,
    .form-group select {
        padding: 0.6rem;
        font-size: 0.9rem;
    }
    .form-actions {
        padding: 0.75rem;
        gap: 0.5rem;
    }
    .form-actions button {
        font-size: 0.85rem;
    }
    .delete-modal p {
        font-size: 0.9rem;
    }
}

@media (max-width: 576px) {
    .filters {
        flex-direction: column; /* Stack filters */
        align-items: stretch;
    }
    .filters .search-box,
    .filters select {
        min-width: 100%;
    }
    .map-container {
        height: 250px;
    }
    .data-table th,
    .data-table td {
        white-space: nowrap; /* Ensure horizontal scrolling works well */
    }
}

/* Optional: Style the emoji icon if needed */
.leaflet-emoji-icon {
    font-size: 24px; /* Adjust emoji size */
    text-align: center;
    line-height: 30px; /* Match iconSize height */
    /* Add other styles like background, border-radius if desired */
    /* background: rgba(255, 255, 255, 0.7); */
    /* border-radius: 50%; */
}

/* 在<style>部分添加 */
.location-input-group {
    display: flex;
    gap: 10px;
}

.location-input-group input {
    flex: 1;
}

.location-btn {
    background-color: #4caf50;
    color: white;
    border: none;
    border-radius: 4px;
    padding: 0.5rem 0.75rem;
    cursor: pointer;
    font-size: 0.9rem;
    white-space: nowrap;
    transition: background-color 0.2s ease;
}

.location-btn:hover:not(:disabled) {
    background-color: #3e8e41;
}

.location-btn:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
}

.error-text {
    color: #f44336;
    font-size: 0.8rem;
    margin-top: 0.25rem;
    display: block;
}
</style>
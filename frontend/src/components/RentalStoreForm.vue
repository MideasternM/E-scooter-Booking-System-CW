<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { RentalStore } from '@/types/RentalStore';
import type { RentalStoreDTO } from '@/types/RentalStoreDTO';
import { StoreStatus, StoreService } from '@/types/RentalStore';
// Assuming FormInput component exists
// import FormInput from '@/components/FormInput.vue';
// Assuming BaseButton component exists
// import BaseButton from '@/components/BaseButton.vue';

// Props and emits
const props = defineProps<{
  storeToEdit: RentalStore | null
}>();

const emit = defineEmits<{
  save: [storeData: RentalStoreDTO]
  cancel: []
}>();

// Form state
const formData = ref<RentalStoreDTO>({
  name: '',
  code: '',
  latitude: 0,
  longitude: 0,
  address: '',
  openingHours: '09:00-18:00',
  contactPhone: null,
  description: null,
  imageUrl: null,
  servicesOffered: [StoreService.RENTAL],
  status: StoreStatus.OPERATIONAL // 默认设置为运营中
});

// Initialize form with existing data when editing
onMounted(() => {
  if (props.storeToEdit) {
    console.log('Editing store with status:', props.storeToEdit.status);
    
    formData.value = {
      name: props.storeToEdit.name,
      code: props.storeToEdit.code,
      latitude: props.storeToEdit.latitude,
      longitude: props.storeToEdit.longitude,
      address: props.storeToEdit.address,
      openingHours: props.storeToEdit.openingHours || '09:00-18:00', // Ensure default if null
      contactPhone: props.storeToEdit.contactPhone,
      description: props.storeToEdit.description,
      imageUrl: props.storeToEdit.imageUrl,
      servicesOffered: props.storeToEdit.servicesOffered?.length ? [...props.storeToEdit.servicesOffered] : [StoreService.RENTAL], // Ensure default if empty/null
      status: props.storeToEdit.status as StoreStatus || StoreStatus.OPERATIONAL // 确保正确转换为枚举类型
    };
    
    console.log('Form data status after initialization:', formData.value.status);
  }
});

// 状态选项
const statusOptions = [
  { value: StoreStatus.OPERATIONAL, label: 'Operational', className: 'status-option-operational' },
  { value: StoreStatus.CLOSED_TEMPORARY, label: 'Temporarily Closed', className: 'status-option-closed-temp' },
  { value: StoreStatus.CLOSED_PERMANENT, label: 'Permanently Closed', className: 'status-option-closed-perm' }
];

// Form validation
const errors = ref<Record<string, string>>({});

function validateForm(): boolean {
  errors.value = {};
  
  if (!formData.value.name?.trim()) {
    errors.value.name = 'Store name is required.';
  }
  
  if (!formData.value.code?.trim()) {
    errors.value.code = 'Store code is required.';
  }
  
  if (formData.value.latitude < -90 || formData.value.latitude > 90) {
    errors.value.latitude = 'Latitude must be between -90 and 90.';
  }
  
  if (formData.value.longitude < -180 || formData.value.longitude > 180) {
    errors.value.longitude = 'Longitude must be between -180 and 180.';
  }
  
  if (!formData.value.address?.trim()) {
    errors.value.address = 'Address is required.';
  }
  
  // Validate opening hours format if provided
  if (formData.value.openingHours && formData.value.openingHours.trim()) {
    const regex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
    if (!regex.test(formData.value.openingHours)) {
      errors.value.openingHours = 'Opening hours format must be HH:MM-HH:MM.';
    }
  }
  
  // 确保状态字段已被设置
  if (!formData.value.status) {
    formData.value.status = StoreStatus.OPERATIONAL;
  }
  
  return Object.keys(errors.value).length === 0;
}

// Form submission
function handleSubmit() {
  if (validateForm()) {
    // 确保状态字段为有效的枚举值
    if (!Object.values(StoreStatus).includes(formData.value.status as StoreStatus)) {
      formData.value.status = StoreStatus.OPERATIONAL;
    }
    
    console.log('Submitting form with status:', formData.value.status);
    
    emit('save', { ...formData.value });
  }
}

// Available store services for multi-select
const availableServices = [
  { value: StoreService.RENTAL, label: 'Rental Service' },
  { value: StoreService.REPAIR, label: 'Repair Service' },
  { value: StoreService.SALES, label: 'Sales Service' },
  { value: StoreService.CHARGING, label: 'Charging Service' }
];

// Helper function to toggle a service
function toggleService(service: StoreService) {
  if (!formData.value.servicesOffered) {
    formData.value.servicesOffered = [];
  }
  
  const index = formData.value.servicesOffered.indexOf(service);
  if (index === -1) {
    formData.value.servicesOffered.push(service);
  } else {
    formData.value.servicesOffered.splice(index, 1);
  }
}

// Check if a service is selected
function isServiceSelected(service: StoreService): boolean {
  return formData.value.servicesOffered?.includes(service) || false;
}

// Form title based on edit/create mode
const formTitle = computed(() => props.storeToEdit ? 'Edit Rental Store' : 'Add New Rental Store');

// 添加地理编码相关的状态变量
const isLocationLoading = ref(false)
const locationError = ref<string | null>(null)

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
            // 取地址的所有部分作为商店地址，因为商店地址通常需要完整
            return data.display_name
        }
        return null
    } catch (error) {
        console.error('Error in reverseGeocode:', error)
        throw error
    }
}

// 从坐标获取地址
const getAddressFromCoordinates = async () => {
    // 检查是否有有效的经纬度
    const lat = Number(formData.value.latitude)
    const lng = Number(formData.value.longitude)
    
    if (isNaN(lat) || isNaN(lng) || !lat || !lng) {
        locationError.value = 'Please enter valid latitude and longitude values'
        return
    }
    
    isLocationLoading.value = true
    locationError.value = null
    
    try {
        const addressValue = await reverseGeocode(lat, lng)
        if (addressValue) {
            formData.value.address = addressValue
            console.log('Address set from coordinates:', addressValue)
        } else {
            locationError.value = 'No address found for these coordinates'
        }
    } catch (error: any) {
        console.error('Error fetching address data:', error)
        locationError.value = error.message || 'Failed to get address from coordinates'
    } finally {
        isLocationLoading.value = false
    }
}

// 坐标变化时清除错误
const handleCoordinateChange = () => {
    locationError.value = null
}
</script>

<template>
  <div>
    <div class="modal-header">
      <h3 class="modal-title">{{ formTitle }}</h3>
      <button type="button" class="modal-close" @click="emit('cancel')">×</button>
    </div>
    
    <div class="modal-body">
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- Basic Information Section -->
        <div>
          <h3 class="section-title">Basic Information</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
            <!-- Name -->
            <div class="form-group">
              <label for="name" class="form-label">Store Name *</label>
              <input
                id="name"
                v-model="formData.name"
                type="text"
                class="form-control"
                :class="errors.name ? 'is-invalid' : ''"
              />
              <p v-if="errors.name" class="form-feedback is-invalid">{{ errors.name }}</p>
            </div>
            
            <!-- Code -->
            <div class="form-group">
              <label for="code" class="form-label">Store Code *</label>
              <input
                id="code"
                v-model="formData.code"
                type="text"
                class="form-control"
                :class="errors.code ? 'is-invalid' : ''"
              />
              <p v-if="errors.code" class="form-feedback is-invalid">{{ errors.code }}</p>
            </div>
            
            <!-- Status 字段添加在这里 -->
            <div class="form-group">
              <label for="status" class="form-label">Store Status *</label>
              <select
                id="status"
                v-model="formData.status"
                class="form-control status-select"
                :class="[
                  formData.status === StoreStatus.OPERATIONAL ? 'status-operational' : 
                  formData.status === StoreStatus.CLOSED_TEMPORARY ? 'status-closed-temp' :
                  formData.status === StoreStatus.CLOSED_PERMANENT ? 'status-closed-perm' : ''
                ]"
              >
                <option
                  v-for="option in statusOptions"
                  :key="option.value"
                  :value="option.value"
                  :class="option.className"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
            
            <!-- Opening Hours -->
            <div class="form-group">
              <label for="openingHours" class="form-label">Opening Hours (HH:MM-HH:MM)</label>
              <input
                id="openingHours"
                v-model="formData.openingHours"
                type="text"
                placeholder="09:00-18:00"
                class="form-control"
                :class="errors.openingHours ? 'is-invalid' : ''"
              />
              <p v-if="errors.openingHours" class="form-feedback is-invalid">{{ errors.openingHours }}</p>
            </div>
            
            <!-- Address -->
            <div class="form-group md:col-span-2">
              <label for="address" class="form-label">Address *</label>
              <input
                id="address"
                v-model="formData.address"
                type="text"
                class="form-control"
                :class="errors.address ? 'is-invalid' : ''"
              />
              <p v-if="errors.address" class="form-feedback is-invalid">{{ errors.address }}</p>
            </div>
            
            <!-- Contact Phone -->
            <div class="form-group">
              <label for="contactPhone" class="form-label">Contact Phone</label>
              <input
                id="contactPhone"
                v-model="formData.contactPhone"
                type="tel" 
                placeholder="e.g., +1 123 456 7890"
                class="form-control"
              />
            </div>
          </div>
        </div>
        
        <!-- Location Section -->
        <div>
          <h3 class="section-title">Location Information</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
            <!-- Latitude -->
            <div class="form-group">
              <label for="latitude" class="form-label">Latitude *</label>
              <input
                id="latitude"
                v-model.number="formData.latitude"
                type="number"
                step="0.000001"
                class="form-control"
                :class="errors.latitude ? 'is-invalid' : ''"
                @change="handleCoordinateChange"
              />
              <p v-if="errors.latitude" class="form-feedback is-invalid">{{ errors.latitude }}</p>
            </div>
            
            <!-- Longitude -->
            <div class="form-group">
              <label for="longitude" class="form-label">Longitude *</label>
              <input
                id="longitude"
                v-model.number="formData.longitude"
                type="number"
                step="0.000001"
                class="form-control"
                :class="errors.longitude ? 'is-invalid' : ''"
                @change="handleCoordinateChange"
              />
              <p v-if="errors.longitude" class="form-feedback is-invalid">{{ errors.longitude }}</p>
            </div>

            <!-- 添加按钮获取地址 -->
            <div class="form-group md:col-span-2">
              <button 
                type="button" 
                @click="getAddressFromCoordinates" 
                :disabled="isLocationLoading"
                class="btn btn-secondary btn-sm"
              >
                {{ isLocationLoading ? 'Getting Address...' : 'Get Address From Coordinates' }}
              </button>
              <p v-if="locationError" class="form-feedback is-invalid">{{ locationError }}</p>
            </div>
          </div>
        </div>
        
        <!-- Additional Information Section -->
        <div>
          <h3 class="section-title">Additional Information</h3>
          <div class="grid grid-cols-1 gap-y-4">
            <!-- Services Offered -->
            <div class="form-group">
              <label class="form-label">Services Offered</label>
              <div class="grid grid-cols-2 gap-4">
                <div v-for="service in availableServices" :key="service.value" class="form-check">
                  <input
                    :id="`service-${service.value}`"
                    :value="service.value"
                    :checked="isServiceSelected(service.value)"
                    @change="toggleService(service.value)"
                    type="checkbox"
                    class="form-check-input"
                  />
                  <label :for="`service-${service.value}`" class="form-check-label">
                    {{ service.label }}
                  </label>
                </div>
              </div>
            </div>
            
            <!-- Description -->
            <div class="form-group">
              <label for="description" class="form-label">Description</label>
              <textarea
                id="description"
                v-model="formData.description"
                rows="3"
                class="form-control"
                placeholder="Optional: Describe the store, special features, etc."
              ></textarea>
            </div>
            
            <!-- Image URL -->
            <div class="form-group">
              <label for="imageUrl" class="form-label">Image URL</label>
              <input
                id="imageUrl"
                v-model="formData.imageUrl"
                type="url"
                class="form-control"
                placeholder="Optional: https://example.com/store-image.jpg"
              />
            </div>
          </div>
        </div>
      </form>
    </div>
    
    <div class="modal-footer">
      <button
        type="button"
        @click="emit('cancel')"
        class="btn btn-outline"
      >
        Cancel
      </button>
      <button
        type="button"
        @click="handleSubmit"
        class="btn btn-primary"
      >
        Save Store
      </button>
    </div>
  </div>
</template>

<style scoped>
.section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-md);
}

.form-group {
  margin-bottom: var(--space-sm);
}

/* 状态选择字段样式 */
.status-select {
  font-weight: var(--font-weight-medium);
  padding-left: 0.75rem;
}

.status-select option {
  font-weight: normal;
  padding: 0.5rem;
}

.status-operational {
  color: var(--success);
  border-color: var(--success);
  background-color: var(--success-light);
}

.status-closed-temp {
  color: var(--warning);
  border-color: var(--warning);
  background-color: var(--warning-light);
}

.status-closed-perm {
  color: var(--error);
  border-color: var(--error);
  background-color: var(--error-light);
}

.status-option-operational {
  color: var(--success);
  background-color: var(--success-light);
}

.status-option-closed-temp {
  color: var(--warning);
  background-color: var(--warning-light);
}

.status-option-closed-perm {
  color: var(--error);
  background-color: var(--error-light);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: var(--space-md);
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 0.6rem 1.5rem;
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-outline {
  background-color: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.btn-outline:hover {
  border-color: var(--text-primary);
  color: var(--text-primary);
}

.btn-primary {
  background-color: var(--primary-600);
  color: white;
  border: 1px solid var(--primary-600);
}

.btn-primary:hover {
  background-color: var(--primary-700);
}

.btn-secondary {
  background-color: #4caf50;
  color: white;
  border: 1px solid #4caf50;
}

.btn-secondary:hover {
  background-color: #3e8e41;
}

.btn-secondary:disabled {
  background-color: #cccccc;
  border-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-sm {
  padding: 0.4rem 1rem;
  font-size: 0.875rem;
}

/* 错误文本样式 */
.form-feedback.is-invalid {
  color: var(--error);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}
</style> 
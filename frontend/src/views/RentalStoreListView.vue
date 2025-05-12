<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { rentalStoreService } from '@/services/rentalStoreService';
import type { RentalStore } from '@/types/RentalStore';
import { StoreStatus, StoreService } from '@/types/RentalStore';
import { useScooterStore } from '@/stores/scooterStore';
import type { Scooter } from '@/types/Scooter';
import { ScooterStatus } from '@/types/Scooter';

// 状态
const stores = ref<RentalStore[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const searchQuery = ref('');
const filterStatus = ref('all');
const sortBy = ref('name');

// --- START: 模态框状态 ---
const isScooterModalVisible = ref(false);
const selectedStoreForModal = ref<RentalStore | null>(null);
const scootersInModal = ref<Scooter[]>([]);
const isLoadingModalScooters = ref(false);
const modalError = ref<string | null>(null);
// --- END: 模态框状态 ---

const router = useRouter();
const scooterStore = useScooterStore();

// 获取所有门店
const fetchStores = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const data = await rentalStoreService.getAllStores();
    stores.value = data;
  } catch (err: any) {
    error.value = err.message || 'Failed to fetch rental stores';
    console.error('Error fetching stores:', error.value);
  } finally {
    isLoading.value = false;
  }
};

// 在组件挂载时获取数据
onMounted(() => {
  fetchStores();
  scooterStore.fetchScooters();
});

// 状态的格式化文字
const getStatusText = (status: StoreStatus): string => {
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
};

// 状态的样式类
const getStatusClass = (status: StoreStatus): string => {
  switch (status) {
    case StoreStatus.OPERATIONAL:
      return 'status-operational';
    case StoreStatus.CLOSED_TEMPORARY:
      return 'status-closed-temp';
    case StoreStatus.CLOSED_PERMANENT:
      return 'status-closed-perm';
    default:
      return '';
  }
};

// 获取服务项目的图标
const getServiceIcon = (service: StoreService): string => {
  switch (service) {
    case StoreService.RENTAL:
      return '🛴';
    case StoreService.REPAIR:
      return '🔧';
    case StoreService.SALES:
      return '🏪';
    case StoreService.CHARGING:
      return '🔋';
    default:
      return '❓';
  }
};

// 获取电池状态样式
const getBatteryClass = (level: number): string => {
  if (level > 70) return 'high';
  if (level > 30) return 'medium';
  return 'low';
};

// --- START: 模态框逻辑 ---
// 打开模态框并加载滑板车
const openScooterModal = async (store: RentalStore) => {
  console.log('Opening scooter modal for store:', store.name);
  selectedStoreForModal.value = store;
  isScooterModalVisible.value = true;
  console.log('isScooterModalVisible set to:', isScooterModalVisible.value);
  modalError.value = null;
  scootersInModal.value = [];

  if (!store.availableScooters || store.availableScooters.length === 0) {
    console.log('No scooter codes found for this store.');
    return;
  }

  isLoadingModalScooters.value = true;
  try {
    if (scooterStore.scooters.length === 0) {
      console.log('Scooter store empty, fetching scooters...');
      await scooterStore.fetchScooters();
    }

    const scooterCodes = store.availableScooters;
    console.log('Finding scooters with codes:', scooterCodes);
    scootersInModal.value = scooterStore.scooters.filter(scooter =>
      scooterCodes.includes(scooter.code || String(scooter.id))
    );
    
    // 添加调试信息，检查滑板车对象的结构和值
    console.log('Found scooters for modal:', JSON.stringify(scootersInModal.value, null, 2));
    
    // 针对每个滑板车，记录其状态
    if (scootersInModal.value.length > 0) {
      const testScooter = scootersInModal.value[0];
      console.log('First scooter details (Object.keys):', Object.keys(testScooter));
      console.log('First scooter details (Object.entries):', Object.entries(testScooter));
      console.log('- Status:', testScooter.status);
      console.log('- Status type:', typeof testScooter.status);
      console.log('- Status constructor:', testScooter.status?.constructor?.name);
      console.log('- Available:', testScooter.available);
      console.log('- Available type:', typeof testScooter.available);
      
      // 测试状态格式化函数
      console.log('Formatted status output:', getFormattedStatus(testScooter.status, testScooter));
    }

  } catch (err: any) {
    modalError.value = 'Failed to load scooter details.';
    console.error('Error fetching scooters for modal:', err);
  } finally {
    isLoadingModalScooters.value = false;
  }
};

// 关闭模态框
const closeScooterModal = () => {
  console.log('Closing scooter modal.');
  isScooterModalVisible.value = false;
  selectedStoreForModal.value = null;
  scootersInModal.value = [];
  modalError.value = null;
};

// 预订滑板车
const bookScooter = (scooter: Scooter) => {
  // 确保滑板车ID可用
  if (!scooter || !scooter.id) {
    console.error('Invalid scooter object:', scooter);
    return;
  }

  // 保存选择的滑板车信息到本地存储
  localStorage.setItem('selectedScooterToBook', JSON.stringify({
    id: scooter.id,
    code: scooter.code,
    model: scooter.model
  }));
  
  const storeId = selectedStoreForModal.value?.id;

  // 关闭模态框
  closeScooterModal();
  
  // 跳转到预订创建页面
  console.log(`Navigating to: /booking/create/${scooter.id}${storeId ? '?fromStore=' + storeId : ''}`);
  router.push(`/booking/create/${scooter.id}${storeId ? '?fromStore=' + storeId : ''}`);
};
// --- END: 模态框逻辑 ---

// 过滤和排序
const filteredStores = computed(() => {
  let result = [...stores.value];

  // 应用搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(
      store => 
        store.name.toLowerCase().includes(query) ||
        store.code.toLowerCase().includes(query) ||
        store.address.toLowerCase().includes(query)
    );
  }

  // 应用状态过滤
  if (filterStatus.value !== 'all') {
    const statusFilter = filterStatus.value as StoreStatus;
    result = result.filter(store => store.status === statusFilter);
  }

  // 应用排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name);
      case 'scooters':
        return (b.availableScooters?.length || 0) - (a.availableScooters?.length || 0);
      case 'status':
        return a.status.localeCompare(b.status);
      default:
        return 0;
    }
  });

  return result;
});

// 查看门店位置和详情
const viewStoreOnMap = (store: RentalStore) => {
  const storeToView = {
    id: store.id,
    latitude: store.latitude,
    longitude: store.longitude,
    name: store.name,
    code: store.code
  };
  
  localStorage.setItem('selectedStoreToView', JSON.stringify(storeToView));
  
  router.push({
    path: '/',
    query: { 
      showStore: store.id.toString(),
      lat: store.latitude.toString(),
      lng: store.longitude.toString() 
    }
  });
};

// 获取滑板车状态的CSS类
const getStatusClassForScooter = (status: any, scooter?: Scooter): string => {
  // 如果状态为空，但有scooter对象，使用available属性生成状态类
  if ((status === null || status === undefined) && scooter) {
    if (scooter.available === true) {
      return 'status-available';
    } else if (scooter.available === false) {
      return 'status-in_use';
    }
  }
  
  // 如果状态为空，返回空字符串
  if (!status) return '';

  // 尝试将状态转换为字符串
  let statusStr;
  try {
    statusStr = String(status).toLowerCase();
  } catch (e) {
    console.error('Error converting status to string:', e);
    return '';
  }
  
  // 判断状态类型
  if (statusStr.includes('available')) return 'status-available';
  if (statusStr.includes('in_use')) return 'status-in_use';
  if (statusStr.includes('maintenance')) return 'status-maintenance';
  if (statusStr.includes('charging')) return 'status-charging';
  if (statusStr.includes('out_of_order')) return 'status-out_of_order';
  
  return '';
};

// 获取格式化的状态文本
const getFormattedStatus = (status: any, scooter?: Scooter): string => {
  // 如果状态为空，但有scooter对象，使用available属性生成状态
  if ((status === null || status === undefined) && scooter) {
    if (scooter.available === true) {
      return 'Available';
    } else if (scooter.available === false) {
      return 'In Use';
    }
  }
  
  // 如果状态为空，返回"Unknown"
  if (status === null || status === undefined) return 'Unknown';
  
  // 尝试将状态转换为字符串
  let statusStr;
  try {
    // 检查是否是枚举值ScooterStatus的情况
    if (status === ScooterStatus.AVAILABLE) {
      return 'Available';
    } else if (status === ScooterStatus.IN_USE) {
      return 'In Use';
    } else if (status === ScooterStatus.MAINTENANCE) {
      return 'Maintenance';
    } else if (status === ScooterStatus.CHARGING) {
      return 'Charging';
    } else if (status === ScooterStatus.OUT_OF_ORDER) {
      return 'Out of Order';
    }
    
    // 尝试直接转换为字符串并格式化
    statusStr = String(status);
  } catch (e) {
    console.error('Error in getFormattedStatus:', e);
    return 'Unknown';
  }
  
  console.log('Status string before formatting:', statusStr);
  
  // 将全大写的状态转换为首字母大写
  const formatted = statusStr.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  return formatted;
};

// 检查滑板车是否可预订
const isScooterBookable = (scooter: Scooter): boolean => {
  // 首先检查available属性
  if (scooter.available === true) return true;
  
  // 如果available属性不可用，检查status
  if (scooter.status) {
    // 转换为字符串
    const statusStr = String(scooter.status).toLowerCase();
    return statusStr.includes('available');
  }
  
  return false;
};
</script>

<template>
  <div class="store-list-container">
    <div class="header">
      <h1>Rental Stores</h1>
      <div class="filters">
        <div class="search-box">
          <input v-model="searchQuery" type="text" placeholder="Search by name, code or address" />
        </div>
        <select v-model="filterStatus">
          <option value="all">All Status</option>
          <option :value="StoreStatus.OPERATIONAL">Operational</option>
          <option :value="StoreStatus.CLOSED_TEMPORARY">Temporarily Closed</option>
          <option :value="StoreStatus.CLOSED_PERMANENT">Permanently Closed</option>
        </select>
        <select v-model="sortBy">
          <option value="name">Sort by Name</option>
          <option value="scooters">Sort by Scooters</option>
          <option value="status">Sort by Status</option>
        </select>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-state">
      <div class="spinner"></div>
      <span>Loading stores...</span>
    </div>

    <!-- 错误提示 -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="fetchStores" class="retry-button">Retry</button>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredStores.length === 0" class="empty-state">
      <div class="empty-icon">🏪</div>
      <h3>No Rental Stores Found</h3>
      <p v-if="searchQuery || filterStatus !== 'all'">Try changing your search or filter criteria.</p>
      <p v-else>There are currently no rental stores available in the system.</p>
    </div>

    <!-- 门店卡片列表 -->
    <div v-else class="store-grid">
      <div v-for="store in filteredStores" :key="store.id" class="store-card" 
        :class="[getStatusClass(store.status)]">
        <div class="store-header">
          <div class="store-icon">🏪</div>
          <div class="store-title">
            <h3>{{ store.name }}</h3>
            <span class="store-code">{{ store.code }}</span>
          </div>
          <div :class="['status-badge', getStatusClass(store.status)]">
            {{ getStatusText(store.status) }}
          </div>
        </div>
        
        <div class="store-details">
          <p class="address"><strong>Address:</strong> {{ store.address }}</p>
          <p><strong>Opening Hours:</strong> {{ store.openingHours }}</p>
          <p v-if="store.contactPhone"><strong>Phone:</strong> {{ store.contactPhone }}</p>
          
          <div class="services-list">
            <strong>Services:</strong>
            <div class="services">
              <span v-for="service in store.servicesOffered" :key="service" class="service-badge">
                {{ getServiceIcon(service) }} {{ service.charAt(0) + service.slice(1).toLowerCase() }}
              </span>
            </div>
          </div>
          
          <div class="scooter-info">
            <strong>Available Scooters:</strong>
            <span class="scooter-count">{{ store.availableScooters?.length || 0 }}</span>
          </div>
        </div>
        
        <div class="store-actions">
          <button @click="viewStoreOnMap(store)" class="view-on-map-button">
            View on Map
          </button>
          <button @click="openScooterModal(store)" class="view-scooters-button"
                  :disabled="!store.availableScooters || store.availableScooters.length === 0">
            View Scooters ({{ store.availableScooters?.length || 0 }})
          </button>
        </div>
      </div>
    </div>

    <!-- --- START: 滑板车详情模态框 (使用 Teleport) --- -->
    <Teleport to="body">
      <div v-if="isScooterModalVisible" class="modal-overlay active" @click.self="closeScooterModal">
        <!-- 移除调试文本 -->
        
        <div class="modal-content modal-container scooter-modal">
          <div class="modal-header">
            <div class="modal-title">
              <span class="modal-icon">🛴</span>
              <h3>Scooters at {{ selectedStoreForModal?.name }}</h3>
            </div>
            <button class="modal-close" @click="closeScooterModal">&times;</button>
          </div>
          <div class="modal-body">
            <div v-if="isLoadingModalScooters" class="loading-state-small">
              <div class="spinner-small"></div>
              <span>Loading scooter details...</span>
            </div>
            <div v-else-if="modalError" class="error-message-small">
              <p>{{ modalError }}</p>
            </div>
            <div v-else-if="scootersInModal.length === 0" class="empty-state-small">
              <p>No scooters currently available at this store.</p>
            </div>
            <div v-else class="scooter-table-container">
              <!-- 表头 -->
              <div class="scooter-table-header">
                <div class="scooter-column code-column">
                  <span class="column-icon">🔢</span>Code
                </div>
                <div class="scooter-column model-column">
                  <span class="column-icon">🏷️</span>Model
                </div>
                <div class="scooter-column battery-column">
                  <span class="column-icon">🔋</span>Battery
                </div>
                <div class="scooter-column status-column">
                  <span class="column-icon">📊</span>Status
                </div>
                <div class="scooter-column action-column">
                  <span class="column-icon">🔄</span>Action
                </div>
              </div>
              
              <!-- 表格内容 -->
              <ul class="scooter-list">
                <li v-for="scooter in scootersInModal" :key="scooter.id">
                  <span class="scooter-code">{{ scooter.code || scooter.id }}</span>
                  <span class="scooter-model">{{ scooter.model || 'N/A' }}</span>
                  <span class="scooter-battery">
                    <div class="battery-indicator" :style="`width: ${scooter.batteryLevel}%`" 
                         :class="getBatteryClass(scooter.batteryLevel)"></div>
                    <span>{{ scooter.batteryLevel }}%</span>
                  </span>
                  <span class="scooter-status" :class="getStatusClassForScooter(scooter.status, scooter)">
                    {{ getFormattedStatus(scooter.status, scooter) }}
                  </span>
                  <span class="scooter-action">
                    <button @click="bookScooter(scooter)" 
                            class="btn-book"
                            :disabled="!isScooterBookable(scooter)">
                      <span class="action-icon">🔖</span> Book
                    </button>
                  </span>
                </li>
              </ul>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeScooterModal" class="btn btn-outline">
              <span class="btn-icon">✖️</span> Close
            </button>
          </div>
        </div>
      </div>
    </Teleport>
    <!-- --- END: 滑板车详情模态框 --- -->

  </div>
</template>

<style scoped>
.store-list-container {
  max-width: 100%;
  margin: 0;
  padding: 0;
  background-color: #f8f9fa;
  min-height: calc(100vh - 60px);
}

.header {
  background-color: #ffffff;
  margin-bottom: 1.5rem;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 1.2rem;
  font-weight: 600;
  font-size: 1.8rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 0;
  flex-wrap: wrap;
}

.search-box input,
.filters select {
  padding: 0.7rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.search-box {
  flex-grow: 1;
}

.search-box input {
  width: 100%;
}

.search-box input:focus,
.filters select:focus {
  border-color: #42b983;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15);
  outline: none;
}

.store-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
  padding: 0 2rem 2rem;
}

.store-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.store-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.store-card.status-operational {
  border-top: 4px solid var(--success);
}

.store-card.status-closed-temp {
  border-top: 4px solid var(--warning);
}

.store-card.status-closed-perm {
  border-top: 4px solid var(--error);
}

.store-header {
  display: flex;
  align-items: center;
  padding: 1.2rem;
  border-bottom: 1px solid #f0f0f0;
}

.store-icon {
  font-size: 1.8rem;
  margin-right: 1rem;
}

.store-title {
  flex-grow: 1;
}

.store-title h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

.store-code {
  font-size: 0.8rem;
  color: #6c757d;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 50px;
  font-size: 0.75rem;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.status-operational {
  background-color: rgba(66, 185, 131, 0.1);
  color: var(--success);
}

.status-badge.status-closed-temp {
  background-color: rgba(255, 193, 7, 0.1);
  color: var(--warning);
}

.status-badge.status-closed-perm {
  background-color: rgba(220, 53, 69, 0.1);
  color: var(--error);
}

.store-details {
  padding: 1.2rem;
  flex-grow: 1;
}

.store-details p {
  margin: 0 0 0.75rem 0;
  font-size: 0.95rem;
  line-height: 1.4;
}

.store-details .address {
  color: #2c3e50;
}

.services-list {
  margin: 1rem 0;
}

.services {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.service-badge {
  background-color: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  white-space: nowrap;
}

.scooter-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px dashed #e0e0e0;
}

.scooter-count {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}

.store-actions {
  padding: 1rem;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 0.75rem;
  justify-content: space-between;
}

.view-on-map-button,
.view-scooters-button {
  flex: 1;
  background-color: var(--primary-500);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.6rem 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  text-align: center;
}

.view-on-map-button:hover,
.view-scooters-button:hover {
  background-color: var(--primary-600);
}

.view-scooters-button {
  background-color: var(--secondary-500);
}
.view-scooters-button:hover {
  background-color: var(--secondary-600);
}
.view-scooters-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(66, 185, 131, 0.1);
  border-top-color: #42b983;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  padding: 4rem 2rem;
  text-align: center;
  margin: 2rem;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: #2c3e50;
}

.empty-state p {
  color: #6c757d;
  max-width: 500px;
  margin: 0 auto;
}

.error-message {
  background-color: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  margin: 2rem;
}

.retry-button {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.retry-button:hover {
  background-color: #c82333;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }
  
  .store-grid {
    grid-template-columns: 1fr;
    padding: 0 1rem 1rem;
  }
  
  .header {
    padding: 1rem;
  }
}

/* --- START: Modal 样式 (恢复) --- */
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100% !important;
  height: 100% !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  z-index: 9999 !important;
  opacity: 1 !important;
  backdrop-filter: blur(5px) !important;
  -webkit-backdrop-filter: blur(5px) !important;
  transition: all 0.3s ease !important;
}

.modal-content {
  background-color: white !important;
  padding: 1.5rem 2rem !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2) !important;
  width: 90% !important;
  max-width: 600px !important; 
  max-height: 80vh !important; 
  display: flex !important;
  flex-direction: column !important;
  opacity: 1 !important;
  transform: scale(1) !important;
  z-index: 10000 !important;
  transition: all 0.3s ease !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.modal-title {
  display: flex;
  align-items: center;
}

.modal-icon {
  font-size: 1.5rem;
  margin-right: 0.7rem;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: #888;
  line-height: 1;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.modal-close:hover {
  color: #333;
  background-color: rgba(0, 0, 0, 0.05);
}

.modal-body {
  overflow-y: auto;
  flex-grow: 1;
  margin-bottom: 1rem;
}

.scooter-table-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

.scooter-table-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 0.7rem 0.5rem;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e9ecef;
  font-size: 0.9rem;
}

.column-icon {
  margin-right: 0.3rem;
  font-size: 0.9rem;
}

.scooter-column {
  display: flex;
  align-items: center;
}

.code-column, .model-column {
  flex-basis: 20%;
}

.battery-column, .status-column {
  flex-basis: 20%;
}

.action-column {
  flex-basis: 20%;
  justify-content: center;
}

.scooter-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 350px;
  overflow-y: auto;
}

.scooter-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 0.5rem;
  border-bottom: 1px solid #f0f0f0;
  font-size: 0.95rem;
  transition: background-color 0.2s ease;
}

.scooter-list li:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.scooter-list li:last-child {
  border-bottom: none;
}

.scooter-code, .scooter-model, .scooter-battery, .scooter-status, .scooter-action {
  flex-basis: 20%;
  padding: 0 5px;
  display: flex;
  align-items: center;
}

.scooter-code {
  font-weight: 500;
  color: #333;
}

.scooter-battery {
  flex-direction: column;
  align-items: flex-start;
}

.battery-indicator {
  height: 8px;
  background-color: var(--primary-500);
  border-radius: 4px;
  margin-bottom: 4px;
}

.battery-indicator.high {
  background-color: var(--success);
}

.battery-indicator.medium {
  background-color: var(--warning);
}

.battery-indicator.low {
  background-color: var(--error);
}

.scooter-status {
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 20px;
  font-size: 0.85rem;
  text-align: center;
  justify-content: center;
}

.status-available { background-color: rgba(66, 185, 131, 0.1); color: var(--success); }
.status-in_use { background-color: rgba(255, 193, 7, 0.1); color: var(--warning); }
.status-maintenance, .status-charging, .status-out_of_order { background-color: rgba(220, 53, 69, 0.1); color: var(--error); }

.scooter-action {
  justify-content: center;
}

.btn-book {
  background-color: var(--primary-500);
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.btn-book:hover {
  background-color: var(--primary-600);
  transform: translateY(-1px);
}

.btn-book:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.action-icon {
  margin-right: 3px;
}

.btn-icon {
  margin-right: 5px;
}

.modal-footer {
  border-top: 1px solid #e0e0e0;
  padding-top: 1rem;
  text-align: right;
}

.btn-outline {
  background-color: transparent;
  border: 1px solid #ccc;
  color: #555;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.btn-outline:hover {
  background-color: #f8f8f8;
  border-color: #aaa;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.btn-outline:active {
  transform: translateY(0);
  box-shadow: none;
}

.loading-state-small, .empty-state-small, .error-message-small {
  text-align: center;
  padding: 2rem 1rem;
  color: #6c757d;
}
.spinner-small {
  width: 25px;
  height: 25px;
  border: 2px solid rgba(66, 185, 131, 0.1);
  border-top-color: #42b983;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 0.5rem;
}
.error-message-small p { color: var(--error); }
/* --- END: Modal 样式 --- */
</style> 
<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRentalStore } from '../stores/rentalStore';
import type { RentalStore } from '../types/RentalStore';
import { useScooterStore } from '../stores/scooterStore';
import type { Scooter } from '../types/Scooter';
// Assuming Spinner and Alert components exist
// import Spinner from './Spinner.vue';
// import Alert from './Alert.vue';

const props = defineProps<{
  store: RentalStore | null
}>();

const emit = defineEmits<{
  update: [store: RentalStore]
  close: []
}>();

const rentalStore = useRentalStore();
const scooterStore = useScooterStore();

// Local state
const scooterCodeToAdd = ref('');
const isLoading = ref(false); // Combined loading state
const isLoadingAvailable = ref(false); // Specific loading for available scooters
const error = ref<string | null>(null);
const availableScooters = ref<Scooter[]>([]);
const searchQuery = ref('');
const allStoreScooters = ref<{[storeId: number]: string[]}>({});

// 获取所有站点的滑板车列表
const fetchAllStoresScooters = async () => {
  try {
    // 获取所有租赁站点
    await rentalStore.fetchStores();
    
    // 建立站点ID到滑板车列表的映射
    const storeScootersMap: {[storeId: number]: string[]} = {};
    
    // 为每个站点记录其滑板车
    rentalStore.stores.forEach(store => {
      if (store.availableScooters && store.availableScooters.length > 0) {
        storeScootersMap[store.id] = [...store.availableScooters];
      }
    });
    
    console.log('所有站点的滑板车映射:', storeScootersMap);
    allStoreScooters.value = storeScootersMap;
  } catch (err) {
    console.error('获取所有站点滑板车失败:', err);
  }
};

// 检查滑板车是否已分配给任何站点
const isScooterAssignedToAnyStore = (scooterCode: string): boolean => {
  // 检查所有站点的所有滑板车
  for (const storeId in allStoreScooters.value) {
    if (allStoreScooters.value[storeId].includes(scooterCode)) {
      console.log(`滑板车 ${scooterCode} 已分配给站点 ${storeId}`);
      return true;
    }
  }
  return false;
};

// 获取滑板车代码（如果代码不存在，则使用ID）
const getScooterCode = (scooter: Scooter): string => {
  return scooter.code || String(scooter.id);
};

// Get all available scooters not assigned to any store
const fetchAvailableScooters = async () => {
  if (!props.store) return;
  
  isLoadingAvailable.value = true;
  error.value = null;
  
  try {
    // 1. 获取所有滑板车
    await scooterStore.fetchScooters(); 
    
    // 2. 获取所有站点的滑板车分配情况
    await fetchAllStoresScooters();
    
    console.log('所有滑板车数据:', scooterStore.scooters);
    console.log('当前站点ID:', props.store.id);
    
    // 3. 严格筛选可用的滑板车 - 不在任何站点的滑板车
    availableScooters.value = scooterStore.scooters.filter((scooter: Scooter) => {
      const scooterCode = getScooterCode(scooter);
      const isAvailable = scooter.available;
      const isNotAssignedToAnyStore = !isScooterAssignedToAnyStore(scooterCode);
      
      console.log(`滑板车 ${scooterCode}: 可用=${isAvailable}, 未分配=${isNotAssignedToAnyStore}`);
      
      // 滑板车必须: 1.可用 2.未分配给任何站点
      return isAvailable && isNotAssignedToAnyStore;
    });
    
    console.log('过滤后的可用滑板车数量:', availableScooters.value.length);
  } catch (err: any) {
    error.value = err.message || 'Failed to fetch available scooters';
    console.error('获取可用滑板车出错:', err);
  } finally {
    isLoadingAvailable.value = false;
  }
};

// Filter scooters based on search
const filteredAvailableScooters = computed(() => {
  // 添加更详细的控制台日志
  console.log('当前可用滑板车数量:', availableScooters.value.length);
  
  if (!searchQuery.value) return availableScooters.value;
  
  const query = searchQuery.value.toLowerCase();
  return availableScooters.value.filter((scooter: Scooter) => {
    // 获取code（如果不存在则使用id转为字符串）
    const scooterCode = getScooterCode(scooter);
    // 添加空值检查
    const codeMatch = scooterCode && scooterCode.toLowerCase().includes(query);
    const modelMatch = scooter.model && scooter.model.toLowerCase().includes(query);
    return codeMatch || modelMatch;
  });
});

// 检查按钮是否应该禁用的函数
const shouldDisableButton = (scooter: Scooter) => {
  const result = isLoading.value || !scooter || !getScooterCode(scooter);
  // 记录每个滑板车的禁用状态和原因
  console.log(`滑板车 ${getScooterCode(scooter) || '未知'} 按钮禁用: ${result}`, 
    {isLoading: isLoading.value, noScooter: !scooter, noCode: !getScooterCode(scooter)});
  return result;
};

// Add a scooter to the store
const addScooterToStore = async () => {
  if (!props.store) {
    error.value = "No store selected.";
    return;
  }
  
  // Log the value before using it
  console.log('Adding scooter code:', scooterCodeToAdd.value);
  
  const code = scooterCodeToAdd.value || '';
  if (!code || code.trim() === '') {
    error.value = "Please enter a valid scooter code.";
    return;
  }
  
  // 检查滑板车是否已经在任何站点
  if (isScooterAssignedToAnyStore(code.trim())) {
    error.value = `Scooter "${code.trim()}" is already assigned to another store.`;
    return;
  }
  
  isLoading.value = true;
  error.value = null;
  
  try {
    const updatedStore = await rentalStore.addScooter(props.store.id, code.trim());
    if (updatedStore) {
      emit('update', updatedStore); // Emit update to parent
      scooterCodeToAdd.value = '';
      
      // 重新获取所有数据
      await Promise.all([
        scooterStore.fetchScooters(),
        fetchAllStoresScooters(),
        fetchAvailableScooters()
      ]);
    } else {
      error.value = rentalStore.error || "Failed to add scooter.";
    }
  } catch (err: any) {
    console.error('Error adding scooter:', err);
    // 提供更友好的错误信息
    if (err.response && err.response.status === 400) {
      error.value = `Cannot add scooter "${code.trim()}". It may already be assigned to another store.`;
    } else {
      error.value = err.message || "An error occurred while adding the scooter.";
    }
  } finally {
    isLoading.value = false;
  }
};

// Remove a scooter from the store
const removeScooterFromStore = async (scooterCode: string) => {
  if (!props.store) return;
  
  // Use a proper confirmation dialog if available
  if (confirm(`Are you sure you want to remove scooter ${scooterCode} from this store?`)) {
    isLoading.value = true;
    error.value = null;
    
    try {
      const updatedStore = await rentalStore.removeScooter(props.store.id, scooterCode);
      if (updatedStore) {
        emit('update', updatedStore); // Emit update to parent
        
        // 重新获取所有数据
        await Promise.all([
          scooterStore.fetchScooters(),
          fetchAllStoresScooters(),
          fetchAvailableScooters()
        ]);
      } else {
        error.value = rentalStore.error || "Failed to remove scooter.";
      }
    } catch (err: any) {
      error.value = err.message || "An error occurred while removing the scooter.";
    } finally {
      isLoading.value = false;
    }
  }
};

// 检查滑板车是否已在当前商店
const isScooterInCurrentStore = (scooterCode: string): boolean => {
  if (!props.store || !props.store.availableScooters) return false;
  return props.store.availableScooters.includes(scooterCode);
};

// Initialize component: Fetch available scooters when the component mounts or store prop changes
onMounted(() => {
  fetchAvailableScooters();
});

// 监听store属性变化，每当打开新的模态框时重新获取数据
watch(() => props.store, (newStore) => {
  if (newStore) {
    console.log('Store changed, refreshing scooters data for store ID:', newStore.id);
    
    // 重新获取所有数据
    Promise.all([
      scooterStore.fetchScooters(),
      fetchAllStoresScooters()
    ]).then(() => {
      fetchAvailableScooters();
    });
  }
}, { immediate: true });
</script>

<template>
  <div>
    <div class="modal-header">
      <h3 class="modal-title">Manage Store Scooters - {{ store?.name }}</h3>
      <button type="button" class="modal-close" @click="$emit('close')">×</button>
    </div>
    
    <div class="modal-body">
      <!-- Error message -->
      <div v-if="error" class="alert alert-danger mb-4" style="font-weight: bold; padding: 12px; border-left: 4px solid #d9534f;">
        <strong>Error:</strong> {{ error }}
      </div>
      
      <!-- Add Scooter Form -->
      <div class="mb-6">
        <h4 class="section-title">Add Scooter by Code</h4>
        <div class="input-group">
          <input
            id="scooterCodeToAdd"
            v-model="scooterCodeToAdd"
            type="text"
            placeholder="Enter Scooter Code"
            class="form-control"
            :disabled="isLoading"
          />
          <button
            @click="addScooterToStore"
            :disabled="isLoading || scooterCodeToAdd === undefined || scooterCodeToAdd === null || scooterCodeToAdd.trim() === ''"
            class="btn btn-primary"
          >
            <span v-if="isLoading">Adding...</span>
            <span v-else>Add</span>
          </button>
        </div>
      </div>
      
      <!-- Current Store Scooters -->
      <div class="mb-5">
        <h4 class="section-title">Scooters Currently in Store</h4>
        <div v-if="isLoading && !store?.availableScooters?.length" class="loading-spinner">
           <div class="spinner"></div>
           <span>Loading...</span>
        </div>
        <div v-else-if="!store?.availableScooters || store.availableScooters.length === 0" class="empty-state-small">
          No scooters currently assigned to this store.
        </div>
        <ul v-else class="list-group">
          <li v-for="(scooterCode, index) in store.availableScooters" :key="index" class="list-item">
            <span class="item-name">{{ scooterCode }}</span>
            <button 
              @click="removeScooterFromStore(scooterCode)" 
              :disabled="isLoading"
              class="btn btn-outline-danger btn-sm"
              aria-label="Remove scooter"
            >
              Remove
            </button>
          </li>
        </ul>
      </div>
      
      <!-- Available Scooters (Not in any store) -->
      <div>
        <h4 class="section-title">Available Scooters to Add</h4>
        <div class="search-box mb-3">
          <span class="search-icon">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"></path>
            </svg>
          </span>
          <input
            v-model="searchQuery"
            type="search"
            placeholder="Search available scooters by code or model..."
            class="form-control search-input"
            :disabled="isLoadingAvailable"
          />
        </div>
        
        <div v-if="isLoadingAvailable" class="loading-spinner">
           <div class="spinner"></div>
           <span>Loading available scooters...</span>
        </div>
        <div v-else-if="filteredAvailableScooters.length === 0" class="empty-state-small">
          <div v-if="searchQuery">
            No available scooters found matching your search.
          </div>
          <div v-else>
            No available scooters found. All scooters may be already assigned to stores.
          </div>
        </div>
        <div v-else class="data-table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Model</th>
                <th>Battery</th>
                <th class="actions-column">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="scooter in filteredAvailableScooters" :key="scooter.id" class="data-row">
                <td>{{ scooter.code || scooter.id }}</td>
                <td>{{ scooter.model || 'N/A' }}</td>
                <td>{{ scooter.batteryLevel }}%</td>
                <td class="actions-cell">
                  <button
                    @click="() => { 
                      console.log('点击了添加按钮，滑板车:', scooter); 
                      scooterCodeToAdd = getScooterCode(scooter); 
                      addScooterToStore(); 
                    }"
                    :disabled="isScooterInCurrentStore(getScooterCode(scooter))"
                    :title="isScooterInCurrentStore(getScooterCode(scooter)) ? 'This scooter is already in this store' : 'Add scooter to store'"
                    class="btn btn-primary btn-sm"
                    aria-label="Add scooter"
                  >
                    {{ isScooterInCurrentStore(getScooterCode(scooter)) ? 'Added' : 'Add' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <div class="modal-footer">
      <button
        @click="$emit('close')"
        class="btn btn-outline"
      >
        Close
      </button>
    </div>
  </div>
</template>

<style scoped>
.section-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-md);
}

.input-group {
  display: flex;
}

.input-group .form-control {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  flex: 1;
}

.input-group .btn {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--space-md) 0;
}

.loading-spinner .spinner {
  width: 1.5rem;
  height: 1.5rem;
  border: 2px solid rgba(var(--primary-500-rgb), 0.2);
  border-top-color: var(--primary-500);
  border-radius: 50%;
  animation: spin 1s infinite linear;
  margin-right: var(--space-sm);
}

.loading-spinner span {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.empty-state-small {
  background-color: var(--bg-muted);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  text-align: center;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.list-group {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--border-color);
}

.list-item:last-child {
  border-bottom: none;
}

.item-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: var(--space-sm);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-icon svg {
  width: 1rem;
  height: 1rem;
}

.search-input {
  padding-left: calc(var(--space-sm) * 2 + 1rem);
}

.data-table-container {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  max-height: 250px;
  overflow-y: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  position: sticky;
  top: 0;
  background-color: var(--bg-muted);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  text-align: left;
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--border-color);
}

.data-table td {
  padding: var(--space-sm) var(--space-md);
  border-bottom: 1px solid var(--border-color);
  font-size: var(--font-size-sm);
}

.data-row:last-child td {
  border-bottom: none;
}

.data-row:hover {
  background-color: var(--bg-hover);
}

.actions-column {
  width: 100px;
  text-align: right;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style> 
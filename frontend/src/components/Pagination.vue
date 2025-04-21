<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Number,
    required: true
  },
  totalItems: {
    type: Number,
    required: true
  },
  itemsPerPage: {
    type: Number,
    default: 10
  },
  visiblePages: {
    type: Number,
    default: 5
  },
  showFirstLast: {
    type: Boolean,
    default: true
  },
  showPrevNext: {
    type: Boolean,
    default: true
  },
  size: {
    type: String as () => 'sm' | 'md' | 'lg',
    default: 'md'
  },
  rounded: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'page-change']);

const totalPages = computed(() => Math.ceil(props.totalItems / props.itemsPerPage));
const currentPage = computed(() => Math.min(Math.max(1, props.modelValue), totalPages.value));

const isFirstPage = computed(() => currentPage.value === 1);
const isLastPage = computed(() => currentPage.value === totalPages.value || totalPages.value === 0);

const paginationClass = computed(() => [
  'pagination',
  `pagination-${props.size}`,
  { 
    'pagination-rounded': props.rounded,
    'pagination-disabled': props.disabled
  }
]);

const pageItemClass = (page: number) => [
  'page-item',
  { 'active': page === currentPage.value }
];

const calculateVisiblePages = () => {
  const pages: (number | string)[] = [];
  
  if (totalPages.value <= props.visiblePages) {
    // If we have fewer pages than the desired visible count, show all pages
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i);
    }
    return pages;
  }
  
  // Always include the first page
  pages.push(1);
  
  const startPage = Math.max(2, currentPage.value - Math.floor((props.visiblePages - 2) / 2));
  const endPage = Math.min(totalPages.value - 1, startPage + props.visiblePages - 3);
  
  // Add ellipsis if needed between first page and start
  if (startPage > 2) {
    pages.push('...');
  }
  
  // Add visible pages
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  
  // Add ellipsis if needed between end and last page
  if (endPage < totalPages.value - 1) {
    pages.push('...');
  }
  
  // Always include the last page if we have more than one page
  if (totalPages.value > 1) {
    pages.push(totalPages.value);
  }
  
  return pages;
};

const pages = computed(() => calculateVisiblePages());

const changePage = (page: number) => {
  if (props.disabled || page === currentPage.value || page < 1 || page > totalPages.value) return;
  
  emit('update:modelValue', page);
  emit('page-change', page);
};

const goToFirstPage = () => changePage(1);
const goToLastPage = () => changePage(totalPages.value);
const goToPrevPage = () => changePage(currentPage.value - 1);
const goToNextPage = () => changePage(currentPage.value + 1);
</script>

<template>
  <nav class="pagination-wrapper" aria-label="Pagination">
    <ul :class="paginationClass">
      <!-- First page button -->
      <li v-if="showFirstLast" class="page-item page-nav" :class="{ disabled: isFirstPage || disabled }">
        <button @click="goToFirstPage" :disabled="isFirstPage || disabled" aria-label="Go to first page">
          «
        </button>
      </li>
      
      <!-- Previous page button -->
      <li v-if="showPrevNext" class="page-item page-nav" :class="{ disabled: isFirstPage || disabled }">
        <button @click="goToPrevPage" :disabled="isFirstPage || disabled" aria-label="Go to previous page">
          ‹
        </button>
      </li>
      
      <!-- Page buttons -->
      <li v-for="(page, index) in pages" :key="index" :class="pageItemClass(page as number)">
        <template v-if="page === '...'">
          <span class="page-ellipsis">{{ page }}</span>
        </template>
        <template v-else>
          <button 
            @click="changePage(page as number)" 
            :disabled="disabled"
            :aria-current="currentPage === page ? 'page' : undefined"
            :aria-label="`Go to page ${page}`"
          >
            {{ page }}
          </button>
        </template>
      </li>
      
      <!-- Next page button -->
      <li v-if="showPrevNext" class="page-item page-nav" :class="{ disabled: isLastPage || disabled }">
        <button @click="goToNextPage" :disabled="isLastPage || disabled" aria-label="Go to next page">
          ›
        </button>
      </li>
      
      <!-- Last page button -->
      <li v-if="showFirstLast" class="page-item page-nav" :class="{ disabled: isLastPage || disabled }">
        <button @click="goToLastPage" :disabled="isLastPage || disabled" aria-label="Go to last page">
          »
        </button>
      </li>
    </ul>
    
    <div v-if="totalPages > 0" class="pagination-info">
      <slot name="info">
        <span>Page {{ currentPage }} of {{ totalPages }}</span>
      </slot>
    </div>
  </nav>
</template>

<style scoped>
.pagination-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-md);
  margin: var(--space-md) 0;
}

.pagination {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
  border-radius: var(--radius-md);
  gap: var(--space-xs);
}

.page-item {
  display: inline-flex;
}

.page-item button,
.page-item .page-ellipsis {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 2.25rem;
  height: 2.25rem;
  padding: 0 0.5rem;
  text-align: center;
  color: var(--text-primary);
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
}

.pagination-rounded .page-item button,
.pagination-rounded .page-item .page-ellipsis {
  border-radius: var(--radius-full);
}

.page-item button:hover:not(:disabled) {
  background-color: var(--neutral-100);
  color: var(--primary-600);
  border-color: var(--primary-200);
  z-index: 1;
}

.page-item button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(var(--primary-500-rgb), 0.2);
  z-index: 2;
}

.page-item.active button {
  background-color: var(--primary-500);
  color: white;
  border-color: var(--primary-500);
  z-index: 3;
}

.page-item.disabled button,
.page-item button:disabled {
  color: var(--text-muted);
  background-color: var(--disabled-bg);
  border-color: var(--border-color);
  cursor: not-allowed;
  opacity: 0.5;
}

.page-ellipsis {
  cursor: default;
  color: var(--text-muted);
}

.page-nav button {
  font-weight: var(--font-weight-bold);
}

/* Size variations */
.pagination-sm .page-item button,
.pagination-sm .page-item .page-ellipsis {
  min-width: 1.75rem;
  height: 1.75rem;
  font-size: var(--font-size-xs);
}

.pagination-lg .page-item button,
.pagination-lg .page-item .page-ellipsis {
  min-width: 2.75rem;
  height: 2.75rem;
  font-size: var(--font-size-md);
}

.pagination-info {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.pagination-disabled {
  opacity: 0.6;
  pointer-events: none;
}

/* Dark mode adjustments */
:root.dark-mode .page-item button,
.dark-mode .page-item button {
  background-color: var(--card-bg);
  border-color: var(--border-color);
}

:root.dark-mode .page-item button:hover:not(:disabled),
.dark-mode .page-item button:hover:not(:disabled) {
  background-color: var(--neutral-800);
  border-color: var(--primary-700);
}

:root.dark-mode .page-item.active button,
.dark-mode .page-item.active button {
  background-color: var(--primary-600);
  border-color: var(--primary-700);
}
</style> 
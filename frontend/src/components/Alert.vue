<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';

type AlertVariant = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info';

const props = defineProps({
  variant: {
    type: String as () => AlertVariant,
    default: 'primary'
  },
  title: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  dismissible: {
    type: Boolean,
    default: false
  },
  autoClose: {
    type: Number,
    default: 0
  },
  bordered: {
    type: Boolean,
    default: false
  },
  elevated: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);

const visible = ref(true);
let autoCloseTimeout: number | null = null;

const alertClasses = computed(() => [
  'alert',
  `alert-${props.variant}`,
  {
    'alert-dismissible': props.dismissible,
    'alert-bordered': props.bordered,
    'alert-elevated': props.elevated,
    'alert-with-icon': props.icon
  }
]);

const variantIcon = computed(() => {
  if (props.icon) return props.icon;
  
  switch (props.variant) {
    case 'success': return '✓';
    case 'danger': return '✗';
    case 'warning': return '⚠️';
    case 'info': return 'ℹ️';
    default: return '';
  }
});

const closeAlert = () => {
  visible.value = false;
  emit('close');
  
  if (autoCloseTimeout) {
    window.clearTimeout(autoCloseTimeout);
    autoCloseTimeout = null;
  }
};

onMounted(() => {
  if (props.autoClose > 0) {
    autoCloseTimeout = window.setTimeout(() => {
      closeAlert();
    }, props.autoClose);
  }
});
</script>

<template>
  <transition name="alert-fade">
    <div v-if="visible" :class="alertClasses" role="alert">
      <div class="alert-content">
        <div v-if="variantIcon" class="alert-icon">{{ variantIcon }}</div>
        <div class="alert-body">
          <div v-if="title" class="alert-title">{{ title }}</div>
          <div class="alert-message">
            <slot></slot>
          </div>
        </div>
      </div>
      
      <button 
        v-if="dismissible" 
        type="button" 
        class="alert-close" 
        aria-label="Close" 
        @click="closeAlert"
      >
        ×
      </button>
    </div>
  </transition>
</template>

<style scoped>
.alert {
  position: relative;
  padding: var(--space-md) var(--space-lg);
  margin-bottom: var(--space-md);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  width: 100%;
}

.alert-primary {
  background-color: var(--info-light);
  color: var(--primary-800);
}

.alert-secondary {
  background-color: var(--neutral-100);
  color: var(--neutral-800);
}

.alert-success {
  background-color: var(--success-light);
  color: var(--secondary-800);
}

.alert-danger {
  background-color: var(--error-light);
  color: #721c24;
}

.alert-warning {
  background-color: var(--warning-light);
  color: #856404;
}

.alert-info {
  background-color: var(--info-light);
  color: var(--primary-800);
}

.alert-bordered {
  border: 1px solid transparent;
}

.alert-primary.alert-bordered {
  border-color: var(--info);
}

.alert-secondary.alert-bordered {
  border-color: var(--neutral-300);
}

.alert-success.alert-bordered {
  border-color: var(--success);
}

.alert-danger.alert-bordered {
  border-color: var(--error);
}

.alert-warning.alert-bordered {
  border-color: var(--warning);
}

.alert-info.alert-bordered {
  border-color: var(--info);
}

.alert-elevated {
  box-shadow: var(--shadow-md);
}

.alert-content {
  display: flex;
  align-items: flex-start;
}

.alert-with-icon .alert-content {
  gap: var(--space-sm);
}

.alert-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  font-size: 1.1em;
}

.alert-body {
  flex: 1;
}

.alert-title {
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-xs);
  font-size: var(--font-size-md);
}

.alert-message {
  font-size: var(--font-size-sm);
}

.alert-close {
  position: absolute;
  top: var(--space-sm);
  right: var(--space-sm);
  padding: var(--space-xs);
  color: inherit;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xl);
  line-height: 0.7;
  cursor: pointer;
  opacity: 0.5;
  transition: opacity var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

.alert-close:hover {
  opacity: 0.75;
}

.alert-close:focus {
  opacity: 1;
  outline: none;
}

.alert-dismissible {
  padding-right: calc(var(--space-lg) + 28px);
}

/* Alert animations */
.alert-fade-enter-active,
.alert-fade-leave-active {
  transition: opacity var(--transition-normal), transform var(--transition-normal);
}

.alert-fade-enter-from,
.alert-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Dark mode adjustments */
:root.dark-mode .alert-primary,
.dark-mode .alert-primary {
  background-color: rgba(var(--primary-500-rgb), 0.15);
  color: var(--primary-200);
}

:root.dark-mode .alert-secondary,
.dark-mode .alert-secondary {
  background-color: rgba(var(--neutral-500-rgb), 0.15);
  color: var(--neutral-200);
}

:root.dark-mode .alert-success,
.dark-mode .alert-success {
  background-color: rgba(var(--secondary-500-rgb), 0.15);
  color: var(--secondary-200);
}

:root.dark-mode .alert-danger,
.dark-mode .alert-danger {
  background-color: rgba(var(--error-rgb), 0.15);
  color: #ffb3b3;
}

:root.dark-mode .alert-warning,
.dark-mode .alert-warning {
  background-color: rgba(var(--warning-rgb), 0.15);
  color: #ffe58f;
}

:root.dark-mode .alert-info,
.dark-mode .alert-info {
  background-color: rgba(var(--primary-500-rgb), 0.15);
  color: var(--primary-200);
}
</style> 
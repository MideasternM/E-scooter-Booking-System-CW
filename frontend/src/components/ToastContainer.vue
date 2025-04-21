<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

export interface ToastOptions {
  id?: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
  duration?: number;
  closable?: boolean;
  position?: ToastPosition;
}

type ToastPosition = 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left' | 'top-center' | 'bottom-center';

const props = defineProps({
  position: {
    type: String as () => ToastPosition,
    default: 'top-right'
  },
  maxToasts: {
    type: Number,
    default: 5
  }
});

const toasts = ref<ToastOptions[]>([]);

// Create global event bus
let toastEventHandler: ((toast: ToastOptions) => void) | null = null;

const addToast = (toast: ToastOptions) => {
  // Generate an ID if not provided
  const toastWithId = {
    ...toast,
    id: toast.id || `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    duration: toast.duration ?? 5000,
    closable: toast.closable ?? true,
    position: toast.position || props.position
  };
  
  // Add new toast to the list
  toasts.value.push(toastWithId);
  
  // Limit the number of visible toasts
  if (toasts.value.length > props.maxToasts) {
    toasts.value.shift();
  }
  
  // Auto-remove toast after duration
  if (toastWithId.duration > 0) {
    setTimeout(() => removeToast(toastWithId.id!), toastWithId.duration);
  }
};

const removeToast = (id: string) => {
  const index = toasts.value.findIndex(toast => toast.id === id);
  if (index !== -1) {
    toasts.value.splice(index, 1);
  }
};

// Create and expose the toast API
const createToast = (options: ToastOptions) => {
  addToast(options);
  return options.id;
};

const createSuccessToast = (message: string, title?: string, options?: Partial<ToastOptions>) => {
  return createToast({ type: 'success', message, title, ...options });
};

const createErrorToast = (message: string, title?: string, options?: Partial<ToastOptions>) => {
  return createToast({ type: 'error', message, title, ...options });
};

const createWarningToast = (message: string, title?: string, options?: Partial<ToastOptions>) => {
  return createToast({ type: 'warning', message, title, ...options });
};

const createInfoToast = (message: string, title?: string, options?: Partial<ToastOptions>) => {
  return createToast({ type: 'info', message, title, ...options });
};

// Expose the API to the app
const toastApi = {
  show: createToast,
  success: createSuccessToast,
  error: createErrorToast,
  warning: createWarningToast,
  info: createInfoToast,
  clear: () => {
    toasts.value = [];
  }
};

// We use window for the global event bus
const initEventBus = () => {
  // Remove previous handler if exists
  if (toastEventHandler) {
    window.removeEventListener('add-toast', toastEventHandler as any);
  }
  
  // Add new handler
  toastEventHandler = ((event: CustomEvent<ToastOptions>) => {
    addToast(event.detail);
  }) as any;
  
  window.addEventListener('add-toast', toastEventHandler as any);
  
  // Make toast API globally available
  window.$toast = toastApi;
};

// Get toast container class based on position
const getContainerClasses = () => [
  'toast-container',
  `position-${props.position}`
];

onMounted(() => {
  initEventBus();
});

onUnmounted(() => {
  if (toastEventHandler) {
    window.removeEventListener('add-toast', toastEventHandler as any);
    toastEventHandler = null;
  }
});

// Get visual style classes based on toast type
const getToastClasses = (type: string) => [
  'toast',
  `toast-${type}`
];

// Track animations
const removingToasts = ref<string[]>([]);

const closeToast = (id: string) => {
  removingToasts.value.push(id);
  setTimeout(() => {
    removeToast(id);
    const index = removingToasts.value.indexOf(id);
    if (index !== -1) {
      removingToasts.value.splice(index, 1);
    }
  }, 300); // Match the CSS animation duration
};

const isRemoving = (id: string) => removingToasts.value.includes(id);

// Define toast icon based on type
const getToastIcon = (type: string) => {
  switch (type) {
    case 'success': return '✓';
    case 'error': return '✗';
    case 'warning': return '⚠️';
    case 'info': return 'ℹ️';
    default: return '';
  }
};

// For TypeScript to recognize window.$toast
declare global {
  interface Window {
    $toast: typeof toastApi;
  }
}
</script>

<template>
  <div :class="getContainerClasses()">
    <transition-group name="toast">
      <div 
        v-for="toast in toasts" 
        :key="toast.id" 
        :class="[...getToastClasses(toast.type), { 'removing': isRemoving(toast.id!) }]"
        role="alert"
      >
        <div class="toast-icon">
          {{ getToastIcon(toast.type) }}
        </div>
        <div class="toast-content">
          <div v-if="toast.title" class="toast-title">{{ toast.title }}</div>
          <div class="toast-message">{{ toast.message }}</div>
        </div>
        <button 
          v-if="toast.closable" 
          @click="closeToast(toast.id!)" 
          class="toast-close" 
          aria-label="Close toast"
        >
          ×
        </button>
      </div>
    </transition-group>
  </div>
</template>

<style>
.toast-container {
  position: fixed;
  z-index: var(--z-toast);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  padding: var(--space-md);
  pointer-events: none;
  max-width: 360px;
  width: 100%;
}

/* Position variations */
.toast-container.position-top-right {
  top: 0;
  right: 0;
}

.toast-container.position-top-left {
  top: 0;
  left: 0;
}

.toast-container.position-bottom-right {
  bottom: 0;
  right: 0;
}

.toast-container.position-bottom-left {
  bottom: 0;
  left: 0;
}

.toast-container.position-top-center {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.toast-container.position-bottom-center {
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.toast {
  display: flex;
  align-items: flex-start;
  padding: var(--space-md);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  pointer-events: auto;
  overflow: hidden;
  position: relative;
  animation: toast-in 0.3s ease-out;
  background-color: var(--card-bg);
  border-left: 4px solid;
}

.toast.removing {
  animation: toast-out 0.3s ease-in forwards;
}

.toast-success {
  border-left-color: var(--success);
}

.toast-error {
  border-left-color: var(--error);
}

.toast-warning {
  border-left-color: var(--warning);
}

.toast-info {
  border-left-color: var(--info);
}

.toast-icon {
  margin-right: var(--space-sm);
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.toast-success .toast-icon {
  color: var(--success);
}

.toast-error .toast-icon {
  color: var(--error);
}

.toast-warning .toast-icon {
  color: var(--warning);
}

.toast-info .toast-icon {
  color: var(--info);
}

.toast-content {
  flex: 1;
}

.toast-title {
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-xs);
  color: var(--text-primary);
}

.toast-message {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  word-break: break-word;
}

.toast-close {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  margin-left: var(--space-sm);
  font-size: var(--font-size-xl);
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  transition: color var(--transition-fast);
}

.toast-close:hover {
  color: var(--text-primary);
}

/* Animations */
@keyframes toast-in {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes toast-out {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(calc(100% + 20px));
    opacity: 0;
  }
}

.toast-enter-active {
  animation: toast-in 0.3s ease-out;
}

.toast-leave-active {
  animation: toast-out 0.3s ease-in forwards;
}

.toast-move {
  transition: transform 0.3s ease;
}

/* Dark mode adjustments */
:root.dark-mode .toast,
.dark-mode .toast {
  background-color: var(--neutral-800);
}
</style> 
<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import BaseButton from './BaseButton.vue';

type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  size: {
    type: String as () => ModalSize,
    default: 'md'
  },
  fullscreen: {
    type: Boolean,
    default: false
  },
  hideClose: {
    type: Boolean,
    default: false
  },
  disableOutsideClick: {
    type: Boolean,
    default: false
  },
  disableEscKey: {
    type: Boolean,
    default: false
  },
  centered: {
    type: Boolean,
    default: false
  },
  scrollable: {
    type: Boolean,
    default: false
  },
  persistent: {
    type: Boolean,
    default: false
  },
  contentClass: {
    type: String,
    default: ''
  },
  overlayClass: {
    type: String,
    default: ''
  },
  noAnimation: {
    type: Boolean,
    default: false
  },
  preventScroll: {
    type: Boolean,
    default: true
  },
  closeOnNavigation: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:modelValue', 'open', 'close']);

const modalVisible = ref(props.modelValue);
const modalActive = ref(false);
const modalElement = ref<HTMLElement | null>(null);
const previouslyFocusedElement = ref<HTMLElement | null>(null);

const modalContainerClasses = computed(() => [
  'modal-container',
  `modal-${props.size}`,
  props.contentClass,
  {
    'modal-fullscreen': props.fullscreen,
    'modal-scrollable': props.scrollable,
    'modal-no-animation': props.noAnimation
  }
]);

const modalOverlayClasses = computed(() => [
  'modal-overlay',
  props.overlayClass,
  {
    'active': modalActive.value,
    'centered': props.centered,
    'modal-no-animation': props.noAnimation
  }
]);

const closeModal = () => {
  if (props.persistent) return;
  
  modalActive.value = false;
  setTimeout(() => {
    modalVisible.value = false;
    emit('update:modelValue', false);
    emit('close');
    
    if (props.preventScroll) {
      document.body.classList.remove('modal-open');
    }
    
    // Return focus to previously focused element
    if (previouslyFocusedElement.value) {
      previouslyFocusedElement.value.focus();
      previouslyFocusedElement.value = null;
    }
  }, 300);
};

const handleOutsideClick = (event: MouseEvent) => {
  if (props.disableOutsideClick || props.persistent) return;
  
  const target = event.target as HTMLElement;
  if (modalElement.value && !modalElement.value.contains(target)) {
    closeModal();
  }
};

const handleEscKey = (event: KeyboardEvent) => {
  if (props.disableEscKey || props.persistent) return;
  
  if (event.key === 'Escape' && modalVisible.value) {
    closeModal();
  }
};

const openModal = () => {
  // Save the currently focused element
  previouslyFocusedElement.value = document.activeElement as HTMLElement;
  
  modalVisible.value = true;
  
  nextTick(() => {
    modalActive.value = true;
    emit('open');
    
    if (props.preventScroll) {
      document.body.classList.add('modal-open');
    }
    
    // Focus the modal
    nextTick(() => {
      const focusableElements = modalElement.value?.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      
      if (focusableElements && focusableElements.length > 0) {
        (focusableElements[0] as HTMLElement).focus();
      } else if (modalElement.value) {
        modalElement.value.focus();
      }
    });
  });
};

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    openModal();
  } else {
    closeModal();
  }
});

// Handle navigation events to close the modal
const handlePopState = () => {
  if (props.closeOnNavigation && modalVisible.value) {
    closeModal();
  }
};

onMounted(() => {
  if (props.modelValue) {
    openModal();
  }
  
  document.addEventListener('keydown', handleEscKey);
  window.addEventListener('popstate', handlePopState);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleEscKey);
  window.removeEventListener('popstate', handlePopState);
  
  if (modalVisible.value && props.preventScroll) {
    document.body.classList.remove('modal-open');
  }
});
</script>

<template>
  <teleport to="body">
    <div
      v-if="modalVisible"
      :class="modalOverlayClasses"
      @click="handleOutsideClick"
      aria-modal="true"
      role="dialog"
      tabindex="-1"
    >
      <div 
        ref="modalElement"
        :class="modalContainerClasses"
        tabindex="0"
      >
        <div class="modal-header" v-if="title || $slots.header">
          <slot name="header">
            <h3 class="modal-title">{{ title }}</h3>
          </slot>
          
          <button 
            v-if="!hideClose" 
            type="button" 
            class="modal-close" 
            aria-label="Close" 
            @click="closeModal"
          >
            ×
          </button>
        </div>
        
        <div class="modal-body">
          <slot></slot>
        </div>
        
        <div class="modal-footer" v-if="$slots.footer">
          <slot name="footer">
            <BaseButton variant="outline" @click="closeModal">Cancel</BaseButton>
            <BaseButton variant="primary">Confirm</BaseButton>
          </slot>
        </div>
      </div>
    </div>
  </teleport>
</template>

<style>
/* Apply to body when modal is open to prevent scrolling */
body.modal-open {
  overflow: hidden;
  padding-right: 15px; /* Prevents layout shift when scrollbar is hidden */
}
</style>

<style scoped>
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
  transition: opacity var(--transition-normal);
}

.modal-overlay.active {
  opacity: 1;
}

.modal-overlay.centered {
  align-items: center;
}

.modal-container {
  background-color: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  width: 100%;
  max-height: calc(100vh - 2 * var(--space-lg));
  display: flex;
  flex-direction: column;
  transform: scale(0.95);
  opacity: 0;
  transition: transform var(--transition-normal), opacity var(--transition-normal);
  margin: 0 var(--space-lg);
  outline: none;
}

.modal-overlay.active .modal-container {
  transform: scale(1);
  opacity: 1;
}

.modal-sm {
  max-width: 300px;
}

.modal-md {
  max-width: 500px;
}

.modal-lg {
  max-width: 800px;
}

.modal-xl {
  max-width: 1140px;
}

.modal-full {
  max-width: calc(100vw - 2 * var(--space-lg));
  margin: 0 var(--space-lg);
}

.modal-fullscreen {
  max-width: 100%;
  max-height: 100vh;
  width: 100vw;
  height: 100vh;
  border-radius: 0;
  margin: 0;
  position: fixed;
  top: 0;
  left: 0;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-lg);
  border-bottom: 1px solid var(--border-color);
}

.modal-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  margin: 0;
  color: var(--text-primary);
}

.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-2xl);
  line-height: 1;
  padding: var(--space-xs);
  color: var(--text-muted);
  transition: color var(--transition-fast);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
}

.modal-close:hover {
  color: var(--error);
  background-color: rgba(var(--error-rgb), 0.1);
}

.modal-close:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(var(--primary-500-rgb), 0.2);
}

.modal-body {
  padding: var(--space-lg);
  overflow-y: auto;
  flex: 1;
  color: var(--text-primary);
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: var(--space-lg);
  border-top: 1px solid var(--border-color);
  gap: var(--space-sm);
}

.modal-scrollable .modal-body {
  max-height: 60vh;
  overflow-y: auto;
}

.modal-no-animation,
.modal-no-animation .modal-container {
  transition: none;
}

/* Responsive adjustments */
@media (max-width: 576px) {
  .modal-container {
    margin: 0 var(--space-md);
  }
  
  .modal-sm,
  .modal-md {
    max-width: calc(100vw - 2 * var(--space-md));
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding: var(--space-md);
  }
}
</style> 
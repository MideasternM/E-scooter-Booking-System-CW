<script setup lang="ts">
import { computed, useSlots } from 'vue';

type CardVariant = 'default' | 'primary' | 'secondary' | 'glass' | 'outlined';

const slots = useSlots();

const props = defineProps({
  variant: {
    type: String as () => CardVariant,
    default: 'default'
  },
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  noPadding: {
    type: Boolean,
    default: false
  },
  hoverable: {
    type: Boolean,
    default: false
  },
  clickable: {
    type: Boolean,
    default: false
  },
  href: {
    type: String,
    default: ''
  },
  target: {
    type: String,
    default: '_self'
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['click']);

const handleClick = (event: MouseEvent) => {
  if (props.clickable && !props.loading) {
    emit('click', event);
  }
};

const cardClasses = computed(() => {
  return [
    'card',
    `card-${props.variant}`,
    {
      'card-no-padding': props.noPadding,
      'card-hoverable': props.hoverable,
      'card-clickable': props.clickable,
      'card-loading': props.loading
    }
  ];
});

const Element = computed(() => props.href ? 'a' : props.clickable ? 'button' : 'div');
</script>

<template>
  <component 
    :is="Element" 
    :class="cardClasses" 
    :href="href" 
    :target="target"
    @click="handleClick"
  >
    <div v-if="loading" class="card-loader">
      <div class="spinner"></div>
    </div>
    
    <div v-if="title || subtitle" class="card-header">
      <div>
        <h3 v-if="title" class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </div>
      <div class="card-header-actions">
        <slot name="actions"></slot>
      </div>
    </div>
    
    <div class="card-content">
      <slot></slot>
    </div>
    
    <div v-if="slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </component>
</template>

<style scoped>
.card {
  background-color: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  color: var(--text-primary);
  padding: var(--space-lg);
  margin-bottom: var(--space-lg);
}

.card-default {
  background-color: var(--card-bg);
}

.card-primary {
  background-color: var(--primary-600);
  color: white;
}

.card-primary .card-subtitle {
  color: rgba(255, 255, 255, 0.8);
}

.card-secondary {
  background-color: var(--secondary-600);
  color: white;
}

.card-secondary .card-subtitle {
  color: rgba(255, 255, 255, 0.8);
}

.card-glass {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.08);
}

.card-outlined {
  background-color: transparent;
  border: 1px solid var(--border-color);
  box-shadow: none;
}

.card-no-padding {
  padding: 0;
}

.card-no-padding .card-header {
  padding: var(--space-lg) var(--space-lg) 0 var(--space-lg);
}

.card-no-padding .card-content {
  padding: var(--space-lg);
}

.card-no-padding .card-footer {
  padding: 0 var(--space-lg) var(--space-lg) var(--space-lg);
}

.card-hoverable {
  cursor: pointer;
}

.card-hoverable:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.card-clickable {
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.card-clickable:focus-visible {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

.card-loading {
  pointer-events: none;
}

.card-loader {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  border-radius: inherit;
}

:root.dark-mode .card-loader,
.dark-mode .card-loader {
  background-color: rgba(0, 0, 0, 0.5);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-md);
  gap: var(--space-md);
}

.card-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  margin: 0 0 var(--space-xs) 0;
  line-height: var(--line-height-tight);
}

.card-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
  line-height: var(--line-height-tight);
}

.card-content {
  flex: 1;
}

.card-content:only-child {
  margin: 0;
}

.card-footer {
  margin-top: var(--space-md);
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
}

/* Dark mode specific styles */
:root.dark-mode .card-glass,
.dark-mode .card-glass {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

@media (prefers-color-scheme: dark) {
  :root:not(.light-mode) .card-glass {
    background: rgba(0, 0, 0, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.05);
  }
}

/* Spinner animation */
.spinner {
  display: inline-block;
  width: 40px;
  height: 40px;
  border: 3px solid rgba(var(--primary-500-rgb), 0.2);
  border-radius: 50%;
  border-top-color: var(--primary-500);
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .card {
    padding: var(--space-md);
  }
  
  .card-no-padding .card-header {
    padding: var(--space-md) var(--space-md) 0 var(--space-md);
  }

  .card-no-padding .card-content {
    padding: var(--space-md);
  }

  .card-no-padding .card-footer {
    padding: 0 var(--space-md) var(--space-md) var(--space-md);
  }
  
  .card-title {
    font-size: var(--font-size-lg);
  }
}
</style> 
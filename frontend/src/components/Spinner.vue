<script setup lang="ts">
defineProps({
  size: {
    type: String as () => 'sm' | 'md' | 'lg',
    default: 'md'
  },
  color: {
    type: String as () => 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark',
    default: 'primary'
  },
  fullPage: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: ''
  }
});
</script>

<template>
  <div :class="['spinner-container', { 'full-page': fullPage }]">
    <div :class="['spinner', `spinner-${size}`, `spinner-${color}`]" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
    <div v-if="text" class="spinner-text">{{ text }}</div>
  </div>
</template>

<style scoped>
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  padding: var(--space-md);
}

.spinner-container.full-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(var(--neutral-500-rgb), 0.5);
  z-index: var(--z-modal);
}

.spinner {
  display: inline-block;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  border: 0.25rem solid currentColor;
  border-right-color: transparent;
  animation: spinner-animation 0.75s linear infinite;
  opacity: 0.7;
}

.spinner-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.125rem;
}

.spinner-md {
  width: 2rem;
  height: 2rem;
  border-width: 0.25rem;
}

.spinner-lg {
  width: 3rem;
  height: 3rem;
  border-width: 0.35rem;
}

.spinner-text {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  text-align: center;
  max-width: 80%;
}

/* Color variants */
.spinner-primary {
  color: var(--primary-500);
}

.spinner-secondary {
  color: var(--secondary-500);
}

.spinner-success {
  color: var(--success);
}

.spinner-danger {
  color: var(--error);
}

.spinner-warning {
  color: var(--warning);
}

.spinner-info {
  color: var(--info);
}

.spinner-light {
  color: var(--neutral-100);
}

.spinner-dark {
  color: var(--neutral-800);
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@keyframes spinner-animation {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Dark mode adjustments */
.dark-mode .spinner-light {
  color: var(--neutral-100);
}

.dark-mode .spinner-dark {
  color: var(--neutral-200);
}

.dark-mode .spinner-text {
  color: var(--text-primary);
}

.dark-mode .spinner-container.full-page {
  background-color: rgba(0, 0, 0, 0.7);
}
</style> 
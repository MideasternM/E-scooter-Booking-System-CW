<script setup lang="ts">
import { computed, useSlots } from 'vue';

type ButtonVariant = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'outline' | 'link';
type ButtonSize = 'sm' | 'md' | 'lg';
type ButtonType = 'button' | 'submit' | 'reset';

// Get slots to check if default slot exists
const slots = useSlots();

const props = defineProps({
  type: {
    type: String as () => ButtonType,
    default: 'button'
  },
  variant: {
    type: String as () => ButtonVariant,
    default: 'primary'
  },
  size: {
    type: String as () => ButtonSize,
    default: 'md'
  },
  block: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  icon: {
    type: String,
    default: ''
  },
  iconRight: {
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
  rel: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['click']);

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event);
  }
};

const buttonClasses = computed(() => {
  return [
    'btn',
    `btn-${props.variant}`,
    `btn-${props.size}`,
    {
      'btn-block': props.block,
      'btn-loading': props.loading,
      'btn-icon-right': props.iconRight && props.icon,
      'btn-icon-only': props.icon && !slots.default
    }
  ];
});

const isLink = computed(() => !!props.href);
</script>

<template>
  <a
    v-if="isLink"
    :href="href"
    :target="target"
    :rel="rel"
    :class="buttonClasses"
    @click="handleClick"
  >
    <span v-if="loading" class="spinner-sm"></span>
    <span v-else-if="icon && !iconRight" class="btn-icon">{{ icon }}</span>
    <slot></slot>
    <span v-if="icon && iconRight" class="btn-icon">{{ icon }}</span>
  </a>

  <button
    v-else
    :type="type"
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="spinner-sm"></span>
    <span v-else-if="icon && !iconRight" class="btn-icon">{{ icon }}</span>
    <slot></slot>
    <span v-if="icon && iconRight" class="btn-icon">{{ icon }}</span>
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background-color: var(--button-bg);
  color: var(--button-text);
  border: none;
  border-radius: var(--radius-md);
  font-family: var(--font-family);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast) var(--easing-standard);
  padding: var(--space-sm) var(--space-lg);
  font-size: var(--font-size-md);
  line-height: 1.5;
  text-align: center;
  gap: var(--space-sm);
  white-space: nowrap;
  outline: none;
  box-shadow: var(--shadow-sm);
  text-decoration: none;
}

/* Sizes */
.btn-sm {
  padding: calc(var(--space-xs) * 1.5) var(--space-md);
  font-size: var(--font-size-sm);
  border-radius: var(--radius-sm);
}

.btn-md {
  padding: var(--space-sm) var(--space-lg);
}

.btn-lg {
  padding: var(--space-md) var(--space-xl);
  font-size: var(--font-size-lg);
  border-radius: var(--radius-lg);
}

/* Variants */
.btn-primary {
  background-color: var(--primary-600);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-700);
  box-shadow: var(--shadow-md);
}

.btn-primary:active:not(:disabled) {
  background-color: var(--primary-800);
  transform: translateY(1px);
}

.btn-secondary {
  background-color: var(--secondary-600);
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: var(--secondary-700);
  box-shadow: var(--shadow-md);
}

.btn-secondary:active:not(:disabled) {
  background-color: var(--secondary-800);
  transform: translateY(1px);
}

.btn-success {
  background-color: var(--success);
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: var(--secondary-700);
  box-shadow: var(--shadow-md);
}

.btn-success:active:not(:disabled) {
  background-color: var(--secondary-800);
  transform: translateY(1px);
}

.btn-danger {
  background-color: var(--error);
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #d9363e;
  box-shadow: var(--shadow-md);
}

.btn-danger:active:not(:disabled) {
  background-color: #b2292f;
  transform: translateY(1px);
}

.btn-warning {
  background-color: var(--warning);
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background-color: #d89614;
  box-shadow: var(--shadow-md);
}

.btn-warning:active:not(:disabled) {
  background-color: #b37910;
  transform: translateY(1px);
}

.btn-info {
  background-color: var(--info);
  color: white;
}

.btn-info:hover:not(:disabled) {
  background-color: var(--primary-700);
  box-shadow: var(--shadow-md);
}

.btn-info:active:not(:disabled) {
  background-color: var(--primary-800);
  transform: translateY(1px);
}

.btn-outline {
  background-color: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  box-shadow: none;
}

.btn-outline:hover:not(:disabled) {
  background-color: var(--neutral-100);
  border-color: var(--neutral-400);
}

.btn-outline:active:not(:disabled) {
  background-color: var(--neutral-200);
  transform: translateY(1px);
}

.btn-link {
  background-color: transparent;
  color: var(--primary-600);
  padding: 0;
  border-radius: 0;
  box-shadow: none;
  border: none;
}

.btn-link:hover:not(:disabled) {
  color: var(--primary-700);
  text-decoration: underline;
  background-color: transparent;
}

/* Block button */
.btn-block {
  display: flex;
  width: 100%;
}

/* State: disabled */
.btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
  box-shadow: none;
}

/* State: loading */
.btn-loading {
  cursor: wait;
}

.btn-loading .spinner-sm {
  animation: spin 1s linear infinite;
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-radius: 50%;
  border-top-color: transparent;
  vertical-align: text-bottom;
  margin-right: var(--space-xs);
}

/* Icons */
.btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-icon-right {
  flex-direction: row-reverse;
}

.btn-icon-only {
  width: 2.5rem;
  height: 2.5rem;
  padding: 0;
}

.btn-sm.btn-icon-only {
  width: 2rem;
  height: 2rem;
}

.btn-lg.btn-icon-only {
  width: 3rem;
  height: 3rem;
}

/* Hover effect */
.btn:hover:not(:disabled):not(.btn-link) {
  transform: translateY(-1px);
}

.btn:active:not(:disabled):not(.btn-link) {
  transition-duration: 0.1s;
}

/* Focus effect */
.btn:focus-visible {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
  box-shadow: var(--shadow-md);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Dark mode adjustments */
:root.dark-mode .btn-outline,
.dark-mode .btn-outline {
  border-color: var(--neutral-700);
}

:root.dark-mode .btn-outline:hover:not(:disabled),
.dark-mode .btn-outline:hover:not(:disabled) {
  background-color: var(--neutral-800);
  border-color: var(--neutral-600);
}
</style> 
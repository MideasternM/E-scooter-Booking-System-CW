<script setup lang="ts">
type BadgeVariant = 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info' | 'light' | 'dark';
type BadgeSize = 'sm' | 'md' | 'lg';

const props = defineProps({
  variant: {
    type: String as () => BadgeVariant,
    default: 'primary'
  },
  size: {
    type: String as () => BadgeSize,
    default: 'md'
  },
  pill: {
    type: Boolean,
    default: true
  },
  outline: {
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
  pulse: {
    type: Boolean,
    default: false
  }
});
</script>

<template>
  <span :class="[
    'badge', 
    `badge-${variant}`, 
    `badge-${size}`,
    { 
      'badge-pill': pill,
      'badge-outline': outline,
      'badge-pulse': pulse,
      'badge-with-icon': icon,
      'badge-icon-right': iconRight && icon
    }
  ]">
    <span v-if="icon && !iconRight" class="badge-icon">{{ icon }}</span>
    <slot></slot>
    <span v-if="icon && iconRight" class="badge-icon">{{ icon }}</span>
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  vertical-align: middle;
  font-weight: var(--font-weight-medium);
  line-height: 1;
  padding: 0.35em 0.65em;
  font-size: 0.75em;
  border-radius: var(--radius-md);
  color: white;
  gap: 0.3em;
}

/* Sizes */
.badge-sm {
  font-size: 0.65em;
  padding: 0.25em 0.5em;
}

.badge-md {
  font-size: 0.75em;
  padding: 0.35em 0.65em;
}

.badge-lg {
  font-size: 0.85em;
  padding: 0.4em 0.75em;
}

/* Pill badges */
.badge-pill {
  border-radius: 9999px;
}

/* Variants */
.badge-primary {
  background-color: var(--primary-500);
}

.badge-secondary {
  background-color: var(--neutral-500);
}

.badge-success {
  background-color: var(--success);
}

.badge-warning {
  background-color: var(--warning);
  color: var(--neutral-900);
}

.badge-danger {
  background-color: var(--error);
}

.badge-info {
  background-color: var(--info);
}

.badge-light {
  background-color: var(--neutral-200);
  color: var(--text-primary);
}

.badge-dark {
  background-color: var(--neutral-800);
}

/* Outline badges */
.badge-outline {
  background-color: transparent;
  border: 1px solid currentColor;
}

.badge-outline.badge-primary {
  color: var(--primary-500);
}

.badge-outline.badge-secondary {
  color: var(--neutral-500);
}

.badge-outline.badge-success {
  color: var(--success);
}

.badge-outline.badge-warning {
  color: var(--warning);
}

.badge-outline.badge-danger {
  color: var(--error);
}

.badge-outline.badge-info {
  color: var(--info);
}

.badge-outline.badge-light {
  color: var(--neutral-600);
  border-color: var(--neutral-300);
}

.badge-outline.badge-dark {
  color: var(--neutral-800);
}

/* Badges with icons */
.badge-with-icon {
  padding-left: 0.45em;
  padding-right: 0.45em;
}

.badge-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.badge-icon-right {
  flex-direction: row-reverse;
}

/* Pulse animation */
.badge-pulse {
  position: relative;
}

.badge-pulse::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  animation: pulse 1.5s ease-in-out infinite;
  z-index: -1;
}

.badge-pulse.badge-primary::before {
  background-color: var(--primary-500);
}

.badge-pulse.badge-secondary::before {
  background-color: var(--neutral-500);
}

.badge-pulse.badge-success::before {
  background-color: var(--success);
}

.badge-pulse.badge-warning::before {
  background-color: var(--warning);
}

.badge-pulse.badge-danger::before {
  background-color: var(--error);
}

.badge-pulse.badge-info::before {
  background-color: var(--info);
}

.badge-pulse.badge-light::before {
  background-color: var(--neutral-200);
}

.badge-pulse.badge-dark::before {
  background-color: var(--neutral-800);
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.2);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

/* Dark mode adjustments */
:root.dark-mode .badge-light,
.dark-mode .badge-light {
  background-color: var(--neutral-700);
  color: var(--neutral-200);
}

:root.dark-mode .badge-outline.badge-light,
.dark-mode .badge-outline.badge-light {
  color: var(--neutral-300);
  border-color: var(--neutral-600);
}
</style> 
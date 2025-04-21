<script setup lang="ts">
import { computed, ref, watch } from 'vue';

type InputType = 'text' | 'email' | 'password' | 'search' | 'tel' | 'number' | 'url' | 'date' | 'time' | 'datetime-local';
type InputSize = 'sm' | 'md' | 'lg';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  type: {
    type: String as () => InputType,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  helperText: {
    type: String,
    default: ''
  },
  errorMessage: {
    type: String,
    default: ''
  },
  id: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  size: {
    type: String as () => InputSize,
    default: 'md'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  autofocus: {
    type: Boolean,
    default: false
  },
  autocomplete: {
    type: String,
    default: 'off'
  },
  min: {
    type: [String, Number],
    default: undefined
  },
  max: {
    type: [String, Number],
    default: undefined
  },
  step: {
    type: [String, Number],
    default: undefined
  },
  pattern: {
    type: String,
    default: undefined
  },
  icon: {
    type: String,
    default: ''
  },
  iconRight: {
    type: Boolean,
    default: false
  },
  iconClickable: {
    type: Boolean,
    default: false
  },
  hideValidation: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'input', 'change', 'focus', 'blur', 'icon-click']);

const inputValue = ref(props.modelValue);
const isFocused = ref(false);
const isPasswordVisible = ref(false);

const inputId = computed(() => props.id || `input-${Math.random().toString(36).substr(2, 9)}`);

const inputClass = computed(() => [
  'form-control',
  `form-control-${props.size}`,
  {
    'has-error': props.errorMessage && !props.hideValidation,
    'has-icon': props.icon,
    'has-icon-right': props.icon && props.iconRight,
    'is-focused': isFocused.value,
    'is-disabled': props.disabled,
    'is-readonly': props.readonly,
  }
]);

const inputType = computed(() => {
  if (props.type === 'password') {
    return isPasswordVisible.value ? 'text' : 'password';
  }
  return props.type;
});

watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue;
});

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const value = target.value;
  
  inputValue.value = value;
  emit('update:modelValue', value);
  emit('input', event);
};

const handleChange = (event: Event) => {
  emit('change', event);
};

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true;
  emit('focus', event);
};

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false;
  emit('blur', event);
};

const handleIconClick = (event: MouseEvent) => {
  if (props.type === 'password') {
    isPasswordVisible.value = !isPasswordVisible.value;
  }
  if (props.iconClickable) {
    emit('icon-click', event);
  }
};
</script>

<template>
  <div class="form-group">
    <label v-if="label" :for="inputId" class="form-label">
      {{ label }}
      <span v-if="required" class="required-indicator">*</span>
    </label>
    
    <div class="input-wrapper">
      <span 
        v-if="icon && !iconRight" 
        class="input-icon input-icon-left" 
        :class="{ 'clickable': iconClickable || type === 'password' }"
        @click="handleIconClick"
      >
        {{ type === 'password' ? (isPasswordVisible ? '👁️' : '👁️‍🗨️') : icon }}
      </span>
      
      <input
        :id="inputId"
        :type="inputType"
        :class="inputClass"
        :name="name"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :autofocus="autofocus"
        :autocomplete="autocomplete"
        :min="min"
        :max="max"
        :step="step"
        :pattern="pattern"
        :value="inputValue"
        @input="handleInput"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <span 
        v-if="icon && iconRight" 
        class="input-icon input-icon-right" 
        :class="{ 'clickable': iconClickable || type === 'password' }"
        @click="handleIconClick"
      >
        {{ type === 'password' ? (isPasswordVisible ? '👁️' : '👁️‍🗨️') : icon }}
      </span>
    </div>
    
    <div v-if="!hideValidation" class="form-feedback">
      <span v-if="errorMessage" class="form-error">{{ errorMessage }}</span>
      <span v-else-if="helperText" class="form-helper">{{ helperText }}</span>
    </div>
  </div>
</template>

<style scoped>
.form-group {
  margin-bottom: var(--space-md);
  width: 100%;
}

.form-label {
  display: block;
  margin-bottom: var(--space-xs);
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.required-indicator {
  color: var(--error);
  margin-left: 0.25em;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-control {
  display: block;
  width: 100%;
  padding: var(--space-sm) var(--space-md);
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: var(--input-text);
  background-color: var(--input-bg);
  background-clip: padding-box;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-md);
  transition: border-color var(--transition-fast), 
              box-shadow var(--transition-fast), 
              background-color var(--transition-fast);
  appearance: none;
}

.form-control::placeholder {
  color: var(--input-placeholder);
  opacity: 1;
}

.form-control:focus {
  outline: none;
  border-color: var(--input-focus);
  box-shadow: 0 0 0 3px rgba(var(--primary-500-rgb), 0.15);
}

.form-control.has-error {
  border-color: var(--error);
}

.form-control.has-error:focus {
  box-shadow: 0 0 0 3px rgba(var(--error-rgb), 0.15);
}

.form-control:disabled, 
.form-control.is-disabled {
  background-color: var(--disabled-bg);
  color: var(--disabled-text);
  cursor: not-allowed;
  opacity: 0.7;
}

.form-control.is-readonly {
  background-color: var(--neutral-100);
  cursor: default;
}

.form-control-sm {
  padding: calc(var(--space-xs) * 1.5) var(--space-md);
  font-size: var(--font-size-sm);
}

.form-control-lg {
  padding: var(--space-md) var(--space-lg);
  font-size: var(--font-size-lg);
}

.form-control.has-icon {
  padding-left: calc(var(--space-md) * 2.5);
}

.form-control.has-icon-right {
  padding-left: var(--space-md);
  padding-right: calc(var(--space-md) * 2.5);
}

.input-icon {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 2.5em;
  color: var(--input-placeholder);
  pointer-events: none;
  z-index: 2;
}

.input-icon.clickable {
  pointer-events: auto;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.input-icon.clickable:hover {
  color: var(--primary-600);
}

.input-icon-left {
  left: 0;
}

.input-icon-right {
  right: 0;
}

.form-feedback {
  min-height: 1.5em;
  margin-top: var(--space-xs);
  font-size: var(--font-size-sm);
}

.form-error {
  color: var(--error);
}

.form-helper {
  color: var(--text-muted);
}

/* Dark mode adjustments */
:root.dark-mode .form-control.is-readonly,
.dark-mode .form-control.is-readonly {
  background-color: var(--neutral-800);
}
</style> 
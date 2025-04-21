<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

// Default to system preference
const isDarkMode = ref(false);

// Check the user's color scheme preference
const checkUserThemePreference = () => {
  // Check localStorage first
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    return savedTheme === 'dark';
  }
  
  // If no localStorage setting, check system preference
  return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
};

// Apply theme to document
const applyTheme = (isDark: boolean) => {
  document.documentElement.classList.toggle('dark-mode', isDark);
  localStorage.setItem('theme', isDark ? 'dark' : 'light');
};

// Toggle theme
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value;
};

// Watch for changes and apply theme
watch(isDarkMode, (newValue) => {
  applyTheme(newValue);
});

// Initialize on mount
onMounted(() => {
  isDarkMode.value = checkUserThemePreference();
  applyTheme(isDarkMode.value);
  
  // Listen for system preference changes
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
    // Only update if user hasn't manually set a preference
    if (!localStorage.getItem('theme')) {
      isDarkMode.value = e.matches;
    }
  });
});
</script>

<template>
  <button 
    class="theme-toggle" 
    @click="toggleTheme" 
    :title="isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'"
    :aria-label="isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'"
  >
    <div class="toggle-track">
      <div class="toggle-indicator" :class="{ 'active': isDarkMode }">
        <span class="toggle-icon">{{ isDarkMode ? '🌙' : '☀️' }}</span>
      </div>
    </div>
  </button>
</template>

<style scoped>
.theme-toggle {
  position: relative;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  width: 60px;
  height: 32px;
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.theme-toggle:hover {
  transform: scale(1.05);
}

.theme-toggle:focus {
  outline: none;
}

.theme-toggle:focus-visible {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
  border-radius: var(--radius-full);
}

.toggle-track {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: rgba(var(--primary-500-rgb), 0.2);
  border-radius: var(--radius-full);
  transition: background-color var(--transition-normal);
  padding: 3px;
}

.toggle-indicator {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 26px;
  height: 26px;
  background-color: white;
  border-radius: 50%;
  transform: translateX(0);
  transition: transform var(--transition-normal) var(--easing-standard), 
              background-color var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.toggle-indicator.active {
  transform: translateX(28px);
  background-color: var(--primary-900);
}

.toggle-icon {
  font-size: 16px;
  line-height: 1;
  transition: transform var(--transition-fast);
}

.theme-toggle:hover .toggle-icon {
  transform: rotate(12deg) scale(1.1);
}

@media (prefers-color-scheme: dark) {
  .toggle-track {
    background-color: rgba(255, 255, 255, 0.2);
  }
  
  .toggle-indicator:not(.active) {
    background-color: var(--primary-800);
  }
  
  .toggle-indicator.active {
    background-color: var(--neutral-100);
  }
}
</style> 
import { vi, beforeEach } from 'vitest'
import { config } from '@vue/test-utils'

// 全局模拟localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}

// 全局注入localStorage
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

// 模拟组件中使用的全局属性
config.global.mocks = {
  $router: {
    push: vi.fn(),
    replace: vi.fn()
  }
}

// 设置全局组件存根
config.global.stubs = {
  // 如果你有全局组件需要存根，可以在这里添加
}

// 重置所有模拟
beforeEach(() => {
  vi.resetAllMocks()
})

export { localStorageMock } 
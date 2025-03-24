import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LoginView from '../../src/views/LoginView.vue'
import axios from 'axios'
import { createRouter, createWebHistory } from 'vue-router'

// 模拟axios和router
vi.mock('axios')
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: vi.fn()
    })
  }
})

// 模拟localStorage
Object.defineProperty(window, 'localStorage', {
  value: {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn()
  }
})

describe('LoginView', () => {
  let wrapper: any

  beforeEach(() => {
    // 重置所有模拟
    vi.resetAllMocks()
    
    // 创建挂载组件
    wrapper = mount(LoginView)
  })

  it('renders login form correctly', () => {
    expect(wrapper.find('h2').text()).toBe('Login')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').text()).toBe('Login')
  })

  it('updates input values when typing', async () => {
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    
    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')
    
    expect(wrapper.vm.username).toBe('testuser')
    expect(wrapper.vm.password).toBe('password123')
  })

  it('calls login API with correct credentials', async () => {
    // 模拟API响应
    vi.mocked(axios.post).mockResolvedValue({
      data: { token: 'fake-token' }
    })
    
    // 设置输入值
    await wrapper.find('input[type="text"]').setValue('testuser')
    await wrapper.find('input[type="password"]').setValue('password123')
    
    // 提交表单
    await wrapper.find('form').trigger('submit')
    
    // 验证API调用
    expect(axios.post).toHaveBeenCalledWith('/api/users/login', {
      username: 'testuser',
      password: 'password123'
    })
    
    // 验证localStorage设置和路由导航
    expect(window.localStorage.setItem).toHaveBeenCalledWith('token', 'fake-token')
  })
}) 
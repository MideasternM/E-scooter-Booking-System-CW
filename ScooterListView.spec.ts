import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ScooterListView from '../../src/views/ScooterListView.vue'
import axios from 'axios'

// 模拟数据
const mockScooters = [
  { id: 1, model: 'Xiaomi Pro 2', location: 'Central Park', batteryLevel: 85 },
  { id: 2, model: 'Segway Ninebot Max', location: 'Downtown', batteryLevel: 92 }
]

// 模拟axios
vi.mock('axios')

describe('ScooterListView', () => {
  let wrapper: any

  beforeEach(() => {
    // 重置所有模拟
    vi.resetAllMocks()
    
    // 模拟API响应
    vi.mocked(axios.get).mockResolvedValue({ data: mockScooters })
    
    // 创建挂载组件
    wrapper = mount(ScooterListView)
  })

  it('shows loading state initially', () => {
    expect(wrapper.find('div').text()).toContain('Loading...')
  })

  it('fetches and displays scooters', async () => {
    // 等待异步操作完成
    await flushPromises()
    
    // 验证API调用
    expect(axios.get).toHaveBeenCalledWith('/api/scooters/available')
    
    // 验证渲染的数据
    const scooterItems = wrapper.findAll('.scooter-item')
    expect(scooterItems.length).toBe(2)
    
    expect(scooterItems[0].text()).toContain('Xiaomi Pro 2')
    expect(scooterItems[0].text()).toContain('Central Park')
    expect(scooterItems[0].text()).toContain('85%')
    
    expect(scooterItems[1].text()).toContain('Segway Ninebot Max')
    expect(scooterItems[1].text()).toContain('Downtown')
    expect(scooterItems[1].text()).toContain('92%')
  })

  it('allows booking a scooter', async () => {
    // 模拟成功的预订请求
    vi.mocked(axios.post).mockResolvedValue({ data: { success: true } })
    
    // 等待初始加载完成
    await flushPromises()
    
    // 模拟window.alert
    const alertMock = vi.fn()
    window.alert = alertMock
    
    // 点击第一个scooter的预订按钮
    await wrapper.findAll('button')[0].trigger('click')
    
    // 等待异步操作完成
    await flushPromises()
    
    // 验证API调用
    expect(axios.post).toHaveBeenCalledWith('/api/bookings', {
      scooterId: 1,
      startTime: expect.any(String),
      endTime: expect.any(String)
    })
    
    // 验证alert被调用
    expect(alertMock).toHaveBeenCalledWith('Scooter booked successfully!')
    
    // 验证列表被刷新了
    expect(axios.get).toHaveBeenCalledTimes(2)
  })
}) 
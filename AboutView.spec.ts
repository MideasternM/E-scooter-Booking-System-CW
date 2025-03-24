import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AboutView from '../../src/views/AboutView.vue'

describe('关于页面组件', () => {
  it('正确渲染关于页面内容', () => {
    const wrapper = mount(AboutView)
    
    // 验证页面包含预期的标题
    expect(wrapper.find('h1').exists()).toBe(true)
    expect(wrapper.find('h1').text()).toBe('This is an about page')
    
    // 验证页面有正确的CSS类
    expect(wrapper.find('.about').exists()).toBe(true)
  })
}) 
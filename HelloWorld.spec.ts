import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import HelloWorld from '../../src/components/HelloWorld.vue'

describe('HelloWorld组件', () => {
  it('正确渲染传入的props', () => {
    const msg = '欢迎使用电动滑板车预订系统'
    const wrapper = mount(HelloWorld, {
      props: { msg }
    })
    
    // 验证标题渲染了正确的消息
    expect(wrapper.find('h1').text()).toBe(msg)
    
    // 验证组件有欢迎段落
    expect(wrapper.find('h3').exists()).toBe(true)
    
    // 验证组件包含链接
    expect(wrapper.findAll('a').length).toBe(2)
  })
}) 
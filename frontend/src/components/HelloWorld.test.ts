import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import HelloWorld from './HelloWorld.vue';

describe('HelloWorld.vue', () => {
  it('renders props.msg when passed', () => {
    const msg = 'Hello Vitest';
    const wrapper = mount(HelloWorld, {
      props: { msg },
    });
    // Check if the h1 element contains the msg
    expect(wrapper.find('h1').text()).toContain(msg);
  });

  it('renders the static text content correctly', () => {
    const wrapper = mount(HelloWorld, {
      props: { msg: 'Test Message' }, // msg is required
    });
    // Check for the presence of the static introductory text
    expect(wrapper.find('h3').text()).toContain('You’ve successfully created a project with');
    // Check for the presence of links
    expect(wrapper.find('a[href="https://vite.dev/"]').exists()).toBe(true);
    expect(wrapper.find('a[href="https://vuejs.org/"]').exists()).toBe(true);
  });
}); 
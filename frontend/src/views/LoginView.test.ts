import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, VueWrapper, RouterLinkStub } from '@vue/test-utils';
import LoginView from './LoginView.vue';
import { createPinia, setActivePinia } from 'pinia';

// Mock vue-router
const mockRouterPush = vi.fn();
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal() as Record<string, unknown>;
  return {
    ...actual, // Preserve other exports from vue-router if any
    useRouter: () => ({
      push: mockRouterPush,
    }),
    RouterLink: RouterLinkStub, // Use RouterLinkStub from @vue/test-utils
  };
});

// Mock auth store
const mockUserLogin = vi.fn();
vi.mock('../stores/auth', () => ({
  useAuthStore: () => ({
    userLogin: mockUserLogin,
    // Mock any other state or getters if the component reads them directly on mount
    // For LoginView, it doesn't seem to read any state directly for rendering,
    // but if it did, e.g., isAuthenticated, you'd mock it here.
    // isAuthenticated: false, 
  }),
}));

describe('LoginView.vue', () => {
  let wrapper: VueWrapper<any>;

  beforeEach(() => {
    // Reset mocks before each test
    mockRouterPush.mockClear();
    mockUserLogin.mockClear();

    // Create a new Pinia instance and activate it
    // This is important if your component interacts with a Pinia store
    setActivePinia(createPinia());

    // Mount the component
    wrapper = mount(LoginView, {
      global: {
        // No need for stubs here if RouterLink is properly mocked via vi.mock
      }
    });
  });

  // --- Initial Render Tests ---
  describe('Initial Render', () => {
    it('renders the main title "User Login"', () => {
      expect(wrapper.find('h2').text()).toBe('User Login');
    });

    it('renders the username input field and label', () => {
      const usernameInput = wrapper.find('input[placeholder="Enter your username"]');
      expect(wrapper.text()).toContain('Username:');
      expect(usernameInput.exists()).toBe(true);
    });

    it('renders the password input field and label', () => {
      const passwordInput = wrapper.find('input[placeholder="Enter your password"]');
      expect(wrapper.text()).toContain('Password:');
      expect(passwordInput.exists()).toBe(true);
    });

    it('renders the "Login" button initially', () => {
      const loginButton = wrapper.find('button[type="submit"]');
      expect(loginButton.exists()).toBe(true);
      expect(loginButton.text()).toBe('Login');
    });

    it('renders the registration link to "/register"', () => {
      const registerLink = wrapper.findComponent(RouterLinkStub); // Find RouterLinkStub
      expect(registerLink.exists()).toBe(true);
      expect(registerLink.props('to')).toBe('/register'); // Check its 'to' prop
      expect(registerLink.text()).toBe('No account? Register now');
    });

    it('does not display an error message initially', () => {
      const errorMessage = wrapper.find('.error-message');
      expect(errorMessage.exists()).toBe(false);
    });
  });
}); 
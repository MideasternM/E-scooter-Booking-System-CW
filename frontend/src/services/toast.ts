import type { ToastOptions } from '../components/ToastContainer.vue';

/**
 * Toast notification service for displaying user notifications
 */
class ToastService {
    /**
     * Show a toast notification with the given options
     */
    show(options: ToastOptions): string | undefined {
        if (typeof window === 'undefined') return;

        const event = new CustomEvent('add-toast', { detail: options });
        window.dispatchEvent(event);
        return options.id;
    }

    /**
     * Show a success toast notification
     */
    success(message: string, title?: string, options?: Partial<ToastOptions>): string | undefined {
        return this.show({ type: 'success', message, title, ...options });
    }

    /**
     * Show an error toast notification
     */
    error(message: string, title?: string, options?: Partial<ToastOptions>): string | undefined {
        return this.show({ type: 'error', message, title, ...options });
    }

    /**
     * Show a warning toast notification
     */
    warning(message: string, title?: string, options?: Partial<ToastOptions>): string | undefined {
        return this.show({ type: 'warning', message, title, ...options });
    }

    /**
     * Show an info toast notification
     */
    info(message: string, title?: string, options?: Partial<ToastOptions>): string | undefined {
        return this.show({ type: 'info', message, title, ...options });
    }

    /**
     * Clear all toast notifications
     */
    clear(): void {
        if (typeof window === 'undefined' || !window.$toast) return;
        window.$toast.clear();
    }
}

export const toast = new ToastService(); 
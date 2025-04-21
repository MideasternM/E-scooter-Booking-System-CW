import type { App } from 'vue';
import { createApp } from 'vue';
import BaseButton from './BaseButton.vue';
import BaseCard from './BaseCard.vue';
import Badge from './Badge.vue';
import FormInput from './FormInput.vue';
import Alert from './Alert.vue';
import Modal from './Modal.vue';
import Pagination from './Pagination.vue';
import Spinner from './Spinner.vue';
import ToastContainer from './ToastContainer.vue';

export {
    BaseButton,
    BaseCard,
    Badge,
    FormInput,
    Alert,
    Modal,
    Pagination,
    Spinner,
    ToastContainer
};

// Install function to register all components
export default {
    install: (app: App) => {
        // Register each component
        app.component('BaseButton', BaseButton);
        app.component('BaseCard', BaseCard);
        app.component('Badge', Badge);
        app.component('FormInput', FormInput);
        app.component('Alert', Alert);
        app.component('Modal', Modal);
        app.component('Pagination', Pagination);
        app.component('Spinner', Spinner);

        // Add global toast container with top-right position
        const toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        document.body.appendChild(toastContainer);

        // Mount toast container
        const toastApp = createApp(ToastContainer, { position: 'top-right' });
        toastApp.mount('#toast-container');
    }
}; 
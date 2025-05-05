// Corresponds to backend RentalStoreDTO
import type { StoreService } from './RentalStore';
import type { StoreStatus } from './RentalStore';

export interface RentalStoreDTO {
    name: string;
    code: string;
    latitude: number;
    longitude: number;
    address: string;
    openingHours?: string;
    contactPhone?: string | null;
    description?: string | null;
    imageUrl?: string | null;
    servicesOffered?: StoreService[];
    status?: StoreStatus;
} 
// Corresponds to backend RentalStore model

export interface RentalStore {
    id: number;
    name: string;
    code: string;
    latitude: number;
    longitude: number;
    address: string;
    availableScooters: string[]; // List of scooter codes
    status: StoreStatus;
    openingHours: string;
    contactPhone?: string | null;
    description?: string | null;
    imageUrl?: string | null;
    servicesOffered: StoreService[];
    createdAt: string; // ISO format date string
    lastUpdateTime: string; // ISO format date string
}

export enum StoreStatus {
    OPERATIONAL = 'OPERATIONAL',
    CLOSED_TEMPORARY = 'CLOSED_TEMPORARY',
    CLOSED_PERMANENT = 'CLOSED_PERMANENT'
}

export enum StoreService {
    RENTAL = 'RENTAL',
    REPAIR = 'REPAIR',
    SALES = 'SALES',
    CHARGING = 'CHARGING'
} 
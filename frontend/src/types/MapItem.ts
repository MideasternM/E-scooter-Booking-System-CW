import type { StoreStatus } from './RentalStore';

export type MapItemType = 'scooter' | 'store';

export interface BaseMapItem {
    id: number;
    type: MapItemType;
    latitude: number | null;
    longitude: number | null;
}

export interface ScooterMapItem extends BaseMapItem {
    type: 'scooter';
    status: string; // Or specific scooter status enum if available
    batteryLevel: number | null;
    model?: string;
    isAvailable?: boolean; // Match existing interface if used
}

export interface StoreMapItem extends BaseMapItem {
    type: 'store';
    name: string;
    code: string;
    status: StoreStatus; // Use the existing enum
    availableScooterCount: number;
    address?: string; // Optional address for popup
}

export type MapItem = ScooterMapItem | StoreMapItem; 
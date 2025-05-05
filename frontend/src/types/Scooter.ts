// Corresponds to backend Scooter model
import { type BigDecimal } from './common';

export interface Scooter {
    id: number;
    code: string;
    model: string;
    batteryLevel: BigDecimal;
    status: ScooterStatus;
    latitude: number;
    longitude: number;
    available: boolean;
    storeId?: number | null;
    createdAt: number; // timestamp
    lastUpdateTime: number; // timestamp
}

export enum ScooterStatus {
    AVAILABLE = 'AVAILABLE',
    IN_USE = 'IN_USE',
    MAINTENANCE = 'MAINTENANCE',
    CHARGING = 'CHARGING',
    OUT_OF_ORDER = 'OUT_OF_ORDER'
} 
export interface Zone {
    id?: number; // Optionnel car généré par la base de données
    patientId: string;
    nomZone: string;
    centreLat: number;
    centreLon: number;
    rayon: number;
}

export interface GeographicZone {
    id: number;
    name: string;
    type: 'authorized' | 'danger' | 'forbidden' | string;
    coordinates: {
        type: 'circle' | string;
        center: { lat: number; lng: number };
        radius: number;
    };
    patientIds: number[];
    isActive: boolean;
    notifyOnExit: boolean;
}

export interface PatientLocation {
    patientId: number;
    patientName: string;
    currentPosition: { lat: number; lng: number };
    lastUpdate: Date;
    isTracking: boolean;
}

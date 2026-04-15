// core/models/alert.model.ts
export interface Alert {
    id: number;
    patientId: string;
    patientName: string;
    type: string;
    timestamp: any;
    status: 'Active' | 'Resolved' | 'ignored'; // ← ajouter 'ignored'
    severity: 'High' | 'Medium' | 'Low';
    distanceHorsZone: number;
    zoneName?: string;
    notes?: string;
}

// ← Rajouter ceci
export interface AlertStatistics {
    totalAlerts:   number;
    activeAlerts:  number;
    resolvedAlerts: number;
    alertsByType:  { [key: string]: number };
    alertsByDay:   { date: string; count: number }[];
}
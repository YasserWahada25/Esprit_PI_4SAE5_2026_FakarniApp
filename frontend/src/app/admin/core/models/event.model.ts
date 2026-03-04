export interface Event {
    id: number;
    title: string;
    date: Date;  // Utilisation de Date pour gérer la date de l'événement
    startTime: string;  // Heure de début sous forme de string, par exemple '10:00 AM'
    status: 'upcoming' | 'past' | 'cancelled';  // État de l'événement
    participantsCount: number;  // Nombre de participants
    description?: string;  // Description facultative de l'événement
    reminders?: string[];  // Liste des rappels, par exemple ['1 day before', '1 hour before']
}
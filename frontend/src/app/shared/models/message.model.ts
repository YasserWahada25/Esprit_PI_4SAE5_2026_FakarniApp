export interface MessageRequest {
  senderId: string;
  receiverId: string;
  content: string;
}

export interface MessageResponse {
  id: string;
  senderId: string;
  receiverId: string;
  content: string;
  conversationId: string;
  timestamp: string;
}

/** Utilisateur réel de la plateforme (user-service) avec qui discuter. */
export interface Contact {
  id: string;
  name: string;
  /** Libellé du rôle (Médecin, Aidant / Proche, Patient). */
  role: string;
  initials: string;
}

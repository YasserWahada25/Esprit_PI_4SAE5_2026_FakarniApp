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

export interface Contact {
  id: string;
  name: string;
  status: 'online' | 'away' | 'offline';
  avatar: string;
  lastMessage?: string;
  unreadCount?: number;
}

export interface MockUser {
  id: string;
  name: string;
  email: string;
  role: string;
  avatar: string;
}

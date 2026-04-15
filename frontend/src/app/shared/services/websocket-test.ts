// Test simple pour vérifier que SockJS fonctionne
import SockJS from 'sockjs-client';

export function testWebSocket() {
  console.log('Test WebSocket...');
  
  try {
    const socket = new SockJS('http://localhost:8090/ws');
    
    socket.onopen = () => {
      console.log('✅ SockJS connecté!');
      socket.close();
    };
    
    socket.onerror = (error: any) => {
      console.error('❌ Erreur SockJS:', error);
    };
    
    socket.onclose = () => {
      console.log('🔌 SockJS fermé');
    };
  } catch (error) {
    console.error('❌ Exception:', error);
  }
}

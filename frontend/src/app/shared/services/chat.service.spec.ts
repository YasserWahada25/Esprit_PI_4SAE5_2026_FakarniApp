import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ChatService } from './chat.service';
import { MessageRequest, MessageResponse } from '../models/message.model';

describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8090/chat-service/api/messages';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ChatService]
    });
    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send a message', () => {
    const mockRequest: MessageRequest = {
      senderId: 'user1',
      receiverId: 'user2',
      content: 'Hello!'
    };

    const mockResponse: MessageResponse = {
      id: '123',
      senderId: 'user1',
      receiverId: 'user2',
      content: 'Hello!',
      conversationId: 'conv1',
      timestamp: '2024-01-01T10:00:00'
    };

    service.sendMessage(mockRequest).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/send`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should get conversation', () => {
    const mockMessages: MessageResponse[] = [
      {
        id: '1',
        senderId: 'user1',
        receiverId: 'user2',
        content: 'Hi',
        conversationId: 'conv1',
        timestamp: '2024-01-01T10:00:00'
      }
    ];

    service.getConversation('user1', 'user2').subscribe(messages => {
      expect(messages).toEqual(mockMessages);
    });

    const req = httpMock.expectOne(`${apiUrl}/conversation?user1=user1&user2=user2`);
    expect(req.request.method).toBe('GET');
    req.flush(mockMessages);
  });

  it('should delete a message', () => {
    service.deleteMessage('123').subscribe();

    const req = httpMock.expectOne(`${apiUrl}/123`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});

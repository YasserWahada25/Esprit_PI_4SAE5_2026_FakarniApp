# Posts Module - Backend Integration Guide

## Overview
The Posts module has been fully integrated with backend API calls. All components now communicate with the Post-Service backend at `http://localhost:8090/api/posts`.

## Architecture

### Components
1. **PostsLayoutComponent** - Layout wrapper for all posts pages
2. **PostsListComponent** - Displays all posts with search, filter, and sort
3. **CreatePostComponent** - Form for creating new posts
4. **MyPostsComponent** - User's posts management dashboard

### Service Layer
**PostsService** (`posts.service.ts`):
- `getPosts()` - GET all posts
- `createPost(request)` - POST new post
- `updatePost(id, request)` - PUT update post
- `deletePost(id)` - DELETE post
- `likePost(id)` / `unlikePost(id)` - Like/Unlike post
- `getUserPosts(userId)` - GET user-specific posts
- `loadPosts()` - Refresh posts in cache

## API Endpoints

The service uses the following endpoint:
```
Base URL: http://localhost:8090/api/posts
```

### Expected Endpoints from Backend

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts` | Get all posts |
| GET | `/api/posts/:id` | Get post by ID |
| POST | `/api/posts` | Create new post |
| PUT | `/api/posts/:id` | Update post |
| DELETE | `/api/posts/:id` | Delete post |
| POST | `/api/posts/:id/like` | Like post |
| DELETE | `/api/posts/:id/like` | Unlike post |
| GET | `/api/posts/user/:userId` | Get user's posts |

## Request/Response Models

### CreatePostRequest (Input)
```typescript
{
  title: string,
  content: string,
  category: string,
  image?: string
}
```

### PostResponse (Backend Output)
```typescript
{
  id: number,
  title: string,
  content: string,
  category: string,
  author: string,
  createdAt: string,
  updatedAt: string,
  userId: number,
  status?: 'published' | 'draft',
  likes?: number,
  comments?: number,
  views?: number
}
```

## Testing the Integration

### Prerequisites
1. Post-Service backend is running on `localhost:8090`
2. MySQL/Database is configured and running
3. Angular frontend is running on `localhost:4200`

### Test Flow

#### 1. Create a Post
1. Navigate to Posts module (click "POSTS" in navbar)
2. Click "Add Post" button
3. Fill in:
   - Title (required, max 200 chars)
   - Category (required)
   - Content (required, min 10 chars, max 5000)
   - Image (optional)
4. Click "Submit"
5. **Expected Result:**
   - Success message appears: "Post created successfully!"
   - Redirected to `/posts/list` after 2 seconds
   - New post appears in the list from database

#### 2. View All Posts
1. Navigate to `/posts/list`
2. Posts Auto-load from backend on page open
3. **Test Features:**
   - **Search**: Type in search box to filter by title/content/author
   - **Category Filter**: Select category from dropdown
   - **Sort**: Toggle between "Most Recent" and "Most Popular"
   - **Like Button**: Click heart icon to like/unlike post

#### 3. View My Posts
1. Click "My Posts" in navbar or navigate to `/posts/my-posts`
2. View published posts in table format
3. View draft posts below (if any)
4. Statistics show: Published posts, Total likes, Total comments, Total views
5. **Expected**: Data loads from backend `getUserPosts()` call

#### 4. Delete Post
1. In "My Posts", find a post
2. Click trash icon in Actions column
3. **Expected**: Post deleted from database and removed from list

### Error Handling

If backend is not running:
1. Error message displays: "Unable to connect to the server"
2. Fallback demo posts are shown
3. Operations fail gracefully with user-friendly messages

### Troubleshooting

**Problem: Posts not loading**
- Solution: Verify backend is running on `localhost:8090`
- Check Network tab in browser DevTools for failed requests
- Verify Post-Service is properly configured

**Problem: Images not saving**
- Current implementation sends images as base64
- Backend needs to support multipart form data for production
- Contact backend team for image upload endpoint

**Problem: User posts not appearing in "My Posts"**
- Verify `userId` is correctly set in MyPostsComponent
- Currently uses hardcoded `'current-user-id'` - update to use actual user ID from auth service
- Verify backend endpoint `/api/posts/user/:userId` is implemented

## Configuration

To change the backend URL:
1. Open `posts.service.ts`
2. Update line: `private apiUrl = 'http://localhost:8090/api/posts'`
3. Change port/host as needed

## Database Persistence

All posts are persisted to database through:
1. `PostsService.createPost()` makes HTTP POST
2. Backend receives request and saves to MySQL
3. Backend returns created post with ID
4. Frontend updates list automatically

To verify persistence:
1. Create a post through UI
2. Refresh page (F5)
3. Post should still appear (loaded from database)
4. Check MySQL database directly

## State Management

The service uses RxJS BehaviorSubject for shared state:
- `posts$` observable provides all loaded posts
- Components subscribe to get real-time updates
- `loadPosts()` refreshes cache from backend

## Next Steps

1. ✅ Create post form working with backend
2. ✅ List posts with real data from database
3. ✅ Like/Unlike functionality connected
4. ⏳ Delete functionality connected
5. ⏳ Edit post functionality (needs implementation)
6. ⏳ Comments functionality (needs backend)
7. ⏳ Image upload to server storage (needs backend)
8. ⏳ Real user authentication integration

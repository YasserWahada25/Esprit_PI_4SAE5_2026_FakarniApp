import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { PostsService } from '../services/posts.service';

@Component({
    selector: 'app-edit-post',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './edit-post.component.html',
    styleUrl: './edit-post.component.css'
})
export class EditPostComponent implements OnInit {
    postId: number = 0;
    postContent: string = '';
    imagePreview: string | null = null;
    originalImageUrl: string | null = null;
    imageFile: File | null = null;
    isSubmitting = signal(false);
    isLoading = signal(true);
    submitMessage = signal('');
    submitSuccess = signal(false);

    constructor(
        private postsService: PostsService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        // Get post ID from route
        this.route.params.subscribe(params => {
            this.postId = +params['id'];
            this.loadPost();
        });
    }

    loadPost() {
        this.isLoading.set(true);
        this.postsService.getPostById(this.postId).subscribe({
            next: (post) => {
                this.postContent = post.content;
                this.originalImageUrl = post.imageUrl || null;
                this.imagePreview = post.imageUrl || null;
                this.isLoading.set(false);
            },
            error: (error) => {
                console.error('Error loading post:', error);
                this.submitSuccess.set(false);
                this.submitMessage.set('Failed to load post. Redirecting...');
                setTimeout(() => {
                    this.router.navigate(['/posts/list']);
                }, 2000);
            }
        });
    }

    onImageSelected(event: any) {
        const file = event.target.files?.[0];
        if (file) {
            // Validate file size (max 5MB)
            const maxSize = 5 * 1024 * 1024;
            if (file.size > maxSize) {
                this.submitSuccess.set(false);
                this.submitMessage.set('Image is too large. Maximum size is 5MB.');
                event.target.value = '';
                return;
            }

            // Validate file type
            if (!file.type.startsWith('image/')) {
                this.submitSuccess.set(false);
                this.submitMessage.set('Please select a valid image file.');
                event.target.value = '';
                return;
            }

            this.imageFile = file;
            const reader = new FileReader();
            reader.onload = (e) => {
                const base64String = e.target?.result as string;
                
                // Check Base64 size
                const base64Size = base64String.length * 0.75;
                if (base64Size > 4 * 1024 * 1024) {
                    this.submitSuccess.set(false);
                    this.submitMessage.set('Image is too large after encoding. Please choose a smaller image.');
                    this.imageFile = null;
                    event.target.value = '';
                    return;
                }
                
                this.imagePreview = base64String;
                this.submitMessage.set('');
            };
            reader.onerror = () => {
                this.submitSuccess.set(false);
                this.submitMessage.set('Error reading image file.');
            };
            reader.readAsDataURL(file);
        }
    }

    removeImage() {
        this.imagePreview = null;
        this.imageFile = null;
        this.originalImageUrl = null;
        const fileInput = document.getElementById('image-input') as HTMLInputElement;
        if (fileInput) {
            fileInput.value = '';
        }
        if (this.submitMessage().includes('Image') || this.submitMessage().includes('image')) {
            this.submitMessage.set('');
        }
    }

    submitPost() {
        if (this.isSubmitting()) {
            return;
        }

        if (!this.postContent.trim() || this.postContent.trim().length < 10) {
            this.submitSuccess.set(false);
            this.submitMessage.set('Post content is required (min 10 characters)');
            return;
        }

        this.isSubmitting.set(true);
        this.submitMessage.set('');

        const request: any = {
            content: this.postContent.trim()
        };

        if (this.imagePreview) {
            request.imageUrl = this.imagePreview;
        }

        console.log('Updating post with image size:', this.imagePreview ? `${(this.imagePreview.length / 1024).toFixed(2)} KB` : 'No image');

        this.postsService.updatePost(this.postId, request).subscribe({
            next: (response) => {
                console.log('Post updated successfully:', response);
                this.isSubmitting.set(false);
                this.submitSuccess.set(true);
                this.submitMessage.set('Post updated successfully!');

                this.postsService.loadPosts();

                setTimeout(() => {
                    this.router.navigate(['/posts/list']);
                }, 2000);
            },
            error: (error: any) => {
                console.error('Full error object:', error);
                this.isSubmitting.set(false);
                this.submitSuccess.set(false);
                
                let errorMsg = 'Failed to update post';
                
                if (error?.error?.message) {
                    errorMsg = error.error.message;
                } else if (error?.message) {
                    errorMsg = error.message;
                } else if (error?.statusText) {
                    errorMsg = error.statusText;
                } else if (error?.status === 413) {
                    errorMsg = 'Image is too large. Please choose a smaller image (max 4MB).';
                } else if (error?.status === 500) {
                    errorMsg = 'Server error. The image might be too large or in an unsupported format.';
                } else if (error?.status === 0) {
                    errorMsg = 'Cannot connect to server. Please check if the backend is running.';
                } else if (error?.status) {
                    errorMsg = `Error ${error.status}: ${error.statusText || 'Unknown error'}`;
                }
                
                this.submitMessage.set(errorMsg);
            }
        });
    }

    cancelEdit() {
        if (confirm('Are you sure you want to cancel? Any unsaved changes will be lost.')) {
            this.router.navigate(['/posts/list']);
        }
    }

    getCharacterCount(): number {
        return this.postContent.length;
    }

    isFormValid(): boolean {
        return this.postContent.trim().length >= 10;
    }
}

import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PostsService } from '../services/posts.service';

@Component({
    selector: 'app-create-post',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule],
    templateUrl: './create-post.component.html',
    styleUrl: './create-post.component.css'
})
export class CreatePostComponent {
    postContent: string = '';
    imagePreview: string | null = null;
    imageFile: File | null = null;
    isSubmitting = signal(false);
    submitMessage = signal('');
    submitSuccess = signal(false);

    constructor(
        private postsService: PostsService,
        private router: Router
    ) { }

    onImageSelected(event: any) {
        const file = event.target.files?.[0];
        if (file) {
            // Validate file size (max 5MB)
            const maxSize = 5 * 1024 * 1024; // 5MB in bytes
            if (file.size > maxSize) {
                this.submitSuccess.set(false);
                this.submitMessage.set('Image is too large. Maximum size is 5MB.');
                event.target.value = ''; // Reset input
                return;
            }

            // Validate file type
            if (!file.type.startsWith('image/')) {
                this.submitSuccess.set(false);
                this.submitMessage.set('Please select a valid image file.');
                event.target.value = ''; // Reset input
                return;
            }

            this.imageFile = file;
            const reader = new FileReader();
            reader.onload = (e) => {
                const base64String = e.target?.result as string;
                
                // Check Base64 size (should be less than 4MB after encoding)
                const base64Size = base64String.length * 0.75; // Approximate size in bytes
                if (base64Size > 4 * 1024 * 1024) {
                    this.submitSuccess.set(false);
                    this.submitMessage.set('Image is too large after encoding. Please choose a smaller image.');
                    this.imageFile = null;
                    event.target.value = '';
                    return;
                }
                
                this.imagePreview = base64String;
                // Clear any previous error messages
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
        // Clear file input
        const fileInput = document.getElementById('image-input') as HTMLInputElement;
        if (fileInput) {
            fileInput.value = '';
        }
        // Clear any error messages related to image
        if (this.submitMessage().includes('Image') || this.submitMessage().includes('image')) {
            this.submitMessage.set('');
        }
    }

    submitPost() {
        // Prevent double submission
        if (this.isSubmitting()) {
            return;
        }

        if (!this.postContent.trim() || this.postContent.trim().length < 10) {
            this.submitSuccess.set(false);
            this.submitMessage.set('Post content is required (min 10 characters)');
            return;
        }

        this.isSubmitting.set(true);
        this.submitMessage.set(''); // Clear previous messages

        // Create post request compatible with backend
        const request: any = {
            content: this.postContent.trim()
        };

        // Only add imageUrl if there's an image preview
        if (this.imagePreview) {
            request.imageUrl = this.imagePreview;
        }

        console.log('Submitting post with image size:', this.imagePreview ? `${(this.imagePreview.length / 1024).toFixed(2)} KB` : 'No image');

        // Submit to backend
        this.postsService.createPost(request).subscribe({
            next: (response) => {
                console.log('Post created successfully:', response);
                this.isSubmitting.set(false);
                this.submitSuccess.set(true);
                this.submitMessage.set('Post created successfully!');

                // Reload posts list and reset form
                this.postsService.loadPosts();
                this.resetForm();

                // Redirect to posts list after 2 seconds
                setTimeout(() => {
                    this.router.navigate(['/posts/list']);
                }, 2000);
            },
            error: (error: any) => {
                console.error('Full error object:', error);
                this.isSubmitting.set(false);
                this.submitSuccess.set(false);
                
                // Try to extract error message from various sources
                let errorMsg = 'Failed to create post';
                
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
                console.log('Error message set to:', errorMsg);
            }
        });
    }

    resetForm() {
        this.postContent = '';
        this.imagePreview = null;
        this.imageFile = null;
    }

    getCharacterCount(): number {
        return this.postContent.length;
    }

    isFormValid(): boolean {
        return this.postContent.trim().length >= 10;
    }
}

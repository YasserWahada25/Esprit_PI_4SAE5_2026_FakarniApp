import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-posts-list-test',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './posts-list-test.component.html'
})
export class PostsListTestComponent {
    searchQuery = '';
    selectedCategory = 'all';

    posts = [
        {
            id: 1,
            author: 'Ahmed Ben Ali',
            title: 'Sharing my daily routine',
            content: 'Today I went for a nice walk in the park...',
            category: 'lifestyle',
            date: '2024-01-15'
        },
        {
            id: 2,
            author: 'Fatima Khoury',
            title: 'Tips for staying mentally active',
            content: 'I discovered that doing puzzles helps...',
            category: 'health',
            date: '2024-01-14'
        }
    ];

    get filteredPosts() {
        return this.posts.filter(p => {
            if (this.selectedCategory !== 'all' && p.category !== this.selectedCategory) {
                return false;
            }
            if (this.searchQuery && !p.title.toLowerCase().includes(this.searchQuery.toLowerCase())) {
                return false;
            }
            return true;
        });
    }
}

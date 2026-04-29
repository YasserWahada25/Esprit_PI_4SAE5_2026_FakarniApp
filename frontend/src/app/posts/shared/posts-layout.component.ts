import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-posts-layout',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './posts-layout.component.html',
    styleUrl: './posts-layout.component.css'
})
export class PostsLayoutComponent {
}


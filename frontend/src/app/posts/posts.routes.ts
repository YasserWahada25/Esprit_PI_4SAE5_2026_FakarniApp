import { Routes } from '@angular/router';
import { PostsLayoutComponent } from './shared/posts-layout.component';

export const POSTS_ROUTES: Routes = [
    {
        path: '',
        component: PostsLayoutComponent,
        children: [
            { path: '', redirectTo: 'list', pathMatch: 'full' },
            {
                path: 'list',
                loadComponent: () => import('./list/posts-list.component').then(m => m.PostsListComponent)
            },
            {
                path: 'create',
                loadComponent: () => import('./create/create-post.component').then(m => m.CreatePostComponent)
            },
            {
                path: 'edit/:id',
                loadComponent: () => import('./edit/edit-post.component').then(m => m.EditPostComponent)
            },
            {
                path: 'my-posts',
                loadComponent: () => import('./my-posts/my-posts.component').then(m => m.MyPostsComponent)
            }
        ]
    }
];

import { apiClient } from './api';

export interface ForumCommentDto {
  id: string;
  postId: string;
  authorId: string | null;
  authorName: string | null;
  content: string;
  isAnonymous: boolean;
  createdAt: string;
}

export interface ForumPostDto {
  id: string;
  authorId: string | null;
  authorName: string | null;
  title: string;
  content: string;
  category: string | null;
  isAnonymous: boolean;
  moderationStatus: string;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
  comments?: ForumCommentDto[];
}

export interface ForumPage {
  content: ForumPostDto[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export const forumService = {
  /** Get paginated approved posts, optionally filtered by category. */
  getPosts(category?: string, page = 0, size = 10): Promise<ForumPage> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (category) params.set('category', category);
    return apiClient.get<ForumPage>(`/forum/posts?${params}`);
  },

  /** Get a single post with its comments. */
  getPost(postId: string): Promise<ForumPostDto> {
    return apiClient.get<ForumPostDto>(`/forum/posts/${postId}`);
  },

  /** Create a new forum post. */
  createPost(data: {
    title: string;
    content: string;
    category?: string;
    anonymous?: boolean;
  }): Promise<ForumPostDto> {
    return apiClient.post<ForumPostDto>('/forum/posts', data);
  },

  /** Delete a post. */
  deletePost(postId: string): Promise<void> {
    return apiClient.delete<void>(`/forum/posts/${postId}`);
  },

  /** Add a comment to a post. */
  addComment(postId: string, content: string, anonymous = false): Promise<ForumCommentDto> {
    return apiClient.post<ForumCommentDto>(`/forum/posts/${postId}/comments`, {
      content,
      anonymous,
    });
  },

  /** Delete a comment. */
  deleteComment(commentId: string): Promise<void> {
    return apiClient.delete<void>(`/forum/comments/${commentId}`);
  },
};

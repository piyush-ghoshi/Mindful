import { useState, useEffect } from 'react';
import { Users, Plus, MessageCircle, ChevronLeft, Loader2, Trash2, Send } from 'lucide-react';
import { forumService, type ForumPostDto, type ForumCommentDto } from '../services/forumService';
import { useAuth } from '../context/AuthContext';
import { FORUM_CATEGORIES } from '../constants';

const timeAgo = (iso: string) => {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const CATEGORY_COLORS: Record<string, string> = {
  anxiety:     'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
  depression:  'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300',
  stress:      'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
  relationships:'bg-pink-100 text-pink-700 dark:bg-pink-900/30 dark:text-pink-300',
  academic:    'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300',
  general:     'bg-teal-100 text-teal-700 dark:bg-teal-900/30 dark:text-teal-300',
};

// ── Create Post Modal ─────────────────────────────────────────────────────────
const CreatePostModal = ({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: (post: ForumPostDto) => void;
}) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) {
      setError('Title and content are required.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      const post = await forumService.createPost({ title, content, category: category || undefined, anonymous });
      onCreated(post);
    } catch {
      setError('Failed to create post. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const inputCls = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
      <div className="bg-white dark:bg-slate-800 rounded-2xl p-8 w-full max-w-lg shadow-2xl border border-slate-100 dark:border-slate-700">
        <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-6">Create a Post</h2>
        {error && (
          <div className="mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-1.5">Title</label>
            <input type="text" value={title} onChange={e => setTitle(e.target.value)}
              placeholder="What's on your mind?" maxLength={255} className={inputCls} />
          </div>
          <div>
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-1.5">Content</label>
            <textarea value={content} onChange={e => setContent(e.target.value)}
              placeholder="Share your thoughts, experiences, or questions…"
              rows={5} className={`${inputCls} resize-none`} />
          </div>
          <div>
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-1.5">Category</label>
            <select value={category} onChange={e => setCategory(e.target.value)} className={inputCls}>
              <option value="">Select a category (optional)</option>
              {FORUM_CATEGORIES.map(c => (
                <option key={c.id} value={c.id}>{c.label}</option>
              ))}
            </select>
          </div>
          <label className="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" checked={anonymous} onChange={e => setAnonymous(e.target.checked)}
              className="w-4 h-4 rounded accent-teal-500" />
            <span className="text-sm text-slate-600 dark:text-slate-300">Post anonymously</span>
          </label>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="flex-1 py-3 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
              Cancel
            </button>
            <button type="submit" disabled={submitting}
              className="flex-1 py-3 rounded-xl bg-teal-500 hover:bg-teal-600 text-white text-sm font-semibold transition-colors disabled:opacity-60">
              {submitting ? 'Posting…' : 'Post'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// ── Post Detail View ──────────────────────────────────────────────────────────
const PostDetail = ({
  post,
  onBack,
  onDelete,
  currentUserId,
}: {
  post: ForumPostDto;
  onBack: () => void;
  onDelete: (postId: string) => void;
  currentUserId: string;
}) => {
  const [comments, setComments] = useState<ForumCommentDto[]>(post.comments ?? []);
  const [newComment, setNewComment] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(!post.comments);

  useEffect(() => {
    if (post.comments) return;
    forumService.getPost(post.id)
      .then(full => setComments(full.comments ?? []))
      .finally(() => setLoading(false));
  }, [post.id, post.comments]);

  const handleComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    setSubmitting(true);
    try {
      const comment = await forumService.addComment(post.id, newComment.trim(), anonymous);
      setComments(prev => [...prev, comment]);
      setNewComment('');
    } catch { /* ignore */ }
    finally { setSubmitting(false); }
  };

  const handleDeleteComment = async (commentId: string) => {
    await forumService.deleteComment(commentId);
    setComments(prev => prev.filter(c => c.id !== commentId));
  };

  const categoryColor = CATEGORY_COLORS[post.category ?? ''] ?? 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300';

  return (
    <div className="space-y-6">
      <button onClick={onBack}
        className="flex items-center gap-2 text-sm font-semibold text-slate-500 dark:text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 transition-colors">
        <ChevronLeft size={16} /> Back to Forum
      </button>

      {/* Post */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50">
        <div className="flex items-start justify-between gap-4 mb-4">
          <div className="flex-1">
            {post.category && (
              <span className={`inline-block px-2.5 py-1 rounded-full text-xs font-semibold mb-3 ${categoryColor}`}>
                {FORUM_CATEGORIES.find(c => c.id === post.category)?.label ?? post.category}
              </span>
            )}
            <h1 className="text-2xl font-bold text-slate-800 dark:text-white">{post.title}</h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
              {post.isAnonymous ? 'Anonymous' : (post.authorName ?? 'Unknown')} · {timeAgo(post.createdAt)}
            </p>
          </div>
          {post.authorId === currentUserId && (
            <button onClick={() => onDelete(post.id)}
              className="p-2 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 text-slate-400 hover:text-red-500 transition-colors">
              <Trash2 size={16} />
            </button>
          )}
        </div>
        <p className="text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">{post.content}</p>
        <div className="flex items-center gap-4 mt-6 pt-4 border-t border-slate-100 dark:border-slate-700/50 text-sm text-slate-500 dark:text-slate-400">
          <span className="flex items-center gap-1.5"><MessageCircle size={14} />{comments.length} comments</span>
        </div>
      </div>

      {/* Comments */}
      <div className="space-y-3">
        <h2 className="text-lg font-bold text-slate-800 dark:text-white">Comments</h2>
        {loading ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 size={24} className="animate-spin text-teal-500" />
          </div>
        ) : comments.length === 0 ? (
          <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50 text-center">
            <p className="text-slate-400 dark:text-slate-500 text-sm">No comments yet. Be the first to reply!</p>
          </div>
        ) : (
          comments.map(comment => (
            <div key={comment.id}
              className="bg-white dark:bg-slate-800/60 rounded-xl p-5 border border-slate-100 dark:border-slate-700/50 flex gap-3">
              <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center text-xs font-bold text-slate-600 dark:text-slate-300 flex-shrink-0">
                {comment.isAnonymous ? '?' : (comment.authorName?.[0] ?? '?')}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                    {comment.isAnonymous ? 'Anonymous' : (comment.authorName ?? 'Unknown')}
                    <span className="text-xs font-normal text-slate-400 dark:text-slate-500 ml-2">{timeAgo(comment.createdAt)}</span>
                  </p>
                  {comment.authorId === currentUserId && (
                    <button onClick={() => handleDeleteComment(comment.id)}
                      className="p-1 rounded hover:bg-red-50 dark:hover:bg-red-900/20 text-slate-400 hover:text-red-500 transition-colors">
                      <Trash2 size={13} />
                    </button>
                  )}
                </div>
                <p className="text-sm text-slate-600 dark:text-slate-400 mt-1 leading-relaxed">{comment.content}</p>
              </div>
            </div>
          ))
        )}

        {/* Add comment */}
        <form onSubmit={handleComment} className="bg-white dark:bg-slate-800/60 rounded-xl p-5 border border-slate-100 dark:border-slate-700/50">
          <textarea
            value={newComment}
            onChange={e => setNewComment(e.target.value)}
            placeholder="Write a supportive comment…"
            rows={3}
            className="w-full bg-slate-50 dark:bg-slate-700/50 border border-slate-200 dark:border-slate-600 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none transition-all mb-3"
          />
          <div className="flex items-center justify-between">
            <label className="flex items-center gap-2 cursor-pointer text-sm text-slate-600 dark:text-slate-300">
              <input type="checkbox" checked={anonymous} onChange={e => setAnonymous(e.target.checked)}
                className="w-4 h-4 rounded accent-teal-500" />
              Post anonymously
            </label>
            <button type="submit" disabled={!newComment.trim() || submitting}
              className="flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-semibold transition-colors disabled:opacity-50">
              {submitting ? <Loader2 size={14} className="animate-spin" /> : <Send size={14} />}
              Reply
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// ── Main Forum Page ───────────────────────────────────────────────────────────
const ForumPage = () => {
  const { user } = useAuth();
  const [posts, setPosts] = useState<ForumPostDto[]>([]);
  const [activeCategory, setActiveCategory] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [selectedPost, setSelectedPost] = useState<ForumPostDto | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const loadPosts = (cat: string, pg: number) => {
    setLoading(true);
    forumService.getPosts(cat || undefined, pg)
      .then(res => {
        setPosts(res.content);
        setTotalPages(res.totalPages);
      })
      .catch(() => setError('Could not load posts.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPosts(activeCategory, page); }, [activeCategory, page]);

  const handleCategoryChange = (cat: string) => {
    setActiveCategory(cat);
    setPage(0);
  };

  const handlePostCreated = (post: ForumPostDto) => {
    setPosts(prev => [post, ...prev]);
    setShowCreate(false);
  };

  const handleDeletePost = async (postId: string) => {
    await forumService.deletePost(postId);
    setPosts(prev => prev.filter(p => p.id !== postId));
    setSelectedPost(null);
  };

  if (selectedPost) {
    return (
      <PostDetail
        post={selectedPost}
        onBack={() => setSelectedPost(null)}
        onDelete={handleDeletePost}
        currentUserId={user?.id ?? ''}
      />
    );
  }

  return (
    <div className="space-y-6">
      {showCreate && (
        <CreatePostModal
          onClose={() => setShowCreate(false)}
          onCreated={handlePostCreated}
        />
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
            <Users size={28} className="text-violet-500" /> Community Forum
          </h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">
            A safe space to share, support, and connect
          </p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 px-5 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl font-semibold text-sm transition-colors shadow-sm shadow-teal-500/20">
          <Plus size={18} /> New Post
        </button>
      </div>

      {/* Category filters */}
      <div className="flex gap-2 flex-wrap">
        <button onClick={() => handleCategoryChange('')}
          className={`px-4 py-2 rounded-full text-sm font-semibold transition-all ${
            activeCategory === ''
              ? 'bg-teal-500 text-white shadow-sm'
              : 'bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:border-teal-400'
          }`}>All</button>
        {FORUM_CATEGORIES.map(cat => (
          <button key={cat.id} onClick={() => handleCategoryChange(cat.id)}
            className={`px-4 py-2 rounded-full text-sm font-semibold transition-all ${
              activeCategory === cat.id
                ? 'bg-teal-500 text-white shadow-sm'
                : 'bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:border-teal-400'
            }`}>{cat.label}</button>
        ))}
      </div>

      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-xl p-4 text-red-700 dark:text-red-300 text-sm">
          {error}
        </div>
      )}

      {/* Posts */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 size={32} className="animate-spin text-teal-500" />
        </div>
      ) : posts.length === 0 ? (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-16 border border-slate-100 dark:border-slate-700/50 text-center">
          <Users size={48} className="text-slate-300 dark:text-slate-600 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 dark:text-slate-300 mb-2">No posts yet</h3>
          <p className="text-slate-400 dark:text-slate-500 text-sm mb-6">
            Be the first to share something with the community.
          </p>
          <button onClick={() => setShowCreate(true)}
            className="inline-flex items-center gap-2 px-6 py-3 bg-teal-500 hover:bg-teal-600 text-white rounded-xl font-semibold text-sm transition-colors">
            <Plus size={16} /> Create First Post
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {posts.map(post => {
            const categoryColor = CATEGORY_COLORS[post.category ?? ''] ?? 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300';
            return (
              <button key={post.id} onClick={() => setSelectedPost(post)}
                className="w-full text-left bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 hover:shadow-md hover:shadow-black/5 dark:hover:shadow-black/20 hover:border-teal-300/50 dark:hover:border-teal-600/50 transition-all">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-2 flex-wrap">
                      {post.category && (
                        <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${categoryColor}`}>
                          {FORUM_CATEGORIES.find(c => c.id === post.category)?.label ?? post.category}
                        </span>
                      )}
                      {post.isAnonymous && (
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-500 dark:bg-slate-700 dark:text-slate-400">
                          Anonymous
                        </span>
                      )}
                    </div>
                    <h3 className="text-base font-bold text-slate-800 dark:text-slate-100 mb-1">{post.title}</h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 line-clamp-2 leading-relaxed">{post.content}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4 mt-4 text-xs text-slate-400 dark:text-slate-500">
                  <span>{post.isAnonymous ? 'Anonymous' : (post.authorName ?? 'Unknown')}</span>
                  <span>·</span>
                  <span>{timeAgo(post.createdAt)}</span>
                  <span>·</span>
                  <span className="flex items-center gap-1"><MessageCircle size={12} />{post.commentCount}</span>
                </div>
              </button>
            );
          })}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-3">
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
            className="px-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40 transition-colors">
            Previous
          </button>
          <span className="text-sm text-slate-500 dark:text-slate-400">
            Page {page + 1} of {totalPages}
          </span>
          <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
            className="px-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40 transition-colors">
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default ForumPage;

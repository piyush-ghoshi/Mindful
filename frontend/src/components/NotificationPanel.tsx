import { useState, useEffect, useRef, useCallback } from 'react';
import { Bell, X, CheckCheck, CalendarCheck, Heart, MessageSquare, Trophy, AlertTriangle, Loader2 } from 'lucide-react';
import { apiClient } from '../services/api';
import { useTheme } from '../context/ThemeContext';

interface Notification {
  id: string;
  title: string;
  message: string;
  notificationType: string;
  status: string; // PENDING, SENT, READ
  createdAt: string;
  readAt?: string;
}

const TYPE_ICON: Record<string, { icon: React.ElementType; color: string; bg: string }> = {
  APPOINTMENT: { icon: CalendarCheck, color: 'text-violet-500', bg: 'bg-violet-100 dark:bg-violet-900/40' },
  MESSAGE:     { icon: MessageSquare, color: 'text-sky-500',    bg: 'bg-sky-100 dark:bg-sky-900/40'    },
  ACHIEVEMENT: { icon: Trophy,        color: 'text-amber-500',  bg: 'bg-amber-100 dark:bg-amber-900/40' },
  ALERT:       { icon: AlertTriangle, color: 'text-rose-500',   bg: 'bg-rose-100 dark:bg-rose-900/40'  },
  WELLNESS:    { icon: Heart,         color: 'text-teal-500',   bg: 'bg-teal-100 dark:bg-teal-900/40'  },
  DEFAULT:     { icon: Bell,          color: 'text-slate-500',  bg: 'bg-slate-100 dark:bg-slate-700'   },
};

function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

const NotificationPanel = () => {
  const { theme } = useTheme();
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);
  const panelRef = useRef<HTMLDivElement>(null);

  const unreadCount = notifications.filter(n => n.status !== 'READ').length;

  // Close on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (panelRef.current && !panelRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const data = await apiClient.get<Notification[] | { content?: Notification[]; data?: Notification[] }>('/notifications');
      const list = Array.isArray(data)
        ? data
        : ((data as { content?: Notification[]; data?: Notification[] }).content
            ?? (data as { content?: Notification[]; data?: Notification[] }).data
            ?? []);
      setNotifications(list.slice(0, 20)); // show latest 20
    } catch {
      // API may return 404 if no notifications — show empty state
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch when opened
  useEffect(() => {
    if (open) fetchNotifications();
  }, [open, fetchNotifications]);

  // Poll unread count every 60s
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const count = await apiClient.get<number>('/notifications/unread/count');
        if (typeof count === 'number' && count > unreadCount) {
          fetchNotifications();
        }
      } catch { /* ignore */ }
    }, 60000);
    return () => clearInterval(interval);
  }, [unreadCount, fetchNotifications]);

  const markAsRead = async (id: string) => {
    try {
      await apiClient.put(`/notifications/${id}/read`);
      setNotifications(prev =>
        prev.map(n => n.id === id ? { ...n, status: 'READ', readAt: new Date().toISOString() } : n)
      );
    } catch { /* ignore */ }
  };

  const markAllAsRead = async () => {
    setMarkingAll(true);
    try {
      const unread = notifications.filter(n => n.status !== 'READ');
      await Promise.all(unread.map(n => apiClient.put(`/notifications/${n.id}/read`)));
      setNotifications(prev => prev.map(n => ({ ...n, status: 'READ', readAt: new Date().toISOString() })));
    } catch { /* ignore */ }
    finally { setMarkingAll(false); }
  };

  const getIconConfig = (type: string) =>
    TYPE_ICON[type?.toUpperCase()] ?? TYPE_ICON.DEFAULT;

  return (
    <div className="relative" ref={panelRef}>
      {/* Bell button */}
      <button
        onClick={() => setOpen(!open)}
        className="relative w-9 h-9 rounded-xl flex items-center justify-center hover:bg-white/70 dark:hover:bg-slate-800 transition-all"
        aria-label="Notifications"
      >
        <Bell size={18} className={`transition-colors ${open ? 'text-teal-600 dark:text-teal-400' : 'text-slate-500 dark:text-slate-400'}`} />
        {/* Unread badge */}
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] rounded-full bg-rose-500 border-2 border-[#f0faf8] dark:border-[#0b1221] flex items-center justify-center">
            <span className="text-[10px] font-black text-white leading-none px-0.5">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          </span>
        )}
      </button>

      {/* Panel */}
      {open && (
        <div
          className="absolute right-0 top-full mt-2 w-80 rounded-2xl border border-slate-100 dark:border-slate-700/60 overflow-hidden z-50 flex flex-col"
          style={{
            background: theme === 'dark' ? 'rgba(15,23,42,0.97)' : 'rgba(255,255,255,0.98)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
            maxHeight: '480px',
          }}
        >
          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100 dark:border-slate-700/50 flex-shrink-0">
            <div className="flex items-center gap-2">
              <Bell size={16} className="text-teal-500" />
              <span className="font-bold text-slate-800 dark:text-white text-sm">Notifications</span>
              {unreadCount > 0 && (
                <span className="px-1.5 py-0.5 bg-rose-100 dark:bg-rose-900/40 text-rose-600 dark:text-rose-400 text-xs font-bold rounded-full">
                  {unreadCount} new
                </span>
              )}
            </div>
            <div className="flex items-center gap-1">
              {unreadCount > 0 && (
                <button
                  onClick={markAllAsRead}
                  disabled={markingAll}
                  className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold text-teal-600 dark:text-teal-400 hover:bg-teal-50 dark:hover:bg-teal-900/20 transition-colors disabled:opacity-50"
                  title="Mark all as read"
                >
                  {markingAll ? <Loader2 size={12} className="animate-spin" /> : <CheckCheck size={12} />}
                  All read
                </button>
              )}
              <button
                onClick={() => setOpen(false)}
                className="p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
              >
                <X size={14} className="text-slate-400" />
              </button>
            </div>
          </div>

          {/* Body */}
          <div className="overflow-y-auto flex-1">
            {loading ? (
              <div className="flex items-center justify-center py-10">
                <Loader2 size={24} className="animate-spin text-teal-500" />
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-10 px-4 text-center">
                <div className="w-14 h-14 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-3">
                  <Bell size={24} className="text-slate-400 dark:text-slate-500" />
                </div>
                <p className="font-semibold text-slate-600 dark:text-slate-400 text-sm">All caught up!</p>
                <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">No notifications yet.</p>
              </div>
            ) : (
              <div className="divide-y divide-slate-50 dark:divide-slate-800/60">
                {notifications.map(n => {
                  const { icon: Icon, color, bg } = getIconConfig(n.notificationType ?? 'DEFAULT');
                  const isUnread = n.status !== 'READ';
                  return (
                    <div
                      key={n.id}
                      onClick={() => isUnread && markAsRead(n.id)}
                      className={`flex gap-3 px-4 py-3 cursor-pointer transition-colors ${
                        isUnread
                          ? 'bg-teal-50/60 dark:bg-teal-900/10 hover:bg-teal-50 dark:hover:bg-teal-900/20'
                          : 'hover:bg-slate-50 dark:hover:bg-slate-800/40'
                      }`}
                    >
                      {/* Icon */}
                      <div className={`w-9 h-9 rounded-xl ${bg} flex items-center justify-center flex-shrink-0 mt-0.5`}>
                        <Icon size={16} className={color} />
                      </div>

                      {/* Content */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2">
                          <p className={`text-sm leading-tight ${isUnread ? 'font-bold text-slate-800 dark:text-white' : 'font-medium text-slate-700 dark:text-slate-300'}`}>
                            {n.title}
                          </p>
                          {isUnread && (
                            <span className="w-2 h-2 rounded-full bg-teal-500 flex-shrink-0 mt-1.5" />
                          )}
                        </div>
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 leading-relaxed line-clamp-2">
                          {n.message}
                        </p>
                        <p className="text-[10px] text-slate-400 dark:text-slate-500 mt-1 font-medium">
                          {timeAgo(n.createdAt)}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Footer */}
          {notifications.length > 0 && (
            <div className="px-4 py-2.5 border-t border-slate-100 dark:border-slate-700/50 flex-shrink-0 text-center">
              <button
                onClick={() => { setOpen(false); fetchNotifications(); }}
                className="text-xs font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-700 dark:hover:text-teal-300 transition-colors"
              >
                Refresh notifications
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default NotificationPanel;

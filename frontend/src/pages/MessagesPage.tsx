import { useState, useEffect, useRef, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  MessageSquare, Send, ArrowLeft, Loader2, Search,
  Trash2, Smile, Check, CheckCheck, MoreVertical, X,
  Phone, Video, Circle,
} from 'lucide-react';
import { messagingService, type ConversationDto, type MessageDto } from '../services/messagingService';
import { apiClient } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useAuthReady } from '../hooks/useAuthReady';
import { appointmentService } from '../services/appointmentService';

// ── Helpers ──────────────────────────────────────────────────────────────────
const timeAgo = (iso: string) => {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  if (hrs < 48) return 'Yesterday';
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const formatTime = (iso: string) =>
  new Date(iso).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

const getDateGroup = (iso: string) => {
  const d = new Date(iso);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) return 'Today';
  const yesterday = new Date(now); yesterday.setDate(now.getDate() - 1);
  if (d.toDateString() === yesterday.toDateString()) return 'Yesterday';
  return d.toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' });
};

const initials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();

const QUICK_EMOJIS = ['😊', '👍', '❤️', '😂', '🙏'];

// ── Avatar ────────────────────────────────────────────────────────────────────
const Avatar = ({ name, size = 'md', online = false }: { name: string; size?: 'sm' | 'md' | 'lg'; online?: boolean }) => {
  const sz = size === 'sm' ? 'w-8 h-8 text-xs' : size === 'lg' ? 'w-12 h-12 text-base' : 'w-10 h-10 text-sm';
  const dot = size === 'sm' ? 'w-2 h-2' : 'w-2.5 h-2.5';
  return (
    <div className="relative flex-shrink-0">
      <div className={`${sz} rounded-full bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center font-bold text-white shadow-sm`}>
        {initials(name)}
      </div>
      {online && (
        <span className={`${dot} bg-green-400 border-2 border-white dark:border-slate-800 rounded-full absolute bottom-0 right-0`} />
      )}
    </div>
  );
};

// ── MessagesPage ──────────────────────────────────────────────────────────────
const MessagesPage = () => {
  const { user } = useAuth();
  const authReady = useAuthReady();
  const [searchParams] = useSearchParams();
  const targetUserId = searchParams.get('userId');

  const [conversations, setConversations] = useState<ConversationDto[]>([]);
  const [activeConv, setActiveConv] = useState<ConversationDto | null>(null);
  const [messages, setMessages] = useState<MessageDto[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loadingConvs, setLoadingConvs] = useState(true);
  const [loadingMsgs, setLoadingMsgs] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [showEmoji, setShowEmoji] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Auto-scroll
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  // Load conversations + auto-inject booked counsellor
  const loadConversations = useCallback(async () => {
    if (!authReady) return;
    setLoadingConvs(true);
    try {
      const [convs, appointments] = await Promise.all([
        messagingService.getConversations(),
        appointmentService.getAppointments().catch(() => []),
      ]);

      let merged = [...convs];

      // For each confirmed/scheduled appointment, inject counsellor into conv list if not already there
      const existingIds = new Set(convs.map(c => c.otherUserId));
      for (const appt of appointments) {
        const counsellorId = (appt as any).counsellorId;
        const counsellorName = (appt as any).counsellorName ?? (appt as any).counsellor?.fullName ?? 'Your Counsellor';
        if (counsellorId && !existingIds.has(counsellorId) && appt.status !== 'CANCELLED') {
          merged = [
            {
              id: `booked-${counsellorId}`,
              otherUserId: counsellorId,
              otherUserName: counsellorName,
              otherUserRole: 'COUNSELLOR',
              lastMessagePreview: `📅 Appointment: ${new Date(appt.scheduledStartTime).toLocaleDateString()}`,
              lastMessageAt: appt.scheduledStartTime,
              unreadCount: 0,
            },
            ...merged,
          ];
          existingIds.add(counsellorId);
        }
      }

      setConversations(merged);

      // Auto-select from URL param
      if (targetUserId) {
        const existing = merged.find(c => c.otherUserId === targetUserId);
        if (existing) {
          setActiveConv(existing);
        } else {
          try {
            const otherUser = await apiClient.get<any>(`/users/${targetUserId}`);
            const tempConv: ConversationDto = {
              id: 'temp-' + targetUserId,
              otherUserId: targetUserId,
              otherUserName: `${otherUser.firstName} ${otherUser.lastName}`,
              otherUserRole: otherUser.role,
              lastMessagePreview: null,
              lastMessageAt: null,
              unreadCount: 0,
            };
            setConversations(prev => [tempConv, ...prev.filter(c => c.otherUserId !== targetUserId)]);
            setActiveConv(tempConv);
          } catch {
            setError('Could not start conversation with requested user.');
          }
        }
      }
    } catch {
      setError('Could not load conversations.');
    } finally {
      setLoadingConvs(false);
    }
  }, [authReady, targetUserId]);

  useEffect(() => { loadConversations(); }, [loadConversations]);

  // Load messages when active conversation changes
  useEffect(() => {
    if (!activeConv) return;
    if (activeConv.id.startsWith('temp-') || activeConv.id.startsWith('booked-')) {
      setMessages([]);
      return;
    }
    setLoadingMsgs(true);
    messagingService.getMessages(activeConv.id)
      .then(page => setMessages([...page.content].reverse()))
      .catch(() => setError('Could not load messages.'))
      .finally(() => setLoadingMsgs(false));
  }, [activeConv]);

  // Send message
  const handleSend = async () => {
    if (!newMessage.trim() || !activeConv || sending) return;
    const content = newMessage.trim();
    setNewMessage('');
    setSending(true);
    if (textareaRef.current) textareaRef.current.style.height = 'auto';
    try {
      const msg = await messagingService.sendMessage(activeConv.otherUserId, content);
      setMessages(prev => [...prev, msg]);
      const realConvId = msg.conversationId;
      setConversations(prev => prev.map(c =>
        c.otherUserId === activeConv.otherUserId
          ? { ...c, id: realConvId, lastMessagePreview: content, lastMessageAt: msg.sentAt }
          : c
      ));
      setActiveConv(prev => prev ? { ...prev, id: realConvId, lastMessagePreview: content, lastMessageAt: msg.sentAt } : null);
    } catch {
      setError('Failed to send message.');
      setNewMessage(content);
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); }
  };

  const handleTextareaChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setNewMessage(e.target.value);
    e.target.style.height = 'auto';
    e.target.style.height = Math.min(e.target.scrollHeight, 120) + 'px';
  };

  // Delete conversation (optimistic)
  const handleDeleteConversation = async (convId: string) => {
    setDeletingId(convId);
    setShowDeleteConfirm(null);
    try {
      if (!convId.startsWith('temp-') && !convId.startsWith('booked-')) {
        await messagingService.deleteConversation(convId).catch(() => {});
      }
    } finally {
      setConversations(prev => prev.filter(c => c.id !== convId));
      if (activeConv?.id === convId) setActiveConv(null);
      setDeletingId(null);
    }
  };

  const addEmoji = (emoji: string) => {
    setNewMessage(prev => prev + emoji);
    setShowEmoji(false);
    textareaRef.current?.focus();
  };

  // Date grouping for messages
  const groupedMessages = () => {
    const groups: { date: string; msgs: MessageDto[] }[] = [];
    for (const msg of messages) {
      const g = getDateGroup(msg.sentAt);
      const last = groups[groups.length - 1];
      if (last && last.date === g) last.msgs.push(msg);
      else groups.push({ date: g, msgs: [msg] });
    }
    return groups;
  };

  const filtered = conversations.filter(c =>
    c.otherUserName.toLowerCase().includes(search.toLowerCase())
  );

  const isBooked = (conv: ConversationDto) => conv.id.startsWith('booked-');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-sky-400 to-blue-500 flex items-center justify-center shadow-lg shadow-sky-500/30">
            <MessageSquare size={20} className="text-white" />
          </div>
          Messages
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1 ml-1">
          Secure messaging with your counsellor
        </p>
      </div>

      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-xl p-4 text-red-700 dark:text-red-300 text-sm flex items-center justify-between">
          {error}
          <button onClick={() => setError('')}><X size={14} /></button>
        </div>
      )}

      {/* Main layout */}
      <div
        className="bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl rounded-3xl border border-white/50 dark:border-slate-700/50 shadow-2xl shadow-slate-200/50 dark:shadow-black/30 overflow-hidden"
        style={{ height: '75vh', display: 'flex' }}
      >
        {/* ── Sidebar ── */}
        <div className={`w-full md:w-80 border-r border-slate-100/80 dark:border-slate-700/50 flex flex-col flex-shrink-0 bg-slate-50/50 dark:bg-slate-800/30 ${activeConv ? 'hidden md:flex' : 'flex'}`}>
          {/* Sidebar header */}
          <div className="px-4 py-4 border-b border-slate-100/80 dark:border-slate-700/50">
            <p className="text-sm font-bold text-slate-700 dark:text-slate-200 mb-3">Conversations</p>
            {/* Search */}
            <div className="relative">
              <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                value={search}
                onChange={e => setSearch(e.target.value)}
                placeholder="Search…"
                className="w-full pl-8 pr-3 py-2 text-xs bg-white dark:bg-slate-700/60 border border-slate-200 dark:border-slate-600 rounded-xl text-slate-700 dark:text-slate-200 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-400/50 transition-all"
              />
            </div>
          </div>

          {/* Conversation list */}
          {loadingConvs ? (
            <div className="flex-1 flex items-center justify-center">
              <Loader2 size={24} className="animate-spin text-sky-500" />
            </div>
          ) : filtered.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-sky-100 to-blue-100 dark:from-sky-900/30 dark:to-blue-900/30 flex items-center justify-center mb-4">
                <MessageSquare size={28} className="text-sky-400" />
              </div>
              <p className="text-sm font-semibold text-slate-600 dark:text-slate-300">No conversations yet</p>
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-1.5 leading-relaxed">
                Book an appointment to start messaging your counsellor.
              </p>
            </div>
          ) : (
            <div className="flex-1 overflow-y-auto">
              {filtered.map(conv => (
                <div
                  key={conv.id}
                  className={`group relative border-b border-slate-100/60 dark:border-slate-700/30 transition-all cursor-pointer ${
                    activeConv?.id === conv.id
                      ? 'bg-gradient-to-r from-sky-50 to-blue-50/50 dark:from-sky-900/20 dark:to-blue-900/10 border-l-2 border-l-sky-400'
                      : 'hover:bg-white/80 dark:hover:bg-slate-700/30'
                  }`}
                  onClick={() => setActiveConv(conv)}
                >
                  <div className="px-4 py-3.5 flex items-center gap-3">
                    <Avatar name={conv.otherUserName} online={!isBooked(conv)} />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <p className="text-sm font-semibold text-slate-800 dark:text-slate-200 truncate">
                            {conv.otherUserName}
                          </p>
                          {isBooked(conv) && (
                            <span className="text-[9px] px-1.5 py-0.5 bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-300 rounded-full font-bold flex-shrink-0">
                              BOOKED
                            </span>
                          )}
                        </div>
                        {conv.lastMessageAt && (
                          <span className="text-[10px] text-slate-400 dark:text-slate-500 flex-shrink-0 ml-2">
                            {timeAgo(conv.lastMessageAt)}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-500 dark:text-slate-400 truncate mt-0.5">
                        {conv.lastMessagePreview ?? 'Tap to start a conversation'}
                      </p>
                    </div>
                    {conv.unreadCount > 0 && (
                      <span className="w-5 h-5 rounded-full bg-sky-500 text-white text-[10px] font-bold flex items-center justify-center flex-shrink-0">
                        {conv.unreadCount}
                      </span>
                    )}
                  </div>

                  {/* Delete button (hover) */}
                  <button
                    onClick={e => { e.stopPropagation(); setShowDeleteConfirm(conv.id); }}
                    className="absolute right-3 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 w-7 h-7 rounded-lg bg-red-50 dark:bg-red-900/30 text-red-400 hover:text-red-600 hover:bg-red-100 dark:hover:bg-red-900/50 flex items-center justify-center transition-all"
                    title="Delete conversation"
                  >
                    {deletingId === conv.id ? <Loader2 size={12} className="animate-spin" /> : <Trash2 size={12} />}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ── Chat pane ── */}
        <div className={`flex-1 flex flex-col ${!activeConv ? 'hidden md:flex' : 'flex'}`}>
          {!activeConv ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-8">
              <div className="w-24 h-24 rounded-3xl bg-gradient-to-br from-sky-100 to-blue-100 dark:from-sky-900/30 dark:to-blue-900/20 flex items-center justify-center mb-5 shadow-inner">
                <MessageSquare size={40} className="text-sky-400" />
              </div>
              <p className="text-slate-600 dark:text-slate-300 font-semibold text-lg">Select a conversation</p>
              <p className="text-slate-400 dark:text-slate-500 text-sm mt-2">
                Choose from the left to start chatting
              </p>
            </div>
          ) : (
            <>
              {/* Thread header */}
              <div className="px-5 py-3.5 border-b border-slate-100/80 dark:border-slate-700/50 flex items-center gap-3 bg-white/60 dark:bg-slate-800/40 backdrop-blur-sm">
                <button
                  onClick={() => setActiveConv(null)}
                  className="md:hidden p-1.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 transition-colors"
                >
                  <ArrowLeft size={18} />
                </button>
                <Avatar name={activeConv.otherUserName} size="md" online={!isBooked(activeConv)} />
                <div className="flex-1">
                  <p className="text-sm font-bold text-slate-800 dark:text-slate-200">{activeConv.otherUserName}</p>
                  <p className="text-xs text-slate-400 dark:text-slate-500 flex items-center gap-1.5">
                    {isBooked(activeConv) ? (
                      <>📅 Appointment booked</>
                    ) : (
                      <><Circle size={6} className="fill-green-400 text-green-400" /> Online</>
                    )}
                  </p>
                </div>
                <div className="flex items-center gap-1">
                  <button className="p-2 rounded-xl text-slate-400 hover:text-sky-500 hover:bg-sky-50 dark:hover:bg-sky-900/20 transition-all" title="Call">
                    <Phone size={16} />
                  </button>
                  <button className="p-2 rounded-xl text-slate-400 hover:text-sky-500 hover:bg-sky-50 dark:hover:bg-sky-900/20 transition-all" title="Video call">
                    <Video size={16} />
                  </button>
                  <button className="p-2 rounded-xl text-slate-400 hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all" title="More options">
                    <MoreVertical size={16} />
                  </button>
                </div>
              </div>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto px-5 py-4 space-y-1" style={{ backgroundImage: 'radial-gradient(circle at 1px 1px, rgba(148,163,184,0.05) 1px, transparent 0)', backgroundSize: '24px 24px' }}>
                {loadingMsgs ? (
                  <div className="flex items-center justify-center h-full">
                    <Loader2 size={24} className="animate-spin text-sky-500" />
                  </div>
                ) : messages.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-full text-center">
                    <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-sky-100 to-blue-100 dark:from-sky-900/30 dark:to-blue-900/20 flex items-center justify-center mb-4">
                      <MessageSquare size={24} className="text-sky-400" />
                    </div>
                    <p className="text-sm font-semibold text-slate-500 dark:text-slate-400">No messages yet</p>
                    <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">Say hello! 👋</p>
                  </div>
                ) : (
                  groupedMessages().map(({ date, msgs }) => (
                    <div key={date}>
                      {/* Date separator */}
                      <div className="flex items-center gap-3 my-4">
                        <div className="flex-1 h-px bg-slate-100 dark:bg-slate-700/50" />
                        <span className="text-[10px] font-semibold text-slate-400 dark:text-slate-500 px-2 py-1 bg-slate-50 dark:bg-slate-800/60 rounded-full">
                          {date}
                        </span>
                        <div className="flex-1 h-px bg-slate-100 dark:bg-slate-700/50" />
                      </div>
                      {msgs.map((msg, i) => {
                        const isMine = msg.senderId === user?.id;
                        const showAvatar = !isMine && (i === 0 || msgs[i - 1]?.senderId !== msg.senderId);
                        return (
                          <div key={msg.id} className={`flex ${isMine ? 'justify-end' : 'justify-start'} mb-1.5`}>
                            {!isMine && (
                              <div className="w-7 flex-shrink-0 mr-2 mt-auto">
                                {showAvatar && <Avatar name={activeConv.otherUserName} size="sm" />}
                              </div>
                            )}
                            <div className={`group/msg max-w-[72%] ${isMine ? 'items-end' : 'items-start'} flex flex-col`}>
                              <div className={`rounded-2xl px-4 py-2.5 text-sm leading-relaxed shadow-sm ${
                                isMine
                                  ? 'bg-gradient-to-br from-sky-500 to-blue-500 text-white rounded-br-sm'
                                  : 'bg-white dark:bg-slate-700/80 text-slate-800 dark:text-slate-200 border border-slate-100/80 dark:border-slate-600/50 rounded-bl-sm'
                              }`}>
                                {msg.content}
                              </div>
                              <div className={`flex items-center gap-1.5 mt-1 px-1 ${isMine ? 'flex-row-reverse' : ''}`}>
                                <span className="text-[10px] text-slate-400 dark:text-slate-500">
                                  {formatTime(msg.sentAt)}
                                </span>
                                {isMine && (
                                  msg.isRead
                                    ? <CheckCheck size={12} className="text-sky-400" />
                                    : <Check size={12} className="text-slate-400" />
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ))
                )}
                <div ref={bottomRef} />
              </div>

              {/* Input area */}
              <div className="px-4 py-3 border-t border-slate-100/80 dark:border-slate-700/50 bg-white/60 dark:bg-slate-800/40 backdrop-blur-sm">
                {/* Quick emojis */}
                {showEmoji && (
                  <div className="flex items-center gap-2 mb-2 p-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-100 dark:border-slate-700 shadow-lg">
                    {QUICK_EMOJIS.map(e => (
                      <button
                        key={e}
                        onClick={() => addEmoji(e)}
                        className="text-xl hover:scale-125 transition-transform"
                      >
                        {e}
                      </button>
                    ))}
                    <button onClick={() => setShowEmoji(false)} className="ml-auto text-slate-400 hover:text-slate-600">
                      <X size={14} />
                    </button>
                  </div>
                )}

                <div className="flex items-end gap-2">
                  {/* Emoji button */}
                  <button
                    onClick={() => setShowEmoji(v => !v)}
                    className={`flex-shrink-0 w-9 h-9 rounded-xl flex items-center justify-center transition-all ${
                      showEmoji ? 'bg-sky-100 dark:bg-sky-900/30 text-sky-500' : 'text-slate-400 hover:text-sky-500 hover:bg-sky-50 dark:hover:bg-sky-900/20'
                    }`}
                  >
                    <Smile size={18} />
                  </button>

                  {/* Text input */}
                  <div className="flex-1 relative">
                    <textarea
                      ref={textareaRef}
                      value={newMessage}
                      onChange={handleTextareaChange}
                      onKeyDown={handleKeyDown}
                      placeholder="Type a message…"
                      rows={1}
                      className="w-full bg-slate-50 dark:bg-slate-700/60 border border-slate-200 dark:border-slate-600 rounded-2xl px-4 py-2.5 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-400/50 focus:border-sky-300 resize-none transition-all"
                      style={{ maxHeight: '120px' }}
                    />
                    {newMessage.length > 200 && (
                      <span className="absolute right-3 bottom-2 text-[10px] text-slate-400">
                        {newMessage.length}/500
                      </span>
                    )}
                  </div>

                  {/* Send button */}
                  <button
                    onClick={handleSend}
                    disabled={!newMessage.trim() || sending}
                    className="flex-shrink-0 w-10 h-10 rounded-2xl bg-gradient-to-br from-sky-500 to-blue-500 hover:from-sky-400 hover:to-blue-400 text-white flex items-center justify-center shadow-md shadow-sky-500/30 transition-all disabled:opacity-40 disabled:cursor-not-allowed hover:scale-105 active:scale-95"
                  >
                    {sending ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Delete confirmation modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm" onClick={() => setShowDeleteConfirm(null)}>
          <div className="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-2xl max-w-sm w-full mx-4 border border-slate-100 dark:border-slate-700" onClick={e => e.stopPropagation()}>
            <div className="w-12 h-12 rounded-2xl bg-red-100 dark:bg-red-900/30 flex items-center justify-center mb-4 mx-auto">
              <Trash2 size={22} className="text-red-500" />
            </div>
            <h3 className="text-lg font-bold text-slate-800 dark:text-white text-center mb-2">Delete Conversation?</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 text-center mb-6">
              This will remove the conversation from your list. This action cannot be undone.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(null)}
                className="flex-1 py-2.5 rounded-xl border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-300 text-sm font-semibold hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeleteConversation(showDeleteConfirm)}
                className="flex-1 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white text-sm font-semibold transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MessagesPage;

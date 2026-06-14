import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { MessageSquare, Send, ArrowLeft, Loader2, User } from 'lucide-react';
import { messagingService, type ConversationDto, type MessageDto } from '../services/messagingService';
import { apiClient } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useAuthReady } from '../hooks/useAuthReady';

const timeAgo = (iso: string) => {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

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
  const bottomRef = useRef<HTMLDivElement>(null);

  // Load conversations on mount
  useEffect(() => {
    if (!authReady) return;
    setLoadingConvs(true);
    messagingService.getConversations()
      .then(async (convs) => {
        setConversations(convs);
        
        // If there's a target user to message
        if (targetUserId) {
          const existing = convs.find(c => c.otherUserId === targetUserId);
          if (existing) {
            setActiveConv(existing);
          } else {
            // Fetch target user's basic info to construct a temp conversation
            try {
              const otherUser = await apiClient.get<any>(`/users/${targetUserId}`);
              const tempConv: ConversationDto = {
                id: 'temp-' + targetUserId,
                otherUserId: targetUserId,
                otherUserName: `${otherUser.firstName} ${otherUser.lastName}`,
                otherUserRole: otherUser.role,
                lastMessagePreview: null,
                lastMessageAt: null,
                unreadCount: 0
              };
              setConversations(prev => [tempConv, ...prev.filter(c => c.otherUserId !== targetUserId)]);
              setActiveConv(tempConv);
            } catch (err) {
              console.error("Failed to load user for conversation bootstrap:", err);
              setError("Could not start conversation with requested user.");
            }
          }
        }
      })
      .catch(() => setError('Could not load conversations.'))
      .finally(() => setLoadingConvs(false));
  }, [authReady, targetUserId]);

  // Load messages when active conversation changes
  useEffect(() => {
    if (!activeConv) return;
    if (activeConv.id.startsWith('temp-')) {
      setMessages([]);
      return;
    }
    setLoadingMsgs(true);
    messagingService.getMessages(activeConv.id)
      .then(page => setMessages([...page.content].reverse())) // API returns newest-first, reverse for display
      .catch(() => setError('Could not load messages.'))
      .finally(() => setLoadingMsgs(false));
  }, [activeConv]);

  // Scroll to bottom when messages change
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async () => {
    if (!newMessage.trim() || !activeConv || sending) return;
    const content = newMessage.trim();
    setNewMessage('');
    setSending(true);
    try {
      const msg = await messagingService.sendMessage(activeConv.otherUserId, content);
      setMessages(prev => [...prev, msg]);
      
      const realConvId = msg.conversationId;
      // Update conversation preview and replace temp ID with real ID
      setConversations(prev => prev.map(c =>
        c.otherUserId === activeConv.otherUserId
          ? { ...c, id: realConvId, lastMessagePreview: content, lastMessageAt: msg.sentAt }
          : c
      ));
      setActiveConv(prev => prev ? { ...prev, id: realConvId, lastMessagePreview: content, lastMessageAt: msg.sentAt } : null);
    } catch {
      setError('Failed to send message.');
      setNewMessage(content); // restore
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <MessageSquare size={28} className="text-sky-500" /> Messages
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">
          Secure messaging with your counsellor
        </p>
      </div>

      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-xl p-4 text-red-700 dark:text-red-300 text-sm">
          {error}
        </div>
      )}

      <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden"
        style={{ height: '70vh', display: 'flex' }}>

        {/* ── Conversation list ── */}
        <div className={`w-full md:w-80 border-r border-slate-100 dark:border-slate-700/50 flex flex-col flex-shrink-0 ${activeConv ? 'hidden md:flex' : 'flex'}`}>
          <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-700/50">
            <p className="text-sm font-bold text-slate-700 dark:text-slate-200">Conversations</p>
          </div>

          {loadingConvs ? (
            <div className="flex-1 flex items-center justify-center">
              <Loader2 size={24} className="animate-spin text-teal-500" />
            </div>
          ) : conversations.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center">
              <MessageSquare size={36} className="text-slate-300 dark:text-slate-600 mb-3" />
              <p className="text-sm text-slate-500 dark:text-slate-400 font-medium">No conversations yet</p>
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">
                Book an appointment to start messaging your counsellor.
              </p>
            </div>
          ) : (
            <div className="flex-1 overflow-y-auto">
              {conversations.map(conv => (
                <button key={conv.id} onClick={() => setActiveConv(conv)}
                  className={`w-full text-left px-4 py-3.5 border-b border-slate-50 dark:border-slate-700/30 hover:bg-slate-50 dark:hover:bg-slate-700/30 transition-colors ${
                    activeConv?.id === conv.id ? 'bg-teal-50 dark:bg-teal-900/20' : ''
                  }`}>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-teal-400/30 to-violet-400/30 flex items-center justify-center text-sm font-bold text-teal-700 dark:text-teal-300 flex-shrink-0">
                      {conv.otherUserName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <p className="text-sm font-semibold text-slate-800 dark:text-slate-200 truncate">
                          {conv.otherUserName}
                        </p>
                        {conv.lastMessageAt && (
                          <span className="text-xs text-slate-400 dark:text-slate-500 flex-shrink-0 ml-2">
                            {timeAgo(conv.lastMessageAt)}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-500 dark:text-slate-400 truncate mt-0.5">
                        {conv.lastMessagePreview ?? 'No messages yet'}
                      </p>
                    </div>
                    {conv.unreadCount > 0 && (
                      <span className="w-5 h-5 rounded-full bg-teal-500 text-white text-xs font-bold flex items-center justify-center flex-shrink-0">
                        {conv.unreadCount}
                      </span>
                    )}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* ── Message thread ── */}
        <div className={`flex-1 flex flex-col ${!activeConv ? 'hidden md:flex' : 'flex'}`}>
          {!activeConv ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-8">
              <MessageSquare size={48} className="text-slate-200 dark:text-slate-700 mb-4" />
              <p className="text-slate-500 dark:text-slate-400 font-medium">Select a conversation</p>
              <p className="text-slate-400 dark:text-slate-500 text-sm mt-1">
                Choose a conversation from the left to start messaging.
              </p>
            </div>
          ) : (
            <>
              {/* Thread header */}
              <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-700/50 flex items-center gap-3">
                <button onClick={() => setActiveConv(null)}
                  className="md:hidden p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 transition-colors">
                  <ArrowLeft size={18} />
                </button>
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-teal-400/30 to-violet-400/30 flex items-center justify-center text-sm font-bold text-teal-700 dark:text-teal-300">
                  {activeConv.otherUserName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}
                </div>
                <div>
                  <p className="text-sm font-bold text-slate-800 dark:text-slate-200">{activeConv.otherUserName}</p>
                  <p className="text-xs text-slate-400 dark:text-slate-500 capitalize">{activeConv.otherUserRole?.toLowerCase()}</p>
                </div>
              </div>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {loadingMsgs ? (
                  <div className="flex items-center justify-center h-full">
                    <Loader2 size={24} className="animate-spin text-teal-500" />
                  </div>
                ) : messages.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-full text-center">
                    <User size={36} className="text-slate-300 dark:text-slate-600 mb-3" />
                    <p className="text-sm text-slate-500 dark:text-slate-400">No messages yet. Say hello!</p>
                  </div>
                ) : (
                  messages.map(msg => {
                    const isMine = msg.senderId === user?.id;
                    return (
                      <div key={msg.id} className={`flex ${isMine ? 'justify-end' : 'justify-start'}`}>
                        <div className={`max-w-[75%] rounded-2xl px-4 py-2.5 ${
                          isMine
                            ? 'bg-teal-500 text-white rounded-br-sm'
                            : 'bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-200 rounded-bl-sm'
                        }`}>
                          <p className="text-sm leading-relaxed">{msg.content}</p>
                          <p className={`text-xs mt-1 ${isMine ? 'text-teal-100' : 'text-slate-400 dark:text-slate-500'}`}>
                            {timeAgo(msg.sentAt)}
                            {isMine && msg.isRead && ' · Read'}
                          </p>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={bottomRef} />
              </div>

              {/* Input */}
              <div className="px-4 py-3 border-t border-slate-100 dark:border-slate-700/50 flex items-end gap-3">
                <textarea
                  value={newMessage}
                  onChange={e => setNewMessage(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="Type a message… (Enter to send)"
                  rows={1}
                  className="flex-1 bg-slate-50 dark:bg-slate-700/50 border border-slate-200 dark:border-slate-600 rounded-xl px-4 py-2.5 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none transition-all"
                  style={{ maxHeight: '120px' }}
                />
                <button
                  onClick={handleSend}
                  disabled={!newMessage.trim() || sending}
                  className="w-10 h-10 rounded-xl bg-teal-500 hover:bg-teal-600 text-white flex items-center justify-center flex-shrink-0 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {sending ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default MessagesPage;

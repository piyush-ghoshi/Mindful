import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Send, Mic, MicOff, Paperclip, FileText, Download,
  ChevronDown, RefreshCw, Brain, AlertTriangle,
  Shield, Sparkles, Loader2, X, ArrowRight, CheckCircle,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  chatService,
  type ChatMessageDto,
  type ChatSessionDto,
  type MentalHealthReportDto,
  type RateLimitInfo,
} from '../services/chatService';
import MindBotReport from '../components/MindBotReport';

// ── Markdown-lite renderer (bold + newlines) ─────────────────────────────────
const renderContent = (text: string) => {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return parts.map((part, i) =>
    part.startsWith('**') && part.endsWith('**')
      ? <strong key={i} className="font-bold">{part.slice(2, -2)}</strong>
      : <span key={i}>{part}</span>
  );
};

const MessageBubble = ({ msg }: { msg: ChatMessageDto }) => {
  const isBot = msg.role === 'BOT';
  return (
    <div className={`flex gap-3 ${isBot ? 'justify-start' : 'justify-end'} mb-4`}>
      {isBot && (
        <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center flex-shrink-0 mt-0.5 shadow-sm">
          <Brain size={16} className="text-white" />
        </div>
      )}
      <div className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm leading-relaxed shadow-sm ${
        isBot
          ? 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 border border-slate-100 dark:border-slate-700/50 rounded-tl-sm'
          : 'bg-gradient-to-br from-teal-500 to-emerald-500 text-white rounded-tr-sm'
      }`}>
        {msg.content.split('\n').map((line, i) => (
          <p key={i} className={i > 0 ? 'mt-1.5' : ''}>{renderContent(line)}</p>
        ))}
        {msg.severityFlag === 'SEVERE' && (
          <div className="mt-2 flex items-center gap-1.5 text-xs text-red-200 bg-red-500/20 rounded-lg px-2 py-1">
            <AlertTriangle size={11} /> Crisis indicator detected
          </div>
        )}
        {msg.createdAt && (
          <p className={`text-[10px] mt-1.5 ${isBot ? 'text-slate-400 dark:text-slate-500' : 'text-white/60'}`}>
            {new Date(msg.createdAt).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
          </p>
        )}
      </div>
    </div>
  );
};

// ── Session type picker ───────────────────────────────────────────────────────
const SessionPicker = ({ onStart, rateLimitInfo }: {
  onStart: (type: 'ASSESSMENT' | 'CASUAL') => void;
  rateLimitInfo: RateLimitInfo | null;
}) => (
  <div className="flex flex-col items-center justify-center h-full text-center px-6 py-12 space-y-8">
    {/* Welcome */}
    <div className="space-y-3">
      <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center mx-auto shadow-xl shadow-teal-500/30">
        <Brain size={36} className="text-white" />
      </div>
      <h1 className="text-3xl font-black text-slate-800 dark:text-white">Hey there 💚</h1>
      <p className="text-slate-500 dark:text-slate-400 max-w-md text-base leading-relaxed">
        I'm <strong className="text-teal-600 dark:text-teal-400">MindBot</strong> — your private, AI-powered wellness companion.
      </p>
    </div>

    {/* Privacy badge */}
    <div className="flex items-center gap-2.5 px-5 py-3 rounded-2xl bg-teal-50 dark:bg-teal-900/20 border border-teal-200 dark:border-teal-700/40 text-sm text-teal-700 dark:text-teal-300 font-medium max-w-sm">
      <Shield size={16} className="flex-shrink-0" />
      <span>Your chats are <strong>completely private</strong> and never disclosed anywhere. This is your safe space. Share as you feel.</span>
    </div>

    {/* Session choice */}
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full max-w-lg">
      <button onClick={() => onStart('ASSESSMENT')}
        className="group flex flex-col items-start gap-3 p-5 rounded-2xl bg-white dark:bg-slate-800 border-2 border-teal-200 dark:border-teal-700/50 hover:border-teal-500 dark:hover:border-teal-500 hover:shadow-lg hover:shadow-teal-500/10 transition-all text-left">
        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center shadow-sm group-hover:shadow-md transition-all">
          <FileText size={22} className="text-white" />
        </div>
        <div>
          <p className="font-bold text-slate-800 dark:text-white">Full Assessment</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 leading-relaxed">
            Guided questions → personalised mental health report with recommendations
          </p>
        </div>
        <div className="flex items-center gap-1.5 text-xs text-teal-600 dark:text-teal-400 font-semibold mt-auto">
          <Sparkles size={12} /> Generates a report
        </div>
      </button>

      <button onClick={() => onStart('CASUAL')}
        className="group flex flex-col items-start gap-3 p-5 rounded-2xl bg-white dark:bg-slate-800 border-2 border-violet-200 dark:border-violet-700/50 hover:border-violet-500 dark:hover:border-violet-500 hover:shadow-lg hover:shadow-violet-500/10 transition-all text-left">
        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-violet-400 to-purple-500 flex items-center justify-center shadow-sm group-hover:shadow-md transition-all">
          <Brain size={22} className="text-white" />
        </div>
        <div>
          <p className="font-bold text-slate-800 dark:text-white">Casual Chat</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 leading-relaxed">
            Talk openly about stress, anxiety, mood, sleep, relationships & more
          </p>
        </div>
        <div className="flex items-center gap-1.5 text-xs text-violet-600 dark:text-violet-400 font-semibold mt-auto">
          <ArrowRight size={12} /> Open conversation
        </div>
      </button>
    </div>

    {/* Rate limit info */}
    {rateLimitInfo && (
      <div className="flex items-center gap-6 text-xs text-slate-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-800/50 rounded-xl px-5 py-3 border border-slate-100 dark:border-slate-700/50">
        <span>💬 {rateLimitInfo.messagesUsedToday}/{rateLimitInfo.messagesDailyLimit} msgs today</span>
        <span>📋 {rateLimitInfo.reportsUsedToday}/{rateLimitInfo.reportsDailyLimit} reports today</span>
        <span className="text-teal-600 dark:text-teal-400 font-semibold">Base Plan</span>
      </div>
    )}
  </div>
);

// ── Main MindBot Page ─────────────────────────────────────────────────────────
const MindBotPage = () => {
  const { user } = useAuth();

  // Only load data once we know the user is authenticated
  const isReady = !!user;

  const [session,  setSession]  = useState<ChatSessionDto | null>(null);
  const [messages, setMessages] = useState<ChatMessageDto[]>([]);
  const [input,    setInput]    = useState('');
  const [sending,  setSending]  = useState(false);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState('');
  const [rateLimitInfo, setRateLimitInfo] = useState<RateLimitInfo | null>(null);

  // Report state
  const [report,        setReport]        = useState<MentalHealthReportDto | null>(null);
  const [reports,       setReports]       = useState<MentalHealthReportDto[]>([]);
  const [showReport,    setShowReport]    = useState(false);
  const [generatingReport, setGeneratingReport] = useState(false);
  const [selectedReport,   setSelectedReport]   = useState<string>('');

  // Upload state
  const [uploadedText, setUploadedText] = useState('');
  const [showUpload,   setShowUpload]   = useState(false);

  // Voice
  const [recording, setRecording] = useState(false);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const recognitionRef = useRef<any>(null);

  const bottomRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Auto-scroll
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  // Load rate limit + past reports + active session — only once Firebase auth is ready
  useEffect(() => {
    if (!isReady) return;
    chatService.getRateLimitInfo().then(setRateLimitInfo).catch(() => {});
    chatService.getReports().then(setReports).catch(() => {});

    // Check for active session to auto-resume
    setLoading(true);
    chatService.getActiveSession()
      .then(sess => {
        if (sess) {
          setSession(sess);
          setMessages(sess.messages);
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [isReady]);

  const startSession = useCallback(async (type: 'ASSESSMENT' | 'CASUAL') => {
    setLoading(true); setError('');
    try {
      const sess = await chatService.startSession(type);
      setSession(sess);
      setMessages(sess.messages);
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message ?? 'Failed to start session';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  const sendMessage = async () => {
    if (!input.trim() || !session || sending) return;
    const content = input.trim();
    setInput('');

    // Optimistic UI
    const userMsg: ChatMessageDto = { role: 'USER', content, createdAt: new Date().toISOString() };
    setMessages(prev => [...prev, userMsg]);
    setSending(true);

    try {
      const botMsg = await chatService.sendMessage(session.id, content, uploadedText || undefined);
      setMessages(prev => [...prev, botMsg]);
      setUploadedText('');
      setShowUpload(false);

      // Update session message count
      setSession(prev => prev ? { ...prev, messageCount: prev.messageCount + 1 } : prev);

      // Check if session ended (SEVERE or assessment complete)
      if (botMsg.severityFlag === 'SEVERE' || botMsg.content.includes('generating your personalised report') || botMsg.content.includes('Generate Report')) {
        setSession(prev => prev ? { ...prev, isActive: false } : prev);
      }
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message ?? 'Failed to send message';
      if (msg.includes('limit')) {
        setMessages(prev => [...prev, {
          role: 'BOT',
          content: `⏳ ${msg}\n\nUpgrade to Premium for 50 messages/day and 2 reports/day.`,
          createdAt: new Date().toISOString(),
        }]);
      } else {
        setError(msg);
      }
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
  };

  const handleGenerateReport = async () => {
    if (!session) return;
    setGeneratingReport(true);
    try {
      const r = await chatService.generateReport(session.id);
      setReport(r);
      setReports(prev => [r, ...prev.filter(x => x.id !== r.id)]);
      setShowReport(true);
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message ?? 'Failed to generate report';
      setError(msg);
    } finally {
      setGeneratingReport(false);
    }
  };

  const handleLoadReport = async (id: string) => {
    if (!id) return;
    try {
      const r = await chatService.getReport(id);
      setReport(r);
      setShowReport(true);
    } catch { setError('Failed to load report'); }
  };

  const handleDownloadReport = () => {
    if (!report) return;
    const el = document.getElementById('mindbot-report');
    if (!el) return;
    // Simple print-to-PDF via browser
    const win = window.open('', '_blank');
    if (!win) return;
    win.document.write(`
      <!DOCTYPE html><html><head>
      <title>${report.title}</title>
      <style>
        body { font-family: 'Segoe UI', sans-serif; color: #1e293b; padding: 32px; max-width: 700px; margin: 0 auto; }
        h1 { color: #006b5f; } h2 { color: #006b5f; font-size: 14px; }
        .badge { background: #f0faf8; border: 1px solid #ccfbf1; color: #0d9488; padding: 4px 10px; border-radius: 99px; font-size: 11px; font-weight: bold; }
        .section { margin-top: 20px; } ul { padding-left: 18px; } li { margin: 6px 0; font-size: 13px; }
        .disclaimer { font-size: 10px; color: #94a3b8; margin-top: 24px; border-top: 1px solid #e2e8f0; padding-top: 12px; }
        .watermark { position: fixed; opacity: 0.06; font-size: 80px; font-weight: 900; color: #14b8a6; transform: rotate(-30deg); top: 35%; left: 10%; pointer-events: none; }
      </style></head><body>
      <div class="watermark">MINDFUL</div>
      <h1>${report.title}</h1>
      <p>${user?.firstName ?? 'User'} ${user?.lastName ?? ''} · ${new Date(report.createdAt).toLocaleString()}</p>
      <span class="badge">Mental State: ${report.mentalStateLevel} · Score: ${report.wellnessScore}/100</span>
      <div class="section"><h2>OBSERVED CONDITION</h2><ul>${report.conditionPoints.map(p => `<li>${p}</li>`).join('')}</ul></div>
      <div class="section"><h2>RECOMMENDED EXERCISES</h2><ul>${report.recommendedExercises.map(e => `<li>${e}</li>`).join('')}</ul></div>
      <div class="section"><h2>RECOMMENDED MEDITATIONS</h2><ul>${report.recommendedMeditations.map(m => `<li>${m}</li>`).join('')}</ul></div>
      <div class="section"><h2>FINAL REVIEW</h2><p>${report.conclusion}</p></div>
      ${report.counsellorReferralSuggested ? '<p style="color:red;font-weight:bold;">⚠️ Counsellor consultation strongly recommended.</p>' : ''}
      <div class="disclaimer">⚠️ AI-generated report for personal reference only. Not a clinical diagnosis. Consult a licensed mental health professional for medical advice. © Mindful</div>
      </body></html>
    `);
    win.document.close();
    win.focus();
    setTimeout(() => { win.print(); win.close(); }, 500);
  };

  // Voice input
  const toggleRecording = () => {
    const SR = (window as typeof window & { SpeechRecognition?: new () => any; webkitSpeechRecognition?: new () => any }).SpeechRecognition
      || (window as typeof window & { webkitSpeechRecognition?: new () => any }).webkitSpeechRecognition;
    if (!SR) { setError('Voice input not supported in this browser.'); return; }

    if (recording) {
      recognitionRef.current?.stop();
      setRecording(false);
      return;
    }

    const rec = new SR();
    rec.continuous = false; rec.interimResults = false; rec.lang = 'en-IN';
    rec.onresult = (e: { results: { [index: number]: { [index: number]: { transcript: string } } } }) => {
      const transcript = e.results[0][0].transcript;
      setInput(prev => prev + (prev ? ' ' : '') + transcript);
    };
    rec.onerror = () => setRecording(false);
    rec.onend   = () => setRecording(false);
    rec.start();
    recognitionRef.current = rec;
    setRecording(true);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = ev => {
      setUploadedText(ev.target?.result as string ?? '');
      setShowUpload(false);
    };
    reader.readAsText(file);
  };

  const endAndNew = async () => {
    setSession(null);
    setMessages([]);
    setReport(null);
    setShowReport(false);
    setError('');
    setInput('');
    try {
      await chatService.endActiveSession();
    } catch {}
    chatService.getRateLimitInfo().then(setRateLimitInfo).catch(() => {});
    chatService.getReports().then(setReports).catch(() => {});
  };

  // Can generate report?
  const sessionEnded = session && !session.isActive;

  return (
    <div className="flex flex-col h-[calc(100vh-80px)] max-w-4xl mx-auto">

      {/* ── Page header ── */}
      <div className="flex items-center justify-between mb-4 flex-shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center shadow-sm">
            <Brain size={20} className="text-white" />
          </div>
          <div>
            <h1 className="text-xl font-black text-slate-800 dark:text-white">MindBot</h1>
            <p className="text-xs text-slate-500 dark:text-slate-400">AI Wellness Companion · Private & Confidential</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Past reports dropdown */}
          {reports.length > 0 && (
            <div className="relative">
              <select
                value={selectedReport}
                onChange={e => { setSelectedReport(e.target.value); handleLoadReport(e.target.value); }}
                className="appearance-none pl-3 pr-8 py-2 text-xs font-semibold bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-slate-700 dark:text-slate-300 focus:outline-none focus:ring-2 focus:ring-teal-500 cursor-pointer">
                <option value="">📋 Past Reports</option>
                {reports.map(r => (
                  <option key={r.id} value={r.id}>{r.title} — {new Date(r.createdAt).toLocaleDateString()}</option>
                ))}
              </select>
              <ChevronDown size={12} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
            </div>
          )}

          {session && (
            <button onClick={endAndNew}
              className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors border border-slate-200 dark:border-slate-700">
              <RefreshCw size={13} /> New Chat
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="mb-3 flex items-center gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700/50 text-red-700 dark:text-red-300 text-sm flex-shrink-0">
          <AlertTriangle size={15} />
          {error}
          <button onClick={() => setError('')} className="ml-auto"><X size={14} /></button>
        </div>
      )}

      {/* ── Report view overlay ── */}
      {showReport && report && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-3xl">
            <div className="flex items-center justify-between mb-3 px-1">
              <p className="text-xs text-white/80 font-semibold">Mental Health Report</p>
              <button onClick={() => setShowReport(false)}
                className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white transition-colors">
                <X size={16} />
              </button>
            </div>
            <MindBotReport
              report={report}
              userName={`${user?.firstName ?? ''} ${user?.lastName ?? ''}`.trim()}
              onDownload={handleDownloadReport}
            />
          </div>
        </div>
      )}

      {/* ── Chat area ── */}
      <div className="flex-1 bg-white/60 dark:bg-slate-900/60 backdrop-blur rounded-3xl border border-white/80 dark:border-slate-700/50 shadow-lg overflow-hidden flex flex-col">

        {loading ? (
          <div className="flex-1 flex items-center justify-center">
            <Loader2 size={28} className="animate-spin text-teal-500" />
          </div>
        ) : !session ? (
          <div className="flex-1 overflow-y-auto">
            <SessionPicker onStart={startSession} rateLimitInfo={rateLimitInfo} />
          </div>
        ) : (
          <>
            {/* Messages */}
            <div className="flex-1 overflow-y-auto px-4 pt-4 pb-2">
              {messages.map((msg, i) => <MessageBubble key={i} msg={msg} />)}

              {sending && (
                <div className="flex gap-3 mb-4">
                  <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center flex-shrink-0 shadow-sm">
                    <Brain size={16} className="text-white" />
                  </div>
                  <div className="bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/50 rounded-2xl rounded-tl-sm px-4 py-3 flex gap-1.5 items-center shadow-sm">
                    {[0,1,2].map(i => (
                      <span key={i} className="w-2 h-2 rounded-full bg-teal-500 animate-bounce" style={{ animationDelay: `${i * 0.15}s` }} />
                    ))}
                  </div>
                </div>
              )}

              {/* Session ended banners */}
              {sessionEnded && !session.reportGenerated && session.sessionType === 'ASSESSMENT' && (
                <div className="flex flex-col items-center gap-3 py-6">
                  <div className="text-center">
                    <p className="text-sm font-semibold text-slate-600 dark:text-slate-400">Assessment complete 🌱</p>
                    <p className="text-xs text-slate-500 dark:text-slate-500 mt-1">Your report is ready to generate.</p>
                  </div>
                  <button onClick={handleGenerateReport} disabled={generatingReport}
                    className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-teal-500 to-emerald-500 text-white rounded-2xl font-bold shadow-lg shadow-teal-500/20 hover:from-teal-600 hover:to-emerald-600 transition-all disabled:opacity-60">
                    {generatingReport ? <Loader2 size={16} className="animate-spin" /> : <Sparkles size={16} />}
                    {generatingReport ? 'Generating…' : 'Generate My Report'}
                  </button>
                </div>
              )}

              {sessionEnded && session.reportGenerated && (
                <div className="flex flex-col items-center gap-3 py-4">
                  <div className="flex items-center gap-2 text-sm text-teal-600 dark:text-teal-400 font-semibold">
                    <CheckCircle size={16} /> Report generated and saved
                  </div>
                  <div className="flex gap-2">
                    <button onClick={() => { setShowReport(true); }}
                      className="flex items-center gap-1.5 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-xs font-bold transition-colors">
                      <FileText size={14} /> View Report
                    </button>
                    <button onClick={handleDownloadReport}
                      className="flex items-center gap-1.5 px-4 py-2 border border-teal-200 dark:border-teal-700/50 text-teal-700 dark:text-teal-300 rounded-xl text-xs font-bold hover:bg-teal-50 dark:hover:bg-teal-900/20 transition-colors">
                      <Download size={14} /> Download PDF
                    </button>
                  </div>
                </div>
              )}

              <div ref={bottomRef} />
            </div>

            {/* ── Input area ── */}
            {session.isActive && (
              <div className="flex-shrink-0 p-3 border-t border-slate-100 dark:border-slate-700/50 space-y-2">
                {/* Upload panel */}
                {showUpload && (
                  <div className="bg-slate-50 dark:bg-slate-800/60 rounded-xl p-3 border border-slate-200 dark:border-slate-700/50 text-sm">
                    <div className="flex items-center justify-between mb-2">
                      <p className="text-xs font-semibold text-slate-600 dark:text-slate-400">Attach previous report</p>
                      <button onClick={() => setShowUpload(false)}><X size={14} className="text-slate-400" /></button>
                    </div>
                    <div className="flex flex-wrap gap-2 items-center">
                      <button onClick={() => fileInputRef.current?.click()}
                        className="flex items-center gap-1.5 px-3 py-1.5 bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-lg text-xs font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-600 transition-colors">
                        <Paperclip size={12} /> Upload File
                      </button>
                      <input ref={fileInputRef} type="file" accept=".txt,.pdf,.docx" className="hidden" onChange={handleFileUpload} />

                      {reports.length > 0 && (
                        <select
                          onChange={e => {
                            const rId = e.target.value;
                            if (!rId) return;
                            const r = reports.find(x => x.id === rId);
                            if (r) {
                              const formatted = `[PAST REPORT REFERENCE]\n` +
                                `Title: ${r.title}\n` +
                                `Date: ${new Date(r.createdAt).toLocaleDateString()}\n` +
                                `Mental State Level: ${r.mentalStateLevel}\n` +
                                `Wellness Score: ${r.wellnessScore}/100\n` +
                                `Observations:\n${r.conditionPoints.map(p => ` - ${p}`).join('\n')}\n` +
                                `Recommendations:\n${r.recommendedExercises.map(ex => ` - ${ex}`).join('\n')}\n` +
                                `Meditations:\n${r.recommendedMeditations.map(m => ` - ${m}`).join('\n')}\n` +
                                `Conclusion: ${r.conclusion}`;
                              setUploadedText(formatted);
                              e.target.value = ''; // Reset select
                            }
                          }}
                          className="px-3 py-1.5 bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-lg text-xs font-semibold text-slate-600 dark:text-slate-300 focus:outline-none focus:ring-1 focus:ring-teal-500 cursor-pointer max-w-[200px]"
                        >
                          <option value="">📋 Select Past Report</option>
                          {reports.map(r => (
                            <option key={r.id} value={r.id}>{r.title} ({new Date(r.createdAt).toLocaleDateString()})</option>
                          ))}
                        </select>
                      )}
                    </div>
                    {uploadedText && (
                      <p className="text-xs text-teal-600 dark:text-teal-400 mt-2 font-medium flex items-center gap-1">
                        <CheckCircle size={11} /> Report loaded ({uploadedText.length} chars)
                      </p>
                    )}
                    <textarea
                      value={uploadedText}
                      onChange={e => setUploadedText(e.target.value)}
                      placeholder="Or paste your previous report text here…"
                      rows={3}
                      className="w-full mt-2 bg-white dark:bg-slate-700/50 border border-slate-200 dark:border-slate-600 rounded-lg px-3 py-2 text-xs text-slate-700 dark:text-slate-300 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none"
                    />
                  </div>
                )}

                {/* Rate limit bar */}
                <div className="flex items-center gap-2 text-[10px] text-slate-400 dark:text-slate-500 px-1">
                  <span className="font-semibold">Daily:</span>
                  <div className="flex-1 h-1 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div className="h-full bg-teal-500 rounded-full transition-all"
                      style={{ width: `${((session.messageCount || 0) / (rateLimitInfo?.messagesDailyLimit ?? 10)) * 100}%` }} />
                  </div>
                  <span>{session.messageCount}/{rateLimitInfo?.messagesDailyLimit ?? 10} msgs</span>
                </div>

                {/* Text input row */}
                <div className="flex items-end gap-2">
                  <button onClick={() => setShowUpload(!showUpload)}
                    className={`p-2.5 rounded-xl transition-colors flex-shrink-0 ${uploadedText ? 'bg-teal-100 dark:bg-teal-900/40 text-teal-600 dark:text-teal-400' : 'hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400'}`}
                    title="Attach previous report">
                    <Paperclip size={17} />
                  </button>

                  <textarea
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder="Type your message… (Enter to send)"
                    rows={1}
                    className="flex-1 bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-2xl px-4 py-2.5 text-sm text-slate-800 dark:text-slate-200 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none transition-all"
                    style={{ maxHeight: '120px' }}
                  />

                  <button onClick={toggleRecording}
                    className={`p-2.5 rounded-xl transition-colors flex-shrink-0 ${recording ? 'bg-red-100 dark:bg-red-900/40 text-red-500 pulse-ring' : 'hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400'}`}
                    title={recording ? 'Stop recording' : 'Voice input'}>
                    {recording ? <MicOff size={17} /> : <Mic size={17} />}
                  </button>

                  <button onClick={sendMessage} disabled={!input.trim() || sending}
                    className="p-2.5 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-500 hover:from-teal-600 hover:to-emerald-600 text-white shadow-md shadow-teal-500/20 transition-all disabled:opacity-40 flex-shrink-0">
                    {sending ? <Loader2 size={17} className="animate-spin" /> : <Send size={17} />}
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default MindBotPage;

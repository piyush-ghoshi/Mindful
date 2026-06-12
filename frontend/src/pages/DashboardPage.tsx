import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Smile, CalendarCheck, BookOpen, TrendingUp,
  AlertTriangle, Lightbulb, ChevronRight, Sparkles,
  Brain, HeartPulse, MessageSquare, Users,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../services/api';
import type { Appointment } from '../types';
import { useAuthReady } from '../hooks/useAuthReady';

const DAILY_TIPS = [
  'Take 5-minute screen breaks every hour. Your mind needs rest to stay sharp.',
  'Try box breathing: inhale 4s, hold 4s, exhale 4s, hold 4s. Repeat 4 times.',
  'A short walk outside can reset your focus and lift your mood.',
  'Drinking water regularly helps maintain energy and concentration.',
  "Writing down 3 things you're grateful for can shift your perspective.",
];

const quickLinks = [
  { to: '/mood-journal',      icon: Smile,         label: 'Mood Check-in',    desc: 'Log your feelings',    gradient: 'from-teal-500/20 to-emerald-500/20 dark:from-teal-500/30 dark:to-emerald-500/30',     iconBg: 'bg-teal-500/20 dark:bg-teal-400/20',     iconColor: 'text-teal-600 dark:text-teal-300',     border: 'border-teal-200/60 dark:border-teal-500/30' },
  { to: '/appointments/book', icon: CalendarCheck,  label: 'Book Appointment', desc: 'Schedule a session',  gradient: 'from-violet-500/20 to-purple-500/20 dark:from-violet-500/30 dark:to-purple-500/30', iconBg: 'bg-violet-500/20 dark:bg-violet-400/20', iconColor: 'text-violet-600 dark:text-violet-300', border: 'border-violet-200/60 dark:border-violet-500/30' },
  { to: '/resources',         icon: BookOpen,       label: 'Resource Hub',     desc: 'Articles & guides',   gradient: 'from-sky-500/20 to-blue-500/20 dark:from-sky-500/30 dark:to-blue-500/30',           iconBg: 'bg-sky-500/20 dark:bg-sky-400/20',       iconColor: 'text-sky-600 dark:text-sky-300',       border: 'border-sky-200/60 dark:border-sky-500/30' },
  { to: '/wellness-tracker',  icon: TrendingUp,     label: 'My Progress',      desc: 'View your stats',     gradient: 'from-amber-500/20 to-orange-500/20 dark:from-amber-500/30 dark:to-orange-500/30',   iconBg: 'bg-amber-500/20 dark:bg-amber-400/20',   iconColor: 'text-amber-600 dark:text-amber-300',   border: 'border-amber-200/60 dark:border-amber-500/30' },
  { to: '/messages',          icon: MessageSquare,  label: 'Messages',         desc: 'Chat with counsellor',gradient: 'from-sky-500/20 to-cyan-500/20 dark:from-sky-500/30 dark:to-cyan-500/30',           iconBg: 'bg-sky-500/20 dark:bg-sky-400/20',       iconColor: 'text-sky-600 dark:text-sky-300',       border: 'border-sky-200/60 dark:border-sky-500/30' },
  { to: '/forum',             icon: Users,          label: 'Community Forum',  desc: 'Peer support',        gradient: 'from-violet-500/20 to-purple-500/20 dark:from-violet-500/30 dark:to-purple-500/30', iconBg: 'bg-violet-500/20 dark:bg-violet-400/20', iconColor: 'text-violet-600 dark:text-violet-300', border: 'border-violet-200/60 dark:border-violet-500/30' },
  { to: '/mindbot', icon: Brain, label: 'Chat with MindBot', desc: 'AI wellness companion', gradient: 'from-teal-500/20 to-cyan-500/20 dark:from-teal-500/30 dark:to-cyan-500/30', iconBg: 'bg-teal-500/20 dark:bg-teal-400/20', iconColor: 'text-teal-600 dark:text-teal-300', border: 'border-teal-200/60 dark:border-teal-500/30' },
  { to: '/crisis-support', icon: AlertTriangle, label: 'Crisis Support', desc: 'Immediate help', gradient: 'from-red-500/20 to-rose-500/20 dark:from-red-500/30 dark:to-rose-500/30', iconBg: 'bg-red-500/20 dark:bg-red-400/20', iconColor: 'text-red-600 dark:text-red-300', border: 'border-red-200/60 dark:border-red-500/30' },
];

const DashboardPage = () => {
  const { user } = useAuth();
  const authReady = useAuthReady();
  const [nextAppt, setNextAppt] = useState<Appointment | null>(null);
  const [moodStreak, setMoodStreak] = useState<number>(0);
  const [completedSessions, setCompletedSessions] = useState<number>(0);
  const [loadingAppt, setLoadingAppt] = useState(true);

  const today = new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
  const tip = DAILY_TIPS[new Date().getDay() % DAILY_TIPS.length];
  const greeting = () => { const h = new Date().getHours(); return h < 12 ? 'Good Morning' : h < 17 ? 'Good Afternoon' : 'Good Evening'; };

  useEffect(() => {
    if (!authReady) return;
    // Fetch upcoming appointment
    apiClient.get<Appointment[]>('/appointments/upcoming')
      .then(res => {
        const list = Array.isArray(res) ? res : (res as { content?: Appointment[] }).content ?? [];
        const upcoming = list.filter(a => a.status !== 'CANCELLED').sort(
          (a, b) => new Date(a.scheduledStartTime).getTime() - new Date(b.scheduledStartTime).getTime()
        );
        setNextAppt(upcoming[0] ?? null);
      })
      .catch(() => {})
      .finally(() => setLoadingAppt(false));

    // Fetch all appointments to count completed sessions
    apiClient.get<Appointment[]>('/appointments')
      .then(res => {
        const list = Array.isArray(res) ? res : (res as { content?: Appointment[] }).content ?? [];
        setCompletedSessions(list.filter(a => a.status === 'COMPLETED').length);
      })
      .catch(() => {});

    // Fetch mood stats for streak (entries in last 7 days = streak proxy)
    apiClient.get<{ totalEntries?: number }>('/mood/stats?days=7')
      .then(res => setMoodStreak(res.totalEntries ?? 0))
      .catch(() => {});
  }, [authReady]);

  const fmtAppt = (iso: string) => new Date(iso).toLocaleString('en-US', { weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });

  return (
    <div className="space-y-8">
      {/* Greeting + Daily Tip */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 space-y-3">
          <p className="text-sm font-medium text-slate-400 dark:text-slate-500">{today}</p>
          <h1 className="text-4xl font-bold text-slate-800 dark:text-white leading-tight">
            {greeting()}, {user?.firstName ?? 'there'} 👋
          </h1>
          <p className="text-slate-500 dark:text-slate-400 max-w-lg text-base leading-relaxed">
            Take a deep breath. You're doing great today. Ready to check in on your wellness goals?
          </p>
        </div>
        <div className="bg-white dark:bg-slate-800/60 backdrop-blur-sm rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 shadow-sm flex flex-col gap-3">
          <div className="flex items-center gap-2 text-amber-500 dark:text-amber-400">
            <Lightbulb size={18} />
            <span className="text-xs font-bold uppercase tracking-widest">Daily Tip</span>
          </div>
          <p className="text-slate-600 dark:text-slate-300 text-sm leading-relaxed italic">"{tip}"</p>
        </div>
      </section>

      {/* Quick Access */}
      <section>
        <h2 className="text-lg font-bold text-slate-700 dark:text-slate-200 mb-4 flex items-center gap-2">
          <Sparkles size={18} className="text-teal-500" /> Quick Access
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {quickLinks.map((item) => {
            const { to, icon: Icon, label, desc, gradient, iconBg, iconColor, border } = item;
            const disabled = (item as { disabled?: boolean }).disabled ?? false;
            const card = (
              <div className={`relative rounded-2xl p-5 border flex flex-col items-center text-center gap-3 bg-gradient-to-br ${gradient} ${border} transition-all duration-200 ${disabled ? 'opacity-50 cursor-not-allowed' : 'hover:scale-[1.02] hover:shadow-lg hover:shadow-black/10 dark:hover:shadow-black/30 cursor-pointer'}`}>
                <div className={`w-12 h-12 rounded-2xl ${iconBg} flex items-center justify-center`}>
                  <Icon size={22} className={iconColor} />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-slate-800 dark:text-slate-100">{label}</h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{desc}</p>
                </div>
                {disabled && <span className="absolute top-2 right-2 text-[10px] bg-slate-200 dark:bg-slate-700 text-slate-500 dark:text-slate-400 px-1.5 py-0.5 rounded-full font-semibold">Soon</span>}
              </div>
            );
            if (disabled) return <div key={label}>{card}</div>;
            return <Link key={label} to={to}>{card}</Link>;
          })}
        </div>
      </section>

      {/* Next Appointment Banner */}
      <section>
        <div className="relative rounded-2xl p-8 overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6 bg-gradient-to-br from-[#006b5f] to-[#004941] dark:from-teal-900 dark:to-slate-900 border border-teal-700/30 shadow-lg">
          <div className="absolute -right-12 -top-12 w-48 h-48 bg-teal-400/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -left-8 -bottom-8 w-32 h-32 bg-emerald-400/10 rounded-full blur-2xl pointer-events-none" />
          <div className="flex items-center gap-5 z-10 text-white w-full md:w-auto">
            <div className="w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-sm border border-white/20 flex items-center justify-center flex-shrink-0">
              <HeartPulse size={26} className="text-teal-200" />
            </div>
            <div>
              <p className="text-xs font-bold text-teal-300 uppercase tracking-widest mb-1">Next Appointment</p>
              {loadingAppt ? (
                <div className="w-40 h-5 bg-white/10 rounded animate-pulse" />
              ) : nextAppt ? (
                <>
                  <h3 className="text-xl font-bold text-white">{nextAppt.appointmentType?.replace('_', ' ')} Session</h3>
                  <p className="text-teal-200/80 text-sm mt-0.5">{fmtAppt(nextAppt.scheduledStartTime)}</p>
                </>
              ) : (
                <>
                  <h3 className="text-xl font-bold text-white">No upcoming appointments</h3>
                  <p className="text-teal-200/80 text-sm mt-0.5">Book a session with a counsellor today</p>
                </>
              )}
            </div>
          </div>
          <Link to={nextAppt ? '/appointments' : '/appointments/book'}
            className="z-10 w-full md:w-auto bg-white/10 hover:bg-white/20 backdrop-blur-sm border border-white/20 text-white px-7 py-3 rounded-xl font-semibold text-sm transition-all flex items-center gap-2 justify-center">
            {nextAppt ? 'View Appointments' : 'Book Now'} <ChevronRight size={16} />
          </Link>
        </div>
      </section>

      {/* Wellness Snapshot */}
      <section>
        <h2 className="text-lg font-bold text-slate-700 dark:text-slate-200 mb-4">Wellness Snapshot</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {[
            { label: 'Mood Entries (7 days)', value: moodStreak > 0 ? `${moodStreak} entries` : '0 entries', sub: moodStreak > 0 ? 'Keep it up!' : 'Start tracking today', color: 'text-teal-500 dark:text-teal-400', bg: 'bg-teal-500/10 dark:bg-teal-500/20', border: 'border-teal-200/50 dark:border-teal-700/40' },
            { label: 'Sessions Completed', value: String(completedSessions), sub: completedSessions > 0 ? `${completedSessions} session${completedSessions !== 1 ? 's' : ''} done` : 'Book your first session', color: 'text-violet-500 dark:text-violet-400', bg: 'bg-violet-500/10 dark:bg-violet-500/20', border: 'border-violet-200/50 dark:border-violet-700/40' },
            { label: 'Wellness Goals', value: 'Track →', sub: 'View your progress', color: 'text-amber-500 dark:text-amber-400', bg: 'bg-amber-500/10 dark:bg-amber-500/20', border: 'border-amber-200/50 dark:border-amber-700/40', link: '/wellness-tracker' },
          ].map(({ label, value, sub, color, bg, border, link }) => (
            <div key={label} className={`rounded-2xl p-6 border ${bg} ${border} ${link ? 'cursor-pointer hover:opacity-80 transition-opacity' : ''}`}
              onClick={link ? () => window.location.href = link : undefined}>
              <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide mb-2">{label}</p>
              <p className={`text-3xl font-bold ${color}`}>{value}</p>
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">{sub}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default DashboardPage;

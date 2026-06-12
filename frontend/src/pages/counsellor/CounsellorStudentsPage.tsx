import { useState, useEffect } from 'react';
import { Users, Search, TrendingUp, Calendar, MessageSquare, Loader2, RefreshCw } from 'lucide-react';
import { apiClient } from '../../services/api';

interface StudentRow {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  totalSessions: number;
  lastAppointmentDate?: string;
  averageMoodRating?: number;
}

const MOOD_COLOR = (m?: number) => {
  if (m == null) return 'text-slate-400 dark:text-slate-500';
  if (m >= 4) return 'text-teal-600 dark:text-teal-400';
  if (m >= 3) return 'text-amber-600 dark:text-amber-400';
  return 'text-red-600 dark:text-red-400';
};

const relativeDate = (iso?: string) => {
  if (!iso) return '—';
  const diff = Date.now() - new Date(iso).getTime();
  const days = Math.floor(diff / 86400000);
  if (days === 0) return 'Today';
  if (days === 1) return 'Yesterday';
  if (days < 7) return `${days} days ago`;
  if (days < 30) return `${Math.floor(days / 7)} week${Math.floor(days / 7) > 1 ? 's' : ''} ago`;
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const CounsellorStudentsPage = () => {
  const [students, setStudents] = useState<StudentRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  const load = () => {
    setLoading(true);
    setError('');
    // Fetch appointments for this counsellor, then derive unique students from them
    apiClient.get<unknown>('/appointments')
      .then(raw => {
        type ApptRaw = {
          id: string;
          studentId: string;
          studentName?: string;
          counsellorId: string;
          scheduledStartTime: string;
          status: string;
        };
        const list: ApptRaw[] = Array.isArray(raw)
          ? (raw as ApptRaw[])
          : ((raw as { content?: ApptRaw[]; data?: ApptRaw[] }).content
              ?? (raw as { content?: ApptRaw[]; data?: ApptRaw[] }).data
              ?? []);

        // Group by studentId
        const map = new Map<string, StudentRow>();
        for (const appt of list) {
          if (!appt.studentId) continue;
          const existing = map.get(appt.studentId);
          const apptDate = appt.scheduledStartTime;
          if (!existing) {
            map.set(appt.studentId, {
              id: appt.studentId,
              firstName: appt.studentName?.split(' ')[0] ?? 'Student',
              lastName: appt.studentName?.split(' ').slice(1).join(' ') ?? '',
              email: '',
              totalSessions: 1,
              lastAppointmentDate: apptDate,
            });
          } else {
            existing.totalSessions += 1;
            if (!existing.lastAppointmentDate || apptDate > existing.lastAppointmentDate) {
              existing.lastAppointmentDate = apptDate;
            }
          }
        }
        setStudents(Array.from(map.values()));
      })
      .catch(() => setError('Could not load students. Make sure the backend is running.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const filtered = students.filter(s => {
    const q = search.toLowerCase();
    return (
      s.firstName.toLowerCase().includes(q) ||
      s.lastName.toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
            <Users size={28} className="text-violet-500" /> My Students
          </h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">
            Students who have booked sessions with you
          </p>
        </div>
        <button onClick={load}
          className="flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
          <RefreshCw size={15} />Refresh
        </button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search students…"
          className="w-full bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl pl-11 pr-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all shadow-sm"
        />
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 size={32} className="animate-spin text-teal-500" />
        </div>
      ) : error ? (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-2xl p-6 text-red-700 dark:text-red-300 text-sm text-center">
          {error}
        </div>
      ) : filtered.length === 0 ? (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-16 border border-slate-100 dark:border-slate-700/50 text-center">
          <Users size={48} className="text-slate-300 dark:text-slate-600 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 dark:text-slate-300 mb-2">
            {search ? 'No students match your search' : 'No students yet'}
          </h3>
          <p className="text-slate-400 dark:text-slate-500 text-sm">
            {search
              ? 'Try a different name or email.'
              : 'Students will appear here once they book a session with you.'}
          </p>
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-slate-50 dark:bg-slate-700/30 border-b border-slate-100 dark:border-slate-700/50">
                {['Student', 'Sessions', 'Last Session', 'Mood Score', 'Actions'].map(h => (
                  <th key={h} className="py-3 px-5 text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50 dark:divide-slate-700/30">
              {filtered.map(s => (
                <tr key={s.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/20 transition-colors">
                  <td className="py-4 px-5">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full bg-gradient-to-br from-teal-400/30 to-violet-400/30 dark:from-teal-500/20 dark:to-violet-500/20 flex items-center justify-center text-sm font-bold text-teal-700 dark:text-teal-300 flex-shrink-0">
                        {s.firstName[0]}{s.lastName?.[0] ?? ''}
                      </div>
                      <div>
                        <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                          {s.firstName} {s.lastName}
                        </p>
                        {s.email && <p className="text-xs text-slate-400">{s.email}</p>}
                        {!s.email && <p className="text-xs text-slate-400 font-mono">…{s.id.slice(-8)}</p>}
                      </div>
                    </div>
                  </td>
                  <td className="py-4 px-5 text-sm text-slate-700 dark:text-slate-300">
                    {s.totalSessions} session{s.totalSessions !== 1 ? 's' : ''}
                  </td>
                  <td className="py-4 px-5 text-sm text-slate-500 dark:text-slate-400">
                    {relativeDate(s.lastAppointmentDate)}
                  </td>
                  <td className="py-4 px-5">
                    {s.averageMoodRating != null ? (
                      <div className="flex items-center gap-2">
                        <span className={`text-lg font-bold ${MOOD_COLOR(s.averageMoodRating)}`}>
                          {s.averageMoodRating.toFixed(1)}
                        </span>
                        <span className="text-xs text-slate-400">/5</span>
                      </div>
                    ) : (
                      <span className="text-xs text-slate-400 dark:text-slate-500">No data</span>
                    )}
                  </td>
                  <td className="py-4 px-5">
                    <div className="flex items-center gap-2">
                      <button
                        title="View mood history"
                        className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 dark:text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 transition-colors">
                        <TrendingUp size={16} />
                      </button>
                      <button
                        title="Book session"
                        className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 dark:text-slate-400 hover:text-violet-600 dark:hover:text-violet-400 transition-colors">
                        <Calendar size={16} />
                      </button>
                      <button
                        title="Message"
                        className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 dark:text-slate-400 hover:text-sky-600 dark:hover:text-sky-400 transition-colors">
                        <MessageSquare size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="px-5 py-3 border-t border-slate-100 dark:border-slate-700/50 text-xs text-slate-400 dark:text-slate-500">
            {filtered.length} student{filtered.length !== 1 ? 's' : ''}
            {search && ` matching "${search}"`}
          </div>
        </div>
      )}
    </div>
  );
};

export default CounsellorStudentsPage;

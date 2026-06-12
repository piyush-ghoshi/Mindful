import { useState, useEffect, useCallback } from 'react';
import { CalendarCheck, Video, Phone, MapPin, ChevronLeft, ChevronRight, FileText, Loader2 } from 'lucide-react';
import { appointmentService } from '../../services/appointmentService';
import type { Appointment } from '../../types';

const TYPE_ICONS: Record<string, React.ElementType> = { VIDEO: Video, PHONE: Phone, IN_PERSON: MapPin };

const STATUS_CFG: Record<string, { label: string; cls: string }> = {
  SCHEDULED:   { label: 'Pending',     cls: 'bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-300' },
  CONFIRMED:   { label: 'Confirmed',   cls: 'bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-300' },
  IN_PROGRESS: { label: 'In Progress', cls: 'bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300' },
  COMPLETED:   { label: 'Completed',   cls: 'bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300' },
  CANCELLED:   { label: 'Cancelled',   cls: 'bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-300' },
  RESCHEDULED: { label: 'Rescheduled', cls: 'bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-300' },
};

const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
type TabId = 'upcoming' | 'pending' | 'past';

const CounsellorAppointmentsPage = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<TabId>('pending');
  const [weekOffset, setWeekOffset] = useState(0);
  const [notesModal, setNotesModal] = useState<{ id: string; notes: string } | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    appointmentService.getAppointments()
      .then(setAppointments)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  const now = new Date();
  const upcoming = appointments.filter(a => new Date(a.scheduledStartTime) >= now && a.status === 'CONFIRMED');
  const pending  = appointments.filter(a => a.status === 'SCHEDULED');
  const past     = appointments.filter(a => new Date(a.scheduledStartTime) < now || a.status === 'COMPLETED' || a.status === 'CANCELLED');
  const displayed = activeTab === 'upcoming' ? upcoming : activeTab === 'pending' ? pending : past;
  const countFor  = (id: TabId) => id === 'upcoming' ? upcoming.length : id === 'pending' ? pending.length : past.length;

  const fmt     = (iso: string) => new Date(iso).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  const fmtDate = (iso: string) => new Date(iso).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });

  const handleAccept = async (id: string) => {
    setActionLoading(id + '-accept');
    try {
      await appointmentService.confirm(id);
      setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'CONFIRMED' as Appointment['status'] } : a));
    } catch (e) {
      alert('Failed to confirm appointment.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDecline = async (id: string) => {
    setActionLoading(id + '-decline');
    try {
      await appointmentService.cancel(id, 'Declined by counsellor');
      setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'CANCELLED' as Appointment['status'] } : a));
    } catch {
      alert('Failed to decline appointment.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleComplete = async (id: string, notes: string) => {
    try {
      await appointmentService.markComplete(id, notes);
      setAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'COMPLETED' as Appointment['status'] } : a));
      setNotesModal(null);
    } catch {
      alert('Failed to mark complete.');
    }
  };

  // Weekly calendar
  const weekStart = new Date(now);
  weekStart.setDate(now.getDate() - now.getDay() + weekOffset * 7);
  const weekDays = Array.from({ length: 7 }, (_, i) => { const d = new Date(weekStart); d.setDate(weekStart.getDate() + i); return d; });
  const apptsByDay = (day: Date) => appointments.filter(a => new Date(a.scheduledStartTime).toDateString() === day.toDateString());

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <CalendarCheck size={28} className="text-teal-500" /> Appointments
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Manage your weekly schedule and student sessions.</p>
      </div>

      {/* Weekly calendar */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white">Weekly View</h2>
          <div className="flex items-center gap-3">
            <button onClick={() => setWeekOffset(w => w - 1)} className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 transition-colors"><ChevronLeft size={18} /></button>
            <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
              {weekDays[0].toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} – {weekDays[6].toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
            </span>
            <button onClick={() => setWeekOffset(w => w + 1)} className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 transition-colors"><ChevronRight size={18} /></button>
          </div>
        </div>
        <div className="grid grid-cols-7 gap-2 min-h-[160px]">
          {weekDays.map((day, i) => {
            const isToday = day.toDateString() === now.toDateString();
            const dayAppts = apptsByDay(day);
            return (
              <div key={i} className={`flex flex-col gap-1.5 ${(i === 0 || i === 6) ? 'opacity-50' : ''}`}>
                <div className="text-center pb-2 border-b border-slate-100 dark:border-slate-700/50">
                  <p className="text-xs font-bold uppercase tracking-wider text-slate-400">{DAYS[i]}</p>
                  <div className={`text-lg font-bold mt-1 w-8 h-8 mx-auto flex items-center justify-center rounded-full ${isToday ? 'bg-teal-500 text-white' : 'text-slate-700 dark:text-slate-300'}`}>{day.getDate()}</div>
                </div>
                <div className="flex flex-col gap-1">
                  {dayAppts.slice(0, 3).map(a => (
                    <div key={a.id} className="p-1.5 rounded text-[10px] font-semibold bg-teal-600 text-white">{fmt(a.scheduledStartTime)}</div>
                  ))}
                  {dayAppts.length > 3 && <p className="text-[10px] text-slate-400 text-center">+{dayAppts.length - 3}</p>}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-slate-200 dark:border-slate-700">
        {(['upcoming', 'pending', 'past'] as TabId[]).map(id => {
          const labels: Record<TabId, string> = { upcoming: 'Confirmed', pending: 'Pending Requests', past: 'Past Sessions' };
          const count = countFor(id);
          return (
            <button key={id} onClick={() => setActiveTab(id)}
              className={`flex items-center gap-2 px-4 py-3 text-sm font-semibold border-b-2 -mb-px transition-colors ${activeTab === id ? 'border-teal-500 text-teal-600 dark:text-teal-400' : 'border-transparent text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'}`}>
              {labels[id]}
              {id === 'pending' && count > 0
                ? <span className="bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">{count}</span>
                : <span className="text-xs text-slate-400">({count})</span>}
            </button>
          );
        })}
      </div>

      {/* List */}
      {loading ? (
        <div className="flex items-center justify-center py-12"><Loader2 size={28} className="animate-spin text-teal-500" /></div>
      ) : displayed.length === 0 ? (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-12 border border-slate-100 dark:border-slate-700/50 text-center">
          <CalendarCheck size={40} className="text-slate-300 dark:text-slate-600 mx-auto mb-3" />
          <p className="text-slate-500 dark:text-slate-400 font-medium">No {activeTab} appointments</p>
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-slate-50 dark:bg-slate-700/30 border-b border-slate-100 dark:border-slate-700/50">
                {['Student', 'Date & Time', 'Mode', 'Reason', 'Status', 'Actions'].map(h => (
                  <th key={h} className="py-3 px-4 text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50 dark:divide-slate-700/30">
              {displayed.map(appt => {
                const TypeIcon = TYPE_ICONS[appt.appointmentType] ?? Video;
                const cfg = STATUS_CFG[appt.status] ?? STATUS_CFG.SCHEDULED;
                const isPending = appt.status === 'SCHEDULED';
                const isConfirmedVideo = appt.status === 'CONFIRMED' && appt.appointmentType === 'VIDEO';
                const isCompleted = appt.status === 'COMPLETED';
                return (
                  <tr key={appt.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/20 transition-colors">
                    <td className="py-4 px-4">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-gradient-to-br from-teal-400/30 to-violet-400/30 flex items-center justify-center text-xs font-bold text-teal-700 dark:text-teal-300">
                          {(appt as { studentName?: string }).studentName?.[0] ?? appt.studentId?.slice(-2).toUpperCase() ?? 'ST'}
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">{(appt as { studentName?: string }).studentName ?? 'Student'}</p>
                          <p className="text-xs text-slate-400 mt-0.5">#{appt.studentId?.slice(-4)}</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      <p className="text-sm font-medium text-slate-700 dark:text-slate-300">{fmtDate(appt.scheduledStartTime)}</p>
                      <p className="text-xs text-slate-400">{fmt(appt.scheduledStartTime)} – {fmt(appt.scheduledEndTime)}</p>
                    </td>
                    <td className="py-4 px-4">
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300">
                        <TypeIcon size={12} />{appt.appointmentType?.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="py-4 px-4">
                      <p className="text-xs text-slate-500 dark:text-slate-400 max-w-[140px] truncate">{appt.reason ?? '—'}</p>
                    </td>
                    <td className="py-4 px-4">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${cfg.cls}`}>{cfg.label}</span>
                    </td>
                    <td className="py-4 px-4">
                      <div className="flex items-center gap-2">
                        {isPending && (
                          <>
                            <button onClick={() => handleDecline(appt.id)} disabled={actionLoading === appt.id + '-decline'}
                              className="px-3 py-1.5 border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-300 rounded-lg text-xs font-semibold hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-600 hover:border-red-300 transition-colors disabled:opacity-50">
                              {actionLoading === appt.id + '-decline' ? <Loader2 size={12} className="animate-spin" /> : 'Decline'}
                            </button>
                            <button onClick={() => handleAccept(appt.id)} disabled={actionLoading === appt.id + '-accept'}
                              className="px-3 py-1.5 bg-teal-600 hover:bg-teal-500 text-white rounded-lg text-xs font-semibold transition-colors disabled:opacity-50 flex items-center gap-1">
                              {actionLoading === appt.id + '-accept' ? <Loader2 size={12} className="animate-spin" /> : 'Accept'}
                            </button>
                          </>
                        )}
                        {isConfirmedVideo && (
                          <button className="px-3 py-1.5 bg-teal-600 hover:bg-teal-500 text-white rounded-lg text-xs font-semibold flex items-center gap-1 transition-colors">
                            <Video size={12} /> Start
                          </button>
                        )}
                        {!isCompleted && appt.status === 'CONFIRMED' && (
                          <button onClick={() => setNotesModal({ id: appt.id, notes: appt.counsellorNotes ?? '' })}
                            className="px-3 py-1.5 border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-300 rounded-lg text-xs font-semibold hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors flex items-center gap-1">
                            <FileText size={12} /> Complete
                          </button>
                        )}
                        {isCompleted && appt.counsellorNotes && (
                          <span className="text-xs text-slate-400 dark:text-slate-500 italic truncate max-w-[100px]">{appt.counsellorNotes}</span>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Complete / Notes modal */}
      {notesModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
          <div className="bg-white dark:bg-slate-800 rounded-2xl p-8 w-full max-w-md shadow-2xl border border-slate-100 dark:border-slate-700">
            <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-4">Complete Session</h2>
            <textarea
              value={notesModal.notes}
              onChange={e => setNotesModal(p => p ? { ...p, notes: e.target.value } : null)}
              placeholder="Add session notes (optional)…"
              rows={4}
              className="w-full bg-slate-50 dark:bg-slate-700/50 border border-slate-200 dark:border-slate-600 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none mb-4"
            />
            <div className="flex gap-3">
              <button onClick={() => setNotesModal(null)} className="flex-1 py-3 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">Cancel</button>
              <button onClick={() => handleComplete(notesModal.id, notesModal.notes)} className="flex-1 py-3 rounded-xl bg-teal-600 hover:bg-teal-500 text-white text-sm font-semibold transition-colors">Mark Complete</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CounsellorAppointmentsPage;

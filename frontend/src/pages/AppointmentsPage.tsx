import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CalendarCheck, Plus, Clock, Video, Phone, MapPin, XCircle, CheckCircle, AlertCircle, MessageSquare } from 'lucide-react';
import { apiClient } from '../services/api';
import type { Appointment } from '../types';
import { useAuthReady } from '../hooks/useAuthReady';

const STATUS_CONFIG: Record<string, { label: string; light: string; dark: string; icon: React.ElementType }> = {
  SCHEDULED:   { label:'Scheduled',   light:'bg-blue-50 text-blue-700',     dark:'dark:bg-blue-900/30 dark:text-blue-300',    icon:Clock },
  CONFIRMED:   { label:'Confirmed',   light:'bg-teal-50 text-teal-700',     dark:'dark:bg-teal-900/30 dark:text-teal-300',    icon:CheckCircle },
  IN_PROGRESS: { label:'In Progress', light:'bg-yellow-50 text-yellow-700', dark:'dark:bg-yellow-900/30 dark:text-yellow-300',icon:AlertCircle },
  COMPLETED:   { label:'Completed',   light:'bg-slate-100 text-slate-600',  dark:'dark:bg-slate-700 dark:text-slate-300',     icon:CheckCircle },
  CANCELLED:   { label:'Cancelled',   light:'bg-red-50 text-red-600',       dark:'dark:bg-red-900/30 dark:text-red-300',      icon:XCircle },
  NO_SHOW:     { label:'No Show',     light:'bg-orange-50 text-orange-600', dark:'dark:bg-orange-900/30 dark:text-orange-300',icon:XCircle },
  RESCHEDULED: { label:'Rescheduled', light:'bg-purple-50 text-purple-600', dark:'dark:bg-purple-900/30 dark:text-purple-300',icon:Clock },
};

const TYPE_ICONS: Record<string, React.ElementType> = { VIDEO:Video, PHONE:Phone, IN_PERSON:MapPin };

const AppointmentsPage = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'upcoming'|'past'>('upcoming');
  const authReady = useAuthReady();

  useEffect(() => {
    if (!authReady) return;
    apiClient.get<Appointment[] | { content?: Appointment[]; data?: Appointment[] }>('/appointments')
      .then(res => {
        const list = Array.isArray(res)
          ? res
          : (res as { content?: Appointment[]; data?: Appointment[] }).content
            ?? (res as { content?: Appointment[]; data?: Appointment[] }).data
            ?? [];
        setAppointments(list);
      })
      .catch((err: unknown) => {
        const msg = (err as { message?: string })?.message;
        setError(msg || 'Could not load appointments.');
      })
      .finally(() => setLoading(false));
  }, [authReady]);

  const now = new Date();
  const upcoming = appointments.filter(a => new Date(a.scheduledStartTime) >= now && a.status !== 'CANCELLED');
  const past     = appointments.filter(a => new Date(a.scheduledStartTime) < now || a.status === 'CANCELLED' || a.status === 'COMPLETED');
  const displayed = activeTab === 'upcoming' ? upcoming : past;

  const fmt = (iso: string, opts: Intl.DateTimeFormatOptions) => new Date(iso).toLocaleString('en-US', opts);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
            <CalendarCheck size={28} className="text-violet-500"/>Appointments
          </h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Manage your counselling sessions</p>
        </div>
        <Link to="/appointments/book"
          className="flex items-center gap-2 px-5 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl font-semibold text-sm transition-colors shadow-sm shadow-teal-500/20">
          <Plus size={18}/>Book Session
        </Link>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 dark:bg-slate-800 p-1 rounded-xl w-fit">
        {(['upcoming','past'] as const).map(tab => (
          <button key={tab} onClick={() => setActiveTab(tab)}
            className={`px-5 py-2 rounded-lg text-sm font-semibold capitalize transition-all ${
              activeTab === tab
                ? 'bg-white dark:bg-slate-700 text-teal-600 dark:text-teal-300 shadow-sm'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
            }`}>
            {tab} ({tab === 'upcoming' ? upcoming.length : past.length})
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-8 h-8 rounded-full border-4 border-teal-500 border-t-transparent animate-spin"/>
        </div>
      ) : error ? (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-2xl p-6 text-red-700 dark:text-red-300 text-sm">{error}</div>
      ) : displayed.length === 0 ? (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-12 border border-slate-100 dark:border-slate-700/50 text-center">
          <CalendarCheck size={48} className="text-slate-300 dark:text-slate-600 mx-auto mb-4"/>
          <h3 className="text-lg font-semibold text-slate-600 dark:text-slate-300 mb-2">
            {activeTab === 'upcoming' ? 'No upcoming appointments' : 'No past appointments'}
          </h3>
          <p className="text-slate-400 dark:text-slate-500 text-sm mb-6">
            {activeTab === 'upcoming' ? 'Book a session with a counsellor to get started.' : 'Your completed sessions will appear here.'}
          </p>
          {activeTab === 'upcoming' && (
            <Link to="/appointments/book"
              className="inline-flex items-center gap-2 px-6 py-3 bg-teal-500 hover:bg-teal-600 text-white rounded-xl font-semibold text-sm transition-colors">
              <Plus size={16}/>Book Your First Session
            </Link>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {displayed.map(appt => {
            const cfg = STATUS_CONFIG[appt.status] ?? STATUS_CONFIG.SCHEDULED;
            const StatusIcon = cfg.icon;
            const TypeIcon = TYPE_ICONS[appt.appointmentType] ?? Video;
            return (
              <div key={appt.id}
                className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-teal-500/10 dark:bg-teal-500/20 flex items-center justify-center flex-shrink-0">
                    <TypeIcon size={22} className="text-teal-600 dark:text-teal-400"/>
                  </div>
                  <div>
                    <p className="font-bold text-slate-800 dark:text-slate-100">{appt.appointmentType.replace('_',' ')} Session</p>
                    <div className="flex items-center gap-3 mt-1 text-sm text-slate-500 dark:text-slate-400">
                      <span>{fmt(appt.scheduledStartTime,{weekday:'short',month:'short',day:'numeric'})}</span>
                      <span>·</span>
                      <span>{fmt(appt.scheduledStartTime,{hour:'2-digit',minute:'2-digit'})} – {fmt(appt.scheduledEndTime,{hour:'2-digit',minute:'2-digit'})}</span>
                    </div>
                    {appt.reason && <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">{appt.reason}</p>}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold ${cfg.light} ${cfg.dark}`}>
                    <StatusIcon size={12}/>{cfg.label}
                  </span>
                  <button
                    onClick={() => navigate(`/messages?userId=${appt.counsellorId}`)}
                    className="flex items-center gap-1.5 px-3 py-1.5 border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 rounded-xl text-xs font-bold transition-all"
                    title="Message Counsellor">
                    <MessageSquare size={13} />
                  </button>
                  {appt.status === 'CONFIRMED' && appt.appointmentType === 'VIDEO' && (
                    <button className="px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-xs font-bold transition-colors">Join</button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default AppointmentsPage;

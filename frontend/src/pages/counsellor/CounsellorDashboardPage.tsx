import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  CalendarCheck, MessageSquare, TrendingUp,
  Video, Phone, MapPin, AlertCircle, ChevronRight,
  Users, FileText,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../services/api';
import type { Appointment } from '../../types';

const StatCard = ({ label, value, sub, icon: Icon, color }: {
  label: string; value: string | number; sub: string;
  icon: React.ElementType; color: string;
}) => (
  <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 relative overflow-hidden group">
    <div className="absolute top-4 right-4 opacity-10 group-hover:opacity-20 transition-opacity">
      <Icon size={40} className={color} />
    </div>
    <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-2">{label}</p>
    <p className={`text-4xl font-bold ${color}`}>{value}</p>
    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{sub}</p>
  </div>
);

const TYPE_ICONS: Record<string, React.ElementType> = { VIDEO: Video, PHONE: Phone, IN_PERSON: MapPin };

const CounsellorDashboardPage = () => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient.get<{ content?: Appointment[]; data?: Appointment[] }>('/appointments')
      .then(res => setAppointments(Array.isArray(res.content ?? res.data) ? (res.content ?? res.data ?? []) : []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const now = new Date();
  const today = appointments.filter(a => {
    const d = new Date(a.scheduledStartTime);
    return d.toDateString() === now.toDateString() && a.status !== 'CANCELLED';
  });
  const pending = appointments.filter(a => a.status === 'SCHEDULED');
  const upcoming = appointments.filter(a => new Date(a.scheduledStartTime) > now && a.status !== 'CANCELLED').slice(0, 5);

  const greeting = () => {
    const h = now.getHours();
    if (h < 12) return 'Good Morning';
    if (h < 17) return 'Good Afternoon';
    return 'Good Evening';
  };

  const fmt = (iso: string) => new Date(iso).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  const fmtDate = (iso: string) => {
    const d = new Date(iso);
    if (d.toDateString() === now.toDateString()) return 'Today';
    const tomorrow = new Date(now); tomorrow.setDate(now.getDate() + 1);
    if (d.toDateString() === tomorrow.toDateString()) return 'Tomorrow';
    return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400 dark:text-slate-500">
            {now.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
          </p>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white mt-1">
            {greeting()}, Dr. {user?.lastName ?? user?.firstName} 👩‍⚕️
          </h1>
        </div>
        <div className="flex gap-3 flex-wrap">
          <Link to="/counsellor/availability"
            className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm font-semibold text-slate-700 dark:text-slate-300 hover:border-teal-400 transition-all">
            <CalendarCheck size={16} /> Set Availability
          </Link>
          <Link to="/counsellor/notes"
            className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm font-semibold text-slate-700 dark:text-slate-300 hover:border-teal-400 transition-all">
            <FileText size={16} /> Add Notes
          </Link>
          <Link to="/counsellor/appointments"
            className="flex items-center gap-2 px-4 py-2 bg-teal-600 hover:bg-teal-500 text-white rounded-xl text-sm font-semibold transition-colors shadow-sm">
            <CalendarCheck size={16} /> View Schedule
          </Link>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Today's Sessions" value={today.length} sub="Scheduled today"
          icon={CalendarCheck} color="text-teal-600 dark:text-teal-400" />
        <StatCard label="Pending Requests" value={pending.length} sub="Action required"
          icon={AlertCircle} color="text-amber-600 dark:text-amber-400" />
        <StatCard label="Total Students" value="—" sub="Under your care"
          icon={Users} color="text-violet-600 dark:text-violet-400" />
        <StatCard label="This Week" value={appointments.length} sub="Total sessions"
          icon={TrendingUp} color="text-sky-600 dark:text-sky-400" />
      </div>

      {/* Main grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Upcoming appointments table */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden">
          <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50 flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <CalendarCheck size={18} className="text-teal-500" /> Upcoming Appointments
            </h2>
            <Link to="/counsellor/appointments"
              className="text-sm font-semibold text-teal-600 dark:text-teal-400 hover:underline flex items-center gap-1">
              View All <ChevronRight size={14} />
            </Link>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="w-7 h-7 rounded-full border-4 border-teal-500 border-t-transparent animate-spin" />
            </div>
          ) : upcoming.length === 0 ? (
            <div className="text-center py-12">
              <CalendarCheck size={40} className="text-slate-300 dark:text-slate-600 mx-auto mb-3" />
              <p className="text-slate-500 dark:text-slate-400 font-medium">No upcoming appointments</p>
              <p className="text-slate-400 dark:text-slate-500 text-sm mt-1">Students will appear here once they book sessions.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="bg-slate-50 dark:bg-slate-700/30 border-b border-slate-100 dark:border-slate-700/50">
                    {['Time', 'Student', 'Mode', 'Status', ''].map(h => (
                      <th key={h} className="py-3 px-5 text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50 dark:divide-slate-700/30">
                  {upcoming.map(appt => {
                    const TypeIcon = TYPE_ICONS[appt.appointmentType] ?? Video;
                    const isPending = appt.status === 'SCHEDULED';
                    const isConfirmed = appt.status === 'CONFIRMED';
                    return (
                      <tr key={appt.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/20 transition-colors">
                        <td className="py-3.5 px-5 text-sm font-medium text-slate-700 dark:text-slate-300 whitespace-nowrap">
                          <p>{fmtDate(appt.scheduledStartTime)}</p>
                          <p className="text-xs text-slate-400">{fmt(appt.scheduledStartTime)}</p>
                        </td>
                        <td className="py-3.5 px-5">
                          <div className="flex items-center gap-2">
                            <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center text-xs font-bold text-slate-600 dark:text-slate-300">
                              {appt.studentId?.slice(0, 2).toUpperCase() ?? 'ST'}
                            </div>
                            <span className="text-sm text-slate-700 dark:text-slate-300">
                              {appt.studentId ? `Student #${appt.studentId.slice(-4)}` : 'Student'}
                            </span>
                          </div>
                        </td>
                        <td className="py-3.5 px-5">
                          <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${
                            appt.appointmentType === 'VIDEO'
                              ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                              : appt.appointmentType === 'IN_PERSON'
                              ? 'bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
                              : 'bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300'
                          }`}>
                            <TypeIcon size={12} />
                            {appt.appointmentType.replace('_', ' ')}
                          </span>
                        </td>
                        <td className="py-3.5 px-5">
                          <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                            isConfirmed ? 'bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-300'
                            : isPending ? 'bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-300'
                            : 'bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300'
                          }`}>
                            <span className={`w-1.5 h-1.5 rounded-full ${isConfirmed ? 'bg-green-500' : isPending ? 'bg-yellow-500' : 'bg-slate-400'}`} />
                            {appt.status}
                          </span>
                        </td>
                        <td className="py-3.5 px-5 text-right">
                          {isPending ? (
                            <div className="flex justify-end gap-2">
                              <button className="px-3 py-1.5 border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-300 rounded-lg text-xs font-semibold hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
                                Decline
                              </button>
                              <button className="px-3 py-1.5 bg-teal-600 hover:bg-teal-500 text-white rounded-lg text-xs font-semibold transition-colors">
                                Accept
                              </button>
                            </div>
                          ) : isConfirmed && appt.appointmentType === 'VIDEO' ? (
                            <button className="px-3 py-1.5 bg-teal-600 hover:bg-teal-500 text-white rounded-lg text-xs font-semibold transition-colors">
                              Start
                            </button>
                          ) : null}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Secure messages preview */}
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 flex flex-col">
          <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50 flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-800 dark:text-white flex items-center gap-2">
              <MessageSquare size={18} className="text-sky-500" /> Messages
            </h2>
            <span className="bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 text-xs font-bold px-2 py-0.5 rounded-full">
              0 New
            </span>
          </div>
          <div className="flex-1 flex flex-col items-center justify-center p-8 text-center">
            <MessageSquare size={40} className="text-slate-300 dark:text-slate-600 mb-3" />
            <p className="text-slate-500 dark:text-slate-400 font-medium text-sm">No messages yet</p>
            <p className="text-slate-400 dark:text-slate-500 text-xs mt-1">Student messages will appear here.</p>
          </div>
          <div className="px-4 py-3 border-t border-slate-100 dark:border-slate-700/50">
            <Link to="/counsellor/messages"
              className="flex items-center justify-center gap-1 text-sm font-semibold text-teal-600 dark:text-teal-400 hover:underline w-full">
              Open Inbox <ChevronRight size={14} />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CounsellorDashboardPage;

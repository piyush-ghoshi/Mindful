import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Star, Calendar, ChevronRight, ChevronLeft, CheckCircle, Video, Phone, MapPin, Users, Loader2 } from 'lucide-react';
import { apiClient } from '../services/api';
import { useAuthReady } from '../hooks/useAuthReady';

interface Counsellor {
  id: string;
  firstName: string;
  lastName: string;
  email?: string;
  bio?: string;
  specializations?: string[];
  yearsOfExperience?: number;
  rating?: number;
  isAcceptingNewStudents?: boolean;
}

const TIME_SLOTS = [
  '09:00 AM','09:30 AM','10:00 AM','10:30 AM','11:00 AM','11:30 AM',
  '02:00 PM','02:30 PM','03:00 PM','03:30 PM','04:00 PM','04:30 PM',
];
const APPT_TYPES = [
  { id: 'VIDEO',     label: 'Video Call', icon: Video },
  { id: 'PHONE',     label: 'Phone Call', icon: Phone },
  { id: 'IN_PERSON', label: 'In Person',  icon: MapPin },
];
const STEPS = ['Choose Counsellor', 'Select Time', 'Confirm Details'];

const inputCls = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

const BookAppointmentPage = () => {
  const navigate = useNavigate();
  const authReady = useAuthReady();
  const [step, setStep] = useState(0);
  const [counsellors, setCounsellors] = useState<Counsellor[]>([]);
  const [loadingCounsellors, setLoadingCounsellors] = useState(true);
  const [fetchError, setFetchError] = useState('');
  const [selected, setSelected] = useState<Counsellor | null>(null);
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [type, setType] = useState('VIDEO');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [booked, setBooked] = useState(false);
  const [bookError, setBookError] = useState('');

  useEffect(() => {
    if (!authReady) return;
    setLoadingCounsellors(true);
    setFetchError('');
    apiClient.get<Counsellor[] | { content?: Counsellor[]; data?: Counsellor[] }>('/users/counsellors')
      .then(res => {
        const list = Array.isArray(res)
          ? res
          : (res as { content?: Counsellor[]; data?: Counsellor[] }).content
            ?? (res as { content?: Counsellor[]; data?: Counsellor[] }).data
            ?? [];
        setCounsellors(list);
      })
      .catch(() => setFetchError('Could not load counsellors. Make sure the backend is running.'))
      .finally(() => setLoadingCounsellors(false));
  }, [authReady]);

  const handleBook = async () => {
    if (!selected || !date || !time) return;
    setBookError('');
    try {
      setSubmitting(true);
      const [h, mp] = time.split(':');
      const [m, p] = mp.split(' ');
      let hr = parseInt(h);
      if (p === 'PM' && hr !== 12) hr += 12;
      if (p === 'AM' && hr === 12) hr = 0;
      const start = new Date(`${date}T${String(hr).padStart(2, '0')}:${m}:00`);
      await apiClient.post('/appointments', {
        counsellorId: selected.id,
        startTime: start.toISOString(),
        appointmentType: type,
        reason,
      });
      setBooked(true);
    } catch (err: unknown) {
      const msg = (err as { message?: string })?.message;
      setBookError(msg || 'Booking failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (booked) return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-12 border border-slate-100 dark:border-slate-700/50 text-center max-w-md w-full shadow-lg">
        <CheckCircle size={56} className="text-teal-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-slate-800 dark:text-white mb-2">Appointment Booked!</h2>
        <p className="text-slate-500 dark:text-slate-400 mb-2">
          Session with <strong className="text-slate-700 dark:text-slate-200">{selected?.firstName} {selected?.lastName}</strong> scheduled.
        </p>
        <p className="text-sm text-slate-400 dark:text-slate-500 mb-8">{date} at {time} · {type.replace('_', ' ')}</p>
        <button onClick={() => navigate('/appointments')}
          className="w-full bg-teal-500 hover:bg-teal-600 text-white font-semibold py-3 rounded-xl transition-colors">
          View My Appointments
        </button>
      </div>
    </div>
  );

  return (
    <div className="space-y-8">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white">Schedule a Session</h1>
        <p className="text-slate-500 dark:text-slate-400 mt-2">Your well-being is our priority.</p>
      </div>

      {/* Steps */}
      <div className="relative flex items-center justify-between max-w-lg mx-auto">
        <div className="absolute left-0 top-5 w-full h-0.5 bg-slate-200 dark:bg-slate-700 z-0" />
        {STEPS.map((label, i) => (
          <div key={label} className="relative z-10 flex flex-col items-center gap-2">
            <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ring-4 ring-[#f9f9ff] dark:ring-slate-950 transition-all ${
              i < step ? 'bg-teal-600 text-white'
              : i === step ? 'bg-teal-500 text-white shadow-md shadow-teal-500/30'
              : 'bg-white dark:bg-slate-800 border-2 border-slate-200 dark:border-slate-700 text-slate-400'
            }`}>
              {i < step ? <CheckCircle size={18} /> : i + 1}
            </div>
            <span className={`text-xs font-semibold ${i <= step ? 'text-teal-600 dark:text-teal-400' : 'text-slate-400 dark:text-slate-500'}`}>{label}</span>
          </div>
        ))}
      </div>

      {/* Step 1 — Choose Counsellor */}
      {step === 0 && (
        <div>
          <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-4">Available Counsellors</h2>

          {loadingCounsellors ? (
            <div className="flex items-center justify-center py-20">
              <Loader2 size={32} className="animate-spin text-teal-500" />
            </div>
          ) : fetchError ? (
            <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-2xl p-6 text-red-700 dark:text-red-300 text-sm text-center">
              {fetchError}
            </div>
          ) : counsellors.length === 0 ? (
            <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-16 border border-slate-100 dark:border-slate-700/50 text-center">
              <Users size={48} className="text-slate-300 dark:text-slate-600 mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-slate-600 dark:text-slate-300 mb-2">No counsellors registered yet</h3>
              <p className="text-slate-400 dark:text-slate-500 text-sm">
                Counsellors will appear here once they create an account. Check back soon.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {counsellors
                .filter(c => c.isAcceptingNewStudents !== false)
                .map(c => (
                  <div key={c.id}
                    onClick={() => { setSelected(c); setStep(1); }}
                    className={`bg-white dark:bg-slate-800/60 rounded-2xl p-6 border-2 cursor-pointer transition-all hover:shadow-lg hover:shadow-black/5 dark:hover:shadow-black/20 ${
                      selected?.id === c.id
                        ? 'border-teal-500'
                        : 'border-slate-100 dark:border-slate-700/50 hover:border-teal-400 dark:hover:border-teal-600'
                    }`}>
                    <div className="flex items-start justify-between mb-4">
                      <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-teal-400/30 to-emerald-400/30 dark:from-teal-500/20 dark:to-emerald-500/20 flex items-center justify-center text-xl font-bold text-teal-700 dark:text-teal-300">
                        {(c.firstName?.[0] ?? '?')}{(c.lastName?.[0] ?? '?')}
                      </div>
                      {c.rating != null && (
                        <div className="flex items-center gap-1 bg-slate-50 dark:bg-slate-700/60 px-2 py-1 rounded-lg">
                          <Star size={14} className="text-amber-400 fill-amber-400" />
                          <span className="text-sm font-bold text-slate-800 dark:text-slate-200">{c.rating.toFixed(1)}</span>
                        </div>
                      )}
                    </div>
                    <h3 className="text-lg font-bold text-slate-800 dark:text-white">{c.firstName} {c.lastName}</h3>
                    {c.bio && <p className="text-sm text-slate-500 dark:text-slate-400 mb-3 line-clamp-2">{c.bio}</p>}
                    {(c.specializations ?? []).length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mb-4">
                        {(c.specializations ?? []).map(s => (
                          <span key={s} className="bg-slate-100 dark:bg-slate-700/60 text-slate-600 dark:text-slate-300 text-xs px-2.5 py-1 rounded-full font-medium">{s}</span>
                        ))}
                      </div>
                    )}
                    {c.yearsOfExperience != null && c.yearsOfExperience > 0 && (
                      <div className="flex items-center gap-2 text-teal-600 dark:text-teal-400 text-sm mb-4">
                        <Calendar size={14} />
                        <span className="font-medium">{c.yearsOfExperience} years experience</span>
                      </div>
                    )}
                    <button className="w-full bg-teal-500 hover:bg-teal-600 text-white font-semibold py-2.5 rounded-xl transition-colors text-sm">
                      Select Counsellor
                    </button>
                  </div>
                ))}
            </div>
          )}
        </div>
      )}

      {/* Step 2 — Select Time */}
      {step === 1 && (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50 max-w-2xl mx-auto">
          <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-6">
            Select Date & Time with {selected?.firstName} {selected?.lastName}
          </h2>
          <div className="mb-6">
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-2">Date</label>
            <input type="date" value={date} min={new Date().toISOString().split('T')[0]}
              onChange={e => setDate(e.target.value)} className={inputCls} />
          </div>
          <div className="mb-6">
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-3">Available Times</label>
            <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
              {TIME_SLOTS.map(slot => (
                <button key={slot} type="button" onClick={() => setTime(slot)}
                  className={`py-2.5 rounded-xl text-sm font-medium transition-all ${
                    time === slot
                      ? 'bg-teal-500 text-white shadow-sm'
                      : 'bg-slate-50 dark:bg-slate-700/50 text-slate-600 dark:text-slate-300 hover:bg-teal-50 dark:hover:bg-teal-900/30 hover:text-teal-700 dark:hover:text-teal-300 border border-slate-200 dark:border-slate-700'
                  }`}>{slot}</button>
              ))}
            </div>
          </div>
          <div className="mb-8">
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-3">Session Type</label>
            <div className="grid grid-cols-3 gap-3">
              {APPT_TYPES.map(({ id, label, icon: Icon }) => (
                <button key={id} type="button" onClick={() => setType(id)}
                  className={`flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-all ${
                    type === id
                      ? 'border-teal-500 bg-teal-50 dark:bg-teal-900/30 text-teal-700 dark:text-teal-300'
                      : 'border-slate-200 dark:border-slate-700 text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-slate-600'
                  }`}>
                  <Icon size={20} /><span className="text-xs font-semibold">{label}</span>
                </button>
              ))}
            </div>
          </div>
          <div className="flex gap-3">
            <button onClick={() => setStep(0)}
              className="flex items-center gap-2 px-5 py-3 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
              <ChevronLeft size={16} />Back
            </button>
            <button onClick={() => setStep(2)} disabled={!date || !time}
              className="flex-1 flex items-center justify-center gap-2 bg-teal-500 hover:bg-teal-600 text-white font-semibold py-3 rounded-xl transition-colors disabled:opacity-50">
              Continue<ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}

      {/* Step 3 — Confirm */}
      {step === 2 && (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50 max-w-2xl mx-auto">
          <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-6">Confirm Your Booking</h2>
          <div className="bg-slate-50 dark:bg-slate-700/40 rounded-xl p-5 mb-6 space-y-3">
            {[
              { label: 'Counsellor', value: `${selected?.firstName} ${selected?.lastName}` },
              { label: 'Date', value: date ? new Date(date).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' }) : '' },
              { label: 'Time', value: time },
              { label: 'Session Type', value: type.replace('_', ' ') },
            ].map(({ label, value }) => (
              <div key={label} className="flex justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400 font-medium">{label}</span>
                <span className="text-slate-800 dark:text-slate-100 font-bold">{value}</span>
              </div>
            ))}
          </div>
          <div className="mb-8">
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-2">
              Reason for Visit <span className="text-slate-400 font-normal">(optional)</span>
            </label>
            <textarea value={reason} onChange={e => setReason(e.target.value)}
              placeholder="Briefly describe what you'd like to discuss…" rows={3}
              className={`${inputCls} resize-none`} />
          </div>
          {bookError && (
            <div className="mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">
              {bookError}
            </div>
          )}
          <div className="flex gap-3">
            <button onClick={() => setStep(1)}
              className="flex items-center gap-2 px-5 py-3 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
              <ChevronLeft size={16} />Back
            </button>
            <button onClick={handleBook} disabled={submitting}
              className="flex-1 flex items-center justify-center gap-2 bg-teal-500 hover:bg-teal-600 text-white font-semibold py-3 rounded-xl transition-colors disabled:opacity-60 shadow-sm">
              {submitting ? <><Loader2 size={16} className="animate-spin" />Booking…</> : 'Confirm Booking'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookAppointmentPage;

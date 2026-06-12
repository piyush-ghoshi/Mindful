import { useState, useEffect } from 'react';
import { CalendarCheck, Save, CheckCircle, Loader2 } from 'lucide-react';
import { apiClient } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
const DAY_MAP: Record<string, string> = {
  Monday: 'MONDAY', Tuesday: 'TUESDAY', Wednesday: 'WEDNESDAY',
  Thursday: 'THURSDAY', Friday: 'FRIDAY', Saturday: 'SATURDAY', Sunday: 'SUNDAY',
};

interface DaySchedule { enabled: boolean; start: string; end: string; }

const DEFAULT: Record<string, DaySchedule> = {
  Monday:    { enabled: true,  start: '09:00', end: '17:00' },
  Tuesday:   { enabled: true,  start: '09:00', end: '17:00' },
  Wednesday: { enabled: false, start: '09:00', end: '17:00' },
  Thursday:  { enabled: true,  start: '09:00', end: '14:00' },
  Friday:    { enabled: true,  start: '09:00', end: '17:00' },
  Saturday:  { enabled: false, start: '09:00', end: '13:00' },
  Sunday:    { enabled: false, start: '09:00', end: '13:00' },
};

const CounsellorAvailabilityPage = () => {
  const { user } = useAuth();
  const [schedule, setSchedule] = useState<Record<string, DaySchedule>>(DEFAULT);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState('');
  const [leaveStart, setLeaveStart] = useState('');
  const [leaveEnd, setLeaveEnd] = useState('');
  const [addingLeave, setAddingLeave] = useState(false);

  // Load existing schedule on mount
  useEffect(() => {
    if (!user?.id) return;
    apiClient.get<{ startTime: string; endTime: string; dayOfWeek: string; isAvailable: boolean }[]>(
      `/counsellors/${user.id}/schedules`
    ).then(schedules => {
      if (!Array.isArray(schedules) || schedules.length === 0) return;
      const updated = { ...DEFAULT };
      const reverseMap: Record<string, string> = Object.fromEntries(
        Object.entries(DAY_MAP).map(([k, v]) => [v, k])
      );
      schedules.forEach(s => {
        const day = reverseMap[s.dayOfWeek];
        if (day) {
          updated[day] = {
            enabled: s.isAvailable,
            start: s.startTime?.slice(0, 5) ?? '09:00',
            end: s.endTime?.slice(0, 5) ?? '17:00',
          };
        }
      });
      setSchedule(updated);
    }).catch(() => { /* use defaults if no schedule set yet */ });
  }, [user?.id]);

  const toggle = (day: string) =>
    setSchedule(p => ({ ...p, [day]: { ...p[day], enabled: !p[day].enabled } }));

  const update = (day: string, field: 'start' | 'end', val: string) =>
    setSchedule(p => ({ ...p, [day]: { ...p[day], [field]: val } }));

  const handleSave = async () => {
    if (!user?.id) return;
    setSaving(true); setError('');
    try {
      // Build the DTO the backend expects: AvailabilityScheduleDto
      const scheduleDto = {
        monday: schedule['Monday'].enabled ? [{ startTime: schedule['Monday'].start + ':00', endTime: schedule['Monday'].end + ':00' }] : [],
        tuesday: schedule['Tuesday'].enabled ? [{ startTime: schedule['Tuesday'].start + ':00', endTime: schedule['Tuesday'].end + ':00' }] : [],
        wednesday: schedule['Wednesday'].enabled ? [{ startTime: schedule['Wednesday'].start + ':00', endTime: schedule['Wednesday'].end + ':00' }] : [],
        thursday: schedule['Thursday'].enabled ? [{ startTime: schedule['Thursday'].start + ':00', endTime: schedule['Thursday'].end + ':00' }] : [],
        friday: schedule['Friday'].enabled ? [{ startTime: schedule['Friday'].start + ':00', endTime: schedule['Friday'].end + ':00' }] : [],
        saturday: schedule['Saturday'].enabled ? [{ startTime: schedule['Saturday'].start + ':00', endTime: schedule['Saturday'].end + ':00' }] : [],
        sunday: schedule['Sunday'].enabled ? [{ startTime: schedule['Sunday'].start + ':00', endTime: schedule['Sunday'].end + ':00' }] : [],
      };
      await apiClient.put(`/counsellors/${user.id}/availability`, scheduleDto);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (e) {
      setError('Failed to save. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleAddLeave = async () => {
    if (!user?.id || !leaveStart || !leaveEnd) return;
    setAddingLeave(true);
    try {
      // Add leave for each date in the range
      const start = new Date(leaveStart);
      const end = new Date(leaveEnd);
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const dateStr = d.toISOString().split('T')[0];
        await apiClient.post(`/counsellors/${user.id}/time-off?date=${dateStr}&reason=Leave`, {});
      }
      setLeaveStart(''); setLeaveEnd('');
      alert('Leave period saved!');
    } catch {
      setError('Failed to save leave period.');
    } finally {
      setAddingLeave(false);
    }
  };

  const inputCls = 'bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-lg px-3 py-2 text-sm text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <CalendarCheck size={28} className="text-teal-500" /> Availability Settings
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Set your weekly schedule so students can book sessions.</p>
      </div>

      {saved && (
        <div className="p-4 rounded-xl bg-teal-50 dark:bg-teal-900/30 border border-teal-200 dark:border-teal-700 text-teal-700 dark:text-teal-300 text-sm font-medium flex items-center gap-2">
          <CheckCircle size={16} /> Availability saved successfully!
        </div>
      )}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">{error}</div>
      )}

      {/* Weekly schedule */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 p-6">
        <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5">Standard Week</h2>
        <div className="space-y-4">
          {DAYS.map(day => {
            const s = schedule[day];
            return (
              <div key={day} className={`flex items-center gap-4 p-3 rounded-xl transition-all ${s.enabled ? 'bg-slate-50 dark:bg-slate-700/30' : 'opacity-50'}`}>
                <button onClick={() => toggle(day)} className={`relative w-11 h-6 rounded-full transition-colors flex-shrink-0 ${s.enabled ? 'bg-teal-500' : 'bg-slate-300 dark:bg-slate-600'}`}>
                  <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${s.enabled ? 'translate-x-5' : 'translate-x-0'}`} />
                </button>
                <span className="w-24 text-sm font-semibold text-slate-700 dark:text-slate-300">{day}</span>
                {s.enabled ? (
                  <div className="flex items-center gap-2 flex-1">
                    <input type="time" value={s.start} onChange={e => update(day, 'start', e.target.value)} className={inputCls} />
                    <span className="text-slate-400 text-sm">to</span>
                    <input type="time" value={s.end} onChange={e => update(day, 'end', e.target.value)} className={inputCls} />
                  </div>
                ) : (
                  <span className="text-sm text-slate-400 italic flex-1">Off</span>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Leave / exceptions */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-slate-100 dark:border-slate-700/50 p-6">
        <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-4">Mark as On Leave</h2>
        <div className="grid grid-cols-2 gap-4 mb-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-300">From</label>
            <input type="date" value={leaveStart} onChange={e => setLeaveStart(e.target.value)} min={new Date().toISOString().split('T')[0]} className={inputCls} />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-300">To</label>
            <input type="date" value={leaveEnd} onChange={e => setLeaveEnd(e.target.value)} min={leaveStart || new Date().toISOString().split('T')[0]} className={inputCls} />
          </div>
        </div>
        {leaveStart && leaveEnd && (
          <p className="text-sm text-amber-600 dark:text-amber-400 mb-4 font-medium">
            ⚠️ Students won't be able to book from {leaveStart} to {leaveEnd}.
          </p>
        )}
        <button onClick={handleAddLeave} disabled={!leaveStart || !leaveEnd || addingLeave}
          className="px-5 py-2.5 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-xl text-sm font-semibold hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors disabled:opacity-50 flex items-center gap-2">
          {addingLeave ? <Loader2 size={14} className="animate-spin" /> : null}
          Add Leave Period
        </button>
      </div>

      {/* Save */}
      <button onClick={handleSave} disabled={saving}
        className="flex items-center gap-2 px-8 py-3.5 bg-teal-600 hover:bg-teal-500 text-white font-bold rounded-xl transition-colors shadow-sm shadow-teal-500/20 disabled:opacity-60">
        {saving ? <Loader2 size={18} className="animate-spin" /> : <Save size={18} />}
        {saving ? 'Saving…' : 'Save Availability'}
      </button>
    </div>
  );
};

export default CounsellorAvailabilityPage;

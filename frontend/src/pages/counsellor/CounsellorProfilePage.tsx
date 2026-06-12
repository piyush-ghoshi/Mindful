import { useState, useEffect } from 'react';
import {
  Stethoscope, Save, CheckCircle, Plus, X, Upload,
  Award, BookOpen, Star, FileText, Loader2, AlertCircle,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../services/api';

const SPECIALIZATIONS = [
  'Anxiety & Stress', 'Depression', 'Grief & Loss', 'Trauma & PTSD',
  'Academic Pressure', 'Relationship Issues', 'Self-Esteem', 'Addiction',
  'Eating Disorders', 'ADHD', 'Social Anxiety', 'Career Counselling',
  'Family Conflict', 'Burnout', 'Crisis Intervention', 'LGBTQ+ Issues',
];

const inputCls = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

interface CounsellorProfileData {
  licenseNumber: string;
  bio: string;
  yearsOfExperience: number;
  specializations: string[];
  qualifications: string[];
  appointmentDuration: number;
  maxAppointmentsPerDay: number;
  isAcceptingNewStudents: boolean;
}

const DEFAULT: CounsellorProfileData = {
  licenseNumber: '',
  bio: '',
  yearsOfExperience: 0,
  specializations: [],
  qualifications: [],
  appointmentDuration: 30,
  maxAppointmentsPerDay: 8,
  isAcceptingNewStudents: true,
};

const CounsellorProfilePage = () => {
  const { user } = useAuth();
  const [form, setForm] = useState<CounsellorProfileData>(DEFAULT);
  const [newQual, setNewQual] = useState('');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  // Load existing profile
  useEffect(() => {
    if (!user?.id) return;
    apiClient.get<CounsellorProfileData>(`/users/counsellors/${user.id}`)
      .then(data => {
        setForm({
          licenseNumber: data.licenseNumber ?? '',
          bio: data.bio ?? '',
          yearsOfExperience: data.yearsOfExperience ?? 0,
          specializations: Array.isArray(data.specializations) ? data.specializations : [],
          qualifications: Array.isArray(data.qualifications) ? data.qualifications : [],
          appointmentDuration: data.appointmentDuration ?? 30,
          maxAppointmentsPerDay: data.maxAppointmentsPerDay ?? 8,
          isAcceptingNewStudents: data.isAcceptingNewStudents ?? true,
        });
      })
      .catch(() => { /* use defaults — profile not yet created */ })
      .finally(() => setLoading(false));
  }, [user?.id]);

  const toggleSpec = (s: string) =>
    setForm(p => ({
      ...p,
      specializations: p.specializations.includes(s)
        ? p.specializations.filter(x => x !== s)
        : [...p.specializations, s],
    }));

  const addQual = () => {
    if (!newQual.trim()) return;
    setForm(p => ({ ...p, qualifications: [...p.qualifications, newQual.trim()] }));
    setNewQual('');
  };

  const removeQual = (i: number) =>
    setForm(p => ({ ...p, qualifications: p.qualifications.filter((_, idx) => idx !== i) }));

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.licenseNumber.trim()) { setError('License number is required.'); return; }
    if (form.bio.length < 20) { setError('Bio must be at least 20 characters.'); return; }
    setSaving(true); setError('');
    try {
      await apiClient.put(`/users/${user?.id}`, {
        firstName: user?.firstName,
        lastName: user?.lastName,
        // Counsellor-specific fields stored via counsellor profile endpoint
      });
      // Update counsellor profile
      await apiClient.put(`/users/counsellors/${user?.id}/availability`, {
        maxAppointmentsPerDay: form.maxAppointmentsPerDay,
        appointmentDuration: form.appointmentDuration,
        isAcceptingNewStudents: form.isAcceptingNewStudents,
        bio: form.bio,
        licenseNumber: form.licenseNumber,
        specializations: form.specializations,
        qualifications: form.qualifications,
        yearsOfExperience: form.yearsOfExperience,
      });
      setSaved(true); setTimeout(() => setSaved(false), 3000);
    } catch {
      setError('Failed to save profile. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center py-24">
      <Loader2 size={32} className="animate-spin text-teal-500" />
    </div>
  );

  return (
    <div className="space-y-8 max-w-3xl">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <Stethoscope size={28} className="text-teal-500" /> Professional Profile
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">
          Your public profile visible to students. Complete it to build trust and get more bookings.
        </p>
      </div>

      {saved && (
        <div className="flex items-center gap-2 p-4 rounded-xl bg-teal-50 dark:bg-teal-900/30 border border-teal-200 dark:border-teal-700 text-teal-700 dark:text-teal-300 text-sm font-medium">
          <CheckCircle size={16} /> Profile saved successfully!
        </div>
      )}
      {error && (
        <div className="flex items-center gap-2 p-4 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">
          <AlertCircle size={16} /> {error}
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">

        {/* ── Identity ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5 flex items-center gap-2">
            <Award size={18} className="text-teal-500" /> Professional Identity
          </h2>

          {/* Profile card preview */}
          <div className="flex items-center gap-4 p-4 bg-slate-50 dark:bg-slate-700/40 rounded-xl mb-6">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center text-white text-2xl font-bold flex-shrink-0">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
            <div>
              <p className="font-bold text-slate-800 dark:text-white">{user?.firstName} {user?.lastName}</p>
              <p className="text-sm text-teal-600 dark:text-teal-400 font-medium">
                {form.specializations[0] ?? 'Counsellor'}{form.specializations.length > 1 ? ` +${form.specializations.length - 1} more` : ''}
              </p>
              <div className="flex items-center gap-1 mt-1">
                {[1,2,3,4,5].map(i => <Star key={i} size={12} className="text-amber-400 fill-amber-400" />)}
                <span className="text-xs text-slate-500 dark:text-slate-400 ml-1">New profile</span>
              </div>
            </div>
            <div className="ml-auto text-right text-xs">
              <span className={`px-2 py-1 rounded-full font-semibold ${form.isAcceptingNewStudents ? 'bg-teal-100 text-teal-700 dark:bg-teal-900/40 dark:text-teal-300' : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400'}`}>
                {form.isAcceptingNewStudents ? 'Accepting Students' : 'Closed'}
              </span>
            </div>
          </div>

          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">License / Registration Number *</label>
                <input value={form.licenseNumber} onChange={e => setForm(p => ({ ...p, licenseNumber: e.target.value }))}
                  placeholder="e.g. RCI/2019/12345" className={inputCls} required />
                <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">Issued by RCI, NIMHANS, or your state board.</p>
              </div>
              <div>
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Years of Experience</label>
                <input type="number" min={0} max={50} value={form.yearsOfExperience}
                  onChange={e => setForm(p => ({ ...p, yearsOfExperience: Number(e.target.value) }))} className={inputCls} />
              </div>
            </div>

            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Professional Bio</label>
              <textarea value={form.bio} onChange={e => setForm(p => ({ ...p, bio: e.target.value }))}
                placeholder="Introduce yourself to students — your approach, philosophy, and what you specialise in. Be warm and approachable."
                rows={4} maxLength={600} className={`${inputCls} resize-none`} />
              <div className="flex justify-between text-xs text-slate-400 mt-1">
                <span>Minimum 20 characters. Be specific about your approach.</span>
                <span>{form.bio.length}/600</span>
              </div>
            </div>

            <div className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-700/40 rounded-xl">
              <div>
                <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">Accepting New Students</p>
                <p className="text-xs text-slate-500 dark:text-slate-400">Toggle off to temporarily close bookings.</p>
              </div>
              <button type="button" onClick={() => setForm(p => ({ ...p, isAcceptingNewStudents: !p.isAcceptingNewStudents }))}
                className={`relative w-12 h-6 rounded-full transition-colors ${form.isAcceptingNewStudents ? 'bg-teal-500' : 'bg-slate-300 dark:bg-slate-600'}`}>
                <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${form.isAcceptingNewStudents ? 'translate-x-6' : ''}`} />
              </button>
            </div>
          </div>
        </section>

        {/* ── Specialisations ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-2 flex items-center gap-2">
            <Star size={18} className="text-amber-500" /> Areas of Specialisation
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">Select all that apply. Students use these to find the right match.</p>
          <div className="flex flex-wrap gap-2">
            {SPECIALIZATIONS.map(s => (
              <button key={s} type="button" onClick={() => toggleSpec(s)}
                className={`px-3.5 py-2 rounded-xl text-xs font-semibold transition-all border ${
                  form.specializations.includes(s)
                    ? 'bg-teal-500 text-white border-teal-500 shadow-sm'
                    : 'bg-white dark:bg-slate-700/50 text-slate-600 dark:text-slate-300 border-slate-200 dark:border-slate-600 hover:border-teal-400'
                }`}>
                {s}
              </button>
            ))}
          </div>
          {form.specializations.length > 0 && (
            <p className="text-xs text-teal-600 dark:text-teal-400 mt-3 font-medium">{form.specializations.length} selected</p>
          )}
        </section>

        {/* ── Qualifications ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-2 flex items-center gap-2">
            <BookOpen size={18} className="text-violet-500" /> Qualifications & Credentials
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">Add your degrees, certifications, and training programs.</p>

          <div className="space-y-2 mb-4">
            {form.qualifications.map((q, i) => (
              <div key={i} className="flex items-center gap-3 p-3 bg-slate-50 dark:bg-slate-700/40 rounded-xl group">
                <FileText size={14} className="text-violet-500 flex-shrink-0" />
                <span className="text-sm text-slate-700 dark:text-slate-300 flex-1">{q}</span>
                <button type="button" onClick={() => removeQual(i)}
                  className="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-red-50 dark:hover:bg-red-900/20 text-slate-400 hover:text-red-500 transition-all">
                  <X size={14} />
                </button>
              </div>
            ))}
            {form.qualifications.length === 0 && (
              <p className="text-sm text-slate-400 dark:text-slate-500 italic py-2">No qualifications added yet.</p>
            )}
          </div>

          <div className="flex gap-3">
            <input value={newQual} onChange={e => setNewQual(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), addQual())}
              placeholder="e.g. M.A. Psychology, NIMHANS (2018)" className={`${inputCls} flex-1`} />
            <button type="button" onClick={addQual}
              className="px-4 py-2.5 bg-violet-500 hover:bg-violet-600 text-white rounded-xl text-sm font-semibold transition-colors flex items-center gap-1.5 flex-shrink-0">
              <Plus size={16} /> Add
            </button>
          </div>

          {/* Document upload placeholder */}
          <div className="mt-4 p-4 rounded-xl border-2 border-dashed border-slate-200 dark:border-slate-700 text-center">
            <Upload size={20} className="text-slate-400 dark:text-slate-500 mx-auto mb-2" />
            <p className="text-sm text-slate-500 dark:text-slate-400 font-medium">Upload credential documents</p>
            <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">PDF, JPG or PNG · Max 5MB per file</p>
            <p className="text-xs text-amber-600 dark:text-amber-400 mt-2 font-medium">
              📎 Document upload coming soon — credentials verified manually by our team for now.
            </p>
          </div>
        </section>

        {/* ── Session Settings ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5">Session Preferences</h2>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Session Duration (minutes)</label>
              <select value={form.appointmentDuration} onChange={e => setForm(p => ({ ...p, appointmentDuration: Number(e.target.value) }))} className={inputCls}>
                {[30, 45, 60, 90].map(d => <option key={d} value={d}>{d} minutes</option>)}
              </select>
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Max Sessions Per Day</label>
              <select value={form.maxAppointmentsPerDay} onChange={e => setForm(p => ({ ...p, maxAppointmentsPerDay: Number(e.target.value) }))} className={inputCls}>
                {[4, 6, 8, 10, 12].map(n => <option key={n} value={n}>{n} sessions</option>)}
              </select>
            </div>
          </div>
        </section>

        {/* ── Save ── */}
        <button type="submit" disabled={saving}
          className="flex items-center gap-2 px-8 py-3.5 bg-teal-600 hover:bg-teal-500 text-white font-bold rounded-xl transition-colors shadow-sm shadow-teal-500/20 disabled:opacity-60">
          {saving ? <Loader2 size={18} className="animate-spin" /> : <Save size={18} />}
          {saving ? 'Saving…' : 'Save Profile'}
        </button>
      </form>
    </div>
  );
};

export default CounsellorProfilePage;

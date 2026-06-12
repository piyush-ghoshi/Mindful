import { useState, useEffect } from 'react';
import {
  User, GraduationCap, BookOpen, Heart, Save,
  CheckCircle, AlertCircle, Loader2, Camera,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../services/api';

const inputCls = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

const GENDER_OPTIONS = ['Prefer not to say', 'Male', 'Female', 'Non-binary', 'Other'];
const YEAR_OPTIONS   = ['1st Year', '2nd Year', '3rd Year', '4th Year', '5th Year', 'Postgraduate', 'PhD'];

const WELLNESS_GOAL_TAGS = [
  'Reduce anxiety', 'Manage stress', 'Improve sleep',
  'Build confidence', 'Overcome loneliness', 'Academic performance',
  'Grief support', 'Better relationships', 'Work-life balance',
  'Self-discovery', 'Addiction support', 'Trauma healing',
];

interface StudentProfileForm {
  // Personal
  firstName: string;
  lastName: string;
  gender: string;
  dateOfBirth: string;
  // Academic
  institution: string;
  course: string;
  yearOfStudy: string;
  studentIdNo: string;
  // Bio (≤100 words)
  bio: string;
  // Wellness goals
  wellnessGoals: string[];
  // Emergency contact
  emergencyContactName: string;
  emergencyContactPhone: string;
  // Preferences
  preferredCounsellorGender: string;
  consentForDataSharing: boolean;
}

const countWords = (text: string) =>
  text.trim() === '' ? 0 : text.trim().split(/\s+/).length;

const StudentProfileSetupPage = () => {
  const { user, updateProfile } = useAuth();

  const [form, setForm] = useState<StudentProfileForm>({
    firstName: user?.firstName ?? '',
    lastName:  user?.lastName  ?? '',
    gender: 'Prefer not to say',
    dateOfBirth: '',
    institution: '',
    course: '',
    yearOfStudy: '1st Year',
    studentIdNo: '',
    bio: '',
    wellnessGoals: [],
    emergencyContactName: '',
    emergencyContactPhone: '',
    preferredCounsellorGender: 'No preference',
    consentForDataSharing: false,
  });

  const [saving, setSaving]   = useState(false);
  const [saved,  setSaved]    = useState(false);
  const [error,  setError]    = useState('');
  const [loading, setLoading] = useState(true);

  const wordCount = countWords(form.bio);
  const bioOver   = wordCount > 100;

  // Load saved profile on mount
  useEffect(() => {
    if (!user?.id) return;
    apiClient.get<Partial<StudentProfileForm>>(`/users/${user.id}`)
      .then(data => {
        setForm(prev => ({
          ...prev,
          firstName:  (data as { firstName?: string }).firstName  ?? prev.firstName,
          lastName:   (data as { lastName?: string }).lastName   ?? prev.lastName,
        }));
      })
      .catch(() => {})
      .finally(() => setLoading(false));

    // Try to load student-specific profile
    apiClient.get<Partial<StudentProfileForm>>(`/users/students/${user.id}/profile`)
      .then(data => {
        setForm(prev => ({
          ...prev,
          ...(data.gender              ? { gender:              data.gender }              : {}),
          ...(data.dateOfBirth         ? { dateOfBirth:         data.dateOfBirth }         : {}),
          ...(data.emergencyContactName  ? { emergencyContactName:  data.emergencyContactName }  : {}),
          ...(data.emergencyContactPhone ? { emergencyContactPhone: data.emergencyContactPhone } : {}),
        }));
      })
      .catch(() => {});
  }, [user?.id]);

  const set = (field: keyof StudentProfileForm, value: unknown) =>
    setForm(prev => ({ ...prev, [field]: value }));

  const toggleGoal = (goal: string) =>
    set('wellnessGoals', form.wellnessGoals.includes(goal)
      ? form.wellnessGoals.filter(g => g !== goal)
      : [...form.wellnessGoals, goal]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (bioOver) { setError('Bio must be 100 words or fewer.'); return; }
    if (!form.institution.trim()) { setError('Institution name is required.'); return; }
    setSaving(true); setError('');
    try {
      // Update base user profile (name)
      await updateProfile({ firstName: form.firstName, lastName: form.lastName });
      // Update student-specific profile via backend
      await apiClient.put(`/users/${user?.id}`, {
        firstName: form.firstName,
        lastName:  form.lastName,
        gender:    form.gender,
        dateOfBirth: form.dateOfBirth || null,
        emergencyContactName:  form.emergencyContactName,
        emergencyContactPhone: form.emergencyContactPhone,
        preferredCounsellorGender: form.preferredCounsellorGender,
        wellnessGoals:  form.wellnessGoals,
        consentForDataSharing: form.consentForDataSharing,
        // Store bio, course etc. as part of extended profile
        bio:            form.bio,
        institution:    form.institution,
        course:         form.course,
        yearOfStudy:    form.yearOfStudy,
        studentIdNo:    form.studentIdNo,
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
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
          <User size={28} className="text-teal-500" /> My Profile
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">
          Help your counsellor understand you better. All fields are private unless you choose to share.
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

        {/* ── Avatar + name ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5 flex items-center gap-2">
            <User size={18} className="text-teal-500" /> Personal Information
          </h2>

          <div className="flex items-center gap-5 mb-6">
            <div className="relative">
              <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center text-white text-3xl font-bold shadow-lg shadow-teal-500/20">
                {form.firstName?.[0] ?? 'U'}{form.lastName?.[0] ?? ''}
              </div>
              <button type="button"
                className="absolute -bottom-1 -right-1 w-7 h-7 bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-full flex items-center justify-center hover:bg-slate-50 dark:hover:bg-slate-600 transition-colors shadow-sm">
                <Camera size={13} className="text-slate-600 dark:text-slate-300" />
              </button>
            </div>
            <div>
              <p className="font-bold text-slate-800 dark:text-white text-lg">{form.firstName} {form.lastName}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">{user?.email}</p>
              <span className="inline-block mt-1 px-2.5 py-0.5 bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-300 text-xs font-bold rounded-full">STUDENT</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">First Name</label>
              <input value={form.firstName} onChange={e => set('firstName', e.target.value)} placeholder="First name" className={inputCls} />
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Last Name</label>
              <input value={form.lastName} onChange={e => set('lastName', e.target.value)} placeholder="Last name" className={inputCls} />
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Gender</label>
              <select value={form.gender} onChange={e => set('gender', e.target.value)} className={inputCls}>
                {GENDER_OPTIONS.map(g => <option key={g}>{g}</option>)}
              </select>
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Date of Birth</label>
              <input type="date" value={form.dateOfBirth} onChange={e => set('dateOfBirth', e.target.value)}
                max={new Date().toISOString().split('T')[0]} className={inputCls} />
            </div>
          </div>
        </section>

        {/* ── Academic Background ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5 flex items-center gap-2">
            <GraduationCap size={18} className="text-violet-500" /> Academic Background
          </h2>
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Institution / University *</label>
              <input value={form.institution} onChange={e => set('institution', e.target.value)}
                placeholder="e.g. Delhi University, IIT Bombay" className={inputCls} required />
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Course / Programme</label>
              <input value={form.course} onChange={e => set('course', e.target.value)}
                placeholder="e.g. B.Tech CSE, M.A. Psychology" className={inputCls} />
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Year of Study</label>
              <select value={form.yearOfStudy} onChange={e => set('yearOfStudy', e.target.value)} className={inputCls}>
                {YEAR_OPTIONS.map(y => <option key={y}>{y}</option>)}
              </select>
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Student ID (optional)</label>
              <input value={form.studentIdNo} onChange={e => set('studentIdNo', e.target.value)}
                placeholder="Your institution's student ID" className={inputCls} />
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">Used for institution-verified access only.</p>
            </div>
          </div>
        </section>

        {/* ── Bio ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-2 flex items-center gap-2">
            <BookOpen size={18} className="text-sky-500" /> About Me
            <span className="ml-auto text-xs font-normal text-slate-500 dark:text-slate-400">Shown to your counsellor</span>
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">
            In up to <strong>100 words</strong>, tell your counsellor who you are — your background, what's been on your mind, or what you're hoping to work on.
          </p>
          <textarea
            value={form.bio}
            onChange={e => set('bio', e.target.value)}
            rows={5}
            placeholder="e.g. I'm a second-year engineering student dealing with exam pressure and feeling disconnected from my peers. I grew up in a small town and adjusting to city life has been harder than I expected. I'm hoping to build better coping mechanisms and find ways to be more present…"
            className={`${inputCls} resize-none ${bioOver ? 'border-red-400 dark:border-red-500 focus:ring-red-500' : ''}`}
          />
          <div className="flex items-center justify-between mt-2">
            <p className={`text-xs font-medium ${bioOver ? 'text-red-500' : wordCount >= 80 ? 'text-amber-500' : 'text-slate-400 dark:text-slate-500'}`}>
              {wordCount}/100 words{bioOver ? ' — please shorten your bio' : wordCount >= 80 ? ' — almost at limit' : ''}
            </p>
            {wordCount > 0 && !bioOver && (
              <p className="text-xs text-teal-600 dark:text-teal-400 font-medium">✓ Looks good</p>
            )}
          </div>
        </section>

        {/* ── Wellness Goals ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-2 flex items-center gap-2">
            <Heart size={18} className="text-rose-500" /> What I'm Working On
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">
            Select areas you'd like support with. This helps us suggest the right counsellors.
          </p>
          <div className="flex flex-wrap gap-2">
            {WELLNESS_GOAL_TAGS.map(goal => (
              <button key={goal} type="button" onClick={() => toggleGoal(goal)}
                className={`px-3.5 py-2 rounded-xl text-xs font-semibold transition-all border ${
                  form.wellnessGoals.includes(goal)
                    ? 'bg-teal-500 text-white border-teal-500 shadow-sm'
                    : 'bg-white dark:bg-slate-700/50 text-slate-600 dark:text-slate-300 border-slate-200 dark:border-slate-600 hover:border-teal-400 dark:hover:border-teal-500'
                }`}>
                {goal}
              </button>
            ))}
          </div>
        </section>

        {/* ── Emergency Contact ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-2">Emergency Contact</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">Only contacted if you are in immediate danger and unable to respond.</p>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Contact Name</label>
              <input value={form.emergencyContactName} onChange={e => set('emergencyContactName', e.target.value)}
                placeholder="e.g. Mum, Dad, Friend" className={inputCls} />
            </div>
            <div>
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Contact Phone</label>
              <input type="tel" value={form.emergencyContactPhone} onChange={e => set('emergencyContactPhone', e.target.value)}
                placeholder="+91 98765 43210" className={inputCls} />
            </div>
          </div>
        </section>

        {/* ── Preferences ── */}
        <section className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5">Counsellor Preferences</h2>
          <div>
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 block mb-1.5">Preferred Counsellor Gender</label>
            <select value={form.preferredCounsellorGender} onChange={e => set('preferredCounsellorGender', e.target.value)} className={`${inputCls} max-w-xs`}>
              {['No preference', 'Male', 'Female', 'Non-binary'].map(g => <option key={g}>{g}</option>)}
            </select>
          </div>

          <div className="mt-4 flex items-start gap-3">
            <input type="checkbox" id="consent" checked={form.consentForDataSharing}
              onChange={e => set('consentForDataSharing', e.target.checked)}
              className="w-4 h-4 rounded accent-teal-500 mt-0.5" />
            <label htmlFor="consent" className="text-sm text-slate-600 dark:text-slate-400 cursor-pointer leading-relaxed">
              I consent to my mood data and session notes being shared anonymously for platform improvement research. No personally identifiable information will be used. I can withdraw consent at any time.
            </label>
          </div>
        </section>

        {/* ── Save ── */}
        <button type="submit" disabled={saving || bioOver}
          className="flex items-center gap-2 px-8 py-3.5 bg-teal-600 hover:bg-teal-500 text-white font-bold rounded-xl transition-colors shadow-sm shadow-teal-500/20 disabled:opacity-60">
          {saving ? <Loader2 size={18} className="animate-spin" /> : <Save size={18} />}
          {saving ? 'Saving…' : 'Save Profile'}
        </button>
      </form>
    </div>
  );
};

export default StudentProfileSetupPage;

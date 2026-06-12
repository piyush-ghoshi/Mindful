import { useState, useEffect } from 'react';
import { X, User, GraduationCap, HeartHandshake, Loader2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface Props {
  onComplete: () => void;
}

const ROLES = [
  {
    id: 'STUDENT',
    label: 'Student',
    desc: 'I want to track my wellness and book counselling sessions.',
    icon: GraduationCap,
    gradient: 'from-teal-500/20 to-emerald-500/20 dark:from-teal-500/30 dark:to-emerald-500/30',
    border: 'border-teal-400 dark:border-teal-500',
    iconColor: 'text-teal-600 dark:text-teal-400',
    iconBg: 'bg-teal-500/10 dark:bg-teal-500/20',
  },
  {
    id: 'COUNSELLOR',
    label: 'Counsellor',
    desc: 'I provide mental health support and manage student sessions.',
    icon: HeartHandshake,
    gradient: 'from-violet-500/20 to-purple-500/20 dark:from-violet-500/30 dark:to-purple-500/30',
    border: 'border-violet-400 dark:border-violet-500',
    iconColor: 'text-violet-600 dark:text-violet-400',
    iconBg: 'bg-violet-500/10 dark:bg-violet-500/20',
  },
];

const GoogleProfileModal = ({ onComplete }: Props) => {
  const { pendingGoogleUser, completeGoogleSignUp, cancelGoogleSignUp, loading, error } = useAuth();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [role, setRole] = useState('STUDENT');
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Pre-fill with Google name
  useEffect(() => {
    if (pendingGoogleUser) {
      setFirstName(pendingGoogleUser.suggestedFirstName);
      setLastName(pendingGoogleUser.suggestedLastName);
    }
  }, [pendingGoogleUser]);

  if (!pendingGoogleUser) return null;

  const validate = () => {
    const e: Record<string, string> = {};
    if (!firstName.trim()) e.firstName = 'First name is required';
    if (!lastName.trim()) e.lastName = 'Last name is required';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    await completeGoogleSignUp(firstName.trim(), lastName.trim(), role);
    onComplete();
  };

  const inputCls = (hasError: boolean) =>
    `w-full bg-slate-50 dark:bg-slate-900/60 border rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all ${
      hasError ? 'border-red-400 dark:border-red-500' : 'border-slate-200 dark:border-slate-700'
    }`;

  return (
    /* Backdrop */
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 dark:bg-black/70 backdrop-blur-sm">
      {/* Modal */}
      <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden animate-in fade-in zoom-in-95 duration-200">

        {/* Header */}
        <div className="relative bg-gradient-to-br from-teal-500 to-emerald-600 p-8 text-white">
          <div className="absolute -right-6 -top-6 w-24 h-24 bg-white/10 rounded-full blur-2xl pointer-events-none" />
          <button
            onClick={cancelGoogleSignUp}
            className="absolute top-4 right-4 p-1.5 rounded-lg bg-white/10 hover:bg-white/20 transition-colors"
            aria-label="Cancel"
          >
            <X size={16} />
          </button>
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center">
              <User size={20} />
            </div>
            <div>
              <h2 className="text-xl font-bold">Almost there!</h2>
              <p className="text-teal-100 text-sm">Just a few details to set up your account.</p>
            </div>
          </div>
        </div>

        {/* Body */}
        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          {error && (
            <div className="p-3 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">
              {error}
            </div>
          )}

          {/* Name */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">
                First Name
              </label>
              <input
                type="text"
                value={firstName}
                onChange={e => { setFirstName(e.target.value); setErrors(p => ({ ...p, firstName: '' })); }}
                placeholder="Alex"
                className={inputCls(!!errors.firstName)}
              />
              {errors.firstName && <p className="text-xs text-red-500">{errors.firstName}</p>}
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">
                Last Name
              </label>
              <input
                type="text"
                value={lastName}
                onChange={e => { setLastName(e.target.value); setErrors(p => ({ ...p, lastName: '' })); }}
                placeholder="Johnson"
                className={inputCls(!!errors.lastName)}
              />
              {errors.lastName && <p className="text-xs text-red-500">{errors.lastName}</p>}
            </div>
          </div>

          {/* Role */}
          <div>
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-200 block mb-3">
              I am a…
            </label>
            <div className="grid grid-cols-2 gap-3">
              {ROLES.map(({ id, label, desc, icon: Icon, gradient, border, iconColor, iconBg }) => (
                <button
                  key={id}
                  type="button"
                  onClick={() => setRole(id)}
                  className={`
                    relative flex flex-col items-center gap-3 p-5 rounded-2xl border-2 text-center
                    bg-gradient-to-br ${gradient} transition-all duration-150
                    ${role === id
                      ? `${border} shadow-md`
                      : 'border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600'
                    }
                  `}
                >
                  {role === id && (
                    <div className="absolute top-2 right-2 w-4 h-4 rounded-full bg-teal-500 flex items-center justify-center">
                      <svg className="w-2.5 h-2.5 text-white" fill="currentColor" viewBox="0 0 12 12">
                        <path d="M10 3L5 8.5 2 5.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
                      </svg>
                    </div>
                  )}
                  <div className={`w-12 h-12 rounded-xl ${iconBg} flex items-center justify-center`}>
                    <Icon size={22} className={iconColor} />
                  </div>
                  <div>
                    <p className="text-sm font-bold text-slate-800 dark:text-slate-100">{label}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 leading-tight">{desc}</p>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 bg-teal-500 hover:bg-teal-600 text-white font-bold py-3.5 rounded-xl transition-all disabled:opacity-60 shadow-lg shadow-teal-500/20 text-sm"
          >
            {loading ? (
              <><Loader2 size={18} className="animate-spin" /> Setting up your account…</>
            ) : (
              'Complete Sign Up'
            )}
          </button>

          <p className="text-center text-xs text-slate-400 dark:text-slate-500">
            You can update your name and role later in Profile settings.
          </p>
        </form>
      </div>
    </div>
  );
};

export default GoogleProfileModal;

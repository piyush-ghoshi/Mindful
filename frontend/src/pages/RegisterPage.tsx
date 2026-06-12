import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Leaf, GraduationCap, HeartHandshake } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import GoogleProfileModal from '../components/GoogleProfileModal';

const GoogleIcon = () => (
  <svg className="w-5 h-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
  </svg>
);

const ROLES = [
  {
    id: 'STUDENT',
    label: 'Student',
    desc: 'Track wellness & book sessions',
    icon: GraduationCap,
    active: 'border-teal-500 bg-teal-50/80 dark:bg-teal-900/30',
    inactive: 'border-slate-200 dark:border-slate-700 hover:border-teal-300 dark:hover:border-teal-700',
    iconColor: 'text-teal-600 dark:text-teal-400',
    iconBg: 'bg-teal-500/10 dark:bg-teal-500/20',
  },
  {
    id: 'COUNSELLOR',
    label: 'Counsellor',
    desc: 'Provide support & manage sessions',
    icon: HeartHandshake,
    active: 'border-violet-500 bg-violet-50/80 dark:bg-violet-900/30',
    inactive: 'border-slate-200 dark:border-slate-700 hover:border-violet-300 dark:hover:border-violet-700',
    iconColor: 'text-violet-600 dark:text-violet-400',
    iconBg: 'bg-violet-500/10 dark:bg-violet-500/20',
  },
];

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, signInWithGoogle, loading, error: authError } = useAuth();

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '',
    password: '', confirmPassword: '', role: 'STUDENT',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.firstName.trim()) e.firstName = 'First name is required';
    if (!form.lastName.trim())  e.lastName  = 'Last name is required';
    if (!form.email) e.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Invalid email address';
    if (!form.password) e.password = 'Password is required';
    else if (form.password.length < 8) e.password = 'Password must be at least 8 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm(p => ({ ...p, [e.target.name]: e.target.value }));
    if (errors[e.target.name]) setErrors(p => ({ ...p, [e.target.name]: '' }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      setSubmitting(true);
      await register(form.email, form.password, form.firstName, form.lastName, form.role);
      navigate('/dashboard');
    } catch { /* shown via authError */ }
    finally { setSubmitting(false); }
  };

  const handleGoogle = async () => {
    try {
      setGoogleLoading(true);
      const { isNewUser } = await signInWithGoogle();
      // New user → GoogleProfileModal appears automatically (pendingGoogleUser set in context)
      // Returning user → go straight to dashboard
      if (!isNewUser) navigate('/dashboard');
    } catch { /* shown via authError */ }
    finally { setGoogleLoading(false); }
  };

  const inputCls = (err?: string) =>
    `w-full bg-slate-50 dark:bg-slate-900/50 border rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all ${err ? 'border-red-400 dark:border-red-500' : 'border-slate-200 dark:border-slate-700'}`;

  return (
    <>
      <div className="min-h-screen flex bg-[#f9f9ff] dark:bg-slate-950">
        {/* Left illustration */}
        <div className="hidden lg:flex w-1/2 relative overflow-hidden items-center justify-center">
          <div className="absolute inset-0 bg-cover bg-center"
            style={{ backgroundImage: "url('https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=800&q=80')" }}/>
          <div className="absolute inset-0 bg-[#006b5f]/60"/>
          <div className="relative z-10 text-center px-16">
            <p className="text-5xl font-bold text-white italic leading-tight max-w-lg drop-shadow-md">
              "Your journey to wellness starts today."
            </p>
            <p className="mt-6 text-teal-100 text-lg">Join thousands of students on Mindful.</p>
          </div>
        </div>

        {/* Right form */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12 overflow-y-auto">
          <div className="w-full max-w-md py-8">
            {/* Logo */}
            <div className="text-center mb-8">
              <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-teal-500 mb-4 shadow-lg shadow-teal-500/30">
                <Leaf size={28} className="text-white"/>
              </div>
              <h1 className="text-3xl font-bold text-slate-800 dark:text-white">Create Account</h1>
              <p className="text-slate-500 dark:text-slate-400 mt-1">Start your wellness journey today.</p>
            </div>

            <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50 shadow-sm">
              {authError && (
                <div className="mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">
                  {authError}
                </div>
              )}

              {/* ── Google sign-up (prominent, at top) ── */}
              <button onClick={handleGoogle} disabled={loading || submitting || googleLoading}
                className="w-full flex items-center justify-center gap-3 bg-white dark:bg-slate-700/60 border-2 border-slate-200 dark:border-slate-600 rounded-xl py-3 text-sm font-semibold text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300 dark:hover:border-slate-500 transition-all disabled:opacity-60 shadow-sm mb-5">
                <GoogleIcon/>
                {googleLoading ? 'Connecting to Google…' : 'Sign up with Google'}
              </button>

              {/* Divider */}
              <div className="flex items-center gap-3 mb-5">
                <div className="flex-1 h-px bg-slate-200 dark:bg-slate-700"/>
                <span className="text-xs text-slate-400 dark:text-slate-500 font-medium uppercase tracking-wider">
                  or fill in the form
                </span>
                <div className="flex-1 h-px bg-slate-200 dark:bg-slate-700"/>
              </div>

              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                {/* Name */}
                <div className="grid grid-cols-2 gap-3">
                  {(['firstName', 'lastName'] as const).map(field => (
                    <div key={field} className="flex flex-col gap-1.5">
                      <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">
                        {field === 'firstName' ? 'First Name' : 'Last Name'}
                      </label>
                      <input name={field} type="text" value={form[field]}
                        onChange={handleChange}
                        placeholder={field === 'firstName' ? 'Alex' : 'Johnson'}
                        disabled={loading || submitting}
                        className={inputCls(errors[field])}/>
                      {errors[field] && <p className="text-xs text-red-500">{errors[field]}</p>}
                    </div>
                  ))}
                </div>

                {/* Email */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">
                    Email Address
                  </label>
                  <input name="email" type="email" value={form.email}
                    onChange={handleChange} placeholder="alex@university.edu"
                    disabled={loading || submitting} className={inputCls(errors.email)}/>
                  {errors.email && <p className="text-xs text-red-500">{errors.email}</p>}
                </div>

                {/* Role picker */}
                <div>
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-200 block mb-2">
                    I am a…
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    {ROLES.map(({ id, label, desc, icon: Icon, active, inactive, iconColor, iconBg }) => (
                      <button key={id} type="button" onClick={() => setForm(p => ({ ...p, role: id }))}
                        className={`relative flex flex-col items-center gap-2 p-4 rounded-xl border-2 text-center transition-all ${form.role === id ? active : inactive}`}>
                        {form.role === id && (
                          <div className="absolute top-2 right-2 w-4 h-4 rounded-full bg-teal-500 flex items-center justify-center">
                            <svg className="w-2.5 h-2.5 text-white" fill="none" viewBox="0 0 12 12" stroke="currentColor" strokeWidth="2">
                              <path d="M2 6l3 3 5-5" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                          </div>
                        )}
                        <div className={`w-10 h-10 rounded-xl ${iconBg} flex items-center justify-center`}>
                          <Icon size={20} className={iconColor}/>
                        </div>
                        <div>
                          <p className="text-sm font-bold text-slate-800 dark:text-slate-100">{label}</p>
                          <p className="text-xs text-slate-500 dark:text-slate-400 leading-tight mt-0.5">{desc}</p>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Password */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">Password</label>
                  <div className="relative">
                    <input name="password" type={showPassword ? 'text' : 'password'}
                      value={form.password} onChange={handleChange}
                      placeholder="Min. 8 characters" disabled={loading || submitting}
                      className={`${inputCls(errors.password)} pr-12`}/>
                    <button type="button" onClick={() => setShowPassword(s => !s)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300">
                      {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
                    </button>
                  </div>
                  {errors.password && <p className="text-xs text-red-500">{errors.password}</p>}
                </div>

                {/* Confirm password */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">
                    Confirm Password
                  </label>
                  <input name="confirmPassword" type="password" value={form.confirmPassword}
                    onChange={handleChange} placeholder="Re-enter password"
                    disabled={loading || submitting} className={inputCls(errors.confirmPassword)}/>
                  {errors.confirmPassword && <p className="text-xs text-red-500">{errors.confirmPassword}</p>}
                </div>

                {/* Submit */}
                <button type="submit" disabled={loading || submitting}
                  className="w-full bg-teal-500 hover:bg-teal-600 text-white font-bold py-3.5 rounded-xl transition-all mt-1 disabled:opacity-60 shadow-sm shadow-teal-500/20">
                  {loading || submitting ? 'Creating account…' : 'Create Account'}
                </button>
              </form>

              <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
                Already have an account?{' '}
                <Link to="/login"
                  className="font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-700 dark:hover:text-teal-300 transition-colors">
                  Sign in
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Profile completion modal — shown when a new Google user signs up */}
      <GoogleProfileModal onComplete={() => navigate('/dashboard')}/>
    </>
  );
};

export default RegisterPage;

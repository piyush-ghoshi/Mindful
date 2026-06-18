import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Sparkles, ArrowRight, GraduationCap, HeartHandshake, CheckCircle } from 'lucide-react';
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
    gradient: 'from-teal-400 to-emerald-500',
    activeBorder: 'border-teal-400/80',
    activeBg: 'bg-teal-400/10',
  },
  {
    id: 'COUNSELLOR',
    label: 'Counsellor',
    desc: 'Provide support & manage sessions',
    icon: HeartHandshake,
    gradient: 'from-violet-400 to-purple-500',
    activeBorder: 'border-violet-400/80',
    activeBg: 'bg-violet-400/10',
  },
];

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, signInWithGoogle, loading, error: authError, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard', { replace: true });
  }, [isAuthenticated, navigate]);

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '',
    password: '', confirmPassword: '', role: 'STUDENT',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [focusedField, setFocusedField] = useState<string | null>(null);

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.firstName.trim()) e.firstName = 'Required';
    if (!form.lastName.trim()) e.lastName = 'Required';
    if (!form.email) e.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Invalid email';
    if (!form.password) e.password = 'Password is required';
    else if (form.password.length < 8) e.password = 'At least 8 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords don\'t match';
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

  const handleGoogle = () => {
    const promise = signInWithGoogle();
    setGoogleLoading(true);
    promise
      .then(({ isNewUser }) => { if (!isNewUser) navigate('/dashboard'); })
      .catch(() => {})
      .finally(() => setGoogleLoading(false));
  };

  const inputCls = (field: string) =>
    `w-full bg-white/10 border-2 rounded-2xl px-4 py-3.5 text-white placeholder:text-white/40 focus:outline-none transition-all duration-200 text-sm ${
      errors[field]
        ? 'border-red-400/70 bg-red-400/5'
        : focusedField === field
        ? 'border-teal-400/80 bg-white/15 shadow-lg shadow-teal-500/10'
        : 'border-white/20 hover:border-white/30'
    }`;

  // Password strength
  const pwStrength = () => {
    const pw = form.password;
    if (!pw) return { pct: 0, label: '', color: '' };
    let score = 0;
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    if (score <= 1) return { pct: 25, label: 'Weak', color: 'bg-red-400' };
    if (score === 2) return { pct: 50, label: 'Fair', color: 'bg-yellow-400' };
    if (score === 3) return { pct: 75, label: 'Good', color: 'bg-teal-400' };
    return { pct: 100, label: 'Strong', color: 'bg-emerald-400' };
  };
  const strength = pwStrength();

  return (
    <>
      <div className="min-h-screen flex" style={{
        background: 'linear-gradient(135deg, #0f2027 0%, #1a3a4a 30%, #0d3d30 60%, #0f2027 100%)',
      }}>
        {/* Animated orbs */}
        <div className="fixed inset-0 overflow-hidden pointer-events-none">
          <div className="absolute -top-32 -right-32 w-96 h-96 rounded-full opacity-20 blur-3xl"
            style={{ background: 'radial-gradient(circle, #a78bfa, transparent)' }} />
          <div className="absolute top-1/2 -left-48 w-[500px] h-[500px] rounded-full opacity-10 blur-3xl"
            style={{ background: 'radial-gradient(circle, #14b8a6, transparent)' }} />
          <div className="absolute -bottom-32 right-1/3 w-80 h-80 rounded-full opacity-15 blur-3xl"
            style={{ background: 'radial-gradient(circle, #10b981, transparent)' }} />
        </div>

        {/* Left panel */}
        <div className="hidden lg:flex w-1/2 flex-col justify-between p-14 relative">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center shadow-lg shadow-teal-500/40">
              <svg viewBox="0 0 24 24" fill="none" className="w-5 h-5 text-white" stroke="currentColor" strokeWidth={2.5}>
                <path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2z" strokeLinecap="round"/>
                <path d="M8 12c0-2.21 1.79-4 4-4s4 1.79 4 4-1.79 4-4 4" strokeLinecap="round"/>
              </svg>
            </div>
            <span className="text-white font-bold text-xl tracking-tight">Mindful</span>
          </div>

          {/* Main text */}
          <div className="space-y-10">
            <div>
              <h1 className="text-5xl font-black text-white leading-tight mb-4">
                Begin your<br />
                <span className="text-transparent bg-clip-text" style={{ backgroundImage: 'linear-gradient(90deg, #a78bfa, #8b5cf6)' }}>
                  wellness journey.
                </span>
              </h1>
              <p className="text-white/60 text-lg leading-relaxed max-w-md">
                Join thousands of students and counsellors building healthier minds together.
              </p>
            </div>

            {/* What you get */}
            <div className="space-y-4">
              {[
                'Personalised AI mental health reports',
                'Direct messaging with counsellors',
                'Daily mood & wellness tracking',
                'Private & completely confidential',
              ].map(text => (
                <div key={text} className="flex items-center gap-3">
                  <div className="w-5 h-5 rounded-full bg-teal-400/20 flex items-center justify-center flex-shrink-0">
                    <CheckCircle size={13} className="text-teal-400" />
                  </div>
                  <span className="text-white/70 text-sm">{text}</span>
                </div>
              ))}
            </div>

            {/* Stats */}
            <div className="flex gap-8">
              {[
                { val: '5,000+', label: 'Students' },
                { val: '200+', label: 'Counsellors' },
                { val: '4.9★', label: 'Rating' },
              ].map(({ val, label }) => (
                <div key={label}>
                  <p className="text-2xl font-black text-white">{val}</p>
                  <p className="text-white/40 text-xs mt-0.5">{label}</p>
                </div>
              ))}
            </div>
          </div>

          <p className="text-white/30 text-xs">© 2025 Mindful Wellness. All rights reserved.</p>
        </div>

        {/* Right panel — form */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-8 relative overflow-y-auto">
          <div className="w-full max-w-md py-4">
            {/* Mobile logo */}
            <div className="flex items-center gap-3 mb-6 lg:hidden">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center">
                <svg viewBox="0 0 24 24" fill="none" className="w-4 h-4 text-white" stroke="currentColor" strokeWidth={2.5}>
                  <path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2z" strokeLinecap="round"/>
                  <path d="M8 12c0-2.21 1.79-4 4-4s4 1.79 4 4-1.79 4-4 4" strokeLinecap="round"/>
                </svg>
              </div>
              <span className="text-white font-bold text-lg">Mindful</span>
            </div>

            {/* Card */}
            <div className="bg-white/10 backdrop-blur-2xl border border-white/20 rounded-3xl p-7 shadow-2xl">
              {/* Header */}
              <div className="mb-6">
                <div className="flex items-center gap-2 mb-1">
                  <Sparkles size={16} className="text-violet-400" />
                  <span className="text-violet-400 text-xs font-semibold uppercase tracking-widest">Get started</span>
                </div>
                <h2 className="text-3xl font-black text-white">Create account</h2>
                <p className="text-white/50 text-sm mt-1">It takes less than a minute</p>
              </div>

              {authError && (
                <div className="mb-4 p-3.5 rounded-2xl bg-red-400/10 border border-red-400/30 text-red-300 text-sm flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-red-400 flex-shrink-0" />
                  {authError}
                </div>
              )}

              {/* Google */}
              <button
                onClick={handleGoogle}
                disabled={loading || submitting || googleLoading}
                className="w-full flex items-center justify-center gap-3 bg-white/90 hover:bg-white text-slate-800 font-semibold py-3.5 rounded-2xl transition-all duration-200 disabled:opacity-50 shadow-lg text-sm mb-5 hover:shadow-xl hover:scale-[1.01] active:scale-[0.99]"
              >
                <GoogleIcon />
                {googleLoading ? 'Connecting…' : 'Sign up with Google'}
              </button>

              {/* Divider */}
              <div className="flex items-center gap-3 mb-5">
                <div className="flex-1 h-px bg-white/15" />
                <span className="text-white/30 text-xs">or fill in the form</span>
                <div className="flex-1 h-px bg-white/15" />
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Role picker */}
                <div>
                  <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2.5">I am a…</label>
                  <div className="grid grid-cols-2 gap-3">
                    {ROLES.map(({ id, label, desc, icon: Icon, gradient, activeBorder, activeBg }) => (
                      <button
                        key={id} type="button"
                        onClick={() => setForm(p => ({ ...p, role: id }))}
                        className={`relative flex flex-col items-center gap-2 p-4 rounded-2xl border-2 text-center transition-all duration-200 ${
                          form.role === id
                            ? `${activeBorder} ${activeBg}`
                            : 'border-white/15 hover:border-white/30 bg-white/5'
                        }`}
                      >
                        {form.role === id && (
                          <div className="absolute top-2 right-2 w-4 h-4 rounded-full bg-teal-400 flex items-center justify-center">
                            <svg className="w-2.5 h-2.5 text-white" fill="none" viewBox="0 0 12 12" stroke="currentColor" strokeWidth="2.5">
                              <path d="M2 6l3 3 5-5" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                          </div>
                        )}
                        <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center shadow-sm`}>
                          <Icon size={18} className="text-white" />
                        </div>
                        <div>
                          <p className="text-sm font-bold text-white">{label}</p>
                          <p className="text-[10px] text-white/40 leading-tight mt-0.5">{desc}</p>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Name */}
                <div className="grid grid-cols-2 gap-3">
                  {(['firstName', 'lastName'] as const).map(field => (
                    <div key={field}>
                      <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2">
                        {field === 'firstName' ? 'First name' : 'Last name'}
                      </label>
                      <input
                        name={field} type="text" value={form[field]}
                        onChange={handleChange}
                        placeholder={field === 'firstName' ? 'Alex' : 'Johnson'}
                        disabled={loading || submitting}
                        onFocus={() => setFocusedField(field)}
                        onBlur={() => setFocusedField(null)}
                        className={inputCls(field)}
                      />
                      {errors[field] && <p className="text-red-400 text-xs mt-1"><span>⚠ </span>{errors[field]}</p>}
                    </div>
                  ))}
                </div>

                {/* Email */}
                <div>
                  <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2" htmlFor="reg-email">
                    Email address
                  </label>
                  <input
                    id="reg-email" name="email" type="email"
                    value={form.email} onChange={handleChange}
                    placeholder="you@university.edu"
                    disabled={loading || submitting}
                    onFocus={() => setFocusedField('email')}
                    onBlur={() => setFocusedField(null)}
                    className={inputCls('email')}
                  />
                  {errors.email && <p className="text-red-400 text-xs mt-1"><span>⚠ </span>{errors.email}</p>}
                </div>

                {/* Password */}
                <div>
                  <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2">Password</label>
                  <div className="relative">
                    <input
                      name="password" type={showPassword ? 'text' : 'password'}
                      value={form.password} onChange={handleChange}
                      placeholder="Min. 8 characters"
                      disabled={loading || submitting}
                      onFocus={() => setFocusedField('password')}
                      onBlur={() => setFocusedField(null)}
                      className={`${inputCls('password')} pr-12`}
                    />
                    <button type="button" onClick={() => setShowPassword(s => !s)}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-white/40 hover:text-white/70 transition-colors">
                      {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                    </button>
                  </div>
                  {form.password && (
                    <div className="flex items-center gap-2 mt-2">
                      <div className="flex-1 h-1 rounded-full bg-white/10 overflow-hidden">
                        <div className={`h-full rounded-full ${strength.color} transition-all duration-300`}
                          style={{ width: `${strength.pct}%` }} />
                      </div>
                      <span className="text-[10px] text-white/50 font-medium">{strength.label}</span>
                    </div>
                  )}
                  {errors.password && <p className="text-red-400 text-xs mt-1"><span>⚠ </span>{errors.password}</p>}
                </div>

                {/* Confirm password */}
                <div>
                  <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2">Confirm password</label>
                  <input
                    name="confirmPassword" type="password"
                    value={form.confirmPassword} onChange={handleChange}
                    placeholder="Re-enter password"
                    disabled={loading || submitting}
                    onFocus={() => setFocusedField('confirmPassword')}
                    onBlur={() => setFocusedField(null)}
                    className={inputCls('confirmPassword')}
                  />
                  {errors.confirmPassword && <p className="text-red-400 text-xs mt-1"><span>⚠ </span>{errors.confirmPassword}</p>}
                </div>

                {/* Submit */}
                <button
                  type="submit"
                  disabled={loading || submitting}
                  className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-violet-500 to-purple-500 hover:from-violet-400 hover:to-purple-400 text-white font-bold py-4 rounded-2xl transition-all duration-200 disabled:opacity-50 shadow-lg shadow-violet-500/30 hover:shadow-xl hover:shadow-violet-500/40 hover:scale-[1.01] active:scale-[0.99] text-sm mt-1"
                >
                  {loading || submitting ? (
                    <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Creating account…</>
                  ) : (
                    <>Create account <ArrowRight size={16} /></>
                  )}
                </button>
              </form>

              <p className="mt-5 text-center text-white/40 text-sm">
                Already have an account?{' '}
                <Link to="/login" className="text-teal-400 hover:text-teal-300 font-semibold transition-colors">
                  Sign in →
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>

      <GoogleProfileModal onComplete={() => navigate('/dashboard')} />
    </>
  );
};

export default RegisterPage;

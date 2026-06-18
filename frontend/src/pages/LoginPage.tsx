import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Eye, EyeOff, Sparkles, ArrowRight, Shield, Brain, Heart } from 'lucide-react';
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

const FEATURES = [
  { icon: Brain, text: 'AI-powered mental wellness companion' },
  { icon: Shield, text: 'Private & confidential — always' },
  { icon: Heart, text: 'Connect with real counsellors' },
];

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, signInWithGoogle, loading, error: authError, isAuthenticated } = useAuth();
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  useEffect(() => {
    if (isAuthenticated) navigate(from, { replace: true });
  }, [isAuthenticated, navigate, from]);

  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [focusedField, setFocusedField] = useState<string | null>(null);

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.email) e.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Invalid email address';
    if (!form.password) e.password = 'Password is required';
    else if (form.password.length < 6) e.password = 'At least 6 characters';
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
      await login(form.email, form.password);
      navigate(from, { replace: true });
    } catch { /* shown via authError */ }
    finally { setSubmitting(false); }
  };

  const handleGoogle = async () => {
    try {
      setGoogleLoading(true);
      await signInWithGoogle();
    } catch { /* shown via authError */ }
    finally { setGoogleLoading(false); }
  };

  const inputCls = (field: string) =>
    `w-full bg-white/10 border-2 rounded-2xl px-4 py-3.5 text-white placeholder:text-white/40 focus:outline-none transition-all duration-200 text-sm ${
      errors[field]
        ? 'border-red-400/70 bg-red-400/5'
        : focusedField === field
        ? 'border-teal-400/80 bg-white/15 shadow-lg shadow-teal-500/10'
        : 'border-white/20 hover:border-white/30'
    }`;

  return (
    <>
      {/* Full-screen gradient background */}
      <div className="min-h-screen flex" style={{
        background: 'linear-gradient(135deg, #0f2027 0%, #1a3a4a 30%, #0d3d30 60%, #0f2027 100%)',
      }}>
        {/* Animated orbs */}
        <div className="fixed inset-0 overflow-hidden pointer-events-none">
          <div className="absolute -top-32 -left-32 w-96 h-96 rounded-full opacity-20 blur-3xl"
            style={{ background: 'radial-gradient(circle, #14b8a6, transparent)' }} />
          <div className="absolute top-1/2 -right-48 w-[500px] h-[500px] rounded-full opacity-10 blur-3xl"
            style={{ background: 'radial-gradient(circle, #6366f1, transparent)' }} />
          <div className="absolute -bottom-32 left-1/3 w-80 h-80 rounded-full opacity-15 blur-3xl"
            style={{ background: 'radial-gradient(circle, #10b981, transparent)' }} />
        </div>

        {/* Left panel */}
        <div className="hidden lg:flex w-1/2 flex-col justify-between p-14 relative">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <img src="/mindful-logo.png" alt="Mindful" className="w-11 h-11 rounded-2xl object-cover shadow-lg shadow-teal-500/40" />
            <span className="text-white font-bold text-xl tracking-tight">Mindful</span>
          </div>

          {/* Main text */}
          <div className="space-y-6">
            <div>
              <h1 className="text-4xl font-black text-white leading-tight mb-3">
                Your mind<br />
                <span className="text-transparent bg-clip-text" style={{ backgroundImage: 'linear-gradient(90deg, #34d399, #14b8a6)' }}>
                  deserves care.
                </span>
              </h1>
              <p className="text-white/60 text-base leading-relaxed max-w-md">
                From overwhelmed to thriving — Mindful guides you every step of the way.
              </p>
            </div>

            {/* Transformation illustration */}
            <div className="relative">
              <img
                src="/mindful-transformation.png"
                alt="Student mental health transformation with Mindful"
                className="w-full rounded-2xl object-cover shadow-2xl border border-white/10"
                style={{ maxHeight: '280px', objectPosition: 'center top' }}
              />
              <div className="absolute inset-0 rounded-2xl" style={{ background: 'linear-gradient(to bottom, transparent 60%, rgba(15,32,39,0.8))' }} />
              <div className="absolute bottom-3 left-0 right-0 text-center">
                <p className="text-white/70 text-xs font-medium">Mindful transforms how students experience mental wellness</p>
              </div>
            </div>

            <div className="space-y-3">
              {FEATURES.map(({ icon: Icon, text }) => (
                <div key={text} className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-white/10 backdrop-blur-sm flex items-center justify-center flex-shrink-0 border border-white/10">
                    <Icon size={15} className="text-teal-400" />
                  </div>
                  <span className="text-white/70 text-sm">{text}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Bottom */}
          <p className="text-white/30 text-xs">© 2025 Mindful Wellness. All rights reserved.</p>
        </div>

        {/* Right panel — form */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-10 relative">
          <div className="w-full max-w-md">
            {/* Mobile logo */}
            <div className="flex items-center gap-3 mb-8 lg:hidden">
              <img src="/mindful-logo.png" alt="Mindful" className="w-9 h-9 rounded-xl object-cover" />
              <span className="text-white font-bold text-lg">Mindful</span>
            </div>

            {/* Card */}
            <div className="bg-white/10 backdrop-blur-2xl border border-white/20 rounded-3xl p-8 shadow-2xl">
              {/* Header */}
              <div className="mb-8">
                <div className="flex items-center gap-2 mb-1">
                  <Sparkles size={16} className="text-teal-400" />
                  <span className="text-teal-400 text-xs font-semibold uppercase tracking-widest">Welcome back</span>
                </div>
                <h2 className="text-3xl font-black text-white">Sign in</h2>
                <p className="text-white/50 text-sm mt-1">to your Mindful account</p>
              </div>

              {/* Error */}
              {authError && (
                <div className="mb-5 p-3.5 rounded-2xl bg-red-400/10 border border-red-400/30 text-red-300 text-sm flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-red-400 flex-shrink-0" />
                  {authError}
                </div>
              )}

              {/* Google button */}
              <button
                onClick={handleGoogle}
                disabled={loading || submitting || googleLoading}
                className="w-full flex items-center justify-center gap-3 bg-white/90 hover:bg-white text-slate-800 font-semibold py-3.5 rounded-2xl transition-all duration-200 disabled:opacity-50 shadow-lg text-sm mb-5 hover:shadow-xl hover:scale-[1.01] active:scale-[0.99]"
              >
                <GoogleIcon />
                {googleLoading ? 'Connecting…' : 'Continue with Google'}
              </button>

              {/* Divider */}
              <div className="flex items-center gap-3 mb-5">
                <div className="flex-1 h-px bg-white/15" />
                <span className="text-white/30 text-xs">or sign in with email</span>
                <div className="flex-1 h-px bg-white/15" />
              </div>

              {/* Form */}
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2" htmlFor="email">
                    Email address
                  </label>
                  <input
                    id="email" name="email" type="email"
                    value={form.email} onChange={handleChange}
                    placeholder="you@university.edu"
                    disabled={loading || submitting}
                    onFocus={() => setFocusedField('email')}
                    onBlur={() => setFocusedField(null)}
                    className={inputCls('email')}
                  />
                  {errors.email && <p className="text-red-400 text-xs mt-1.5 flex items-center gap-1"><span>⚠</span>{errors.email}</p>}
                </div>

                <div>
                  <label className="block text-white/70 text-xs font-semibold uppercase tracking-wider mb-2" htmlFor="password">
                    Password
                  </label>
                  <div className="relative">
                    <input
                      id="password" name="password" type={showPassword ? 'text' : 'password'}
                      value={form.password} onChange={handleChange}
                      placeholder="••••••••"
                      disabled={loading || submitting}
                      onFocus={() => setFocusedField('password')}
                      onBlur={() => setFocusedField(null)}
                      className={`${inputCls('password')} pr-12`}
                    />
                    <button
                      type="button" onClick={() => setShowPassword(s => !s)}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-white/40 hover:text-white/70 transition-colors"
                    >
                      {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                    </button>
                  </div>
                  {errors.password && <p className="text-red-400 text-xs mt-1.5 flex items-center gap-1"><span>⚠</span>{errors.password}</p>}
                </div>

                <div className="flex justify-end">
                  <Link to="/forgot-password" className="text-teal-400 hover:text-teal-300 text-xs font-semibold transition-colors">
                    Forgot password?
                  </Link>
                </div>

                <button
                  type="submit"
                  disabled={loading || submitting}
                  className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-400 hover:to-emerald-400 text-white font-bold py-4 rounded-2xl transition-all duration-200 disabled:opacity-50 shadow-lg shadow-teal-500/30 hover:shadow-xl hover:shadow-teal-500/40 hover:scale-[1.01] active:scale-[0.99] text-sm mt-2"
                >
                  {loading || submitting ? (
                    <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Signing in…</>
                  ) : (
                    <>Sign in <ArrowRight size={16} /></>
                  )}
                </button>
              </form>

              <p className="mt-6 text-center text-white/40 text-sm">
                Don't have an account?{' '}
                <Link to="/register" className="text-teal-400 hover:text-teal-300 font-semibold transition-colors">
                  Create one free →
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>

      <GoogleProfileModal onComplete={() => navigate(from, { replace: true })} />
    </>
  );
};

export default LoginPage;

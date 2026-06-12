import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Leaf, ArrowLeft, CheckCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const ForgotPasswordPage = () => {
  const { sendPasswordResetEmail, loading } = useAuth();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) { setError('Email is required'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { setError('Invalid email address'); return; }
    try {
      await sendPasswordResetEmail(email);
      setSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send reset email');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f9f9ff] dark:bg-slate-950 p-6">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-teal-500 mb-4 shadow-lg shadow-teal-500/30">
            <Leaf size={28} className="text-white"/>
          </div>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white">Reset Password</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">We'll send you a reset link.</p>
        </div>
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50 shadow-sm">
          {sent ? (
            <div className="text-center py-4">
              <CheckCircle size={48} className="text-teal-500 mx-auto mb-4"/>
              <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-2">Check your email</h2>
              <p className="text-slate-500 dark:text-slate-400 text-sm mb-6">We sent a reset link to <strong className="text-slate-700 dark:text-slate-200">{email}</strong></p>
              <Link to="/login" className="inline-flex items-center gap-2 text-sm font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-700 dark:hover:text-teal-300 transition-colors">
                <ArrowLeft size={16}/>Back to Login
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200" htmlFor="email">Email Address</label>
                <input id="email" type="email" value={email} onChange={e => { setEmail(e.target.value); setError(''); }} placeholder="Enter your email" disabled={loading}
                  className={`w-full bg-slate-50 dark:bg-slate-900/50 border rounded-lg px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all ${error?'border-red-400':'border-slate-200 dark:border-slate-700'}`}/>
                {error && <p className="text-xs text-red-600 dark:text-red-400">{error}</p>}
              </div>
              <button type="submit" disabled={loading}
                className="w-full bg-teal-500 hover:bg-teal-600 text-white font-semibold py-3.5 rounded-lg transition-all disabled:opacity-60 shadow-sm">
                {loading ? 'Sending…' : 'Send Reset Link'}
              </button>
              <Link to="/login" className="flex items-center justify-center gap-2 text-sm font-semibold text-slate-500 dark:text-slate-400 hover:text-teal-600 dark:hover:text-teal-400 transition-colors">
                <ArrowLeft size={16}/>Back to Login
              </Link>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;

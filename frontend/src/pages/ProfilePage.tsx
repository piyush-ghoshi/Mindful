import { useState } from 'react';
import { User, Mail, Shield, LogOut, Save, CheckCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const input = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

const ProfilePage = () => {
  const { user, logout, updateProfile } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstName:user?.firstName??'', lastName:user?.lastName??'', email:user?.email??'' });
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState('');

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true); setError('');
      await updateProfile({ firstName:form.firstName, lastName:form.lastName });
      setSaved(true); setTimeout(() => setSaved(false), 3000);
    } catch (err) { setError(err instanceof Error ? err.message : 'Failed to update profile'); }
    finally { setSaving(false); }
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <User size={28} className="text-teal-500"/>Profile & Settings
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Manage your account information</p>
      </div>

      {/* Avatar card */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 flex items-center gap-5">
        <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center text-white text-3xl font-bold flex-shrink-0 shadow-lg shadow-teal-500/20">
          {user?.firstName?.[0]??'U'}{user?.lastName?.[0]??''}
        </div>
        <div>
          <h2 className="text-xl font-bold text-slate-800 dark:text-white">{user?.firstName} {user?.lastName}</h2>
          <p className="text-slate-500 dark:text-slate-400 text-sm">{user?.email}</p>
          <span className="inline-block mt-2 px-3 py-1 bg-teal-500/10 dark:bg-teal-500/20 text-teal-700 dark:text-teal-300 text-xs font-bold rounded-full">
            {user?.role ?? 'STUDENT'}
          </span>
        </div>
      </div>

      {/* Edit */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
        <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-5 flex items-center gap-2">
          <User size={18} className="text-teal-500"/>Personal Information
        </h2>
        {saved && <div className="mb-4 p-3 rounded-xl bg-teal-50 dark:bg-teal-900/30 border border-teal-200 dark:border-teal-700 text-teal-700 dark:text-teal-300 text-sm font-medium flex items-center gap-2"><CheckCircle size={16}/>Profile updated!</div>}
        {error && <div className="mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">{error}</div>}
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {(['firstName','lastName'] as const).map(field => (
              <div key={field} className="flex flex-col gap-1.5">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-300">{field==='firstName'?'First Name':'Last Name'}</label>
                <input name={field} type="text" value={form[field]} onChange={e => setForm(p=>({...p,[e.target.name]:e.target.value}))} className={input}/>
              </div>
            ))}
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2"><Mail size={14}/>Email Address</label>
            <input name="email" type="email" value={form.email} disabled className={`${input} opacity-50 cursor-not-allowed`}/>
            <p className="text-xs text-slate-400 dark:text-slate-500">Email cannot be changed here.</p>
          </div>
          <button type="submit" disabled={saving}
            className="flex items-center gap-2 px-6 py-3 bg-teal-500 hover:bg-teal-600 text-white rounded-xl font-semibold text-sm transition-colors disabled:opacity-60 shadow-sm">
            <Save size={16}/>{saving ? 'Saving…' : 'Save Changes'}
          </button>
        </form>
      </div>

      {/* Security */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
        <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-4 flex items-center gap-2">
          <Shield size={18} className="text-sky-500"/>Security
        </h2>
        <div className="space-y-3">
          {[
            { label:'Password', sub:'Last changed: Unknown', action:() => navigate('/forgot-password'), actionLabel:'Change' },
            { label:'Email Verified', sub:user?.email??'', action:null, actionLabel:null },
          ].map(({ label, sub, action, actionLabel }) => (
            <div key={label} className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-700/40 rounded-xl">
              <div>
                <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">{label}</p>
                <p className="text-xs text-slate-500 dark:text-slate-400">{sub}</p>
              </div>
              {action ? (
                <button onClick={action} className="text-sm font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-700 dark:hover:text-teal-300 transition-colors">{actionLabel}</button>
              ) : (
                <span className="flex items-center gap-1 text-xs font-semibold text-teal-600 dark:text-teal-400"><CheckCircle size={14}/>Verified</span>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Sign out */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-red-100 dark:border-red-900/30">
        <h2 className="text-lg font-bold text-red-600 dark:text-red-400 mb-4 flex items-center gap-2"><LogOut size={18}/>Account Actions</h2>
        <button onClick={async () => { await logout(); navigate('/login'); }}
          className="flex items-center gap-2 px-6 py-3 bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800 rounded-xl font-semibold text-sm hover:bg-red-100 dark:hover:bg-red-900/30 transition-colors">
          <LogOut size={16}/>Sign Out
        </button>
      </div>
    </div>
  );
};

export default ProfilePage;

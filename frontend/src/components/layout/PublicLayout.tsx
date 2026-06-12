import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { Leaf, Menu, X, Sun, Moon, LogOut } from 'lucide-react';
import { useState } from 'react';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';

const publicNav = [
  { to: '/about',           label: 'About Us' },
  { to: '/success-stories', label: 'Success Stories' },
  { to: '/crisis-support',  label: 'Crisis Support' },
];

const PublicLayout = () => {
  const [menuOpen, setMenuOpen] = useState(false);
  const { theme, toggleTheme } = useTheme();
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const dashPath = user?.role === 'COUNSELLOR' ? '/counsellor/dashboard' : '/dashboard';

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-[#f0faf8] dark:bg-[#0b1221] flex flex-col">

      {/* ── Top nav ── */}
      <header className="sticky top-0 z-40 transition-all duration-300"
        style={{
          background: theme === 'dark' ? 'rgba(11,18,33,0.85)' : 'rgba(240,250,248,0.88)',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          borderBottom: theme === 'dark' ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(20,184,166,0.12)',
          boxShadow: theme === 'dark' ? '0 4px 20px rgba(0,0,0,0.3)' : '0 2px 16px rgba(20,184,166,0.08)',
        }}>
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">

          {/* Brand */}
          <Link to={isAuthenticated ? dashPath : '/'} className="flex items-center gap-2.5 group">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center shadow-md shadow-teal-500/30 group-hover:shadow-lg group-hover:shadow-teal-500/40 transition-all">
              <Leaf size={17} className="text-white" />
            </div>
            <span className="text-xl font-black text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-emerald-600 dark:from-teal-400 dark:to-emerald-400 tracking-tight">
              Mindful
            </span>
          </Link>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-0.5 bg-white/50 dark:bg-slate-800/50 rounded-2xl px-2 py-1.5 border border-white/60 dark:border-slate-700/50">
            {publicNav.map(({ to, label }) => {
              const active = location.pathname === to;
              return (
                <Link key={to} to={to}
                  className={`px-3.5 py-1.5 rounded-xl text-sm font-semibold transition-all ${
                    active
                      ? 'bg-gradient-to-r from-teal-500 to-emerald-500 text-white shadow-sm shadow-teal-500/30'
                      : 'text-slate-600 dark:text-slate-400 hover:text-teal-700 dark:hover:text-teal-300 hover:bg-teal-50 dark:hover:bg-slate-700/60'
                  }`}>
                  {label}
                </Link>
              );
            })}
          </nav>

          {/* Right actions */}
          <div className="flex items-center gap-1.5">
            <button onClick={toggleTheme}
              className="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-white/70 dark:hover:bg-slate-800 transition-all">
              {theme === 'dark' ? <Sun size={18} className="text-amber-400" /> : <Moon size={18} className="text-slate-500" />}
            </button>

            {isAuthenticated ? (
              <>
                <Link to={dashPath}
                  className="hidden md:flex items-center gap-2 pl-1 pr-3 py-1 rounded-2xl bg-white/60 dark:bg-slate-800/60 border border-transparent hover:border-teal-200 dark:hover:border-slate-700 transition-all">
                  <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-teal-400 to-emerald-500 flex items-center justify-center text-white text-xs font-black">
                    {user?.firstName?.[0]}{user?.lastName?.[0]}
                  </div>
                  <div className="text-left">
                    <p className="text-xs font-bold text-slate-800 dark:text-slate-200 leading-tight">{user?.firstName}</p>
                    <p className="text-[10px] text-teal-600 dark:text-teal-400 font-semibold leading-tight">Dashboard →</p>
                  </div>
                </Link>
                <button onClick={handleLogout}
                  className="hidden md:flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold text-slate-500 dark:text-slate-400 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-500 transition-all">
                  <LogOut size={14} /> Sign Out
                </button>
              </>
            ) : (
              <>
                <Link to="/login"
                  className="hidden md:block px-4 py-2 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:text-teal-600 dark:hover:text-teal-400 transition-colors">
                  Sign In
                </Link>
                <Link to="/register"
                  className="hidden md:flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-600 hover:to-emerald-600 text-white rounded-xl text-sm font-bold transition-all shadow-md shadow-teal-500/20">
                  Get Started
                </Link>
              </>
            )}

            <button onClick={() => setMenuOpen(!menuOpen)}
              className="md:hidden p-2 rounded-xl hover:bg-white/70 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 transition-all">
              {menuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {menuOpen && (
          <div className="md:hidden px-4 py-3 flex flex-col gap-1"
            style={{ borderTop: '1px solid rgba(20,184,166,0.12)', background: theme === 'dark' ? 'rgba(11,18,33,0.95)' : 'rgba(240,250,248,0.97)' }}>
            {publicNav.map(({ to, label }) => (
              <Link key={to} to={to} onClick={() => setMenuOpen(false)}
                className="px-4 py-2.5 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-teal-50 dark:hover:bg-slate-800 transition-colors">
                {label}
              </Link>
            ))}
            <div className="flex gap-2 mt-2 pt-2" style={{ borderTop: '1px solid rgba(20,184,166,0.12)' }}>
              {isAuthenticated ? (
                <>
                  <Link to={dashPath} onClick={() => setMenuOpen(false)}
                    className="flex-1 py-2.5 rounded-xl text-sm font-bold text-center bg-gradient-to-r from-teal-500 to-emerald-500 text-white">
                    Dashboard
                  </Link>
                  <button onClick={handleLogout}
                    className="px-4 py-2.5 rounded-xl text-sm font-semibold border border-slate-200 dark:border-slate-700 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
                    Sign Out
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" onClick={() => setMenuOpen(false)}
                    className="flex-1 py-2.5 rounded-xl text-sm font-semibold text-center border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300">
                    Sign In
                  </Link>
                  <Link to="/register" onClick={() => setMenuOpen(false)}
                    className="flex-1 py-2.5 rounded-xl text-sm font-bold text-center bg-gradient-to-r from-teal-500 to-emerald-500 text-white">
                    Get Started
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      {/* Page content */}
      <main className="flex-1 max-w-6xl mx-auto w-full px-6 py-12">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="mt-8 py-10"
        style={{ borderTop: '1px solid rgba(20,184,166,0.12)', background: theme === 'dark' ? 'rgba(11,18,33,0.8)' : 'rgba(240,250,248,0.9)' }}>
        <div className="max-w-6xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center">
              <Leaf size={15} className="text-white" />
            </div>
            <span className="font-black text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-emerald-600 dark:from-teal-400 dark:to-emerald-400">Mindful</span>
          </div>
          <nav className="flex items-center gap-6 text-sm text-slate-500 dark:text-slate-400">
            {publicNav.map(({ to, label }) => (
              <Link key={to} to={to} className="hover:text-teal-600 dark:hover:text-teal-400 transition-colors">{label}</Link>
            ))}
          </nav>
          <p className="text-xs text-slate-400 dark:text-slate-500">© 2024 Mindful · All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default PublicLayout;

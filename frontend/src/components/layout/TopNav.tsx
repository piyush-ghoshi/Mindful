import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Menu, Sun, Moon, ChevronDown, User, Settings, LogOut, Leaf, HeartPulse } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import NotificationPanel from '../NotificationPanel';

interface TopNavProps {
  onMenuToggle: () => void;
}

const NAV_LINKS = [
  { to: '/dashboard',        label: 'Dashboard' },
  { to: '/resources',        label: 'Resources' },
  { to: '/mood-journal',     label: 'Journal' },
  { to: '/wellness-tracker', label: 'Tracker' },
  { to: '/about',            label: 'About' },
];

const TopNav = ({ onMenuToggle }: TopNavProps) => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [dropOpen, setDropOpen] = useState(false);
  const dropRef = useRef<HTMLDivElement>(null);

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropRef.current && !dropRef.current.contains(e.target as Node)) {
        setDropOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleLogout = async () => {
    setDropOpen(false);
    await logout();
    navigate('/login');
  };

  // Avatar color based on name
  const avatarColors = [
    'from-teal-400 to-emerald-500',
    'from-violet-400 to-purple-500',
    'from-sky-400 to-blue-500',
    'from-rose-400 to-pink-500',
    'from-amber-400 to-orange-500',
  ];
  const colorIdx = (user?.firstName?.charCodeAt(0) ?? 0) % avatarColors.length;
  const avatarGradient = avatarColors[colorIdx];
  const initials = `${user?.firstName?.[0] ?? ''}${user?.lastName?.[0] ?? ''}`.toUpperCase() || 'ME';

  return (
    <header className="sticky top-0 z-40 transition-all duration-300"
      style={{
        background: theme === 'dark'
          ? 'rgba(11, 18, 33, 0.85)'
          : 'rgba(240, 250, 248, 0.88)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderBottom: theme === 'dark' ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(20,184,166,0.12)',
        boxShadow: theme === 'dark' ? '0 4px 20px rgba(0,0,0,0.3)' : '0 2px 16px rgba(20,184,166,0.08)',
      }}>
      <div className="flex items-center justify-between px-4 md:px-6 h-16 max-w-[1440px] mx-auto">

        {/* ── Left: hamburger + brand ── */}
        <div className="flex items-center gap-3">
          <button onClick={onMenuToggle}
            className="md:hidden p-2 rounded-xl hover:bg-teal-50 dark:hover:bg-slate-800 transition-colors"
            aria-label="Toggle menu">
            <Menu size={20} className="text-slate-600 dark:text-slate-400" />
          </button>

          <Link to="/dashboard" className="flex items-center gap-2.5 group">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center shadow-md shadow-teal-500/30 group-hover:shadow-lg group-hover:shadow-teal-500/40 transition-all duration-200">
              <Leaf size={17} className="text-white" />
            </div>
            <span className="text-xl font-black text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-emerald-600 dark:from-teal-400 dark:to-emerald-400 tracking-tight">
              Mindful
            </span>
          </Link>
        </div>

        {/* ── Center: nav links (desktop) ── */}
        <nav className="hidden md:flex items-center gap-0.5 bg-white/50 dark:bg-slate-800/50 rounded-2xl px-2 py-1.5 border border-white/60 dark:border-slate-700/50">
          {NAV_LINKS.map(({ to, label }) => {
            const active = location.pathname === to || location.pathname.startsWith(to + '/');
            return (
              <Link key={to} to={to}
                className={`px-3.5 py-1.5 rounded-xl text-sm font-semibold transition-all duration-150 ${
                  active
                    ? 'bg-gradient-to-r from-teal-500 to-emerald-500 text-white shadow-sm shadow-teal-500/30'
                    : 'text-slate-600 dark:text-slate-400 hover:text-teal-700 dark:hover:text-teal-300 hover:bg-teal-50 dark:hover:bg-slate-700/60'
                }`}>
                {label}
              </Link>
            );
          })}
        </nav>

        {/* ── Right: actions + avatar ── */}
        <div className="flex items-center gap-1.5">

          {/* Dark mode */}
          <button onClick={toggleTheme}
            className="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-white/70 dark:hover:bg-slate-800 transition-all"
            aria-label="Toggle theme">
            {theme === 'dark'
              ? <Sun size={18} className="text-amber-400" />
              : <Moon size={18} className="text-slate-500" />}
          </button>

          {/* Notifications */}
          <NotificationPanel />

          {/* ── Avatar + dropdown ── */}
          <div className="relative" ref={dropRef}>
            <button onClick={() => setDropOpen(!dropOpen)}
              className={`flex items-center gap-2 pl-1 pr-2.5 py-1 rounded-2xl transition-all duration-200 border ${
                dropOpen
                  ? 'bg-white dark:bg-slate-800 border-teal-200 dark:border-teal-700/60 shadow-md'
                  : 'bg-white/60 dark:bg-slate-800/60 border-transparent hover:bg-white dark:hover:bg-slate-800 hover:border-teal-200 dark:hover:border-slate-700'
              }`}>
              {/* Avatar */}
              <div className={`w-8 h-8 rounded-xl bg-gradient-to-br ${avatarGradient} flex items-center justify-center text-white text-xs font-black shadow-sm flex-shrink-0`}>
                {initials}
              </div>
              <div className="hidden sm:block text-left">
                <p className="text-xs font-bold text-slate-800 dark:text-slate-200 leading-tight">{user?.firstName}</p>
                <p className="text-[10px] text-slate-500 dark:text-slate-500 leading-tight capitalize">{user?.role?.toLowerCase()}</p>
              </div>
              <ChevronDown size={14} className={`text-slate-400 transition-transform duration-200 ${dropOpen ? 'rotate-180' : ''}`} />
            </button>

            {/* Dropdown panel */}
            {dropOpen && (
              <div className="absolute right-0 top-full mt-2 w-64 rounded-2xl border border-slate-100 dark:border-slate-700/60 overflow-hidden z-50"
                style={{
                  background: theme === 'dark' ? 'rgba(15,23,42,0.96)' : 'rgba(255,255,255,0.97)',
                  backdropFilter: 'blur(20px)',
                  boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
                }}>

                {/* User info header */}
                <div className={`p-4 bg-gradient-to-br ${avatarGradient} relative overflow-hidden`}>
                  <div className="absolute inset-0 opacity-20"
                    style={{ backgroundImage: 'radial-gradient(circle at 80% 20%, white 0%, transparent 50%)' }} />
                  <div className="relative flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-white/25 border-2 border-white/40 flex items-center justify-center text-white text-lg font-black">
                      {initials}
                    </div>
                    <div>
                      <p className="font-bold text-white text-sm">{user?.firstName} {user?.lastName}</p>
                      <p className="text-white/80 text-xs mt-0.5 truncate max-w-[140px]">{user?.email}</p>
                      <span className="inline-block mt-1 px-2 py-0.5 bg-white/20 rounded-full text-[10px] text-white font-semibold">
                        {user?.role ?? 'STUDENT'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Menu items */}
                <div className="p-2">
                  <Link to="/my-profile" onClick={() => setDropOpen(false)}
                    className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-teal-50 dark:hover:bg-slate-800 transition-colors group">
                    <div className="w-8 h-8 rounded-lg bg-teal-100 dark:bg-teal-900/40 flex items-center justify-center group-hover:bg-teal-200 dark:group-hover:bg-teal-900/60 transition-colors">
                      <User size={15} className="text-teal-600 dark:text-teal-400" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">My Profile</p>
                      <p className="text-xs text-slate-500 dark:text-slate-500">Edit personal info & bio</p>
                    </div>
                  </Link>

                  <Link to="/profile" onClick={() => setDropOpen(false)}
                    className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors group">
                    <div className="w-8 h-8 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center group-hover:bg-slate-200 dark:group-hover:bg-slate-700 transition-colors">
                      <Settings size={15} className="text-slate-500 dark:text-slate-400" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">Account Settings</p>
                      <p className="text-xs text-slate-500 dark:text-slate-500">Password & security</p>
                    </div>
                  </Link>

                  <Link to="/wellness-tracker" onClick={() => setDropOpen(false)}
                    className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-violet-50 dark:hover:bg-slate-800 transition-colors group">
                    <div className="w-8 h-8 rounded-lg bg-violet-100 dark:bg-violet-900/40 flex items-center justify-center group-hover:bg-violet-200 dark:group-hover:bg-violet-900/60 transition-colors">
                      <HeartPulse size={15} className="text-violet-600 dark:text-violet-400" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">Wellness Tracker</p>
                      <p className="text-xs text-slate-500 dark:text-slate-500">Goals & achievements</p>
                    </div>
                  </Link>
                </div>

                <div className="px-2 pb-2 pt-1 border-t border-slate-100 dark:border-slate-700/50">
                  <button onClick={handleLogout}
                    className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors group">
                    <div className="w-8 h-8 rounded-lg bg-red-50 dark:bg-red-900/30 flex items-center justify-center group-hover:bg-red-100 dark:group-hover:bg-red-900/50 transition-colors">
                      <LogOut size={15} className="text-red-500" />
                    </div>
                    <p className="text-sm font-semibold text-red-500">Sign Out</p>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default TopNav;

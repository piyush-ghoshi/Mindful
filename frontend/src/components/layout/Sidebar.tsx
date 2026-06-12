import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Bot, BookOpen, CalendarCheck, Library,
  MessageSquare, TrendingUp, Settings, LogOut, AlertTriangle,
  Users, User, Leaf, Heart,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

interface SidebarProps { open: boolean; onClose: () => void; }

const navItems = [
  { to: '/dashboard',        icon: LayoutDashboard, label: 'Dashboard',       color: 'text-teal-600 dark:text-teal-400',   bg: 'bg-teal-500/10 dark:bg-teal-500/20' },
  { to: '/mood-journal',     icon: BookOpen,        label: 'Mood Journal',     color: 'text-violet-600 dark:text-violet-400', bg: 'bg-violet-500/10 dark:bg-violet-500/20' },
  { to: '/appointments',     icon: CalendarCheck,   label: 'Appointments',     color: 'text-sky-600 dark:text-sky-400',     bg: 'bg-sky-500/10 dark:bg-sky-500/20' },
  { to: '/messages',         icon: MessageSquare,   label: 'Messages',         color: 'text-emerald-600 dark:text-emerald-400', bg: 'bg-emerald-500/10 dark:bg-emerald-500/20' },
  { to: '/forum',            icon: Users,           label: 'Community Forum',  color: 'text-amber-600 dark:text-amber-400', bg: 'bg-amber-500/10 dark:bg-amber-500/20' },
  { to: '/resources',        icon: Library,         label: 'Resource Hub',     color: 'text-rose-600 dark:text-rose-400',   bg: 'bg-rose-500/10 dark:bg-rose-500/20' },
  { to: '/wellness-tracker', icon: TrendingUp,      label: 'Wellness Tracker', color: 'text-orange-600 dark:text-orange-400', bg: 'bg-orange-500/10 dark:bg-orange-500/20' },
  { to: '/mindbot',         icon: Bot,            label: 'AI MindBot',       color: 'text-teal-600 dark:text-teal-400',     bg: 'bg-teal-500/10 dark:bg-teal-500/20' },
  { to: '/my-profile',       icon: User,            label: 'My Profile',       color: 'text-indigo-600 dark:text-indigo-400', bg: 'bg-indigo-500/10 dark:bg-indigo-500/20' },
];

const Sidebar = ({ open, onClose }: SidebarProps) => {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  const handleLogout = async () => { await logout(); navigate('/login'); };

  const avatarColors = ['from-teal-400 to-emerald-500', 'from-violet-400 to-purple-500', 'from-sky-400 to-blue-500', 'from-rose-400 to-pink-500', 'from-amber-400 to-orange-500'];
  const avatarGradient = avatarColors[(user?.firstName?.charCodeAt(0) ?? 0) % avatarColors.length];

  return (
    <>
      <aside className={`
        fixed md:static inset-y-0 left-0 z-30 w-64 h-screen flex flex-col
        bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl
        border-r border-teal-100/60 dark:border-slate-800/60
        shadow-lg dark:shadow-slate-900/50
        transform transition-transform duration-200 ease-out
        ${open ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
      `}>
        <div className="flex flex-col h-full p-3 gap-1">

          {/* Brand */}
          <div className="flex items-center gap-2.5 px-3 pt-3 pb-4 mb-1">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 flex items-center justify-center shadow-md shadow-teal-500/30 flex-shrink-0">
              <Leaf size={17} className="text-white" />
            </div>
            <div>
              <h1 className="text-base font-black text-transparent bg-clip-text bg-gradient-to-r from-teal-600 to-emerald-600 dark:from-teal-400 dark:to-emerald-400 leading-tight">Mindful</h1>
              <p className="text-[10px] text-slate-500 dark:text-slate-500 leading-tight">Student Portal</p>
            </div>
          </div>

          {/* Main nav */}
          <nav className="flex-1 flex flex-col gap-0.5 overflow-y-auto">
            {navItems.map(({ to, icon: Icon, label, color, bg }) => (
              <NavLink key={to} to={to} onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-150 ${
                    isActive
                      ? 'bg-gradient-to-r from-teal-500/10 to-emerald-500/10 dark:from-teal-500/20 dark:to-emerald-500/20 text-teal-700 dark:text-teal-300 shadow-sm border border-teal-200/50 dark:border-teal-700/30'
                      : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800/60 hover:text-slate-900 dark:hover:text-slate-100'
                  }`
                }>
                {({ isActive }) => (
                  <>
                    <div className={`w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0 transition-all ${isActive ? bg : 'bg-transparent'}`}>
                      <Icon size={15} className={isActive ? color : 'text-slate-500 dark:text-slate-500'} />
                    </div>
                    {label}
                  </>
                )}
              </NavLink>
            ))}

            {/* placeholder for future items */}
          </nav>

          {/* Bottom section */}
          <div className="flex flex-col gap-1 pt-3 mt-1 border-t border-teal-100/50 dark:border-slate-800/60">
            {/* Crisis Support */}
            <NavLink to="/crisis-support" onClick={onClose}
              className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30 transition-colors border border-red-100 dark:border-red-800/30">
              <div className="w-7 h-7 rounded-lg bg-red-100 dark:bg-red-900/40 flex items-center justify-center flex-shrink-0">
                <AlertTriangle size={15} className="text-red-600 dark:text-red-400" />
              </div>
              Crisis Support
              <Heart size={13} className="ml-auto text-red-400 fill-red-400" />
            </NavLink>

            <NavLink to="/profile" onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all ${
                  isActive ? 'bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800/60'
                }`
              }>
              <div className="w-7 h-7 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center flex-shrink-0">
                <Settings size={15} className="text-slate-500" />
              </div>
              Settings
            </NavLink>

            {/* User card */}
            <div className="flex items-center justify-between px-3 py-2.5 mt-1 rounded-xl bg-gradient-to-r from-teal-50 to-emerald-50 dark:from-slate-800/80 dark:to-slate-800/80 border border-teal-100/60 dark:border-slate-700/60">
              <div className="flex items-center gap-2.5 min-w-0">
                <div className={`w-9 h-9 rounded-xl bg-gradient-to-br ${avatarGradient} flex items-center justify-center text-white text-sm font-black flex-shrink-0 shadow-sm`}>
                  {user?.firstName?.[0] ?? 'U'}{user?.lastName?.[0] ?? ''}
                </div>
                <div className="min-w-0">
                  <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate leading-tight">{user?.firstName} {user?.lastName}</p>
                  <p className="text-[10px] text-slate-500 dark:text-slate-500 truncate leading-tight">{user?.email}</p>
                </div>
              </div>
              <button onClick={handleLogout}
                className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 text-slate-400 hover:text-red-500 transition-colors flex-shrink-0"
                aria-label="Sign out">
                <LogOut size={15} />
              </button>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;

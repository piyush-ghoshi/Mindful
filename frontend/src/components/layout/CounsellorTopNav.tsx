import { Menu, Search, Sun, Moon } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import NotificationPanel from '../NotificationPanel';

interface Props { onMenuToggle: () => void; }

const CounsellorTopNav = ({ onMenuToggle }: Props) => {
  const { user } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 shadow-sm z-30 sticky top-0">
      <div className="flex items-center justify-between px-6 h-16">
        {/* Left */}
        <div className="flex items-center gap-4">
          <button onClick={onMenuToggle}
            className="md:hidden p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            aria-label="Toggle menu">
            <Menu size={20} className="text-slate-600 dark:text-slate-400" />
          </button>
          <span className="text-xl font-bold text-slate-900 dark:text-white hidden md:block">
            MindCare Portal
          </span>
          {/* Search */}
          <div className="relative hidden md:block">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Search students, notes…"
              className="pl-9 pr-4 py-2 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-full text-sm w-64 focus:outline-none focus:ring-2 focus:ring-teal-500/50 dark:text-slate-200 transition-all"
            />
          </div>
        </div>

        {/* Right */}
        <div className="flex items-center gap-1 text-slate-500 dark:text-slate-400">
          <button onClick={toggleTheme}
            className="p-2 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-full transition-colors"
            aria-label="Toggle theme">
            {theme === 'dark' ? <Sun size={20} className="text-amber-400" /> : <Moon size={20} />}
          </button>
          {/* Notifications */}
          <NotificationPanel />
          <div className="ml-2 flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
            <div className="w-7 h-7 rounded-full bg-teal-600 flex items-center justify-center text-white text-xs font-bold">
              {user?.firstName?.[0] ?? 'C'}
            </div>
            <span className="text-sm font-medium text-slate-700 dark:text-slate-300 hidden sm:block">
              Dr. {user?.lastName ?? user?.firstName}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default CounsellorTopNav;

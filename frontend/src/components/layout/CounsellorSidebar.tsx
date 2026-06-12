import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, CalendarCheck, MessageSquare,
  Users, FileText, Settings, LogOut, Plus, Stethoscope,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

interface Props { open: boolean; onClose: () => void; }

const navItems = [
  { to: '/counsellor/dashboard',    icon: LayoutDashboard, label: 'Dashboard'            },
  { to: '/counsellor/appointments', icon: CalendarCheck,   label: 'Appointments'         },
  { to: '/counsellor/students',     icon: Users,           label: 'Students'             },
  { to: '/counsellor/messages',     icon: MessageSquare,   label: 'Messages'             },
  { to: '/counsellor/notes',        icon: FileText,        label: 'Session Notes'        },
  { to: '/counsellor/availability', icon: CalendarCheck,   label: 'Availability'         },
  { to: '/counsellor/profile',      icon: Stethoscope,     label: 'Professional Profile' },
  { to: '/counsellor/settings',     icon: Settings,        label: 'Account Settings'     },
];

const CounsellorSidebar = ({ open, onClose }: Props) => {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => { await logout(); navigate('/login'); };

  return (
    <aside className={`
      fixed md:static inset-y-0 left-0 z-30
      w-64 bg-white dark:bg-slate-950 border-r border-slate-200 dark:border-slate-800
      flex flex-col h-screen shadow-sm dark:shadow-xl
      transform transition-transform duration-200
      ${open ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
    `}>
      {/* Brand */}
      <div className="px-6 py-6 flex items-center gap-3 border-b border-slate-200 dark:border-slate-800">
        <div className="w-10 h-10 rounded-xl bg-teal-500/10 dark:bg-teal-400/20 border border-teal-500/20 dark:border-teal-400/30 flex items-center justify-center flex-shrink-0">
          <Stethoscope size={20} className="text-teal-600 dark:text-teal-400" />
        </div>
        <div>
          <h1 className="text-sm font-black text-slate-800 dark:text-white uppercase tracking-wider leading-tight">Clinical Portal</h1>
          <p className="text-slate-500 dark:text-slate-400 text-xs mt-0.5">Counsellor Access</p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 flex flex-col gap-0.5 px-2 py-4 overflow-y-auto">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg transition-all ${
                isActive
                  ? 'text-teal-600 dark:text-teal-400 bg-teal-50 dark:bg-slate-900 border-l-4 border-teal-500 dark:border-teal-400 pl-3'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-900/50'
              }`
            }>
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* New Session CTA */}
      <div className="px-4 pb-4">
        <button
          onClick={() => { navigate('/counsellor/appointments'); onClose(); }}
          className="w-full bg-teal-600 hover:bg-teal-500 text-white font-semibold py-2.5 rounded-xl flex items-center justify-center gap-2 transition-colors text-sm mb-3"
        >
          <Plus size={16} /> New Session
        </button>

        {/* User card */}
        <div className="flex items-center justify-between px-3 py-2 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
          <div className="flex items-center gap-2 min-w-0">
            <div className="w-8 h-8 rounded-full bg-teal-600 flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
              {user?.firstName?.[0] ?? 'C'}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-medium text-slate-800 dark:text-white truncate">{user?.firstName} {user?.lastName}</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button onClick={handleLogout}
            className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/30 text-slate-400 hover:text-red-600 dark:hover:text-red-400 transition-colors flex-shrink-0"
            aria-label="Logout">
            <LogOut size={15} />
          </button>
        </div>
      </div>
    </aside>
  );
};

export default CounsellorSidebar;

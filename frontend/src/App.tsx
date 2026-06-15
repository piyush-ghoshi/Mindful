import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { isConfigValid } from './config/firebase';

// ── Public pages ──────────────────────────────────────────────────────────────
import LoginPage         from './pages/LoginPage';
import RegisterPage      from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import AboutPage         from './pages/AboutPage';
import SuccessStoriesPage from './pages/SuccessStoriesPage';
import CrisisSupportPage from './pages/CrisisSupportPage';

// ── Student pages ─────────────────────────────────────────────────────────────
import DashboardPage         from './pages/DashboardPage';
import MoodJournalPage       from './pages/MoodJournalPage';
import AppointmentsPage      from './pages/AppointmentsPage';
import BookAppointmentPage   from './pages/BookAppointmentPage';
import ResourcesPage         from './pages/ResourcesPage';
import WellnessTrackerPage   from './pages/WellnessTrackerPage';
import MessagesPage          from './pages/MessagesPage';
import ForumPage             from './pages/ForumPage';
import ProfilePage           from './pages/ProfilePage';
import StudentProfileSetupPage from './pages/StudentProfileSetupPage';
import MindBotPage           from './pages/MindBotPage';

// ── Counsellor pages ──────────────────────────────────────────────────────────
import CounsellorDashboardPage    from './pages/counsellor/CounsellorDashboardPage';
import CounsellorAppointmentsPage from './pages/counsellor/CounsellorAppointmentsPage';
import CounsellorStudentsPage     from './pages/counsellor/CounsellorStudentsPage';
import CounsellorMessagesPage     from './pages/counsellor/CounsellorMessagesPage';
import CounsellorNotesPage        from './pages/counsellor/CounsellorNotesPage';
import CounsellorAvailabilityPage from './pages/counsellor/CounsellorAvailabilityPage';
import CounsellorProfilePage      from './pages/counsellor/CounsellorProfilePage';

// ── Layouts ───────────────────────────────────────────────────────────────────
import AppLayout          from './components/layout/AppLayout';
import CounsellorLayout   from './components/layout/CounsellorLayout';
import PublicLayout       from './components/layout/PublicLayout';
import ProtectedRoute     from './components/ProtectedRoute';

const StudentRoute = ({ children }: { children: React.ReactNode }) => {
  const { user } = useAuth();
  if (user?.role === 'COUNSELLOR') return <Navigate to="/counsellor/dashboard" replace />;
  return <>{children}</>;
};

const CounsellorRoute = ({ children }: { children: React.ReactNode }) => {
  const { user } = useAuth();
  if (user?.role !== 'COUNSELLOR') return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
};

function App() {
  if (!isConfigValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900 text-slate-100 p-6">
        <div className="max-w-md w-full bg-slate-800 border border-slate-700 rounded-xl p-8 shadow-2xl space-y-6">
          <div className="flex items-center justify-center w-16 h-16 bg-amber-500/10 text-amber-500 rounded-full mx-auto">
            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          
          <div className="text-center space-y-2">
            <h1 className="text-2xl font-bold text-slate-50">Firebase Configuration Missing</h1>
            <p className="text-slate-400 text-sm">
              The application could not start because the required environment variables are not configured in your deployment.
            </p>
          </div>

          <div className="bg-slate-950 rounded-lg p-4 font-mono text-xs text-teal-400 space-y-1 border border-slate-800">
            <p className="text-slate-500">// Missing Vercel Environment Variables:</p>
            <p>• VITE_FIREBASE_API_KEY</p>
            <p>• VITE_FIREBASE_AUTH_DOMAIN</p>
            <p>• VITE_FIREBASE_PROJECT_ID</p>
            <p>• VITE_API_BASE_URL (should be /api)</p>
          </div>

          <div className="space-y-3 text-left">
            <h3 className="text-sm font-semibold text-slate-200">How to fix this:</h3>
            <ol className="text-xs text-slate-400 list-decimal list-inside space-y-2 leading-relaxed">
              <li>Go to your Vercel Project Dashboard → Settings → Environment Variables.</li>
              <li>Add the missing keys (copy values from your local <code>.env.local</code> file).</li>
              <li>Go to the Deployments tab, click the three dots on your latest deployment, and select <strong>Redeploy</strong>.</li>
            </ol>
          </div>
        </div>
      </div>
    );
  }

  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#f9f9ff] dark:bg-slate-950">
        <div className="flex flex-col items-center gap-4">
          <div className="w-12 h-12 rounded-full border-4 border-teal-500 border-t-transparent animate-spin" />
          <p className="text-teal-600 dark:text-teal-400 font-semibold text-lg">Loading Mindful…</p>
        </div>
      </div>
    );
  }

  return (
    <Routes>
      {/* ── Standalone auth pages (no layout) ── */}
      <Route path="/login"          element={<LoginPage />} />
      <Route path="/register"       element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />

      {/* ── Public pages with PublicLayout ── */}
      <Route element={<PublicLayout />}>
        <Route path="/about"           element={<AboutPage />} />
        <Route path="/success-stories" element={<SuccessStoriesPage />} />
        <Route path="/crisis-support"  element={<CrisisSupportPage />} />
      </Route>

      {/* ── Student routes ── */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <StudentRoute>
              <AppLayout />
            </StudentRoute>
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard"        element={<DashboardPage />} />
        <Route path="mood-journal"     element={<MoodJournalPage />} />
        <Route path="appointments"     element={<AppointmentsPage />} />
        <Route path="appointments/book" element={<BookAppointmentPage />} />
        <Route path="resources"        element={<ResourcesPage />} />
        <Route path="crisis-support-app" element={<CrisisSupportPage />} />
        <Route path="wellness-tracker" element={<WellnessTrackerPage />} />
        <Route path="messages"         element={<MessagesPage />} />
        <Route path="forum"            element={<ForumPage />} />
        <Route path="mindbot"          element={<MindBotPage />} />
        <Route path="profile"          element={<ProfilePage />} />
        <Route path="my-profile"       element={<StudentProfileSetupPage />} />
      </Route>

      {/* ── Counsellor routes ── */}
      <Route
        path="/counsellor"
        element={
          <ProtectedRoute>
            <CounsellorRoute>
              <CounsellorLayout />
            </CounsellorRoute>
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/counsellor/dashboard" replace />} />
        <Route path="dashboard"    element={<CounsellorDashboardPage />} />
        <Route path="appointments" element={<CounsellorAppointmentsPage />} />
        <Route path="students"     element={<CounsellorStudentsPage />} />
        <Route path="messages"     element={<CounsellorMessagesPage />} />
        <Route path="notes"        element={<CounsellorNotesPage />} />
        <Route path="availability" element={<CounsellorAvailabilityPage />} />
        <Route path="profile"      element={<CounsellorProfilePage />} />
        <Route path="settings"     element={<ProfilePage />} />
      </Route>

      {/* ── Catch-all ── */}
      <Route path="*" element={<RoleRedirect />} />
    </Routes>
  );
}

const RoleRedirect = () => {
  const { user, isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role === 'COUNSELLOR') return <Navigate to="/counsellor/dashboard" replace />;
  return <Navigate to="/dashboard" replace />;
};

export default App;

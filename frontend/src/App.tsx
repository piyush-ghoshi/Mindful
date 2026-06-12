import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

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

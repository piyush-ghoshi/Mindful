import React, { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type { User as FirebaseUser } from 'firebase/auth';
import { firebaseAuthService } from '../services/firebaseAuthService';
import { setAuthToken, apiClient } from '../services/api';
import type { AuthResponse, User } from '../types';

/** Holds the pending Google user info while the profile-completion modal is open */
export interface PendingGoogleUser {
  suggestedFirstName: string;
  suggestedLastName: string;
}

interface AuthContextType {
  user: User | null;
  firebaseUser: FirebaseUser | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
  /** Set when a new Google user needs to complete their profile */
  pendingGoogleUser: PendingGoogleUser | null;
  register: (email: string, password: string, firstName: string, lastName: string, role?: string) => Promise<AuthResponse>;
  login: (email: string, password: string) => Promise<AuthResponse>;
  /** Triggers Google popup. If new user, sets pendingGoogleUser instead of navigating. */
  signInWithGoogle: () => Promise<{ isNewUser: boolean }>;
  /** Called after the profile-completion modal is submitted */
  completeGoogleSignUp: (firstName: string, lastName: string, role: string) => Promise<void>;
  /** Dismiss pending state (e.g. user closes modal) */
  cancelGoogleSignUp: () => Promise<void>;
  logout: () => Promise<void>;
  updateProfile: (updates: Partial<User>) => Promise<void>;
  sendPasswordResetEmail: (email: string) => Promise<void>;
  confirmPasswordReset: (code: string, newPassword: string) => Promise<void>;
  getIdToken: (forceRefresh?: boolean) => Promise<string | null>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [firebaseUser, setFirebaseUser] = useState<FirebaseUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingGoogleUser, setPendingGoogleUser] = useState<PendingGoogleUser | null>(null);

  // ── Firebase auth state listener ──────────────────────────────────────────
  useEffect(() => {
    const unsubscribe = firebaseAuthService.onAuthStateChanged(async (fbUser) => {
      try {
        setFirebaseUser(fbUser);
        if (fbUser) {
          // Get a fresh token and inject it into the API client immediately
          const idToken = await fbUser.getIdToken(false);
          setAuthToken(idToken);

          const displayName = fbUser.displayName ?? '';
          const [firstName = '', ...rest] = displayName.split(' ');
          const lastName = rest.join(' ');
          let storedRole = localStorage.getItem(`mindful_role_${fbUser.uid}`) ?? 'STUDENT';

          // Fetch profile from backend to sync role (essential if role updated in database directly)
          try {
            const profile = await apiClient.get<any>(`/users/${fbUser.uid}`);
            if (profile && profile.role) {
              storedRole = profile.role;
            }
          } catch (e) {
            console.warn('Could not fetch user profile from backend, using local/default role:', e);
          }
          localStorage.setItem(`mindful_role_${fbUser.uid}`, storedRole);

          setUser({
            id: fbUser.uid,
            email: fbUser.email ?? '',
            firstName,
            lastName,
            role: storedRole as User['role'],
            isEmailVerified: fbUser.emailVerified,
            isActive: true,
            profilePictureUrl: fbUser.photoURL ?? undefined,
            createdAt: fbUser.metadata.creationTime ?? new Date().toISOString(),
          });
        } else {
          setUser(null);
        }
      } catch (err) {
        console.error('Auth state error:', err);
        setError('Failed to load user data');
      } finally {
        setLoading(false);
      }
    });
    return unsubscribe;
  }, []);

  // ── Helpers ───────────────────────────────────────────────────────────────
  const withLoading = async <T,>(fn: () => Promise<T>): Promise<T> => {
    setError(null);
    setLoading(true);
    try {
      return await fn();
    } catch (err) {
      const msg = firebaseAuthService.handleAuthError(err).message;
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Auth actions ──────────────────────────────────────────────────────────
  const register = (email: string, password: string, firstName: string, lastName: string, role = 'STUDENT') =>
    withLoading(() => firebaseAuthService.register(email, password, firstName, lastName, role));

  const login = (email: string, password: string) =>
    withLoading(() => firebaseAuthService.login(email, password));

  /**
   * Google sign-in / sign-up.
   * - Existing user  → resolves with { isNewUser: false }, caller navigates.
   * - New user       → resolves with { isNewUser: true }, pendingGoogleUser is set,
   *                    caller shows the profile-completion modal.
   */
  const signInWithGoogle = (): Promise<{ isNewUser: boolean }> => {
    setError(null);
    return firebaseAuthService.loginWithGoogle()
      .then((result) => {
        if (result.isNewUser) {
          setPendingGoogleUser({
            suggestedFirstName: result.suggestedFirstName,
            suggestedLastName: result.suggestedLastName,
          });
        }
        return { isNewUser: result.isNewUser };
      })
      .catch((err) => {
        const msg = firebaseAuthService.handleAuthError(err).message;
        setError(msg);
        throw new Error(msg);
      });
  };

  /**
   * Called when the profile-completion modal is submitted.
   * Updates Firebase displayName, stores role, refreshes local user state.
   */
  const completeGoogleSignUp = async (firstName: string, lastName: string, role: string) => {
    setError(null);
    setLoading(true);
    try {
      await firebaseAuthService.completeGoogleProfile(firstName, lastName, role);
      // Refresh user state with the new name + role
      const fbUser = firebaseAuthService.getCurrentUser();
      if (fbUser) {
        setUser({
          id: fbUser.uid,
          email: fbUser.email ?? '',
          firstName,
          lastName,
          role: role as User['role'],
          isEmailVerified: fbUser.emailVerified,
          isActive: true,
          profilePictureUrl: fbUser.photoURL ?? undefined,
          createdAt: fbUser.metadata.creationTime ?? new Date().toISOString(),
        });
      }
      setPendingGoogleUser(null);
    } catch (err) {
      const msg = firebaseAuthService.handleAuthError(err).message;
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  };

  /** User closed the modal — sign them out and clear pending state */
  const cancelGoogleSignUp = async () => {
    setPendingGoogleUser(null);
    await firebaseAuthService.logout();
    setUser(null);
    setFirebaseUser(null);
  };

  const logout = async () => {
    setAuthToken(null);
    await firebaseAuthService.logout();
    setUser(null);
    setFirebaseUser(null);
    setPendingGoogleUser(null);
  };

  const updateProfile = async (updates: Partial<User>) => {
    await firebaseAuthService.updateUserProfile(updates);
    setUser((prev) => (prev ? { ...prev, ...updates } : null));
  };

  const sendPasswordResetEmail = (email: string) =>
    firebaseAuthService.sendPasswordResetEmail(email);

  const confirmPasswordReset = (code: string, newPassword: string) =>
    firebaseAuthService.confirmPasswordReset(code, newPassword);

  const getIdToken = (forceRefresh = false) =>
    firebaseAuthService.getIdToken(forceRefresh);

  return (
    <AuthContext.Provider
      value={{
        user,
        firebaseUser,
        loading,
        error,
        isAuthenticated: !!user && !pendingGoogleUser,
        pendingGoogleUser,
        register,
        login,
        signInWithGoogle,
        completeGoogleSignUp,
        cancelGoogleSignUp,
        logout,
        updateProfile,
        sendPasswordResetEmail,
        confirmPasswordReset,
        getIdToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};

/**
 * Firebase Authentication Service — auth only.
 * No Firestore, no database calls. Just Firebase Auth.
 */
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signInWithPopup,
  getAdditionalUserInfo,
  signOut,
  onAuthStateChanged,
  type User as FirebaseUser,
  updateProfile,
  sendPasswordResetEmail,
  confirmPasswordReset,
  verifyPasswordResetCode,
} from 'firebase/auth';
import { auth, googleProvider } from '../config/firebase';
import type { AuthResponse, User } from '../types';

export interface GoogleSignInResult {
  authResponse: AuthResponse;
  isNewUser: boolean;
  suggestedFirstName: string;
  suggestedLastName: string;
}

export class FirebaseAuthService {
  private static instance: FirebaseAuthService;
  private constructor() {}

  static getInstance(): FirebaseAuthService {
    if (!FirebaseAuthService.instance) {
      FirebaseAuthService.instance = new FirebaseAuthService();
    }
    return FirebaseAuthService.instance;
  }

  /** Register with email + password */
  async register(
    email: string,
    password: string,
    firstName: string,
    lastName: string,
    role: string = 'STUDENT'
  ): Promise<AuthResponse> {
    const credential = await createUserWithEmailAndPassword(auth, email, password);
    const firebaseUser = credential.user;

    await updateProfile(firebaseUser, { displayName: `${firstName} ${lastName}` });

    // Role is stored in localStorage — the only local state we keep
    localStorage.setItem(`mindful_role_${firebaseUser.uid}`, role);

    const idToken = await firebaseUser.getIdToken();
    this.storeIdToken(idToken);

    return this.buildAuthResponse(firebaseUser, firstName, lastName, role);
  }

  /** Sign in with email + password */
  async login(email: string, password: string): Promise<AuthResponse> {
    const credential = await signInWithEmailAndPassword(auth, email, password);
    const firebaseUser = credential.user;

    const idToken = await firebaseUser.getIdToken();
    this.storeIdToken(idToken);

    const displayName = firebaseUser.displayName ?? '';
    const [firstName = '', ...rest] = displayName.split(' ');
    const lastName = rest.join(' ');

    return this.buildAuthResponse(firebaseUser, firstName, lastName);
  }

  /** Google sign-in / sign-up */
  async loginWithGoogle(): Promise<GoogleSignInResult> {
    const credential = await signInWithPopup(auth, googleProvider);
    const firebaseUser = credential.user;
    const additionalInfo = getAdditionalUserInfo(credential);

    const idToken = await firebaseUser.getIdToken();
    this.storeIdToken(idToken);

    const displayName = firebaseUser.displayName ?? '';
    const [firstName = '', ...rest] = displayName.split(' ');
    const lastName = rest.join(' ');

    return {
      authResponse: this.buildAuthResponse(firebaseUser, firstName, lastName),
      isNewUser: additionalInfo?.isNewUser ?? false,
      suggestedFirstName: firstName,
      suggestedLastName: lastName,
    };
  }

  /** Called after the Google profile-completion modal is submitted */
  async completeGoogleProfile(firstName: string, lastName: string, role: string): Promise<void> {
    const user = auth.currentUser;
    if (!user) throw new Error('No user logged in');
    await updateProfile(user, { displayName: `${firstName} ${lastName}` });
    localStorage.setItem(`mindful_role_${user.uid}`, role);
  }

  async logout(): Promise<void> {
    await signOut(auth);
    this.clearTokens();
  }

  getCurrentUser(): FirebaseUser | null {
    return auth.currentUser;
  }

  onAuthStateChanged(callback: (user: FirebaseUser | null) => void): () => void {
    return onAuthStateChanged(auth, callback);
  }

  async getIdToken(forceRefresh = false): Promise<string | null> {
    return auth.currentUser?.getIdToken(forceRefresh) ?? null;
  }

  async sendPasswordResetEmail(email: string): Promise<void> {
    await sendPasswordResetEmail(auth, email);
  }

  async verifyPasswordResetCode(code: string): Promise<string> {
    return verifyPasswordResetCode(auth, code);
  }

  async confirmPasswordReset(code: string, newPassword: string): Promise<void> {
    await confirmPasswordReset(auth, code, newPassword);
  }

  async updateUserProfile(updates: Partial<User>): Promise<void> {
    const user = auth.currentUser;
    if (!user) throw new Error('No user logged in');
    if (updates.firstName || updates.lastName) {
      const displayName = `${updates.firstName ?? ''} ${updates.lastName ?? ''}`.trim();
      await updateProfile(user, { displayName });
    }
  }

  // ── Token helpers ──────────────────────────────────────────────────────────

  private storeIdToken(token: string): void {
    localStorage.setItem('firebase_id_token', token);
  }

  getStoredIdToken(): string | null {
    return localStorage.getItem('firebase_id_token');
  }

  private clearTokens(): void {
    localStorage.removeItem('firebase_id_token');
  }

  // ── Build AuthResponse from Firebase user ──────────────────────────────────

  private buildAuthResponse(
    firebaseUser: FirebaseUser,
    firstName: string,
    lastName: string,
    roleOverride?: string
  ): AuthResponse {
    const storedRole = roleOverride
      ?? localStorage.getItem(`mindful_role_${firebaseUser.uid}`)
      ?? 'STUDENT';

    const user: User = {
      id: firebaseUser.uid,
      email: firebaseUser.email ?? '',
      firstName,
      lastName,
      role: storedRole as User['role'],
      isEmailVerified: firebaseUser.emailVerified,
      isActive: true,
      createdAt: firebaseUser.metadata.creationTime ?? new Date().toISOString(),
      profilePictureUrl: firebaseUser.photoURL ?? undefined,
    };

    return { token: this.getStoredIdToken() ?? '', refreshToken: '', user };
  }

  handleAuthError(error: unknown): Error {
    const code = (error as { code?: string }).code ?? '';
    const messages: Record<string, string> = {
      'auth/email-already-in-use': 'An account with this email already exists.',
      'auth/invalid-email': 'Invalid email address.',
      'auth/weak-password': 'Password must be at least 6 characters.',
      'auth/user-not-found': 'No account found with this email.',
      'auth/wrong-password': 'Incorrect password.',
      'auth/user-disabled': 'This account has been disabled.',
      'auth/too-many-requests': 'Too many attempts. Please try again later.',
      'auth/popup-closed-by-user': 'Sign-in popup was closed.',
      'auth/cancelled-popup-request': 'Sign-in cancelled.',
      'auth/network-request-failed': 'Network error. Check your connection.',
    };
    return new Error(messages[code] ?? (error as Error).message ?? 'Authentication failed.');
  }
}

export const firebaseAuthService = FirebaseAuthService.getInstance();

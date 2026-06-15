import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getAnalytics } from 'firebase/analytics';

const isConfigValid = typeof import.meta.env.VITE_FIREBASE_API_KEY === 'string' && import.meta.env.VITE_FIREBASE_API_KEY.length > 0;

let app: any = null;
let auth: any = null;
let googleProvider: any = null;
let db: any = null;
let analytics: any = null;

if (isConfigValid) {
  try {
    const firebaseConfig = {
      apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
      authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
      projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
      storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
      messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
      appId: import.meta.env.VITE_FIREBASE_APP_ID,
      measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID,
    };

    // Initialize Firebase
    app = initializeApp(firebaseConfig);

    // Auth
    auth = getAuth(app);

    // Google provider — configured for popup sign-in
    googleProvider = new GoogleAuthProvider();
    googleProvider.setCustomParameters({ prompt: 'select_account' });

    // Firestore
    db = getFirestore(app);

    // Analytics (only in browser)
    analytics = typeof window !== 'undefined' ? getAnalytics(app) : null;
  } catch (error) {
    console.error('Failed to initialize Firebase with configured credentials:', error);
  }
} else {
  console.warn('Firebase environment variables are missing. App is running in unconfigured mode.');
  
  // Safe mock objects to prevent top-level module resolution crashes
  auth = {
    authStateReady: () => Promise.resolve(),
    onAuthStateChanged: (callback: any) => {
      // Trigger a null state immediately to unblock app loading
      callback(null);
      return () => {};
    },
    currentUser: null,
    getIdToken: () => Promise.resolve(null),
  } as any;
  
  googleProvider = {
    setCustomParameters: () => {},
  } as any;
}

export { app, auth, googleProvider, db, analytics, isConfigValid };
export default app;

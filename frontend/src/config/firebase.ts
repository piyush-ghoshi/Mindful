import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getAnalytics } from 'firebase/analytics';

const isConfigValid = true;

let app: any = null;
let auth: any = null;
let googleProvider: any = null;
let db: any = null;
let analytics: any = null;

if (true) { // Config is always valid now with hardcoded fallbacks
  try {
    const firebaseConfig = {
      apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyC0ZNbKQFXPVgKA8vYx2mzIKH7fcv7qncM",
      authDomain: (typeof window !== 'undefined' && 
                   window.location.hostname !== 'localhost' && 
                   !window.location.hostname.match(/^\d+\.\d+\.\d+\.\d+$/))
                    ? window.location.hostname
                    : (import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "mindful-54fd2.firebaseapp.com"),
      projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "mindful-54fd2",
      storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "mindful-54fd2.firebasestorage.app",
      messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "259988372381",
      appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:259988372381:web:eaae125adb2be6bbd78d74",
      measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || "G-7S3CZYHGGW",
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

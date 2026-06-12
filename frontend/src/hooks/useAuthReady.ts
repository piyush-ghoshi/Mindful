import { useState, useEffect } from 'react';
import { auth } from '../config/firebase';

/**
 * Returns true once Firebase has finished restoring its auth session.
 * Use this to gate API calls that need an auth token.
 *
 * Usage:
 *   const authReady = useAuthReady();
 *   useEffect(() => { if (!authReady) return; fetchData(); }, [authReady]);
 */
export function useAuthReady(): boolean {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    auth.authStateReady()
      .then(() => setReady(true))
      .catch(() => setReady(true)); // even on error, unblock the page
  }, []);

  return ready;
}

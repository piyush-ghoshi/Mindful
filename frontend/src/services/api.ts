import type { ApiError } from '../types';
import { auth } from '../config/firebase';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const API_TIMEOUT = parseInt(import.meta.env.VITE_API_TIMEOUT || '30000', 10);

/** Token cache — set by AuthContext after every successful auth state change */
let _cachedToken: string | null = null;
let _tokenPromise: Promise<string | null> | null = null;

/** Called by AuthContext whenever the user logs in or token refreshes */
export function setAuthToken(token: string | null): void {
  _cachedToken = token;
  if (token) localStorage.setItem('firebase_id_token', token);
  else localStorage.removeItem('firebase_id_token');
}

async function getFreshToken(): Promise<string | null> {
  // If we already have a cached token, return it immediately
  if (_cachedToken) return _cachedToken;

  // Reuse in-flight promise to avoid multiple concurrent token fetches
  if (!_tokenPromise) {
    _tokenPromise = (async () => {
      try {
        await auth.authStateReady();
        if (auth.currentUser) {
          const token = await auth.currentUser.getIdToken(false);
          _cachedToken = token;
          localStorage.setItem('firebase_id_token', token);
          return token;
        }
      } catch { /* fall through */ }
      finally { _tokenPromise = null; }
      return localStorage.getItem('firebase_id_token');
    })();
  }

  return _tokenPromise;
}

async function buildHeaders(contentType = 'application/json'): Promise<HeadersInit> {
  const headers: HeadersInit = { 'Content-Type': contentType };
  const token = await getFreshToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  // Send stored role so backend assigns correct role on first sign-in
  try {
    const currentUser = auth.currentUser;
    if (currentUser) {
      const role = localStorage.getItem(`mindful_role_${currentUser.uid}`) ?? 'STUDENT';
      headers['X-User-Role'] = role;
    } else {
      // Fallback if currentUser is not populated yet
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key?.startsWith('mindful_role_')) {
          const role = localStorage.getItem(key);
          if (role) { headers['X-User-Role'] = role; break; }
        }
      }
    }
  } catch { /* ignore */ }

  return headers;
}

export class ApiClient {
  private static instance: ApiClient;
  private constructor() {}

  static getInstance(): ApiClient {
    if (!ApiClient.instance) ApiClient.instance = new ApiClient();
    return ApiClient.instance;
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const error: ApiError = { code: response.status.toString(), message: response.statusText };
      try {
        const body = await response.json();
        error.message = body.message || error.message;
        error.details = body.details;
      } catch { /* not JSON */ }
      throw error;
    }
    try {
      return await response.json();
    } catch {
      return {} as T;
    }
  }

  async get<T>(endpoint: string): Promise<T> {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), API_TIMEOUT);
    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'GET',
        headers: await buildHeaders(),
        signal: controller.signal,
      });
      return this.handleResponse<T>(res);
    } finally { clearTimeout(id); }
  }

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), API_TIMEOUT);
    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: await buildHeaders(),
        body: data !== undefined ? JSON.stringify(data) : undefined,
        signal: controller.signal,
      });
      return this.handleResponse<T>(res);
    } finally { clearTimeout(id); }
  }

  async put<T>(endpoint: string, data?: unknown): Promise<T> {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), API_TIMEOUT);
    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'PUT',
        headers: await buildHeaders(),
        body: data !== undefined ? JSON.stringify(data) : undefined,
        signal: controller.signal,
      });
      return this.handleResponse<T>(res);
    } finally { clearTimeout(id); }
  }

  async patch<T>(endpoint: string, data?: unknown): Promise<T> {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), API_TIMEOUT);
    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'PATCH',
        headers: await buildHeaders(),
        body: data !== undefined ? JSON.stringify(data) : undefined,
        signal: controller.signal,
      });
      return this.handleResponse<T>(res);
    } finally { clearTimeout(id); }
  }

  async delete<T>(endpoint: string): Promise<T> {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), API_TIMEOUT);
    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'DELETE',
        headers: await buildHeaders(),
        signal: controller.signal,
      });
      return this.handleResponse<T>(res);
    } finally { clearTimeout(id); }
  }

  async uploadFile<T>(endpoint: string, file: File): Promise<T> {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), API_TIMEOUT);
    try {
      const token = await getFreshToken();
      const formData = new FormData();
      formData.append('file', file);
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: formData,
        signal: controller.signal,
      });
      return this.handleResponse<T>(res);
    } finally { clearTimeout(id); }
  }
}

export const apiClient = ApiClient.getInstance();

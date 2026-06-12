import { apiClient } from './api';
import type { Appointment } from '../types';

export interface BookAppointmentPayload {
  counsellorId: string;
  startTime: string;
  appointmentType: string;
  reason?: string;
  studentNotes?: string;
}

export interface AppointmentListResponse {
  content?: Appointment[];
  data?: Appointment[];
}

export const appointmentService = {
  /** Get all appointments for the current user. */
  async getAppointments(): Promise<Appointment[]> {
    const res = await apiClient.get<Appointment[] | AppointmentListResponse>('/appointments');
    if (Array.isArray(res)) return res;
    return (res as AppointmentListResponse).content ?? (res as AppointmentListResponse).data ?? [];
  },

  /** Get upcoming appointments. */
  async getUpcoming(): Promise<Appointment[]> {
    const res = await apiClient.get<Appointment[] | AppointmentListResponse>('/appointments/upcoming');
    if (Array.isArray(res)) return res;
    return (res as AppointmentListResponse).content ?? (res as AppointmentListResponse).data ?? [];
  },

  /** Book a new appointment. */
  book(payload: BookAppointmentPayload): Promise<Appointment> {
    return apiClient.post<Appointment>('/appointments', payload);
  },

  /** Confirm (accept) an appointment — counsellor only. */
  confirm(appointmentId: string): Promise<Appointment> {
    return apiClient.post<Appointment>(`/appointments/${appointmentId}/confirm`, {});
  },

  /** Cancel an appointment. */
  cancel(appointmentId: string, reason?: string): Promise<void> {
    return apiClient.post<void>(`/appointments/${appointmentId}/cancel`, { reason });
  },

  /** Reschedule an appointment. */
  reschedule(appointmentId: string, newStartTime: string): Promise<Appointment> {
    return apiClient.put<Appointment>(`/appointments/${appointmentId}/reschedule`, { newStartTime });
  },

  /** Mark an appointment as complete (counsellor only). */
  markComplete(appointmentId: string, notes?: string): Promise<void> {
    return apiClient.put<void>(`/appointments/${appointmentId}/complete`, { notes });
  },
};

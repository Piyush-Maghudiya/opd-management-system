export type AppointmentStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | string;

export interface Appointment {
  id: number;
  patientId: number;
  patientName?: string;
  doctorName: string;
  appointmentDate: string;
  appointmentTime: string;
  status: AppointmentStatus;
}

export interface AppointmentCreateRequest {
  patientId: number;
  doctorName: string;
  appointmentDate: string;
  appointmentTime: string;
}

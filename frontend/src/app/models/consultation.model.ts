export type ConsultationStatus = 'IN_PROGRESS' | 'COMPLETED' | string;

export interface Consultation {
  id: number;
  appointmentId: number;
  patientId?: number;
  patientName?: string;
  doctorName?: string;
  appointmentDate?: string;
  bloodPressure?: string;
  temperature?: number;
  notes?: string;
  status: ConsultationStatus;
}

export interface ConsultationCreateRequest {
  appointmentId: number;
  bloodPressure: string;
  temperature: number;
  notes: string;
}

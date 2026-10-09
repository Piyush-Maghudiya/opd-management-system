import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Consultation, ConsultationCreateRequest } from '../models/consultation.model';

@Injectable({ providedIn: 'root' })
export class ConsultationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/consultations`;

  saveConsultation(payload: ConsultationCreateRequest): Observable<Consultation> {
    return this.http.post<Consultation>(this.baseUrl, payload);
  }

  completeConsultation(id: number): Observable<Consultation> {
    return this.http.patch<Consultation>(`${this.baseUrl}/${id}/complete`, {});
  }

  getConsultationsByPatient(patientId: number): Observable<Consultation[]> {
    return this.http.get<Consultation[]>(
      `${environment.apiBaseUrl}/patients/${patientId}/consultations`
    );
  }
}

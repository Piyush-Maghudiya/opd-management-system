import { Routes } from '@angular/router';
import { PatientComponent } from './components/patient/patient.component';
import { AppointmentComponent } from './components/appointment/appointment.component';
import { ConsultationComponent } from './components/consultation/consultation.component';

export const routes: Routes = [
  { path: '', redirectTo: 'patients', pathMatch: 'full' },
  { path: 'patients', component: PatientComponent, title: 'Patient Registration' },
  { path: 'appointments', component: AppointmentComponent, title: 'Appointment Booking' },
  { path: 'consultations', component: ConsultationComponent, title: 'Consultation Summary' },
  { path: '**', redirectTo: 'patients' }
];

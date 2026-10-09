import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AppointmentService } from '../../services/appointment.service';
import { PatientService } from '../../services/patient.service';
import { ConsultationService } from '../../services/consultation.service';
import { Appointment } from '../../models/appointment.model';
import { Patient } from '../../models/patient.model';
import { Consultation } from '../../models/consultation.model';
import { getErrorMessage } from '../../utils/http-error.util';

@Component({
  selector: 'app-consultation',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './consultation.component.html',
  styleUrl: './consultation.component.css'
})
export class ConsultationComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly appointmentService = inject(AppointmentService);
  private readonly patientService = inject(PatientService);
  private readonly consultationService = inject(ConsultationService);

  appointments: Appointment[] = [];
  patients: Patient[] = [];
  historyPatientId: number | null = null;
  consultations: Consultation[] = [];

  loadingAppointments = false;
  loadingPatients = false;
  loadingHistory = false;
  saving = false;
  completing = false;

  successMessage = '';
  errorMessage = '';

  /** Set after a successful POST so we can complete without creating duplicates. */
  activeConsultationId: number | null = null;
  activeConsultationStatus: string | null = null;

  form = this.fb.nonNullable.group({
    appointmentId: [null as number | null, Validators.required],
    bloodPressure: ['', [Validators.required, Validators.pattern(/^\d{2,3}\/\d{2,3}$/)]],
    temperature: [null as number | null, [Validators.required, Validators.min(90), Validators.max(110)]],
    notes: ['', [Validators.required, Validators.minLength(3)]]
  });

  ngOnInit(): void {
    this.loadAppointments();
    this.loadPatients();
  }

  loadAppointments(): void {
    this.loadingAppointments = true;
    this.appointmentService.getTodayAppointments().subscribe({
      next: (data) => {
        this.appointments = data;
        this.loadingAppointments = false;
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, 'Failed to load appointments.');
        this.loadingAppointments = false;
      }
    });
  }

  loadPatients(): void {
    this.loadingPatients = true;
    this.patientService.getPatients().subscribe({
      next: (data) => {
        this.patients = data;
        this.loadingPatients = false;
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, 'Failed to load patients.');
        this.loadingPatients = false;
      }
    });
  }

  onAppointmentChange(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.activeConsultationId = null;
    this.activeConsultationStatus = null;
    this.form.patchValue({ bloodPressure: '', temperature: null, notes: '' });
  }

  selectedAppointment(): Appointment | undefined {
    const id = this.form.controls.appointmentId.value;
    return this.appointments.find((a) => a.id === id);
  }

  saveConsultation(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.isConsultationLocked()) {
      this.errorMessage = 'This consultation is already completed and cannot be modified.';
      return;
    }

    if (this.activeConsultationId) {
      this.errorMessage =
        'Consultation already saved for this appointment. Use Complete Consultation or select another appointment.';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving = true;

    this.consultationService
      .saveConsultation({
        appointmentId: value.appointmentId as number,
        bloodPressure: value.bloodPressure.trim(),
        temperature: value.temperature as number,
        notes: value.notes.trim()
      })
      .subscribe({
        next: (created) => {
          this.activeConsultationId = created.id;
          this.activeConsultationStatus = created.status;
          this.successMessage = 'Consultation saved. You can mark it complete when ready.';
          this.saving = false;
          this.refreshHistoryIfMatchingPatient();
        },
        error: (err) => {
          this.errorMessage = getErrorMessage(err, 'Failed to save consultation.');
          this.saving = false;
        }
      });
  }

  completeConsultation(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.activeConsultationId) {
      this.errorMessage = 'Save the consultation first before completing it.';
      return;
    }

    if (this.activeConsultationStatus === 'COMPLETED') {
      this.errorMessage = 'This consultation is already completed.';
      return;
    }

    this.completing = true;
    this.consultationService.completeConsultation(this.activeConsultationId).subscribe({
      next: (updated) => {
        this.activeConsultationStatus = updated.status;
        this.successMessage = 'Consultation marked as complete.';
        this.completing = false;
        this.loadAppointments();
        this.refreshHistoryIfMatchingPatient();
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, 'Failed to complete consultation.');
        this.completing = false;
      }
    });
  }

  onHistoryPatientChange(): void {
    if (this.historyPatientId == null) {
      this.consultations = [];
      return;
    }
    this.loadConsultationHistory(this.historyPatientId);
  }

  loadConsultationHistory(patientId: number): void {
    this.loadingHistory = true;
    this.consultationService.getConsultationsByPatient(patientId).subscribe({
      next: (data) => {
        this.consultations = data.filter((c) => c.status === 'COMPLETED');
        this.loadingHistory = false;
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, 'Failed to load consultation history.');
        this.loadingHistory = false;
      }
    });
  }

  private refreshHistoryIfMatchingPatient(): void {
    const appointment = this.selectedAppointment();
    if (appointment && this.historyPatientId === appointment.patientId) {
      this.loadConsultationHistory(appointment.patientId);
    }
  }

  isConsultationLocked(): boolean {
    return this.activeConsultationStatus === 'COMPLETED';
  }

  displayPatientNameForAppointment(appointment: Appointment): string {
    if (appointment.patientName) {
      return appointment.patientName;
    }
    const patient = this.patients.find((p) => p.id === appointment.patientId);
    return patient ? patient.name : `Patient #${appointment.patientId}`;
  }

  fieldError(field: 'appointmentId' | 'bloodPressure' | 'temperature' | 'notes'): string {
    const control = this.form.controls[field];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (field === 'bloodPressure' && control.hasError('pattern')) {
      return 'Use format like 120/80.';
    }
    if (field === 'temperature' && control.hasError('min')) {
      return 'Enter a realistic temperature (°F).';
    }
    if (field === 'temperature' && control.hasError('max')) {
      return 'Enter a realistic temperature (°F).';
    }
    if (field === 'notes' && control.hasError('minlength')) {
      return 'Notes must be at least 3 characters.';
    }
    return '';
  }
}

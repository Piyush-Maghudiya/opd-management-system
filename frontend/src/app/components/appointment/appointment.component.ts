import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AppointmentService } from '../../services/appointment.service';
import { PatientService } from '../../services/patient.service';
import { Patient } from '../../models/patient.model';
import { Appointment } from '../../models/appointment.model';
import { getErrorMessage } from '../../utils/http-error.util';

@Component({
  selector: 'app-appointment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './appointment.component.html',
  styleUrl: './appointment.component.css'
})
export class AppointmentComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly appointmentService = inject(AppointmentService);
  private readonly patientService = inject(PatientService);

  patients: Patient[] = [];
  todayAppointments: Appointment[] = [];
  loadingPatients = false;
  loadingAppointments = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';

  form = this.fb.nonNullable.group({
    patientId: [null as number | null, Validators.required],
    doctorName: ['', [Validators.required, Validators.minLength(2)]],
    appointmentDate: ['', Validators.required],
    appointmentTime: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadPatients();
    this.loadTodayAppointments();
    this.form.patchValue({ appointmentDate: this.todayDateString() });
  }

  private todayDateString(): string {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  loadPatients(): void {
    this.loadingPatients = true;
    this.patientService.getPatients().subscribe({
      next: (data) => {
        this.patients = data;
        this.loadingPatients = false;
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, 'Failed to load patients for booking.');
        this.loadingPatients = false;
      }
    });
  }

  loadTodayAppointments(): void {
    this.loadingAppointments = true;
    this.appointmentService.getTodayAppointments().subscribe({
      next: (data) => {
        this.todayAppointments = data;
        this.loadingAppointments = false;
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, "Failed to load today's appointments.");
        this.loadingAppointments = false;
      }
    });
  }

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.submitting = true;

    this.appointmentService
      .bookAppointment({
        patientId: value.patientId as number,
        doctorName: value.doctorName.trim(),
        appointmentDate: value.appointmentDate,
        appointmentTime: this.normalizeTime(value.appointmentTime)
      })
      .subscribe({
        next: () => {
          this.successMessage = 'Appointment booked successfully.';
          this.form.patchValue({
            doctorName: '',
            appointmentTime: '',
            appointmentDate: this.todayDateString()
          });
          this.submitting = false;
          this.loadTodayAppointments();
        },
        error: (err) => {
          this.errorMessage = getErrorMessage(err, 'Booking failed.');
          this.submitting = false;
        }
      });
  }

  /** HTML time input returns HH:mm; backend may expect HH:mm:ss (Jackson). */
  private normalizeTime(time: string): string {
    return time.length === 5 ? `${time}:00` : time;
  }

  displayPatientName(appointment: Appointment): string {
    if (appointment.patientName) {
      return appointment.patientName;
    }
    const patient = this.patients.find((p) => p.id === appointment.patientId);
    return patient ? patient.name : `Patient #${appointment.patientId}`;
  }

  fieldError(field: 'patientId' | 'doctorName' | 'appointmentDate' | 'appointmentTime'): string {
    const control = this.form.controls[field];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (field === 'doctorName' && control.hasError('minlength')) {
      return 'Doctor name must be at least 2 characters.';
    }
    return '';
  }
}

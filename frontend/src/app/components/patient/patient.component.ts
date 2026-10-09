import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PatientService } from '../../services/patient.service';
import { Patient } from '../../models/patient.model';
import { getErrorMessage } from '../../utils/http-error.util';

@Component({
  selector: 'app-patient',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './patient.component.html',
  styleUrl: './patient.component.css'
})
export class PatientComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly patientService = inject(PatientService);

  patients: Patient[] = [];
  loadingList = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  searchTerm = '';

  readonly genders = ['Male', 'Female', 'Other'];

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    gender: ['', Validators.required],
    age: [null as number | null, [Validators.required, Validators.min(1), Validators.max(150)]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]{7,15}$/)]]
  });

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.loadingList = true;
    this.errorMessage = '';
    this.patientService.getPatients(this.searchTerm).subscribe({
      next: (data) => {
        this.patients = data;
        this.loadingList = false;
      },
      error: (err) => {
        this.errorMessage = getErrorMessage(err, 'Failed to load patients.');
        this.loadingList = false;
      }
    });
  }

  onSearch(): void {
    this.loadPatients();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.loadPatients();
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

    this.patientService
      .registerPatient({
        name: value.name.trim(),
        gender: value.gender,
        age: value.age as number,
        phone: value.phone.trim()
      })
      .subscribe({
        next: () => {
          this.successMessage = 'Patient registered successfully.';
          this.form.reset({ name: '', gender: '', age: null, phone: '' });
          this.submitting = false;
          this.loadPatients();
        },
        error: (err) => {
          this.errorMessage = getErrorMessage(err, 'Registration failed.');
          this.submitting = false;
        }
      });
  }

  fieldError(field: 'name' | 'gender' | 'age' | 'phone'): string {
    const control = this.form.controls[field];
    if (!control.touched && !control.dirty) {
      return '';
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (field === 'name' && control.hasError('minlength')) {
      return 'Name must be at least 2 characters.';
    }
    if (field === 'age' && control.hasError('min')) {
      return 'Age must be at least 1.';
    }
    if (field === 'age' && control.hasError('max')) {
      return 'Age must be 150 or less.';
    }
    if (field === 'phone' && control.hasError('pattern')) {
      return 'Enter a valid phone number (7–15 digits).';
    }
    return '';
  }
}

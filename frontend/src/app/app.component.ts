import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly navItems = [
    { path: '/patients', label: 'Patient Registration' },
    { path: '/appointments', label: 'Appointment Booking' },
    { path: '/consultations', label: 'Consultation Summary' }
  ];
}

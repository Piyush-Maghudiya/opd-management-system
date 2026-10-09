export interface Patient {
  id: number;
  name: string;
  gender: string;
  age: number;
  phone: string;
}

export interface PatientCreateRequest {
  name: string;
  gender: string;
  age: number;
  phone: string;
}

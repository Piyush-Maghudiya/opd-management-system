import { HttpErrorResponse } from '@angular/common/http';

/** Turn Spring Boot / network errors into a user-friendly message. */
export function getErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (error.status === 0) {
    return 'Cannot reach the server. Is the backend running at http://localhost:8080?';
  }

  const body = error.error;
  if (typeof body === 'string' && body.trim()) {
    return body;
  }

  if (body && typeof body === 'object') {
    const record = body as Record<string, unknown>;
    if (typeof record['message'] === 'string') {
      return record['message'] as string;
    }
    if (Array.isArray(record['errors'])) {
      return (record['errors'] as string[]).join(', ');
    }
    if (record['error'] && typeof record['error'] === 'string') {
      return record['error'] as string;
    }
  }

  return error.message || fallback;
}

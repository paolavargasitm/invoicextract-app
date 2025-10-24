export function parseBody(text: string | null | undefined): any {
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

export function toUserError(res: Response | { status?: number }, parsedBody: any): Error & { status?: number; code?: any; body?: any; details?: any } {
  const status = (res as Response)?.status ?? 0;
  const backendMsg = typeof parsedBody === 'string'
    ? parsedBody
    : parsedBody?.error?.message || parsedBody?.message || null;

  const defaultMessage = httpMessageForStatus(status);
  const message = backendMsg || defaultMessage || 'Ocurrió un error. Intenta nuevamente más tarde.';

  const err: any = new Error(message);
  err.status = status;
  err.code = (typeof parsedBody === 'object' && (parsedBody?.error?.code || parsedBody?.code)) || status;
  err.body = parsedBody;
  err.details = extractDetails(parsedBody);
  return err;
}

function httpMessageForStatus(status?: number): string {
  if (!status) return 'Error de red. Verifica tu conexión.';
  if (status === 400) return 'Solicitud inválida. Revisa los datos ingresados.';
  if (status === 401) return 'No autorizado. Inicia sesión nuevamente.';
  if (status === 403) return 'Acceso denegado. No tienes permisos para esta acción.';
  if (status === 404) return 'Recurso no encontrado.';
  if (status === 409) return 'Conflicto. La operación no pudo completarse.';
  if (status === 422) return 'Datos inválidos. Corrige los campos marcados.';
  if (status >= 500) return 'Error del servidor. Intenta más tarde.';
  return `Error (HTTP ${status}).`;
}

function extractDetails(parsedBody: any) {
  if (!parsedBody || typeof parsedBody !== 'object') return null;
  const errs = parsedBody.errors || parsedBody.violations || parsedBody.fieldErrors;
  if (Array.isArray(errs) && errs.length) return errs;
  return null;
}

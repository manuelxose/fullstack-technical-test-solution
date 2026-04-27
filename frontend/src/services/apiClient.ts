const DEFAULT_API_BASE_URL = import.meta.env.DEV ? '' : 'https://itx-frontend-test.onrender.com';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? DEFAULT_API_BASE_URL;
const REQUEST_TIMEOUT_MS = 10000;

export class ApiError extends Error {
  constructor(message: string, public readonly status: number) {
    super(message);
  }
}

export async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
  const request = createRequestSignal(signal);
  try {
    const response = await fetch(`${API_BASE_URL}${path}`, { signal: request.signal });
    if (!response.ok) throw new ApiError(`GET ${path} failed`, response.status);
    return response.json() as Promise<T>;
  } finally {
    request.cleanup();
  }
}

export async function postJson<TBody, TResponse>(path: string, body: TBody, signal?: AbortSignal): Promise<TResponse> {
  const request = createRequestSignal(signal);
  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      signal: request.signal
    });
    if (!response.ok) throw new ApiError(`POST ${path} failed`, response.status);
    return response.json() as Promise<TResponse>;
  } finally {
    request.cleanup();
  }
}

function createRequestSignal(parentSignal?: AbortSignal): { signal: AbortSignal; cleanup: () => void } {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => {
    controller.abort(new DOMException('Request timed out', 'TimeoutError'));
  }, REQUEST_TIMEOUT_MS);
  const abortFromParent = () => controller.abort(parentSignal?.reason);

  if (parentSignal?.aborted) {
    abortFromParent();
  } else {
    parentSignal?.addEventListener('abort', abortFromParent, { once: true });
  }

  return {
    signal: controller.signal,
    cleanup: () => {
      window.clearTimeout(timeoutId);
      parentSignal?.removeEventListener('abort', abortFromParent);
    }
  };
}

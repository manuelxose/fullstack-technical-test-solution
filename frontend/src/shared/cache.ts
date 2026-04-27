interface CacheEntry<T> {
  expiresAt: number;
  value: T;
}

const DEFAULT_TTL_MS = 60 * 60 * 1000;

export class LocalStorageCache {
  constructor(private readonly prefix = 'itx-cache') {}

  get<T>(key: string): T | null {
    try {
      const raw = window.localStorage.getItem(this.buildKey(key));
      if (!raw) return null;
      const entry = JSON.parse(raw) as CacheEntry<T>;
      if (Date.now() > entry.expiresAt) {
        window.localStorage.removeItem(this.buildKey(key));
        return null;
      }
      return entry.value;
    } catch {
      return null;
    }
  }

  set<T>(key: string, value: T, ttlMs = DEFAULT_TTL_MS): void {
    try {
      const entry: CacheEntry<T> = { value, expiresAt: Date.now() + ttlMs };
      window.localStorage.setItem(this.buildKey(key), JSON.stringify(entry));
    } catch {
      // If storage is not available, the app still works without cache.
    }
  }

  private buildKey(key: string): string {
    return `${this.prefix}:${key}`;
  }
}

export const cache = new LocalStorageCache();

/**
 * Universal provider adapter: configuration-driven provider support.
 *
 * Any provider speaking the OpenAI chat-completions shape — or the Anthropic
 * messages shape — can be added at runtime purely through configuration (base
 * URL + API key + schema family). Built-in named providers are just pre-filled
 * instances of the same two adapters; no per-provider logic exists anywhere.
 * For OpenAI-shaped providers, model lists are fetched live from the
 * provider's /models endpoint so newly released models appear without an app
 * update; Anthropic-shaped providers and endpoints without a models listing
 * fall back to user-typed model ids.
 *
 * Author: AjiroDesu
 */
import { fetchWithTimeout } from "@/core/fetch-with-timeout";
import type { ProviderFamily } from "@/core/types/app-state";

/** Wire-format families the universal adapter speaks natively. */
export type ProviderSchemaFamily = Extract<
  ProviderFamily,
  "openai-compatible" | "anthropic"
>;

export type UniversalProviderConfig = {
  /** Any stable id; custom providers use "custom-<uuid>". */
  apiKey?: string | null;
  baseUrl: string;
  family: ProviderSchemaFamily;
  headers?: Record<string, string>;
  id: string;
};export type UniversalModelInfo = {
  contextWindow: number | null;
  id: string;
  source: "live" | "cache" | "manual";
};

export type FetchProviderModelsResult = {
  error: string | null;
  models: UniversalModelInfo[];
  source: "live" | "cache" | "manual";
};

const MODELS_TTL_MS = 5 * 60 * 1000;
const REQUEST_TIMEOUT_MS = 15_000;

const modelsCache = new Map<
  string,
  { expiresAt: number; models: UniversalModelInfo[] }
>();

export function invalidateProviderModels(providerId?: string) {
  if (providerId) {
    modelsCache.delete(providerId);
  } else {
    modelsCache.clear();
  }
}

function normalizeBaseUrl(baseUrl: string) {
  return baseUrl.trim().replace(/\/+$/, "");
}

function authHeaders(config: UniversalProviderConfig): Record<string, string> {
  const headers: Record<string, string> = {
    ...(config.headers ?? {}),
  };

  if (config.apiKey) {
    if (config.family === "anthropic") {
      headers["x-api-key"] = config.apiKey;
      headers["anthropic-version"] = "2023-06-01";
    } else {
      headers.Authorization = `Bearer ${config.apiKey}`;
    }
  }

  return headers;
}

/**
 * Live model listing. For openai-shaped providers this hits GET {base}/models;
 * for anthropic-shaped providers it hits the v1 models endpoint when available.
 * Falls back to the last cached list on failure. `manualModels` (user-typed
 * ids) are always appended and labeled "manual".
 */
export async function fetchProviderModels(
  config: UniversalProviderConfig,
  options: {
    forceRefresh?: boolean;
    manualModels?: string[];
    signal?: AbortSignal;
  } = {},
): Promise<FetchProviderModelsResult> {
  const manual: UniversalModelInfo[] = (options.manualModels ?? [])
    .map((id) => id.trim())
    .filter(Boolean)
    .map((id) => ({
      contextWindow: null,
      id,
      source: "manual" as const,
    }));

  const cached = modelsCache.get(config.id);
  const cacheValid =
    cached &&
    cached.expiresAt > Date.now() &&
    (!options.forceRefresh || cached.models.length > 0);

  if (cacheValid && !options.forceRefresh) {
    return mergeModelSources(cached!.models, manual, "cache");
  }

  try {
    const models = await fetchModelsFromApi(config, options.signal);
    modelsCache.set(config.id, {
      expiresAt: Date.now() + MODELS_TTL_MS,
      models,
    });

    return mergeModelSources(models, manual, "live");
  } catch (error) {
    if (cached && cached.models.length > 0) {
      // Stale-cache fallback beats an empty picker.
      return {
        error:
          error instanceof Error
            ? `Live refresh failed (${error.message}); showing the last cached list.`
            : "Live refresh failed; showing the last cached list.",
        models: mergeModelSources(cached.models, manual, "cache").models,
        source: "cache",
      };
    }

    return {
      error:
        error instanceof Error
          ? error.message
          : "Could not fetch the model list.",
      models: manual,
      source: "manual",
    };
  }
}

function mergeModelSources(
  fetched: UniversalModelInfo[],
  manual: UniversalModelInfo[],
  source: "live" | "cache",
): FetchProviderModelsResult {
  const seen = new Set(fetched.map((model) => model.id));
  const tagged = fetched.map(
    (model): UniversalModelInfo => ({ ...model, source }),
  );
  const merged: UniversalModelInfo[] = [...tagged];

  for (const model of manual) {
    if (!seen.has(model.id)) {
      merged.push(model);
    }
  }

  return {
    error: null,
    models: merged,
    source,
  };
}

async function fetchModelsFromApi(
  config: UniversalProviderConfig,
  signal?: AbortSignal,
): Promise<UniversalModelInfo[]> {
  const base = normalizeBaseUrl(config.baseUrl);
  const url = `${base}/models`;

  const response = await fetchWithTimeout(
    url,
    { headers: authHeaders(config), signal },
    REQUEST_TIMEOUT_MS,
  );

  if (!response.ok) {
    const detail =
      response.status === 401 || response.status === 403
        ? "invalid credentials"
        : `HTTP ${response.status}`;
    throw new Error(`Model listing failed: ${detail}`);
  }

  const payload = (await response.json()) as {
    data?: { id?: string; context_length?: number }[];
    models?: { id?: string }[];
  };

  const rawList: { id?: string; context_length?: number }[] =
    Array.isArray(payload.data)
      ? payload.data
      : Array.isArray(payload.models)
        ? payload.models
        : [];

  const models: UniversalModelInfo[] = [];

  for (const item of rawList) {
    if (typeof item?.id === "string" && item.id.trim()) {
      models.push({
        contextWindow:
          typeof item.context_length === "number" ? item.context_length : null,
        id: item.id.trim(),
        source: "live",
      });
    }
  }

  return models.sort((left, right) => left.id.localeCompare(right.id));
}

/**
 * Lightweight credential/connection test used at setup time. A 200 from the
 * models endpoint (any list shape) proves reachability + auth; a 401/403
 * reports bad credentials; anything else reports reachability problems.
 */
export type ProviderConnectionTestResult = {
  message: string;
  ok: boolean;
};

export async function testProviderConnection(
  config: UniversalProviderConfig,
): Promise<ProviderConnectionTestResult> {
  const base = normalizeBaseUrl(config.baseUrl);

  if (!/^https?:\/\//i.test(base)) {
    return {
      message: "Base URL must start with http:// or https://",
      ok: false,
    };
  }

  try {
    const response = await fetchWithTimeout(
      `${base}/models`,
      { headers: authHeaders(config) },
      REQUEST_TIMEOUT_MS,
    );

    if (response.ok) {
      return { message: "Connection OK — credentials accepted.", ok: true };
    }

    if (response.status === 401 || response.status === 403) {
      return {
        message: "Reached the provider, but the API key was rejected.",
        ok: false,
      };
    }

    return {
      message: `Provider reachable but returned HTTP ${response.status}.`,
      ok: false,
    };
  } catch (error) {
    return {
      message:
        error instanceof Error
          ? `Could not reach the provider: ${error.message}`
          : "Could not reach the provider.",
      ok: false,
    };
  }
}

/**
 * Capability negotiation defaults per schema family. Live catalog tags and
 * user overrides refine these; the runtime degrades gracefully when a
 * capability is missing (buffer non-streaming, disable tools with a message).
 */
export type ProviderCapabilityDefaults = {
  imageInput: boolean;
  jsonMode: boolean;
  streaming: boolean;
  tools: boolean;
};

export function getCapabilityDefaults(
  family: ProviderSchemaFamily,
): ProviderCapabilityDefaults {
  if (family === "anthropic") {
    return { imageInput: true, jsonMode: false, streaming: true, tools: true };
  }

  // openai-compatible: streaming is universally supported by the chat
  // completions shape; tools/vision depend on the model and get refined by
  // hints. Conservative defaults keep unknown providers working.
  return { imageInput: false, jsonMode: true, streaming: true, tools: true };
}

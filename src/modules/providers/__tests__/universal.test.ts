import { beforeEach, describe, expect, it, vi } from "vitest";

import {
  fetchProviderModels,
  getCapabilityDefaults,
  invalidateProviderModels,
  testProviderConnection,
  type UniversalProviderConfig,
} from "@/modules/providers/universal";

function openAiConfig(overrides: Partial<UniversalProviderConfig> = {}) {
  return {
    apiKey: "sk-test",
    baseUrl: "https://provider.example/v1/",
    family: "openai-compatible" as const,
    id: "test-provider",
    ...overrides,
  };
}

describe("universal provider adapter", () => {
  beforeEach(() => {
    invalidateProviderModels();
  });

  describe("getCapabilityDefaults", () => {
    it("assumes streaming+tools for openai-compatible, conservative vision", () => {
      const defaults = getCapabilityDefaults("openai-compatible");

      expect(defaults.streaming).toBe(true);
      expect(defaults.tools).toBe(true);
      expect(defaults.imageInput).toBe(false);
    });

    it("enables vision for anthropic-shaped providers", () => {
      const defaults = getCapabilityDefaults("anthropic");

      expect(defaults.imageInput).toBe(true);
      expect(defaults.streaming).toBe(true);
      expect(defaults.tools).toBe(true);
    });
  });

  describe("fetchProviderModels", () => {
    it("falls back to manual models (labeled) when the endpoint fails", async () => {
      const result = await fetchProviderModels(openAiConfig(), {
        manualModels: ["my-custom-model"],
      });

      expect(result.source).toBe("manual");
      expect(result.error).toBeTruthy();
      expect(result.models.map((model) => model.id)).toEqual([
        "my-custom-model",
      ]);
      expect(result.models[0]!.source).toBe("manual");
    });

    it("parses OpenAI-shaped model lists and normalizes trailing slashes", async () => {
      const fetchSpy = vi
        .spyOn(globalThis, "fetch")
        .mockResolvedValue(
          new Response(JSON.stringify({ data: [{ id: "m-1" }, { id: "m-2" }] }), {
            status: 200,
          }),
        );

      try {
        const result = await fetchProviderModels(openAiConfig());

        expect(result.error).toBeNull();
        expect(result.source).toBe("live");
        expect(result.models.map((model) => model.id)).toEqual(["m-1", "m-2"]);
        expect(result.models.every((model) => model.source === "live")).toBe(
          true,
        );
        expect(fetchSpy.mock.calls[0]![0]).toBe(
          "https://provider.example/v1/models",
        );
      } finally {
        fetchSpy.mockRestore();
      }
    });

    it("reports auth failures clearly for 401 responses", async () => {
      vi.spyOn(globalThis, "fetch").mockResolvedValue(
        new Response("unauthorized", { status: 401 }),
      );

      try {
        const result = await fetchProviderModels(openAiConfig());

        expect(result.error).toMatch(/credentials/i);
      } finally {
        vi.mocked(globalThis.fetch).mockRestore();
      }
    });

    it("caches the live list and serves it after a later failure", async () => {
      const fetchSpy = vi
        .spyOn(globalThis, "fetch")
        .mockResolvedValueOnce(
          new Response(JSON.stringify({ data: [{ id: "m-live" }] }), {
            status: 200,
          }),
        )
        .mockRejectedValueOnce(new Error("network down"));

      try {
        const first = await fetchProviderModels(openAiConfig());
        expect(first.source).toBe("live");

        const second = await fetchProviderModels(openAiConfig(), {
          forceRefresh: true,
          manualModels: ["manual-only"],
        });
        expect(second.source).toBe("cache");
        expect(second.error).toMatch(/cached/i);
        expect(second.models.map((model) => model.id)).toEqual([
          "m-live",
          "manual-only",
        ]);
      } finally {
        fetchSpy.mockRestore();
      }
    });
  });

  describe("testProviderConnection", () => {
    it("rejects malformed base URLs before any network call", async () => {
      const result = await testProviderConnection({
        baseUrl: "not-a-url",
        family: "openai-compatible",
        id: "t",
      });

      expect(result.ok).toBe(false);
      expect(result.message).toMatch(/http/i);
    });

    it("reports success on 200 and clear auth rejection on 401", async () => {
      const okSpy = vi
        .spyOn(globalThis, "fetch")
        .mockResolvedValue(new Response("[]", { status: 200 }));
      const ok = await testProviderConnection(openAiConfig());
      expect(ok.ok).toBe(true);
      okSpy.mockRestore();

      const badSpy = vi
        .spyOn(globalThis, "fetch")
        .mockResolvedValue(new Response("no", { status: 403 }));
      const rejected = await testProviderConnection(openAiConfig());
      expect(rejected.ok).toBe(false);
      expect(rejected.message).toMatch(/key was rejected/i);
      badSpy.mockRestore();
    });
  });
});

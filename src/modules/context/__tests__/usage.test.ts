import { describe, expect, it } from "vitest";

import {
  cacheHitRatio,
  computeContextLoad,
  contextRingTone,
  estimateStreamingTokens,
  formatTokenNumber,
  sumNullable,
} from "../usage";

describe("contextRingTone", () => {
  it("returns unknown for missing or non-finite input", () => {
    expect(contextRingTone(null)).toBe("unknown");
    expect(contextRingTone(Number.NaN)).toBe("unknown");
  });

  it("is green below 60%", () => {
    expect(contextRingTone(0)).toBe("ok");
    expect(contextRingTone(59.9)).toBe("ok");
  });

  it("is amber from 60% up to (not including) 85%", () => {
    expect(contextRingTone(60)).toBe("warn");
    expect(contextRingTone(84.9)).toBe("warn");
  });

  it("is red at 85% and above", () => {
    expect(contextRingTone(85)).toBe("critical");
    expect(contextRingTone(100)).toBe("critical");
    expect(contextRingTone(250)).toBe("critical");
  });
});

describe("computeContextLoad", () => {
  it("returns null when the window or usage is unknown", () => {
    expect(computeContextLoad({ contextWindow: null, usedTokens: 100 })).toBeNull();
    expect(computeContextLoad({ contextWindow: 200_000, usedTokens: null })).toBeNull();
  });

  it("returns null for invalid windows", () => {
    expect(computeContextLoad({ contextWindow: 0, usedTokens: 100 })).toBeNull();
    expect(computeContextLoad({ contextWindow: -5, usedTokens: 100 })).toBeNull();
  });

  it("computes a one-decimal percentage", () => {
    expect(computeContextLoad({ contextWindow: 200_000, usedTokens: 123_456 })).toBe(61.7);
    expect(computeContextLoad({ contextWindow: 200_000, usedTokens: 0 })).toBe(0);
  });

  it("clamps estimates that exceed the window instead of showing over 100%", () => {
    expect(computeContextLoad({ contextWindow: 1000, usedTokens: 5000 })).toBe(100);
  });
});

describe("estimateStreamingTokens", () => {
  it("estimates ~4 chars per token", () => {
    expect(estimateStreamingTokens(400)).toBe(100);
    expect(estimateStreamingTokens(401)).toBe(101);
  });

  it("returns 0 for empty or invalid input", () => {
    expect(estimateStreamingTokens(0)).toBe(0);
    expect(estimateStreamingTokens(-10)).toBe(0);
  });
});

describe("formatTokenNumber", () => {
  it("formats with thousands separators and em-dash for unknown", () => {
    expect(formatTokenNumber(123456)).toBe("123,456");
    expect(formatTokenNumber(null)).toBe("—");
  });
});

describe("cacheHitRatio", () => {
  it("computes the read share of cacheable input", () => {
    expect(cacheHitRatio({ cacheReadTokens: 9000, noCacheTokens: 1000 })).toBe(0.9);
  });

  it("returns null when data is missing or there is no input", () => {
    expect(cacheHitRatio({ cacheReadTokens: null, noCacheTokens: 100 })).toBeNull();
    expect(cacheHitRatio({ cacheReadTokens: 0, noCacheTokens: 0 })).toBeNull();
  });
});

describe("sumNullable", () => {
  it("sums present values and returns null only when all are missing", () => {
    expect(sumNullable([1, null, 2])).toBe(3);
    expect(sumNullable([null, null])).toBeNull();
    expect(sumNullable([])).toBeNull();
  });
});

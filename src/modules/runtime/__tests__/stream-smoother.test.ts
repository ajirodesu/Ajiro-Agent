import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { createStreamSmoother } from "@/modules/runtime/stream-smoother";

describe("createStreamSmoother", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("buffers pushes and flushes them together on the cadence", () => {
    const flushed: string[] = [];
    const smoother = createStreamSmoother({
      flushIntervalMs: 50,
      onFlush: (text) => flushed.push(text),
    });

    smoother.push("Hel");
    smoother.push("lo ");
    smoother.push("wor");

    expect(flushed).toEqual([]);

    vi.advanceTimersByTime(50);
    expect(flushed).toEqual(["Hello wor"]);

    smoother.dispose();
  });

  it("flushes multiple times over multiple intervals", () => {
    const flushed: string[] = [];
    const smoother = createStreamSmoother({
      flushIntervalMs: 40,
      onFlush: (text) => flushed.push(text),
    });

    smoother.push("a");
    vi.advanceTimersByTime(40);
    smoother.push("b");
    vi.advanceTimersByTime(40);
    smoother.push("c");
    smoother.push("d");
    vi.advanceTimersByTime(40);

    expect(flushed).toEqual(["a", "b", "cd"]);

    smoother.dispose();
  });

  it("end() flushes any remaining queued text exactly once", () => {
    const flushed: string[] = [];
    const smoother = createStreamSmoother({
      flushIntervalMs: 1_000,
      onFlush: (text) => flushed.push(text),
    });

    smoother.push("final bits");
    smoother.end();

    expect(flushed).toEqual(["final bits"]);
    expect(smoother).toBeDefined();
  });

  it("dispose() drops the queue without flushing", () => {
    const flushed: string[] = [];
    const smoother = createStreamSmoother({
      onFlush: (text) => flushed.push(text),
    });

    smoother.push("discarded");
    smoother.dispose();

    expect(flushed).toEqual([]);
  });

  it("flushes pushes that arrive after end() immediately", () => {
    const flushed: string[] = [];
    const smoother = createStreamSmoother({
      flushIntervalMs: 60_000,
      onFlush: (text) => flushed.push(text),
    });

    smoother.end();
    smoother.push("late arrival");

    expect(flushed).toEqual(["late arrival"]);
  });

  it("does not fire the interval when nothing is queued", () => {
    const flushed: string[] = [];
    const smoother = createStreamSmoother({
      flushIntervalMs: 10,
      onFlush: (text) => flushed.push(text),
    });

    smoother.push("");
    vi.advanceTimersByTime(100);

    expect(flushed).toEqual([]);
    smoother.dispose();
  });
});

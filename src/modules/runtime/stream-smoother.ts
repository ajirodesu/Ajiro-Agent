/**
 * StreamSmoother: decouples network I/O from UI rendering during streaming.
 *
 * Raw deltas arrive in bursts of unpredictable size and timing; pushing each
 * one straight into React state causes visible stutter as the response grows.
 * The smoother queues incoming text and flushes the queue on a fixed short
 * cadence, producing an even render cadence regardless of network timing. No
 * data is ever dropped: end() flushes whatever remains.
 *
 * Author: AjiroDesu
 */
export const DEFAULT_FLUSH_INTERVAL_MS = 48;

export type StreamSmoother = {
  push: (delta: string) => void;
  /** Flush everything still queued and stop the cadence timer. */
  end: () => void;
  /** Tear down without flushing (run aborted). */
  dispose: () => void;
};

export function createStreamSmoother(input: {
  flushIntervalMs?: number;
  onFlush: (text: string) => void;
}): StreamSmoother {
  const flushIntervalMs = input.flushIntervalMs ?? DEFAULT_FLUSH_INTERVAL_MS;
  let queue = "";
  let timer: ReturnType<typeof setInterval> | null = null;
  let ended = false;

  const flush = () => {
    if (queue.length === 0) {
      return;
    }

    const text = queue;
    queue = "";
    input.onFlush(text);
  };

  const stopTimer = () => {
    if (timer !== null) {
      clearInterval(timer);
      timer = null;
    }
  };

  return {
    push(delta) {
      if (ended) {
        // Pushes after end() are flushed immediately so late deltas are not
        // silently dropped.
        if (delta) {
          input.onFlush(delta);
        }
        return;
      }

      queue += delta;

      if (timer === null) {
        timer = setInterval(() => {
          flush();
        }, flushIntervalMs);
      }
    },
    end() {
      if (ended) {
        return;
      }

      ended = true;
      stopTimer();
      flush();
    },
    dispose() {
      ended = true;
      queue = "";
      stopTimer();
    },
  };
}

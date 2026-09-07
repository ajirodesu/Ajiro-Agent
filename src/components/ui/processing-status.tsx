/**
 * Claude-Code-style live processing status line.
 *
 * A lightweight status strip shown while an agent run is active: animated
 * glyph + rotating present-continuous verb + live metadata cluster (elapsed
 * seconds, running token estimate, interrupt hint). Updates every second while
 * active and is only rendered during streaming, so it collapses automatically
 * on completion.
 *
 * Author: AjiroDesu
 */
import { useEffect, useMemo, useRef, useState } from "react";
import { Pressable, Text, View } from "react-native";

import { Loading } from "@/components/ui/loading";
import { useTheme } from "@/hooks/use-theme";

/**
 * Varied verb pool — cycled while the same operation is still running so the
 * line does not read as frozen. Original wordlist, not copied from any tool.
 */
const STATUS_VERBS = [
  "Thinking",
  "Working",
  "Reasoning",
  "Composing",
  "Analyzing",
  "Exploring",
  "Considering",
  "Drafting",
] as const;

const VERB_ROTATE_MS = 2400;

export function estimateTokensFromChars(contentLength: number) {
  // ~4 chars per token is the usual rough ratio; good enough for a live gauge.
  return Math.ceil(contentLength / 4);
}

export function formatTokenCount(tokens: number) {
  if (tokens >= 1000) {
    return `${(tokens / 1000).toFixed(1)}k tokens`;
  }

  return `${tokens} token${tokens === 1 ? "" : "s"}`;
}

export function useElapsedSeconds(startedAt: string, active: boolean) {
  const startMs = useMemo(() => {
    const parsed = Date.parse(startedAt);

    return Number.isNaN(parsed) ? 0 : parsed;
  }, [startedAt]);
  const [elapsed, setElapsed] = useState(0);

  useEffect(() => {
    // Effects may read the clock; render must stay pure.
    const tick = () => {
      setElapsed(Math.max(0, Math.floor((Date.now() - startMs) / 1000)));
    };

    if (active) {
      tick();
      const interval = setInterval(tick, 1000);

      return () => {
        clearInterval(interval);
      };
    }
  }, [active, startMs]);

  return elapsed;
}

export function useRotatingVerb(active: boolean) {
  const [verbIndex, setVerbIndex] = useState(0);
  const activeRef = useRef(active);
  activeRef.current = active;

  useEffect(() => {
    if (!active) {
      return;
    }

    const interval = setInterval(() => {
      if (activeRef.current) {
        setVerbIndex((current) => (current + 1) % STATUS_VERBS.length);
      }
    }, VERB_ROTATE_MS);

    return () => {
      clearInterval(interval);
    };
  }, [active]);

  return STATUS_VERBS[verbIndex];
}

export type ProcessingStatusProps = {
  /** ISO timestamp the operation started (assistant message createdAt). */
  createdAt: string;
  /** Live content length used to estimate generated tokens when usage is absent. */
  contentLength: number;
  /** Authoritative token total from usage metadata, when available. */
  tokens?: number | null;
  /** Single tap interrupts the run; double tap opens the step history. */
  onInterrupt?: () => void;
  onOpenHistory?: () => void;
};

export function ProcessingStatus({
  contentLength,
  createdAt,
  onInterrupt,
  onOpenHistory,
  tokens,
}: ProcessingStatusProps) {
  const theme = useTheme();
  const elapsed = useElapsedSeconds(createdAt, true);
  const verb = useRotatingVerb(true);
  const tokenCount = tokens ?? estimateTokensFromChars(contentLength);
  const lastPressRef = useRef(0);

  const handlePress = () => {
    const now = Date.now();
    const isDoubleTap = now - lastPressRef.current < 320;
    lastPressRef.current = now;

    if (isDoubleTap) {
      // Double-tap: open the step history instead of interrupting.
      onOpenHistory?.();
      return;
    }

    onInterrupt?.();
  };

  return (
    <View
      accessibilityLiveRegion="polite"
      className="flex-row flex-wrap items-center gap-sp-2"
    >
      <Loading label="" size={14} />
      <Text className="font-mono text-sm" style={{ color: theme.textSecondary }}>
        {verb}… ({elapsed}s · {formatTokenCount(tokenCount)} ·{" "}
      </Text>
      {onInterrupt ? (
        <Pressable
          accessibilityHint="Single tap interrupts the run. Double tap opens the step history."
          accessibilityRole="button"
          hitSlop={6}
          onPress={handlePress}
          style={({ pressed }) => (pressed ? { opacity: 0.7 } : null)}
        >
          <Text
            className="font-mono text-sm underline"
            style={{ color: theme.text }}
          >
            tap to interrupt
          </Text>
        </Pressable>
      ) : (
        <Text
          className="font-mono text-sm"
          style={{ color: theme.textSecondary }}
        >
          running
        </Text>
      )}
      <Text className="font-mono text-sm" style={{ color: theme.textSecondary }}>
        )
      </Text>
    </View>
  );
}

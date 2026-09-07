/**
 * Predictive-text style suggestions for the composer.
 *
 * The OS keyboard's own suggestion strip is not exposed to React Native, so
 * this module provides deterministic, in-app predictions: prefix completion of
 * the word being typed against a curated vocabulary, then next-word predictions
 * from a small bigram map, then generic defaults. Pure functions so the
 * behavior is unit-testable.
 *
 * Author: AjiroDesu
 */

const VOCABULARY = [
  "about",
  "agent",
  "all",
  "and",
  "are",
  "bug",
  "build",
  "can",
  "check",
  "class",
  "code",
  "commit",
  "component",
  "continue",
  "create",
  "debug",
  "delete",
  "describe",
  "design",
  "document",
  "email",
  "explain",
  "feature",
  "file",
  "fix",
  "folder",
  "for",
  "from",
  "function",
  "generate",
  "git",
  "give",
  "help",
  "how",
  "improve",
  "is",
  "landing",
  "list",
  "make",
  "meeting",
  "message",
  "my",
  "new",
  "note",
  "page",
  "please",
  "project",
  "prompt",
  "refactor",
  "remove",
  "rename",
  "review",
  "run",
  "search",
  "show",
  "skill",
  "status",
  "summarize",
  "test",
  "thanks",
  "that",
  "the",
  "this",
  "to",
  "today",
  "tomorrow",
  "translate",
  "update",
  "what",
  "when",
  "where",
  "which",
  "why",
  "write",
];

const NEXT_WORDS: Record<string, string[]> = {
  create: ["a", "an", "new"],
  explain: ["how", "this", "why"],
  fix: ["this", "the", "it"],
  how: ["are", "do", "to"],
  make: ["a", "it", "this"],
  review: ["the", "this", "my"],
  run: ["the", "tests", "it"],
  show: ["me", "the", "all"],
  summarize: ["this", "the", "it"],
  test: ["this", "the", "it"],
  update: ["the", "this", "my"],
  what: ["are", "is", "about"],
  write: ["a", "me", "the"],
};

export const DEFAULT_SUGGESTIONS = ["create", "explain", "summarize"];

export const MAX_SUGGESTIONS = 3;

export function getSuggestions(text: string, cursor = text.length): string[] {
  const before = text.slice(0, cursor);
  const partialMatch = /(\S*)$/.exec(before);
  const partial = partialMatch?.[1]?.toLowerCase() ?? "";

  if (partial.length > 0) {
    const completions = VOCABULARY.filter(
      (word) => word.startsWith(partial) && word !== partial,
    ).slice(0, MAX_SUGGESTIONS);

    if (completions.length > 0) {
      return completions;
    }

    // A complete word was just typed: predict what usually comes next.
    const fromPartial = NEXT_WORDS[partial];

    if (fromPartial && fromPartial.length > 0) {
      return fromPartial.slice(0, MAX_SUGGESTIONS);
    }
  }

  const beforePartial = before.slice(0, before.length - partial.length);
  const words = beforePartial
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((word) => word.toLowerCase());
  const lastWord = words[words.length - 1] ?? "";
  const nextWords = NEXT_WORDS[lastWord];

  if (nextWords && nextWords.length > 0) {
    return nextWords.slice(0, MAX_SUGGESTIONS);
  }

  const defaults = DEFAULT_SUGGESTIONS.filter((word) => word !== partial);

  return defaults.length > 0 ? defaults : DEFAULT_SUGGESTIONS;
}

export function insertSuggestion(
  text: string,
  start: number,
  end: number,
  word: string,
): { text: string; cursor: number } {
  const safeStart = Math.max(0, Math.min(start, text.length));
  const safeEnd = Math.max(safeStart, Math.min(end, text.length));
  const before = text.slice(0, safeStart);
  const after = text.slice(safeEnd);
  const leadingSpace =
    before.length > 0 && !/\s$/.test(before) ? " " : "";
  // Trailing space at end of text or before another word; none before
  // punctuation/whitespace so we don't create ",  word" or "word , " messes.
  const trailingSpace =
    after.length === 0 || /^[A-Za-z0-9]/.test(after) ? " " : "";
  const insertion = `${leadingSpace}${word}${trailingSpace}`;

  return {
    text: `${before}${insertion}${after}`,
    cursor: (before + insertion).length,
  };
}

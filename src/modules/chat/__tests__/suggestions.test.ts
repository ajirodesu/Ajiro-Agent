import { describe, expect, it } from "vitest";

import {
  DEFAULT_SUGGESTIONS,
  getSuggestions,
  insertSuggestion,
} from "@/modules/chat/suggestions";

describe("getSuggestions", () => {
  it("returns defaults for empty input", () => {
    expect(getSuggestions("")).toEqual(DEFAULT_SUGGESTIONS);
  });

  it("completes the word being typed by prefix", () => {
    const suggestions = getSuggestions("please cre");

    expect(suggestions).toContain("create");
    expect(suggestions.length).toBeLessThanOrEqual(3);
  });

  it("is case-insensitive and excludes the exact typed word", () => {
    const suggestions = getSuggestions("Create");

    expect(suggestions).not.toContain("Create");
    expect(suggestions).not.toContain("create");
  });

  it("suggests next words after a completed word", () => {
    const suggestions = getSuggestions("how ");

    expect(suggestions).toEqual(["are", "do", "to"]);
  });

  it("suggests next words based on the word before the cursor", () => {
    const suggestions = getSuggestions("how are you", 7);

    expect(suggestions).toEqual(["are", "do", "to"]);
  });

  it("falls back to defaults when nothing matches", () => {
    const suggestions = getSuggestions("zzzzqqq");

    expect(suggestions).toEqual(DEFAULT_SUGGESTIONS);
  });
});

describe("insertSuggestion", () => {
  it("appends a word with a trailing space at the end", () => {
    const result = insertSuggestion("hello", 5, 5, "world");

    expect(result.text).toBe("hello world ");
    expect(result.cursor).toBe("hello world ".length);
  });

  it("inserts at the cursor position mid-text", () => {
    const result = insertSuggestion("hello world", 5, 5, "big");

    expect(result.text).toBe("hello big world");
    expect(result.cursor).toBe("hello big".length);
  });

  it("adds a leading space when the previous text does not end with whitespace", () => {
    const result = insertSuggestion("hello", 5, 5, "there");

    expect(result.text).toBe("hello there ");
  });

  it("does not add a trailing space when followed by punctuation", () => {
    const result = insertSuggestion("hello, world", 5, 5, "there");

    expect(result.text).toBe("hello there, world");
  });

  it("does not add a leading space when the text already ends with whitespace", () => {
    const result = insertSuggestion("hello ", 6, 6, "there");

    expect(result.text).toBe("hello there ");
  });

  it("replaces the selected range", () => {
    const result = insertSuggestion("hello old world", 6, 9, "big");

    expect(result.text).toBe("hello big world");
  });

  it("clamps out-of-range cursors safely", () => {
    const result = insertSuggestion("hi", 99, 120, "there");

    expect(result.text).toBe("hi there ");
    expect(result.cursor).toBe(result.text.length);
  });
});

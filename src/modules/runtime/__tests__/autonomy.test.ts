import { describe, expect, it } from "vitest";

import {
  buildAutonomySystemPrompt,
} from "@/modules/runtime/autonomy";

describe("autonomy policy prompt", () => {
  const prompt = buildAutonomySystemPrompt();

  it("instructs the agent to proceed on reversible, verifiable decisions", () => {
    expect(prompt).toMatch(/easily reversible/i);
    expect(prompt).toMatch(/verify.*yourself|could verify/si);
  });

  it("reserves pausing for destructive, diverging, or credential-blocked cases", () => {
    expect(prompt).toMatch(/destructive|irreversible/i);
    expect(prompt).toMatch(/credentials|permissions/i);
    expect(prompt).toMatch(/meaningfully different/i);
  });

  it("requires single-question asks with options plus freeform", () => {
    expect(prompt).toMatch(/one question at a time/i);
    expect(prompt).toMatch(/2[–-]4 concrete/i);
    expect(prompt).toMatch(/free-form/i);
  });

  it("forbids silent stops and requires stated assumptions", () => {
    expect(prompt).toMatch(/never stop mid-task silently/i);
    expect(prompt).toMatch(/state the assumption|mention it/i);
  });
});

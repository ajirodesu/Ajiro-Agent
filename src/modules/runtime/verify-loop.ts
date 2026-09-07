/**
 * Verify loop for the coding harness.
 *
 * After the agent edits files, this runs the configured allow-listed exec
 * checks and formats failures as a follow-up turn. Callers (agent-run) decide
 * how many iterations to allow and how to surface the result; this module is
 * pure orchestration over the exec surface so it stays testable.
 *
 * Author: AjiroDesu
 */
import type { ExternalFolderSession } from "@/core/types/app-state";
import type { CodingExecCommandId } from "@/core/services/coding/coding-settings";
import { runExecCommand } from "@/modules/tools/coding/exec";

export type VerifyRunResult = {
  attempt: number;
  command: CodingExecCommandId;
  output: string;
  passed: boolean;
};

export type VerifyLoopResult = {
  attempts: VerifyRunResult[];
  passed: boolean;
  /** Formatted failure report to feed back to the model, null if passed. */
  followUpPrompt: string | null;
};

function looksLikeFailure(output: string) {
  const lowered = output.toLowerCase();

  return (
    lowered.includes("syntax error") ||
    lowered.includes("parse error") ||
    /\b\d+ finding\(s\)/.test(lowered) ||
    /\b\d+ file\(s\) with syntax errors/.test(lowered) ||
    lowered.includes("failed")
  );
}

export async function runVerifyLoop(input: {
  commands: CodingExecCommandId[];
  maxRetries: number;
  maxTotalAttempts?: number;
  onAttempt?: (result: VerifyRunResult) => void;
  session: ExternalFolderSession;
}): Promise<VerifyLoopResult> {
  const attempts: VerifyRunResult[] = [];
  const totalCap = input.maxTotalAttempts ?? Math.max(input.maxRetries, 1);

  for (
    let attempt = 1;
    attempt <= totalCap;
    attempt += 1
  ) {
    const failures: VerifyRunResult[] = [];
    let anyPassed = false;

    for (const command of input.commands) {
      const output = await runExecCommand(input.session, { command });
      const passed = !looksLikeFailure(output.output) && !output.timedOut;
      const result: VerifyRunResult = {
        attempt,
        command,
        output: output.output,
        passed,
      };

      attempts.push(result);
      input.onAttempt?.(result);

      if (passed) {
        anyPassed = true;
      } else {
        failures.push(result);
      }
    }

    if (failures.length === 0 && anyPassed) {
      return {
        attempts,
        passed: true,
        followUpPrompt: null,
      };
    }
  }

  const lastFailed = attempts.filter((attempt) => !attempt.passed).slice(-4);
  const report = lastFailed
    .map((attempt) => `[${attempt.command}] attempt ${attempt.attempt}\n${attempt.output}`)
    .join("\n\n");

  return {
    attempts,
    passed: false,
    followUpPrompt: [
      "Your previous edits did not pass automated checks.",
      "Fix the issues below, then re-apply the changes:",
      report,
    ].join("\n\n"),
  };
}

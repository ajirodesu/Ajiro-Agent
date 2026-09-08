import type { ToolSet } from "ai";

import { createRecord, summarizeValue } from "@/modules/tools/built-in/shared";
import type {
  PendingToolApprovalRequest,
  ToolApprovalMode,
  ToolExecutionRecord,
} from "@/core/types/app-state";

/**
 * Approval decisions for a pending risky-action prompt:
 * - approve: run once
 * - approve_session: run, and stop asking for this tool for the rest of the
 *   conversation/run without re-prompting
 * - deny: skip this action and tell the model why
 * - abort: the user canceled the whole run
 */
export type ToolApprovalDecision =
  | "approve"
  | "approve_session"
  | "deny"
  | "abort";

let approvalSequence = 0;

function createApprovalId(toolName: string) {
  approvalSequence += 1;
  return `${toolName}:${Date.now()}:${approvalSequence}`;
}

export function wrapToolsWithApproval<T extends ToolSet>(
  tools: T,
  input: {
    getRequestSummary?: (toolName: string, toolInput: unknown) => string;
    mode: ToolApprovalMode;
    onRecord?: (record: ToolExecutionRecord) => void;
    shouldRequireApproval?: (toolName: string, toolInput: unknown) => boolean;
    /** Session-approved tool names: "Approve for this session" adds to this. */
    sessionApprovedTools?: Set<string>;
    requestApproval: (
      request: PendingToolApprovalRequest,
    ) => Promise<ToolApprovalDecision>;
  },
) {
  const sessionApproved = input.sessionApprovedTools ?? new Set<string>();

  return Object.fromEntries(
    Object.entries(tools).map(([toolName, toolDefinition]) => {
      if (!toolDefinition || typeof toolDefinition.execute !== "function") {
        return [toolName, toolDefinition];
      }

      const execute = toolDefinition.execute as (
        toolInput: unknown,
        options?: unknown,
      ) => Promise<unknown>;

      return [
        toolName,
        {
          ...toolDefinition,
          execute: async (toolInput: unknown, options?: unknown) => {
            const inputSummary =
              input.getRequestSummary?.(toolName, toolInput) ??
              summarizeValue(toolInput);
            const needsApproval =
              input.shouldRequireApproval?.(toolName, toolInput) ?? true;
            const requiresPrompt =
              input.mode === "ask" &&
              needsApproval &&
              !sessionApproved.has(toolName);

            if (requiresPrompt) {
              const decision = await input.requestApproval({
                id: createApprovalId(toolName),
                inputSummary,
                toolName,
              });

              if (decision === "abort") {
                throw new Error("Request aborted.");
              }

              if (decision === "approve_session") {
                sessionApproved.add(toolName);
              } else if (decision === "deny") {
                input.onRecord?.(
                  createRecord({
                    toolName,
                    status: "failed",
                    inputSummary,
                    error: "User denied this tool call.",
                  }),
                );

                // The denied action must not be retried; the model gets an
                // explicit path forward instead of the same prompt again.
                return {
                  denied: true,
                  message:
                    "The user denied this action. Do not attempt it again in this conversation. Explain briefly why you wanted it, then continue with the best alternative that does not require this action.",
                };
              }
            }

            return execute(toolInput, options);
          },
        },
      ];
    }),
  ) as T;
}

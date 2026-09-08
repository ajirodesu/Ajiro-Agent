import { tool } from "ai";
import { z } from "zod";

import { createRecord, summarizeValue } from "@/modules/tools/built-in/shared";
import type {
  PendingQuestionnaireAnswer,
  PendingQuestionnaireRequest,
  ToolExecutionRecord,
} from "@/core/types/app-state";

const MAX_QUESTIONS = 3;
const MAX_CHOICES = 4;
const MAX_FREEFORM_LENGTH = 1000;

let questionnaireSequence = 0;

const questionnaireItemSchema = z.object({
  allowFreeform: z.boolean().optional(),
  choices: z
    .array(z.string().trim().min(1).max(200))
    .min(1)
    .max(MAX_CHOICES)
    .optional(),
  description: z.string().trim().max(500).optional(),
  freeformPlaceholder: z.string().trim().max(200).optional(),
  id: z.string().trim().min(1).max(64),
  multiple: z.boolean().optional(),
  prompt: z.string().trim().min(1).max(500),
  required: z.boolean().optional(),
});

export function createQuestionTool(input: {
  onRecord?: (record: ToolExecutionRecord) => void;
  requestQuestionnaire: (
    request: PendingQuestionnaireRequest,
  ) => Promise<PendingQuestionnaireAnswer[] | null>;
}) {
  return {
    tools: {
      question: tool({
        description:
          "Ask the user for a decision you genuinely cannot make or verify yourself (destructive/irreversible confirmations, missing credentials, or truly diverging paths). Prefer proceeding with a stated assumption when the answer is discoverable or easily reversible — do not use this tool for things a read can answer. Ask the single most blocking question in one call; never stack multiple unrelated questions. Provide 2-4 concrete, mutually exclusive choices when the answer space is small; the user can always type their own answer instead, and the run resumes immediately with their decision applied.",
        inputSchema: z.object({
          questions: z
            .array(questionnaireItemSchema)
            .min(1)
            .max(MAX_QUESTIONS),
        }),
        execute: async ({ questions }) => {
          questionnaireSequence += 1;
          const request: PendingQuestionnaireRequest = {
            id: `questionnaire:${Date.now()}:${questionnaireSequence}`,
            items: questions.map((item) => ({
              allowFreeform: true,
              choices: item.choices,
              description: item.description ?? null,
              freeformPlaceholder: item.freeformPlaceholder ?? "Type your own answer",
              id: item.id,
              multiple: item.multiple ?? false,
              prompt: item.prompt,
              required: item.required ?? true,
            })),
          };
          const inputSummary = summarizeValue({ count: questions.length });

          try {
            const answers = await input.requestQuestionnaire(request);

            if (!answers) {
              throw new Error(
                "The user dismissed the questions. Ask for the missing information in your reply instead.",
              );
            }

            const output = { answers };

            input.onRecord?.(
              createRecord({
                toolName: "question",
                status: "completed",
                inputSummary,
                outputSummary: summarizeValue(output),
              }),
            );

            return output;
          } catch (error) {
            input.onRecord?.(
              createRecord({
                toolName: "question",
                status: "failed",
                inputSummary,
                error: error instanceof Error ? error.message : String(error),
              }),
            );
            throw error;
          }
        },
      }),
    },
  };
}

export { MAX_FREEFORM_LENGTH };

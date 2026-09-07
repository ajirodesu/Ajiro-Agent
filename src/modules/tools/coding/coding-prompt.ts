/**
 * System prompt additions for the on-device coding harness.
 *
 * Author: AjiroDesu
 */
import type { ExternalFolderSession } from "@/core/types/app-state";

export function buildCodingSystemPrompt(input: {
  execEnabled: boolean;
  gitEnabled: boolean;
  verifyEnabled: boolean;
  session: ExternalFolderSession;
}) {
  const lines = [
    "Coding session active.",
    `Project root: "${input.session.displayName}" (a user-granted folder; all paths are relative to it).`,
    "You are operating as a coding agent inside this project. Read before editing. Make focused, minimal changes.",
    "When you create or modify files, mention exactly which paths you changed and why.",
  ];

  if (input.execEnabled) {
    lines.push(
      "The exec tool runs fixed, allow-listed in-process checks (no shell exists on Android). Use it to verify assumptions before claiming a change works.",
    );
  }

  if (input.gitEnabled) {
    lines.push(
      "Local git tools (git-status, git-diff, git-add, git-commit, git-branch, git-log) operate on the local working copy. Remote GitHub operations (PRs, issues) go through the GitHub MCP server instead, if connected.",
    );
  }

  if (input.verifyEnabled) {
    lines.push(
      "After edits, automated verification may run. If you receive a verification failure report, fix the reported issues and re-apply.",
    );
  }

  return lines.join("\n");
}

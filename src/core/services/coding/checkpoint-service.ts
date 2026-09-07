/**
 * Checkpoint service: snapshots file contents before agent edits so a change
 * set can be reverted ("undo"). Snapshots are stored per conversation + project
 * via the CheckpointRepository; the undo tool restores the latest one.
 *
 * Author: AjiroDesu
 */
import type { CheckpointRepository } from "@/core/db/repositories/checkpoint-repository";
import type { ExternalFolderSession } from "@/core/types/app-state";
import { createExternalFolderService } from "@/core/services/external-folder/external-folder-service";

const MAX_SNAPSHOT_CHARS = 400_000;

export type RecordedChange = {
  path: string;
  previousContent: string | null;
};

export function createCheckpointService(input: {
  conversationId: string;
  repository: CheckpointRepository;
  runId?: string | null;
  session: ExternalFolderSession;
}) {
  const service = createExternalFolderService();
  const pending = new Map<string, RecordedChange>();

  async function snapshotBeforeWrite(path: string) {
    if (pending.has(path)) {
      // First snapshot of this file for this change set wins.
      return;
    }

    try {
      const content = await service.readTextFile(
        input.session,
        path,
        MAX_SNAPSHOT_CHARS,
      );
      pending.set(path, { path, previousContent: content });
    } catch {
      // File does not exist yet — the undo for it is a delete.
      pending.set(path, { path, previousContent: null });
    }
  }

  async function commitCheckpoint(label: string) {
    if (pending.size === 0) {
      return null;
    }

    const checkpoint = await input.repository.create({
      conversationId: input.conversationId,
      label,
      projectUri: input.session.uri,
      runId: input.runId ?? null,
      snapshot: [...pending.values()],
    });
    pending.clear();

    return checkpoint;
  }

  async function undoLatestCheckpoint() {
    const checkpoint = await input.repository.getLatestForConversation(
      input.conversationId,
      input.session.uri,
    );

    if (!checkpoint) {
      return "No checkpoint found to undo for this project.";
    }

    const restored: string[] = [];

    for (const entry of checkpoint.snapshot) {
      try {
        if (entry.previousContent === null) {
          try {
            await service.deleteEntry(input.session, entry.path, false);
            restored.push(`${entry.path}: deleted (was created by the agent)`);
          } catch {
            restored.push(`${entry.path}: already absent`);
          }
          continue;
        }

        const parent = entry.path.split("/").slice(0, -1).join("/");
        const parentExists =
          !parent ||
          service
            .listEntries(input.session, parent)
            .some((candidate) => candidate.kind === "directory");

        if (!parentExists && parent) {
          await service.createDirectory(input.session, parent);
        }

        await service.writeTextFile(
          input.session,
          entry.path,
          entry.previousContent,
          "overwrite",
        );
        restored.push(`${entry.path}: restored previous content`);
      } catch (error) {
        restored.push(
          `${entry.path}: FAILED — ${error instanceof Error ? error.message : String(error)}`,
        );
      }
    }

    return [
      `Undo applied for checkpoint "${checkpoint.label}" (${checkpoint.createdAt}):`,
      ...restored,
    ].join("\n");
  }

  return {
    commitCheckpoint,
    getPendingCount: () => pending.size,
    snapshotBeforeWrite,
    undoLatestCheckpoint,
  };
}

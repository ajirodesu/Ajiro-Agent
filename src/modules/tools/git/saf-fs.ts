/**
 * A minimal fs adapter that backs isomorphic-git with the app's virtual
 * filesystem. isomorphic-git expects Node-style fs paths; on Android the
 * project lives behind SAF content:// URIs, so git operations run against a
 * persistent local mirror of the project (see project-mirror.ts) instead of
 * trying to teach git about SAF.
 *
 * Author: AjiroDesu
 */
import { Directory, File, Paths } from "expo-file-system";

import type { ExternalFolderSession } from "@/core/types/app-state";
import { createExternalFolderService } from "@/core/services/external-folder/external-folder-service";

const MIRROR_ROOT = new Directory(Paths.document, "ajiro-projects");

export function getMirrorRoot(session: ExternalFolderSession) {
  const safeName =
    session.displayName
      .replace(/[^a-zA-Z0-9-_ ]/g, "")
      .trim()
      .replace(/\s+/g, "-")
      .slice(0, 40) || "project";
  const hash = Math.abs(
    [...session.uri].reduce((acc, char) => (acc * 31 + char.charCodeAt(0)) | 0, 7),
  ).toString(36);

  return new Directory(MIRROR_ROOT, `${safeName}-${hash}`);
}

function resolveEntry(root: Directory, relativePath: string) {
  const parts = relativePath
    .replace(/\\/g, "/")
    .split("/")
    .filter((part) => part && part !== ".");

  let current: Directory = root;

  for (let index = 0; index < parts.length; index += 1) {
    const part = parts[index];
    const isLast = index === parts.length - 1;
    const childFile = new File(current.uri, part);
    const childDir = new Directory(current.uri, part);

    if (isLast) {
      if (childFile.exists) {
        return childFile;
      }

      if (childDir.exists) {
        return childDir;
      }

      return null;
    }

    if (childDir.exists) {
      current = childDir;
      continue;
    }

    if (childFile.exists) {
      // A file where a directory is expected in the path.
      return null;
    }

    return null;
  }

  return current;
}

function statLike(target: Directory | File) {
  const isDirectory = target instanceof Directory;

  return {
    isFile: () => !isDirectory,
    isDirectory: () => isDirectory,
    isSymbolicLink: () => false,
    size: target instanceof File ? target.size : 0,
    mtimeMs: 0,
  };
}

export type MirrorFs = {
  root: Directory;
  readFile: (
    path: string,
    options?: { encoding?: string | null } | string,
  ) => Promise<Uint8Array | string>;
  writeFile: (
    path: string,
    data: string | Uint8Array,
    options?: { encoding?: string | null } | string,
  ) => Promise<void>;
  unlink: (path: string) => Promise<void>;
  readdir: (path: string) => Promise<string[]>;
  mkdir: (path: string) => Promise<void>;
  rmdir: (path: string, options?: { recursive?: boolean }) => Promise<void>;
  stat: (path: string) => Promise<ReturnType<typeof statLike>>;
  lstat: (path: string) => Promise<ReturnType<typeof statLike>>;
  rename: (oldPath: string, newPath: string) => Promise<void>;
};

export function createMirrorFs(root: Directory): MirrorFs {
  return {
    root,
    async readFile(path, options) {
      const entry = resolveEntry(root, path);

      if (!entry || entry instanceof Directory) {
        throw new Error(`ENOENT: ${path}`);
      }

      const encoding =
        typeof options === "string" ? options : options?.encoding ?? null;

      if (encoding === null || encoding === undefined) {
        return entry.bytes();
      }

      return entry.text();
    },
    async writeFile(path, data) {
      const parts = path.replace(/\\/g, "/").split("/").filter(Boolean);
      const name = parts[parts.length - 1] ?? path;
      const parentPath = parts.slice(0, -1).join("/");
      const parent = parentPath ? resolveEntry(root, parentPath) : root;

      if (!parent || !(parent instanceof Directory)) {
        throw new Error(`ENOTDIR: ${parentPath}`);
      }

      const file = new File(parent, name);

      if (data instanceof Uint8Array) {
        file.write(data);
      } else if (typeof data === "string") {
        file.write(data);
      } else {
        file.write(String(data));
      }
    },
    async unlink(path) {
      const entry = resolveEntry(root, path);

      if (entry && entry instanceof File) {
        entry.delete();
      }
    },
    async readdir(path) {
      const entry = path ? resolveEntry(root, path) : root;

      if (!entry || !(entry instanceof Directory)) {
        throw new Error(`ENOTDIR: ${path}`);
      }

      return entry.list().map((child) => child.name);
    },
    async mkdir(path) {
      const parts = path.replace(/\\/g, "/").split("/").filter(Boolean);
      let current = root;

      for (const part of parts) {
        const next = new Directory(current, part);

        if (!next.exists) {
          next.create();
        }

        current = next;
      }
    },
    async rmdir(path, options) {
      const entry = resolveEntry(root, path);

      if (entry && entry instanceof Directory) {
        if (options?.recursive) {
          entry.delete();
        } else if (entry.list().length === 0) {
          entry.delete();
        } else {
          throw new Error(`ENOTEMPTY: ${path}`);
        }
      }
    },
    async stat(path) {
      const entry = path ? resolveEntry(root, path) : root;

      if (!entry) {
        throw new Error(`ENOENT: ${path}`);
      }

      return statLike(entry);
    },
    async lstat(path) {
      return this.stat(path);
    },
    async rename(oldPath, newPath) {
      const entry = resolveEntry(root, oldPath);

      if (!entry) {
        throw new Error(`ENOENT: ${oldPath}`);
      }

      const parts = newPath.replace(/\\/g, "/").split("/").filter(Boolean);
      const name = parts[parts.length - 1] ?? newPath;
      const parentPath = parts.slice(0, -1).join("/");
      const parent = parentPath ? resolveEntry(root, parentPath) : root;

      if (!parent || !(parent instanceof Directory)) {
        throw new Error(`ENOTDIR: ${parentPath}`);
      }

      if (entry instanceof File) {
        entry.move(new File(parent, name), { overwrite: true });
      } else {
        entry.move(new Directory(parent, name), { overwrite: true });
      }
    },
  };
}

/**
 * Copies the SAF project into the persistent local mirror. `.git` lives in the
 * mirror across sessions; the SAF tree is treated as the source of truth for
 * working files.
 */
export async function syncProjectToMirror(
  session: ExternalFolderSession,
  onProgress?: (synced: number) => void,
) {
  const service = createExternalFolderService();
  const root = getMirrorRoot(session);

  if (!root.exists) {
    root.create();
  }

  const skip = new Set(["node_modules", ".gradle", "build", "dist", ".expo"]);
  let synced = 0;

  const walk = async (safPath: string, mirrorRelativePath: string) => {
    const entries = service.listEntries(session, safPath);

    for (const entry of entries) {
      if (skip.has(entry.name)) {
        continue;
      }

      const childRelative = mirrorRelativePath
        ? `${mirrorRelativePath}/${entry.name}`
        : entry.name;

      if (entry.kind === "directory") {
        const dir = resolveEntry(root, childRelative);

        if (!dir || !(dir instanceof Directory)) {
          await createMirrorFs(root).mkdir(childRelative);
        }

        await walk(entry.path, childRelative);
      } else {
        const target = new File(root, childRelative);

        if (target.exists && target.size === entry.size) {
          synced += 1;
          continue;
        }

        const { bytes } = await service.readBytesFile(session, entry.path);
        target.write(bytes);
        synced += 1;

        if (onProgress && synced % 25 === 0) {
          onProgress(synced);
        }
      }
    }
  };

  await walk("", "");

  return { root, synced };
}

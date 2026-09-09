import { readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { getTableConfig } from "drizzle-orm/sqlite-core";
import { describe, expect, it } from "vitest";

import { schema } from "@/core/db/schema";

/**
 * Regression guard for the "no such table / no such column" outage family
 * (e.g. skill_files): every table and column the drizzle schema can query
 * must be created somewhere in migrations.ts — the v0 fresh-install block,
 * the repair SQL, or a versioned upgrade step (CREATE TABLE / ALTER TABLE).
 * Runs in plain node, no SQLite needed.
 */
const migrationsSql = readFileSync(
  join(dirname(fileURLToPath(import.meta.url)), "..", "migrations.ts"),
  "utf8",
);

function statementsFor(tableName: string): string[] {
  const found: string[] = [];
  const createPattern = new RegExp(
    `CREATE TABLE IF NOT EXISTS ${tableName}\\b`,
    "g",
  );
  const alterPattern = new RegExp(`ALTER TABLE ${tableName}\\b[\\s\\S]*?;`, "g");

  for (const match of migrationsSql.matchAll(createPattern)) {
    const start = match.index ?? 0;
    const end = migrationsSql.indexOf(");", start);
    found.push(migrationsSql.slice(start, end < 0 ? start + 4000 : end + 2));
  }

  for (const match of migrationsSql.matchAll(alterPattern)) {
    found.push(match[0]);
  }

  return found;
}

describe("schema/migration consistency", () => {
  for (const [key, table] of Object.entries(schema)) {
    const config = getTableConfig(table);
    const tableName = config.name;

    it(`creates table ${tableName} (schema key ${key})`, () => {
      expect(
        statementsFor(tableName).length,
        `migrations.ts never creates table "${tableName}" — drizzle queries against it will throw "no such table"`,
      ).toBeGreaterThan(0);
    });

    for (const column of config.columns) {
      it(`provides column ${tableName}.${column.name}`, () => {
        const statements = statementsFor(tableName);
        const pattern = new RegExp(`\\b${column.name}\\b`);
        expect(
          statements.some((statement) => pattern.test(statement)),
          `migrations.ts never provides column "${tableName}.${column.name}" — drizzle queries selecting it will throw "no such column"`,
        ).toBe(true);
      });
    }
  }
});

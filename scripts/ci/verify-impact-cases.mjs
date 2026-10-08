#!/usr/bin/env node
// Verifies the CI routing policy (.github/appfactory-impact.json) against the
// expected routing cases, using the same AppFactory engine as the CI dispatcher.
//
// Usage: node scripts/ci/verify-impact-cases.mjs --engine <path/to/src/impact/engine.mjs>

import { readFile } from "node:fs/promises";
import { resolve } from "node:path";
import { pathToFileURL } from "node:url";

const args = Object.fromEntries(
  process.argv.slice(2).reduce((pairs, token, index, all) => {
    if (token.startsWith("--")) pairs.push([token.slice(2), all[index + 1]]);
    return pairs;
  }, [])
);

if (!args.engine) {
  console.error("Missing --engine <path to AppFactory src/impact/engine.mjs>.");
  process.exit(2);
}

const root = process.cwd();
const configPath = resolve(root, args.config ?? ".github/appfactory-impact.json");
const casesPath = resolve(root, args.cases ?? ".github/appfactory-impact.cases.json");
const workflowPath = resolve(root, args.workflow ?? ".github/workflows/ci.yml");

const { analyzeImpact } = await import(pathToFileURL(resolve(root, args.engine)).href);
const config = JSON.parse(await readFile(configPath, "utf8"));
const { cases } = JSON.parse(await readFile(casesPath, "utf8"));

const failures = [];

for (const testCase of cases) {
  const analysis = analyzeImpact(structuredClone(config), testCase.files);
  const actual = [...analysis.gates].sort();
  const expected = [...testCase.gates].sort();

  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    failures.push(
      `${testCase.name}: expected gates ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`
    );
  }

  const expectedFallback = testCase.fallbackApplied ?? false;
  if (analysis.fallbackApplied !== expectedFallback) {
    failures.push(
      `${testCase.name}: expected fallbackApplied=${expectedFallback}, got ${analysis.fallbackApplied} ` +
        `(unmatched: ${JSON.stringify(analysis.unmatchedFiles)})`
    );
  }
}

// Every AppFactory reference in the dispatcher must point at the same reviewed runtime,
// otherwise the impact job and the policy check could run different engines.
const workflow = await readFile(workflowPath, "utf8");
const pinnedRefs = new Set(
  [...workflow.matchAll(/appfactory-project-automation\/[^@\s]+@([0-9a-f]{40})|appfactory_ref:\s*([0-9a-f]{40})|APPFACTORY_REF:\s*"?([0-9a-f]{40})/g)]
    .map((match) => match[1] ?? match[2] ?? match[3])
);

if (pinnedRefs.size !== 1) {
  failures.push(
    `ci.yml must pin exactly one AppFactory SHA across uses/appfactory_ref/APPFACTORY_REF, found ${JSON.stringify([...pinnedRefs])}`
  );
}

if (failures.length > 0) {
  console.error(`Impact policy verification failed (${failures.length}):`);
  failures.forEach((failure) => console.error(`  - ${failure}`));
  process.exit(1);
}

console.log(`Impact policy verified: ${cases.length} routing cases, AppFactory runtime ${[...pinnedRefs][0]}.`);

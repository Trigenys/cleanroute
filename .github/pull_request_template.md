## Summary

Describe the change and why it is needed.

## Validation

- [ ] relevant tests/checks pass
- [ ] documentation/config examples are updated when public behavior changes

## RAIDER review

- [ ] **Reusable** — no unnecessary consumer-specific fork or copy/paste
- [ ] **Agnostic** — variable repository/branch/stack/workflow assumptions are discovered or configured
- [ ] **Idempotent** — retries/repeated runs converge safely where state is mutated
- [ ] **Durable / Non-regressive** — established supported behavior remains covered and green
- [ ] **Failure memory** — meaningful failures/near misses discovered during this work were captured with root cause, resolution and prevention when applicable
- [ ] **Engineering-grade** — responsibilities, permissions, contracts, tests and errors follow appropriate professional patterns
- [ ] **Reuse-first** — for non-trivial work, relevant repositories/libraries/Actions/standards were evaluated and the Adopt / Adapt / Learn / Build decision is justified
- [ ] **Retroactive** — existing projects/data can adopt the change safely when applicable

If a RAIDER item does not apply or cannot be satisfied, explain why in the PR description.

# Milestones

## M1: Design Sign-Off

- Architecture, contracts, recovery policy, and tests documented.
- Stakeholders align on constraints and non-goals.

## M2: Skeleton + Contracts

- Java multi-module project compiles.
- Canonical contracts and contract tests exist.

## M3: Snapshot + Ordered Apply

- Snapshot load into off-heap store implemented.
- Deterministic replay tests prove version gate behavior.

## M4: Dual Publish

- State-change notifications published to Aeron and Kafka.
- Backpressure and retry handling validated.

## M5: Hardening

- Fault injection and integration suites pass.
- Metrics, health checks, and performance baselines finalized.

## First Two-Week Plan

- Week 1:
  - Finalize all docs.
  - Define canonical event fixtures and ordering matrix.
- Week 2:
  - Bootstrap modules.
  - Implement snapshot loader stub and apply-engine prototype.
  - Add first deterministic integration test.

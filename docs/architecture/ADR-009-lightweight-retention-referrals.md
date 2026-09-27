# ADR-009 — Lightweight retention and referral workflow

Status: Accepted  
Date: 2026-09-27  
Issue: #11

## Context

CleanRoute should help the operator retain good customers and attribute neighborhood referrals without becoming a loyalty platform, social network or engagement system.

The workflow must work offline, be understandable from the customer profile and remain safe under retries or double taps.

## Decision

### Feature flag at composition

Retention is controlled by RetentionProgramConfig.enabled.

When disabled:
- RoomRetentionRepository is not constructed;
- no retention repository is passed to Compose;
- the customer profile renders no retention/referral UI.

Existing referral data remains in Room and is not destructively removed.

### Configuration-driven policy

The program config owns:
- referral-code prefix;
- optional referral base URL;
- qualification rule;
- reward label.

The pilot composition uses:
- code prefix CR;
- no public referral URL until a real onboarding destination exists;
- eligibility after one successful collection by the referred customer;
- reward label Avantage parrainage.

No cash amount or discount is invented in code. A future business policy can change the reward label and qualification threshold without changing Compose screens.

### Stable referral codes

Each customer receives a persisted referral profile with a deterministic code generated from the configured prefix and a SHA-256 digest of the customer ID.

The code is persisted once created. If configuration changes later, already-issued codes remain valid.

If a base URL is configured, the same code is exposed as ?ref=<code>.

### Attribution

A referred customer can have at most one referral attribution.

Room enforces this with a unique index on referredCustomerId.

Replaying the same referrer/code against the same referred customer returns the existing referral. Attempting to replace the referrer fails explicitly.

Self-referral is rejected.

### Reward state

Referral reward state is:

PENDING -> ELIGIBLE -> AWARDED

Qualification is evaluated by the pure ReferralRewardPolicy, outside UI and messaging.

The repository counts successful COLLECTED visits for the referred customer. When the configured threshold is met, the referral becomes ELIGIBLE.

Awarding uses a status-constrained SQL update with rewardStatus = ELIGIBLE.

This makes concurrent/repeated award attempts idempotent. Once AWARDED, a later attempt returns the stored referral without granting again.

Each real referral state transition is queued in the existing outbox as UPSERT_REFERRAL.

### Retention indicators

The customer profile shows factual indicators only:
- customer tenure in days;
- completed collections during the last 90 days;
- all recorded visits during the last 90 days.

There is no score, tier, streak, public leaderboard or engagement ranking.

## Schema

Database version 3 adds:
- referral_profiles;
- referrals;
- unique indexes for referral code and referred customer;
- indexes for referrer and reward status.

Migration 2 -> 3 is explicit and destructive migration remains disabled.

## Consequences

- referral attribution remains offline-first;
- reward logic is configuration-driven and testable without Compose;
- the same referral cannot be rewarded twice;
- customer profiles expose the complete operational state;
- the feature can be removed from the running UI by configuration without deleting data.
# ADR-008 — Explicit customer contact actions and configurable message templates

Status: Accepted  
Date: 2026-09-27  
Issue: #10

## Context

The operator and customers already use phone calls and WhatsApp. The MVP should use those familiar tools rather than require a customer app or introduce automated outbound messaging.

Phone numbers arrive from manually entered and imported customer data, so formatting is not guaranteed to be consistent.

## Decision

### Explicit user action only

CleanRoute never sends a message automatically in the MVP.

A call or WhatsApp conversation is opened only after the operator taps the corresponding action. WhatsApp opens with a prefilled message when enough known data exists; the operator still reviews and sends it inside WhatsApp.

### Shared phone normalization

A single PhoneNumberNormalizer converts stored values to an international representation.

The pilot injects country calling code 237 at the composition root. The normalizer also preserves numbers already written with + or 00 international prefixes.

Both dial and WhatsApp intents use the same normalized result.

### Message templates are outside domain entities

Customer/domain entities contain no message text, URI construction or Android intent behavior.

A CustomerMessageTemplateCatalog renders reusable French templates for:
- upcoming collection;
- completed collection;
- payment reminder.

Screen logic chooses the business event. The template catalog owns the wording and can later be replaced by tenant configuration without rewriting screens.

Templates return no message if required known data is absent rather than inventing a date, amount or service period.

### Android capability handling

Dial intents use ACTION_DIAL and require no call permission.

WhatsApp deep links target either:
- com.whatsapp;
- com.whatsapp.w4b.

Android package queries are declared for those packages. If neither application is available, or if the phone number is invalid, the UI receives a human-readable failure instead of crashing.

### Contact audit

After Android successfully opens the contact intent, CleanRoute records a contact action in the existing contact_actions table and queues the existing RECORD_CONTACT_ACTION outbox kind.

The audit is best-effort: a local audit write failure must not retroactively make a successfully opened phone/WhatsApp action look failed to the operator.

## Consequences

- no customer application is required;
- no message can be sent silently by CleanRoute;
- phone formatting rules are not duplicated across screens;
- WhatsApp-specific URI/package logic stays in the communication/platform layer;
- message wording can be replaced later without changing customer, collection or payment screens.

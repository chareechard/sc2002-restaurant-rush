# Stage 1 deliverables

This document describes files present in this repository. The filename is retained for existing links;
it does not certify an individual member's authorship or record reviews from another repository.

The Stage 1 source baseline is `02f5c5a46320fc3dc5bb4b5507b1dde5c2437bac` on the history of `work/stage-1`.
Its production source and tests match the current Stage 1 snapshot.

## Implementation

The source under `src/main/java/rush/` implements Regular customers, menu prices in cents,
tables, orders, Waiter/Chef task limits, automatic Cashier collection and an 18-turn game.
The engine preserves later-turn service, eating and payment guards and retries invalid actions
without spending the staff task. The [code guide](../stage-1-code-guide.md) explains these boundaries.

## Tests and replay evidence

The Stage 1 suite contains 106 checks. Run `./build.ps1 test` on Windows or `sh build.sh test`
on Linux/macOS. Feed the files in `replays/` through standard input to play saved decisions.
The victory replay ends with 5 paid customers, $75.00 revenue and 74.40 average paid satisfaction;
the defeat replay ends with no payments and N/A average satisfaction. Both last 18 turns.
See [current test notes](../test-notes.md) and the transcripts under `docs/evidence/`.

## UML artifacts

[Stage 1 UML](../uml/stage-1/README.md) contains an editable VPP, PDF/PNG/SVG exports,
XML descriptors, layout inputs and member verifiers. The diagram selects 21 production types
and 19 relationships. Its [verification record](../uml/stage-1/verification.json) identifies
the represented source and the checks performed during the current cleanup.

Use `work/stage-1` for Stage 1 changes and `main` for final integration. See the
[repository workflow](../course-cleanup.md). Confirm human contribution and review details
with the team before adding them to submission material.

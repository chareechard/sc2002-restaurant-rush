# Stage diagram verification

The three runnable stage snapshots are tagged `stage-1`, `stage-2` and `stage-3`. The linked diagrams are design evidence and should be reviewed by the team against those versions before submission.

| Stage | Class diagram | Independent selected-member check |
| --- | --- | --- |
| 1 | [Stage 1 class diagram](stage-1/README.md) | 21 types, 50 fields, 50 selected operations, 20 enum values |
| 2 | [Stage 2 class diagram](stage-2/README.md) | 23 types, 50 fields, 58 selected methods, 20 enum values |
| 3 | [Stage 3 reviewed class diagram](stage-3/wesley-review/README.md) | 31 types, 62 fields, 76 selected operations, 21 enum values |

The Stage 3 figures above come from compiling the Java code at `stage-3` and checking `stage-3/wesley-review/source/model.xml` on 13 November 2026. The source layout validator passed for 31 classes, 28 relationships and 36 labels. Recorded artifact checksums matched. These checks do not re-open the native Visual Paradigm project, validate every relationship against code, or replace a visual review.

The [Stage 3 sequence diagram](stage-3-sequence.svg) depicts VIP arrival, ordering, preparation, the serve-or-wait choice, payment and the final result. The team must check its messages, guards and outcomes against the final code and replay evidence before submission. The original source-checkpoint fields remain in the historical verification records; independent checks do not change what those original reviews covered.

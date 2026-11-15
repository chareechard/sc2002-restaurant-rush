# Stage 2 deliverables

This document describes the Stage 2 files in this repository. Its retained filename does not
certify personal authorship or import review claims from another repository.
The source baseline is `c6e3c3f9ffe7e79e6dae1c2bb8b9436df7ec2b33`, an ancestor of `work/stage-2` with matching production source and tests.

| File under `src/main/java/rush/` | Stage 2 behaviour |
| --- | --- |
| `domain/VIPCustomer.java` | Burger preference, waiting loss of 5 and a 10% discount. |
| `domain/CriticCustomer.java` | Pasta preference and waiting loss of 12. |
| `game/Scenario.java` | Registers the deterministic mixed-customer arrivals. |
| `cli/ConsoleApp.java` | Identifies the running stage. |

Customer lifecycle, staff sequencing and payment guards remain shared. The engine calls
Customer operations through their common contract. Compare the pinned Stage 1 and Stage 2
source commits from the [verification guide](../uml/verification-guide.md) for an exact file comparison.

## Validation and artifacts

Run `./build.ps1 test` or `sh build.sh test`; the Stage 2 suite contains 115 checks.
Use standard input for the saved decisions in `replays/victory.txt` and `replays/defeat.txt`.
Victory ends at turn 18 with 4 paid customers, $67.50 revenue and 70.25 average paid satisfaction.
Defeat has no payments and N/A average satisfaction. See [test notes](../test-notes.md)
and the Stage 2 transcripts under `docs/evidence/`.

The [reviewed Stage 2 class diagram](../uml/stage-2/wesley-review/README.md) includes the editable project, PDF, PNG, SVG and verification record.

Use `work/stage-2` for Stage 2 changes and `main` for final integration. Follow the
[repository workflow](../course-cleanup.md). Human authorship and review declarations remain for the team to confirm.

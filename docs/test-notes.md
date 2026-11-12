# Stage 3 verification notes

The current Stage 3 suite contains 181 checks. The pinned source and tests at
`f41ce70e652bf7a34a5a0b740b300e1e5d4ad01b` match this stage's current tracked Java files.

Run `./build.ps1 test` on Windows or `sh build.sh test` on Linux/macOS. Both compile for Java 17
with UTF-8, all compiler warnings enabled and warnings treated as errors.
Tests cover legal lifecycle transitions, invalid-action retries, staff task limits,
service/payment timing, abandonment, price arithmetic and the stage's customer behaviours.

## Saved decisions

Build the JAR, then pipe `replays/victory.txt` or `replays/defeat.txt` through standard input.
PowerShell uses `Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar`;
POSIX shells use `java -jar build/restaurant-rush.jar < replays/victory.txt`.

| Replay | Paid | Revenue | Average paid satisfaction |
| --- | ---: | ---: | ---: |
| Victory | 5 | $86.10 | 84.00 |
| Delayed VIP | 5 | $86.10 | 83.00 |
| Defeat | 0 | $0.00 | N/A |

All these replays last 18 turns. The transcripts in `docs/evidence/` record the corresponding
stage outcomes. A transcript is supporting evidence; rerun the commands to verify the current checkout.

The [UML verification guide](uml/verification-guide.md) gives reproducible model checks against
the represented source. Those automated checks do not certify native GUI reopening or human contribution declarations.

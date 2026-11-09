# Course syntax and repository workflow

The playable Java source uses ordinary classes, explicit types, loops, traditional switches,
Scanner/PrintStream input and output, and direct exception checks. Receipt-or-null and
hasAverageSatisfaction() make absent payment and average results explicit. Replay files are
supplied through standard input; the game does not open replay files or download dependencies.

| Stage | Valid working branch | Expected suite checks |
| --- | --- | ---: |
| 1 | `work/stage-1` | 106 |
| 2 | `work/stage-2` | 115 |
| 3 | `work/stage-3` | 181 |

`main` is the final-stage integration branch. Exact source commits are listed in the
[UML verification guide](uml/verification-guide.md); source provenance is independent of movable branch names.

## Update and verify a checkout

Save local edits and check `git status`. Fetch the repository, switch to the appropriate
branch from the table, and use a fast-forward pull for that same branch. If the histories
have diverged, coordinate integration before changing either history. Preserve local work.

```powershell
git fetch origin
git switch work/stage-1
git pull --ff-only origin work/stage-1
.\build.ps1 test
Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar
Get-Content replays/defeat.txt | java -jar build/restaurant-rush.jar
```

Substitute `work/stage-2` or `work/stage-3` when working on that stage.
On Linux/macOS use `sh build.sh test` and standard-input redirection for the replays.
Later-stage additions should be reviewed as changes to their own stage; keep earlier-stage
gameplay intact. Run the [UML checks](uml/verification-guide.md) after changing model artifacts.

The [test notes](test-notes.md) describe current outcomes. Individual contribution records,
reflections and declarations require confirmation by the actual team members.

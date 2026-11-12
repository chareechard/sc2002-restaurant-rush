# UML source provenance and verification

The source pins below are commits in this repository. Their production Java files and tests
match the corresponding current stage snapshots. They precede the documentation artifacts,
which avoids a verification record referring to the commit that must contain that same record.

| Stage | Source commit | Source branch |
| --- | --- | --- |
| 1 | `02f5c5a46320fc3dc5bb4b5507b1dde5c2437bac` | `work/stage-1` |
| 2 | `c6e3c3f9ffe7e79e6dae1c2bb8b9436df7ec2b33` | `work/stage-2` |
| 3 | `f41ce70e652bf7a34a5a0b740b300e1e5d4ad01b` | `work/stage-3` / `main` |

`source_files_sha256` hashes sorted source paths, a zero byte, file content and another zero byte
for each file under `src/main/java/`. It identifies the production content independently of commit names.
The per-artifact `artifact_sha256` values identify the current diagram files.

## Reproduce the automated checks

In a Git checkout with this repository's history, install Python 3.11+ and JDK 17+ and run:

```sh
python tools/verify-uml.py
```

The checker uses only the Python standard library. It resolves each source commit, checks
ancestry and source digests, compiles the pinned production code, checks the selected descriptors
and native XML exports against compiled signatures, checks SQLite integrity for each VPP,
checks available layout geometry and verifies artifact checksums. `--json` prints the results.
An extracted ZIP alone has no Git objects; use the corresponding repository checkout for historical-stage checks.

## Native projects and exports

The provenance refresh changes source annotations in the VPP databases and XML exports.
Database compaction removes superseded annotation bytes from unused pages. Model rows,
relationship definitions, member selections and diagram geometry are preserved after normalizing
those annotation substitutions. The Stage 1/2 SVG source subtitles are updated, including labels originally stored as glyph outlines.
Their PNG and vector PDF views are regenerated from those synchronized SVGs. Other graphics retain
their existing geometry. The modified views are derived artifacts; no fresh native GUI export is claimed.

The VPP changes were made as structured SQLite field updates, not through the Visual Paradigm GUI.
Opening each modified VPP in Visual Paradigm and re-exporting its graphics remains a manual check.
The verification records mark that check as pending; automated storage/signature checks do not claim it occurred.
After native editing, export the associated XML and graphics, refresh checksums and rerun this checker.

These records verify source and artifacts. They do not certify individual contributions or signed declarations.

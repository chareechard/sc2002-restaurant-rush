# Stage 3 class diagram - aligned baseline

Open `Restaurant_Rush_Stage3_Class.vpp` and select **Stage 3 - Reviewed Class Diagram**.
Saved and reopened with Visual Paradigm Standard **18.0 build 20260521**.
The model contains 31 native class shapes and 28 relationship connectors.
Source checkpoint: `f41ce70e652bf7a34a5a0b740b300e1e5d4ad01b`. Displayed signatures were checked against
compiled Java for this stage: 62 fields, 76 operations
(including constructors), and 21 enum values.

- [PDF](Stage_3_Class_Diagram.pdf)
- [PNG](Stage_3_Class_Diagram.png)
- [SVG](Stage_3_Class_Diagram.svg)
- [Editable project](Restaurant_Rush_Stage3_Class.vpp)

## Consistency across the three stages

All shared classes use the same coordinates and box sizes. Common relationships
use the same connector routes and multiplicity-label positions. Empty spaces in
earlier stages reserve room for later additions, so the existing layout remains
stable. The selected members remain accurate to each stage's code.

Unchanged classes display the same selected members as the preceding stage.
Pre-existing helpers omitted from the baseline are also omitted from later views,
so changing the display selection does not suggest a new feature. Genuine new
members and changed signatures remain visible.

Stage 1 classes are white. Types introduced in Stage 2 remain blue in Stage 3;
Stage 3 additions are green. Orange borders identify existing types whose source
changed. Static members are underlined; abstract names and operations are italic.
The graphical relationship legend stays in fixed positions across stages.

Stage 3 now also retains the baseline RuleViolation utility and GameEngine's Order
reference. Its Receipt reference uses the same association notation as Stage 1.
The Dish/interface relationships in Stage 3 reflect actual source changes.

## Layout and verification

Multiplicity labels clear every connector. Parallel connector segments have at
least 28 px separation, and other connectors stay at least 24 px from arrowheads.
The Chef-to-Order dashed dependency passes below the four GameEngine labels
without being hidden by them. Source descriptors, native editable projects and
PDF/PNG/SVG presentations use the same reviewed geometry.

`source/model.xml` stores the selected model; `source/layout.json` stores geometry.
`source/validate_layout.py` checks the routes and labels, and
`source/render_svg.py` produces the curated presentation. The optional public API
generator is `source/ClassDiagramExportPlugin.java`; no generator is required to
open or edit the VPP. `model-xml/` is the native project export.
Routine constructors/accessors and minor method dependencies are omitted from
this overview. Parameter names are hidden for long staff signatures while all
parameter types remain visible; the editable model retains full parameters.
Production code, tests and replays are unchanged.

## Current provenance checks

See `verification.json` and the repository's `docs/uml/verification-guide.md`. The 2026-11-14 refresh checks current source signatures and native database integrity. Native GUI reopening after this metadata refresh remains pending.

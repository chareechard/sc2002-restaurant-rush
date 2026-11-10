# Stage 2 class diagram - Richard draft

This diagram represents the complete production type set on
`work/stage-2` at code checkpoint
`c6e3c3f9ffe7e79e6dae1c2bb8b9436df7ec2b33`. The course cleanup passes
**115 checks** and updates the member contracts in Richard's reviewed diagram.
The original diagram and PR #6 integration remain in Git history; `main` and
the historical stage tags have not moved.

## Open and review

Open `Restaurant_Rush_Stage2.vpp` in Visual Paradigm 18 and select
**Stage 2 - Class Diagram - Richard Draft**. The project contains editable
UML classes, attributes, operations, enum literals, relationships and notes;
it is not an image embedded in a project file.

The project is maintained in **Visual Paradigm Standard 18.0**. The SVG is derived from the native export with its source subtitle synchronized; PNG and PDF views are rendered from that SVG
with UML font styles preserved. Use vector zoom to inspect members.
The native project does not require the optional generator plug-in to open or edit.

| File | Purpose |
| --- | --- |
| `Restaurant_Rush_Stage2.vpp` | Editable native project. |
| `class-diagram.svg` | Scalable diagram export. |
| `class-diagram.pdf` | Vector PDF export. |
| `class-diagram.png` | Raster preview. |
| `verification.json` | Source commit, validation results and artifact SHA-256 hashes. |
| `source/stage2-model.xml` | Auditable list of the selected source members, relationships and layout inputs. |
| `source/StageDiagramPlugin.java` | Optional native model generator, adapted from the team's Stage 1 generator; excluded from the game build. |
| `source/plugin.xml` | Generator plug-in descriptor. |
| `source/VerifyMembers.java` | Optional read-only checker against compiled Java classes; excluded from the game build. |
| `model-xml/` | Native Visual Paradigm XML export for inspection. |

## Reading the design

All **23 production types** are represented: the 21 retained Stage 1 types
and the two Stage 2 customer subclasses. Boundary classes are in `rush.cli`,
turn control and result types are in `rush.game`, entities and value types
are in `rush.domain`, and staff responsibilities are in `rush.staff`.

- White fill identifies a retained Stage 1 type, including the reviewed
  readability changes integrated through PR #6.
- Blue fill identifies `VIPCustomer` and `CriticCustomer`, introduced in Stage 2.
- An orange border identifies an existing class changed for Stage 2:
  `Scenario` creates the mixed customer schedule and `ConsoleApp` changes its
  displayed stage banner. The shared `Customer` contract already existed in
  Stage 1 and is retained.
- Visibility uses `+` for public and `-` for private. Abstract names and
  operations are italicized; static members are underlined. Enums use the
  enumeration stereotype. MenuItem, Receipt and GameStats are ordinary classes.

`Customer` owns the lifecycle and defines `chooseMeal`, `waitingLoss` and
`discountedBill`. `placeOrder` invokes the preference and pricing methods;
`losePatience` invokes the patience method. `GameEngine` handles these
objects through the shared contract. The three generalizations point to
the abstract superclass.

| Customer | Waiting loss per unserved turn | Meal choice | Pricing |
| --- | ---: | --- | --- |
| Regular | 8 | Pasta, Burger or Salad according to customer id | Full price |
| VIP | 5 | Burger | Pays 90% of the base price, rounded in integer cents |
| Critic | 12 | Pasta, a distinct preference from the Regular customer with id 3 | Full price |

`Scenario` creates C2 as VIP on turn 4 and C3 as Critic on turn 7. C1, C4,
C5 and C6 are Regular customers. Its three dashed factory dependencies
make those concrete creations explicit. The remaining three dependencies
show Waiter using Customer, Chef using Order, and Cashier using Customer.

## Relationships and multiplicities

The drawing contains **24 selected relationships**: 11 compositions, four
ordinary associations, six dependencies and three generalizations.
A filled diamond marks the composite owner. Stored references and parameter
types distinguish ownership from temporary use; an ordinary association does
not imply ownership. Structural navigability arrowheads are omitted, with
the actual reference types visible in the members and source model.

| Relationship | Multiplicity and meaning |
| --- | --- |
| ConsoleApp to GameEngine / ConsoleView | One of each per application. |
| GameEngine to Scenario / Menu / each staff role | One of each per game. |
| GameEngine to Table | Exactly two tables. |
| GameEngine to Customer | Zero to six customers as arrivals occur. |
| GameEngine to Order | The engine indexes zero to six orders; Customer is the composite owner. |
| GameEngine to Receipt | Zero to five payments can complete within the 18-turn base schedule. |
| Customer to Order | One customer owns zero or one order; each order belongs to one customer. |
| Table to Customer | Zero or one at each end: a free table has no occupant and an unseated or departed customer has no table. |
| Menu to MenuItem | One menu owns three fixed items. |
| Order to MenuItem | Each order references one menu item; an item can be referenced by many orders. |

These bounds describe the base-setting game. They do not impose constructor
restrictions on every possible standalone Java object. `RuleViolation` extends
the external JDK `IllegalArgumentException`; that superclass is recorded in
the diagram note rather than added to the production type count.

## Member selection and verification

The diagram selects key fields and operations, rather than repeating every
constructor or accessor. Reference fields represented by structural relationships
are not all duplicated in the GameEngine attribute compartment. Enum references
remain visible in field and operation types without a separate connector for
each use.

The current ConsoleApp contract uses Scanner and PrintStream; ConsoleView uses
PrintStream. Cashier.collect returns a Receipt or null. GameStats stores a double
average and exposes hasAverageSatisfaction() for the N/A display. List getters
build independent ArrayList copies with loops. The former record stereotypes
and Optional return/field types have been replaced in the model.

`verification.json` records the current member counts and checks against
compiled Stage 2 classes, including visibility, field types, static/final status,
parameters, return types and abstract status. It also records native-model and
relationship validation and artifact hashes.

The represented code checkpoint passes **115 Stage 2 checks**. Victory ends
at turn 18 with 5 served, 4 paid, $67.50 revenue and average paid satisfaction
70.25. Defeat has 0 paid, $0.00 and N/A average satisfaction. Full stdin replay
output is unchanged from the pre-cleanup Stage 2 code.

This diagram is prepared for review. Check future code changes against the
diagram and update its represented commit before the final handoff. Save and close Visual Paradigm before committing;
omit `.vpp.lck` files and `.vpp.bak*` backups.

## Current provenance checks

See `verification.json` and the repository's `docs/uml/verification-guide.md`. The 2026-11-14 refresh checks current source signatures and native database integrity. Native GUI reopening after this metadata refresh remains pending.

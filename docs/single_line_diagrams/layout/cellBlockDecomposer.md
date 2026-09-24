# Structure the cells into `Blocks`

Once the cells are detected (see [Cell detection](cellDetector.md)), each `BusCell` (`InternCell`, `ExternCell`, `ArchCell`)
and `ShuntCell` has to be organized into `Block` hierarchy so that a coordinate can later be computed for every node
(see [PositionFinder](positionFinder.md) and `BlockPositionner`).

Blocks are the building elements that are assembled hierarchically to organize the layout of the cells: a `Block` can
either directly embed a list of `Node`, or be composed of several sub-`Blocks`.

## The `Block` hierarchy

A `Block` is identified by a `Block.Type`:

* `LEGPRIMARY`\
  A primary block starting on a `BUS` node and going up to the first non-switch node, following the pattern
  `BUS - n*SWITCH - FICTITIOUS` (n >= 0). Such blocks instantiate `LegPrimaryBlock` and implement `LegBlock`.
* `FEEDERPRIMARY`\
  A primary block ending on a `FEEDER` node, following the pattern `FICTITIOUS - FEEDER`. Such blocks instantiate `FeederPrimaryBlock`.
* `BODYPRIMARY`\
  A primary block between two non-switch nodes that are not a `BUS` nor a `FEEDER`, following the pattern
  `FICTITIOUS - n*SWITCH - FICTITIOUS` (n >= 0). Such blocks instantiate `BodyPrimaryBlock`.
* `LEGPARALLEL`\
  Several `LegPrimaryBlock` sharing the same extremities, merged together. Such blocks instantiate `LegParallelBlock`
  and implement `LegBlock`.
* `BODYPARALLEL`\
  Several blocks (of any type, except `LEGPRIMARY` alone, which would be a `LEGPARALLEL`) sharing the same extremities,
  merged together. Such blocks instantiate `BodyParallelBlock`.
* `SERIAL`\
  A chain composed of blocks, each one starting where the previous one ends. Such blocks instantiate `SerialBlock`.
* `UNDEFINED`\
  A fallback block gathering the remaining blocks when the algorithm could not identify any further merge
  (see [Step 2: merge the blocks](#step-2-merge-the-blocks) below). Such blocks instantiate `UndefinedBlock`.

```{note}
All primaryBlocks are leaves of the hierarchy 
```

The `Block` type hierarchy, showing which types are primary/parallel/composed and which interfaces/classes they map to:

![blockHierarchy](../../_static/img/sld/layout/blockHierarchy.svg){align=center class="forced-white-background"}

The algorithm ends up building a single root `Block` per cell, stored by `Cell.setRootBlock`, on which the size and the
position of the cell will later be computed.

## Building the block hierarchy: `CellBlockDecomposer`

For each `BusCell`, the `CellBlockDecomposer.determineComplexCell` method builds the block hierarchy in two steps.

### Step 1: identify the primary blocks

Starting from each `BUS` node of the cell, the algorithm walks along the chains of `SWITCH` nodes until it reaches a
non-switch node, creating one `LegPrimaryBlock` per branch. It does the same starting from each `FEEDER` node, creating
one `FeederPrimaryBlock` per branch. The remaining chains of nodes (not starting on a `BUS` or ending on a `FEEDER`) are
then explored recursively to create the `BodyPrimaryBlock` instances, until every node of the cell has been assigned to
exactly as many primary blocks as its cardinality allows (a node shared by several blocks, e.g. a fork node, can belong
to several primary blocks).

A small cell graph and the resulting set of primary blocks (`LegPrimaryBlock`, `BodyPrimaryBlock`, `FeederPrimaryBlock`)
it is decomposed into: two parallel `SWITCH` branches respectively connect `BUS1` and `BUS2` to the same first
`FICTITIOUS` node, each one forming its own `LegPrimaryBlock` (`LegPrimaryBlock 1` on `BUS1` and `LegPrimaryBlock 2`
on `BUS2`); the `BodyPrimaryBlock` then connects that `FICTITIOUS` node to a second one through a `SWITCH`, and the
`FeederPrimaryBlock` connects that second `FICTITIOUS` node directly to the `FEEDER`:

![primaryBlockDecomposition](../../_static/img/sld/layout/primaryBlockDecomposition.svg){align=center class="forced-white-background"}

### Step 2: merge the blocks

The primary blocks are then merged together, repeatedly, until only one block remains:

* **Parallel merge**: two blocks are parallel if they share two "similar" extremity nodes (in the same order or
  reversed); two nodes are considered similar as soon as they are the same node, or they are both `FEEDER`-like nodes,
  or they are both `BUS` nodes (regardless of whether it is the very same `BusNode` or not). All the blocks found to
  be parallel to one another are merged into a single `LegParallelBlock` (if they are all `LegPrimaryBlock`) or
  `BodyParallelBlock` (otherwise). In the example above, `LegPrimaryBlock 1` and `LegPrimaryBlock 2` share the same
  `FICTITIOUS` fork node on one side, and, on the other side, `BUS1` and `BUS2`: even though `BUS1` and `BUS2` are two
  distinct `BusNode`, they are still considered similar extremities, so the two blocks are merged into a single
  `LegParallelBlock`.
* **Serial merge**: a chain of blocks, each one starting where the previous one ends, is merged into a single `SerialBlock`.

Both kinds of merge are attempted alternately until no more blocks can be merged. If, at some point, none of the two
merges can be applied while several blocks remain, the remaining blocks are gathered into an `UndefinedBlock`: this
means the pattern of the cell is not handled by the algorithm (this can be turned into an exception thanks to the
`exceptionIfPatternNotHandled` parameter, see [`PositionVoltageLevelLayoutFactoryParameters`](layout.md#the-positionvoltagelevellayoutfactoryparameters-class)).

The resulting `LegParallelBlock`, together with the `BodyPrimaryBlock` and `FeederPrimaryBlock` identified above, each
start where the previous one ends, so they are merged, in a single serial merge, into the final `SerialBlock` root:

![blockMerge](../../_static/img/sld/layout/blockMerge.svg){align=center class="forced-white-background"}

## Shunt cells

`ShuntCell` cells are simpler to handle: `CellBlockDecomposer.determineShuntCellBlocks` directly builds a single
`BodyPrimaryBlock` containing all the nodes of the cell.

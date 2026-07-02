# Zone Matrix Layout

In this layout, the substations are displayed like elements of a matrix (rows and columns).
The user can choose the location of each substation.

## Input parameters

- `VoltageLevelLayoutFactory`: builder of the layout used by voltage levels
- `SubstationLayoutFactory`: builder of the layout used by substations
- `ZoneLayoutPathFinderFactory`: builder of the pathfinders used to draw lines between substations
- `2D String array`: substation matrix position (ex: `{{"A", "B", "C"}}` = 1 row, 3 columns)

**Usage example:**
The following example displays three substations distributed on two columns and two lines,
with an empty area at the middle of the second line.

```java
// build zone graph
Network network = ...
List<String> zone = Arrays.asList("A", "B", "C");
ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

// Create substation 2D array representation
String[][] matrix = {{"A", "B"},
                     {"D", ""}};
// Create matrix zone layout using 2D array
ZoneLayoutPathFinderFactory pFinderFactory = DijkstraPathFinder::new;
SubstationLayoutFactory sFactory = new HorizontalSubstationLayoutFactory();
VoltageLevelLayoutFactory vFactory = new PositionVoltageLevelLayoutFactory();
MatrixZoneLayoutFactory mFactory = new MatrixZoneLayoutFactory(matrix);
Layout matrixLayout = mFactory.create(g, pFinderFactory, sFactory, vFactory);

// Apply matrix zone layout
matrixLayout.run(layoutParameters);
```

# Manually Positioned Zone Layout

In this layout, the substations are placed at user-defined positions.
In case the user-defined positions lead to overlaps of substations, those are automatically resolved.
Overlap resolution is done iteratively, keeping the first entries at their desired location and moving the later ones down or right in case of overlap.

## Input parameters

- `VoltageLevelLayoutFactory`: builder of the layout used by voltage levels
- `SubstationLayoutFactory`: builder of the layout used by substations
- `ZoneLayoutPathFinderFactory`: builder of the pathfinders used to draw lines between substations
- `desiredPositions`: list of the substations and their desired positions (ex: `List.of(Pair.of("A", new Point(100, 100)), Pair.of("B", new Point(800, 100)), Pair.of("C", new Point(500, 100)))`)

**Usage example:**
The following example displays three substations distributed on two columns and two lines,
with an empty area at the middle of the second line.

```java
// build zone graph
Network network = ...
List<String> zone = Arrays.asList("A", "B", "C");
ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

List<Pair<String, Point>> positions = List.of(
    Pair.of("A", new Point(100, 100)),
    Pair.of("B", new Point(800, 100)),
    Pair.of("C", new Point(500, 100))  // In between A and B with overlaps ==> Overlap resolution will move it elsewhere
);
// Create matrix zone layout using 2D array
ZoneLayoutPathFinderFactory pFinderFactory = DijkstraPathFinder::new;
SubstationLayoutFactory sFactory = new HorizontalSubstationLayoutFactory();
VoltageLevelLayoutFactory vFactory = new PositionVoltageLevelLayoutFactory();
ManuallyPositionedZoneLayoutFactory mFactory = new ManuallyPositionedZoneLayoutFactory(positions);
Layout layout = mFactory.create(g, pFinderFactory, sFactory, vFactory);

// Apply matrix zone layout
layout.run(layoutParameters);
```

# Path finding description

Both `MatrixZoneLayout` and `ManuallyPositionedZoneLayout` inherit from `AbstractPositionedZoneLayout` which handles pathfinding of lines between the different substations.

## Substation positioning

When the layout is run, `AbstractPositionedZoneLayout` calls `calculateCoordSubstations`, this function first runs the layout of all substations inside the zone layout:

```java
getGraph().getSubstations().forEach(sg -> layoutBySubstation.get(sg).run(layoutParameters));
```

This allows to get the size of each substation diagram. Then, it calls the abstract method `computeSubstationPositions`. Implementations of this method are responsible to compute substation positions that do not cause overlaps between substations, including a margin for `layoutParameters.getZoneLayoutSnakeLinePadding()`.

For the `MatrixZoneLayout`, this is done by placing substations in a matrix, where the height of each row is taken as the height of its largest element plus the snakeline margin (and the same for the width of each column).

For the `ManuallyPositionedZoneLayout`, this is done by placing substations at user-defined positions, then automatically resolving overlaps.

Once those substations positions are computed, the `AbstractPositionedZoneLayout` move the substations at those positions and then size the diagram accordingly

## Snakeline route computation between substations

The `Grid` class contains a 2D-array of `Node`, each `Node` representing a pixel of the SLD output file.
Each `Node` stores :
* A position (x and y)
* Availability (whether the `Node` can be used to draw the snakeline or not)
* A walk-through cost
* A parent node reference
* The distance to the end point of the snakeline

### Exclusion area
An exclusion area is an area where all `Node` have availability equals to `false`
This area cannot be used to draw a `snakeline`.
Those areas are:
- diagram padding
- voltage levels with padding
- snakelines right angles

### Shorter path computation
Dijkstra's computation steps:
* The starting point cost is set to 0
* The nearest neighbors (left, right, up and down) are computed (no diagonal moves allowed here)
    * These neighbors are used only if:
        * The neighbor is available
        * The neighbor was not yet visited
    * To avoid superfluous right angles, the cost is increased when the next point creates a right angle
* If no route can be computed by the algorithm, a straight line is drawn from the starting point to the end point of the snakeline (diagonal moves are allowed here)

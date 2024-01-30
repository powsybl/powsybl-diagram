# Zone Matrix Layout

We want to display each substation included in same zone as matrix way (row and column).<BR>
The user can choose the location of each substation.

## Input parameters

- `VoltageLevelLayoutFactory`: builder of  layout used by voltagelevels<br>
- `SubstationLayoutFactory`: builder of layout used by substations<br>
- `2D String array`: substation matrix position (ex: {{"A", "B", "C"}} = 1 row, 3 columns)<br>

**Usage example:**<BR>
The following example displays 3 substations distributed on 2 columns and 2 lines,<BR>
with an empty area at the middle of the second line.
```java
// build zone graph
Network network = ...
List<String> zone = Arrays.asList("A", "B", "C");
ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

// Create substation 2D array representation
String[][] substationsIds = {{"A", "B"},
                             {"D", ""}};
// Create matrix zone layout using 2D array
ZoneLayoutPathFinderFactory pFinderFactory = DijkstraPathFinder::new;
SubstationLayoutFactory sFactory = new HorizontalSubstationLayoutFactory();
VoltageLevelLayoutFactory vFactory = new PositionVoltageLevelLayoutFactory();
MatrixZoneLayoutFactory mFactory = new MatrixZoneLayoutFactory();
Layout matrixLayout = mFactory.create(g, substationsIds, pFinderFactory, sFactory, vFactory);
// Apply matrix zone layout
matrixLayout.run(layoutParameters);
```

## Path finding description

### Premise:
- The column width is computed for each column as the maximum width of all the substations on the column
- The row height is computed for each row as the maximum height of all the substations on the row
- Snakeline lane dimensions (both horizontal and vertical) are set with `LayoutParameters.setZoneLayoutSnakeLinePadding`

Example:

|                 |                     |      ZonePadding      |                      |                 |                     |      ZonePadding      |                      |                 |
|:---------------:|:-------------------:|:---------------------:|:--------------------:|:---------------:|:-------------------:|:---------------------:|:--------------------:|:---------------:|
|                 |                     |  __VL TOP Padding__   |                      |                 |                     |  __VL TOP Padding__   |                      |                 | 
| __ZonePadding__ | __VL LEFT Padding__ |         __A__         | __VL RIGHT Padding__ | __ZonePadding__ | __VL LEFT Padding__ |         __B__         | __VL RIGHT Padding__ | __ZonePadding__ |
|                 |                     | __VL BOTTOM Padding__ |                      |                 |                     | __VL BOTTOM Padding__ |                      |                 | 
|                 |                     |    __ZonePadding__    |                      |                 |                     |    __ZonePadding__    |                      |                 |
|                 |                     |  __VL TOP Padding__   |                      |                 |                     |  __VL TOP Padding__   |                      |                 |
| __ZonePadding__ | __VL LEFT Padding__ |         __D__         | __VL RIGHT Padding__ | __ZonePadding__ | __VL LEFT Padding__ |           _           | __VL RIGHT Padding__ | __ZonePadding__ |
|                 |                     | __VL BOTTOM Padding__ |                      |                 |                     | __VL BOTTOM Padding__ |                      |                 |
|                 |                     |    __ZonePadding__    |                      |                 |                     |    __ZonePadding__    |                      |                 |


The class `MatrixZoneLayout` represents the matrix layout.<BR>
The class `MatrixZoneLayoutModel` represents the matrix and the path finder information.
The class `Matrix` contains an array of `MatrixCell`.

The class `MatrixCell` stores information related to the matrix cell:
- Position (indices) in the matrix : row, column
- The id of the substation grpah contained by the cell

### Substation positioning
1) In the `MatrixZoneLayout` constructor, each `SubstationGraph` is added to the `MatrixZoneLayoutModel` (internal model of matrix layout) as following:
```java
for (int row = 0; row < matrix.length; row++) {
    for (int col = 0; col < matrix[row].length; col++) {
        String id = matrix[row][col];
        SubstationGraph sGraph = graph.getSubstationGraph(id);
        if (sGraph == null && !id.isEmpty()) {
            throw new PowsyblException("Substation '" + id + "' was not found in zone graph '" + getGraph().getId() + "'");
        }
        model.addSubstationGraph(sGraph, row, col);
    }
}
    ...
```
2) Each substation position is computed following this method:
```java
protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
```
- the `SubstationLayout` is applied on each not empty substation specified as following:
```java
    // Display substations on not empty Matrix cell
    matrix.stream().filter(c -> !c.isEmpty()).map(MatrixCell::graph).forEach(graph -> layoutBySubstation.get(graph).run(layoutParameters));
 ```
- Each substation is moved into its matrix position as specified

### Snakeline route computation between substations

The `Grid` class contains a 2D-array of `Node`, each `Node` representing a pixel of the SLD output file.
Each `Node` store :
* A position (x and y)
* An availability (whether the `Node` can be used to draw the snakeline or not)
* A walk-through cost
* A parent node reference
* The distance to the end point of the snakeline

#### Exclusion area
An exclusion area is an area where all `Node` have an availability equals to `false`
This area cannot be used to draw a `snakeline`.
Those areas are:
- diagram padding
- voltagelevels with padding
- snakelines right angles

#### Shorter path computation
Dijkstra's computation steps:
* The starting point cost is set to 0
* The nearest neighbors (left, right, up and down) are computed (no diagonal moves allowed here)
  * These neighbors are used only if:
    * The neighbor is available
    * The neighbor was not already visited
  * In order to avoid superfluous right angles, the cost is increased when the next point creates a right angle
* If no route can be computed by the algorithm, a straight line is drawn from the starting point to the end point of the snakeline (diagonal moves are allowed here)

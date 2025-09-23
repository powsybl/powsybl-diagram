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
ZoneLayoutPathFinderFactory pFinderFactory = AStarPathFinder::new;
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

The `AvailabilityGrid` class contains a 2D-array of `byte`, each `byte` representing a state of the pixel of the SLD output file.
Possible values are :
* 0 : not available
* 1 : wire (where there is a snakeline)
* 2 : around wire
* 3 : available

#### Exclusion area
An exclusion area is an area where all `pixel` of the grid are not available.
This area cannot be used to draw a `snakeline`.
This currently includes:
- voltagelevels with padding

All other areas start with a default value of 3 : available

#### Shorter path computation
A* computation steps:
* Find all VoltageLevel + padding and set state to 1 : Not available; this is done in `MatrixZoneLayoutModel.fillPathFindingGridStates`
* For each path:
  * Insert a free path in the VoltageLevel padding, in order to reach the point from which the snakeline starts
  * Set the current point as the starting point, set cost of the path to 0
  * For the current point, explore neighbors, a neighbor is explored only if:
    * the neighbor point is left, right, down or up compared to the current point (no diagonal moves) 
    * the neighbor is not "not available" (meaning wire, around wire and available are all ok)
    * the cost of the path to that neighbor is the lowest cost of a path to that neighbor that has been found (meaning if we found another path with lower cost to the same point, we ignore that neighbor)
  * To compute the cost of moving to that neighbor, we calculate the sum of:
    * the cost of path to the current node
    * 1 (cost of the default movement)
    * if the neighbor node is a wire, the cost of landing on a wire
    * if the neighbor node is around a wire, the cost of landing around a wire
    * if there is a right angle between the previous point of the path, the current point and the neighbor, the cost of a right angle turn
  * The idea behind this computation is to penalize wire crossing, wires that are too close (less readable) and too many right angle turns
  * Calculate the total cost as the cost of the path + a metric (currently the manhattan distance between the neighbor and the goal point)
  * take the next point with the lowest total cost and explore its neighbors (we use a priority queue to keep track of the lowest cost)
* We end once one path reaches the goal, it's the path with the lowest cost

# Zone Matrix Layout

We want to display each substation included in same zone as matrix way (row and column).<BR>
The user can choose the location of each substation.

## Input parameters

- `VoltageLevelLayoutFactory`: builder of  layout used by voltagelevels<br>
- `SubstationLayoutFactory`: builder of layout used by substations<br>
- `2D String array`: substation matrix position (ex: {{"A", "B", "C"}} = 1 orw, 3 columns)<br>

**Usage example:**<BR>
The following example use 5 substations distributed on 3 columns and 2 lines,<BR>
with an empty area at middle of the second line.
```java
// build zone graph
Network network = ...
List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

// Create substation 2D array representation
String[][] substationsIds = {{"A", "B", "C"},
                             {"D", "", "E"}};
// Create matrix zone layout using 2D array
Layout matrixLayout = new MatrixZoneLayoutFactory().create(g, substationsIds, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory());
// Apply matrix zone layout
matrixLayout.run(layoutParameters);
```

## Path finding description 

### Premise:
- same height for each line: maximum height of all voltagelevels
- same width for each columns: maximum width of all voltagelevels
- each lines margin can be set with `LayoutParameters.getMatrixLayoutPadding`
- each lines columns can be set with `LayoutParameters.getMatrixLayoutPadding`

Example:

|            |       |   Margin   |        |            |       |   Margin   |       |            |       |  Margin    |       |            |
|:----------:|:-----:|:----------:|:------:|:----------:|:-----:|:----------:|:-----:|:----------:|:-----:|:----------:|:-----:|:----------:|
|            | __X__ |   __X__    | __X__  |            | __X__ |   __X__    | __X__ |            | __X__ |   __X__    | __X__ |            |
| __Margin__ | __X__ |   __A__    | __X__  | __Margin__ | __X__ |   __B__    | __X__ | __Margin__ | __X__ |   __C__    | __X__ | __Margin__ |
|            | __X__ |   __X__    | __X__  |            | __X__ |   __X__    | __X__ |            | __X__ |   __X__    | __X__ |            |
|            |       | __Margin__ |        |            |       | __Margin__ |       |            |       | __Margin__ |       |            |
|            | __X__ |   __X__    | __X__  |            | __X__ |   __X__    | __X__ |            | __X__ |   __X__    | __X__ |            |
| __Margin__ | __X__ |   __D__    | __X__  | __Margin__ | __X__ |     _      | __X__ | __Margin__ | __X__ |   __E__    | __X__ | __Margin__ |
|            | __X__ |   __X__    | __X__  |            | __X__ |   __X__    | __X__ |            | __X__ |   __X__    | __X__ |            |
|            |       | __Margin__ |        |            |       | __Margin__ |       |            |       | __Margin__ |       |            |


The class `MatrixZoneLayout` represent the matrix layout.<BR>
Each substation position is computed in following method: 
```java
protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
```
The `SubstationLayout` is applied on each substation specified.<BR>
Each `SubstationGraph` is added to the class `MatrixZoneLayoutModel` (internal model of matrix layout).
```java

    for (int row = 0; row < matrix.length; row++) {
        for (int col = 0; col < matrix[row].length; col++) {
            String id = matrix[row][col];
            SubstationGraph graph = getGraph().getSubstationGraph(id);
            if (graph != null) {
                // Display substations
                layoutBySubstation.get(graph).run(layoutParameters);
            }
            model.addGraph(graph, col, row);
        }
    }
    ...
```

### Snakeline way computation between substation

The `Grid` class is a 2D array as a list of `Node` class representing each pixel of SLD output file.
Each `Node` store :
* a carteian position (x and y)
* a walkthrough cost
* a parent node reference
* a distance to goal point

#### Exclusion area
An exclusion area is all `Node` with cost equals to `-1` 
This area can be used to draw a `snakeline`.
This area allow snakeline to escape to:
- voltagelevels
- empty areas (missing substations)
- previous snakelines

Example:



#### Shorter path computation

Computation steps:
* starting point cost is set to 0
* get nearest neighbors (left, right, up and down): no diagonal moves
  * these neighbors are used only if:
    * the neighbor is available (cost != -1)
    * the neighbor was not already visited
  * to avoid useless right angle the cost is increased when next point will create a right angle
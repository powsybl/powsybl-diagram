# Cell detection

A `Cell` represents a connected subgraph of nodes that participate in a common purpose.\

The goal of the cell detection is to create these subgraphs that will then:

* Structure the organisation of the busbar layout;
* Be displayed according to their types.

## Cell types

Cells can be of one of the following enum `CellType`:

- `INTERN`\
The smallest subGraph delimited by `BUS` nodes (i.e. not including `FEEDER`) and that is not shunting `ExternCell` cells (see `SHUNT` definition below).\
Such cells instantiate the `InternCell` subclass.  

- `EXTERN`\
The smallest subGraph delimited by `BUS` nodes and `FEEDER` nodes with at least one node having the following property: each branch extracted from this very node ends with nodes of a single `nodeType` among `BUS` or `FEEDER` or `SHUNT`.\
Such cells instantiate the `ExternCell` subclass.  

- `SHUNT`\
A path between two `FictitiousNode` nodes of two `ExternCell` cells.\
Such cells instantiate the `ShuntCell` subclass.  

- `ARCH`\
A path between an `ExternCell` and a `BusNode`.\
Such cells instantiate the `ArchCell` class. \
This type is obtained when an `ExternCell` is linked to two subsections, which cannot be displayed with the current algorithm.
Such an `ExternCell` is then cut in an `ExternCell` only on one subsection, and in several `ArchCell`.

The figure shows examples of cells of several `CellType`.  

![cellType](/_static/img/cellTypes.svg)

## Cell detection implementation

### Preliminary step: cleaning the graph
A preliminary step is done inside the `GraphRefiner` class. It performs some preprocessing on the input graph by calling several methods:
* An _optional_ call to `Graph.substituteInternalMiddle2wtByEquipmentNodes()` to simplify the graph by substituting each internal two-winding transformers (i.e. both ends are in the same voltage level) `FeederNode` by a `EquipmentNode`, to avoid unnecessary snake lines;
* A _systematic_ call to `GraphRefiner::handleConnectedComponents` which ensures that each connected component contains a `BusNode`: it is adding one if there is none;
* A _systematic_ call to `Graph.substituteFictitiousNodesMirroringBusNodes()` to simplify the graph by removing `FICTITIOUS` nodes which are the only adjacent node of a `BusNode`;
* An _optional_ call to `Graph.removeUnnecessaryConnectivityNodes()` to simplify the graph by removing redundant `FICTITIOUS` nodes;
* An _optional_ call to `Graph.substituteSingularFictitiousByFeederNode()` to simplify the graph by replacing internal nodes with only one neighbor with a fictitious feeder node;
* An _optional_ call to `Graph.removeFictitiousSwitchNode()` to simplify the graph by removing the fictitious switch nodes;
* A _systematic_ call to `Graph.extendBusesConnectedToBuses()` to add 2 connectivity nodes between 2 buses that are connected to each other; 
* A _systematic_ call to `Graph.insertBusConnections` to create a connection between a bus node and its adjacent nodes; 
* A _systematic_ call to `Graph.insertHookNodesAtBuses()` to TODO
* A _systematic_ call to `Graph.insertHookNodesAtFeeders()` to TODO
* A _systematic_ call to `Graph.substituteNodesMirroringGroundDisconnectionComponent()` to deal with ground disconnector displaying.


### Cell detection algorithm

The `ImplicitCellDetector` class implements an algorithm sticking to the above cell type definitions.
The `detectCell` method is used to detect cells by exploring the graph.

It takes as parameter two lists of types that delimit the traversal algorithm :

* `stopTypes` list: for types that end a current branch
* `exclusionTypes` list: for types that invalidate the current subgraph.

The algorithm is explained based on the following graph that would result in the figure displayed to illustrate the cellTypes enum:

![rawGraph](/_static/img/rawGraph.svg)

##### Step 1: identify `InternCell` cells

- stopTypes: BUS
- exclusionTypes: FEEDER

`InternCell` cells are easy to determine as being exclusively bordered by `BUS` nodes.

![rawGraphIntern](/_static/img/rawGraphIntern.svg)

##### Step 2: identifies `ExternCell` cells

- stopTypes: BUS, FEEDER
- exclusionTypes: 

If one node of the subgraph has each of its branches ending with one single kind of `NodeType` among `BUS` and `FEEDER`, ("_bottleneck_" node in the picture) this is an `ExternCell`.

Other `ExternCell` cells could be discovered in the next steps when adding the `SHUNT NodeType`.

![rawGraphExtern](/_static/img/rawGraphExtern.svg)

##### Step 3: discriminates `EXTERN` and `SHUNT` cells

- stopTypes: 
- exclusionTypes: BUS, FEEDER, SHUNT
  To identify the first candidate `SHUNT` node, each `FICTITIOUS` node with more than 3 branches are visited. The expected property of the `SHUNT` node is that:

. 1+ branch(s) ends with only `BUS` nodes
. 1+ branch(s) ends with only `FEEDER` nodes
. 1 branch is ends with `FEEDER` *and* `BUS` nodes.

The branches of the first two categories constitutes the first `ExternCell` cell.

Then the `SHUNT` cell is constituted of:

* The first `SHUNT` node
* The string of nodes that have only 2 adjacent nodes
* The first node with more than 2 adjacent nodes that becomes the second `SHUNT` node

Last, the second `ExternCell` cell is build with the second `SHUNT` node and the remaining nodes.

![rawGraphExternShunt](/_static/img/rawGraphExternShunt.svg)


⚠️ Any other pattern is not handled by the algorithm.

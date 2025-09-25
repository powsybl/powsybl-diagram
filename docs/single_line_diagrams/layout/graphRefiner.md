# Graph refiner

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
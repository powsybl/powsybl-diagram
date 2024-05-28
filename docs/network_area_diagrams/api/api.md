# API guide

## Introduction

The powsybl-network-area-diagram artifact provides features to generate concise and customized diagrams of the network:
- Display of the graph whose nodes are the network voltage levels, and whose edges are the lines and transformers between those voltage levels;
- Generation of diagrams of the whole network or of part of the network, given a voltage level and a depth, or a list of voltage levels and a (unique) depth;
- Graph layout default implementation using a basic force layout algorithm, taken from [springy](https://github.com/dhotson/springy)
- Diagram customization:
    - Possible use of your own graph layout implementation;
    - Possible use of your own label provider to display custom directed values on the graph edges (default label provider displays the active power);
    - Possible use of your own style provider to have a custom style for nodes and edges (default style provider gives the nodes and edges a class corresponding to their voltage level and gives disconnected lines a specific class);
    - Possible use of your custom layout parameters and svg rendering parameters.

![nad-example](/_static/img/nad-example.png)

The powsybl-network-area-diagram artifact belongs to the [powsybl-diagram repository](https://github.com/powsybl/powsybl-diagram). Before the 3.0.0 version, it was stored in an [independent repository](https://github.com/powsybl/powsybl-network-area-diagram) (now archived).

## Examples

These examples show how to write a network-area diagram into an SVG file.

* Generate a network-area diagram for the network `network`

```java
NetworkAreaDiagram.draw(network, Path.of("/tmp/diagram.svg"));
```

* Generate a network-area diagram for the network `network`, with customized `SvgParameters` and `LayoutParameters`

```java
SvgParameters svgParameters = new SvgParameters().setFixedHeight(1000);
LayoutParameters layoutParameters = new LayoutParameters().setSpringRepulsionFactorForceLayout(0.2);
NadParameters nadParameters = new NadParameters().setSvgParameters(svgParameters).setLayoutParameters(layoutParameters);
NetworkAreaDiagram.draw(network, Path.of("/tmp/diagram2.svg"), nadParameters, VoltageLevelFilter.NO_FILTER);
```
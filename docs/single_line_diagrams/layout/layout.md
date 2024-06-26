# Layouts

A layout represents the way in which the elements of a graph are arranged.

It is possible to use your own graph layout implementation but there are also existing layouts in powsybl-diagram, ready to use.


## Layouts for voltage levels

### PositionVoltageLevelLayout

This class is used to position the different elements inside a voltage level according to the following process:

- [Clean the graph to have the expected patterns](graphRefiner.md)
- [Detect the cells (intern / extern / shunt)](cellDetector.md)
- Organize the cells into blocks
- Calculate real coordinates of busNode and blocks connected to busbars

### RandomVoltageLevelLayout
TODO

## Layouts for substations
TODO

## Layouts for multi-substation graphs
TODO
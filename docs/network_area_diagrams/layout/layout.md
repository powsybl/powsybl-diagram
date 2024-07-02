# Layouts

Several layout factories implementations are available.

## Basic force layout
The layout factory `BasicForceLayoutFactory` is based on a force layout algorithm, with the following forces interacting on the voltage levels graph:
- repulsion Coulomb forces between all nodes,
- attraction spring forces between nodes linked with an edge,
- attraction force to the diagram center for all the node, to avoid the connected components to get further and further away at each step.

## Fixed layout
The layout factory `FixedLayoutFactory` is based on a set of provided fixed positions, and on an additional layout.
The provided additional layout is run only on voltage levels with missing positions.

## Geographical layout
The layout factory `GeographicalLayoutFactory` is based on the geographical positions provided by the `SubstationPosition` extension, and on an additional layout.

First, a Mercator projection (scale factor may be specified) is used to put each latitude/longitude coordinate on an x/y plane.
Note that to avoid overlapping, voltage levels within the same substation are placed on a circle (radius may be specified). 

Then, the provided additional layout is run, with the previously mentioned projected coordinates fixed.
Note that, by default, the additional layout is the basic force layout mentioned above, with some forces disabled:
- the repulsion Coulomb forces are only between non-fixed nodes,
- the attraction force to the diagram center is disabled.

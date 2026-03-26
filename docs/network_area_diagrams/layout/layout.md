# Layouts

```{toctree}
---
maxdepth: 2
hidden: true
---

layoutParameters.md
atlas2Parameters.md
```

Several layout factory implementations are available.

## Force layout

### Basic force layout
The layout factory `BasicForceLayoutFactory` is based on a force layout algorithm, with the following forces interacting on the voltage level graph:
- Repulsion Coulomb forces between all nodes;
- Attraction spring forces between nodes linked with an edge;
- Attraction force to the diagram center for all the nodes, to avoid the connected components to get further and further away at each step.

### Atlas2 force layout

The layout `Atlas2ForceLayout` is based on a research paper for an algorithm (Atlas2). The forces used are:
- Linear repulsion force between all nodes
- Linear attraction force between nodes with an edge
- Attraction for to the diagram center for all the nodes, to avoid the connected components to get further and further away at each step.

Compared to the Basic force layout, it tends to give better results visually, and it also does it faster (for multiple reasons, like adaptative local speed for each point and forces
that are easier to calculate). This is the preferred way to make graphs that are visually pleasing.

## Fixed layout
The layout factory `FixedLayoutFactory` is based on a set of provided fixed positions, and on an additional layout.
The provided additional layout is run only on voltage levels with missing positions.

## Geographical layout
The layout factory `GeographicalLayoutFactory` is based on the geographical positions provided by the `SubstationPosition` extension, and on an additional layout.

First, a Mercator projection (scale factor may be specified) is used to put each latitude/longitude coordinate on an x/y plane.
Note that to avoid overlapping, voltage levels within the same substation are placed on a circle (radius may be specified). 

Then, the provided additional layout is run, with the previously mentioned projected coordinates fixed.
Note that, by default, the additional layout is the basic force layout mentioned above, with some forces disabled:
- The repulsion Coulomb forces are only between non-fixed nodes;
- The attraction force to the diagram center is disabled.

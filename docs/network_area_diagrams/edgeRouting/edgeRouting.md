# Edge Routing

The `EdgeRouting` interface provides a way to customize the computation of paths of the network-area diagram.
The chosen edge routing can be specified through `NadParameters::setEdgeRouting`.
Note that, internally, the edge routing is passed to the `SvgWriter` which launches the computation.

Currently, there are 2 implementations of the `EdgeRouting`: the `StraightEdgeRouting` and the `CustomPathRouting`.
The default implementation if none specified is the `StraightEdgeRouting`.

## Common features
The common features are factorized in the abstract class `AbstractEdgeRouting`.

### Loops
The loop edges points are computed so that they are distributed between branch edges when the space available is big enough.

![loops](/_static/img/nad/loops-distribution.png)

### Injections
If injections are displayed, the injection edge points are computed similarly to loops: they are distributed between branch edges when the space available is big enough.

![injections](/_static/img/nad/injections-distribution.png)

### Three-winding transformers
The three-winding edge points are computed by finding a "leading" transformer edge and then placing the other edges at 120°.
The leading edge is defined as the opposite edge of the smallest aperture.

![3WTedges](/_static/img/nad/3wt-edges.png)

## StraightEdgeRouting feature
In the `StraightEdgeRouting` implementation,
- the parallel edges points are computed so that they form a fork,
- the other edges points are computed as straight lines.

![forkstraight](/_static/img/nad/fork-straight.png)

## CustomPathRouting feature
In the `CustomPathRouting` implementation, custom paths can be provided through two maps in the constructor
- a map whose keys are branch ids and whose values are the list of "bending" points to add to the corresponding branch edge,
- a map whose keys are voltage level ids and whose values are the list of "bending" points to add to the corresponding text edge.

If a branch id is missing in the given map, we fall back to `StraightEdgeRouting` implementation.
Similarly for text edges, if a voltage level id is missing in the corresponding map, we fall back to `StraightEdgeRouting` implementation.

![custompaths](/_static/img/nad/custom-paths.png)

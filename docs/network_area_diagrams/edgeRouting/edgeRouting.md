# Edge Routing

The `EdgeRouting` interface provides a way to customize the computation of paths of the network-area diagram.

Currently, there are 2 implementations of the `EdgeRouting`: the `StraightEdgeRouting` and the `CustomPathsRouting`

## Common features
The common features are factorized in the abstract classes `AbstractEdgeRouting`.

### Loops
The loop edges points are computed so that they are distributed between branch edges when the space available is big enough.

### Injections
If injections are displayed, the injection edge points are computed similarly to loops: they are distributed between branch edges when the space available is big enough. 

### Three-winding transformers
The three-winding edge points are computed by finding a "leading" transformer edge and then placing the other edges at 120°.
The leading edge is defined as the opposite edge of the smallest aperture.

## StraightEdgeRouting feature
In the `StraightEdgeRouting` implementation, 
- the parallel edges points are computed so that they form a fork,
- the other edges points are computed as straight lines.

## CustomPathsRouting feature
In the `CustomPathsRouting` implementation, custom paths can be provided through two maps in the constructor
- a map whose keys are branch ids and whose values are the list of "bending" points to add to the corresponding branch edge,
- a map whose keys are voltage level ids and whose values are the list of "bending" points to add to the corresponding text edge.

If a branch id is missing in the given map, we fall back to `StraightEdgeRouting` implementation.
Similarly for text edges, if a voltage level id is missing in the corresponding map, we fall back to `StraightEdgeRouting` implementation.

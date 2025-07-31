# Other layout parameters

Apart from using layoutParameters, more parameters can be set by using the parameters from diagram-util.
This is currently only available with Atlas2 (and not with basic force layout).

Parameters can be built as follows:

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder()
        .withMaxSteps(500)
        .withRepulsion(3)
        .withSpeedFactor(1.2)
        .build();
Atlas2ForceLayout atlas2ForceLayout = new Atlas2ForceLayout(new SquareRandomSetup<>(), atlas2Parameters);
atlas2ForceLayout.run(graph, layoutParameters);
```

Note that once created, atlas2ForceLayout can be used on multiple different graph without having to create the object again (but the calculations will still have to be done again from the start).

## Default values

| Name                                    | Type      | Default value | Value range       |
|-----------------------------------------|-----------|---------------|-------------------|
| $maxSteps$                              | `int`     | 6000          | $\geq$ 1          |
| $repulsion$                             | `double`  | 4             | $\gt$ 0           |
| $edgeAttraction$                        | `double`  | 1             | $\gt$ 0           |
| $attractToCenter$                       | `double`  | 0.001         | $\geq$ 0          |
| $speedFactor$                           | `double`  | 1             | $\gt$ 0           |
| $maxSpeedFactor$                        | `double`  | 10            | $\gt speedFactor$ |
| $swingTolerance$                        | `double`  | 1             | $\gt$ 0           |
| $maxGlobalSpeedIncreaseRatio$           | `double`  | 1.5           | $\gt$ 1           |
| $activateRepulsionForceFromFixedPoints$ | `boolean` | true          | true / false      |
| $activateAttractToCenterForce$          | `boolean` | true          | true / false      |
| $iterationNumberIncreasePercent$        | `double`  | 0             | $\geq$ 0          |

### maxSteps

Change the maximum number of iteration the algorithm is allowed to run. Atlas2 has a stopping criterion, so for most networks, the run ends before the maximum
number of steps is reached. Changing the maximum number of steps generally becomes relevant only when going past 8k nodes networks, with the default parameters.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withMaxSteps(500).build();
```

### repulsion

The coefficient of repulsion controls the intensity of the repulsion force between all nodes. Increasing this will make the network more sparse (ie nodes will be further apart).

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withRepulsion(10).build();
```

### edgeAttraction

The coefficient of edge attraction controls the force between points that share an edge, increasing this might help with emphasizing clusters of points. It will also tend to make the graph smaller.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withEdgeAttraction(0.5).build();
```

### attractToCenter

The coefficient for the force that attracts all points to the center of the 2D space. Smaller values will lead to a less dense graph.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withAttractToCenter(0.006).build();
```

### speedFactor

Coefficient used to calculate individual point speed factor based on the global graph speed. The link between global and local speed is not a simple multiplication by this coefficient, but it is used in the calculation.
If this is lower, points will be slower. A lower value might give worse results in terms of convergence speed, but it might help with stability.
A value between 0.8 and 1.2 is generally good.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withSpeedFactor(1.1).build();
```

### maxSpeedFactor

The maximum factor for local speed compared to global speed. Lowering this might mean slower convergence, but it improves stability (ie points might swing less around their stability position).
This value should always be bigger than the speedFactor (it still works if it's smaller, but it will work better if it's bigger).
Increasing this too much opens the door to erratic behaviour.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withMaxSpeedFactor(15).build();
```

### swingTolerance

How much do we accept swing contributing to the global speed of the graph. A lower value means that we accept less swinging. 
If this value is too low, the global speed of the graph will be low and convergence will slow down. If it's too high, we can
observe erratic behaviour in the way the points move. You probably shouldn't change this unless you know what you are doing.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withSwingTolerance(1.3).build();
```

### maxGlobalSpeedIncreaseRatio

How much can the global speed increase between each step. Higher means that the graph will reach a good global speed faster, but it might lead to more erratic behaviour between each step

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withMaxGlobalSpeedIncreaseRatio(2).build();
```

### activateRepulsionForceFromFixedPoints

If set to true, other points will get a repulsion effect from unmovable points (fixed points), otherwise fixed points do not repel other points.

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withActivateRepulsionForceFromFixedPoints(true).build();
```

### activateAttractToCenterForce

Activate or deactivate the force that attracts points to the center of the graph. It is used to prevent non-connected points 
from drifting away. It is generally ill-advised to deactivate this, but if you are sure that everything is connected together then you can deactivate it

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withActivateAttractToCenterForce(true).build();
```

### iterationNumberIncreasePercent

By how many iteration (in percent) you want to increase the run once the stopping condition was met.
The stopping condition generally stops when the graph is "good enough", but for specific use you might want a graph that looks better.
Increasing the number of iterations past the stopping condition will increase the visual quality of the graph, but it will also take longer.
This coefficient will also be directly the increase in runtime (e.g. if you use 10%, you will have a 10% longer runtime).

```java
Atlas2Parameters atlas2Parameters = new Atlas2Parameters.Builder().withIterationNumberIncreasePercent(25).build();
```

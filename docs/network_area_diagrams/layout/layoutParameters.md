# Layout parameters

The `LayoutParameters` class gathers parameters to customize the layout of the graph, i.e. the way the different elements are positioned on the graph.

All parameters have default values.

| Name                           | Type      | Default value   |
|--------------------------------|-----------|-----------------|
| $maxSteps$                     | `int`     | 1000            |
| $textNodesForceLayout$         | `boolean` | false           |
| $textNodeFixedShift$           | `Point`   | Point(100, -40) |
| $textNodeEdgeConnectionYShift$ | `double`  | 25              |
| $injectionsAdded$              | `boolean` | false           |

Users can customize one or several parameters according to their needs. 

In the following example, the `maxSteps` parameter and the `textNodesForceLayout` parameter are customized, the other parameters are left to their default values:

```java
LayoutParameters layoutParameters = new LayoutParameters().setMaxSteps(500).setTextNodesForceLayout(true);
```

Layout parameters are further described below.

## Layout algorithm parameters

### The `maxSteps` parameter

The `maxSteps` parameter represents the maximum number of iterations that an automatic layout algorithm is permitted to perform. The value assigned to this parameter strikes a balance between speed and rendering quality.

With `maxSteps = 1000`(default value)

![diamond1000](/_static/img/nad/diamond-network_1000.svg)

With `maxSteps = 100`:

```java
LayoutParameters layoutParameters = new LayoutParameters().setMaxSteps(100);
```

![diamond100](/_static/img/nad/diamond-network_100.svg)


NB1: for a very simple network like the one displayed above, the difference in speed is not significant. 

NB2: the maximum number of iterations is not always reached as there are typically other stopping criteria in layout algorithms.


## Text node parameters

### The `textNodesForceLayout` parameter

If the `textNodesForceLayout` parameter is set to `true`, the text box nodes are positioned by the force layout algorithm. If the parameter is set to `false`, the text boxes are fixed in position relative to the voltage level node to which they are attached.

With `textNodesForceLayout = false` (default value)

![textNodeFixed](/_static/img/nad/text-node-fixed.svg)

With `textNodesForceLayout = true`

```java
LayoutParameters layoutParameters = new LayoutParameters().setTextNodesForceLayout(true);
```

![textNodeForce](/_static/img/nad/text-node-force-layout.svg)

### The `textNodeFixedShift` parameter

The `textNodeFixedShift` parameter represents the offset between the text box node and the voltage level node when the text node positions are fixed (i.e. when `textNodesForceLayout = false`).

With `textNodeFixedShift = Point(100, -40)` (default value)

![textNodeFixed](/_static/img/nad/text-node-fixed.svg)

With `textNodeFixedShift = Point(50, 40)`

```java
LayoutParameters layoutParameters = new LayoutParameters().setTextNodeFixedShift(50, 40);
```

![textNode50_40](/_static/img/nad/text-node-50-40.svg)

### The `textNodeEdgeConnectionYShift` parameter

The `textNodeEdgeConnectionYShift` parameter is used to customize the position of the edge connection point on the text box.

With `textNodeEdgeConnectionYShift = 25` (default value)

![textNodeFixed](/_static/img/nad/text-node-fixed.svg)

With `textNodeEdgeConnectionYShift = 50`

```java
LayoutParameters layoutParameters = new LayoutParameters().setTextNodeEdgeConnectionYShift(50d);
```

![textNodeYShift50](/_static/img/nad/text-node-connection-y-shift-50.svg)

## Injection parameters

### The `injectionsAdded` parameter

This parameter allows the user to display the injections that are present on the bus nodes of the voltage levels.

With `injectionsAdded = false`:

![4substationsNoInjections](/_static/img/nad/four_substations_no_injection.svg)

With `injectionsAdded = true`:

```java
LayoutParameters layoutParameters = new LayoutParameters().setInjectionsAdded(true);
```

![4substationsWithInjections](/_static/img/nad/four_substations_with_injections.svg)

The represented injections are listed in the table below.

| Icon in the DefaultLibrary                                                      | Injection type                                                                                       |
|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| ![generatorInjection](/_static/img/nad/nad_icons/generator.svg)                 | [Generator](inv:powsyblcore:*:*#grid_model/network_subnetwork#generator)                             |
| ![batteryInjection](/_static/img/nad/nad_icons/battery.svg)                     | [Battery](inv:powsyblcore:*:*#grid_model/network_subnetwork#battery)                                 |
| ![loadInjection](/_static/img/nad/nad_icons/load.svg)                           | [Load](inv:powsyblcore:*:*#grid_model/network_subnetwork#load)                                       |
| ![shuntCompensatorCapacitorInjection](/_static/img/nad/nad_icons/capacitor.svg) | [Shunt compensator (capacitor)](inv:powsyblcore:*:*#grid_model/network_subnetwork#shunt-compensator) |
| ![shuntCompensatorInductorInjection](/_static/img/nad/nad_icons/inductor.svg)   | [Shunt compensator (inductor)](inv:powsyblcore:*:*#grid_model/network_subnetwork#shunt-compensator)  |
| ![staticVarCompensatorInjection](/_static/img/nad/nad_icons/svc.svg)            | [Static Var compensator](inv:powsyblcore:*:*#grid_model/network_subnetwork#static-var-compensator)   |
| ![unknownComponentInjection](/_static/img/nad/nad_icons/unknown-component.svg)  | Unknown component                                                                                    |


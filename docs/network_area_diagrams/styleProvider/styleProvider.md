# Style provider

The `StylelProvider` interface provides a way to customize the appearance of the network-area diagram.

Regarding nodes and bus nodes, the color and the blinking is fully customizable.
Regarding edges, the color, the width, the stroke and the blinking is also fully customizable

Currently, there are 2 implementations of the `StyleProvider`: the `NominalStyleProvider` and the `TopologicalStyleProvider`

## Common features: `AbstractStyleProvider` and `AbstractVoltageStyleProvider`

### Overvoltage and undervoltage voltage levels
TODO

### Overloaded lines
TODO

### Subnetworks

If the SvgParameters attribute `highlightSubnetworks` is set to `true`, subnetworks are highlighted in the network-area diagram:
![subnetworks](/_static/img/subnetworks.png)


# Style provider

The `StyleProvider` interface provides a way to customize the appearance of the network-area diagram.

Regarding nodes and bus nodes, the color and the blinking is fully customizable.
Regarding edges, the color, the width, the stroke and the blinking is also fully customizable

Currently, there are 2 implementations of the `StyleProvider`: the `NominalStyleProvider` and the `TopologicalStyleProvider`

## Common features
The common features are factorized in the abstract classes `AbstractStyleProvider` and `AbstractVoltageStyleProvider`.

### Overvoltage and undervoltage voltage levels
- Bus nodes which are over-voltage (meaning their voltage is above the voltage level high voltage limit) have their corresponding ring marked with class `nad-overvoltage`.
If default CSS is used, this leads to the corresponding ring stroke blinking in orange.
- Similarly, for bus nodes which are under-voltage (meaning their voltage is below the voltage level low voltage limit); they have their corresponding ring marked with class `nad-undervoltage`.
If default CSS is used, this leads to the corresponding ring stroke blinking in blue.

### Overloaded lines
Branches which are overloaded are marked with class `nad-overload`. If default CSS is used, this leads to a branch yellow-blinking.

### Subnetworks
If the SvgParameters attribute `highlightGraph` is set to `true`, the highlight classes for voltage level nodes and branches are based on the corresponding subnetwork.
With default CSS used, this leads to subnetworks being highlighted:
![subnetworks](/_static/img/subnetworks.png)

## NominalStyleProvider feature
The voltage level nodes are marked with a class depending on their nominal voltage, leading to one colour for each range of nominal voltages defined by the BaseVoltagesConfig. 

## TopologicalStyleProvider feature
The bus nodes of a voltage level are each marked with a class depending on the nominal voltage and on their bus index.
This leads to a colour shading for each range of nominal voltages defined by the BaseVoltagesConfig.
The text node of corresponding voltage level gives a legend for the colour shading, based on the selected given `LabelProvider` - the default being the voltage and angle. 

# SVG Parameters

The SLD `SvgParameters` is the configuration class to determine how to customize the SVG render of a single line diagram.
The way to integrate it is to set a `SvgParameters` value object to a `SldParameters` before drawing the SVG.

Use example:
```java
SvgParameters svgParams = new SvgParameters()
        .setUseName(true)
        .setLabelDiagonal(true)
        .setPowerValuePrecision(1)
        .setCssLocation(SvgParameters.CssLocation.EXTERNAL_IMPORTED);
SldParameters sldParams = new SldParameters()
        .setSvgParameters(svgParams);
SingleLineDiagram.draw(network, "VL1", Path.of("/tmp/vl1.svg"), sldParams);
```

## Parameters

### Value formatter
The `ValueFormatter` consumes some of the `SvgParameters`, related to the value precision and language to format electrical values to display.

#### powerValuePrecision
Default value: `0`

Number of decimals for active and reactive power display (in kV).

#### voltageValuePrecision
Default value: `1`

Number of decimals for voltage display (in kV).

#### currentValuePrecision
Default value: `0`

Number of decimals for current display (in A or kA, depending on `currentUnit` value).

#### angleValuePrecision
Default value: `1`

Number of decimals for phase angles display (in degrees °).

#### percentageValuePrecision
Default value: `0`

Number of decimals for percentage display (power report rate, etc..).

#### languageTag
Default value: `en`

Language tag to dertermine the `Locale` used for number formatting (decimal separator, etc...)

#### undefinedValueSymbol 
Default value: `"\u2014"` (em dash unicode for undefined value)

Symbol to display in place of a numerical value when that value is not defined (e.g. for undefined current, power, angle, percentage, voltage values for use cases: power flow not calculated, sensor missing).

## Units
### currentUnit
Default value: is empty

Unit suffix displayed after active power values. Examples: "A", "kA".

## activePowerUnit
Default value: is empty

Unit suffix displayed after current values. Examples: "MW", "kW".

## reactivePowerUnit
Default value: is empty

Unit suffix displayed after current values. Examples: "MVAR", "kVAR".

## prefixId

Default value: is empty

The prefix to set on SVG cells and elements ids.

## busInfoMargin
Default value: `0.0`

## feederInfosIntraMargin
Default value: `10`

## feederInfosOuterMargin
Default value: `20`

## feederInfoSymmetry
Default value: `false`

## busesLegendAdded
Default value: `false`

## useName
Default value: `false`

## angleLabelShift
Default value: `15.`

## labelCentered
Default value: `false`

## labelDiagonal
Default value: `false`

## tooltipEnabled
Default value: `false`

## svgWidthAndHeightAdded
Default value: `false`

## cssLocation
Default value: `CssLocation.INSERTED_IN_SVG`

Among the following list: `INSERTED_IN_SVG`, `EXTERNAL_IMPORTED`, `EXTERNAL_NO_IMPORT`

## avoidSVGComponentsDuplication
Default value: `false`

## diagramName
Default value: `null`

## drawStraightWires
Default value: `false`

## showGrid
Default value: `false`

## showInternalNodes
Default value: `false`

## displayEquipmentNodesLabel
Default value: ``

## displayConnectivityNodesId
Default value: ``

## unifyVoltageLevelColors
Default value: `false`


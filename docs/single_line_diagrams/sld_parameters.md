# Customizing single-line-diagrams

The `SldParameters` class allows users to customize their single-line diagrams.

## Customization options

- Customize the svg rendering : see the `SvgParameters` class.
- Customize the layout : see the `LayoutParameters` class.
- Choose the component library : see the `ComponentLibrary` class.
- Customize labels and decorators : see the `LabelProviderFactory` and `LabelProvider` interfaces.
- Highlight information on the diagram : see the `LabelProviderFactory` and `LabelProvider` interfaces.
- Choose the layout algorithm for voltage levels : see the `VoltageLevelLayoutFactoryCreator` interface.
- Choose the layout algorithm for substations : see the `SubstationLayoutFactory` interface.
- Choose the layout algorithm for multi-substation diagrams : see the `ZoneLayoutFactory` interface.


## How to use it

Default values are defined for each customization aspect.

Users only need to define want they want to customize. On the following example, a customized `ComponentLibrary` is set, as well as a customized `StyleProvider`.

```java
SldParameters sldParameters = new SldParameters()
        .setComponentLibrary(componentLibrary)
        .setStyleProviderFactory(styleProviderFactory);
```

The `sldParameters` object should be then passed as an attribute to one of the `SingleLineDiagram` `draw` function. For example:

```java
SingleLineDiagram.draw(network, voltageLevelOrSubstationId, pathToSvgFile, sldParameters);
```

# Layouts

A layout represents the way in which the elements of a graph are arranged.

It is possible to use your own graph layout implementation but there are also existing layouts in powsybl-diagram, ready to use.


## Layouts for voltage levels

### Existing implementations

#### PositionVoltageLevelLayout

This layout positions the different elements inside a voltage level according to the following process:

- [Clean the graph to have the expected patterns](graphRefiner.md)
- [Detect the cells (intern / extern / shunt)](cellDetector.md)
- Organize the cells into blocks
- Calculate real coordinates of busNode and blocks connected to busbars

##### The `PositionVoltageLevelLayoutFactoryParameters` class
TODO

##### The `PositionFinder` class
TODO

#### RandomVoltageLevelLayout

With this layout, the graph node coordinates are randomly fixed:
- Between 0 and `width` for the x coordinate;
- Between 0 and `height` for the y coordinate.
The `width` and `height` variables are provided by the user.

#### CgmesVoltageLevelLayout

With this layout, the elements of the graph are arranged according to the data present in the CGMES DL profile.

### Choosing a `VoltageLevelLayout`

The `voltageLevelLayoutFactoryCreator` attribute in the [`SldParameters`](../sld_parameters.md) class is the customization parameter to use to choose a specific `VoltageLevelLayout`.

The `VoltageLevelLayoutFactoryCreator` creates a `VoltageLevelLayoutFactory` which in turn creates a `VoltageLevelLayout`.

#### Choose a specific PositionVoltageLevelLayout

Static methods are available in the `VoltageLevelLayoutFactoryCreator` interface to help users manipulate those objects.

Some examples are shown below.

__Example 1__

PositionVoltageLevelLayout using default parameters:

```java
VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = VoltageLevelLayoutFactoryCreator.newPositionVoltageLevelLayoutFactoryCreator();
SldParameters sldParameters = new SldParameters().setVoltageLevelLayoutFactoryCreator(voltageLevelLayoutFactoryCreator);
```

__Example 2__

PositionVoltageLevelLayout with a chosen [`PositionFinder`](#the-positionfinder-class):

```java
VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = VoltageLevelLayoutFactoryCreator.newPositionVoltageLevelLayoutFactoryCreator(positionFinder);
SldParameters sldParameters = new SldParameters().setVoltageLevelLayoutFactoryCreator(voltageLevelLayoutFactoryCreator);
```

__Example 3__

PositionVoltageLevelLayout with chosen [`PositionVoltageLevelLayoutFactoryParameters`](#the-positionvoltagelevellayoutfactoryparameters-class):

```java
VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = VoltageLevelLayoutFactoryCreator.newPositionVoltageLevelLayoutFactoryCreator(positionVoltageLevelLayoutFactoryParameters);
SldParameters sldParameters = new SldParameters().setVoltageLevelLayoutFactoryCreator(voltageLevelLayoutFactoryCreator);
```

__Example 4__

PositionVoltageLevelLayout with a chosen [`PositionFinder`](#the-positionfinder-class) and chosen [`PositionVoltageLevelLayoutFactoryParameters`](#the-positionvoltagelevellayoutfactoryparameters-class):

```java
VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = VoltageLevelLayoutFactoryCreator.newPositionVoltageLevelLayoutFactoryCreator(positionFinder, positionVoltageLevelLayoutFactoryParameters);
SldParameters sldParameters = new SldParameters().setVoltageLevelLayoutFactoryCreator(voltageLevelLayoutFactoryCreator);
```

#### Use the `SmartVoltageLevelLayoutFactory`

The SmartVoltageLevelLayoutFactory picks the "best" `VoltageLevelLayout` according to the information available in the network.

There is also a static method in the `VoltageLevelLayoutFactoryCreator` interface to help users pick the `SmartVoltageLevelLayoutFactory`:

```java
VoltageLevelLayoutFactoryCreator voltageLevelLayoutFactoryCreator = VoltageLevelLayoutFactoryCreator.newSmartVoltageLevelLayoutFactoryCreator();
SldParameters sldParameters = new SldParameters().setVoltageLevelLayoutFactoryCreator(voltageLevelLayoutFactoryCreator);
```

## Layouts for substations
TODO

## Layouts for multi-substation graphs
TODO
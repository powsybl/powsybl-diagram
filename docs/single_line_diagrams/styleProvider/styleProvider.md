# Style provider

The `StyleProvider` interface provides a way to customize the appearance of the single-area diagram.

Currently, there are 2 implementations of the `StyleProvider`: the `BasicStyleProvider`, the `NominalVoltageStyleProvider` and the `TopologicalStyleProvider`

## Common features
The common features are factorized in the abstract classes `AbstractStyleProvider` and `AbstractVoltageStyleProvider`.

## TopologicalStyleProvider feature
The bus nodes of a voltage level are each marked with a class depending on the nominal voltage and on their bus index.
This leads to a colour shading for each range of nominal voltages defined by the BaseVoltagesConfig.
Supports highlighting of buses on hover if enabled.

![sld-highlight-buses](/_static/img/sld-highlight-buses.svg){class="forced-white-background"}
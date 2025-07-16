# Style provider

The `StyleProvider` interface provides a way to customize the appearance of the single-area diagram.

Currently, there are 2 implementations of the `StyleProvider`: the `BasicStyleProvider`, the `NominalVoltageStyleProvider` and the `TopologicalStyleProvider`

## Common features
The common features are factorized in the abstract classes `AbstractStyleProvider` and `AbstractVoltageStyleProvider`.

## TopologicalStyleProvider feature
The bus nodes of a voltage level are each marked with a class depending on the nominal voltage and on their bus index.
This leads to a colour shading for each range of nominal voltages defined by the BaseVoltagesConfig.

The `TopologicalStyleProvider` also supports highlighting of electrical buses on hover.
In order to enable this feature, you can customize your `SldParameters` by using the `BusHighlightStyleProviderFactory`:

```java
SldParameters sldParameters = new SldParameters().setStyleProviderFactory(new BusHighlightStyleProviderFactory());
```
If you don't want to use the provided `BusHighlightStyleProviderFactory` in your `SldParameters`, you could also create your own `StyleProviderFactory` and include the `TopologicalStyleProvider` with the `busHighlightOnHover` parameter set to `true` in the `StyleProvidersList`:

```java
public class YourCustomStyleProviderFactory implements StyleProviderFactory {
    @Override
    public StyleProvider create(Network network, SvgParameters svgParameters) {
        return new StyleProvidersList(new TopologicalStyleProvider(network, svgParameters, true), ...);
    }
}
```

![sld-highlight-buses](/_static/img/sld-highlight-buses.svg){class="forced-white-background"}
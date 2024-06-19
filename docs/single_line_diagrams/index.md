# Single line diagrams


```{toctree}
---
maxdepth: 2
hidden: true
---

layout/layout.md
```


The powsybl-single-line-diagram-core artifact provides features to generate customized single-line diagrams:
- Creation of single-line diagrams for given voltage levels, substations or zones in SVG format, for both node/breaker and bus/breaker topologies:
    - From an IIDM network: a graph is built from the input network and then written as a single-line diagram;
    - By directly providing the underlying graph to the writer.
- Diagram customization:
    - Several layout algorithms to generate the diagrams;
    - Many layout parameters to adjust the rendering;
    - Possible use of your own component library. Modification of the existing library is also an option;

Some extensions are also available. You may check the powsybl-single-line-diagram-cgmes-iidm-extensions artifact to force positions in the diagram for instance.

![sld-example](/_static/img/sld-example.svg)

## Examples

These examples show how to write a single-line diagram into an SVG file.

* Generate a single-line diagram for the voltage level `N` of the network `network`

```java
SingleLineDiagram.draw(network, "N", "/tmp/n.svg");
```

* Generate a single-line diagram for the substation `A` of the network `network`, with customized `SvgParameters`

```java
SldParameters sldParameters = new SldParameters().setSvgParameters(new SvgParameters().setUseName(true));
SingleLineDiagram.draw(network, "A", Path.of("/tmp/a.svg"), sldParameters);
```
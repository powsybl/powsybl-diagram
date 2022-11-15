# PowSyBl Diagram

[![Actions Status](https://github.com/powsybl/powsybl-diagram/workflows/CI/badge.svg)](https://github.com/powsybl/powsybl-diagram/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-diagram&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-diagram&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-diagram&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-diagram)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Slack](https://img.shields.io/badge/slack-powsybl-blueviolet.svg?logo=slack)](https://join.slack.com/t/powsybl/shared_invite/zt-rzvbuzjk-nxi0boim1RKPS5PjieI0rA)

PowSyBl (**Pow**er **Sy**stem **Bl**ocks) is an open source framework written in Java, that makes it easy to write complex
software for power systems’ simulations and analysis. Its modular approach allows developers to extend or customize its
features.

PowSyBl is part of the LF Energy Foundation, a project of The Linux Foundation that supports open source innovation projects
within the energy and electricity sectors.

<p align="center">
<img src="https://raw.githubusercontent.com/powsybl/powsybl-gse/main/gse-spi/src/main/resources/images/logo_lfe_powsybl.svg?sanitize=true" alt="PowSyBl Logo" width="50%"/>
</p>

Read more at https://www.powsybl.org!

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/main/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl-tsc@lists.lfenergy.org](mailto:powsybl-tsc@lists.lfenergy.org).

## PowSyBl vs PowSyBl Single Line Diagram

PowSyBl Single Line Diagram is a component build on top of the `Network` model available in the PowSyBl Core repository responsible 
for generating a [single line diagram](https://en.wikipedia.org/wiki/One-line_diagram).

The main features are:
 - Node/Breaker and bus/breaker topology.
 - [SVG](https://fr.wikipedia.org/wiki/Scalable_Vector_Graphics) diagram to be used in various front-end technologies.
 - Voltage level, substation and zone diagrams.
 - Highly customizable rendering using equipment component libraries, CSS and configurable labels (position and content).
 - Multiple layout modes: fully automatic, semi-automatic (using relative positions for busbar sections and feeders), CGMES DL.

![Diagram demo](.github/diagram-demo.svg)
*The example above corresponds to a CGMES file from the ENTSO-E sample files.*
*A guide to generate this diagram is available [here](https://www.powsybl.org/pages/documentation/developer/api_guide/single-line-diagram/svg-writing.html).*

## Getting started

To generate a SVG single line diagram from a voltage level, we first need to add a Maven dependency for the `Network` model 
and additionally for this example three other ones: two for the `Network` test case, one for simple logging capabilities:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-impl</artifactId>
    <version>4.10.0</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-test</artifactId>
    <version>4.10.0</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-config-test</artifactId>
    <version>4.10.0</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.22</version>
</dependency>
```

We can now load a node/breaker test `Network`:
```java
Network network = FictitiousSwitchFactory.create();
```

After adding the single line diagram core module dependency:
```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-single-line-diagram-core</artifactId>
    <version>2.13.0</version>
</dependency>
```

We can now generate a SVG for the voltage level `N` with the following simple unique code line:
```java
SingleLineDiagram.draw(network, "N", "/tmp/n.svg");
```

We obtain the SVG below.

![Diagram demo](.github/example_n.svg)

Note that a JSON file named `n_metadata.json` is also generated in the same folder, containing all the metadata needed to interact with the diagram.


## PowSyBl vs PowSyBl Network Area Diagram

<p align="center">
<img src="https://user-images.githubusercontent.com/66690739/158350044-36293484-0b0b-4cca-91fa-e8037d4b76bb.png?sanitize=true" alt="Diagram example" width="50%"/>
</p>

PowSyBl Network Area Diagram is a component build on top of the `Network` model available in the PowSyBl Core repository responsible for generating a concise diagram of the whole network or of a part of the network, showing in particular the interconnections between the different voltage levels.
A network area diagram emphasizes the electrical structure of the network, and may differ substantially from the network physical geography.
It displays the graph whose nodes are the network voltage levels, and whose edges are the lines and transformers between those voltage levels.

## Getting started
In order to generate a SVG from a given network, we need to add some Maven dependencies:
- `powsybl-network-area-diagram` for the network area diagram itself
- `powsybl-iidm-impl` for the network model
- `powsybl-config-test` and `powsybl-ieee-cdf-converter` to load the `Network` example
- `slf4j-simple` for simple logging capabilities

```xml
<properties>
    <powsybl.nad.version>0.6.0</powsybl.nad.version>
    <powsybl.core.version>4.10.0</powsybl.core.version>
    <slf4j.version>1.7.22</slf4j.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.powsybl</groupId>
        <artifactId>powsybl-network-area-diagram</artifactId>
        <version>${powsybl.nad.version}</version>
    </dependency>
    <dependency>
        <groupId>com.powsybl</groupId>
        <artifactId>powsybl-iidm-impl</artifactId>
        <version>${powsybl.core.version}</version>
    </dependency>
    <dependency>
        <groupId>com.powsybl</groupId>
        <artifactId>powsybl-config-test</artifactId>
        <version>${powsybl.core.version}</version>
    </dependency>
    <dependency>
        <groupId>com.powsybl</groupId>
        <artifactId>powsybl-ieee-cdf-converter</artifactId>
        <version>${powsybl.core.version}</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>${slf4j.version}</version>
    </dependency>
</dependencies>
```

Then we simply need to load the IEEE 30-bus example network and then generate the corresponding network area diagram SVG.
```java
Network network = IeeeCdfNetworkFactory.create30();
new NetworkAreaDiagram(network).draw(Path.of("/tmp/diagram.svg"));
```
We obtain the following SVG:

<p align="center">
<img src=".github/diagram_example.svg?sanitize=true" alt="Diagram IEEE30 network" width="65%"/>
</p>

If only part of the network is wanted, we can generate a partial graph of the network, by providing
- either a voltage level id and a depth,
- or a list of voltage level ids and a (unique) depth.

For instance let's generate the subgraph centered on voltage level `"VL25"` with a depth of `2`:

```java
new NetworkAreaDiagram(network, "VL25", 2).draw(Path.of("/tmp/partial_diagram_25.svg"));
```

This leads to following diagram:

<p align="center">
<img src=".github/partial_diagram_example_25.svg?sanitize=true" alt="Diagram IEEE30 partial network VL25" width="80%"/>
</p>

Now let's generate the subgraph with voltage levels at a maximum distance of 2 from `"VL1"` and `"VL25"`:

```java
new NetworkAreaDiagram(network, Arrays.asList("VL1", "VL25"), 2).draw(Path.of("/tmp/partial_diagram_1_25.svg"));
```

This gives us the diagram below. Note that nothing ensures that the parts displayed in resulting diagram are connected.
That is, the voltage levels between two voltage levels which are connected in the full graph are not necessarily drawn.

<p align="center">
<img src=".github/partial_diagram_example_1_25.svg?sanitize=true" alt="Diagram IEEE30 partial network VL1-VL25" width="65%"/>
</p>

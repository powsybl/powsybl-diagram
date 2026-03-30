# BSCluster

```{toctree}
---
maxdepth: 2
hidden: true
---

verticalBusSet.md
horizontalBusList.md
```

## Definition

`BSCluster` (`BusNode` Sets Cluster) is used by implementations of `PositionFinder`.

It is composed of two kinds of `BusNode` Sets that present a horizontal and a vertical view of the structure of a `VoltageLevel`:

- `List<VerticalBusSet> verticalBusSets` see [VerticalBusSet](verticalBusSet.md)
- `List<HorizontalBusList> horizontalBusLists` see [HorizontalBusList](horizontalBusList.md)

> **Important:**
> `VerticalBusSet` is a `Set` as it is important to have no duplicate `BusNodes`.
> On the opposite, it is possible to have duplicate `BusNodes` in the `HorizontalBusList`.
>
> The rules are as follows:
>
> - for `VerticalBusSet`, a `BusNode` may appear:
>   * in several `VerticalBusSet`,
>   * but only once in a `VerticalBusSet`;
> - for `HorizontalBusList`, a `BusNode` may appear:
>   * several times in a `HorizontalBusList`, in that case the occurrences shall have contiguous indexes,
>   * only in one single `HorizontalBusList`.

The goal is to merge all the `BSCluster` in the `VoltageLevel` into a single `BSCluster` having to this kind of pattern:

![BSClusterFinal](../../_static/img/sld/layout/BSClusterFinal.svg){align=center}

## Key methods

### Build

A `BSCluster` is initiated with one `VerticalBusSet` that:

- is put as the first element of the `verticalBusSets` list,
- initiates one `HorizontalBusList` for each of its `BusNode`

Each `PositionFinder` using `BSCluster` implementation provides a strategy to merge them to get a single `BSCluster`.

### Merge

The merge of two `BSCluster` is done by calling the `merge` method, thanks to the `HorizontalBusListsMerger` given.
Indeed, if the merging of the `verticalBusSets` is just a concatenation, the `horizontalBusLists` merging differs from one `PositionFinder` to another.

# Layout Parameters

Layout parameters are parameters that are common to all layouts.

## Parameters available

<h4 style="color:red">TODO: complete missing parameters</h4>

`threeWindingsTransformerFeederInfoMode`: Defines on which leg of the three-winding transformer power and current arrows are displayed.
Possible values are:
- `ONLY_OUTSIDE_VOLTAGE_LEVEL` (default): display on the two outer legs of the three-winding transformer.
- `ONLY_INSIDE_VOLTAGE_LEVEL`: display on the inner leg of the three-winding transformer.
- `FULL_3WT`: display on all three legs of the three-winding transformer.

To set, use the `setThreeWindingsTransformerFeederInfoMode` method.

| <div style="width:240px">![3wt arrows outside](../../_static/img/sld/layout/layoutParameters/threeWinding_arrow_display_outside.svg){class="forced-white-background"}</div> | <div style="width:190px">![3wt arrows inside](../../_static/img/sld/layout/layoutParameters/threeWinding_arrow_display_inside.svg){class="forced-white-background"}</div> | <div style="width:190px">![3wt arrows all](../../_static/img/sld/layout/layoutParameters/threeWinding_arrow_display_full.svg){class="forced-white-background"}</div> |
|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|                                                                    Default (ONLY_OUTSIDE_VOLTAGE_LEVEL)                                                                     |                                                                         ONLY_INSIDE_VOLTAGE_LEVEL                                                                         |                                                                               FULL_3WT                                                                               |

Note that adding arrows to the internal leg of the three-winding transformer will increase the height of all vertical feeders of that voltage level by the span of the arrows.

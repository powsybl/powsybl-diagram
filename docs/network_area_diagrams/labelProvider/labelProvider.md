# Label provider

The `LabelProvider` interface provides a way to customize labels.

## Edge information

## Voltage level description

## Bus description

## Voltage level details

### Default implementation

If `svgParameters.isVoltageLevelDetails()` returns `true`, then the production (~) and consumption (⌂) of each voltage level is displayed as text.
If there is no value (for production or consumption), the corresponding line in the text box is not displayed.

![production-consumption](/_static/img/nad/production-consumption.png)

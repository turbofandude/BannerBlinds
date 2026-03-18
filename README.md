# BannerBlinds
## Introduction
BannerBlinds exists to let players have what feels like toggleable blinds/curtains in their buildings. It works by detecting the bounds of blinds and if retracted, replicating banners vertically downward. If not retracted, it removes the banners until only one remains.

# Installation
Copy BannerBlinds-{version}.jar to your `plugins` folder within Paper.

## Usage
Simply right-click (place) with an empty hand on a banner hanging on a wall.

## Arrangement
Banners are considered continous blinds if they terminate on the same Y-value and are oriented the same way. Multiple colors can be used side by side, but *not* within a column, as the top-most color will be replicated down.

## Configuration
By default, BannerBlinds can be used by all players and have a maximum continous detection of 100 blocks in any direction. In `BannerBlinds/config.yml`, you can adjust this:

```yml
usePermission: true
maxWidth: 200
maxHeight: 200
```

If `usePermission` is set to `true`, players must have the `bannerblinds.use` permission to extend or retract them.

# Theatrical

[![](https://img.shields.io/discord/481830554447118371.svg?label=Join%20our%20Discord)](https://discord.gg/7qMs5d6)

Theatrical is a mod all about adding equipment from the live events industry including tungsten lights, LEDs, Moving Heads, Sound Systems, Rigging Equipment, Stage Setups, and Lighting Control Systems.

For downloads please check our [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/theatrical).

The mod is currently Work In Progress and is being developed by Rushmead with support from members of the community. Rushmead streams development of the mod often on his [twitch channel](https://twitch.tv/Rushmead).

Pull Requests are welcome and will be reviewed by Rushmead.

## Art-Net universes

The client config (`config/theatrical-client.yml`) supports `maxArtNetUniverses` (default: `512`, maximum: `32768`). This controls how many network universes can be added in the Art-Net configuration screen. Existing universe entries are not removed when the setting is lowered.

Network universe 1 maps to Art-Net Net 0, Sub-Net 0, Universe 0. The Art-Net `subnet` value in each universe entry is a continuous index: 0–15 use Net 0, 16–31 use Net 1, and so on. Each subnet has universe values 0–15. For example, network universe 257 maps to `subnet: 16`, `universe: 0` (Art-Net Net 1, Sub-Net 0, Universe 0). Set the sending console's Art-Net Net and Sub-Net to match.

## Contributors

* Rushmead
* bright_spark
* FreneticScribbler
* Dumaan089

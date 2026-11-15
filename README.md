# Create Fly Connector Patch

A companion mod that lets [Create Fly](https://modrinth.com/mod/create-fly) (Fabric) and its Fabric addons run on **NeoForge 26.2** through [Sinytra Connector](https://github.com/Sinytra/Connector).

It does not replace Create Fly or any addon. It runs alongside them and fixes the places where Fabric code breaks on NeoForge.

> **Made with AI.** The code, commit messages and this README were written by Claude (Anthropic's AI model, used through Claude Code) under my direction. I tested it in game. It is not reviewed or endorsed by the Create, Create Fly or Sinytra teams. Use at your own risk, and don't report issues it causes to them.

## Requirements

- NeoForge 26.2 (tested with 26.2.0.93)
- Sinytra Connector 3.1.0-beta.4+26.2
- Forgified Fabric API 0.161.0+26.2
- Create Fly 26.2-6.0.9-2

## Install

Put `createfly-connector-patch-1.0.0.jar` in your `mods` folder next to Connector and Create Fly. After updating the patch, delete `.cache/connector` in your instance folder so Connector re-transforms the mods.

## What it fixes

For all Fabric mods loaded through Connector:
- Server reload listeners added via mixin no longer hang world loading.
- Registries stay open while NeoForge mods are constructed, like during Fabric init (fixes "Registry is already frozen", e.g. Vanillin + Steam 'n' Rails).
- Modded creative tabs no longer crash vanilla's tab-position check.
- Restores the vanilla GUI picture-in-picture hook that Fabric mods inject into.

For Create Fly:
- Retargets or replaces its mixins that fail on NeoForge (block breaking, train HUD, entity section tracking, super glue and symmetry wand placement, sound/friction blocks, registry setup).
- Its bundled Flywheel no longer crashes on GL buffer types it doesn't know (Crankshaft + Distant Horizons).

## Status

- 26.2 only. There is no 26.3 build because Connector has no 26.3 release yet.
- The Flywheel GL buffer fix passes the mixin audit but hasn't been confirmed in game yet.
- Dedicated servers: Connector 3.1.0-beta.4 does not auto-patch Fabric mods on servers (an upstream bug), so server support is limited.

## Building

Place the jars listed in `gradle.properties` into `libs/` (not committed), then:

```
./gradlew :plugin:build
```

The output is `plugin/build/libs/createfly-connector-patch-1.0.0.jar`. `./gradlew :testenv:runClient` starts a dev client with Connector, Create Fly and the patch (extra mods go in `testenv/extra-mods/`).

## License

MIT

# Connector Compat Patch

A companion mod that fixes Fabric mods running on **NeoForge 26.2** through [Sinytra Connector](https://github.com/Sinytra/Connector), including [Create Fly](https://modrinth.com/mod/create-fly) and its Fabric addons.

It does not replace any mod. It runs alongside them and fixes the places where Fabric code breaks on NeoForge. The Create fixes only switch on when Create Fly is installed.

*Formerly "Create Fly Connector Patch".*

> **Made with AI.** The code, commit messages and this README were written by Claude (Anthropic's AI model, used through Claude Code) under my direction. I tested it in game. It is not reviewed or endorsed by the Create, Create Fly or Sinytra teams. Use at your own risk, and don't report issues it causes to them.

## Requirements

- NeoForge 26.2 (tested with 26.2.0.93)
- Sinytra Connector 3.1.0-beta.4+26.2
- Forgified Fabric API 0.161.0+26.2
- Optional: Create Fly 26.2-6.0.9-2 (for the Create fixes)

## Install

Put `connector-compat-patch-1.0.0.jar` in your `mods` folder next to Connector (remove the old `createfly-connector-patch` jar if you had it). After updating the patch, delete `.cache/connector` in your instance folder so Connector re-transforms the mods.

## What it fixes

For all Fabric mods loaded through Connector:
- Server reload listeners added via mixin no longer hang world loading.
- Registries stay open while NeoForge mods are constructed, like during Fabric init (fixes "Registry is already frozen", e.g. Vanillin + Steam 'n' Rails).
- Modded creative tabs no longer crash vanilla's tab-position check.
- Restores the vanilla GUI picture-in-picture hook that Fabric mods inject into.
- Faster world loading ("Preparing for world creation"): each mod jar's file list is read once instead of on every resource lookup. Connector bundles many mods as multi-jar packs, so this was most of the wait (2:42 → 0:54 in a ~125-mod pack, same recipes loaded).
- Adds Forge Config API Port's Fabric-side `ConfigurationScreen(String modId, Screen parent)` constructors to NeoForge's class, so Fabric mods' config screens don't crash at startup (e.g. Exposure).
- Loads COMMON/CLIENT configs that Fabric mods register from their entrypoints, which run after NeoForge has already loaded configs (fixes "Cannot get config value before config is loaded" on world load, e.g. Exposure).

- New built-in registries that Fabric mods create while NeoForge has the root registry locked get added the way NeoForge adds its own (e.g. WorldWeaver, Create Fly).
- Registries stay open while their own bake step runs, so Fabric code it triggers can still register (e.g. Better End's attributes).
- NeoForge's early registry lock no longer fails on Fabric content that's created but only registered once the Fabric mod starts (e.g. Better Nether's entities). Connector's lock after Fabric startup still checks.

For BCLib / WorldWeaver (Better End, Better Nether), only when installed:
- BCLib's compostables work with NeoForge's compostables data map.
- WorldWeaver's "world registries ready" event fires on NeoForge's world loader.
- Turns off WorldWeaver's data-generation-only model hook, which can't apply on NeoForge (never runs in game).

For Create Fly (only when it's installed):
- Retargets or replaces its mixins that fail on NeoForge (block breaking, train HUD, entity section tracking, super glue and symmetry wand placement, sound/friction blocks, registry setup).
- Its bundled Flywheel no longer crashes on GL buffer types it doesn't know (Crankshaft + Distant Horizons).

## Status

- 26.2 only. There is no 26.3 build because Connector has no 26.3 release yet.
- The Flywheel GL buffer fix passes the mixin audit but hasn't been confirmed in game yet.
- Not yet tested in a pack without Create Fly.
- Dedicated servers: Connector 3.1.0-beta.4 does not auto-patch Fabric mods on servers (an upstream bug), so server support is limited.

## Building

Place the jars listed in `gradle.properties` into `libs/` (not committed), then:

```
./gradlew :plugin:build
```

The output is `plugin/build/libs/connector-compat-patch-1.0.0.jar`. `./gradlew :testenv:runClient` starts a dev client with Connector, Create Fly and the patch (extra mods go in `testenv/extra-mods/`).

## License

MIT

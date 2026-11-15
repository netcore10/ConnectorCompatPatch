package net.netco.createflypatch;

import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;

/**
 * Fabric mods register Forge Config API Port configs from their Fabric entrypoint, which Connector runs after NeoForge has
 * already loaded all COMMON and CLIENT configs. NeoForge only loads late configs of type STARTUP, so those values stay
 * unloaded and throw "Cannot get config value before config is loaded" (e.g. Exposure's loot modifier on world load).
 * <p>
 * Right after the Fabric entrypoints have run, open every COMMON/CLIENT config NeoForge has already loaded the type of but not this config,
 * the same way NeoForge loads them. SERVER configs are loaded per world by NeoForge and need nothing.
 */
public final class LateConfigs {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFlyPatchMod.MOD_ID);

    @SuppressWarnings("unchecked")
    public static void load() {
        try {
            Field setsField = ConfigTracker.class.getDeclaredField("configSets");
            Method open = ConfigTracker.class.getDeclaredMethod("openConfig", ModConfig.class, Path.class, Path.class);
            setsField.setAccessible(true);
            open.setAccessible(true);
            EnumMap<ModConfig.Type, Set<ModConfig>> sets = (EnumMap<ModConfig.Type, Set<ModConfig>>) setsField.get(ConfigTracker.INSTANCE);

            for (ModConfig.Type type : new ModConfig.Type[]{ModConfig.Type.COMMON, ModConfig.Type.CLIENT}) {
                Set<ModConfig> ofType = sets.get(type);
                if (ofType.stream().noneMatch(c -> c.getLoadedConfig() != null)) continue; // not loaded on this side
                for (ModConfig config : ofType) {
                    if (config.getLoadedConfig() == null) {
                        open.invoke(null, config, FMLPaths.CONFIGDIR.get(), null);
                        LOGGER.info("Loaded late-registered {} config {}", type, config.getFileName());
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not load late-registered configs", e);
        }
    }
}

package net.netco.connectorcompat.plugin;

import net.neoforged.neoforgespi.locating.IDependencyLocator;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;

import java.util.List;

/**
 * Does nothing. FML only loads a mods/ jar as an early service (where Connector looks up TransformerPlugins)
 * if it provides one of FML's locator/window SPIs, so this marker makes the plugin visible to Connector.
 */
public class EarlyServiceMarker implements IDependencyLocator {
    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
    }
}

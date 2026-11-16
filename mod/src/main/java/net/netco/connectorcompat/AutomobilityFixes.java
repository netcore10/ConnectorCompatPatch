package net.netco.connectorcompat;

import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.List;

/** Client-only fixes for Automobility (port bugs, also present on Fabric). */
final class AutomobilityFixes {
    private AutomobilityFixes() {
    }

    static void register(IEventBus modBus) {
        // The port never registers a tint for grass off-road, so its greyscale texture stays grey; tint it like grass blocks
        modBus.addListener((RegisterColorHandlersEvent.BlockTintSources e) -> e.register(
            List.of(BlockTintSources.grassBlock()),
            BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("automobility", "grass_off_road"))));
        // Item definitions the port gets wrong (flat, skewed part icons in menus; untinted grass off-road item), shipped
        // as an always-on built-in pack above mod resources so nobody has to enable a resource pack
        modBus.addListener((AddPackFindersEvent e) -> {
            if (e.getPackType() == PackType.CLIENT_RESOURCES) {
                e.addPackFinders(Identifier.fromNamespaceAndPath(ConnectorCompatMod.MOD_ID, "resourcepacks/automobility_fixes"),
                    PackType.CLIENT_RESOURCES, Component.literal("Automobility fixes"), PackSource.BUILT_IN, true, Pack.Position.TOP);
            }
        });
    }
}

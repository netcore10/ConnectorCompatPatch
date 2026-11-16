package net.netco.connectorcompat;

import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

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
    }
}

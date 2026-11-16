package net.netco.connectorcompat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.netco.connectorcompat.create.CreateFixes;

@Mod(ConnectorCompatMod.MOD_ID)
public class ConnectorCompatMod {
    public static final String MOD_ID = "connector_compat_patch";

    public ConnectorCompatMod(IEventBus modBus) {
        RootRegistryQueue.register(modBus); // before CreateFixes: its NewRegistryEvent listener adds to the queue
        if (ModList.get().isLoaded("create")) {
            CreateFixes.register(modBus);
        }
        // Diagnostic only (testenv auditClient run): apply every pending mixin now, logging each failure instead of
        // crashing on the first one the moment its target class happens to load in game
        if (Boolean.getBoolean("connectorcompat.mixinAudit")) {
            modBus.addListener((net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent e) ->
                org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit());
        }
    }
}

package net.netco.createflypatch;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.trains.entity.CarriageEntityHandler;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(CreateFlyPatchMod.MOD_ID)
public class CreateFlyPatchMod {
    public static final String MOD_ID = "createfly_connector_patch";

    public CreateFlyPatchMod(IEventBus modBus) {
        modBus.addListener(CreateFlyPatchMod::registerCreateRegistries);
        modBus.addListener(CreateFlyPatchMod::buildSortedTypes);
        NeoForge.EVENT_BUS.addListener(CreateFlyPatchMod::onEntityEnterSection);
        // Diagnostic only (testenv auditClient run): apply every pending mixin now, logging each failure instead of
        // crashing on the first one the moment its target class happens to load in game
        if (Boolean.getBoolean("createflypatch.mixinAudit")) {
            modBus.addListener((net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent e) ->
                org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit());
        }
    }

    /** NeoForge's sanctioned point for new registries; replaces CreateRegistries' direct root-registry writes. */
    private static void registerCreateRegistries(NewRegistryEvent event) {
        CreateRegistries.register(); // forces CreateRegistries' static init if Create hasn't yet
        for (Registry<?> registry : PendingRegistries.drain()) {
            event.register(registry);
        }
    }

    /**
     * Replaces PersistentEntitySectionManagerCallbackMixin#onEnteringSection with NeoForge's own section-change event, which
     * passes the real old and new section (as upstream Create on NeoForge does). Server-side entity tracking only.
     */
    private static void onEntityEnterSection(EntityEvent.EnteringSection event) {
        if (event.getEntity().level() instanceof ServerLevel) {
            CarriageEntityHandler.onEntityEnterSection(event.getEntity(), event.getPackedOldPos(), event.getPackedNewPos());
        }
    }

    /** What Create Fly's BuiltInRegistriesMixin#afterFreeze did: sort types once their registries are populated. */
    private static void buildSortedTypes(FMLCommonSetupEvent event) {
        ArmInteractionPointType.register();
        FanProcessingTypeRegistry.register();
    }
}

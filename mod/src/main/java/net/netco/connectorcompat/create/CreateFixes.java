package net.netco.connectorcompat.create;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.trains.entity.CarriageEntityHandler;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

/** Create Fly fixes that run as NeoForge event listeners. Only registered when Create is installed. */
public final class CreateFixes {
    public static void register(IEventBus modBus) {
        modBus.addListener(CreateFixes::registerCreateRegistries);
        modBus.addListener(CreateFixes::buildSortedTypes);
        NeoForge.EVENT_BUS.addListener(CreateFixes::onEntityEnterSection);
    }

    /** Makes sure Create's registries exist by NewRegistryEvent; CreateRegistriesMixin hands them to it (RootRegistryQueue). */
    private static void registerCreateRegistries(NewRegistryEvent event) {
        CreateRegistries.register(); // forces CreateRegistries' static init if Create hasn't yet
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

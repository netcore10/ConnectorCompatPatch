package net.netco.createflypatch;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Registry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(CreateFlyPatchMod.MOD_ID)
public class CreateFlyPatchMod {
    public static final String MOD_ID = "createfly_connector_patch";

    public CreateFlyPatchMod(IEventBus modBus) {
        modBus.addListener(CreateFlyPatchMod::registerCreateRegistries);
        modBus.addListener(CreateFlyPatchMod::buildSortedTypes);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            CreateFlyPatchClient.init(modBus);
        }
    }

    /** NeoForge's sanctioned point for new registries; replaces CreateRegistries' direct root-registry writes. */
    private static void registerCreateRegistries(NewRegistryEvent event) {
        CreateRegistries.register(); // forces CreateRegistries' static init if Create hasn't yet
        for (Registry<?> registry : PendingRegistries.drain()) {
            event.register(registry);
        }
    }

    /** What Create Fly's BuiltInRegistriesMixin#afterFreeze did: sort types once their registries are populated. */
    private static void buildSortedTypes(FMLCommonSetupEvent event) {
        ArmInteractionPointType.register();
        FanProcessingTypeRegistry.register();
    }
}

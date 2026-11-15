package net.netco.createflypatch;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrieFinder;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.List;
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
        NeoForge.EVENT_BUS.addListener(CreateFlyPatchMod::addServerReloadListeners);
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

    /** Replaces ReloadableServerResourcesMixin#add: same four listeners, appended after vanilla's, through NeoForge's event. */
    private static void addServerReloadListeners(AddServerReloadListenersEvent event) {
        for (PreparableReloadListener listener : List.of(RecipeFinder.LISTENER, RecipeTrieFinder.LISTENER, BeltHelper.LISTENER, AllConfigs.LISTENER)) {
            event.addListener(((CreateResourceReloader) listener).getId(), listener);
        }
    }

    /** What Create Fly's BuiltInRegistriesMixin#afterFreeze did: sort types once their registries are populated. */
    private static void buildSortedTypes(FMLCommonSetupEvent event) {
        ArmInteractionPointType.register();
        FanProcessingTypeRegistry.register();
    }
}

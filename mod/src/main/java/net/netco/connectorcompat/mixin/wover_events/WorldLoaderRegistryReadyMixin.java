package net.netco.connectorcompat.mixin.wover_events;

import com.mojang.datafixers.util.Pair;
import de.ambertation.wover.events.api.types.OnRegistryReady;
import de.ambertation.wover.events.impl.WorldLifecycleImpl;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * WorldWeaver's WorldLoaderMixin#wover_captureWorldgenRegistries fires its "registry ready (LOADING)" event at the start of
 * WorldLoader's dimension-loading lambda. NeoForge's version of that lambda takes one of its Lists earlier, so the handler's
 * arguments no longer match and the mixin fails. Same logic, NeoForge's arguments.
 */
@Mixin(WorldLoader.class)
public abstract class WorldLoaderRegistryReadyMixin {
    @Inject(method = "lambda$load$1", at = @At("HEAD"))
    private static void connectorCompat$woverRegistryReady(
        List<?> a, CloseableResourceManager resources, Executor executor, List<?> b, Pair<?, ?> pair,
        WorldLoader.WorldDataSupplier<?> supplier, LayeredRegistryAccess<RegistryLayer> layers,
        WorldLoader.InitConfig config, Executor executor2, WorldLoader.ResultFactory<?, ?> factory, RegistryAccess.Frozen worldgen,
        CallbackInfoReturnable<CompletionStage<?>> cir
    ) {
        RegistryAccess.Frozen access = layers.replaceFrom(RegistryLayer.WORLDGEN, worldgen).getAccessForLoading(RegistryLayer.DIMENSIONS);
        WorldLifecycleImpl.WORLD_REGISTRY_READY.emit(access, OnRegistryReady.Stage.LOADING);
    }
}

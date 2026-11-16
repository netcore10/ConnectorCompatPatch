package net.netco.connectorcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.netco.connectorcompat.RootRegistryQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A Fabric mod creating a built-in registry (BuiltInRegistries#registerSimple etc.) while NeoForge has the root registry
 * frozen gets "Registry is already frozen" (e.g. WorldWeaver's wover:wover/biome_codec). Hand it to NewRegistryEvent
 * instead (see RootRegistryQueue). internalRegister ignores the returned holder.
 */
@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesQueueMixin {
    @WrapOperation(
        method = "internalRegister",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;")
    )
    private static Holder.Reference<?> connectorCompat$queueWhileRootFrozen(
        WritableRegistry<Object> root, ResourceKey<Object> key, Object registry, RegistrationInfo info, Operation<Holder.Reference<?>> original
    ) {
        if (((MappedRegistryAccessor) root).connectorCompat$isFrozen() && RootRegistryQueue.accepting()) {
            RootRegistryQueue.add((Registry<?>) registry);
            return null;
        }
        return original.call(root, key, registry, info);
    }
}

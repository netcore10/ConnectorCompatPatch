package net.netco.createflypatch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.netco.createflypatch.PendingRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create Fly creates its 13 registries by writing them straight into the root registry. On NeoForge the root is frozen
 * outside NewRegistryEvent, so queue them instead; CreateFlyPatchMod registers them in NewRegistryEvent (issue #369).
 * The returned holder is unused by Create's code.
 */
@Mixin(CreateRegistries.class)
public abstract class CreateRegistriesMixin {
    @WrapOperation(
        method = "register(Lnet/minecraft/resources/ResourceKey;Z)Lnet/minecraft/core/Registry;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;")
    )
    private static Holder.Reference<?> createFlyPatch$queueForNewRegistryEvent(
        WritableRegistry<?> root, ResourceKey<?> key, Object registry, RegistrationInfo info, Operation<Holder.Reference<?>> original
    ) {
        PendingRegistries.add((Registry<?>) registry);
        return null;
    }
}

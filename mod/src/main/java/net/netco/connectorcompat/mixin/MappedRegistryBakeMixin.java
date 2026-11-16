package net.netco.connectorcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.MappedRegistry;
import net.netco.connectorcompat.EarlyFreeze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

/**
 * NeoForge's bake callbacks run inside MappedRegistry#freeze, after the registry is marked frozen. Some reach Fabric mod
 * code that registers into that same registry from a static initializer. Example: the attribute bake validates
 * DefaultAttributes -> LivingEntity#createLivingAttributes -> Better End's mixin -> EndAttributes registers its attribute.
 * On Fabric that registration happens while registries are still open. Keep the registry open while its own bake callbacks
 * run; it's frozen again right after, before NeoForge snapshots the registries.
 */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryBakeMixin {
    @Shadow private boolean frozen;

    @WrapOperation(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void connectorCompat$openDuringBake(List<?> bakeCallbacks, Consumer<?> run, Operation<Void> original) {
        this.frozen = false;
        try {
            original.call(bakeCallbacks, run);
        } finally {
            this.frozen = true;
        }
    }

    /**
     * Fabric content can be created before its mod registers it: Better Nether's naga projectile entity type exists (as an
     * intrusive holder) once its class is loaded during NeoForge's registry setup, but is only registered by Better Nether's
     * entrypoint, which Launchpad runs later. Don't fail NeoForge's early freeze on that; Launchpad's freeze after the
     * entrypoints still checks, as Fabric does after mod init.
     */
    @WrapOperation(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private boolean connectorCompat$skipIntrusiveCheckBeforeEntrypoints(java.util.Map<?, ?> unregistered, Operation<Boolean> original) {
        return EarlyFreeze.active || original.call(unregistered);
    }
}

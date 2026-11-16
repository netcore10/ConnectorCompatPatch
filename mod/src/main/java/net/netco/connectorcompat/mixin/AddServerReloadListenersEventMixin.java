package net.netco.connectorcompat.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.resource.VanillaServerListeners;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

/**
 * Fabric mods (Create Fly, its addons, ...) add server reload listeners by appending to
 * ReloadableServerResources#listeners() with a mixin, which works on Fabric. NeoForge names every entry of that list and
 * throws for any listener that isn't a known vanilla one ("must go through the event"), which hangs world loading.
 * Give such listeners a stable id from their class instead, so they keep running where they were added, after vanilla's.
 */
@Mixin(AddServerReloadListenersEvent.class)
public abstract class AddServerReloadListenersEventMixin {
    @Inject(method = "lookupName", at = @At("HEAD"), cancellable = true)
    private static void connectorCompat$nameMixinAddedListener(PreparableReloadListener listener, CallbackInfoReturnable<Identifier> cir) {
        Class<? extends PreparableReloadListener> type = listener.getClass();
        if (VanillaServerListeners.getNameForClass(type) == null && !type.getPackageName().startsWith("net.minecraft")) {
            // Names must be unique per reload (NeoForge throws on duplicates); a mod can add several listeners of one class
            String path = type.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_")
                + "/" + Integer.toHexString(System.identityHashCode(listener));
            cir.setReturnValue(Identifier.fromNamespaceAndPath("fabric_mixin_listener", path));
        }
    }
}

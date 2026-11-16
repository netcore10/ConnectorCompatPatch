package net.netco.connectorcompat;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.netco.connectorcompat.mixin.MappedRegistryAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * On Fabric, a mod can add a new built-in registry whenever its classes initialize. NeoForge keeps the root registry frozen
 * and only accepts new registries through NewRegistryEvent. Registries Fabric mods create before that event (Create Fly's,
 * at construction) are queued and handed to it. Ones created once it has started (WorldWeaver's, during NeoForge's
 * data-pack registry step, which runs after the event has filled the root) are added the way NewRegistryEvent#fill does:
 * open the root, register, close it again. Either way they're in the root before NeoForge's RegisterEvents.
 */
public final class RootRegistryQueue {
    private static final List<Registry<?>> pending = new ArrayList<>();
    private static volatile boolean eventStarted;
    private static volatile boolean done;

    /** False once NeoForge has filled the root registry; later additions fail as they would without this patch. */
    public static boolean accepting() {
        return !done;
    }

    public static synchronized void add(Registry<?> registry) {
        if (eventStarted) {
            addToRoot(registry);
        } else {
            pending.add(registry);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addToRoot(Registry<?> registry) {
        MappedRegistryAccessor root = (MappedRegistryAccessor) BuiltInRegistries.REGISTRY;
        boolean wasFrozen = root.connectorCompat$isFrozen();
        root.connectorCompat$setFrozen(false);
        try {
            ((WritableRegistry) BuiltInRegistries.REGISTRY).register(registry.key(), registry, RegistrationInfo.BUILT_IN);
        } finally {
            root.connectorCompat$setFrozen(wasFrozen);
        }
    }

    static void register(IEventBus modBus) {
        modBus.addListener(EventPriority.HIGHEST, (NewRegistryEvent event) -> {
            synchronized (RootRegistryQueue.class) {
                eventStarted = true;
                pending.forEach(event::register);
                pending.clear();
            }
        });
        modBus.addListener((ModifyRegistriesEvent event) -> {
            eventStarted = false;
            done = true;
        });
    }
}

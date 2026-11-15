package net.netco.connectorcompat.create;

import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;

/** Registries Create Fly tried to add to the root registry directly; registered through NewRegistryEvent instead. */
public final class PendingRegistries {
    private static final List<Registry<?>> PENDING = new ArrayList<>();

    private PendingRegistries() {
    }

    public static synchronized void add(Registry<?> registry) {
        PENDING.add(registry);
    }

    static synchronized List<Registry<?>> drain() {
        List<Registry<?>> copy = List.copyOf(PENDING);
        PENDING.clear();
        return copy;
    }
}

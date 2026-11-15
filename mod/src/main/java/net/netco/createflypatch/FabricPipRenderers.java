package net.netco.createflypatch;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Called from GuiRenderer's constructor (inserted by GuiRendererHookRestorer). The builder is the one vanilla would have
 * created, already filled by any Fabric mod's {@code @WrapOperation} on {@code ImmutableMap.builder()}. Its renderer
 * instances are added to NeoForge's factory list, where Connector's GuiRenderer compat picks them out and runs them like
 * vanilla (one persistent instance per render state class).
 */
public final class FabricPipRenderers {
    private FabricPipRenderers() {
    }

    public static List<Object> merge(List<?> factories, ImmutableMap.Builder<?, ?> builder) {
        ImmutableMap<?, ?> renderers = builder.buildOrThrow();
        if (renderers.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Object> unchanged = (List<Object>) factories;
            return unchanged;
        }
        List<Object> merged = new ArrayList<>(factories);
        merged.addAll(renderers.values());
        return merged;
    }
}

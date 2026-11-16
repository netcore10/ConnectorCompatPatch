package net.netco.connectorcompat;

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
        List<Object> merged = new ArrayList<>(factories);
        merged.addAll(builder.buildOrThrow().values());
        return merged;
    }
}

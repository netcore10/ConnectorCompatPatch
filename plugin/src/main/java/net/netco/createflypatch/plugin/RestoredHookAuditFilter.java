package net.netco.createflypatch.plugin;

import net.neoforged.art.api.Transformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.sinytra.adapter.api.AuditTrail;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Runs after Connector's mixin patching. Adapter judges mixins against NeoForge's classes without this patch's
 * GuiRendererHookRestorer, so it rejects handlers that wrap the restored {@code ImmutableMap.builder()} call in
 * GuiRenderer's constructor. A rejected handler is left exactly as the mod wrote it, which is correct here because the call
 * exists again at runtime; only the audit entry (which would trip Connector's safeguard) is marked as expected.
 */
final class RestoredHookAuditFilter implements Transformer {
    private static final String GUI_RENDERER = "Lnet/minecraft/client/gui/render/GuiRenderer;";
    private static final String GUI_RENDERER_NAME = "net.minecraft.client.gui.render.GuiRenderer";
    private static final String IMMUTABLE_MAP_BUILDER = "Lcom/google/common/collect/ImmutableMap;builder()";

    private final AuditTrail auditTrail;

    RestoredHookAuditFilter(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Override
    public Collection<? extends Entry> getExtras() {
        for (Map.Entry<AuditTrail.Candidate, AuditTrail.Match> entry : this.auditTrail.getCandidates().entrySet()) {
            AuditTrail.Candidate candidate = entry.getKey();
            if (entry.getValue() == AuditTrail.Match.NONE
                && targetsGuiRenderer(candidate.classNode().invisibleAnnotations)
                // Adapter's working copy may already point elsewhere (e.g. createPools); the jar keeps the original
                && mentions(candidate.methodNode().visibleAnnotations, IMMUTABLE_MAP_BUILDER)) {
                entry.setValue(AuditTrail.Match.IGNORED);
            }
        }
        return List.of();
    }

    private static boolean targetsGuiRenderer(List<AnnotationNode> annotations) {
        return mentions(annotations, GUI_RENDERER) || mentions(annotations, GUI_RENDERER_NAME);
    }

    private static boolean mentions(List<AnnotationNode> annotations, String text) {
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (valuesMention(annotation.values, text)) return true;
        }
        return false;
    }

    private static boolean valuesMention(Object value, String text) {
        if (value instanceof List<?> list) {
            for (Object item : list) if (valuesMention(item, text)) return true;
            return false;
        }
        if (value instanceof AnnotationNode node) return valuesMention(node.values, text);
        if (value instanceof Type type) return type.getDescriptor().equals(text) || type.getClassName().equals(text);
        return value instanceof String s && s.contains(text);
    }
}

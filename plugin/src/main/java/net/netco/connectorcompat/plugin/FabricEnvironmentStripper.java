package net.netco.connectorcompat.plugin;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ClassProcessorIds;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Set;

/**
 * Fabric loader strips members annotated {@code @Environment(EnvType.CLIENT)} (and client-only interfaces declared with
 * {@code @EnvironmentInterface}) from classes on a dedicated server. Connector only does that for jars it transforms, and
 * on 26.2 many Fabric jars load untransformed, so their client-only members stay. Any that mention client classes in a
 * way the verifier must check (e.g. Farmer's Delight's {@code SkilletItem#getClientPlayerHack} returning a LocalPlayer as
 * a Player) then crash the server with NoClassDefFoundError the moment the class loads.
 * <p>
 * Do Fabric's stripping on dedicated servers. Clients are untouched.
 */
public class FabricEnvironmentStripper implements ClassProcessor {
    private static final ProcessorName ID = new ProcessorName("connector_compat_patch", "fabric_environment_stripper");
    private static final String ENVIRONMENT = "Lnet/fabricmc/api/Environment;";
    private static final String ENVIRONMENT_INTERFACE = "Lnet/fabricmc/api/EnvironmentInterface;";
    private static final String ENVIRONMENT_INTERFACES = "Lnet/fabricmc/api/EnvironmentInterfaces;";
    private static final String ENV_TYPE = "Lnet/fabricmc/api/EnvType;";

    private Boolean dedicatedServer;

    @Override
    public ProcessorName name() {
        return ID;
    }

    @Override
    public Set<ProcessorName> runsBefore() {
        return Set.of(ClassProcessorIds.MIXIN);
    }

    @Override
    public boolean handlesClass(SelectionContext context) {
        if (context.empty()) return false;
        if (dedicatedServer == null) dedicatedServer = FMLEnvironment.getDist().isDedicatedServer();
        if (!dedicatedServer) return false;
        String name = context.type().getInternalName();
        // Game and loader code never carries Fabric's annotations
        return !(name.startsWith("net/minecraft/") || name.startsWith("com/mojang/") || name.startsWith("net/neoforged/")
            || name.startsWith("java/") || name.startsWith("org/objectweb/") || name.startsWith("org/spongepowered/"));
    }

    @Override
    public ComputeFlags processClass(TransformationContext context) {
        ClassNode node = context.node();
        int fields = node.fields.size(), methods = node.methods.size(), interfaces = node.interfaces.size();
        node.fields.removeIf(f -> isClientOnly(f.invisibleAnnotations) || isClientOnly(f.visibleAnnotations));
        node.methods.removeIf(m -> isClientOnly(m.invisibleAnnotations) || isClientOnly(m.visibleAnnotations));
        stripClientInterfaces(node, node.invisibleAnnotations);
        stripClientInterfaces(node, node.visibleAnnotations);
        int removed = (fields - node.fields.size()) + (methods - node.methods.size()) + (interfaces - node.interfaces.size());
        if (removed == 0) return ComputeFlags.NO_REWRITE;
        context.audit("stripped", removed + " client-only members");
        return ComputeFlags.COMPUTE_FRAMES;
    }

    private static boolean isClientOnly(List<AnnotationNode> annotations) {
        if (annotations == null) return false;
        for (AnnotationNode a : annotations) {
            if (ENVIRONMENT.equals(a.desc) && isClient(value(a, "value"))) return true;
        }
        return false;
    }

    private static void stripClientInterfaces(ClassNode node, List<AnnotationNode> annotations) {
        if (annotations == null) return;
        for (AnnotationNode a : annotations) {
            if (ENVIRONMENT_INTERFACE.equals(a.desc)) {
                stripInterface(node, a);
            } else if (ENVIRONMENT_INTERFACES.equals(a.desc) && value(a, "value") instanceof List<?> list) {
                for (Object each : list) {
                    if (each instanceof AnnotationNode inner) stripInterface(node, inner);
                }
            }
        }
    }

    private static void stripInterface(ClassNode node, AnnotationNode annotation) {
        if (isClient(value(annotation, "value")) && value(annotation, "itf") instanceof Type itf) {
            node.interfaces.remove(itf.getInternalName());
        }
    }

    /** Enum annotation values are stored by ASM as {descriptor, constant name}. */
    private static boolean isClient(Object enumValue) {
        return enumValue instanceof String[] e && e.length == 2 && ENV_TYPE.equals(e[0]) && "CLIENT".equals(e[1]);
    }

    private static Object value(AnnotationNode annotation, String key) {
        if (annotation.values == null) return null;
        for (int i = 0; i + 1 < annotation.values.size(); i += 2) {
            if (key.equals(annotation.values.get(i))) return annotation.values.get(i + 1);
        }
        return null;
    }
}

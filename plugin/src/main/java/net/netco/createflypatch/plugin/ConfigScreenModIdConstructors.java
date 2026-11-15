package net.netco.createflypatch.plugin;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * On Fabric, Forge Config API Port ships NeoForge's ConfigurationScreen with a mod id String where NeoForge takes a
 * ModContainer, and Fabric mods call it that way (e.g. Exposure: {@code ConfigurationScreen::new} as its config screen
 * factory). On NeoForge that constructor doesn't exist, so the mod crashes during client init with NoSuchMethodError.
 * <p>
 * For every {@code ConfigurationScreen(ModContainer, ...)} constructor, add a {@code ConfigurationScreen(String, ...)} twin
 * that looks the mod up and delegates: {@code this(ModList.get().getModContainerById(modId).orElseThrow(), ...)}.
 */
public class ConfigScreenModIdConstructors implements ClassProcessor {
    private static final ProcessorName ID = new ProcessorName("createfly_connector_patch", "config_screen_mod_id");
    private static final String OWNER = "net/neoforged/neoforge/client/gui/ConfigurationScreen";
    private static final String MOD_CONTAINER = "Lnet/neoforged/fml/ModContainer;";

    @Override
    public ProcessorName name() {
        return ID;
    }

    @Override
    public Set<ProcessorName> runsBefore() {
        return Set.of(net.neoforged.neoforgespi.transformation.ClassProcessorIds.MIXIN);
    }

    @Override
    public boolean handlesClass(SelectionContext context) {
        return !context.empty() && OWNER.equals(context.type().getInternalName());
    }

    @Override
    public ComputeFlags processClass(TransformationContext context) {
        List<MethodNode> added = new ArrayList<>();
        for (MethodNode ctor : context.node().methods) {
            if (!ctor.name.equals("<init>") || !ctor.desc.startsWith("(" + MOD_CONTAINER)) continue;
            Type[] args = Type.getArgumentTypes(ctor.desc);
            String twinDesc = "(Ljava/lang/String;" + ctor.desc.substring(MOD_CONTAINER.length() + 1);
            if (context.node().methods.stream().anyMatch(m -> m.name.equals("<init>") && m.desc.equals(twinDesc))) continue;

            MethodNode twin = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", twinDesc, null, null);
            InsnList code = twin.instructions;
            code.add(new VarInsnNode(Opcodes.ALOAD, 0));
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/neoforged/fml/ModList", "get", "()Lnet/neoforged/fml/ModList;", false));
            code.add(new VarInsnNode(Opcodes.ALOAD, 1));
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/neoforged/fml/ModList", "getModContainerById", "(Ljava/lang/String;)Ljava/util/Optional;", false));
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Optional", "orElseThrow", "()Ljava/lang/Object;", false));
            code.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/neoforged/fml/ModContainer"));
            int slot = 2;
            for (int i = 1; i < args.length; i++) {
                code.add(new VarInsnNode(args[i].getOpcode(Opcodes.ILOAD), slot));
                slot += args[i].getSize();
            }
            code.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, OWNER, "<init>", ctor.desc, false));
            code.add(new InsnNode(Opcodes.RETURN));
            added.add(twin);
        }
        if (added.isEmpty()) return ComputeFlags.NO_REWRITE;
        context.node().methods.addAll(added);
        context.audit("added", added.size() + " mod-id String constructors");
        return ComputeFlags.COMPUTE_MAXS;
    }
}

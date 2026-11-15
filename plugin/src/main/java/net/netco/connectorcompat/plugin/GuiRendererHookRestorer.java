package net.netco.connectorcompat.plugin;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ClassProcessorIds;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

/**
 * Restores the vanilla hook point that Fabric mods use to add picture-in-picture GUI renderers.
 * <p>
 * Vanilla's GuiRenderer constructor builds its renderer map with {@code ImmutableMap.builder()}, and Fabric mods (Create Fly,
 * its addons, ...) {@code @WrapOperation} that call to put their own renderer instances. NeoForge replaced the whole block with
 * {@code PictureInPictureRendererPool.createPools(list)}, so the call those mixins look for is gone.
 * <p>
 * Before Mixin runs, this re-inserts {@code ImmutableMap.builder()} right before {@code createPools}, and passes the builder
 * (after any Fabric mixins have filled it) to FabricPipRenderers#merge, which adds the renderer instances to the list.
 * Connector's own GuiRenderer compat then runs them as single persistent instances, like vanilla, instead of NeoForge pools.
 */
public class GuiRendererHookRestorer implements ClassProcessor {
    private static final ProcessorName ID = new ProcessorName("connector_compat_patch", "gui_renderer_hook");
    private static final String GUI_RENDERER = "net.minecraft.client.gui.render.GuiRenderer";
    private static final String CTOR_DESC = "(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V";

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
        return !context.empty() && GUI_RENDERER.equals(context.type().getClassName());
    }

    @Override
    public ComputeFlags processClass(TransformationContext context) {
        for (MethodNode method : context.node().methods) {
            if (!method.name.equals("<init>") || !method.desc.equals(CTOR_DESC)) continue;
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC
                    && call.owner.equals("net/neoforged/neoforge/client/gui/PictureInPictureRendererPool")
                    && call.name.equals("createPools")) {
                    // stack: [list] -> [list, builder] -> [mergedList] -> createPools(mergedList)
                    InsnList inject = new InsnList();
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableMap", "builder",
                        "()Lcom/google/common/collect/ImmutableMap$Builder;", false));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/netco/connectorcompat/FabricPipRenderers", "merge",
                        "(Ljava/util/List;Lcom/google/common/collect/ImmutableMap$Builder;)Ljava/util/List;", false));
                    method.instructions.insertBefore(call, inject);
                    context.audit("restored", "ImmutableMap.builder() hook before createPools");
                    return ComputeFlags.COMPUTE_MAXS;
                }
            }
        }
        return ComputeFlags.NO_REWRITE;
    }
}

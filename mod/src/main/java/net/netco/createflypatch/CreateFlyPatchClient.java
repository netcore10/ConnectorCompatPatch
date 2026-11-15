package net.netco.createflypatch;

import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

/**
 * Replaces Create Fly's GuiRendererMixin#addRenderer. Vanilla builds GuiRenderer's picture-in-picture renderer map with
 * ImmutableMap.builder() in the constructor; NeoForge builds pools from RegisterPictureInPictureRenderersEvent instead.
 * Same 21 renderers, in the same order, as Create Fly 6.0.9-2's mixin.
 */
final class CreateFlyPatchClient {
    private CreateFlyPatchClient() {
    }

    static void init(IEventBus modBus) {
        modBus.addListener(CreateFlyPatchClient::registerPipRenderers);
    }

    private static void registerPipRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(ItemTransformRenderState.class, ItemTransformElementRenderer::new);
        event.register(BlockTransformRenderState.class, BlockTransformElementRenderer::new);
        event.register(EntityBlockRenderState.class, EntityBlockRenderer::new);
        event.register(PartialRenderState.class, PartialElementRenderer::new);
        event.register(BlazeBurnerRenderState.class, BlazeBurnerElementRenderer::new);
        event.register(PressBasinRenderState.class, PressBasinRenderer::new);
        event.register(PressRenderState.class, PressRenderer::new);
        event.register(MixingBasinRenderState.class, MixingBasinRenderer::new);
        event.register(BasinBlazeBurnerRenderState.class, BasinBlazeBurnerRenderer::new);
        event.register(MillstoneRenderState.class, MillstoneRenderer::new);
        event.register(SawRenderState.class, SawRenderer::new);
        event.register(CrushWheelRenderState.class, CrushWheelRenderer::new);
        event.register(DeployerRenderState.class, DeployerRenderer::new);
        event.register(ManualBlockRenderState.class, ManualBlockRenderer::new);
        event.register(SpoutRenderState.class, SpoutRenderer::new);
        event.register(CrafterRenderState.class, CrafterRenderer::new);
        event.register(DrainRenderState.class, DrainRenderer::new);
        event.register(SandPaperRenderState.class, SandPaperRenderer::new);
        event.register(TitleTextRenderState.class, TitleTextRenderer::new);
        event.register(SceneRenderState.class, SceneRenderer::new);
        event.register(FanRenderState.class, FanRenderer::new);
    }
}

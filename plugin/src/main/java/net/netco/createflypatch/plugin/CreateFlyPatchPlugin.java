package net.netco.createflypatch.plugin;

import org.sinytra.adapter.api.MethodPatch;
import org.sinytra.connector.transformer.api.PatchRegistrar;
import org.sinytra.connector.transformer.api.TransformerContext;
import org.sinytra.connector.transformer.api.TransformerPlugin;

import java.util.List;

/**
 * Fixes the Create Fly (Fabric) mixins that NeoForge's patches break and Connector's Adapter can't remap.
 * Retargets the ones whose handler still fits; disables the rest, which the nested NeoForge mod reimplements.
 * Patches are only registered while Connector transforms Create Fly itself (Fabric mod id "create").
 */
public class CreateFlyPatchPlugin implements TransformerPlugin {
    private static final String CREATE_FLY_ID = "create";

    private static final String GET_SOUND_TYPE = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;";
    private static final String DESTROY_BLOCK = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z";

    // NeoForge's lambda also captures its LeftClickBlock event
    private static final String NEO_START_DESTROY_LAMBDA = "lambda$startDestroyBlock$1(Lnet/minecraft/world/level/block/state/BlockState;"
        + "Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$LeftClickBlock;Lnet/minecraft/core/BlockPos;"
        + "Lnet/minecraft/core/Direction;I)Lnet/minecraft/network/protocol/Packet;";
    // NeoForge's addMainPass overload adds a Matrix4fc capture to the main-pass lambda
    private static final String NEO_MAIN_PASS_LAMBDA = "lambda$addMainPass$0(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
        + "Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/util/profiling/ProfilerFiller;"
        + "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/resource/ResourceHandle;"
        + "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/resource/ResourceHandle;"
        + "Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;)V";

    @Override
    public String name() {
        return "createfly_connector_patch";
    }

    @Override
    public void registerPatches(PatchRegistrar registrar, TransformerContext context) {
        if (!CREATE_FLY_ID.equals(context.candidateJar().modMetadata().getId())) {
            return;
        }
        registrar.add(List.of(
            // --- Retargeted: handler unchanged, only the target lambda's descriptor differs on NeoForge ---
            // MultiPlayerGameModeMixin#onLeftClick2 (clipboard left-click paste)
            MethodPatch.builder()
                .targetClass("net/minecraft/client/multiplayer/MultiPlayerGameMode")
                .targetMethod("lambda$startDestroyBlock$1")
                .targetInjectionPoint("INVOKE", DESTROY_BLOCK)
                .modifyTarget(NEO_START_DESTROY_LAMBDA)
                .build(),
            // LevelRendererMixin#flywheel$beforeSolid / flywheel$beforeTranslucent
            MethodPatch.builder()
                .targetClass("net/minecraft/client/renderer/LevelRenderer")
                .targetMethod("lambda$addMainPass$0")
                .modifyTarget(NEO_MAIN_PASS_LAMBDA)
                .build(),

            // --- Disabled: reimplemented natively in the nested mod ---
            // ServerPlayerGameModeMixin#removeBlock -> ServerPlayerGameModeBreakControlMixin
            MethodPatch.builder()
                .targetClass("net/minecraft/server/level/ServerPlayerGameMode")
                .targetMethod("destroyBlock")
                .targetInjectionPoint("INVOKE", "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z")
                .disable()
                .build(),
            // LevelEventHandlerMixin#getBreakSound and LivingEntityMixin#getBlockFallSound -> BlockSoundControlMixin
            MethodPatch.builder()
                .targetClass("net/minecraft/client/renderer/LevelEventHandler")
                .targetMethod("levelEvent")
                .targetInjectionPoint("INVOKE", GET_SOUND_TYPE)
                .disable()
                .build(),
            MethodPatch.builder()
                .targetClass("net/minecraft/world/entity/LivingEntity")
                .targetMethod("playBlockFallSound")
                .targetInjectionPoint("INVOKE", GET_SOUND_TYPE)
                .disable()
                .build(),
            // BuiltInRegistriesMixin#afterFreeze (issue #369): runs at the end of BuiltInRegistries.freeze(), which on NeoForge
            // happens at Bootstrap with the root registry already frozen, so CreateRegistries' new registries can't be added.
            // -> CreateRegistries are queued for NewRegistryEvent and the sorted caches built at common setup (CreateFlyPatchMod)
            MethodPatch.builder()
                .targetClass("net/minecraft/core/registries/BuiltInRegistries")
                .targetMethod("freeze")
                .targetInjectionPoint("TAIL", null)
                .disable()
                .build(),
            // HudMixin#renderMainHud: Adapter moves it into NeoForge's no-arg updateContextualBarRenderer(), where its
            // @Local graphics/deltaTracker args don't exist (fails at runtime) -> HudTrainOverlayMixin
            MethodPatch.builder()
                .targetClass("net/minecraft/client/gui/Hud")
                .targetMethod("extractHotbarAndDecorations")
                .targetInjectionPoint("INVOKE", "Lnet/minecraft/client/gui/Hud;nextContextualInfoState()Lnet/minecraft/client/gui/Hud$ContextualInfo;")
                .disable()
                .build(),
            // ReloadableServerResourcesMixin#add: appends Create's server reload listeners to vanilla's list, which NeoForge
            // rejects ("must go through the event"), hanging world creation -> AddServerReloadListenersEvent in CreateFlyPatchMod
            MethodPatch.builder()
                .targetClass("net/minecraft/server/ReloadableServerResources")
                .targetMethod("listeners")
                .targetInjectionPoint("RETURN", null)
                .disable()
                .build(),
            // GuiRendererMixin#addRenderer -> RegisterPictureInPictureRenderersEvent in CreateFlyPatchClient
            MethodPatch.builder()
                .targetClass("net/minecraft/client/gui/render/GuiRenderer")
                .targetMethod("<init>")
                .targetInjectionPoint("INVOKE", "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;")
                .disable()
                .build()
        ));
    }
}

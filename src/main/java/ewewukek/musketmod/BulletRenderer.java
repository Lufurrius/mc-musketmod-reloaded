package ewewukek.musketmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class BulletRenderer extends EntityRenderer<BulletEntity, BulletRenderer.BulletRenderState> {
    public static final Identifier TEXTURE = MusketMod.resource("textures/entity/bullet.png");
    public static final Identifier TEXTURE_FIRE = MusketMod.resource("textures/entity/bullet_fire.png");

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BulletRenderState createRenderState() {
        return new BulletRenderState();
    }

    @Override
    public void extractRenderState(BulletEntity bullet, BulletRenderState state, float partialTick) {
        super.extractRenderState(bullet, state, partialTick);
        state.isFirstTick = bullet.isFirstTick();
        state.pelletCount = bullet.pelletCount();
        state.isOnFire = bullet.isOnFire();
    }

    @Override
    public void submit(BulletRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.isFirstTick) return;

        int light = state.lightCoords;

        poseStack.pushPose();

        if (state.pelletCount == 1) {
            poseStack.scale(0.1f, 0.1f, 0.1f);
        } else {
            poseStack.scale(state.isOnFire ? 0.075f : 0.05f, 0.05f, 0.05f);
        }

        // billboarding - use camera rotation from render state
        poseStack.mulPose(cameraRenderState.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        Identifier texture = state.isOnFire ? TEXTURE_FIRE : TEXTURE;
        
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(texture), (pose, builder) -> {
            addVertex(builder, pose, -1, -1, 0, 0, 1, 0, 0, 1, light);
            addVertex(builder, pose,  1, -1, 0, 1, 1, 0, 0, 1, light);
            addVertex(builder, pose,  1,  1, 0, 1, 0, 0, 0, 1, light);
            addVertex(builder, pose, -1,  1, 0, 0, 0, 0, 0, 1, light);
        });

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
    }

    void addVertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        builder.addVertex(pose, x, y, z)
               .setColor(255, 255, 255, 255)
               .setUv(u, v)
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(light)
               .setNormal(pose, nx, ny, nz);
    }

    public static class BulletRenderState extends EntityRenderState {
        public boolean isFirstTick;
        public int pelletCount;
        public boolean isOnFire;
    }
}

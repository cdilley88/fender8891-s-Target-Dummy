package com.fender8891.targetdummy.client;

import com.fender8891.targetdummy.entity.TargetDummy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class TargetDummyRenderer extends HumanoidMobRenderer<TargetDummy, HumanoidModel<TargetDummy>> {
    private final net.minecraft.client.gui.Font dummyFont;
    private static final ResourceLocation STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    public TargetDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.dummyFont = context.getFont();
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override public ResourceLocation getTextureLocation(TargetDummy entity) { return STEVE; }

    @Override
    public void render(TargetDummy entity, float yaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        if (!entity.isInfoCardEnabled() || entity.getModelMode() != com.fender8891.targetdummy.entity.DummyMobCatalog.IMMORTAL_STEVE) return;
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 0.85D, 0.0D);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.032F, -0.032F, 0.032F);
        Matrix4f matrix = poseStack.last().pose();
        drawCardLine("  TARGET DUMMY  ", -32.0F, 0xFF5DEAF5, matrix, buffer, light);
        float healthRatio = entity.getHealth() / entity.getMaxHealth();
        int healthColor = healthRatio > 0.6F ? 0xFF62E889 : healthRatio > 0.25F ? 0xFFFFC857 : 0xFFFF5C68;
        String health = String.format("  HP  [%s]  %.0f/%.0f  ",
                makeBar(healthRatio, 10), entity.getHealth(), entity.getMaxHealth());
        drawCardLine(health, -21.0F, healthColor, matrix, buffer, light);

        int armor = entity.getArmorValue();
        String armorStatus = String.format("  ARMOR  [%s]  %d  ", makeBar(armor / 20.0F, 10), armor);
        drawCardLine(armorStatus, -10.0F, 0xFF67A9FF, matrix, buffer, light);

        if (entity.getDamageTicks() > 0 && entity.getLastDamage() > 0.0F) {
            drawCardLine(String.format("  LAST HIT  -%.1f  ", entity.getLastDamage()), 1.0F,
                    0xFFFF6573, matrix, buffer, light);
        } else {
            drawCardLine("  LAST HIT  --  ", 1.0F, 0xFF8D98A6, matrix, buffer, light);
        }
        poseStack.popPose();
    }

    private String makeBar(float ratio, int length) {
        int filled = Math.round(Math.max(0.0F, Math.min(1.0F, ratio)) * length);
        return "|".repeat(filled) + ".".repeat(length - filled);
    }

    private void drawCardLine(String text, float y, int color, Matrix4f matrix,
            MultiBufferSource buffer, int light) {
        float x = -dummyFont.width(text) / 2.0F;
        dummyFont.drawInBatch(text, x, y, 0x55FFFFFF, false, matrix, buffer,
                net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, 0xF0000000, light);
        dummyFont.drawInBatch(text, x, y, color, false, matrix, buffer,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, light);
    }
}





package com.fender8891.targetdummy.client;

import com.fender8891.targetdummy.world.TestMobEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

public final class TestMobInfoCardRenderer {
    private static final Map<Integer, Float> LAST_HEALTH = new HashMap<>();
    private static final Map<Integer, Float> LAST_DAMAGE = new HashMap<>();
    private static final Map<Integer, Integer> DAMAGE_EXPIRES = new HashMap<>();

    private TestMobInfoCardRenderer() {
    }

    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!TestMobEvents.isClientVisibleTestMob(entity) || !TestMobEvents.isCardEnabled(entity)) return;

        float previous = LAST_HEALTH.getOrDefault(entity.getId(), entity.getHealth());
        if (entity.getHealth() < previous) {
            LAST_DAMAGE.put(entity.getId(), previous - entity.getHealth());
            DAMAGE_EXPIRES.put(entity.getId(), entity.tickCount + 30);
        }
        LAST_HEALTH.put(entity.getId(), entity.getHealth());

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 0.85D, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.032F, -0.032F, 0.032F);
        Matrix4f matrix = poseStack.last().pose();

        drawLine(font, "  " + entity.getType().getDescription().getString().toUpperCase() + "  ",
                -32.0F, 0xFF5DEAF5, matrix, buffer, light);
        float healthRatio = entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
        int healthColor = healthRatio > 0.6F ? 0xFF62E889 : healthRatio > 0.25F ? 0xFFFFC857 : 0xFFFF5C68;
        drawLine(font, String.format("  HP  [%s]  %.0f/%.0f  ", makeBar(healthRatio, 10),
                entity.getHealth(), entity.getMaxHealth()), -21.0F, healthColor, matrix, buffer, light);
        int armor = entity.getArmorValue();
        drawLine(font, String.format("  ARMOR  [%s]  %d  ", makeBar(armor / 20.0F, 10), armor),
                -10.0F, 0xFF67A9FF, matrix, buffer, light);
        if (DAMAGE_EXPIRES.getOrDefault(entity.getId(), 0) > entity.tickCount) {
            drawLine(font, String.format("  LAST HIT  -%.1f  ", LAST_DAMAGE.getOrDefault(entity.getId(), 0.0F)),
                    1.0F, 0xFFFF6573, matrix, buffer, light);
        } else {
            drawLine(font, "  LAST HIT  --  ", 1.0F, 0xFF8D98A6, matrix, buffer, light);
        }
        poseStack.popPose();
    }

    private static String makeBar(float ratio, int length) {
        int filled = Math.round(Math.max(0.0F, Math.min(1.0F, ratio)) * length);
        return "|".repeat(filled) + ".".repeat(length - filled);
    }

    private static void drawLine(Font font, String text, float y, int color, Matrix4f matrix,
            MultiBufferSource buffer, int light) {
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, y, 0x55FFFFFF, false, matrix, buffer,
                Font.DisplayMode.SEE_THROUGH, 0xF0000000, light);
        font.drawInBatch(text, x, y, color, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, light);
    }
}

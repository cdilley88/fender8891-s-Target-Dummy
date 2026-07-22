package com.fender8891.targetdummy;

import com.fender8891.targetdummy.client.TargetDummyRenderer;
import com.fender8891.targetdummy.client.TargetDummyScreen;
import com.fender8891.targetdummy.client.TestMobInfoCardRenderer;
import com.fender8891.targetdummy.registry.ModRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = TargetDummyMod.MOD_ID, dist = Dist.CLIENT)
public final class TargetDummyModClient {
    public TargetDummyModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerMenuScreens);
        NeoForge.EVENT_BUS.addListener(TestMobInfoCardRenderer::onRenderLiving);
    }
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModRegistries.TARGET_DUMMY_ENTITY.get(), TargetDummyRenderer::new);
    }
    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistries.TARGET_DUMMY_MENU.get(), TargetDummyScreen::new);
    }
}
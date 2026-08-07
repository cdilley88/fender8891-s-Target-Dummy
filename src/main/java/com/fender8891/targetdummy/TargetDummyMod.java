package com.fender8891.targetdummy;

import com.fender8891.targetdummy.entity.TargetDummy;
import com.fender8891.targetdummy.registry.ModRegistries;
import com.fender8891.targetdummy.registry.TargetDummyNetworking;
import com.fender8891.targetdummy.world.TestMobEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(TargetDummyMod.MOD_ID)
public final class TargetDummyMod {
    public static final String MOD_ID = "fender8891_target_dummy";
    public TargetDummyMod(IEventBus modEventBus) {
        ModRegistries.ENTITY_TYPES.register(modEventBus);
        ModRegistries.ITEMS.register(modEventBus);
        ModRegistries.MENUS.register(modEventBus);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::addCreativeTabContents);
        modEventBus.addListener(TargetDummyNetworking::registerPayloads);
        NeoForge.EVENT_BUS.addListener(TestMobEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(TestMobEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(TestMobEvents::onMobDespawn);
        NeoForge.EVENT_BUS.addListener(TestMobEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(TestMobEvents::onLivingExperienceDrop);
    }
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModRegistries.TARGET_DUMMY_ENTITY.get(), TargetDummy.createAttributes().build());
    }
    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(ModRegistries.TARGET_DUMMY_ITEM.get());
    }
}
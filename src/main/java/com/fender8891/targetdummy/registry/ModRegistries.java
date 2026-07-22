package com.fender8891.targetdummy.registry;

import com.fender8891.targetdummy.TargetDummyMod;
import com.fender8891.targetdummy.entity.TargetDummy;
import com.fender8891.targetdummy.inventory.TargetDummyMenu;
import com.fender8891.targetdummy.item.TargetDummyItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistries {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TargetDummyMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TargetDummyMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TargetDummyMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TargetDummy>> TARGET_DUMMY_ENTITY =
            ENTITY_TYPES.register("target_dummy", () -> EntityType.Builder.of(TargetDummy::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(32)
                    .build(TargetDummyMod.MOD_ID + ":target_dummy"));

    public static final DeferredItem<TargetDummyItem> TARGET_DUMMY_ITEM = ITEMS.registerItem(
            "target_dummy", TargetDummyItem::new, new Item.Properties().stacksTo(16));

    public static final DeferredHolder<MenuType<?>, MenuType<TargetDummyMenu>> TARGET_DUMMY_MENU =
            MENUS.register("target_dummy", () -> IMenuTypeExtension.create(TargetDummyMenu::fromNetwork));

    private ModRegistries() {
    }
}
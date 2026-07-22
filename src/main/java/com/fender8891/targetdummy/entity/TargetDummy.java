package com.fender8891.targetdummy.entity;

import com.fender8891.targetdummy.inventory.TargetDummyMenu;
import com.fender8891.targetdummy.item.PresetArmorData;
import com.fender8891.targetdummy.registry.ModRegistries;
import com.fender8891.targetdummy.world.TestMobEvents;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TargetDummy extends PathfinderMob implements MenuProvider {
    public static final int CUSTOM_LOADOUT = 0;
    public static final int FIRST_PRESET = 1;
    public static final int LAST_PRESET = 6;
    private static final int RESPAWN_DELAY_TICKS = 20;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] LOADOUT_NAMES = {
            "CUSTOM", "LEATHER", "CHAIN", "IRON", "GOLD", "DIAMOND", "NETHERITE"
    };

    private static final EntityDataAccessor<Float> LAST_DAMAGE = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DAMAGE_TICKS = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INFO_CARD_ENABLED = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LOADOUT_INDEX = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MODEL_MODE = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> PASSIVE_MOB = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> HOSTILE_MOB = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.STRING);

    private final NonNullList<ItemStack> customArmor = NonNullList.withSize(4, ItemStack.EMPTY);
    @Nullable private UUID proxyUuid;
    private int respawnTicks;
    private int missingProxyTicks;

    public TargetDummy(EntityType<? extends TargetDummy> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override protected void registerGoals() {}
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) {}
    @Override public void push(double x, double y, double z) {}
    @Override public boolean isPickable() { return getModelMode() == DummyMobCatalog.IMMORTAL_STEVE && super.isPickable(); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAST_DAMAGE, 0.0F);
        builder.define(DAMAGE_TICKS, 0);
        builder.define(INFO_CARD_ENABLED, true);
        builder.define(LOADOUT_INDEX, CUSTOM_LOADOUT);
        builder.define(MODEL_MODE, DummyMobCatalog.IMMORTAL_STEVE);
        builder.define(PASSIVE_MOB, DummyMobCatalog.defaultSelection(DummyMobCatalog.PASSIVE_MOBS));
        builder.define(HOSTILE_MOB, DummyMobCatalog.defaultSelection(DummyMobCatalog.HOSTILE_MOBS));
    }

    @Override
    public void tick() {
        super.tick();
        double verticalMotion = getModelMode() == DummyMobCatalog.IMMORTAL_STEVE ? getDeltaMovement().y : 0.0D;
        setDeltaMovement(0.0D, verticalMotion, 0.0D);
        if (level().isClientSide()) return;
        if (entityData.get(DAMAGE_TICKS) > 0) entityData.set(DAMAGE_TICKS, entityData.get(DAMAGE_TICKS) - 1);
        if (hasPresetLoadout()) maintainPresetArmor();
        if (getModelMode() == DummyMobCatalog.IMMORTAL_STEVE) return;
        maintainTestMob();
    }

    private void maintainTestMob() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Entity current = proxyUuid == null ? null : serverLevel.getEntity(proxyUuid);
        if (current instanceof Mob mob) {
            missingProxyTicks = 0;
            if (!mob.isAlive()) {
                proxyUuid = null;
                respawnTicks = RESPAWN_DELAY_TICKS;
                return;
            }
            mob.setNoAi(true);
            return;
        }
        if (proxyUuid != null && missingProxyTicks++ < RESPAWN_DELAY_TICKS) return;
        proxyUuid = null;
        missingProxyTicks = 0;
        if (respawnTicks > 0) {
            respawnTicks--;
            return;
        }
        spawnTestMob(serverLevel);
    }

    private void spawnTestMob(ServerLevel level) {
        ResourceLocation id = ResourceLocation.tryParse(getSelectedMobId());
        if (id == null) return;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        Entity created = type.create(level);
        if (!(created instanceof Mob mob)) return;
        mob.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setCanPickUpLoot(false);
        mob.getPersistentData().putUUID(TestMobEvents.CONTROLLER_KEY, getUUID());
        updateProxyCardName(mob);
        if (level.addFreshEntity(mob)) proxyUuid = mob.getUUID();
    }

    private void updateProxyCardName(Entity proxy) {
        String marker = isInfoCardEnabled() ? TestMobEvents.CARD_ON_MARKER : TestMobEvents.CARD_OFF_MARKER;
        proxy.setCustomName(proxy.getType().getDescription().copy().withStyle(style -> style.withInsertion(marker)));
        proxy.setCustomNameVisible(false);
    }

    private void discardProxy() {
        if (proxyUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity proxy = serverLevel.getEntity(proxyUuid);
            if (proxy != null) proxy.discard();
        }
        proxyUuid = null;
        respawnTicks = 0;
        missingProxyTicks = 0;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float before = getHealth();
        super.actuallyHurt(source, amount);
        float dealt = Math.max(0.0F, before - getHealth());
        entityData.set(LAST_DAMAGE, dealt);
        entityData.set(DAMAGE_TICKS, 30);
        if (getHealth() <= 0.0F) setHealth(getMaxHealth());
    }

    @Override public void die(DamageSource source) {
        setHealth(getMaxHealth());
        entityData.set(DAMAGE_TICKS, 30);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) interactThroughProxy(serverPlayer);
        return InteractionResult.CONSUME;
    }

    public void interactThroughProxy(ServerPlayer player) {
        if (player.isShiftKeyDown()) {
            packUp();
        } else {
            player.openMenu(this, buffer -> buffer.writeVarInt(getId()));
        }
    }

    private void packUp() {
        discardProxy();
        if (hasPresetLoadout()) setLoadoutIndex(CUSTOM_LOADOUT);
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)) {
            ItemStack stack = getItemBySlot(slot);
            if (!stack.isEmpty()) spawnAtLocation(stack.copy());
            setItemSlot(slot, ItemStack.EMPTY);
        }
        spawnAtLocation(new ItemStack(ModRegistries.TARGET_DUMMY_ITEM.get()));
        discard();
    }

    public float getLastDamage() { return entityData.get(LAST_DAMAGE); }
    public int getDamageTicks() { return entityData.get(DAMAGE_TICKS); }
    public boolean isInfoCardEnabled() { return entityData.get(INFO_CARD_ENABLED); }
    public void setInfoCardEnabled(boolean enabled) {
        entityData.set(INFO_CARD_ENABLED, enabled);
        if (proxyUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity proxy = serverLevel.getEntity(proxyUuid);
            if (proxy != null) updateProxyCardName(proxy);
        }
    }
    public int getLoadoutIndex() { return entityData.get(LOADOUT_INDEX); }
    public boolean hasPresetLoadout() { return getLoadoutIndex() != CUSTOM_LOADOUT; }
    public String getLoadoutName() { return loadoutName(getLoadoutIndex()); }
    public int getModelMode() { return entityData.get(MODEL_MODE); }
    public String getModelModeName() { return DummyMobCatalog.modeName(getModelMode()); }
    public String getSelectedMobId() {
        return getModelMode() == DummyMobCatalog.HOSTILE_MOBS ? entityData.get(HOSTILE_MOB) : entityData.get(PASSIVE_MOB);
    }

    public void setModelMode(int requestedMode) {
        int next = Math.max(DummyMobCatalog.IMMORTAL_STEVE, Math.min(DummyMobCatalog.HOSTILE_MOBS, requestedMode));
        if (next == getModelMode()) return;
        discardProxy();
        entityData.set(MODEL_MODE, next);
        boolean steve = next == DummyMobCatalog.IMMORTAL_STEVE;
        setInvisible(!steve);
        setInvulnerable(!steve);
        setNoGravity(!steve);
        noPhysics = !steve;
        if (steve) setHealth(getMaxHealth());
    }

    public void setSelectedMob(String mobId) {
        int mode = getModelMode();
        if (!DummyMobCatalog.isValidSelection(mode, mobId)) return;
        if (mode == DummyMobCatalog.PASSIVE_MOBS) entityData.set(PASSIVE_MOB, mobId);
        else if (mode == DummyMobCatalog.HOSTILE_MOBS) entityData.set(HOSTILE_MOB, mobId);
        else return;
        discardProxy();
    }

    public static String loadoutName(int index) {
        return LOADOUT_NAMES[Math.max(CUSTOM_LOADOUT, Math.min(LAST_PRESET, index))];
    }

    public void setLoadoutIndex(int requestedIndex) {
        int next = Math.max(CUSTOM_LOADOUT, Math.min(LAST_PRESET, requestedIndex));
        int current = getLoadoutIndex();
        if (next == current) return;
        if (current == CUSTOM_LOADOUT && next != CUSTOM_LOADOUT) snapshotCustomArmor();
        entityData.set(LOADOUT_INDEX, next);
        if (next == CUSTOM_LOADOUT) restoreCustomArmor(); else applyPresetArmor(next);
    }

    private void snapshotCustomArmor() {
        for (int i = 0; i < ARMOR_SLOTS.length; i++) customArmor.set(i, getItemBySlot(ARMOR_SLOTS[i]).copy());
    }
    private void restoreCustomArmor() {
        for (int i = 0; i < ARMOR_SLOTS.length; i++) setItemSlot(ARMOR_SLOTS[i], customArmor.get(i).copy());
    }
    private void maintainPresetArmor() {
        Item[] preset = presetItems(getLoadoutIndex());
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            ItemStack equipped = getItemBySlot(ARMOR_SLOTS[i]);
            if (!equipped.is(preset[i]) || equipped.isDamaged() || !PresetArmorData.isPreset(equipped)) {
                setItemSlot(ARMOR_SLOTS[i], presetStack(preset[i]));
            }
        }
    }
    private void applyPresetArmor(int index) {
        Item[] preset = presetItems(index);
        for (int i = 0; i < ARMOR_SLOTS.length; i++) setItemSlot(ARMOR_SLOTS[i], presetStack(preset[i]));
    }
    private static ItemStack presetStack(Item item) { return PresetArmorData.mark(new ItemStack(item)); }
    private static Item[] presetItems(int index) {
        return switch (index) {
            case 1 -> new Item[] { Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS };
            case 2 -> new Item[] { Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS };
            case 3 -> new Item[] { Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS };
            case 4 -> new Item[] { Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS };
            case 5 -> new Item[] { Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS };
            case 6 -> new Item[] { Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS };
            default -> throw new IllegalArgumentException("No armor preset for index " + index);
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("InfoCardEnabled", isInfoCardEnabled());
        tag.putInt("LoadoutIndex", getLoadoutIndex());
        tag.putInt("ModelMode", getModelMode());
        tag.putString("PassiveMob", entityData.get(PASSIVE_MOB));
        tag.putString("HostileMob", entityData.get(HOSTILE_MOB));
        if (proxyUuid != null) tag.putUUID("ProxyUuid", proxyUuid);
        CompoundTag customArmorTag = new CompoundTag();
        ContainerHelper.saveAllItems(customArmorTag, customArmor, registryAccess());
        tag.put("CustomArmor", customArmorTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("InfoCardEnabled")) setInfoCardEnabled(tag.getBoolean("InfoCardEnabled"));
        if (tag.contains("CustomArmor")) ContainerHelper.loadAllItems(tag.getCompound("CustomArmor"), customArmor, registryAccess());
        int loadout = Math.max(CUSTOM_LOADOUT, Math.min(LAST_PRESET, tag.getInt("LoadoutIndex")));
        entityData.set(LOADOUT_INDEX, loadout);
        if (loadout != CUSTOM_LOADOUT) applyPresetArmor(loadout);
        if (tag.contains("PassiveMob") && DummyMobCatalog.PASSIVE.contains(tag.getString("PassiveMob"))) entityData.set(PASSIVE_MOB, tag.getString("PassiveMob"));
        if (tag.contains("HostileMob") && DummyMobCatalog.HOSTILE.contains(tag.getString("HostileMob"))) entityData.set(HOSTILE_MOB, tag.getString("HostileMob"));
        int mode = Math.max(DummyMobCatalog.IMMORTAL_STEVE, Math.min(DummyMobCatalog.HOSTILE_MOBS, tag.getInt("ModelMode")));
        entityData.set(MODEL_MODE, mode);
        boolean steve = mode == DummyMobCatalog.IMMORTAL_STEVE;
        setInvisible(!steve);
        setInvulnerable(!steve);
        setNoGravity(!steve);
        noPhysics = !steve;
        if (tag.hasUUID("ProxyUuid")) proxyUuid = tag.getUUID("ProxyUuid");
    }

    @Override public Component getDisplayName() { return Component.translatable("entity.fender8891_target_dummy.target_dummy"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new TargetDummyMenu(id, inventory, this); }
}
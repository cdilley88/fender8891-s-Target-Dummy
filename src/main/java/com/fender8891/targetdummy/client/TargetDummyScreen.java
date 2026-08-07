package com.fender8891.targetdummy.client;

import com.fender8891.targetdummy.entity.DummyMobCatalog;
import com.fender8891.targetdummy.entity.TargetDummy;
import com.fender8891.targetdummy.inventory.TargetDummyMenu;
import com.fender8891.targetdummy.network.TargetDummyFacingPayload;
import com.fender8891.targetdummy.network.TargetDummyInfoCardPayload;
import com.fender8891.targetdummy.network.TargetDummyLoadoutPayload;
import com.fender8891.targetdummy.network.TargetDummyModelPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class TargetDummyScreen extends AbstractContainerScreen<TargetDummyMenu> {
    private static final int PANEL = 0xF21A202A;
    private static final int PANEL_INNER = 0xFF252E3A;
    private static final int SECTION = 0xFF1B2530;
    private static final int CYAN = 0xFF42D9E8;
    private static final int ORANGE = 0xFFFFA23A;
    private static final int SLOT_BORDER = 0xFF76879A;
    private static final int SLOT_DARK = 0xFF111820;
    private static final int MOBS_PER_PAGE = 8;

    private final List<Button> mobButtons = new ArrayList<>();
    private Button modelButton;
    private Button modeButton;
    private Button presetButton;
    private Button infoCardButton;
    private Button facingButton;
    private Button previousPageButton;
    private Button nextPageButton;
    private int mobPage;
    private int lastModelMode = -1;

    public TargetDummyScreen(TargetDummyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 276;
        titleLabelX = 12;
        titleLabelY = 9;
        inventoryLabelX = 69;
        inventoryLabelY = 182;
    }

    @Override
    protected void init() {
        super.init();
        modelButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            int next = (menu.getModelMode() + 1) % 3;
            sendModel(next, "");
        }).bounds(leftPos + 200, topPos + 37, 88, 20).build());
        facingButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            int entityId = menu.getDummyEntityId();
            if (entityId >= 0) {
                int next = (menu.getFacingIndex() + 1) % 4;
                PacketDistributor.sendToServer(new TargetDummyFacingPayload(entityId, next));
            }
        }).bounds(leftPos + 200, topPos + 61, 88, 20).build());

        modeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            int current = menu.getLoadoutIndex();
            sendLoadout(current == TargetDummy.CUSTOM_LOADOUT ? TargetDummy.FIRST_PRESET : TargetDummy.CUSTOM_LOADOUT);
        }).bounds(leftPos + 200, topPos + 93, 88, 20).build());

        presetButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            int next = menu.getLoadoutIndex() + 1;
            if (next > TargetDummy.LAST_PRESET) next = TargetDummy.FIRST_PRESET;
            sendLoadout(next);
        }).bounds(leftPos + 200, topPos + 117, 88, 20).build());

        infoCardButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            int entityId = menu.getDummyEntityId();
            if (entityId >= 0) {
                boolean enabled = !menu.isInfoCardEnabled();
                menu.setInfoCardEnabled(enabled);
                PacketDistributor.sendToServer(new TargetDummyInfoCardPayload(entityId, enabled));
            }
        }).bounds(leftPos + 200, topPos + 145, 88, 20).build());

        for (int i = 0; i < MOBS_PER_PAGE; i++) {
            final int buttonIndex = i;
            int col = i % 2;
            int row = i / 2;
            mobButtons.add(addRenderableWidget(Button.builder(Component.empty(), button -> selectMob(buttonIndex))
                    .bounds(leftPos + 18 + col * 86, topPos + 49 + row * 23, 82, 20).build()));
        }
        previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            if (mobPage > 0) mobPage--;
        }).bounds(leftPos + 18, topPos + 145, 28, 18).build());
        nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> mobPage++)
                .bounds(leftPos + 160, topPos + 145, 28, 18).build());
        updateButtons();
    }

    private void sendLoadout(int loadoutIndex) {
        if (menu.getDummyEntityId() >= 0) PacketDistributor.sendToServer(new TargetDummyLoadoutPayload(menu.getDummyEntityId(), loadoutIndex));
    }
    private void sendModel(int mode, String mobId) {
        if (menu.getDummyEntityId() >= 0) PacketDistributor.sendToServer(new TargetDummyModelPayload(menu.getDummyEntityId(), mode, mobId));
    }
    private void selectMob(int buttonIndex) {
        List<String> entries = DummyMobCatalog.entries(menu.getModelMode());
        int index = mobPage * MOBS_PER_PAGE + buttonIndex;
        if (index < entries.size()) sendModel(menu.getModelMode(), entries.get(index));
    }

    private void updateButtons() {
        if (modelButton == null) return;
        int modelMode = menu.getModelMode();
        if (modelMode != lastModelMode) {
            mobPage = 0;
            lastModelMode = modelMode;
        }
        boolean steve = modelMode == DummyMobCatalog.IMMORTAL_STEVE;
        modelButton.setMessage(Component.literal(menu.getModelModeName()));
        facingButton.setMessage(Component.literal("FACING: " + menu.getFacingName()));
        modeButton.visible = steve;
        modeButton.active = steve;
        modeButton.setMessage(Component.literal(menu.getLoadoutIndex() == TargetDummy.CUSTOM_LOADOUT ? "CUSTOM" : "PRESET"));
        presetButton.visible = steve && menu.getLoadoutIndex() != TargetDummy.CUSTOM_LOADOUT;
        presetButton.active = presetButton.visible;
        presetButton.setMessage(Component.literal(menu.getLoadoutName()));
        infoCardButton.setMessage(Component.literal(menu.isInfoCardEnabled() ? "CARD: ON" : "CARD: OFF"));

        List<String> entries = DummyMobCatalog.entries(modelMode);
        int pageCount = Math.max(1, (entries.size() + MOBS_PER_PAGE - 1) / MOBS_PER_PAGE);
        mobPage = Math.min(mobPage, pageCount - 1);
        for (int i = 0; i < mobButtons.size(); i++) {
            Button button = mobButtons.get(i);
            int index = mobPage * MOBS_PER_PAGE + i;
            button.visible = !steve && index < entries.size();
            button.active = button.visible;
            if (button.visible) {
                String id = entries.get(index);
                String label = mobName(id);
                if (id.equals(menu.getSelectedMobId())) label = "> " + label;
                button.setMessage(Component.literal(label));
            }
        }
        previousPageButton.visible = !steve && pageCount > 1;
        previousPageButton.active = previousPageButton.visible && mobPage > 0;
        nextPageButton.visible = !steve && pageCount > 1;
        nextPageButton.active = nextPageButton.visible && mobPage < pageCount - 1;
    }

    private String mobName(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) return id;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(location);
        return type.getDescription().getString();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        boolean steve = menu.getModelMode() == DummyMobCatalog.IMMORTAL_STEVE;
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, PANEL_INNER);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + 5, CYAN);
        graphics.fill(leftPos + 10, topPos + 28, leftPos + 192, topPos + 169, SECTION);
        graphics.fill(leftPos + 196, topPos + 28, leftPos + 290, topPos + 169, SECTION);
        graphics.fill(leftPos + 3, topPos + 173, leftPos + imageWidth - 3, topPos + 175, ORANGE);

        graphics.drawString(font, steve ? "EQUIPMENT" : "MOB SELECTION", leftPos + 18, topPos + 20, 0xFFA9F5FC, false);
        graphics.drawString(font, "DUMMY MODEL", leftPos + 203, topPos + 20, 0xFF72E6F0, false);
        graphics.drawString(font, "INFO CARD", leftPos + 203, topPos + 134, 0xFF9EACBC, false);

        if (steve) {
            graphics.drawString(font, "ARMOR", leftPos + 18, topPos + 37, 0xFF72E6F0, false);
            graphics.drawString(font, "HANDS", leftPos + 102, topPos + 37, 0xFFFFB654, false);
            graphics.drawString(font, "LOADOUT MODE", leftPos + 203, topPos + 83, 0xFFFFC978, false);
            for (int i = 0; i < 6; i++) {
                var slot = menu.slots.get(i);
                if (slot.isActive()) drawSlot(graphics, leftPos + slot.x, topPos + slot.y, i < 4 ? CYAN : ORANGE);
            }
            drawSlotLabel(graphics, "HEAD", 52, 52);
            drawSlotLabel(graphics, "CHEST", 52, 76);
            drawSlotLabel(graphics, "LEGS", 52, 100);
            drawSlotLabel(graphics, "FEET", 52, 124);
            drawSlotLabel(graphics, "MAIN", 136, 63);
            drawSlotLabel(graphics, "OFF", 136, 105);
        } else {
            graphics.drawString(font, menu.getModelModeName(), leftPos + 18, topPos + 36, 0xFFFFC978, false);
            List<String> entries = DummyMobCatalog.entries(menu.getModelMode());
            int pages = Math.max(1, (entries.size() + MOBS_PER_PAGE - 1) / MOBS_PER_PAGE);
            graphics.drawCenteredString(font, "PAGE " + (mobPage + 1) + "/" + pages, leftPos + 103, topPos + 150, 0xFF8EA0B4);
            graphics.drawString(font, "NATIVE STATS", leftPos + 207, topPos + 89, 0xFF62E889, false);
            graphics.drawString(font, "AI DISABLED", leftPos + 207, topPos + 103, 0xFF8EA0B4, false);
            graphics.drawString(font, "RESPAWN 1.0s", leftPos + 207, topPos + 117, 0xFF8EA0B4, false);
        }

        for (int i = 6; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            drawSlot(graphics, leftPos + slot.x, topPos + slot.y, SLOT_BORDER);
        }
        graphics.drawString(font, "INVENTORY", leftPos + inventoryLabelX, topPos + inventoryLabelY, 0xFFE9EEF5, false);
        graphics.drawString(font, "Sneak + Right Click to Pack Up", leftPos + 69, topPos + 267, 0xFF8EA0B4, false);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, int accent) {
        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xFF090D12);
        graphics.fill(x - 1, y - 1, x + 17, y + 17, accent);
        graphics.fill(x, y, x + 16, y + 16, SLOT_DARK);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF1B2632);
    }
    private void drawSlotLabel(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(font, text, leftPos + x, topPos + y, 0xFFB8C6D6, false);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateButtons();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
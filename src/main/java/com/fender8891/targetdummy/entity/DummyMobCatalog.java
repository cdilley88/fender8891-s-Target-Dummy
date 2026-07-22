package com.fender8891.targetdummy.entity;

import java.util.List;

public final class DummyMobCatalog {
    public static final int IMMORTAL_STEVE = 0;
    public static final int PASSIVE_MOBS = 1;
    public static final int HOSTILE_MOBS = 2;

    public static final List<String> PASSIVE = List.of(
            "minecraft:cow", "minecraft:pig", "minecraft:sheep", "minecraft:chicken",
            "minecraft:rabbit", "minecraft:wolf", "minecraft:cat", "minecraft:fox",
            "minecraft:goat", "minecraft:horse", "minecraft:donkey", "minecraft:llama",
            "minecraft:panda", "minecraft:polar_bear", "minecraft:mooshroom", "minecraft:sniffer",
            "minecraft:armadillo", "minecraft:turtle", "minecraft:bee"
    );

    public static final List<String> HOSTILE = List.of(
            "minecraft:zombie", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider",
            "minecraft:cave_spider", "minecraft:enderman", "minecraft:witch", "minecraft:slime",
            "minecraft:drowned", "minecraft:husk", "minecraft:stray", "minecraft:pillager",
            "minecraft:vindicator", "minecraft:evoker", "minecraft:piglin", "minecraft:zombified_piglin",
            "minecraft:blaze", "minecraft:wither_skeleton", "minecraft:guardian", "minecraft:breeze",
            "minecraft:bogged"
    );

    private DummyMobCatalog() {
    }

    public static List<String> entries(int mode) {
        return mode == PASSIVE_MOBS ? PASSIVE : mode == HOSTILE_MOBS ? HOSTILE : List.of();
    }

    public static boolean isValidSelection(int mode, String mobId) {
        return entries(mode).contains(mobId);
    }

    public static String defaultSelection(int mode) {
        return mode == HOSTILE_MOBS ? HOSTILE.getFirst() : PASSIVE.getFirst();
    }

    public static String modeName(int mode) {
        return switch (mode) {
            case PASSIVE_MOBS -> "PASSIVE MOBS";
            case HOSTILE_MOBS -> "HOSTILE MOBS";
            default -> "IMMORTAL STEVE";
        };
    }
}

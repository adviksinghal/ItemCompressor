package com.ic.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class ItemClusterManager {
    private static final Map<UUID, Integer> CLUSTER_COUNTS = new HashMap<>();
    private static final Map<UUID, Component> CLUSTER_NAMES = new HashMap<>();
    private static final Set<UUID> SUPPRESSED_ENTITIES = new HashSet<>();
    private static long lastRunTick = -1;

    public static void updateClustersIfNeeded(ClientLevel level) {
        long currentTick = level.getGameTime();
        // Throttle to once per tick to keep performance buttery smooth
        if (currentTick == lastRunTick) return;
        lastRunTick = currentTick;

        CLUSTER_COUNTS.clear();
        CLUSTER_NAMES.clear();
        SUPPRESSED_ENTITIES.clear();

        List<ItemEntity> allItems = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive() && !itemEntity.getItem().isEmpty()) {
                allItems.add(itemEntity);
            }
        }

        Set<UUID> processed = new HashSet<>();

        for (ItemEntity primary : allItems) {
            if (processed.contains(primary.getUUID())) continue;

            Item targetItem = primary.getItem().getItem();
            AABB searchArea = primary.getBoundingBox().inflate(2.0, 1.0, 2.0);

            List<ItemEntity> nearby = new ArrayList<>();
            for (ItemEntity candidate : allItems) {
                if (candidate.isAlive() && candidate.getItem().getItem() == targetItem && searchArea.intersects(candidate.getBoundingBox())) {
                    nearby.add(candidate);
                }
            }

            if (nearby.size() > 1) {
                nearby.sort(Comparator.comparing(e -> e.getUUID().toString()));
                ItemEntity master = nearby.get(0);

                int totalItemCount = 0;
                for (ItemEntity e : nearby) {
                    totalItemCount += e.getItem().getCount();
                    processed.add(e.getUUID());
                    if (!e.getUUID().equals(master.getUUID())) {
                        SUPPRESSED_ENTITIES.add(e.getUUID());
                    }
                }

                CLUSTER_COUNTS.put(master.getUUID(), totalItemCount);
                String itemName = primary.getItem().getHoverName().getString();
                CLUSTER_NAMES.put(master.getUUID(), Component.literal("x" + totalItemCount + " " + itemName));
            }
        }
    }

    public static boolean shouldSuppressRender(ItemEntity entity) {
        return SUPPRESSED_ENTITIES.contains(entity.getUUID());
    }

    public static int getClusterCount(Entity entity) {
        return CLUSTER_COUNTS.getOrDefault(entity.getUUID(), -1);
    }

    public static Component getClusterName(Entity entity) {
        return CLUSTER_NAMES.get(entity.getUUID());
    }
}
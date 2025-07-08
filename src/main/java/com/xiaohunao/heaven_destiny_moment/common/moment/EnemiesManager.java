package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class EnemiesManager {
    private final Set<UUID> enemies = Sets.newHashSet();
    private final Set<UUID> loadingEntities = Sets.newHashSet();

    private final Map<UUID,Entity> entitySet = Maps.newHashMap();

    public void addEnemy(Entity entity) {
        UUID uuid = entity.getUUID();
        enemies.add(uuid);
        entitySet.put(entity.getUUID(),entity);
        loadingEntities.remove(uuid);
    }

    public void removeEnemy(UUID uuid) {
        enemies.remove(uuid);
        entitySet.remove(uuid);
        loadingEntities.remove(uuid);
    }

    public boolean hasEnemy(UUID uuid) {
        return enemies.contains(uuid);
    }

    public boolean isEmpty() {
        return enemies.isEmpty();
    }

    public int size() {
        return enemies.size();
    }

    public Set<UUID> getEnemies() {
        return enemies;
    }

    public void markEntityAsLoaded(UUID uuid) {
        if (enemies.contains(uuid)) {
            loadingEntities.remove(uuid);
        }
    }

    public boolean shouldRemoveEntity(UUID uuid, ServerLevel serverLevel) {
        if (!enemies.contains(uuid)) {
            return false;
        }

        if (loadingEntities.contains(uuid)) {
            return false;
        }

        Entity entity = serverLevel.getEntity(uuid);
        if (entity != null) {
            return false;
        }

        if (serverLevel.entityManager.isLoaded(uuid)) {
            return false;
        }

        return true;
    }

    public void deserializeNBT(CompoundTag compoundTag) {
        enemies.clear();
        loadingEntities.clear();
        
        compoundTag.getList("enemies", Tag.TAG_STRING).forEach(uid -> {
            UUID uuid = UUID.fromString(uid.getAsString());
            enemies.add(uuid);
            loadingEntities.add(uuid);
        });
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        
        ListTag enemiesListTag = new ListTag();

        
        enemies.forEach(uid -> {
            enemiesListTag.add(StringTag.valueOf(uid.toString()));
        });
        compoundTag.put("enemies", enemiesListTag);
        return compoundTag;
    }

    public void killAllEnemies(ServerLevel serverLevel) {
        List<UUID> enemiesCopy = new ArrayList<>(enemies);

        Set<UUID> toRemove = Sets.newHashSet();

        for (UUID enemy : enemiesCopy) {
            Entity entity = serverLevel.getEntity(enemy);
            if (entity != null) {
                entity.kill();
                toRemove.add(enemy);
            }
        }

        for (UUID uuid : toRemove) {
            enemies.remove(uuid);
            entitySet.remove(uuid);
            loadingEntities.remove(uuid);
        }
    }

    public void clearAllEnemiesFlags(ServerLevel serverLevel) {
        enemies.forEach(uuid -> {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity != null) {
                entity.setData(HDMAttachments.MOMENT_ENTITY, entity.getData(HDMAttachments.MOMENT_ENTITY).setUid(null));
            }
        });
    }
}

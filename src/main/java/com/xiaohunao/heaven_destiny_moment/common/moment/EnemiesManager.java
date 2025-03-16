package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EnemiesManager {
    private final Set<UUID> enemies = Sets.newHashSet();
    private final Map<UUID, CompoundTag> enemiesStorage = Maps.newHashMap();

    public void addEnemy(Entity entity) {
        UUID uuid = entity.getUUID();
        enemies.add(uuid);
        CompoundTag tag = new CompoundTag();
        entity.save(tag);
        enemiesStorage.put(uuid, tag);
    }

    public void removeEnemy(UUID uuid) {
        enemies.remove(uuid);
        enemiesStorage.remove(uuid);
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

    public void deserializeNBT(CompoundTag compoundTag) {
        enemies.clear();
        enemiesStorage.clear();
        
        compoundTag.getList("enemies", Tag.TAG_STRING).forEach(uid -> {
            enemies.add(UUID.fromString(uid.getAsString()));
        });

        compoundTag.getList("enemiesStorage", Tag.TAG_COMPOUND).forEach(tag -> {
            CompoundTag tag1 = (CompoundTag) tag;
            UUID uuid = tag1.getUUID("uuid");
            CompoundTag compoundTag1 = tag1.getCompound("tag");
            enemiesStorage.put(uuid, compoundTag1);
        });
    }



    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        
        ListTag enemiesListTag = new ListTag();
        ListTag enemiesStorageTag = new ListTag();
        
        enemies.forEach(uid -> {
            enemiesListTag.add(StringTag.valueOf(uid.toString()));
        });
        compoundTag.put("enemies", enemiesListTag);

        enemiesStorage.forEach((uuid, tag) -> {
            CompoundTag tag1 = new CompoundTag();
            tag1.putUUID("uuid", uuid);
            tag1.put("tag", tag);
            enemiesStorageTag.add(tag1);
        });
        compoundTag.put("enemiesStorage", enemiesStorageTag);
        return compoundTag;
    }

    public void loadStoredEntities(Level level) {
        enemiesStorage.forEach((uuid, tag) -> {
            EntityType.create(tag, level).ifPresent(entity -> {
                level.addFreshEntity(entity);
                enemies.add(uuid);
            });
        });
    }

    public void updateEntityStorage(Entity entity) {
        UUID uuid = entity.getUUID();
        if (enemies.contains(uuid)) {
            CompoundTag tag = new CompoundTag();
            entity.save(tag);
            enemiesStorage.put(uuid, tag);
        }
    }

    public void killAllEnemies(ServerLevel level) {
        Set<UUID> toRemove = Sets.newHashSet();

        enemies.forEach(uuid -> {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                entity.kill();
                toRemove.add(uuid);
            }
        });

        toRemove.forEach(uuid -> {
            enemies.remove(uuid);
            enemiesStorage.remove(uuid);
        });
    }
}

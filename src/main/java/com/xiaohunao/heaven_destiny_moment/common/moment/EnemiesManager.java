package com.xiaohunao.heaven_destiny_moment.common.moment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnemiesManager {
    private final UUID instanceUUID;

    private final Map<UUID, EnemyEntry> enemyMap = new ConcurrentHashMap<>();

    public EnemiesManager(UUID instanceUUID) {
        this.instanceUUID = instanceUUID;
        NeoForge.EVENT_BUS.register(this);
    }


    public void addEnemy(Entity entity) {
        if (entity == null) return;

        UUID uuid = entity.getUUID();
        enemyMap.put(uuid, new EnemyEntry(entity, true)); // 已加载的实体，isLoading=true
    }


    public void removeEnemy(UUID uuid) {
        if (uuid == null) return;
        enemyMap.remove(uuid);
    }


    public boolean hasEnemy(UUID uuid) {
        return uuid != null && enemyMap.containsKey(uuid);
    }


    public int size() {
        return enemyMap.size();
    }

    public boolean isEmpty() {
        return enemyMap.isEmpty();
    }


    public Set<UUID> getEnemies() {
        return enemyMap.keySet();
    }


    public void markEntityAsLoaded(UUID uuid) {
        if (uuid == null) return;

        EnemyEntry entry = enemyMap.get(uuid);
        if (entry != null) {
            entry.isLoading = true; // 实体已加载
        }
    }

    /**
     * 标记实体为未加载状态
     */
    public void markEntityAsUnloaded(UUID uuid) {
        if (uuid == null) return;

        EnemyEntry entry = enemyMap.get(uuid);
        if (entry != null) {
            entry.isLoading = false; // 实体未加载
        }
    }

    /**
     * 判断是否应该移除实体
     * 逻辑：如果实体未加载，且在世界中找不到，且不在实体管理器的加载列表中，则应该移除
     */
    public boolean shouldRemoveEntity(UUID uuid, ServerLevel serverLevel) {
        if (uuid == null || !enemyMap.containsKey(uuid)) {
            return false;
        }

        EnemyEntry entry = enemyMap.get(uuid);

        // 如果实体已加载，不应移除
        if (entry.isLoading) {
            return false;
        }

        // 尝试从缓存获取实体
        Entity entity = entry.getEntity();
        if (entity != null && entity.isAlive()) {
            entry.isLoading = true; // 找到活着的实体，标记为已加载
            return false;
        }

        // 尝试从世界获取实体
        entity = serverLevel.getEntity(uuid);
        if (entity != null) {
            entry.updateReference(entity);
            entry.isLoading = true; // 找到实体，标记为已加载
            return false;
        }

        // 检查实体是否仍在加载中
        return !serverLevel.entityManager.isLoaded(uuid);
    }


    public void deserializeNBT(CompoundTag compoundTag) {
        enemyMap.clear();

        if (compoundTag == null) return;

        ListTag enemiesListTag = compoundTag.getList("enemies", Tag.TAG_STRING);
        for (Tag tag : enemiesListTag) {
            try {
                UUID uuid = UUID.fromString(tag.getAsString());
                enemyMap.put(uuid, new EnemyEntry(null, false)); // 标记为未加载
            } catch (IllegalArgumentException e) {
                // 忽略无效的UUID
            }
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        ListTag enemiesListTag = new ListTag();

        for (UUID uuid : enemyMap.keySet()) {
            enemiesListTag.add(StringTag.valueOf(uuid.toString()));
        }

        compoundTag.put("enemies", enemiesListTag);
        return compoundTag;
    }

    public void killAllEnemies(ServerLevel serverLevel) {
        if (serverLevel == null) return;

        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, EnemyEntry> entry : enemyMap.entrySet()) {
            UUID uuid = entry.getKey();

            // 尝试从缓存获取实体
            Entity entity = entry.getValue().getEntity();

            // 如果缓存中没有或已死亡，从世界获取
            if (entity == null || !entity.isAlive()) {
                entity = serverLevel.getEntity(uuid);
            }

            if (entity != null && entity.isAlive()) {
                entity.kill();
                toRemove.add(uuid);
            }
        }

        // 批量移除已杀死的敌人
        toRemove.forEach(enemyMap::remove);
    }




    public Entity getEntity(UUID uuid, ServerLevel level) {
        if (uuid == null || level == null || !enemyMap.containsKey(uuid)) {
            return null;
        }

        EnemyEntry entry = enemyMap.get(uuid);
        Entity entity = entry.getEntity();

        if (entity == null || !entity.isAlive()) {
            entity = level.getEntity(uuid);
            if (entity != null) {
                entry.updateReference(entity);
                entry.isLoading = true; // 找到实体后标记为已加载
            }
        }

        return entity;
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }

        if (enemyMap.isEmpty()) return;

        EntitySectionStorage<Entity> sectionStorage = serverLevel.entityManager.sectionStorage;
        ChunkAccess chunk = event.getChunk();
        ChunkPos pos = chunk.getPos();

        // 处理区块中的实体
        Set<UUID> toRemove = new HashSet<>();

        sectionStorage.getExistingSectionsInChunk(pos.toLong())
                .flatMap(EntitySection::getEntities)
                .forEach(entity -> {
                    UUID uuid = entity.getUUID();
                    if (enemyMap.containsKey(uuid)) {
                        // 区块卸载时，标记实体为未加载
                        markEntityAsUnloaded(uuid);

                        // 检查是否应该移除
                        if (shouldRemoveEntity(uuid, serverLevel)) {
                            toRemove.add(uuid);
                        }
                    }
                });

        toRemove.forEach(this::removeEnemy);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }

        if (enemyMap.isEmpty()) return;

        EntitySectionStorage<Entity> sectionStorage = serverLevel.entityManager.sectionStorage;
        ChunkAccess chunk = event.getChunk();
        ChunkPos pos = chunk.getPos();

        // 更新区块中已加载的敌人状态
        sectionStorage.getExistingSectionsInChunk(pos.toLong())
                .flatMap(EntitySection::getEntities)
                .forEach(entity -> {
                    UUID uuid = entity.getUUID();
                    if (enemyMap.containsKey(uuid)) {
                        markEntityAsLoaded(uuid); // 区块加载时，标记实体为已加载
                        enemyMap.get(uuid).updateReference(entity);
                    }
                });
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (enemyMap.isEmpty()) return;

        Entity entity = event.getEntity();
        UUID uuid = entity.getUUID();

        if (enemyMap.containsKey(uuid)) {
            enemyMap.get(uuid).updateReference(entity);
            markEntityAsLoaded(uuid); // 等实体加入世界时，标记为已加载
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        Level level = event.getEntity().level();
        DamageSource source = event.getSource();
        if (source == null) return;
        if (level.isClientSide() || enemyMap.isEmpty()) return;


        UUID uuid = event.getEntity().getUUID();
        if (enemyMap.containsKey(uuid)) {
            removeEnemy(uuid);
            MomentInstance momentInstance = MomentInstanceManager.of(level).getMomentInstance(instanceUUID);
            if (momentInstance != null) {
                momentInstance.addKillCount(event.getEntity(), event.getSource());
                momentInstance.livingDeath(event.getEntity(), event.getSource());
            }
        }
    }


    private static class EnemyEntry {
        private WeakReference<Entity> entityRef;
        private boolean isLoading; // true表示实体已加载，false表示实体未加载

        public EnemyEntry(Entity entity, boolean isLoading) {
            this.entityRef = entity != null ? new WeakReference<>(entity) : null;
            this.isLoading = isLoading;
        }

        public Entity getEntity() {
            return entityRef != null ? entityRef.get() : null;
        }

        public void updateReference(Entity entity) {
            if (entity != null) {
                this.entityRef = new WeakReference<>(entity);
            }
        }
    }
}

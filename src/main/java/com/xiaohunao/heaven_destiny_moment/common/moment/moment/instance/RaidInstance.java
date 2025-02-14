package com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMomentRegister;
import com.xiaohunao.heaven_destiny_moment.common.mixed.EntityManagerAccessor;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.EnemiesManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

import java.util.*;

public class RaidInstance extends MomentInstance<RaidMoment> {
    protected Vec3 originalPos;
    protected int currentWave = -1;
    private int totalWaves;
    protected int totalEnemy;
    private int readyTime;

    public RaidInstance(Level level, ResourceKey<Moment<?>> momentKey) {
        super(HDMMomentRegister.RAID.get(), level, momentKey);
    }


    public RaidInstance(UUID uuid, Level level, ResourceKey<Moment<?>> momentResourceKey) {
        super(HDMMomentRegister.RAID.get(), uuid, level, momentResourceKey);
    }

    @Override
    public void initSpawnPosList() {
        if (this.originalPos != null) {
            spawnPosList.add(this.originalPos);
        }
    }

    @Override
    public void init() {
        super.init();

        moment().ifPresent(raidMoment -> {
            this.readyTime = raidMoment.readyTime();
            this.totalWaves = raidMoment.momentData()
                    .flatMap(MomentData::entitySpawnSettings)
                    .flatMap(EntitySpawnSettings::entitySpawnList)
                    .map(List::size)
                    .orElse(0);
        });
    }

    @Override
    public void finalizeSpawn(Entity entity) {
        attackRandomPlayer(entity);
    }


    @Override
    protected void ready() {
        if (this.bar == null) {
            setState(MomentState.END);
            return;
        }
        
        int readyTime = moment().map(RaidMoment::readyTime).orElse(100);
        if (this.readyTime <= 0) {
            setState(MomentState.START);
        }
        updateBarProgress(1 - (float) this.readyTime / readyTime);
        this.readyTime--;
    }

    @Override
    protected void ongoing() {
        checkNextWave();
        updateWave();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        super.deserializeNBT(compoundTag);
        this.currentWave = compoundTag.getInt("currentWave");
        this.totalWaves = compoundTag.getInt("totalWaves");
        this.totalEnemy = compoundTag.getInt("totalEnemy");
        this.readyTime = compoundTag.getInt("readyTime");
        if (compoundTag.contains("originalPos")) {
            this.originalPos = Vec3.CODEC.decode(NbtOps.INSTANCE, compoundTag.getList("originalPos", 6)).getOrThrow().getFirst();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = super.serializeNBT();
        compoundTag.putInt("currentWave", currentWave);
        compoundTag.putInt("totalWaves", totalWaves);
        compoundTag.putInt("totalEnemy", totalEnemy);
        compoundTag.putInt("readyTime", readyTime);
        if (this.originalPos != null) {
            compoundTag.put("originalPos", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.originalPos).getOrThrow());
        }
        return compoundTag;
    }

    protected void checkNextWave(){
        if (level.isClientSide){
            return;
        }
        if (enemiesManager.isEmpty()){
            if(this.currentWave >= this.totalWaves - 1) {
                setState(MomentState.VICTORY);
            } else {
                currentWave++;
                totalEnemy = 0;
            }
        }
    }

    protected void updateWave() {
        if (level.isClientSide){
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (enemiesManager.isEmpty() && state == MomentState.ONGOING){
            moment().flatMap(Moment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .map(entitySpawnSettings -> entitySpawnSettings.spawnList(level, currentWave))
                    .ifPresent(entities -> entities.forEach(entity -> {
                        addEnemy(entity);
                        entity.setGlowingTag(true);
                        spawnEntity(entity);
                        totalEnemy++;
                    }));
        }

        Set<UUID> toRemove = Sets.newHashSet();
        getEnemies().forEach(uid -> {
            Entity entity = serverLevel.getEntity(uid);
            updateBarProgress(getEnemyCount() / (float) totalEnemy);
            EntityManagerAccessor managerAccessor = (EntityManagerAccessor) serverLevel;
            if (entity == null && !managerAccessor.getEntityManager().isLoaded(uid)){
                toRemove.add(uid);
            }

            if (entity != null){
                enemiesManager.updateEntityStorage(entity);
            }
        });
        
        toRemove.forEach(this::removeEnemy);
    }

    public void setOriginalPos(Vec3 originalPos) {
        this.originalPos = originalPos;
    }

    private void attackRandomPlayer(Entity entity) {
        if (!level.isClientSide && entity instanceof Mob mob && !this.players.isEmpty()) {
            List<Player> players = this.players.stream().filter(player -> !player.isCreative()).toList();
            Optional<Player> target = Util.getRandomSafe(players, level.random);
            target.ifPresent(player -> {
                mob.getBrain().setMemory(MemoryModuleType.ANGRY_AT, player.getUUID());
                mob.setTarget(player);
            });
        }
    }
}

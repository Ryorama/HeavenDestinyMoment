package com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance;

import com.google.common.collect.Sets;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMomentTypes;
import com.xiaohunao.heaven_destiny_moment.common.moment.*;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class RaidInstance extends MomentInstance {
    protected Vec3 originalPos;
    protected int currentWave = -1;
    private int totalWaves;
    protected int totalEnemy;
    private int readyTime;

    public RaidInstance(Level level, IMoment moment) {
        super(HDMMomentTypes.RAID.get(), level, moment);
    }


    public RaidInstance(UUID uuid, Level level, IMoment moment) {
        super(HDMMomentTypes.RAID.get(), uuid, level, moment);
    }

    public RaidInstance(MomentType<?> type, Level level, IMoment moment) {
        super(type, level, moment);
    }

    public RaidInstance(MomentType<?> type, UUID uuid, Level level, IMoment moment) {
        super(type, uuid, level, moment);
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

        RaidMoment raidMoment = (RaidMoment) moment;
        this.readyTime = raidMoment.readyTime();

        this.totalWaves = moment.momentData()
                .flatMap(MomentData::entitySpawnSettings)
                .flatMap(EntitySpawnSettings::entitySpawnList)
                .map(List::size)
                .orElse(0);
    }



    @Override
    public void finalizeSpawn(Entity entity) {
        playerListManager.mandatoryAttackRandomPlayer(entity);
    }


    @Override
    protected void ready() {
        if (this.bar == null) {
            setState(MomentState.END);
            return;
        }

        RaidMoment raidMoment = (RaidMoment) moment;
        int readyTime = raidMoment.readyTime();
        if (this.readyTime <= 0) {
            setState(MomentState.START);
            updateBarProgress(1 - (float) this.readyTime / readyTime);
            return;
        }
        updateBarProgress(1 - (float) this.readyTime / readyTime);
        this.readyTime--;
        setState(MomentState.READY);
    }

    @Override
    protected void ongoing() {
        checkNextWave();
        updateWave();
        //当没有任何玩家参与时视为失败
//        if (players.isEmpty() && !level.players().isEmpty()){
//            setState(MomentState.LOSE);
//        }
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
            moment.momentData()
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
            if (enemiesManager.shouldRemoveEntity(uid, serverLevel)) {
                toRemove.add(uid);
            }
        });
        toRemove.forEach(this::removeEnemy);

        updateBarProgress(enemiesManager.size() / (float) totalEnemy);
    }

    public void setOriginalPos(Vec3 originalPos) {
        this.originalPos = originalPos;
    }

    @Override
    public Predicate<Player> validPlayer() {
        Predicate<Player> playerPredicate = super.validPlayer();
        return playerPredicate.and(player -> this.originalPos == null || level.isLoaded(BlockPos.containing(this.originalPos)));
    }
}

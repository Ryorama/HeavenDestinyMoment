package com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance;

import com.google.common.collect.Sets;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMomentRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RaidInstance extends MomentInstance<RaidMoment> {
    protected Vec3 originalPos;
    protected Set<UUID> enemies = Sets.newHashSet();
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
        addRaidTeam(entity);
        attackRandomPlayer(entity);
    }

    @Override
    public void end() {
        if (!level.isClientSide) {
            ServerScoreboard scoreboard = ((ServerLevel)level).getServer().getScoreboard();
            String teamName = getTeamName();
            PlayerTeam playerTeam = scoreboard.getPlayerTeam(teamName);
            if (playerTeam != null) {
                scoreboard.removePlayerTeam(playerTeam);
            }
        }
    }

    private void addRaidTeam(Entity entity){
        if(!level.isClientSide) {
            String teamName = getTeamName();
            ServerScoreboard scoreboard = ((ServerLevel)level).getServer().getScoreboard();
            PlayerTeam playerTeam = scoreboard.getPlayerTeam(teamName);
            if (playerTeam == null){
                PlayerTeam team = scoreboard.addPlayerTeam(teamName);
                team.setColor(ChatFormatting.RED);
                team.setDisplayName(Component.translatable(HeavenDestinyMoment.asDescriptionId("team." + teamName)));
                team.setAllowFriendlyFire(false);
                scoreboard.addPlayerToTeam(entity.getScoreboardName(), team);
            }else {
                scoreboard.addPlayerToTeam(entity.getScoreboardName(), playerTeam);
            }
        }
    }




    private String getTeamName() {
        return momentKey.location().toLanguageKey() + uuid;
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
            this.originalPos = Vec3.CODEC.decode(NbtOps.INSTANCE, compoundTag.getCompound("originalPos")).getOrThrow().getFirst();
        }

        compoundTag.getList("enemies", Tag.TAG_STRING).forEach(uid -> {
            enemies.add(UUID.fromString(uid.getAsString()));
        });
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = super.serializeNBT();
        compoundTag.put("currentWave",IntTag.valueOf(currentWave));
        compoundTag.put("totalWaves",IntTag.valueOf(totalWaves));
        compoundTag.put("totalEnemy",IntTag.valueOf(totalEnemy));
        compoundTag.put("readyTime",IntTag.valueOf(readyTime));
        if (compoundTag.contains("originalPos")) {
            compoundTag.put("originalPos", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.originalPos).getOrThrow());
        }

        ListTag enemiesListTag = new ListTag();
        enemies.forEach(uid -> {
            enemiesListTag.add(StringTag.valueOf(uid.toString()));
        });
        compoundTag.put("enemies",enemiesListTag);
        return compoundTag;
    }

    protected void checkNextWave(){
        if (enemies.isEmpty()){
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
        if (enemies.isEmpty() && state == MomentState.ONGOING){
            moment().flatMap(Moment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .map(entitySpawnSettings -> entitySpawnSettings.spawnList(level, currentWave))
                    .ifPresent(entities -> entities.forEach(entity -> {
                        enemies.add(entity.getUUID());
                        entity.setGlowingTag(true);
                        spawnEntity(entity);
                        totalEnemy++;
                    }));
        }

        enemies.removeIf(uid -> {
            Entity entity = serverLevel.getEntity(uid);
            updateBarProgress(enemies.size() / (float) totalEnemy);
            return entity == null;
        });
    }

    public int getReadyTime() {
        return readyTime;
    }

    public RaidInstance setOriginalPos(Vec3 originalPos) {
        this.originalPos = originalPos;
        return this;
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

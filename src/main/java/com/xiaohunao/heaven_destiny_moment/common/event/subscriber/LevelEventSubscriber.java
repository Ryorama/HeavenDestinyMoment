package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class LevelEventSubscriber {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide || entity instanceof Player){
            return;
        }

        if (event.loadedFromDisk() && level instanceof ServerLevel serverLevel) {
            MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);
            momentInstanceManager.getMomentInstances().forEach(instance -> {
                if (instance.hasEnemy(entity.getUUID())) {
                    instance.markEnemyAsLoaded(entity.getUUID());
                }
            });
        }
    }




    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        MomentInstanceManager.of(level).tick();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (source == null) return;
        LivingEntity entity = event.getEntity();
        entity.getData(HDMAttachments.MOMENT_ENTITY).getMomentInstance(entity).ifPresent(instance -> {
            instance.addKillCount(entity,source);
            instance.livingDeath(entity,source);
        });
    }

}

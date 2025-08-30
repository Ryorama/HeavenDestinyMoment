package com.xiaohunao.heaven_destiny_moment.common.tracker;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.moment.EnemiesManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

public class MobTeamTracker extends Tracker{
    public static final MapCodec<Tracker> CODEC = createCodec(tag -> new MobTeamTracker());


    @SubscribeEvent
    public void onLivingAttack(LivingChangeTargetEvent event) {
        LivingEntity attackEntity = event.getEntity();
        LivingEntity hurtEntity = event.getNewAboutToBeSetTarget();
        if (hurtEntity == null){
            return;
        }

        MomentInstance momentInstance = MomentInstanceManager.of(hurtEntity.level()).getMomentInstance(instanceUUID);
        if (momentInstance != null){
            EnemiesManager enemiesManager = momentInstance.getEnemiesManager();
            if (enemiesManager.hasEnemy(hurtEntity.getUUID()) && enemiesManager.hasEnemy(attackEntity.getUUID())) {
                event.setCanceled(true);
            }
        }
    }


    @Override
    public MapCodec<? extends ITracker> codec(){
        return CODEC;
    }
}

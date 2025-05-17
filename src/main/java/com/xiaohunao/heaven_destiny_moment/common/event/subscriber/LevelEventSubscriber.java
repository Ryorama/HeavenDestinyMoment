package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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
//        System.out.println("onEntityJoinLevel :" + entity);

    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        MomentInstanceManager.of(level).tick();
//        if (level instanceof ServerLevel serverLevel) {
//            Registry<Moment<?>> moments = level.registryAccess().registryOrThrow(HDMRegistries.Keys.MOMENT);
//            moments.entrySet().forEach(resourceKeyMomentEntry -> {
//                Moment<?> moment = resourceKeyMomentEntry.getValue();
//
//                serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> {
//                    moment.momentData()
//                            .flatMap(MomentData::conditionGroup)
//                            .flatMap(ConditionGroup::create)
//                            .ifPresent(createCondition -> {
//                                boolean allMatch = createCondition.getFirst() &&
//                                        createCondition.getSecond().stream()
//                                                .allMatch(condition -> condition.matches(momentInstance, serverPlayer.blockPosition(),serverPlayer));
//
//                                if (allMatch) {
//                                    MomentManager.of(serverLevel).createMomentInstance(resourceKeyMomentEntry.getKey(), serverPlayer.blockPosition(), serverPlayer);
//                                }
//                            });
//                });
//            });
//        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        entity.getData(HDMAttachments.MOMENT_ENTITY).getMomentInstance(entity).ifPresent(instance -> {
            instance.addKillCount(entity,source);
            instance.livingDeath(entity,source);
        });
    }

}

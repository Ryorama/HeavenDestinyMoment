package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.*;
import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.network.KillRequiredSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class TriggerTypeManager{
    private static final TriggerTypeManager INSTANCE = new TriggerTypeManager();
    public final Multimap<TriggerType<?>,Moment> registeredMomentsPerTrigger  = HashMultimap.create();
    private final Map<UUID,Map<ActuatorContext,Integer>> actuatorRemainingUses  = Maps.newHashMap();

    private TriggerTypeManager(){}

    public static <T extends ITrigger> void trigger(TriggerType<T> triggerType, Level level, ICanTrigger<T> iCanTrigger, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
        TriggerTypeManager triggerTypeManager = TriggerTypeManager.getInstance();
        Collection<Moment> moments = triggerTypeManager.get(triggerType);

        moments.forEach(moment -> {
            moment.momentData().flatMap(MomentData::autoActuatorGroupSettings)
                    .map(AutoActuatorGroupSettings::autoActuators)
                    .ifPresent(map -> {
                        map.forEach((triggerContext, actuatorContext) -> {
                            ITrigger rawTrigger = triggerContext.trigger();

                            if (!triggerType.clazz().isInstance(rawTrigger)) {
                                return;
                            }

                            T typedTrigger = triggerType.clazz().cast(rawTrigger); // 安全转换

                            if (actuatorContext.actuator() instanceof CreateMomentInstanceActuator) {
                                if (iCanTrigger.canTrigger(typedTrigger)) {
                                    momentInstanceManager.createMomentInstance(moment, pos, serverPlayer);
                                }
                            }

                            momentInstanceManager.getMomentInstances(moment).forEach(momentInstance -> {
                                boolean allMatch = true;
                                for (ICondition condition : triggerContext.conditions()) {
                                    if (condition instanceof KillEntityCondition killEntityCondition &&
                                            killEntityCondition.killType() == KillEntityRecorderAttachment.KillType.MOMENT &&
                                            actuatorContext.actuator() instanceof StateSettingActuator(MomentState state)) {
                                        KillEntityCondition.RequiredKill killRecord = killEntityCondition.getKillRecord(level);
                                        momentInstance.setVictoryRequiredKill(state,killRecord);
                                        PacketDistributor.sendToAllPlayers(new KillRequiredSyncPayload(momentInstance.getID(),state,killRecord));
                                    }

                                    if (!condition.matches(momentInstance, pos, serverPlayer)) {
                                        allMatch = false;
                                        break;
                                    }
                                }

                                boolean canTrigger = iCanTrigger.canTrigger(typedTrigger);

                                if (allMatch && canTrigger) {
                                    Integer remainingUses = triggerTypeManager.actuatorRemainingUses.get(momentInstance.getID()).get(actuatorContext);

                                    if (remainingUses != null) {
                                        actuatorContext.actuator().execute(momentInstance);

                                        // 只有当不是无限使用(-1)时才更新计数
                                        if (remainingUses != -1) {
                                            if (remainingUses == 1) {
                                                triggerTypeManager.actuatorRemainingUses.get(momentInstance.getID()).remove(actuatorContext);
                                            } else {
                                                triggerTypeManager.actuatorRemainingUses.get(momentInstance.getID()).put(actuatorContext, remainingUses - 1);
                                            }
                                        }
                                    }
                                }
                            });
                        });
                    });
        });
    }

    private <T extends ITrigger> Collection<Moment> get(TriggerType<T> triggerType) {
        return registeredMomentsPerTrigger.get(triggerType);
    }

    public static TriggerTypeManager getInstance() {
        return INSTANCE;
    }

    public void clear() {
        registeredMomentsPerTrigger.clear();
    }

    public void add(TriggerType<?> triggerType, Moment moment) {
        registeredMomentsPerTrigger.put(triggerType, moment);
    }

    public void addActuatorRemainingUses(UUID uuid,ActuatorContext actuatorContext, int remainingUses) {
        this.actuatorRemainingUses.computeIfAbsent(uuid, k -> Maps.newHashMap()).put(actuatorContext, remainingUses);
    }

    public void removeActuatorRemainingUses(UUID uuid,ActuatorContext actuatorContext) {
        this.actuatorRemainingUses.get(uuid).remove(actuatorContext);
    }

    public void removeActuatorRemainingUses(UUID uuid) {
        this.actuatorRemainingUses.remove(uuid);
    }

    @FunctionalInterface
    public interface ICanTrigger<T extends ITrigger> {
        boolean canTrigger(T trigger);
    }

}

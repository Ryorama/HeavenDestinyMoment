package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public interface ITrigger {
    Codec<ITrigger> CODEC = Codec.lazyInitialized(() -> HDMRegistries.TRIGGER_TYPE.byNameCodec()).dispatch(
            trigger -> {
                Registry<TriggerType<?>> registry = HDMRegistries.Suppliers.TRIGGER_TYPE.get();
                for (TriggerType<?> type : registry) {
                    if (type.getTriggerClass().isInstance(trigger)) {
                        return type;
                    }
                }
                throw new IllegalStateException("Unknown triggerType for " + trigger.getClass());
            },
            new Function<TriggerType<?>, MapCodec<? extends ITrigger>>() {
                @Override
                public MapCodec<? extends ITrigger> apply(TriggerType<?> triggerType) {
                    if (triggerType.isSerializable()){
                        return triggerType.getCodec();
                    }else {
                        Class<?> triggerClass = triggerType.getTriggerClass();
                        //创建实例
                        try {
                            Object newInstance = triggerClass.getDeclaredConstructor().newInstance();
                            if (newInstance instanceof ITrigger) {
                                return MapCodec.unit(() -> (ITrigger) newInstance);
                            }
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    return null;
                }
            }
    );


//    boolean canTrigger(MomentInstance momentInstance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer);
}

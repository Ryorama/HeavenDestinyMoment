package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public record ModLoadedCondition(String modid) implements ICondition {
    public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modid)
    ).apply(instance, ModLoadedCondition::new));

    public static ModLoadedCondition of(String modid) {
        return new ModLoadedCondition(modid);
    }

    @Override
    public boolean matches(AutomationContext context) {
        return ModList.get().isLoaded(modid);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

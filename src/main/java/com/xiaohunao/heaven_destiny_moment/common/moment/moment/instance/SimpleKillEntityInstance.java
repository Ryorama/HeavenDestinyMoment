package com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance;

import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SimpleKillEntityInstance extends DefaultInstance{
    public SimpleKillEntityInstance(Level level, Moment moment) {
        super(level, moment);
    }

    public SimpleKillEntityInstance(UUID uuid, Level level, Moment moment) {
        super(uuid, level, moment);
    }

    @Override
    public void initMomentBar() {
        super.initMomentBar();
        updateBarProgress(0.0f);
    }

    @Override
    public void addKillCount(LivingEntity livingEntity, DamageSource source) {
        super.addKillCount(livingEntity, source);
        KillEntityCondition.RequiredKill requiredKill = tryModifyStateRequiredKill.get(MomentState.VICTORY);
        int totalScore = getData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER).getTotalScore();
        updateBarProgress((float) totalScore / requiredKill.totalScore());
    }
}

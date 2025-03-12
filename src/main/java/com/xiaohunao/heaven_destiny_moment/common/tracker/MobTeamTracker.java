package com.xiaohunao.heaven_destiny_moment.common.tracker;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.attachment.MomentEntityAttachment;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.IAttachable;
import com.xiaohunao.heaven_destiny_moment.common.event.LivingAttackEvent;
import com.xiaohunao.heaven_destiny_moment.common.event.MomentEvent;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.function.Consumer;
import java.util.function.Function;

public class MobTeamTracker extends Tracker{
    public static final MapCodec<Tracker> CODEC = createCodec(tag -> new MobTeamTracker());
    @Override
    public void init() {
        addEvent(LivingChangeTargetEvent.class, this::onLivingAttack);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingChangeTargetEvent event) {
        LivingEntity attackEntity = event.getEntity();
        LivingEntity hurtEntity = event.getNewAboutToBeSetTarget();
        if (hurtEntity == null){
            return;
        }

        if (hurtEntity.level() instanceof ServerLevel && attackEntity != hurtEntity) {
            if (hurtEntity.hasData(HDMAttachments.MOMENT_ENTITY) && attackEntity.hasData(HDMAttachments.MOMENT_ENTITY)) {
                MomentEntityAttachment hurtEntityTeam = hurtEntity.getData(HDMAttachments.MOMENT_ENTITY);
                MomentEntityAttachment attackEntityTeam = attackEntity.getData(HDMAttachments.MOMENT_ENTITY);
                if (hurtEntityTeam.getMomentUid() == null){
                    return;
                }

                if (hurtEntityTeam.getMomentUid().equals(attackEntityTeam.getMomentUid())){
                    event.setCanceled(true);
                }
            }
        }
    }


    @Override
    public MapCodec<? extends ITracker> codec(){
        return HDMContextRegister.MOB_TEAM_TRACKER.get();
    }
}

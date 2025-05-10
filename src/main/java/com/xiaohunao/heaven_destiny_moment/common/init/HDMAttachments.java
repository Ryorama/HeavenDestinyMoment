package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.attachment.MomentEntityAttachment;
import com.xiaohunao.heaven_destiny_moment.common.attachment.MomentKillEntityRecorderAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class HDMAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, HeavenDestinyMoment.MODID);

    public static final Supplier<AttachmentType<MomentEntityAttachment>> MOMENT_ENTITY = TYPES.register("moment_entity",
            () -> AttachmentType.serializable(MomentEntityAttachment::new).copyOnDeath().build());

    public static final Supplier<AttachmentType<MomentKillEntityRecorderAttachment>> MOMENT_KILL_ENTITY_RECORDER = TYPES.register("moment_kill_entity_recorder",
            () -> AttachmentType.builder(MomentKillEntityRecorderAttachment::create).serialize(MomentKillEntityRecorderAttachment.CODEC).build());
}

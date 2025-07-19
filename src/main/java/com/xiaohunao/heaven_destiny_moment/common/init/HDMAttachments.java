package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;


public class HDMAttachments {
    public static final FlexibleRegister<AttachmentType<?>> ATTACHMENT_TYPES = FlexibleRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HeavenDestinyMoment.MODID);

    public static final FlexibleHolder<AttachmentType<?>, AttachmentType<KillEntityRecorderAttachment>> MOMENT_KILL_ENTITY_RECORDER = ATTACHMENT_TYPES.registerStatic("moment_kill_entity_recorder",
            () -> AttachmentType.builder(KillEntityRecorderAttachment::create).serialize(KillEntityRecorderAttachment.CODEC).build());
}

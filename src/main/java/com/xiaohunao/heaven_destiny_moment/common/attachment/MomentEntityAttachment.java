package com.xiaohunao.heaven_destiny_moment.common.attachment;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.UUID;

public class MomentEntityAttachment implements INBTSerializable<CompoundTag> {
    private final IAttachmentHolder attachmentHolder;
    private UUID momentUid;

    public MomentEntityAttachment(IAttachmentHolder attachmentHolder) {
        this.attachmentHolder = attachmentHolder;
    }

    @Override
    @UnknownNullability
    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();

        if (momentUid != null) {
            compoundTag.putUUID("momentUid", this.momentUid);
        }

        return compoundTag;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider,@NotNull CompoundTag compoundTag) {
        if (compoundTag.contains("momentUid")) {
            this.momentUid = compoundTag.getUUID("momentUid");
        }
    }

    public MomentEntityAttachment setUid(UUID momentUid) {
        this.momentUid = momentUid;
        return this;
    }

    public UUID getMomentUid() {
        return momentUid;
    }

    public Optional<MomentInstance<?>> getMomentInstance(Entity entity) {
        MomentManager momentManager = MomentManager.of(entity.level());
        if(momentUid == null){
            return Optional.empty();
        }
        return Optional.ofNullable(momentManager.getMomentInstance(momentUid));
    }
}

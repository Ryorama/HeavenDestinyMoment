package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public class MomentRunningRecord {
    public final MomentType<?> type;
    public final UUID uuid;
    public final long createTime;
    private long endTime = -1L;

    public MomentRunningRecord(MomentType<?> type, UUID uuid, long createTime) {
        this.type = type;
        this.uuid = uuid;
        this.createTime = createTime;
    }

    public MomentRunningRecord(MomentType<?> type, UUID uuid, long createTime, long endTime) {
        this.type = type;
        this.uuid = uuid;
        this.createTime = createTime;
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isFinished() {
        return endTime != -1L;
    }

    public long getDuration() {
        return isFinished() ? endTime - createTime : -1L;
    }


    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ResourceLocation typeKey = HDMRegistries.MOMENT_TYPE.getKey(type);
        if (typeKey != null) {
            tag.putString("momentType", typeKey.toString());
        }
        tag.putUUID("uuid", uuid); // 使用 putUUID 方法
        tag.putLong("createTime", createTime);
        tag.putLong("endTime", endTime);
        return tag;
    }

    public static MomentRunningRecord deserializeNBT(CompoundTag tag) {
        try {
            ResourceLocation typeLocation = ResourceLocation.tryParse(tag.getString("momentType"));
            if (typeLocation == null) {
                throw new IllegalArgumentException("Invalid moment type resource location");
            }

            MomentType<?> momentType = HDMRegistries.MOMENT_TYPE.get(typeLocation);
            if (momentType == null) {
                throw new IllegalArgumentException("Unknown moment type: " + typeLocation);
            }

            UUID uuid = tag.getUUID("uuid");
            long createTime = tag.getLong("createTime");
            long endTime = tag.getLong("endTime");

            return new MomentRunningRecord(momentType, uuid, createTime, endTime);
        } catch (Exception e) {
            // 记录错误日志
            throw new RuntimeException("Failed to deserialize MomentRunningRecord", e);
        }
    }
}

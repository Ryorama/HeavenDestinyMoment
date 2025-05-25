package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ISerializableTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.resources.ResourceLocation;

public abstract class PhaseTrigger implements ISerializableTrigger {
    private ResourceLocation phase;
    public PhaseTrigger(ResourceLocation phase){
        this.phase = phase;
    }

    protected ResourceLocation getPhase() {
        return phase;
    }

    public static PhaseTrigger ofAdd(ResourceLocation phase){
        return new Add(phase);
    }

    public static PhaseTrigger ofRemove(ResourceLocation phase){
        return new Remove(phase);
    }

    public static class Add extends PhaseTrigger {
        public static final MapCodec<Add> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("phase").forGetter(Add::getPhase)
        ).apply(instance, Add::new));

        public Add(ResourceLocation phase) {
            super(phase);
        }
    }
    public static class Remove extends PhaseTrigger{
        public static final MapCodec<Remove> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("phase").forGetter(Remove::getPhase)
        ).apply(instance, Remove::new));

        public Remove(ResourceLocation phase) {
            super(phase);
        }
    }
}

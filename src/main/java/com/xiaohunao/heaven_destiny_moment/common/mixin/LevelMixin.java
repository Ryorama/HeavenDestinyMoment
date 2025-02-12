package com.xiaohunao.heaven_destiny_moment.common.mixin;


import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public class LevelMixin implements MomentManagerMixed {
    @Unique
    private final MomentManager heaven_destiny_moment$momentManager = new MomentManager((Level)(Object)this);


    public MomentManager heaven_destiny_moment$getMomentManager() {
        return heaven_destiny_moment$momentManager;
    }

}

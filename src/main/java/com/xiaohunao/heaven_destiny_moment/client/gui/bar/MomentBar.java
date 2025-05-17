package com.xiaohunao.heaven_destiny_moment.client.gui.bar;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class MomentBar {
    public static final Codec<MomentBar> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(MomentBar::getID),
            HDMRegistries.BAR_RENDER_TYPE.byNameCodec().fieldOf("type").forGetter(MomentBar::getType),
            Codec.FLOAT.fieldOf("progress").forGetter(MomentBar::getProgress),
            Codec.STRING.xmap(BossEvent.BossBarColor::valueOf, BossEvent.BossBarColor::name).fieldOf("color").forGetter(MomentBar::getColor)

    ).apply(inst, MomentBar::new));


    private final UUID uuid;
    private final IBarRenderType type;
    private final Set<Player> players = Sets.newHashSet();
    private float progress = 1.0F;
    private BossEvent.BossBarColor color = BossEvent.BossBarColor.YELLOW;


    public MomentBar(UUID uuid, IBarRenderType type) {
        this.uuid = uuid;
        this.type = type;
    }

    public MomentBar(UUID uuid, IBarRenderType type, float progress, BossEvent.BossBarColor color) {
        this.uuid = uuid;
        this.type = type;
        this.progress = progress;
        this.color = color;
    }

    public void updateProgress(float progress) {
        if (progress != this.progress) {
            this.progress = progress;
            broadcast(MomentBarSyncPayload::updateProgress);
        }
    }

    public void addPlayer(Player player) {
        if (this.players.add(player)) {
            broadcast(MomentBarSyncPayload::addPlayer);
        }
    }

    public void removePlayer(Player player) {
        if (this.players.remove(player)) {
            broadcast(MomentBarSyncPayload::removePlayer);
        }
    }

    public UUID getID() {
        return uuid;
    }

    public IBarRenderType getType() {
        return type;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public float getProgress() {
        return progress;
    }

    public BossEvent.BossBarColor getColor() {
        return color;
    }

    public void broadcast(Function<MomentBar, MomentBarSyncPayload> function) {
        if (!this.players.isEmpty()) {
            MomentBarSyncPayload packet = function.apply(this);
            PacketDistributor.sendToAllPlayers(packet);
        }
    }
}

package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClearClientCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("clear_only_client")
                .executes(ClearClientCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        
        // 发送清理客户端时刻的网络包
        if (player != null) {
            PacketDistributor.sendToPlayer(player,ClientOnlyMomentSyncPayload.clearClientMoments(player.getUUID()));
            source.sendSuccess(() -> Component.translatable("commands.moment.clear_only_client.success"), false);
        }
        
        return 1;
    }
} 
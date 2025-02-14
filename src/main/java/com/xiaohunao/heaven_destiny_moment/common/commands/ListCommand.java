package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import java.util.Collection;

public class ListCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(){
        return Commands.literal("list")
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var manager = MomentManager.of(source.getLevel());
                    var moments = manager.getMomentInstances();
                    return list(ctx, moments);
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            var source = ctx.getSource();
                            var manager = MomentManager.of(source.getLevel());
                            var player = EntityArgument.getPlayer(ctx, "player");
                            var moments = manager.getPlayerMoments(player);
                            return list(ctx, moments);
                        }));
    }

    private static int list(CommandContext<CommandSourceStack> ctx, Collection<MomentInstance<?>> momentInstances) {
        var source = ctx.getSource();
        ServerLevel level = source.getLevel();
        if (momentInstances.isEmpty()) {
            source.sendFailure(Component.translatable("commands.moment.list.empty"));
            return 0;
        }

        try {
            // 尝试获取玩家参数，如果存在则使用玩家header
            var player = EntityArgument.getPlayer(ctx, "player");
            source.sendSuccess(() -> Component.translatable("commands.moment.list.player_header", 
                    player.getDisplayName(), momentInstances.size()), false);
        } catch (Exception e) {
            // 如果获取玩家参数失败，说明是查看所有时刻，使用普通header
            source.sendSuccess(() -> Component.translatable("commands.moment.list.header", momentInstances.size()), false);
        }

        for (var momentInstance : momentInstances) {
            Registry<Moment<?>> momentRegistry = level.registryAccess().registryOrThrow(HDMRegistries.Keys.MOMENT);

            MutableComponent message = Component.literal("- ")
                    .append(Component.translatable(momentRegistry.getKey(momentInstance.moment().get()).toLanguageKey()))
                    .append(" (")
                    .append(Component.literal(momentInstance.getID().toString()).withStyle(ChatFormatting.GRAY))
                    .append(")");
            source.sendSuccess(() -> message, false);
        }
        
        return momentInstances.size();
    }
}

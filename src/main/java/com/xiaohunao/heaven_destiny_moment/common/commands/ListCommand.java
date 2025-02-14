package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.Collection;
import javax.annotation.Nullable;

public class ListCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list")
                .executes(ListCommand::listAll)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ListCommand::listPlayer));
    }

    private static int listAll(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var manager = MomentManager.of(source.getLevel());
        return list(ctx, manager.getMomentInstances(), null);
    }

    private static int listPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        var manager = MomentManager.of(source.getLevel());
        var player = EntityArgument.getPlayer(ctx, "player");
        return list(ctx, manager.getPlayerMoments(player), player);
    }

    private static int list(CommandContext<CommandSourceStack> ctx, Collection<MomentInstance<?>> moments, @Nullable ServerPlayer player) {
        var source = ctx.getSource();
        ServerLevel level = source.getLevel();
        
        if (moments.isEmpty()) {
            source.sendFailure(Component.translatable("commands.moment.list.empty"));
            return 0;
        }

        if (player != null) {
            source.sendSuccess(() -> Component.translatable("commands.moment.list.player_header", 
                    player.getDisplayName(), moments.size()), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.moment.list.header", moments.size()), false);
        }

        moments.forEach(moment -> 
            source.sendSuccess(() -> MomentCommand.formatMomentInfo(moment, level), false)
        );
        
        return moments.size();
    }
}

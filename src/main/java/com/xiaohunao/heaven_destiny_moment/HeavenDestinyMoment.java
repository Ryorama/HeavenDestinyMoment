package com.xiaohunao.heaven_destiny_moment;

import com.mojang.brigadier.CommandDispatcher;
import com.xiaohunao.heaven_destiny_moment.client.gui.hud.MomentBarOverlay;
import com.xiaohunao.heaven_destiny_moment.common.commands.MomentCommand;
import com.xiaohunao.heaven_destiny_moment.common.init.*;
import com.xiaohunao.heaven_destiny_moment.compat.LoadedCompat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(HeavenDestinyMoment.MODID)
public class HeavenDestinyMoment {
    public static final String MODID = "heaven_destiny_moment";
    public static final Logger LOGGER = LoggerFactory.getLogger("HeavenDestinyMoment");

    public HeavenDestinyMoment(IEventBus modEventBus, ModContainer modContainer) {
        HDMTriggerTypes.TRIGGER_TYPE.register(modEventBus);
        HDMMomentTypes.MOMENT_TYPE.register(modEventBus);
        HDMBarRenderTypes.BAR_RENDER_TYPE.register(modEventBus);
        HDMScalingFunctions.PLAYER_COUNT_SCALING.register(modEventBus);
        HDMScalingFunctions.DIFFICULTY_SCALING.register(modEventBus);
        HDMConditions.CONDITION_CODEC.register(modEventBus);
        HDMAttachments.ATTACHMENT_TYPES.register(modEventBus);

        HDMActuators.ACTUATOR_CODEC.register(modEventBus);
        HDMMomentRegister.register(modEventBus);
        HDMContextRegister.register(modEventBus);
        LoadedCompat.register(modEventBus);

        modEventBus.addListener(HDMRegistries::registerRegistries);
        modEventBus.addListener(this::onFMLCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public void onFMLCommonSetup(FMLCommonSetupEvent event) {
//        NeoForge.EVENT_BUS.start();
//        NeoForge.EVENT_BUS.post(new RegisterCallbackEvent());
    }

    public static String asDescriptionId(String path) {
        return MODID + "." + path;
    }

    public static <T> ResourceKey<T> asResourceKey(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, HeavenDestinyMoment.asResource(path));
    }

    public static <T> ResourceKey<Registry<T>> asResourceKey(String path) {
        return ResourceKey.createRegistryKey(HeavenDestinyMoment.asResource(path));
    }

    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        MomentCommand.register(dispatcher);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerOverlay(RegisterGuiLayersEvent event) {
            event.registerAboveAll(HeavenDestinyMoment.asResource("moment_bar"), new MomentBarOverlay());
        }
    }
}

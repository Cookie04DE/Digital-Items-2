package com.example.digital_items_2;

import com.mojang.logging.LogUtils;
import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DigitalItems2.MODID)
public class DigitalItems2 {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "digital_items_2";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public DigitalItems2()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        Registration.register(modEventBus);

        ComputerCraftAPI.registerPeripheralProvider((level, pos, side) -> {
            BlockEntity entity = level.getBlockEntity(pos);
            if(!(entity instanceof DigitizerBlockEntity blockEntity)) {
                return LazyOptional.empty();
            }
            return LazyOptional.of(() -> new DigitizerPeripheral(blockEntity));
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.spec);
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save levelEvent) {
        if(!(levelEvent.getLevel() instanceof ServerLevel sl)) {
            return;
        }
        if(sl != sl.getServer().overworld()) {
            return;
        }
        DigitalItemsSavedData.getFrom(sl).prune(sl);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MenuScreens.register(Registration.DIGITIZER_MENU.get(), DigitizerScreen::new);
        }
    }
}

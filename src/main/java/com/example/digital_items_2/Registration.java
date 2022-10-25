package com.example.digital_items_2;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DigitalItems2.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DigitalItems2.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, DigitalItems2.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DigitalItems2.MODID);


    public static final RegistryObject<Block> DIGITIZER_BLOCK = BLOCKS.register("digitizer", () -> new Digitizer(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<BlockEntityType<DigitizerBlockEntity>> DIGITIZER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("digitizer", () ->
                    BlockEntityType.Builder.of(DigitizerBlockEntity::new, DIGITIZER_BLOCK.get()).build(null));
    public static final RegistryObject<Item> DIGITIZER_BLOCK_ITEM = ITEMS.register("digitizer", () -> new BlockItem(DIGITIZER_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
    public static final RegistryObject<MenuType<DigitizerMenu>> DIGITIZER_MENU = MENUS.register("digitizer", () -> IForgeMenuType.create(DigitizerMenu::new));

    public static void register(IEventBus e) {
        BLOCKS.register(e);
        ITEMS.register(e);
        MENUS.register(e);
        BLOCK_ENTITIES.register(e);
    }
}

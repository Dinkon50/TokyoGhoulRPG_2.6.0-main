package com.tokyoghoul.rpg.init;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TokyoGhoulRPG.MODID);

    public static final RegistryObject<CreativeModeTab> INJECTIONS = TABS.register("injections", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.injections"))
            .icon(() -> new ItemStack(ModItems.HUMANIZING_INJECTION.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.RC_INJECTOR.get());
                output.accept(ModItems.HUMANIZING_INJECTION.get());
            })
            .build()
    );


    public static final RegistryObject<CreativeModeTab> QUINQUES = TABS.register("quinques", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.tokyoghoulrpg.quinques"))
            .icon(() -> new ItemStack(ModItems.QUINQUE_CRIMSON.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.QUINQUE_CRIMSON.get());
                output.accept(ModItems.QUINQUE_VENOM.get());
                output.accept(ModItems.QUINQUE_FROST.get());
                output.accept(ModItems.QUINQUE_BLIND.get());
                output.accept(ModItems.QUINQUE_INFERNO.get());
                output.accept(ModItems.QUINQUE_TITAN.get());
                output.accept(ModItems.QUINQUE_RAPIER.get());
                output.accept(ModItems.QUINQUE_HARPOON.get());
                output.accept(ModItems.QUINQUE_SCYTHE.get());
                output.accept(ModItems.QUINQUE_TWIN.get());
                output.accept(ModItems.QUINQUE_HAMMER.get());
                output.accept(ModItems.QUINQUE_CHAIN.get());
                output.accept(ModItems.QUINQUE_LANCE.get());
                output.accept(ModItems.QUINQUE_CLAW.get());
                output.accept(ModItems.QUINQUE_CANNON.get());
                output.accept(ModItems.QUINQUE_MANTIS.get());
                output.accept(ModItems.QUINQUE_GLAIVE.get());
                output.accept(ModItems.QUINQUE_EDGE.get());
                output.accept(ModItems.QUINQUE_GUARD.get());
                output.accept(ModItems.QUINQUE_VOID.get());
            })
            .build()
    );

    private ModCreativeTabs() {}
}

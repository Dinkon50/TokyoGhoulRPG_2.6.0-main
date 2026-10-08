package com.tokyoghoul.rpg.init;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.entity.ModEntities;
import com.tokyoghoul.rpg.item.GhoulCoatItem;
import com.tokyoghoul.rpg.item.HalfGhoulOrganItem;
import com.tokyoghoul.rpg.item.HumanizingInjectionItem;
import com.tokyoghoul.rpg.item.KaguneItem;
import com.tokyoghoul.rpg.item.RCInjectorItem;
import com.tokyoghoul.rpg.item.RoleSpawnEggItem;
import com.tokyoghoul.rpg.item.QuinqueItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TokyoGhoulRPG.MODID);

    public static final RegistryObject<Item> RC_CELL = ITEMS.register("rc_cell",()->new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> RC_INJECTOR = ITEMS.register("rc_injector",()->new RCInjectorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HUMANIZING_INJECTION = ITEMS.register("humanizing_injection",()->new HumanizingInjectionItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HALF_GHOUL_ORGAN = ITEMS.register("half_ghoul_organ",()->new HalfGhoulOrganItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SKILL_TOKEN = ITEMS.register("skill_token",()->new Item(new Item.Properties().stacksTo(64)));

    // Clothing / masks. Vanilla armor renderer supplies the equipped 3D silhouette; textures are custom.
    public static final RegistryObject<Item> GHOUL_MASK = ITEMS.register("ghoul_mask",()->new ArmorItem(TokyoArmorMaterials.GHOUL, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_COAT = ITEMS.register("ghoul_coat",()->new ArmorItem(TokyoArmorMaterials.GHOUL,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_PANTS = ITEMS.register("ghoul_pants",()->new ArmorItem(TokyoArmorMaterials.GHOUL,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_BOOTS = ITEMS.register("ghoul_boots",()->new ArmorItem(TokyoArmorMaterials.GHOUL,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> INVESTIGATOR_COAT = ITEMS.register("investigator_coat",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> CCG_HELMET = ITEMS.register("ccg_helmet",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> CCG_LEGS = ITEMS.register("ccg_legs",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> CCG_BOOTS = ITEMS.register("ccg_boots",()->new ArmorItem(TokyoArmorMaterials.CCG,ArmorItem.Type.BOOTS,new Item.Properties()));

    // Kaneki-inspired one-eyed ghoul outfit. It is an original fan-made texture set, not a ripped anime asset.
    public static final RegistryObject<Item> KANEKI_MASK = ITEMS.register("kaneki_mask",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> KANEKI_COAT = ITEMS.register("kaneki_coat",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> KANEKI_PANTS = ITEMS.register("kaneki_pants",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> KANEKI_BOOTS = ITEMS.register("kaneki_boots",()->new ArmorItem(TokyoArmorMaterials.KANEKI,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> QUINQUE_BLADE = ITEMS.register("quinque_blade",()->new QuinqueItem(4, QuinqueItem.Effect.NONE, 0.45f, -2.1f));
    public static final RegistryObject<Item> CCG_MASTER_QUINQUE = ITEMS.register("ccg_master_quinque",()->new QuinqueItem(9, QuinqueItem.Effect.CRIT, 0.45f, -2.25f));
    public static final RegistryObject<Item> BOSS_QUINQUE = ITEMS.register("boss_quinque",()->new QuinqueItem(13, QuinqueItem.Effect.CRIT, 0.45f, -2.05f));

    public static final RegistryObject<Item> QUINQUE_CRIMSON = ITEMS.register("quinque_crimson",()->new QuinqueItem(8, QuinqueItem.Effect.BLEED, 0.45f, -2.15f));
    public static final RegistryObject<Item> QUINQUE_VENOM = ITEMS.register("quinque_venom",()->new QuinqueItem(7, QuinqueItem.Effect.POISON, 0.45f, -2.05f));
    public static final RegistryObject<Item> QUINQUE_FROST = ITEMS.register("quinque_frost",()->new QuinqueItem(7, QuinqueItem.Effect.SLOW, 0.45f, -2.20f));
    public static final RegistryObject<Item> QUINQUE_BLIND = ITEMS.register("quinque_blind",()->new QuinqueItem(6, QuinqueItem.Effect.BLIND, 0.45f, -2.00f));
    public static final RegistryObject<Item> QUINQUE_INFERNO = ITEMS.register("quinque_inferno",()->new QuinqueItem(9, QuinqueItem.Effect.BURN, 0.45f, -2.10f));
    public static final RegistryObject<Item> QUINQUE_TITAN = ITEMS.register("quinque_titan",()->new QuinqueItem(11, QuinqueItem.Effect.KNOCK, 0.45f, -2.35f));
    public static final RegistryObject<Item> QUINQUE_RAPIER = ITEMS.register("quinque_rapier",()->new QuinqueItem(6, QuinqueItem.Effect.CRIT, 0.45f, -1.75f));
    public static final RegistryObject<Item> QUINQUE_HARPOON = ITEMS.register("quinque_harpoon",()->new QuinqueItem(7, QuinqueItem.Effect.LONG, 0.45f, -2.00f));
    public static final RegistryObject<Item> QUINQUE_SCYTHE = ITEMS.register("quinque_scythe",()->new QuinqueItem(10, QuinqueItem.Effect.BLEED, 0.45f, -2.35f));
    public static final RegistryObject<Item> QUINQUE_TWIN = ITEMS.register("quinque_twin",()->new QuinqueItem(5, QuinqueItem.Effect.CRIT, 0.45f, -1.55f));
    public static final RegistryObject<Item> QUINQUE_HAMMER = ITEMS.register("quinque_hammer",()->new QuinqueItem(12, QuinqueItem.Effect.KNOCK, 0.45f, -2.55f));
    public static final RegistryObject<Item> QUINQUE_CHAIN = ITEMS.register("quinque_chain",()->new QuinqueItem(7, QuinqueItem.Effect.SLOW, 0.45f, -1.95f));
    public static final RegistryObject<Item> QUINQUE_LANCE = ITEMS.register("quinque_lance",()->new QuinqueItem(9, QuinqueItem.Effect.LONG, 0.45f, -2.15f));
    public static final RegistryObject<Item> QUINQUE_CLAW = ITEMS.register("quinque_claw",()->new QuinqueItem(8, QuinqueItem.Effect.BLEED, 0.45f, -1.80f));
    public static final RegistryObject<Item> QUINQUE_CANNON = ITEMS.register("quinque_cannon",()->new QuinqueItem(10, QuinqueItem.Effect.BURN, 0.45f, -2.45f));
    public static final RegistryObject<Item> QUINQUE_MANTIS = ITEMS.register("quinque_mantis",()->new QuinqueItem(8, QuinqueItem.Effect.POISON, 0.45f, -1.90f));
    public static final RegistryObject<Item> QUINQUE_GLAIVE = ITEMS.register("quinque_glaive",()->new QuinqueItem(9, QuinqueItem.Effect.KNOCK, 0.45f, -2.25f));
    public static final RegistryObject<Item> QUINQUE_EDGE = ITEMS.register("quinque_edge",()->new QuinqueItem(8, QuinqueItem.Effect.BLIND, 0.45f, -1.85f));
    public static final RegistryObject<Item> QUINQUE_GUARD = ITEMS.register("quinque_guard",()->new QuinqueItem(5, QuinqueItem.Effect.SLOW, 0.45f, -2.00f));
    public static final RegistryObject<Item> QUINQUE_VOID = ITEMS.register("quinque_void",()->new QuinqueItem(12, QuinqueItem.Effect.CRIT, 0.45f, -2.30f));

    public static final RegistryObject<Item> GHOUL_SPAWN_EGG = ITEMS.register("ghoul_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.GHOUL_NPC,0x5B0A12,0xD7193F,new Item.Properties()));
    public static final RegistryObject<Item> CCG_SPAWN_EGG = ITEMS.register("ccg_spawn_egg",()->new ForgeSpawnEggItem(ModEntities.CCG_NPC,0x173B5E,0x55B7E8,new Item.Properties()));

    // Role eggs: same entity logic, different 3D/texture role in the renderer.
    public static final RegistryObject<Item> GHOUL_REFUGE_EGG = ITEMS.register("ghoul_refuge_spawn_egg",()->new RoleSpawnEggItem(ModEntities.GHOUL_NPC,"refuge",new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_MASKMAKER_EGG = ITEMS.register("ghoul_maskmaker_spawn_egg",()->new RoleSpawnEggItem(ModEntities.GHOUL_NPC,"maskmaker",new Item.Properties()));
    public static final RegistryObject<Item> GHOUL_ELITE_EGG = ITEMS.register("ghoul_elite_spawn_egg",()->new RoleSpawnEggItem(ModEntities.GHOUL_NPC,"elite",new Item.Properties()));
    public static final RegistryObject<Item> CCG_MEDIC_EGG = ITEMS.register("ccg_medic_spawn_egg",()->new RoleSpawnEggItem(ModEntities.CCG_NPC,"medic",new Item.Properties()));
    public static final RegistryObject<Item> CCG_HUNTER_EGG = ITEMS.register("ccg_hunter_spawn_egg",()->new RoleSpawnEggItem(ModEntities.CCG_NPC,"hunter",new Item.Properties()));
    public static final RegistryObject<Item> CCG_ELITE_EGG = ITEMS.register("ccg_elite_spawn_egg",()->new RoleSpawnEggItem(ModEntities.CCG_NPC,"elite",new Item.Properties()));

    private ModItems(){}
}

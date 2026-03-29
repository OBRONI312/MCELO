package me.yourname.eloplugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.block.ShulkerBox;

public class DefaultKitProvider {

    public static ItemStack[] getDefaultKit(String kitName) {
        switch (kitName.toLowerCase()) {
            case "vanilla": return getVanillaKit();
            case "uhc": return getUHCKit();
            case "pot": return getPotKit();
            case "nethop": return getNethOPKit();
            case "mace": return getMaceKit();
            case "smp": return getSMPKit();
            case "sword": return getSwordKit();
            case "axe": return getAxeKit();
            case "lifesteal": return getLifestealKit();
            case "spear-mace": return getSpearMaceKit();
            case "cartpvp": return getCartPvPKit();
            default: return new ItemStack[41];
        }
    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getVanillaKit() {
        ItemStack[] items = new ItemStack[41];
        
        // --- Hotbar ---
        // 0: Netherite Sword (Sharp 5, Unb 3, Mend, KB 1)
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta mSword = sword.getItemMeta();
        if (mSword != null) {
            applySafeEnchants(mSword, "sharpness", 5, "unbreaking", 3, "mending", 1, "knockback", 1);
            sword.setItemMeta(mSword);
        }
        items[0] = sword;

        // 1: End Crystals
        items[1] = new ItemStack(Material.END_CRYSTAL, 64);

        // 2: Obsidian
        items[2] = new ItemStack(Material.OBSIDIAN, 64);

        // 3: Mace (Density 5, Unb 3, Mend)
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta mMace = mace.getItemMeta();
        if (mMace != null) {
            applySafeEnchants(mMace, "density", 5, "unbreaking", 3, "mending", 1);
            mace.setItemMeta(mMace);
        }
        items[3] = mace;

        // 4: Pickaxe (Eff 5, Unb 3, Mend)
        ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta mPick = pick.getItemMeta();
        if (mPick != null) {
            applySafeEnchants(mPick, "efficiency", 5, "unbreaking", 3, "mending", 1);
            pick.setItemMeta(mPick);
        }
        items[4] = pick;

        // 5: Axe (Sharp 5, Unb 3, Mend)
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta mAxe = axe.getItemMeta();
        if (mAxe != null) {
            applySafeEnchants(mAxe, "sharpness", 5, "unbreaking", 3, "mending", 1);
            axe.setItemMeta(mAxe);
        }
        items[5] = axe;

        // 6: Crossbow (Quick Charge 3, Unb 3, Mend, Piercing 4)
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta mCross = crossbow.getItemMeta();
        if (mCross != null) {
            applySafeEnchants(mCross, "quick_charge", 3, "unbreaking", 3, "mending", 1, "piercing", 4);
            crossbow.setItemMeta(mCross);
        }
        items[6] = crossbow;

        // 7: Ender Pearls
        items[7] = new ItemStack(Material.ENDER_PEARL, 16);

        // 8: Golden Apples
        items[8] = new ItemStack(Material.GOLDEN_APPLE, 64);

        // --- Inventory ---
        // 9: Elytra (Unb 3, Mend)
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta mElytra = elytra.getItemMeta();
        if (mElytra != null) {
            applySafeEnchants(mElytra, "unbreaking", 3, "mending", 1);
            elytra.setItemMeta(mElytra);
        }
        items[9] = elytra;

        items[10] = new ItemStack(Material.FIREWORK_ROCKET, 64);
        items[11] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        items[12] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        items[13] = new ItemStack(Material.OBSIDIAN, 64); // 2nd stack
        items[14] = new ItemStack(Material.END_CRYSTAL, 64); // 2nd stack
        items[15] = new ItemStack(Material.ENDER_PEARL, 16); // 2nd stack
        items[16] = new ItemStack(Material.RESPAWN_ANCHOR, 64);
        items[17] = new ItemStack(Material.GLOWSTONE, 64);
        items[18] = new ItemStack(Material.SHIELD);
        
        // Slow Falling Arrows (30s)
        ItemStack arrows = new ItemStack(Material.TIPPED_ARROW, 64);
        PotionMeta arrowMeta = (PotionMeta) arrows.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setBasePotionType(PotionType.LONG_SLOW_FALLING);
            arrows.setItemMeta(arrowMeta);
        }
        items[19] = arrows;

        // Fill remainder with Totems (20-35)
        for (int i = 20; i < 36; i++) {
            items[i] = new ItemStack(Material.TOTEM_OF_UNDYING);
        }

        // --- Armor ---
        // Boots: Blast Prot 4, Unb 3, Mend, FF 4, Depth 3, Soul Speed 3
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta mBoots = boots.getItemMeta();
        if (mBoots != null) {
            applySafeEnchants(mBoots, "blast_protection", 4, "unbreaking", 3, "mending", 1, "feather_falling", 4, "depth_strider", 3, "soul_speed", 3);
            boots.setItemMeta(mBoots);
        }
        items[36] = boots;

        // Leggings: Blast Prot 4, Unb 3, Mend, Swift Sneak 3
        ItemStack legs = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemMeta mLegs = legs.getItemMeta();
        if (mLegs != null) {
            applySafeEnchants(mLegs, "blast_protection", 4, "unbreaking", 3, "mending", 1, "swift_sneak", 3);
            legs.setItemMeta(mLegs);
        }
        items[37] = legs;

        // Chestplate: Prot 4, Unb 3, Mend
        ItemStack chest = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta mChest = chest.getItemMeta();
        if (mChest != null) {
            applySafeEnchants(mChest, "protection", 4, "unbreaking", 3, "mending", 1);
            chest.setItemMeta(mChest);
        }
        items[38] = chest;

        // Helmet: Prot 4, Unb 3, Mend, Resp 3, Aqua
        ItemStack helm = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta mHelm = helm.getItemMeta();
        if (mHelm != null) {
            applySafeEnchants(mHelm, "protection", 4, "unbreaking", 3, "mending", 1, "respiration", 3, "aqua_affinity", 1);
            helm.setItemMeta(mHelm);
        }
        items[39] = helm;
        
        // Offhand
        items[40] = new ItemStack(Material.TOTEM_OF_UNDYING);

        return items;
    }

    private static ItemStack[] getUHCKit() {
        ItemStack[] items = new ItemStack[41];
        // Hotbar
        items[0] = createItem(Material.DIAMOND_SWORD, null, "sharpness", 4);
        items[1] = createItem(Material.DIAMOND_AXE, null, "sharpness", 1);
        items[2] = createItem(Material.BOW, null, "power", 1);
        items[3] = createItem(Material.CROSSBOW, null, "piercing", 1);
        
        // Pick with Unbreaking
        ItemStack pick = createItem(Material.DIAMOND_PICKAXE, null, "efficiency", 3);
        ItemMeta pm = pick.getItemMeta();
        if (pm != null) {
            applySafeEnchants(pm, "unbreaking", 3);
            pick.setItemMeta(pm);
        }
        items[4] = pick;

        items[5] = new ItemStack(Material.WATER_BUCKET);
        items[6] = new ItemStack(Material.LAVA_BUCKET);
        items[7] = new ItemStack(Material.COBBLESTONE, 64);
        items[8] = new ItemStack(Material.OAK_PLANKS, 64);

        // Inventory: Ammo & Food
        items[9] = new ItemStack(Material.ARROW, 64);
        items[10] = new ItemStack(Material.GOLDEN_APPLE, 7);
        
        // Golden Heads (Renamed Player Heads)
        ItemStack gHead = new ItemStack(Material.PLAYER_HEAD, 2);
        ItemMeta gHeadMeta = gHead.getItemMeta();
        if (gHeadMeta != null) {
            gHeadMeta.setDisplayName("§6Golden Head");
            gHead.setItemMeta(gHeadMeta);
        }
        items[11] = gHead;
        
        items[12] = new ItemStack(Material.COBWEB, 8);

        // Extras
        items[14] = new ItemStack(Material.WATER_BUCKET);
        items[15] = new ItemStack(Material.WATER_BUCKET);
        items[16] = new ItemStack(Material.WATER_BUCKET);
        items[17] = new ItemStack(Material.LAVA_BUCKET);
        items[18] = new ItemStack(Material.COBBLESTONE, 64);
        items[19] = new ItemStack(Material.OAK_PLANKS, 64);
        items[20] = new ItemStack(Material.SHIELD); // Spare Shield
        
        // Armor
        items[36] = createItem(Material.DIAMOND_BOOTS, null, "protection", 3);
        items[37] = createItem(Material.DIAMOND_LEGGINGS, null, "protection", 2); // Prot 2
        items[38] = createItem(Material.DIAMOND_CHESTPLATE, null, "protection", 3);
        items[39] = createItem(Material.DIAMOND_HELMET, null, "protection", 3);
        items[40] = new ItemStack(Material.SHIELD);
        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getPotKit() {
        // Pot: NethOP but diamond armor/sword, no enchanting bottles, steak, 4 str/speed
        ItemStack[] items = new ItemStack[41];

        // Diamond Sword (Sharp 5)
        items[0] = createItem(Material.DIAMOND_SWORD, null, "sharpness", 5);
        items[1] = new ItemStack(Material.ENDER_PEARL, 16);

        // Strength and Speed Potions
        for (int i = 2; i < 6; i++) items[i] = createPotion(PotionType.STRONG_STRENGTH, true);
        for (int i = 6; i < 10; i++) items[i] = createPotion(PotionType.STRONG_SWIFTNESS, true);

        // Fill with Instant Health II
        for (int i = 10; i < 36; i++) items[i] = createPotion(PotionType.STRONG_HEALING, true);

        // Steak in offhand
        items[40] = new ItemStack(Material.COOKED_BEEF, 64);

        // Diamond Armor (Prot 4)
        // Diamond Armor (Prot 4)
        
        items[36] = createItem(Material.DIAMOND_BOOTS, "§bDiamond Boots", "protection", 4);
        items[37] = createItem(Material.DIAMOND_LEGGINGS, "§bDiamond Leggings", "protection", 4);
        items[38] = createItem(Material.DIAMOND_CHESTPLATE, "§bDiamond Chestplate", "protection", 4);
        items[39] = createItem(Material.DIAMOND_HELMET, "§bDiamond Helmet", "protection", 4);
        return items;

    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getNethOPKit() {
        ItemStack[] items = new ItemStack[41];
        // Sword: Sharp 5, Unb 3, Mending
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta sMeta = sword.getItemMeta();
        if (sMeta != null) {
            applySafeEnchants(sMeta, "sharpness", 5, "unbreaking", 3, "mending", 1);
            sword.setItemMeta(sMeta);
        }
        items[0] = sword;
        
        items[1] = new ItemStack(Material.TOTEM_OF_UNDYING);
        items[2] = new ItemStack(Material.TOTEM_OF_UNDYING);
        items[3] = new ItemStack(Material.TOTEM_OF_UNDYING);
        items[4] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        items[5] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        
        for (int i = 6; i < 12; i++) items[i] = createPotion(PotionType.STRONG_STRENGTH, true);
        for (int i = 12; i < 18; i++) items[i] = createPotion(PotionType.STRONG_SWIFTNESS, true);
        for (int i = 18; i < 36; i++) items[i] = createPotion(PotionType.STRONG_HEALING, true);
        
        items[36] = createSMPArmor(Material.NETHERITE_BOOTS);
        items[37] = createSMPArmor(Material.NETHERITE_LEGGINGS);
        items[38] = createSMPArmor(Material.NETHERITE_CHESTPLATE);
        items[39] = createSMPArmor(Material.NETHERITE_HELMET);
        items[40] = new ItemStack(Material.GOLDEN_APPLE, 64);
        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getMaceKit() {
        ItemStack[] items = new ItemStack[41];
        
        // --- Weapons ---
        // Mace 1: Breach 4, Unb 3, Mending
        ItemStack mace1 = new ItemStack(Material.MACE);
        ItemMeta m1 = mace1.getItemMeta();
        if (m1 != null) {
            applySafeEnchants(m1, "breach", 4, "unbreaking", 3, "mending", 1);
            mace1.setItemMeta(m1);
        }
        items[0] = mace1;

        // Mace 2: Density 5, Unb 3, Mending, Wind Burst 3
        ItemStack mace2 = new ItemStack(Material.MACE);
        ItemMeta m2 = mace2.getItemMeta();
        if (m2 != null) {
            applySafeEnchants(m2, "density", 5, "unbreaking", 3, "mending", 1, "wind_burst", 3);
            mace2.setItemMeta(m2);
        }
        items[1] = mace2;

        // Sword: Sharp 5, Unb 3, Mending, Knockback 1
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta mSword = sword.getItemMeta();
        if (mSword != null) {
            applySafeEnchants(mSword, "sharpness", 5, "unbreaking", 3, "mending", 1, "knockback", 1);
            sword.setItemMeta(mSword);
        }
        items[2] = sword;

        // Axe: Sharp 5, Unb 3, Mending, Eff 5
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta mAxe = axe.getItemMeta();
        if (mAxe != null) {
            applySafeEnchants(mAxe, "sharpness", 5, "unbreaking", 3, "mending", 1, "efficiency", 5);
            axe.setItemMeta(mAxe);
        }
        items[3] = axe;

        // --- Hotbar & Inventory Items ---
        items[4] = new ItemStack(Material.WIND_CHARGE, 64);
        items[5] = new ItemStack(Material.ENDER_PEARL, 16);
        items[6] = createPotion(PotionType.STRONG_STRENGTH, true);
        items[7] = new ItemStack(Material.GOLDEN_APPLE, 64);
        
        // Shield: Unbreaking 2
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta mShield = shield.getItemMeta();
        if (mShield != null) {
            applySafeEnchants(mShield, "unbreaking", 2);
            shield.setItemMeta(mShield);
        }
        items[8] = shield;

        // Elytra: Unenchanted, 150 durability remaining (Max 432 - 150 = 282 damage)
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        org.bukkit.inventory.meta.Damageable dElytra = (org.bukkit.inventory.meta.Damageable) elytra.getItemMeta();
        if (dElytra != null) {
            dElytra.setDamage(282);
            elytra.setItemMeta(dElytra);
        }
        items[9] = elytra;
        
        items[10] = new ItemStack(Material.TOTEM_OF_UNDYING); // 2nd Totem

        // 3 more stacks of Wind Charges & Pearls
        for (int i = 11; i < 14; i++) items[i] = new ItemStack(Material.WIND_CHARGE, 64);
        for (int i = 14; i < 17; i++) items[i] = new ItemStack(Material.ENDER_PEARL, 16);
        items[17] = new ItemStack(Material.GOLDEN_APPLE, 64); // 2nd stack of GApples

        // Potions: 50% Str 2 (1:30) and 50% Speed 2 (1:30)
        boolean str = true;
        for (int i = 18; i < 36; i++) {
            items[i] = createPotion(str ? PotionType.STRONG_STRENGTH : PotionType.STRONG_SWIFTNESS, true);
            str = !str;
        }

        // --- Armor ---
        // Boots: Prot 4, Unb 3, Mend, FF 4, Depth 3, Soul Speed 3
        items[36] = createMaceArmor(Material.NETHERITE_BOOTS, "feather_falling", 4, "depth_strider", 3, "soul_speed", 3);
        // Leggings: Prot 4, Unb 3, Mend, Swift Sneak 3
        items[37] = createMaceArmor(Material.NETHERITE_LEGGINGS, "swift_sneak", 3, null, 0, null, 0);
        // Chest: Prot 4, Unb 3, Mend
        items[38] = createMaceArmor(Material.NETHERITE_CHESTPLATE, null, 0, null, 0, null, 0);
        // Helmet: Prot 4, Unb 3, Mend, Respiration 3, Aqua Affinity
        items[39] = createMaceArmor(Material.NETHERITE_HELMET, "respiration", 3, "aqua_affinity", 1, null, 0);

        items[40] = new ItemStack(Material.TOTEM_OF_UNDYING); // Offhand Totem
        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack createMaceArmor(Material mat, String ench1, int lvl1, String ench2, int lvl2, String ench3, int lvl3) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            applySafeEnchants(meta, "protection", 4, "unbreaking", 3, "mending", 1);
            
            if (ench1 != null) {
                Enchantment e1 = getEnchantment(ench1);
                if (e1 != null) meta.addEnchant(e1, lvl1, true);
            }
            if (ench2 != null) {
                Enchantment e2 = getEnchantment(ench2);
                if (e2 != null) meta.addEnchant(e2, lvl2, true);
            }
            if (ench3 != null) {
                Enchantment e3 = getEnchantment(ench3);
                if (e3 != null) meta.addEnchant(e3, lvl3, true);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getSMPKit() {
        ItemStack[] items = new ItemStack[41];
        // 1. Netherite Sword (Sharp 5, Mending, Unb 3)
        ItemStack sword1 = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m1 = sword1.getItemMeta();
        if (m1 != null) {
            applySafeEnchants(m1, "sharpness", 5, "mending", 1, "unbreaking", 3);
            sword1.setItemMeta(m1);
        }
        items[0] = sword1;

        // 2. Netherite Sword (Sharp 5, Mending, Unb 3, Knockback 1)
        ItemStack sword2 = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m2 = sword2.getItemMeta();
        if (m2 != null) {
            applySafeEnchants(m2, "sharpness", 5, "mending", 1, "unbreaking", 3, "knockback", 1);
            sword2.setItemMeta(m2);
        }
        items[1] = sword2;

        // 3. Netherite Axe (Sharp 5, Unb 3, Mending)
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta mAxe = axe.getItemMeta();
        if (mAxe != null) {
            applySafeEnchants(mAxe, "sharpness", 5, "mending", 1, "unbreaking", 3);
            axe.setItemMeta(mAxe);
        }
        items[2] = axe;

        // 4. Pearls (4 stacks), XP, and Hotbar Totem
        for (int i = 3; i < 7; i++) items[i] = new ItemStack(Material.ENDER_PEARL, 16);
        items[7] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        items[8] = new ItemStack(Material.TOTEM_OF_UNDYING); // Hotbar Totem
        items[9] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        items[10] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        items[16] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64); // XP moved to inv

        // 5. Fire Res (8:00) x5
        for (int i = 11; i < 16; i++) items[i] = createPotion(PotionType.LONG_FIRE_RESISTANCE, true);

        // 2 Stacks of Golden Apples
        items[17] = new ItemStack(Material.GOLDEN_APPLE, 64);
        items[18] = new ItemStack(Material.GOLDEN_APPLE, 64);

        // 6. Rest of inventory (Str 2 / Speed 2)
        boolean str = true;
        for (int i = 19; i < 36; i++) {
            items[i] = createPotion(str ? PotionType.STRONG_STRENGTH : PotionType.STRONG_SWIFTNESS, true);
            str = !str;
        }

        // Armor (Prot 4, Unb 3, Mending)
        items[36] = createSMPArmor(Material.NETHERITE_BOOTS);
        items[37] = createSMPArmor(Material.NETHERITE_LEGGINGS);
        items[38] = createSMPArmor(Material.NETHERITE_CHESTPLATE);
        items[39] = createSMPArmor(Material.NETHERITE_HELMET);
        
        // Shield (Mending, Unbreaking 3) in Offhand
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta sMeta = shield.getItemMeta();
        if (sMeta != null) {
            applySafeEnchants(sMeta, "mending", 1, "unbreaking", 3);
            shield.setItemMeta(sMeta);
        }
        items[40] = shield;
        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack createSMPArmor(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            applySafeEnchants(meta, "protection", 4, "unbreaking", 3, "mending", 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack[] getSwordKit() {
        ItemStack[] items = new ItemStack[41];
        items[0] = new ItemStack(Material.DIAMOND_SWORD);
        items[1] = new ItemStack(Material.STONE_SWORD);
        
        items[36] = new ItemStack(Material.DIAMOND_BOOTS);
        items[37] = new ItemStack(Material.DIAMOND_LEGGINGS);
        items[38] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        items[39] = new ItemStack(Material.DIAMOND_HELMET);
        return items;
    }

    private static ItemStack[] getAxeKit() {
        ItemStack[] items = new ItemStack[41];
        items[0] = new ItemStack(Material.DIAMOND_SWORD);
        items[1] = new ItemStack(Material.DIAMOND_AXE);
        items[2] = new ItemStack(Material.CROSSBOW);
        items[3] = new ItemStack(Material.BOW);
        items[8] = new ItemStack(Material.ARROW, 7);
        
        items[36] = new ItemStack(Material.DIAMOND_BOOTS);
        items[37] = new ItemStack(Material.DIAMOND_LEGGINGS);
        items[38] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        items[39] = new ItemStack(Material.DIAMOND_HELMET);
        items[40] = new ItemStack(Material.SHIELD);
        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getLifestealKit() {
        ItemStack[] items = new ItemStack[41];
        
        // --- Hotbar & Weapons ---
        // 0: Diamond Sword (Sharp 5, Unb 3, Mend, Fire Aspect 2)
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta mSword = sword.getItemMeta();
        if (mSword != null) {
            applySafeEnchants(mSword, "sharpness", 5, "unbreaking", 3, "mending", 1, "fire_aspect", 2);
            sword.setItemMeta(mSword);
        }
        items[0] = sword;

        // 1: Diamond Axe (Sharp 5, Unb 3, Mend, Eff 5)
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta mAxe = axe.getItemMeta();
        if (mAxe != null) {
            applySafeEnchants(mAxe, "sharpness", 5, "unbreaking", 3, "mending", 1, "efficiency", 5);
            axe.setItemMeta(mAxe);
        }
        items[1] = axe;

        // 2: Diamond Pickaxe (Eff 5, Unb 3, Mend)
        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta mPick = pick.getItemMeta();
        if (mPick != null) {
            applySafeEnchants(mPick, "efficiency", 5, "unbreaking", 3, "mending", 1);
            pick.setItemMeta(mPick);
        }
        items[2] = pick;

        // Hotbar Items
        items[3] = new ItemStack(Material.GOLDEN_APPLE, 64);
        items[4] = new ItemStack(Material.WIND_CHARGE, 64);
        items[5] = new ItemStack(Material.COBWEB, 64);
        items[6] = new ItemStack(Material.OAK_LOG, 64);
        items[7] = new ItemStack(Material.WATER_BUCKET);
        items[8] = new ItemStack(Material.WATER_BUCKET);

        // --- Inventory ---
        items[9] = new ItemStack(Material.WIND_CHARGE, 64); // 2nd stack
        items[10] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);

        // 4x 8min Fire Res (Long Fire Res)
        for (int i = 11; i < 15; i++) items[i] = createPotion(PotionType.LONG_FIRE_RESISTANCE, true);

        // Rest of inventory (15-35): 50% Strength II, 50% Speed II
        boolean str = true;
        for (int i = 15; i < 36; i++) {
            items[i] = createPotion(str ? PotionType.STRONG_STRENGTH : PotionType.STRONG_SWIFTNESS, true);
            str = !str;
        }

        // --- Armor ---
        // Boots: Prot 3, Unb 3, Mend, FF 4, Depth 3
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta mBoots = boots.getItemMeta();
        if (mBoots != null) {
            applySafeEnchants(mBoots, "protection", 3, "unbreaking", 3, "mending", 1, "feather_falling", 4, "depth_strider", 3);
            boots.setItemMeta(mBoots);
        }
        items[36] = boots;

        // Leggings: Prot 3, Unb 3, Mend, Swift Sneak 3
        ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemMeta mLegs = legs.getItemMeta();
        if (mLegs != null) {
            applySafeEnchants(mLegs, "protection", 3, "unbreaking", 3, "mending", 1, "swift_sneak", 3);
            legs.setItemMeta(mLegs);
        }
        items[37] = legs;

        // Chestplate: Prot 3, Unb 3, Mend
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta mChest = chest.getItemMeta();
        if (mChest != null) {
            applySafeEnchants(mChest, "protection", 3, "unbreaking", 3, "mending", 1);
            chest.setItemMeta(mChest);
        }
        items[38] = chest;

        // Helmet: Prot 3, Unb 3, Mend, Resp 3
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta mHelm = helm.getItemMeta();
        if (mHelm != null) {
            applySafeEnchants(mHelm, "protection", 3, "unbreaking", 3, "mending", 1, "respiration", 3);
            helm.setItemMeta(mHelm);
        }
        items[39] = helm;

        // Offhand: Shield
        items[40] = new ItemStack(Material.SHIELD);
        
        return items;
    }

    private static void applySafeEnchants(ItemMeta meta, Object... params) {
        for (int i = 0; i < params.length; i += 2) {
            String name = ((Object) params[i]).toString().toLowerCase();
            int level = (int) params[i+1];
            Enchantment ench = getEnchantment(name);

            if (ench != null && meta != null) {
                meta.addEnchant(ench, level, true);
            }
        }
    }

    private static Enchantment getEnchantment(String name) {
        if (name == null) return null;
        // Modern lookup using NamespacedKey
        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()
                .replace("protection_environmental", "protection")
                .replace("damage_all", "sharpness")
                .replace("dig_speed", "efficiency")
                .replace("durability", "unbreaking")
                .replace("protection_fall", "feather_falling")
                .replace("oxygen", "respiration")
                .replace("water_worker", "aqua_affinity")
                .replace("arrow_damage", "power")
                .replace("arrow_knockback", "punch")
                .replace("arrow_fire", "flame")
                .replace("arrow_infinite", "infinity")));
        return ench;
    }

    private static ItemStack[] getSpearMaceKit() {
        ItemStack[] items = new ItemStack[41];
        
        // --- Weapons ---
        // 0: Netherite Spear (Lunge 3, Unb 3, Sharp 5)
        Material spearMat = Material.matchMaterial("NETHERITE_SPEAR");
        if (spearMat == null) spearMat = Material.TRIDENT;
        
        ItemStack spear = new ItemStack(spearMat);
        ItemMeta sMeta = spear.getItemMeta();
        if (sMeta != null) {
            sMeta.setDisplayName("§3Netherite Spear");
            applySafeEnchants(sMeta, "unbreaking", 3, "sharpness", 5);
            
            Enchantment lunge = getEnchantment("lunge");
            if (lunge != null) {
                sMeta.addEnchant(lunge, 3, true);
            }
            spear.setItemMeta(sMeta);
        }
        items[0] = spear;
        
        // 1: Mace (Density 5, Unb 3, Mend, Wind Burst 3)
        ItemStack maceWind = new ItemStack(Material.MACE);
        ItemMeta mMaceW = maceWind.getItemMeta();
        if (mMaceW != null) {
            applySafeEnchants(mMaceW, "density", 5, "unbreaking", 3, "mending", 1, "wind_burst", 3);
            maceWind.setItemMeta(mMaceW);
        }
        items[1] = maceWind;
        
        // 2: Mace (Breach 4, Unb 3, Mend)
        ItemStack maceBreach = new ItemStack(Material.MACE);
        ItemMeta mMaceB = maceBreach.getItemMeta();
        if (mMaceB != null) {
            applySafeEnchants(mMaceB, "breach", 4, "unbreaking", 3, "mending", 1);
            maceBreach.setItemMeta(mMaceB);
        }
        items[2] = maceBreach;
        
        // 3: Netherite Sword (Sharp 5, Unb 3, Mend, KB 1)
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta mSword = sword.getItemMeta();
        if (mSword != null) {
            applySafeEnchants(mSword, "sharpness", 5, "unbreaking", 3, "mending", 1, "knockback", 1);
            sword.setItemMeta(mSword);
        }
        items[3] = sword;
        
        // 4: Netherite Axe (Sharp 5, Unb 3, Mend, Eff 5)
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta mAxe = axe.getItemMeta();
        if (mAxe != null) {
            applySafeEnchants(mAxe, "sharpness", 5, "unbreaking", 3, "mending", 1, "efficiency", 5);
            axe.setItemMeta(mAxe);
        }
        items[4] = axe;
        
        // --- Hotbar & Inventory Items ---
        items[5] = new ItemStack(Material.WIND_CHARGE, 64);
        items[6] = new ItemStack(Material.ENDER_PEARL, 16);
        items[7] = createPotion(PotionType.STRONG_STRENGTH, true);
        items[8] = new ItemStack(Material.GOLDEN_APPLE, 64);
        
        // 9-11: 3 more stacks of Wind Charges
        for (int i = 9; i < 12; i++) items[i] = new ItemStack(Material.WIND_CHARGE, 64);
        // 12-14: 3 more stacks of Pearls
        for (int i = 12; i < 15; i++) items[i] = new ItemStack(Material.ENDER_PEARL, 16);
        
        items[15] = new ItemStack(Material.GOLDEN_APPLE, 64); // 2nd stack GApples
        
        // Shield: Unbreaking 2
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta mShield = shield.getItemMeta();
        if (mShield != null) {
            applySafeEnchants(mShield, "unbreaking", 2);
            shield.setItemMeta(mShield);
        }
        items[16] = shield;
        
        items[17] = new ItemStack(Material.TOTEM_OF_UNDYING); // 2nd Totem
        
        // Potions: 50% Str 2 (1:30) and 50% Speed 2 (1:30) (Splash)
        boolean str = true;
        for (int i = 18; i < 36; i++) {
            items[i] = createPotion(str ? PotionType.STRONG_STRENGTH : PotionType.STRONG_SWIFTNESS, true);
            str = !str;
        }
        
        // Armor (Reusing helper from Mace kit)
        items[36] = createMaceArmor(Material.NETHERITE_BOOTS, "feather_falling", 4, "depth_strider", 3, "soul_speed", 3);
        items[37] = createMaceArmor(Material.NETHERITE_LEGGINGS, "swift_sneak", 3, null, 0, null, 0);
        items[38] = createMaceArmor(Material.NETHERITE_CHESTPLATE, null, 0, null, 0, null, 0);
        items[39] = createMaceArmor(Material.NETHERITE_HELMET, "respiration", 3, "aqua_affinity", 1, null, 0);
        
        // Offhand: Totem
        items[40] = new ItemStack(Material.TOTEM_OF_UNDYING);
        
        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack[] getCartPvPKit() {
        ItemStack[] items = new ItemStack[41];

        // 0: Netherite Sword (Sharp 5, Unb 3, Mend, KB 1)
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta mSword = sword.getItemMeta();
        if (mSword != null) {
            applySafeEnchants(mSword, "sharpness", 5, "unbreaking", 3, "mending", 1, "knockback", 1);
            sword.setItemMeta(mSword);
        }
        items[0] = sword;

        // 1: Netherite Axe (Sharp 5, Unb 3, Mend, Eff 5)
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta mAxe = axe.getItemMeta();
        if (mAxe != null) {
            applySafeEnchants(mAxe, "sharpness", 5, "unbreaking", 3, "mending", 1, "efficiency", 5);
            axe.setItemMeta(mAxe);
        }
        items[1] = axe;

        // 2: Crossbow (Quick Charge 3, Unb 3, Piercing 4)
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta mCross = crossbow.getItemMeta();
        if (mCross != null) {
            applySafeEnchants(mCross, "quick_charge", 3, "unbreaking", 3, "piercing", 4);
            crossbow.setItemMeta(mCross);
        }
        items[2] = crossbow;

        // 3: Flint and Steel (Unb 3, Mend)
        ItemStack flint = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta mFlint = flint.getItemMeta();
        if (mFlint != null) {
            applySafeEnchants(mFlint, "unbreaking", 3, "mending", 1);
            flint.setItemMeta(mFlint);
        }
        items[3] = flint;

        // 4: Shulker Box filled with TNT Minecarts
        ItemStack shulker = new ItemStack(Material.RED_SHULKER_BOX);
        org.bukkit.inventory.meta.BlockStateMeta bsm = (org.bukkit.inventory.meta.BlockStateMeta) shulker.getItemMeta();
        if (bsm != null) {
            org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) bsm.getBlockState();
            for (int i = 0; i < 27; i++) {
                box.getInventory().setItem(i, new ItemStack(Material.TNT_MINECART));
            }
            bsm.setBlockState(box);
            shulker.setItemMeta(bsm);
        }
        items[4] = shulker;

        // 5-8: Rails, Logs, Pearls, GApples
        items[5] = new ItemStack(Material.RAIL, 64);
        items[6] = new ItemStack(Material.OAK_LOG, 64);
        items[7] = new ItemStack(Material.ENDER_PEARL, 16);
        items[8] = new ItemStack(Material.GOLDEN_APPLE, 64);

        // Inventory
        items[9] = new ItemStack(Material.RAIL, 64); // 2nd stack
        items[10] = new ItemStack(Material.OAK_LOG, 64); // 2nd stack
        items[11] = new ItemStack(Material.COBWEB, 64);
        items[12] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
        
        // Bow: Punch 2, Power 5, Flame
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta mBow = bow.getItemMeta();
        if (mBow != null) {
            applySafeEnchants(mBow, "punch", 2, "power", 5, "flame", 1);
            bow.setItemMeta(mBow);
        }
        items[13] = bow;

        items[14] = new ItemStack(Material.ARROW, 64);
        items[15] = new ItemStack(Material.ARROW, 64);

        // Fill remaining inventory with TNT Minecarts
        for (int i = 16; i < 36; i++) {
            items[i] = new ItemStack(Material.TNT_MINECART);
        }

        // Boots: Prot 4, Unb 3, Mend, FF 4, Depth 3, Soul Speed 3
        items[36] = createMaceArmor(Material.NETHERITE_BOOTS, "feather_falling", 4, "depth_strider", 3, "soul_speed", 3);
        
        // Leggings: Blast Prot 4, Unb 3, Mend, Swift Sneak 3
        ItemStack legs = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemMeta mLegs = legs.getItemMeta();
        if (mLegs != null) {
            applySafeEnchants(mLegs, "blast_protection", 4, "unbreaking", 3, "mending", 1, "swift_sneak", 3);
            legs.setItemMeta(mLegs);
        }
        items[37] = legs;

        // Chest: Prot 4, Unb 3, Mend
        items[38] = createMaceArmor(Material.NETHERITE_CHESTPLATE, null, 0, null, 0, null, 0);

        // Helmet: Prot 4, Unb 3, Mend, Resp 3, Aqua
        items[39] = createMaceArmor(Material.NETHERITE_HELMET, "respiration", 3, "aqua_affinity", 1, null, 0);

        // Offhand: Shield
        items[40] = new ItemStack(Material.SHIELD);

        return items;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack createItem(Material mat, String name, String enchantType, int level) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (name != null) {
            meta.setDisplayName(name);
        }

        Enchantment target = getEnchantment(enchantType);

        if (target != null) {
            meta.addEnchant(target, level, true);
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPotion(PotionType type, boolean splash) {
        ItemStack pot = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION);
        PotionMeta meta = (PotionMeta) pot.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(type);
            pot.setItemMeta(meta);
        }
        return pot;
    }
}
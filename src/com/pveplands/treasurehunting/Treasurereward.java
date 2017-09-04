package com.pveplands.treasurehunting;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to generate rewards for a treasure chest in the Treasure Hunting system.
 */
public class Treasurereward {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(Treasurereward.class));
    private static final Random random = new Random();
    private static final TreasureOptions options = TreasureHunting.getOptions();
    
    private static final int[] woodMaterials = new int[] { 14, 37, 38, 39, 40, 41, 42, 43, 44, 45, 63, 64, 65, 66, 46, 47, 48, 49, 50, 51 };
    private static final int[] metalMaterials = new int[] { };

    /**
     * Gets a karma reward in the form of liquid source.
     * @param quality Quality of the treasure. The higher the quality, the more karma depending on the config file.
     * @return A small barrel item with at most 45 kg of 99 quality liquid source.
     */
    public static Item getKarmaReward(double quality) {
        if (options.getBaseKarmaReward() < 1)
            return null; // Config does not want liquid source.
        
        Item barrel = null, source = null;
        
        try {
            int bonus = (int)(quality * options.getKarmaMultiplier());
            int weight = options.getBaseKarmaReward();
            
            if (bonus > 0) weight += bonus; // random.nextInt(bonus); 
            else weight = (int)Math.ceil(quality / 10 * options.getBaseKarmaReward());
            
            if (weight <= 0){
                logger.info(String.format("Liquid source reward was of weight %d grams, so not creating any.", weight));
                return null;
            }
            
            barrel = ItemFactory.createItem(189, (float)quality, getRarity(), null);
            barrel.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(189)));
            logger.info(String.format("Creating barrel (%d) for %d grams of liquid source.", barrel.getWurmId(), weight));

            weight = Math.min(barrel.getContainerVolume(), weight); // barrel capacity.

            source = ItemFactory.createItem(763, 99f, null);
            source.setWeight(weight, true);
            source.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(763)));
            logger.info(String.format("Creating liquid source (%d) for barrel (%d).", source.getWurmId(), barrel.getWurmId()));
            
            barrel.insertItem(source, true);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not generate karma reward.", e);
            
            if (source != null)
                Items.destroyItem(source.getWurmId());
            
            if (barrel != null)
                Items.destroyItem(barrel.getWurmId());
            
            return null;
        }
        
        return barrel;
    }

    /**
     * Generates item rewards as specified in the config file.
     * @param quality Quality of the treasure.
     * @return A list if items, may be empty.
     */
    public static ArrayList<Item> getTierRewards(double quality) {
        ArrayList<Item> list = new ArrayList<>();
        int tier = Math.max(0, Math.min(9, (int)quality / 10));
        
        /**
         * 1098: returner tool chest - always contains one of the following,
         *       and it will always have tin as their material: hatchet, pick,
         *              saw, shovel, rake, hammer, file, or leather knife.
         */
        try {
            // Guaranteed number of items.
            for (int i = 0; i < options.getTierGuaranteed()[tier]; i++) {
                Item reward = getRewardItem(tier, quality);
                
                if (reward == null) {
                    logger.warning(String.format("Could not get guaranteed item reward for tier %d, quality %.2f, is the config malformed?", tier, quality));
                    continue;
                }
                
                list.add(reward);
            }
            
            // Additional items as per chance configured.
            for (int i = 0; i < options.getTierOptional()[tier]; i++) {
                if (options.getTierChances().length < tier) {
                    logger.warning(String.format("Optional tier chances has only %d tiers, was expecting at least %d.", options.getTierChances().length, tier));
                    continue;
                }
                
                int tierChance = options.getTierChances()[tier];
                
                if (tierChance <= 0) {
                    logger.warning(String.format("Options reward item chance for tier %d is %d, was expecting greater than zero.", tier, tierChance));
                    continue;
                }
                
                if (random.nextInt(tierChance) == 0) {
                    Item reward = getRewardItem(tier, quality);
                    
                    if (reward == null) {
                        logger.warning(String.format("Could not get optional ittem reward for tier %d, quality %.2f, is the config malformed?", tier, quality));
                        continue;
                    }
                    
                    list.add(reward);
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not generate all item tier rewards.", e);
        }
        
        return list;
    }
    
    /**
     * Utility method to generate a single item dependent on a treasure's tier,
     * setting it to the specified quality level.
     * @param tier Tier of the treasure from 0 to 9.
     * @param quality Quality level of the treasure.
     * @return An item as reward, may be null if it fails.
     * @throws Exception Item template not found, or item failed to generate.
     */
    public static Item getRewardItem(int tier, double quality) throws Exception {
        // Make sure tier is not out of bounds, e.g. when called from another mod?
        tier = Math.max(0, Math.min(9, tier));
        
        if (tier >= options.getTierItems().length) {
            logger.warning("tierItems array did not have the expected dimension. The mod has been broken by somebody else? No item reward was generated.");
            return null;
        }
        
        if (options.getTierItems()[tier].length == 0) {
            logger.warning(String.format("Treasure tier %d has no reward groups specified, is the config malformed? No item reward was generated.", tier));
            return null;
        }
        
        int rewardGroup = options.getTierItems()[tier][random.nextInt(options.getTierItems()[tier].length)];

        if (options.getTierGroups()[rewardGroup].length == 0) {
            logger.warning(String.format("Reward group %d has no items. No item reward was generated.", rewardGroup));
            return null;
        }
        
        int rewardIndex = random.nextInt(options.getTierGroups()[rewardGroup].length);
        int templateId = options.getTierGroups()[rewardGroup][rewardIndex];

        if (templateId <= 0) {
            logger.warning(String.format("Reward group %d at index %d has %d as item template ID reward, this is an invalid value. No item was generated.",
                rewardGroup, rewardIndex, templateId));
            return null;
        }
        
        int guaranteedRarity = 0;
        
        switch (templateId) {
            case 867: // Strange bone.
                guaranteedRarity = 1;
                break;
        }
        
        logger.info(String.format("Picking reward %d (%d) from reward group %d for tier %d.", rewardIndex, templateId, rewardGroup, tier));
        
        Item reward = ItemFactory.createItem(templateId, (float)quality, getRarity(guaranteedRarity), null);
        reward.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(templateId)));
        
        return reward;
    }
    
    /**
     * Generates an unfinished item as reward for a treasure chest.
     * @param owner Owner of the map/treasure.
     * @param quality Quality level of the treasure.
     * @return An item as reward, may be null if it fails.
     */
    public static Item getUnfinishedItem(Creature owner, double quality) {
        if (options.getUnfinishedChance() < 1 || (options.getUnfinisheds().length == 0 && options.getKingdomItems().length == 0))
            return null;
        
        int chance = Math.max(1, options.getUnfinishedChance() - (int)(quality / 10d * options.getUnfinishedMultiplier()));
        
        if (random.nextInt(chance) > 0)
            return null;
        
        Item item = null;
        
        try {
            int index = random.nextInt(options.getUnfinisheds().length + options.getKingdomItems().length);
            int realTemplateId = 0, templateId = 179;
            
            // picks from equal chance either an unfinished, or simple item.
            if (index < options.getUnfinisheds().length)
                realTemplateId = options.getUnfinisheds()[index];
            else {
                realTemplateId = options.getKingdomItems()[index - options.getUnfinisheds().length];
                templateId = realTemplateId;
            }

            if (realTemplateId <= 0)
                throw new Exception(String.format("unfinishedItems has template Id %d at index %d, this is an invalid item template ID value!", index, realTemplateId));
            
            item = ItemFactory.createItem(templateId, (float)quality, getRarity(), null);
            
            byte kingdom = (byte)(options.getUnfinishedKingdoms().length > 0 ? options.getUnfinishedKingdoms()[random.nextInt(options.getUnfinishedKingdoms().length)] : owner.getKingdomId());
            
            if (kingdom == 0)
                kingdom = owner.getKingdomId();
            
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(realTemplateId);
            item.setData(0, 0);
            
            if (templateId == 179) {
                AdvancedCreationEntry.setTemplateId(item, realTemplateId);
                // ^ does the two lines below.
                //item.setData1(realTemplateId << 16);
                //item.setRealTemplate(realTemplateId);
                item.setWeight(10000, false);
                item.setName("unfinished" + (template.sizeString.length() > 0 ? " " : "") + template.sizeString + template.getName());
            }
            
            item.setAuxData(kingdom);
            item.setMaterial(getMaterial(template));
            
            logger.info(String.format("Unfinished reward %s gets kingdom ID %d", template.getName(), kingdom));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not generate unfinished kingdom item.", e);
        }
        
        return item;
    }
    
    /**
     * Generates an item in the list of extremely rare rewards.
     * @param quality Quality level of the treasure.
     * @return An item, may be null if it fails.
     */
    public static Item getExtremelyRareItem(double quality) {
        if (options.getExtremelyRares().length == 0 || options.getExtremelyRareChance() <= 0)
            return null;
        
        int chance = Math.max(1, options.getExtremelyRareChance() - (int)Math.round(quality / 10d * options.getExtremelyRareMultiplier()));
        
        if (random.nextInt(chance) > 0)
            return null;
        
        Item item = null;
        
        try { 
            int index = random.nextInt(options.getExtremelyRares().length);
            int templateId = options.getExtremelyRares()[index];
            
            if (templateId <= 0)
                throw new Exception(String.format("extremelyRare has template Id %d at index %d, this is an invalid item template ID value!", templateId, index));
            
            int guaranteedRarity = 0;
            
            if (templateId == 867)
                guaranteedRarity = 1;
            
            item = ItemFactory.createItem(templateId, (float)quality, getRarity(guaranteedRarity), null);
            item.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(templateId)));
        }
        catch (Exception e) { 
            logger.log(Level.SEVERE, "Could not generate extremely rare item.", e); 
        }
        
        return item;
    }
    
    /**
     * Generates an item from the very rare item list in the config.
     * @param quality Quality level of the treasure.
     * @return An item, may be null.
     */
    public static Item getVeryRareItem(double quality) {
        if (options.getVeryRares().length == 0 || options.getVeryRareChance() <= 0)
            return null;

        int chance = Math.max(1, options.getVeryRareChance() - (int)Math.round(quality / 10d * options.getVeryRareMultiplier()));
        
        if (random.nextInt(chance) > 0)
            return null;
        
        Item item = null;
        
        try { 
            int index = random.nextInt(options.getVeryRares().length);
            int templateId = options.getVeryRares()[index];
            
            if (templateId <= 0)
                throw new Exception(String.format("veryRare has template Id %d at index %d, this is an invalid template ID value!", templateId, index));
            
            int guaranteedRarity = 0;
            
            if (templateId == 867)
                guaranteedRarity = 1;
            
            item = ItemFactory.createItem(templateId, (float)quality, getRarity(guaranteedRarity), null); 
            item.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(templateId)));
        }
        catch (Exception e) { 
            logger.log(Level.SEVERE, "Could not generate very rare item.", e); 
        }
        
        return item;
    }
    
    /**
     * Generates an item that is guaranteed to have at least rare quality.
     * @param performer Creature who the treasure is for (to get a kingdom id).
     * @param quality Quality level of the treasure.
     * @return An item, may be null if it fails.
     */
    public static Item getRareItem(Creature performer, double quality) {
        if ((options.getRareItems().length == 0 && options.getUnfinishedRares().length == 0) || options.getRareChance() <= 0f)
            return null;
        
        float c = options.getRareChance();
        
        if (options.getRareMultiplier() > 0f) c += (quality / 10d * options.getRareMultiplier());
        
        if (random.nextFloat() > c / 100f)
            return null;
        
        Item item = null;
        
        try {
            int index = random.nextInt(options.getUnfinishedRares().length + options.getRareItems().length);
            
            if (options.getRareItems().length == 0 || (options.getUnfinishedRares().length > 0 && index < options.getUnfinishedRares().length)) {
                //int index = random.nextInt(options.getUnfinishedRares().length);
                int templateId = options.getUnfinishedRares()[index];
                
                if (templateId <= 0)
                    throw new Exception(String.format("unfinishedRare has template Id %d at index %d, this is an invalid item template ID!", templateId, index));

                ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
                
                item = ItemFactory.createItem(179, (float)quality, getRarity(1), null);
                item.setData(0, 0);
                AdvancedCreationEntry.setTemplateId(item, templateId);
                // ^ does the two lines below.
                //item.setData1(templateId << 16);
                //item.setRealTemplate(templateId);
                item.setWeight(10000, false);
                item.setName("unfinished " + (template.sizeString.length() > 0 ? " " : "") + template.sizeString + template.getName());
                item.setMaterial(getMaterial(template));
                
                if (templateId == 384) { // guard tower
                    byte kingdomId = 0;
                    
                    if (options.getUnfinishedKingdoms().length > 0)
                        kingdomId = (byte)options.getUnfinishedKingdoms()[random.nextInt(options.getUnfinishedKingdoms().length)];
                    
                    if (kingdomId <= 0)
                        kingdomId = performer.getKingdomId();
                    
                    logger.info(String.format("Rare item %d, setting kingdom ID to %d", templateId, kingdomId));
                    
                    item.setAuxData(kingdomId);
                }
            }
            else {
                index -= options.getUnfinishedRares().length;
                int templateId = options.getRareItems()[index];
                
                if (templateId <= 0)
                    throw new Exception(String.format("rareItems has temlate Id %d at index %d, this is an invalid item template ID value!", templateId, index));
                
                item = ItemFactory.createItem(templateId, (float)quality, getRarity(1), null);
                item.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(templateId)));
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not create rare item.", e);
        }
        
        return item;
    }
    
    /**
     * Generates a HotA statue as reward.
     * @param quality Quality level of the treasure.
     * @return An item, may be null if it fails.
     */
    public static Item getHotaStatue(double quality) {
        if (options.getHotaChance() <= 0f)
            return null;
        
        // Needs at least one element for Random.nextInt(int) not to fail.
        if (options.getHotaAux().length == 0)
            options.setHotaAux(new int[] { 0 });
        
        float c = options.getHotaChance();
        
        if (options.getHotaMultiplier() > 0) c += (quality / 10d * options.getHotaMultiplier());
            
        if (random.nextFloat() > c / 100f)
            return null;
        
        Item statue = null;
        
        try {
            statue = ItemFactory.createItem(742, (float)quality, getRarity(), null);
            statue.setAuxData((byte)options.getHotaAux()[random.nextInt(options.getHotaAux().length)]);
            statue.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(742)));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not create HOTA statue.", e);
        }
        
        return statue;
    }
    
    /**
     * Generates a material from the precious metals list in the config,
     * the weight of it depends on the quality as per config file.
     * @param quality Quality level of the treasure.
     * @return An item, may be null if it fails.
     * @throws Exception Item template was not found or could not be instantiated.
     */
    public static Item getPreciousMetal(double quality) throws Exception {
        if (options.getMetals().length == 0 || options.getBaseMetalWeight() <= 0)
            return null;
        
        int bonus = (int)Math.ceil(quality / 10d * options.getMetalMultiplier() * options.getBaseMetalWeight() - options.getBaseMetalWeight());
        int weight = options.getBaseMetalWeight();
        
        if (bonus > 0) weight += random.nextInt(bonus);
        else weight = (int)Math.ceil(quality / 10d * options.getBaseMetalWeight());
        
        int templateId = options.getMetals()[random.nextInt(options.getMetals().length)];
        
        if (templateId <= 0)
            throw new Exception("One of the precious metal template IDs is equal or less than 0, this is an invalid ID.");
        
        if (templateId == 371 || templateId == 372)
            weight = (int)Math.ceil(weight * options.getDragonMultiplier());
        
        Item metal = null;
        
        try {
            metal = ItemFactory.createItem(templateId, (float)quality, getRarity(), null);
            metal.setWeight(Math.min(weight, metal.getTemplate().getWeightGrams() * 64), true);
            metal.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(templateId)));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not create precious metal.", e);
        }
        
        return metal;
    }
    
    /**
     * Determines the amount of sleep powder for a specific treasure quality.
     * @param quality Effective quality of the treasure.
     * @return List of items with sleep powders, or null if it fails. List may be empty.
     */
    public static ArrayList<Item> getSleepPowder(double quality) {
        if (options.getBaseSleepPowderReward() <= 0)
            return null;
        
        ArrayList<Item> list = new ArrayList<>();
        
        try {
            int count = options.getBaseSleepPowderReward();
            int bonus = (int)Math.ceil(quality / 10d * options.getSleepPowderMultiplier() * options.getBaseSleepPowderReward() - options.getBaseSleepPowderReward());
            
            if (bonus > 0) count += random.nextInt(bonus);
            else count = (int)Math.ceil(quality / 10d * options.getBaseSleepPowderReward());
            
            while (count-- > 0) {
                Item cocaine = ItemFactory.createItem(666, 99.0f, getRarity(), null);
                cocaine.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(666)));
                list.add(cocaine);
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not generate sleep powders.", e);
        }
        
        return list;
    }
    
    /**
     * Creates an amount of money dependent on a quality value (of a treasure).
     * @param quality Quality level.
     * @return NULL or a List with coin items. The List might be empty.
     */
    public static ArrayList<Item> getMoney(double quality) {
        if (options.getBaseMoneyReward() < 1)
            return null;

        ArrayList<Item> list = new ArrayList<>();
        
        try {
            double bonus = Math.round(options.getBaseMoneyReward() * ((quality / 10) * options.getMoneyMultiplier())) - options.getBaseMoneyReward();
            int money = options.getBaseMoneyReward() + (int)Math.round(random.nextDouble() * bonus);
            
            if (money < 1) {
                logger.log(Level.INFO, String.format("Money reward of %d including %.6f bonus could not be generated, because it's less than 1.", money, bonus));
                return null;
            }
            
            logger.log(Level.INFO, "Creating {0} money as reward.", money);
            
            list.addAll(getMoneyCoins(money, 20000000, 61)); money %= 20000000;
            list.addAll(getMoneyCoins(money, 5000000, 57)); money %= 5000000;
            list.addAll(getMoneyCoins(money, 1000000, 53)); money %= 1000000;
            list.addAll(getMoneyCoins(money, 200000, 60)); money %= 200000;
            list.addAll(getMoneyCoins(money, 50000, 56)); money %= 50000;
            list.addAll(getMoneyCoins(money, 10000, 52)); money %= 10000;
            list.addAll(getMoneyCoins(money, 2000, 58)); money %= 2000;
            list.addAll(getMoneyCoins(money, 500, 54)); money %= 500;
            list.addAll(getMoneyCoins(money, 100, 50)); money %= 50;
            list.addAll(getMoneyCoins(money, 20, 59)); money %= 20;
            list.addAll(getMoneyCoins(money, 5, 55)); money %= 5;
            list.addAll(getMoneyCoins(money, 1, 51));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Money reward for treasure could not be created.", e);
        }
        
        return list;
    }
    
    /**
     * Gets as many coins of a specific value, to match up to a total amount.
     * @param money Total amount of money wanted.
     * @param value Value of a single coin.
     * @param templateId Template ID of the single coin.
     * @return List of items with coins.
     * @throws FailedException Item could not be created in the ItemFactory.
     * @throws NoSuchTemplateException The provided template ID could not be found in the ItemTemplateFactory.
     */
    private static ArrayList<Item> getMoneyCoins(int money, int value, int templateId) throws FailedException, NoSuchTemplateException {
        int count = getMoneyCoins(money, value);
        ArrayList<Item> coins = new ArrayList<>();
        
        while (count-- > 0) {
            Item coin = ItemFactory.createItem(templateId, random.nextFloat() * 100, getRarity(), null);
            coin.setMaterial(getMaterial(ItemTemplateFactory.getInstance().getTemplate(templateId)));
            coins.add(coin);
        }
        
        return coins;
    }
    
    /**
     * Calculates the amount of coins to get for a total value.
     * @param money Total amount of money.
     * @param value Value of the coin.
     * @return Number of coins that fit into the total money.
     */
    private static int getMoneyCoins(int money, int value) {
        return money / value;
    }
    
    /**
     * Returns a random rarity, mimicking vanilla rare item chance.
     * @return Rarity level of 0 none, 1 rare, 2 supreme, or 3 fantastic.
     */
    public static byte getRarity() {
        return getRarity(0);
    }
    
    /**
     * Gets a rarity that mimicks the vanilla behaviour.
     * 
     * @param guaranteed Minimum rarity level to return.
     * @return A rarity of at least guaranteed rarity, or 0 none, 1 rare, 2 supreme, 3 fantastic.
     */
    public static byte getRarity(int guaranteed) {
        if (guaranteed > 3) {
            logger.warning(String.format("getRarity(I)B tried to guarantee a rarity of %d, but it can't be greater than 3 (fantastic). It will be normalised down to 3.", guaranteed));
            guaranteed = 3;
        }
        else if (guaranteed < 0) {
            logger.warning(String.format("getRarity(I)B tried to guarantee a rarity of %d, but it can't be less than 0 (no rarity). It will be normalised up to 0.", guaranteed));
            guaranteed = 0;
        }
        
        /**
         * To emulate player rarity window (20 seconds if random(3600), we
         * do random(3600/20) and then go from there.
         * */
        if (random.nextInt(options.getRareWindow()) > 0) // rare window not open.
            return (byte)Math.max(guaranteed, 0);

        if (random.nextInt(options.getFantasticRarity()) == 0)
            return (byte)Math.max(guaranteed, 3);

        if (random.nextInt(options.getSupremeRarity()) == 0)
            return (byte)Math.max(guaranteed, 2);

        if (random.nextInt(options.getRareRarity()) == 0)
            return (byte)Math.max(guaranteed, 1);

        return (byte)Math.max(guaranteed, 0);
    }
    
    /**
     * Gets a random wooden material value.
     * @return Wooden material value.
     */
    public static byte getRandomWood() {
        return (byte)woodMaterials[random.nextInt(woodMaterials.length)];
    }
    
    /**
     * Tries to find the right, or one of the appropriate materials for
     * any one item.
     * 
     * @param template Item template to get the material for.
     * @return Returns 0 (unknown material), or a material.
     */
    public static byte getMaterial(ItemTemplate template) {
        CreationEntry creationEntry = CreationMatrix.getInstance().getCreationEntry(template.getTemplateId());

        if (creationEntry != null && creationEntry.getTotalNumberOfItems() > 2) {
            byte material = creationEntry.getFinalMaterial();
            
            try {
                if (material == 0) 
                    material = ItemTemplateFactory.getInstance().getTemplate(creationEntry.getObjectSource()).getMaterial();
            } catch (Exception e) { }
            
            if (MaterialUtilities.isWood(material) || template.isWood())
                material = getRandomWood();
            
            if (material > 0) {
                logger.info(String.format("Get material for %s. Total number of items is > 2, picking %s as material #%d",
                    template.getName(), MaterialUtilities.getMaterialString(material), material));
            
                return material;
            }
            else logger.info(String.format("Get material for %s. Although total number of items is > 2, can't find material.", template.getName()));
        }
        
        if (creationEntry != null && creationEntry.getTotalNumberOfItems() <= 2) {
            try {
                ItemTemplate targetItem = ItemTemplateFactory.getInstance().getTemplate(creationEntry.getObjectTarget());
                byte material = targetItem.getMaterial();
                
                if (MaterialUtilities.isWood(material))
                    material = getRandomWood();
                
                if (material > 0) {
                    logger.info(String.format("Get material for %s. Picking %s as material #%d.",
                        template.getName(), MaterialUtilities.getMaterialString(material), material));

                    return material;
                }
                else {
                    logger.info(String.format("Get material for %s. Although there's a creation entry, can't find a material. Looking deeper.", template.getName()));
                    ArrayList<Byte> list = new ArrayList<>();
                    
                    for (CreationEntry deeper : CreationMatrix.getInstance().getSimpleEntries()) {
                        if (deeper.getObjectCreated() == creationEntry.getObjectTarget()) {
                            material = ItemTemplateFactory.getInstance().getTemplate(deeper.getObjectTarget()).getMaterial();
                            
                            if (material > 0) {
                                list.add(material);
                                logger.info(String.format("Adding %s as possible material #%d.", MaterialUtilities.getMaterialString(material), material));
                            }
                        }
                    }
                    
                    if (list.size() > 0) {
                        material = list.get(random.nextInt(list.size()));
                        logger.info(String.format("Returning %s as random material from the list", MaterialUtilities.getMaterialString(material)));
                        
                        return material;
                    }
                    else logger.info("It's hopeless, I can't find a material.");
                }
            }
            catch (Exception e) {
                
            }
        }

        byte material = template.getMaterial();
        
        if (MaterialUtilities.isWood(material))
            material = getRandomWood();
        
        logger.info(String.format("Get material for %s. Everything else failed, returning %s as material #%d.",
            template.getName(), MaterialUtilities.getMaterialString(material), material));
            
        return material;
    }
}

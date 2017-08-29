package com.pveplands.treasurehunting;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to spawn a treasure chest with all its contents.
 */
public class Treasurechest {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(Treasurechest.class));

    /**
     * Generates a treasure chest with all contents from a treasure map.
     * @param performer Creatures the treasure is generated for.
     * @param map Treasure map being used.
     * @return Chest with items, may be null.
     */
    public static Item CreateTreasurechest(Creature performer, Item map) {
        if (map == null || map.getTemplateId() != TreasureHunting.getOptions().getTreasuremapTemplateId()) {
            logger.warning("Tried to generate treasure chest for an item that is not a treasure map or null. Who is calling this method?");
            logger.warning(Arrays.toString(new Exception().getStackTrace()).replace(", ", "\r\n\t"));
            return null;
        }
        
        Item chest = null;
        
        try {
            double quality = Math.max(1, Math.min(100d, map.getCurrentQualityLevel() + (map.getRarity() * 10)));
            
            logger.info("Creating treasure chest item.");
            chest = ItemFactory.createItem(995, (float)quality, map.getRarity(), null);
            
            logger.info("Creating money reward.");
            add(chest, Treasurereward.getMoney(quality));
            logger.info("Creating sleep powder reward.");
            add(chest, Treasurereward.getSleepPowder(quality));
            logger.info("Creating precious metals.");
            add(chest, Treasurereward.getPreciousMetal(quality));
            logger.info("Creating HOTA reward");
            add(chest, Treasurereward.getHotaStatue(quality));
            logger.info("Creating rare item reward.");
            add(chest, Treasurereward.getRareItem(performer, quality));
            logger.info("Creating very rare item reward.");
            add(chest, Treasurereward.getVeryRareItem(quality));
            logger.info("Creating extremely rare item reward.");
            add(chest, Treasurereward.getExtremelyRareItem(quality));
            logger.info("Creating unfinished (kingdom) item reward.");
            add(chest, Treasurereward.getUnfinishedItem(performer, quality));
            logger.info("Creating actual item reward.");
            add(chest, Treasurereward.getTierRewards(quality));
            logger.info("Creating karma reward.");
            add(chest, Treasurereward.getKarmaReward(quality));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Treasure chest creation failed.", e);
        }
        
        return chest;
    }
    
    /**
     * Inserts an item into another.
     * @param chest Container to insert the item into.
     * @param item Item to be inserted, may be null.
     */
    public static void add(Item chest, Item item) {
        if (item == null)
            return;
        
        logger.log(Level.INFO, "Inserting {0} into treasurechest.", item.getName());
        chest.insertItem(item, true);
    }
    
    /**
     * Inserts a list of items into another.
     * @param chest Container to insert the items into.
     * @param list List of items, may be null or empty.
     */
    public static void add(Item chest, ArrayList<Item> list) {
        if (list == null)
            return;
        
        for (Item item : list) {
            logger.log(Level.INFO, "Inserting {0} into treasurechest.", item.getName());
            chest.insertItem(item, true);
        }
    }
}

package com.pveplands.treasurehunting;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

/**
 * Gamemaster feature to spawn a treasure chest.
 */
public class SpawnTreasurechestAction implements ActionPerformer, ModAction {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(SpawnTreasurechestAction.class));
    
    private short actionId;
    private ActionEntry actionEntry;
    
    public SpawnTreasurechestAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Spawn treasurechest HERE", "spawning treasurechest", MiscConstants.EMPTY_INT_ARRAY);
        ModActions.registerAction(actionEntry);
    }
    
    @Override
    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        if (performer.getPower() <= 1) {
            Logger.getLogger(TreasureHunting.getLoggerName(CreateRandomTreasuremapAction.class))
                .warning(String.format("%s tried to spawn a treasure chest, this might well fall under exploiting.", performer));
            
            return true;
        }
        
        String log = String.format("%s is spawning a %.2f quality treasurechest.", performer.getName(), source.getCurrentQualityLevel());
        performer.getLogger().info(log);
        logger.info(log);
        log = null;
        
        Item treasuremap = Treasuremap.CreateTreasuremap(performer, null, null, null, true);
        
        if (treasuremap == null) {
            performer.getCommunicator().sendNormalServerMessage("Treasuremap dummy creation failed, probably couldn't find a suitable spot. Try again.");
            logger.info("Treasuremap dummy creation failed, probably couldn't find a suitable spot. Try again.");
            performer.getLogger().info("Treasuremap dummy creation failed, probably couldn't find a suitable spot. Try again.");
        }
        else {
            treasuremap.setDataXY(tilex, tiley);
            
            float quality;
            
            if (source.getAuxData() < 1 || source.getAuxData() > 100)
                quality = new Random().nextFloat() * 100;
            else
                quality = source.getAuxData();
            
            treasuremap.setQualityLevel(Math.min(100, Math.max(1, quality)));
            Item chest = Treasurechest.CreateTreasurechest(performer, treasuremap);

            //performer.getInventory().insertItem(treasuremap, true);
            //performer.getCommunicator().sendNormalServerMessage("Treasuremap created in your inventory at the selected coordinates, quality level is your wand's AuxData value.");
            
            Items.destroyItem(treasuremap.getWurmId());
            treasuremap = null;
            
            if (chest == null) {
                performer.getCommunicator().sendNormalServerMessage("Treasurechest could not be created.");
                performer.getLogger().info("Treasurechest could not be created.");
                logger.info("Treasurechest could not be created.");
            }
            else {
                Random random = new Random();
                Item lock = null;
                
                try {
                    if (random.nextFloat() <= TreasureHunting.getOptions().getLockChance() / 100f) {
                        float lockQuality = chest.getCurrentQualityLevel() * TreasureHunting.getOptions().getLockMultiplier();

                        // large padlock.
                        lock = ItemFactory.createItem(194, lockQuality, null);
                        chest.setLockId(lock.getWurmId());
                        chest.lock();
                        lock.lock();

                        SoundPlayer.playSound("sound.object.lockunlock", chest.getTileX(), chest.getTileY(), true, 1.0f);
                        performer.getCommunicator().sendNormalServerMessage(String.format("Chest was locked with a lock of %.2f quality.", lockQuality));
                        logger.log(Level.INFO, "Chest was locked with a lock of {0} quality.", lockQuality);
                    }
                    else {
                        performer.getCommunicator().sendNormalServerMessage("Chest was not locked.");
                        logger.info("Chest was not locked.");
                    }
                    
                    Zone zone = Zones.getZone(tilex, tiley, true);
                    chest.setPos((tilex << 2) + 2, (tiley << 2) + 2, performer.getPositionZ(), performer.getStatus().getRotation(), performer.getBridgeId());
                    zone.addItem(chest);
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Chest locking failed.", e);
                    performer.getCommunicator().sendAlertServerMessage("Error creating chest lock, check server console/logfile.");
                    
                    if (lock != null)
                        Items.destroyItem(lock.getWurmId());
                }
            }
        }
        
        return true;
    }
    
    @Override
    public short getActionId() {
        return actionId;
    }
    
    public ActionEntry getActionEntry() {
        return actionEntry;
    }
}

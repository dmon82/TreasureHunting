package com.pveplands.treasurehunting;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

/**
 * Enables using a shovel or pickaxe on a treasure map to dig up the treasure.
 * Accounts of power 2 and above can use any item.
 */
public class DigUpTreasureAction implements ActionPerformer, ModAction {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(DigUpTreasureAction.class));
    private static final Random random = new Random();
    
    private short actionId;
    private ActionEntry actionEntry;
    
    public DigUpTreasureAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Dig for treasure", "digging for treasure", new int[] { 6, 36 });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public boolean action(Action action, Creature performer, Item activated, Item target, short num, float counter) {
        Item chest = null, lock = null;
        
        try {
            if (target.getOwnerId() != performer.getWurmId()) {
                performer.getCommunicator().sendNormalServerMessage("The map needs to be in your inventory.");
                return true;
            }
            
            if (performer.getVehicle() != -10) {
                performer.getCommunicator().sendNormalServerMessage("You need to be on solid ground.");
                return true;
            }
            
            int x = performer.getTileX(), y = performer.getTileY();

            if (performer.isWithinTileDistanceTo(target.getDataX(), target.getDataY(), 0, 1)) {
                performer.getCommunicator().sendNormalServerMessage("You're too far away.");
                return true;
            }
                
            int tile = Server.surfaceMesh.getTile(x, y);
            int type = Tiles.decodeType(tile);

            if (counter == 1.0f) {
                if (activated.getTemplateId() != 20 && (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CLIFF.id) && performer.getPower() == 0) {
                    performer.getCommunicator().sendNormalServerMessage("You need to use a pickaxe to dig here.");
                    return true;
                }
                else if (type != Tiles.Tile.TILE_ROCK.id && activated.getTemplateId() != 25 && performer.getPower() == 0) {
                    performer.getCommunicator().sendNormalServerMessage("You need to use a shovel to dig here.");
                    return true;
                }
                
                int time = (int)Math.max(40, 150 - ((int)activated.getCurrentQualityLevel() / 20 + activated.getRarity()) * 10);
                performer.getCurrentAction().setTimeLeft(time);
                performer.sendActionControl("Digging for treasure", true, time);
                performer.getCommunicator().sendNormalServerMessage("You start to dig for treasure.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to dig for treasure.", performer, 5);
            }
            else {
                int time = performer.getCurrentAction().getTimeLeft();
                
                if (counter * 10f > time) {
                    if (x != target.getDataX() || y != target.getDataY()) {
                        performer.getCommunicator().sendNormalServerMessage("You can't seem to find anything here.");
                        Server.getInstance().broadCastAction(performer.getName() + " frowns as no treasure seems to be here.", performer, 5);
                        return true;
                    }

                    logger.info(String.format("%s will find a treasure at %d, %d and QL being %f", performer.getName(), target.getDataX(), target.getDataY(), target.getCurrentQualityLevel()));

                    // Notice
                    performer.getCommunicator().sendNormalServerMessage("You find a treasure chest!");
                    Server.getInstance().broadCastAction(performer.getName() + " digs up a treasure chest!", performer, 5);
                    
                    // Damages shovel or pickaxe for players only.
                    if (performer.getPower() == 0)
                        activated.setDamage(activated.getDamage() + 0.0015f * activated.getDamageModifier());
                    
                    chest = Treasurechest.CreateTreasurechest(performer, target);
                    if (chest == null)
                        throw new Exception("No treasure chest was generated. Creation returned null, something is wrong!");
                    
                    if (random.nextFloat() <= TreasureHunting.getOptions().getLockChance() / 100f) {
                        float lockQuality = chest.getCurrentQualityLevel() * TreasureHunting.getOptions().getLockMultiplier();
                        
                        // large padlock.
                        lock = ItemFactory.createItem(194, lockQuality, null);
                        chest.setLockId(lock.getWurmId());
                        chest.lock();
                        lock.lock();
                        
                        SoundPlayer.playSound("sound.object.lockunlock", chest.getTileX(), chest.getTileY(), true, 1.0f);

                        logger.log(Level.INFO, "Chest was locked with a lock of {0} quality.", lockQuality);
                    }
                    else {
                        logger.info("Chest was not locked.");
                        
                        if (lock != null)
                            Items.destroyItem(lock.getWurmId());
                    }

                    // add chest to world.
                    Zone zone = Zones.getZone(target.getDataX(), target.getDataY(), true);
                    chest.setPos((target.getDataX() << 2) + 2, (target.getDataY() << 2) + 2, performer.getPositionZ(), performer.getStatus().getRotation(), performer.getBridgeId());
                    zone.addItem(chest);
                    
                    if (Treasuremap.SpawnGuards(performer, target, chest)) {
                        performer.getCommunicator().sendAlertServerMessage("You are ambushed!");
                        Server.getInstance().broadCastAction(performer.getName() + " is ambushed!", performer, 10, false);
                    }
                    
                    //SoundPlayer.playSound("sound.3.4.002.0001.001", x, y, true, 0f);
                    //SoundPlayer.playSound("sound.3.4.002.0001.002", x, y, true, 0f);
                    //SoundPlayer.playSound("sound.3.4.002.0005.001", x, y, true, 0f);
                    SoundPlayer.playSound("sound.chest.open", x, y, true, 0f);
                    
                    // destroy treasure map.
                    Items.destroyItem(target.getWurmId());
                    
                    return true;
                }
            }
            
            if ((action.currentSecond() - 1) % 3 == 0) {
                if (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CLIFF.id) {
                    SoundPlayer.playSound("sound.work.mining" + String.valueOf(random.nextInt(3) + 1), x, y, true, 0f);
                    performer.playAnimation("mine", false);
                }
                else {
                    SoundPlayer.playSound("sound.work.digging" + String.valueOf(random.nextInt(3) + 1), x, y, true, 0f);
                    performer.playAnimation("dig", false);
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Dig for treasure failed. Deleting treasure map %d of effective quality %.2f anyway.", target.getWurmId(), target.getCurrentQualityLevel()), e);
            Items.destroyItem(target.getWurmId());
            
            if (lock != null) {
                logger.info(String.format("Destroying treasuremap large padlock with WurmId %d", lock.getWurmId()));
                
                try {
                    if (chest != null) {
                        chest.unlock();
                        chest.setLockId(-10);
                    }
                    
                    Items.destroyItem(lock.getWurmId());
                }
                catch (Exception inner) {
                    logger.log(Level.SEVERE, "Destroying lock has failed.", inner);
                }
            }
            
            if (chest != null) {
                logger.info(String.format("Destroying failed treasurechest and contents with WurmId %d", chest.getWurmId()));
                
                try {
                    for (Item contents : chest.getItemsAsArray())
                        Items.destroyItem(contents.getWurmId());
                }
                catch (Exception inner) {
                    logger.log(Level.SEVERE, "Destroying chest contents failed.", inner);
                }
                
                Items.destroyItem(chest.getWurmId());
            }
        }
        
        return false;
    }
    
    @Override
    public short getActionId() {
        return actionId;
    }
    
    public ActionEntry getActionEntry() {
        return actionEntry;
    }
}

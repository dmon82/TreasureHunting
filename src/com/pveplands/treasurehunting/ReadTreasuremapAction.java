package com.pveplands.treasurehunting;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

/**
 * Action to enable reading treasure maps using a compass.
 */
public class ReadTreasuremapAction implements ActionPerformer, ModAction {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(ReadTreasuremapAction.class));
    
    private final short actionId;
    private final ActionEntry actionEntry;
    
    public ReadTreasuremapAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Read map", "reading the map", new int[] { 6, 36 });
        ModActions.registerAction(actionEntry);
    }
    
    @Override
    public short getActionId() {
        return actionId;
    }
    
    public ActionEntry getActionEntry() {
        return actionEntry;
    }
    
    @Override
    public boolean action(Action action, Creature performer, Item activated, Item target, short num, float counter) {
        try {
            if (target.getOwnerId() != performer.getWurmId()) {
                performer.getCommunicator().sendNormalServerMessage("You need to have the map in your inventory.");
                return true;
            }
            
            if (counter == 1.0f) {
                if (target.getData1() <= 0) {
                    performer.getCommunicator().sendNormalServerMessage("This treasuremap is too weathered to read.");
                    return true;
                }
                
                performer.getCommunicator().sendNormalServerMessage("You roll out the map, find your bearings, and try to locate the marked spot.");
                
                // time is seconds * 10.
                int time = 150 - ((int)(activated.getCurrentQualityLevel() / 20) + activated.getRarity() + target.getRarity()) * 10;
                performer.getCurrentAction().setTimeLeft(time);
                performer.sendActionControl("Reading the treasuremap", true, time);
                Server.getInstance().broadCastAction(performer.getName() + " starts to read a treasuremap.", performer, 5);
            }
            else {
                int time = performer.getCurrentAction().getTimeLeft();
                
                if (counter * 10f > time) {
                    int xDistance = Math.abs(performer.getTileX() - target.getDataX());
                    int yDistance = Math.abs(performer.getTileY() - target.getDataY());
                    int distance = (int)Math.sqrt(xDistance * xDistance + yDistance * yDistance);
                    int direction = MethodsCreatures.getDir(performer, target.getDataX(), target.getDataY());
                    
                    performer.getCommunicator().sendNormalServerMessage(
                        EndGameItems.getDistanceString(
                            distance, 
                            "marked spot", 
                            MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), direction, "you"), 
                            true));
                    
                    Server.getInstance().broadCastAction(performer.getName() + " folds up a treasure map and looks over yonder.", performer, 5);
                    
                    if (TreasureHunting.getOptions().isDamageCompass()) activated.setDamage(activated.getDamage() + 0.0015f * activated.getDamageModifier());
                    
                    if (TreasureHunting.getOptions().isDamageMap()) {
                        float damage = Math.min(100f, Math.max(0.0015f,
                            new java.util.Random().nextFloat() * (0.0015f * TreasureHunting.getOptions().getDamageMultiplier())));
                        
                        // rare, supreme, and fantastic maps take 10%, 20%, and
                        // 30% less damage respectively.
                        damage = damage * (1f - target.getRarity() / 10f);

                        performer.getCommunicator().sendNormalServerMessage("You wear out the treasure map a bit.");
                        
                        logger.info(String.format("%s (%d) reading %.2f quality treasure map (%d) causing %.6f damage to it.",
                            performer.getName(), performer.getWurmId(), target.getCurrentQualityLevel(), target.getWurmId(), damage));
                        
                        if (target.setDamage(target.getDamage() + damage, true)) {
                            Items.destroyItem(target.getWurmId());
                            performer.getCommunicator().sendNormalServerMessage("The treasure map is in such a bad shape, that you can't make out anything anymore and throw it away.");
                        }
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Reading treasuremap action failed.", e);
            
            return false;
        }
    }
}

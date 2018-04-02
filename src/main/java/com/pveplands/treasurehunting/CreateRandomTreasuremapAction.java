package com.pveplands.treasurehunting;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

/**
 * Enables staff members to create a treasure map with random target coordinates,
 * the quality will be their wand's AuxData, or random if not ranging from 1
 * to 100.
 */
public class CreateRandomTreasuremapAction implements ActionPerformer, ModAction {
    private short actionId;
    private ActionEntry actionEntry;
    
    public CreateRandomTreasuremapAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Create random treasuremap", "creating treasuremap", new int[0]);
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
    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        return performMyAction(performer, source);
    }
    
    @Override
    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Item target, short num, float counter) {
        return performMyAction(performer, source);
    }
    
    private boolean performMyAction(Creature performer, Item activated) {
        if (performer.getPower() <= 1) {
            Logger.getLogger(TreasureHunting.getLoggerName(CreateRandomTreasuremapAction.class))
                .warning(String.format("%s tried to spawn a random treasuremap, this might well fall under exploiting.", performer));
            
            return true;
        }
        
        Item map = Treasuremap.CreateTreasuremap(performer, null, null, null, true);
        
        if (map == null) {
            performer.getCommunicator().sendNormalServerMessage("Treasuremap creation failed, probably couldn't find a suitable spot within 100 tries. Try again.");
        }
        else {
            float quality;

            if (activated.getAuxData() < 1 || activated.getAuxData() > 100) 
                quality = new java.util.Random().nextFloat() * 100f;
            else
                quality = activated.getAuxData();
            
            map.setQualityLevel(quality);

            performer.getInventory().insertItem(map, true);
            performer.getCommunicator().sendNormalServerMessage(String.format("Treasuremap with treasure at %d, %d placed in your inventory.", map.getDataX(), map.getDataY()));
        }
        
        return true;
    }
}

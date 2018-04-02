package com.pveplands.treasurehunting;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Enables staff members to use their wand, to create a treasure map with the
 * target coordinates being on the right-clicked tile. The quality will be set
 * to the wand's AuxData if in range from 1 to 100, or set randomly otherwise.
 */
public class CreateTreasuremapHereAction implements ActionPerformer, ModAction {
    private short actionId;
    private ActionEntry actionEntry;
    
    public CreateTreasuremapHereAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Create treasuremap HERE", "creating treasuremap", new int[0]);
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
    public boolean action(@NotNull Action action, @NotNull Creature performer, @NotNull Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        if (performer.getPower() <= 1) {
            Logger.getLogger(TreasureHunting.getLoggerName(CreateRandomTreasuremapAction.class))
                .warning(String.format("%s tried to spawn a treasuremap at their location, this might well fall under exploiting.", performer));
            
            return true;
        }
        
        Item treasuremap = Treasuremap.CreateTreasuremap(performer, null, null, null, true);
        
        if (treasuremap == null) {
            performer.getCommunicator().sendNormalServerMessage("Treasuremap creation failed, probably couldn't find a suitable spot. Try again.");
        }
        else {
            treasuremap.setDataXY(tilex, tiley);
            
            float quality;
            
            if (source.getAuxData() < 1 || source.getAuxData() > 100)
                quality = new Random().nextFloat() * 100;
            else
                quality = source.getAuxData();
            
            treasuremap.setQualityLevel(Math.min(100, Math.max(1, quality)));

            performer.getInventory().insertItem(treasuremap, true);
            performer.getCommunicator().sendNormalServerMessage("Treasuremap created in your inventory at the selected coordinates, quality level is your wand's AuxData value.");
        }
        
        return true;
    }
}

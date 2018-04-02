package com.pveplands.treasurehunting;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enables removing loadable items from treasure chests (e.g. large magical chest).
 */
public class UnloadFromTreasureAction implements ActionPerformer, ModAction {
    private short actionId;
    private ActionEntry actionEntry;
    
    public UnloadFromTreasureAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Unload", "unloading", new int[] { });
        ModActions.registerAction(actionEntry);
    }
    
    @Override
    public boolean action(@NotNull Action action, @NotNull Creature performer, @NotNull Item source, @NotNull Item target, short num, float counter) {
        return performMyAction(performer, target);
    }
    
    @Override
    public boolean action(@NotNull Action action, @NotNull Creature performer, @NotNull Item target, short num, float counter) {
        return performMyAction(performer, target);
    }
    
    private boolean performMyAction(Creature performer, Item target) {
        try {
            if (!performer.isWithinDistanceTo(target, 8f)) {
                performer.getCommunicator().sendNormalServerMessage("You're too far away.");
                return true;
            }
            
            target.getParent().dropItem(target.getWurmId(), true);
            target.setLastOwnerId(performer.getWurmId());
            Zones.getZone(performer.getTilePos(), true).addItem(target);
        }
        catch (Exception ex) {
            Logger.getLogger(TreasureHunting.getLoggerName(UnloadFromTreasureAction.class))
                .log(Level.SEVERE, String.format("Can't unload item %s (%d) from treasure chest (%d).",
                    target.getName(), target.getWurmId(), target.getParentId()), ex);
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

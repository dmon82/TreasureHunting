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

import java.util.logging.Logger;

/**
 * Action to enable staff to teleport to the treasure map's target coordinates.
 */
public class TeleportToTreasureAction implements ActionPerformer, ModAction {
    private short actionId;
    private ActionEntry actionEntry;
    
    public TeleportToTreasureAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Teleport to treasure", "teleporting to treasure", new int[0]);
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
    public boolean action(@NotNull Action action, @NotNull Creature performer, @NotNull Item source, @NotNull Item target, short num, float counter) {
        return performMyAction(performer, target);
    }
    
    @Override
    public boolean action(@NotNull Action action, @NotNull Creature performer, @NotNull Item target, short num, float counter) {
        return performMyAction(performer, target);
    }
    
    private boolean performMyAction(Creature performer, Item target) {
        if (performer.getPower() <= 1) {
            Logger.getLogger(TreasureHunting.getLoggerName(CreateRandomTreasuremapAction.class))
                .warning(String.format("%s tried to teleport themselves to , this might well fall under exploiting.", performer));
            
            return true;
        }
        
        // X, Y coordinates are saved in Data1 as (x << 16) | y;
        int x = target.getDataX();
        int y = target.getDataY();
        
        if (x < 0 || x >= Zones.worldTileSizeX || y < 0 || y >= Zones.worldTileSizeY) {
            performer.getCommunicator().sendNormalServerMessage(String.format("The map's coordinates seem to be out of whack, you can't teleport to %d, %d.", x, y));
        }
        else {
            performer.getCommunicator().sendNormalServerMessage(String.format("Trying to teleport you to %d, %d.", x, y));
            performer.setTeleportPoints((short)x, (short)y, 0, 0);
            if (performer.startTeleporting())
                performer.getCommunicator().sendTeleport(false);
        }
        
        return true;
    }
}

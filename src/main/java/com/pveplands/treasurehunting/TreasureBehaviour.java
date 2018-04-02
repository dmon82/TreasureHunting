package com.pveplands.treasurehunting;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import java.util.ArrayList;
import java.util.List;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.jetbrains.annotations.NotNull;

public class TreasureBehaviour implements BehaviourProvider, ModAction {
    public TreasureBehaviour() {
    }
    
    @Override
    public List<ActionEntry> getBehavioursFor(@NotNull Creature performer, @NotNull Item activated, @NotNull Item target) {
        return getMyBehaviours(performer, activated, target, 0, 0, false, 0);
    }
    
    @Override
    public List<ActionEntry> getBehavioursFor(@NotNull Creature performer, @NotNull Item target) {
        return getMyBehaviours(performer, null, target, 0, 0, false, 0);
    }
    
    @Override
    public List<ActionEntry> getBehavioursFor(@NotNull Creature performer, @NotNull Item activated, int tilex, int tiley, boolean onSurface, int tile) {
        return getMyBehaviours(performer, activated, null, tilex, tiley, onSurface, tile);
    }
    
    private List<ActionEntry> getMyBehaviours(Creature performer, Item activated, Item target, int tilex, int tiley, boolean onSurface, int tile) {
        List<ActionEntry> list = new ArrayList<>();
        TreasureOptions options = TreasureHunting.getOptions();

        short menuItems = 0;
        
        if (performer.getPower() > 1) {
            menuItems--;
            list.add(options.getCreatemapAction().getActionEntry());
            
            if (tile != 0) {
                menuItems -= 2;
                list.add(options.getCreatehereAction().getActionEntry());
                list.add(options.getChestAction().getActionEntry());
            }
            
            if (target != null && target.getTemplateId() == TreasureHunting.getOptions().getTreasuremapTemplateId()) {
                menuItems--;
                list.add(options.getTeleportAction().getActionEntry());
            }
            
            if (performer.getPower() > 4) {
                menuItems--;
                list.add(options.getReloadAction().getActionEntry());
            }
        }
        
        if (menuItems < 0)
            list.add(0, new ActionEntry(menuItems, "Treasuremap", "Treasuremap"));
        
        if (target != null && target.getTemplateId() == TreasureHunting.getOptions().getTreasuremapTemplateId()) {
            if (activated != null) {
                if (activated.getTemplateId() == ItemList.compass)
                    list.add(options.getReadmapAction().getActionEntry());
                else if (activated.getTemplateId() == ItemList.shovel || activated.getTemplateId() == ItemList.pickAxe)
                    list.add(options.getDigAction().getActionEntry());
            }
        }
        else if (target != null && target.getTemplate().isTransportable() && target.getParentId() != -10) {
            Item parent = target.getParentOrNull();
            
            if (parent != null && parent.getTemplateId() == ItemList.treasureChest)
                list.add(options.getUnloadAction().getActionEntry());
        }
        
        if (list.isEmpty())
            list = null;
        
        return list;
    }
    
    private boolean atTreasureLocation(Creature performer, Item map) {
        return performer.getTileX() == map.getDataX() &&
            performer.getTileY() == map.getDataY();
    }
}

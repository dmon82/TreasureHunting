package com.pveplands.treasurehunting;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class ReloadConfigAction implements ActionPerformer, ModAction {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(ReloadConfigAction.class));
    
    private short actionId;
    private ActionEntry actionEntry;
    
    public ReloadConfigAction() {
        actionId = (short)ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Reload config", "reloading", MiscConstants.EMPTY_INT_ARRAY);
        ModActions.registerAction(actionEntry);
    }

    public short getActionId() {
        return actionId;
    }

    public ActionEntry getActionEntry() {
        return actionEntry;
    }
    
    @Override
    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Item target, short num, float counter) {
        return performMyAction(performer);
    }
    
    @Override
    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item target, short num, float counter) {
        return performMyAction(performer);
    }
    
    @Override
    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        return performMyAction(performer);
    }
    
    public boolean performMyAction(Creature performer) {
        if (performer.getPower() <= 1) {
            logger.warning(String.format("%s tried to reload the TreasureHunting config, likely trying to execute actions they shouldn't have access to, perhaps looking for exploits.", performer));
            return true;
        }
        
        Path path = Paths.get("mods/TreasureHunting.properties");
        
        if (!Files.exists(path)) {
            performer.getCommunicator().sendAlertServerMessage("The config file seems to be missing.");
            return true;
        }
        
        InputStream stream = null;
        
        try {
            performer.getCommunicator().sendAlertServerMessage("Opening the config file.");
            stream = Files.newInputStream(path);
            Properties properties = new Properties();
            
            performer.getCommunicator().sendAlertServerMessage("Reading from the config file.");
            properties.load(stream);
            
            logger.info("Reloading configuration.");
            performer.getCommunicator().sendAlertServerMessage("Loading all options.");
            TreasureHunting.getOptions().configure(properties);
            
            logger.info("Configuration reloaded.");
            performer.getCommunicator().sendAlertServerMessage("The config file has been reloaded.");
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while reloading properties file.", ex);
            performer.getCommunicator().sendAlertServerMessage("Error reloading the config file, check the server log.");
        }
        finally {
            try {
                if (stream != null)
                    stream.close();
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, "Properties file not closed, possible file lock.", ex);
                performer.getCommunicator().sendAlertServerMessage("Error closing the config file, possible file lock.");
            }
        }
        return true;
    }
}

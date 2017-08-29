package com.pveplands.treasurehunting;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplateCreator;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ItemTemplatesCreatedListener;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

/**
 * Treasure Hunting may create treasure maps found by digging, mining, fishing,
 * or killing creatures as specified in the config file. Maps can be read by
 * using a compass on them. Once at the right spot, a pickaxe or shovel can be
 * used on the map to dig up the treasure chest. Rewards and monster spawns can
 * be configured.
 * 
 * GameMasters may create treasure maps using their wands, teleport to the
 * target location, and use any tool to dig up the treasure.
 * 
 * This mod was brought to you by http://pveplands.com
 * Forum post: 
 */
public class TreasureHunting implements WurmServerMod, Configurable, Initable, PreInitable, ItemTemplatesCreatedListener, ServerStartedListener {
    private static final Logger logger = Logger.getLogger(getLoggerName(TreasureHunting.class));
    public static String getLoggerName(Class c) { return String.format("%s (v%s)", c.getName(), c.getPackage().getImplementationVersion()); }
    
    private static final Random random = new Random();
    
    private static final TreasureOptions options = new TreasureOptions();
    public static TreasureOptions getOptions() {
        return options;
    }
    
    public TreasureHunting () {
    }
    
    @Override
    public void preInit() {
        AddMethodCallsTerraforming();
        AddMethodCallsMining();
        AddMethodCallsFishing();
        AddMethodCallsHunting();
        
        ModActions.init();
    }
    
    @Override
    public void configure(Properties p) {
        logger.info("Loading configuration.");
        options.configure(p);
        logger.info("Configuration loaded.");
    }
    
    @Override
    public void onItemTemplatesCreated() {
        try {
            ItemTemplateCreator.createItemTemplate(options.getTreasuremapTemplateId(), "treasure map", "treasure maps", "excellent", "good", "ok", "poor",
                "An old weathered treasure map with a big X marking a spot. What could you use on it, to get directions?",
                new short[] { 48, 157, 187 }, (short)640, (short)1, 0, 3024000L, 5, 5, 5, -10,
                MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY, "model.resource.sheet.", 5f, 500, (byte)33, 5000, true);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not create treasure map item.", e);
        }
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(options.setCreatemapAction(new CreateRandomTreasuremapAction()));
        ModActions.registerAction(options.setCreatehereAction(new CreateTreasuremapHereAction()));
        ModActions.registerAction(options.setTeleportAction(new TeleportToTreasureAction()));
        ModActions.registerAction(options.setReloadAction(new ReloadConfigAction()));
        ModActions.registerAction(options.setReadmapAction(new ReadTreasuremapAction()));
        ModActions.registerAction(options.setDigAction(new DigUpTreasureAction()));
        ModActions.registerAction(options.setUnloadAction(new UnloadFromTreasureAction()));
        ModActions.registerAction(options.setChestAction(new SpawnTreasurechestAction()));
        ModActions.registerAction(options.setBehaviours(new TreasureBehaviour()));
    }
    
    /**
     * Injects calls to our mod whenever a creature dies and there were
     * attackers (i.e. it did not die of old age). This will create treasure
     * maps if applicable.
     */
    private void AddMethodCallsHunting() {
        try {
            HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature")
                .getMethod("die", "(Z)V")
                .insertBefore("{ if (this.getLatestAttackers().length > 0) com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(null, null, null, this); }");
                                //com.pveplands.treasurehunting.Treasuremap.debugDeath(this);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can't add methods to hunting.", e);
        }
    }
    
    /**
     * Injects calls to mining, whenever rock shards are created during mining
     * a wall, and leveling or flatting a cave floor or ceiling.
     */
    private void AddMethodCallsMining() {
        try {
            CtMethod wallMethod = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.CaveWallBehaviour")
                .getMethod("action", "(Lcom/wurmonline/server/behaviours/Action;Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IIZIISF)Z");
            
            wallMethod.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (methodCall.getMethodName().equals("putItemInfrontof")) {
                        wallMethod.insertAt(
                            methodCall.getLineNumber() + 1, 
                            "{ com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(performer, source, performer.getSkills().getSkill(1008), (com.wurmonline.server.creatures.Creature)null); }"
                        );
                    }
                }
            });
            
            CtMethod tileMineMethod = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.CaveTileBehaviour")
                .getMethod("handle_MINE", "(Lcom/wurmonline/server/behaviours/Action;Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IISFI)Z");
            
            tileMineMethod.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (methodCall.getMethodName().equals("putItemInfrontof")) {
                        tileMineMethod.insertAt(
                            methodCall.getLineNumber() + 1, 
                            "{ com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(performer, source, performer.getSkills().getSkill(1008), (com.wurmonline.server.creatures.Creature)null); }"
                        );
                    }
                }
            });
            
            // private boolean flatten(Creature performer, Item source, int tile, int tilex, int tiley, float counter, Action act, int dir) {
            CtMethod tileFlattenMethod = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.CaveTileBehaviour")
                .getMethod("flatten", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IIIFLcom/wurmonline/server/behaviours/Action;I)Z");
            
            tileFlattenMethod.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (methodCall.getMethodName().equals("putItemInfrontof")) {
                        tileFlattenMethod.insertAt(
                            methodCall.getLineNumber() + 1, 
                            "{ com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(performer, source, performer.getSkills().getSkill(1008), (com.wurmonline.server.creatures.Creature)null); }"
                        );
                    }
                }
            });
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can't add method calls to mining.", e);
        }
    }
    
    /**
     * Injects a call to fishing, whenever it tries to give the player a
     * certain achievement, which is equivalent to a fish being called. But
     * the achievement call is more unique in the vanilla call. It creates a
     * treasure map when conditions are met.
     */
    private void AddMethodCallsFishing() {
        try {
            CtMethod method = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Fish")
                .getMethod("fish", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IIIFLcom/wurmonline/server/behaviours/Action;)Z");
            
            method.instrument(new ExprEditor() {
                private boolean done = false;
                
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (!done && methodCall.getMethodName().equals("achievement")) {
                        done = true;
                        
                        method.insertAt(
                            methodCall.getLineNumber() + 1, 
                            "{ com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(performer, source, performer.getSkills().getSkill(10033), (com.wurmonline.server.creatures.Creature)null); }"
                        );
                    }
                }
            });
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can't add methods to fishing.", e);
        }
    }
    
    /**
     * Injects calls to the terraforming class whenever something is dug,
     * like dirt, clay, or tar, and when flattening or leveling modifies
     * the terrain. It creates a treasuremap when conditions are met.
     */
    private void AddMethodCallsTerraforming() {
        try {
            CtMethod method = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Terraforming")
                .getMethod("dig", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IIIFZLcom/wurmonline/mesh/MeshIO;)Z");
            
            method.instrument(new ExprEditor() {
                private boolean done = false;
                
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (!done && methodCall.getMethodName().equals("createItem")){
                        method.insertAt(methodCall.getLineNumber() + 1, "{ com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(performer, source, performer.getSkills().getSkill(1009), (com.wurmonline.server.creatures.Creature)null); }");
                        done = true;
                    }
                }
            });
            
            // private static final boolean flatten(long borderId, Creature performer, Item source, int tile, int tilex, int tiley, int endX, int endY, int numbCorners, float counter, Action act) {
            CtMethod flatten = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Flattening")
                .getMethod("flatten", "(JLcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IIIIIIFLcom/wurmonline/server/behaviours/Action;)Z");
            
            flatten.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (methodCall.getMethodName().equals("resetChangedTiles")) {
                        flatten.insertAt(methodCall.getLineNumber() + 1, "{ com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap(performer, source, performer.getSkills().getSkill(1009), (com.wurmonline.server.creatures.Creature)null); }");
                        logger.log(Level.INFO, "Found method call in Flattening, inserting call to treasuremap generation.");
                }
                }
            });
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can't add method calls to terraforming.", e);
        }
    }
    
    @Override
    public void init() {
    }
}

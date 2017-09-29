package com.pveplands.treasurehunting;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for the treasure map item.
 */
public class Treasuremap {
    private static final Random random = new Random();
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(Treasuremap.class));
    
    /**
     * Spawns a set of creatures when a treasure is dug up.
     * 
     * @param performer Player who dug up the treasure.
     * @param map The treasure map item.
     * @param chest The chest that was spawned.
     * @return true if one or more creatures were spawns, or false otherwise.
     */
    public static boolean SpawnGuards(Creature performer, Item map, Item chest) {
        TreasureOptions options = TreasureHunting.getOptions();
        boolean spawnedGuards = false;
        double quality = map.getCurrentQualityLevel() + map.getRarity() * 10;
        int tier = (int)Math.min(9, Math.max(0, quality / 10d));
        
        int weightToSpawn = options.getSpawnWeights()[tier];
        int weightLimit = options.getSpawnLimits()[tier];
        
        logger.info(String.format("%.2f treasure map, tier %d, effective quality %.2f will start spawning %d weight, weight limit %d for %s",
            map.getCurrentQualityLevel(), tier, quality, weightToSpawn, weightLimit, performer.getName() ));
        
        while (weightToSpawn > 0) {
            int x = chest.getTileX() + (random.nextInt(3) * (random.nextBoolean() ? -1 : 1));
            int y = chest.getTileY() + (random.nextInt(3) * (random.nextBoolean() ? -1 : 1));
            
            try {
                //int id = SpawnCreature(weightToSpawn, weightLimit);
                
                int highSpawn = -1, lowSpawn = -1, id;
        
                for (int i = options.getGroupWeights().length - 1; i >= 0; i--) {
                    if (weightToSpawn >= options.getGroupWeights()[i] && options.getGroupWeights()[i] <= weightLimit) {
                        if (highSpawn < 0)
                            highSpawn = i;
                        else {
                            lowSpawn = i;
                            break;
                        }
                    }
                }

                if (lowSpawn < 0)
                    lowSpawn = highSpawn;

                if (highSpawn < 0)
                    throw new Exception(String.format("Could not determine a spawnable creature group for totalWeigh %d, with limit %d.", weightToSpawn, weightLimit));

                int spawnWeight = options.getGroupWeights()[highSpawn] + options.getGroupWeights()[lowSpawn];
                
                if (random.nextInt(spawnWeight) <= options.getGroupWeights()[highSpawn]) {
                    id = options.getSpawnGroups()[highSpawn][random.nextInt(options.getSpawnGroups()[highSpawn].length)];
                    
                    logger.log(Level.INFO, String.format("Available spawn weight: %d, heaviest available: %d, spawning ID %d from high group #%d.",
                        weightToSpawn, weightLimit, id, highSpawn));
                    
                    weightToSpawn -= options.getGroupWeights()[highSpawn];
                }
                else {
                    id = options.getSpawnGroups()[lowSpawn][random.nextInt(options.getSpawnGroups()[lowSpawn].length)];
                    
                    logger.log(Level.INFO, String.format("Available spawn weight: %d, heaviest allowed: %d, spawning ID %d from low group %d.",
                        weightToSpawn, weightLimit, id, lowSpawn));
                    
                    weightToSpawn -= options.getGroupWeights()[lowSpawn];
                }
                
                CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(id);
                int age = (int)(random.nextFloat() * Math.min(48, template.getMaxAge()));
                byte gender = (byte)(random.nextBoolean() ? 1 : 0);
                
                logger.info(String.format("Spawning %s at age %d.", template.getName(), age));
                String name = String.format("%s ambushing %s", template.getName(), performer.getName());
                
                Creature.doNew(id, true, (x << 2) + 2, (y << 2) + 2, random.nextFloat() * 360f, 0, name, gender, (byte)0, (byte)0, false, (byte)age);
                SoundPlayer.playSound(template.getHitSound(gender), x, y, true, 0.3f);
                
                spawnedGuards = true;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to spawn treasure chest guard.", e);
            }
        }
        
        return spawnedGuards;
    }
    
    /**
     * Returns a creature template ID to spawn for treasure chest guardians.
     * 
     * @param totalWeight Total spawn weight that's available.
     * @param limit The heaviest creature that can spawn.
     * @return Creature template ID to spawn.
     * @throws Exception Config options are probably invalid.
     */
    public static int SpawnCreature(int totalWeight, int limit) throws Exception {
        TreasureOptions options = TreasureHunting.getOptions();
        int highSpawn = -1, lowSpawn = -1;
        int spawnTemplateId = 0;
        
        for (int i = options.getGroupWeights().length - 1; i >= 0; i--) {
            if (totalWeight >= options.getGroupWeights()[i] && options.getGroupWeights()[i] <= limit) {
                if (highSpawn < 0)
                    highSpawn = i;
                else {
                    lowSpawn = i;
                    break;
                }
            }
        }
        
        if (lowSpawn < 0)
            lowSpawn = highSpawn;
        
        if (highSpawn < 0)
            throw new Exception(String.format("Could not determine a spawnable creature group for totalWeigh %d, with limit %d.", totalWeight, limit));
        
        if (options.getSpawnGroups()[highSpawn].length == 0)
            throw new Exception(String.format("The spawn group %d is empty, can't spawn creatures.", highSpawn));
        
        if (options.getSpawnGroups()[lowSpawn].length == 0)
            throw new Exception(String.format("The spawn group %d is empty, can't spawn creatures.", lowSpawn));
        
        int spawnWeight = options.getGroupWeights()[highSpawn] + options.getGroupWeights()[lowSpawn];
        
        if (random.nextInt(spawnWeight) <= options.getGroupWeights()[highSpawn])
            spawnTemplateId = options.getSpawnGroups()[highSpawn][random.nextInt(options.getSpawnGroups()[highSpawn].length)];
        
        spawnTemplateId = options.getSpawnGroups()[lowSpawn][random.nextInt(options.getSpawnGroups()[lowSpawn].length)];
        
        if (spawnTemplateId <= 0)
            throw new Exception("One of the creature template IDs for spawning is equal or less than 0, that's not a valid ID.");
        
        return spawnTemplateId;
    }
    
    /**
     * Creates a new treasure map when chances are met. The Data1 (DataX, and
     * DataY) field contains the X, Y tile coordinates of the treasure location.
     * 
     * @param performer Player or creature performing an action, NULL if killed is given.
     * @param activated Activated item (e.g. shovel), NULL if killed is given.
     * @param skill The skill being used (e.g. Digging), NULL if killed is given.
     * @param killed The creature that died, NULL if performer, activated, and skill are given.
     * @return NULL if it failed, or an instance of Item, the treasure map.
     */
    public static Item CreateTreasuremap(Creature performer, Item activated, Skill skill, Creature killed) {
        return CreateTreasuremap(performer, activated, skill, killed, false);
    }
    
    /**
     * Creates a new treasure map when chances are met. The Data1 (DataX, and
     * DataY) field contains the X, Y tile coordinates of the treasure location.
     * 
     * @param performer Player or creature performing an action, NULL if killed is given.
     * @param activated Activated item (e.g. shovel), NULL if killed is given.
     * @param skill The skill being used (e.g. Digging), NULL if killed is given.
     * @param killed The creature that died, NULL if performer, activated, and skill are given.
     * @param gamemaster For staff utility functions, will always create a map.
     * @return NULL if it failed, or an instance of Item, the treasure map.
     */
    public static Item CreateTreasuremap(Creature performer, Item activated, Skill skill, Creature killed, boolean gamemaster) {
        // If this is called from a GM utility function, it should bypass the
        // check for chances and other conditions.
        if (!gamemaster && !ShouldCreateTreasuremap(performer, activated, skill, killed))
            return null;
        
        // Pickaxe is an identifier for surface mining, and used in the
        // method ShouldCreateTreasuremap, because it's so much faster than
        // regular mining and really needs a separate drop chance. Also, the
        // code wasn't designed for it, so this is a little workaround.
        if (!gamemaster && skill != null && performer != null && skill.getNumber() == 10009 /*SkillList.PICKAXE*/)
            skill = performer.getSkills().getSkillOrLearn(1008); // Mining.
        
        TreasureOptions options = TreasureHunting.getOptions();
        Item treasuremap = null;
        
        try {
            // TODO: remove profiling??
            long profiling = System.nanoTime();
            
            int x, y;
            int tries = 0;
            int padding = Zones.worldTileSizeX / 20;
            int maxHeight = 0, minHeight = 0;
            boolean foundSpot = false, isWaterOrLava = false;
            
            int waterCount = 0, heightCount = 0, altarCount = 0, villageCount = 0;
            
            while (true) {
                // Gets random X, Y tile coordinates. Stays away from the server
                // border, 5 % tiles of the world's size.
                x = random.nextInt(Zones.worldTileSizeX - padding * 2) + padding;
                y = random.nextInt(Zones.worldTileSizeY - padding * 2) + padding;

                if (!IsAcceptableDistance(performer != null ? performer : killed, x, y)) {
                    if (++tries > options.getCreationTries()) {
                        logger.warning("Could not find a treasure location that isn't too close or too far away.");
                        break; // stop trying.
                    }
                    
                    continue; // try again.
                }
                
                // Reset this variable every time we have new coordinates.
                isWaterOrLava = false;

                maxHeight = Integer.MIN_VALUE;
                minHeight = Integer.MAX_VALUE;

abort:          for (int ix = x; ix < x + 3; ix++) {
                    for (int iy = y; iy < y + 3; iy++) {
                        int tile = Server.surfaceMesh.getTile(ix, iy);
                        int height = Tiles.decodeHeight(tile);

                        maxHeight = Math.max(maxHeight, height);
                        minHeight = Math.min(minHeight, height);

                        // Seems a most likey occurance.
                        if (Terraforming.isTileUnderWater(tile, ix, iy, true) || Tiles.decodeType(tile) == Tiles.Tile.TILE_LAVA.id) {
                            isWaterOrLava = true;
                            break abort; // exit the outer loop rightaway!
                        }
                    }
                }

                // Too many tries. This qualified for a map, but we didn't find a good spot.
                if (++tries > options.getCreationTries()) break;
                
                // All of the below proceeds to pick a new random coordinate.
                if (isWaterOrLava) { waterCount++; continue; }
                if (maxHeight - minHeight > options.getMaxHeightDiff()) { heightCount++; continue; }
                if (Terraforming.isAltarBlocking(performer, x, y)) { altarCount++; continue; }
                if (Villages.getVillageWithPerimeterAt(x, y, true) != null) { villageCount++; continue; }
                
                foundSpot = true;
                break;
            }
            
            float elapsed = (System.nanoTime() - profiling) / 1000000f;
            logger.info(String.format("%d tries took %.6f ms. Height diff %d.", tries, elapsed, maxHeight - minHeight));
            
            // How many times we failed to find a good spot, and what were the
            // reasons that the randomly picked coordinates weren't good.
            if (!foundSpot) {
                if (killed != null)
                    logger.info(String.format("No suitable treasuremap spot found for killed creature %s after %d tries. Failing at Water=%d, Height=%d, Altar=%d, Village=%d.",
                        killed, tries, waterCount, heightCount, altarCount, villageCount));
                else if (performer != null)
                    logger.log(Level.INFO, String.format("No suitable treasuremap spot found for %s after %d tries. Failing at Water=%d, Height=%d, Altar=%d, Village=%d.",
                        performer.getName(), tries, waterCount, heightCount, altarCount, villageCount));
                
                return null;
            }
            
            // Vanilla-like rarity chance/
            byte rarity = GetMapRarity(performer);
            if (!gamemaster && !options.isExtraSwirl() && rarity > 0 && performer != null) // extraSwirl ALWAYS does this, so don't duplicate it!
                performer.playPersonalSound("sound.fx.drumroll");

            double diff = options.getMapBaseDiff();

            // Map QL is based on skillchecks, for killed creatures it's
            // weaponless fighting in vanilla code. Yes, even trolls, but they
            // have 70 in Huge Club as well. No Fighting, Normal, Aggressive,
            // or Defensive fighting skill.
            if (skill == null && killed != null) {
                skill = killed.getSkills().getSkill(SkillList.WEAPONLESS_FIGHTING);
                
                long[] attackers = killed.getLatestAttackers();
                int fightingSkill = 0;
                int playerAttackers = 0;
                
                for (long attacker : attackers) {
                    Player player = Players.getInstance().getPlayerOrNull(attacker);
                    if (player != null) {
                        fightingSkill += (int)player.getSkills().getSkillOrLearn(SkillList.GROUP_FIGHTING).getKnowledge();
                        playerAttackers++;
                    }
                }
                
                attackers = null;
                
                // Difficulty is reduced by the average fighting skill of all
                // players in percent, i.e. 56 average FS:
                // difficulty = (maximum reducable number) * 56%
                // e.g. difficulty = (70 - 4) * 0.56 = 36.96
                if (playerAttackers > 0)
                    diff = (options.getMapBaseDiff() - 4d) * ((fightingSkill / playerAttackers) / 100d);
            }
            
            // Reduce diff by skill level, tool level, and tool rarity.
            if (skill != null) diff -= skill.getKnowledge() / 10d;
            if (activated != null) diff -= activated.getCurrentQualityLevel() / 10d + activated.getRarity() * 5d;
            
            // Normalise diff to a difficulty of at least 4.
            diff = Math.max(4d, diff);
            
            double power = 0d;
            
            // Uniques always drop maps at QL 90+, if we can't make a skillcheck
            // for some reason, the quality will be completely random.
            if (skill != null) power = skill.skillCheck(diff, 0d, false, 4f);
            else power = (killed != null && killed.isUnique() ? (90d + random.nextDouble() * 10d) : random.nextDouble() * 100d);
            
            treasuremap = ItemFactory.createItem(options.getTreasuremapTemplateId(), Math.min(99f, Math.max(1.0f, (float)power)), rarity, null);
            
            // This sets the Data1 value to (x << 16) | y.
            treasuremap.setDataXY(x, y);
            
            // Vanilla behaviour, for items less than QL 1, it'll set it to 1.00
            // and puts half of it as damage.
            if (power < 1.0) treasuremap.setDamage((float)-power / 2f);
            
            // Remember the skill number and skill value in Data2. This is
            // currently not used, but was an idea. The code can remain in,
            // as it has no effect.
            if (skill != null) treasuremap.setData2((skill.getNumber() << 16 | (int)skill.getKnowledge() * 100));
            else {
                try { treasuremap.setData2(SkillList.GROUP_FIGHTING << 16 | (int)killed.getSkills().getSkill(SkillList.WEAPONLESS_FIGHTING).getKnowledge() * 100); }
                catch (Exception e) { treasuremap.setData2(SkillList.GROUP_FIGHTING << 16); }
            }
            
            // TODO: set log level to FINE after alpha/beta?
            if (performer != null && !gamemaster) {
                logger.log(Level.INFO, String.format("%s found a %f quality treasure map for %d, %d using %.2f %s. Skillcheck difficulty was %.2f. Their location is %d, %d.",
                    performer.getName(), treasuremap.getCurrentQualityLevel(), treasuremap.getDataX(), treasuremap.getDataY(), skill.getKnowledge(), skill.getName(), 
                    diff,
                    performer.getTileX(), performer.getTileY()));

                // Notify the player and force the map into their inventory.
                if (options.isExtraWarning()) performer.getCommunicator().sendAlertServerMessage("You find a treasuremap!");
                else performer.getCommunicator().sendNormalServerMessage("You find a treasuremap!");
                if (options.isExtraSwirl()) performer.playPersonalSound("sound.fx.drumroll");

                performer.getInventory().insertItem(treasuremap, true);
            }
            else {
                if (killed != null) {
                    killed.getInventory().insertItem(treasuremap, true);
                    
                    logger.info(String.format("A %f quality treasure map was created in the corpse of %s at %d, %d. Skillcheck difficulty for %.2f %s was %.2f.",
                        treasuremap.getCurrentQualityLevel(), killed.getName(), killed.getTileX(), killed.getTileY(),
                        skill == null ? 0d : skill.getKnowledge(), skill == null ? "-noskill-" : skill.getName(), diff));
                    
                    try {
                        logger.info("Attackers: " + killed.getLatestAttackers().length);
                        
                        for (long attackerId : killed.getLatestAttackers()) {
                            Player attacker = Players.getInstance().getPlayerOrNull(attackerId);
                            if (attacker == null)
                                continue;
                            
                            if (options.isExtraWarning()) {
                                switch (random.nextInt(4)) {
                                    default:
                                    case 0:
                                        if (killed.isHuman() || killed.isAggHuman())
                                            attacker.getCommunicator().sendAlertServerMessage(String.format("You notice that %s is grasping on to something.", killed.getName()));
                                        else
                                            attacker.getCommunicator().sendAlertServerMessage(String.format("You notice that %s seems to have something stuck in its mouth.", killed.getName()));
                                        break;
                                    case 1:
                                        attacker.getCommunicator().sendAlertServerMessage("You smell old parchment and salt water.");
                                        break;
                                    case 2:
                                        attacker.getCommunicator().sendAlertServerMessage(String.format("You find it odd how %s seemed to hide something precious before it succumbs to death.", killed.getName()));
                                        break;
                                    case 3:
                                        if (killed.isHuman() || killed.isAggHuman())
                                            attacker.getCommunicator().sendAlertServerMessage(String.format("%s seems to have been searching for something in the area, maybe you should check the corpse?", killed.getName()));
                                        else
                                            attacker.getCommunicator().sendAlertServerMessage(String.format("You hear some crumpling of parchment from the corpse of %s.", killed.getName()));
                                        break;
                                }
                                
                                attacker.playPersonalSound("sound.fx.drumroll");
                            }
                            
                            logger.info(String.format("Attacker: %s (%d)", attacker.getName(), attacker.getWurmId()));
                        }
                    }
                    catch (Exception inner) {
                        logger.log(Level.SEVERE, "Some attackers of the creature could not be found anymore.", inner);
                    }
                }
                else {
                    if (!gamemaster) {
                        logger.info("Nowhere to put the treasuremap, destroying item.");
                        Items.destroyItem(treasuremap.getWurmId());
                        return null;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not create treasure map.", e);
        }
        
        return treasuremap;
    }
    
    /**
     * Determines if a treasure map should be created either for a killed
     * creature (in which case performer, activated and skill should be NULL),
     * or for a performed activity, i.e. a skill use (in which case the
     * creature should be NULL).
     * 
     * @param performer The player or creature that performed an action.
     * @param activated The active item that was used (e.g. shovel).
     * @param skill The skill that was used (e.g. digging).
     * @param killed The killed creature.
     * @return True if a map should be created, otherwise false.
     */
    public static boolean ShouldCreateTreasuremap(Creature performer, Item activated, Skill skill, Creature killed) {
        TreasureOptions options = TreasureHunting.getOptions();
        
        if (killed != null) {
            Creature caretaker = null;
            
            // TODO: Chance this to FINE logging level.
            try {
                if (killed.getCareTakerId() != -10)
                    caretaker = Server.getInstance().getCreature(killed.getCareTakerId());
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Could not get caretaker id {0}.", killed.getCareTakerId());
                logger.log(Level.SEVERE, null, e);
            }
            
            logger.log(Level.INFO, String.format("Checking if creature %s (id %d) at %d, %d aged %d (cared for by? %s) should drop a treasuremap.",
                killed.getName(), killed.getWurmId(), killed.getTileX(), killed.getTileY(), killed.getStatus().age, 
                caretaker == null ? "Nobody" : caretaker.getName()));
            
            // Should uniques drop a map?
            if (killed.isUnique()) {
                if (options.getMapUniqueChance() <= 0)
                    return false;
                
                return random.nextInt(options.getMapUniqueChance()) == 0;
            }
            
            // Disable this way of dropping entirely.
            if (options.getMapHuntingChance() <= 0)
                return false;
            
            // The dying creature has to have been attacked by a player, or
            // a player's pet or dominated creature.
            boolean hasPlayerAttacker = false;
            try {
                for (long creatureId : killed.getLatestAttackers()) {
                    Creature attacker = Server.getInstance().getCreature(creatureId);
                    
                    if (attacker.isPlayer() || (attacker.isDominated() && Server.getInstance().getCreature(attacker.dominator).isPlayer())) {
                        hasPlayerAttacker = true;
                        break;
                    }
                }                
            }
            catch (NoSuchPlayerException | NoSuchCreatureException e) {
                logger.log(Level.SEVERE, String.format("Could not determine if killed creature %s (%d) has a player as attacker.", killed.getName(), killed.getWurmId()), e);
            }
            
            if (!hasPlayerAttacker)
                return false;
            
            // Check if killed creature should drop a map.
            // Champion creatures have a 33 % to drop.
            for (int creatureId : options.getMapDrops())
                if (creatureId == killed.getTemplate().getTemplateId())
                    return ((killed.isChampion() && random.nextInt(3) == 0)) || (random.nextInt(options.getMapHuntingChance()) == 0);
            
            // This creature should not drop a map afterall.
            return false;
        }
        
        // All arguments were null, the method should not be called like this.
        if (performer == null || activated == null || skill == null) {
            logger.info("Performer, activated, or skill is null, and killed was null as well. Should not create treasuremap.");
            return false;
        }
        
        // Checks the odds for any one skill being used.
        switch (skill.getNumber()) {
            case SkillList.DIGGING:
                if (options.getMapDiggingChance() <= 0)
                    return false;
                
                return random.nextInt(options.getMapDiggingChance()) == 0;
            case SkillList.FISHING:
                if (options.getMapFishingChance() <= 0)
                    return false;
                
                return random.nextInt(options.getMapFishingChance()) == 0;
            case SkillList.MINING:
                if (options.getMapMiningChance() <= 0)
                    return false;
                
                return random.nextInt(options.getMapMiningChance()) == 0;
            case SkillList.PICKAXE:
                if (options.getMapSurfaceMiningChance() <= 0)
                    return false;
                
                return random.nextInt(options.getMapSurfaceMiningChance()) == 0;
            default:
                logger.warning("Tried to create treasuremap for unapproved activity " + skill.getName());
                break;
        }
        
        return false;
    }
    
    /**
     * Picks a rarity for the created treasure map, mimicks vanilla behaviour.
     * 
     * @param performer The creature who gets the map, their rarity window will be checked.
     * @return Returns a rarity, 0 none, 1 rare, 2 supreme, or 3 fantastic.
     */
    private static byte GetMapRarity(Creature performer) {
        if (performer != null)
            return performer.getRarity();
        
        /**
         * To emulate player rarity window (20 seconds if random(3600), we
         * do random(3600/20) and then go from there.
         * */
        if (random.nextInt(180) > 0)
            return 0;

        if (random.nextFloat() * 10000f <= 1f)
            return 3;

        if (random.nextInt(100) == 0)
            return 2;

        if (random.nextBoolean())
            return 1;

        return 0;
    }
    
    /**
     * Checks if the distance from a creature (player or killed creature) is
     * within the desired bounds set in the properties file.
     * 
     * @param from Creature to take the tile distance from.
     * @param toX Target X tile coordinate.
     * @param toY Target Y tile coordinate.
     * @return True if the distance is within bounds, otherwise false.
     */
    public static boolean IsAcceptableDistance(Creature from, int toX, int toY) {
        int distanceFromPlayer = Math.min(Math.abs(toX - from.getTileX()), Math.abs(toY - from.getTileY()));

        logger.info(String.format("%s distance to %d, %d is %d", from, toX, toY, distanceFromPlayer));
        
        return distanceFromPlayer >= TreasureHunting.getOptions().getMinTreasureDistance()
            && distanceFromPlayer <= TreasureHunting.getOptions().getMaxTreasureDistance();
    }
    
    /**
     * Debug code to determine the cause of randomly dying creatures.
     * 
     * This code has determined, that a lot of creatures on 4k servers simply
     * die of old age. Not all creatures grow to venerable.
     * 
     * This code is no longer in use.
     * 
     * @param killed The creature that has died.
     */
    public static void debugDeath(Creature killed) {
        try {
            logger.info(String.format("DEATH %s (%d) died at %d, %d. It's target is %s (%d), it had %d latest attackers.",
                killed.getName(), killed.getWurmId(), killed.getTileX(), killed.getTileY(),
                killed.getTarget() == null ? "-null-" : killed.getTarget().getName(),
                killed.getTarget() == null ? -10 : killed.getTarget().getWurmId(),
                killed.getLatestAttackers().length));
            logger.info(String.format("It was %s (Age %d), it had %d fat and %d damage, disease value was %d.",
                killed.getStatus().getAgeString(), killed.getStatus().age,
                killed.getStatus().fat, killed.getStatus().damage,
                killed.getStatus().disease));
            if (killed.getLatestAttackers().length > 0) {
                logger.info("Its attackers were:");
                for (long wurmId : killed.getLatestAttackers()) {
                    Creature c = Server.getInstance().getCreature(wurmId);
                    if (c == null) logger.info(String.format("    - Creature %d can't be found anymore", wurmId));
                    else
                        logger.info(String.format("    - %s (%d) at %d, %d",
                            c.getName(), c.getWurmId(), c.getTileX(), c.getTileY()));
                }
            }
            logger.info("How did we get here?");
            logger.info(Arrays.toString(new Exception().getStackTrace()).replace(", ", "\r\n\t"));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed debugging a death.", e);
        }
    }
}

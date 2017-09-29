package com.pveplands.treasurehunting;

import com.wurmonline.server.MiscConstants;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to hold all the mod's properties.
 */
public class TreasureOptions {
    private static final Logger logger = Logger.getLogger(TreasureHunting.getLoggerName(TreasureOptions.class));
    
    private int treasuremapTemplateId = 4200;
    
    private boolean extraWarning = false;
    private boolean extraSwirl = false;
    
    private boolean damageCompass = true;
    private boolean damageMap = true;
    private float damageMultiplier = 100f;
    
    private float lockChance = 100f;
    private float lockMultiplier = 0.25f;
    
    private int creationTries = 1000;
    
    private int mapDiggingChance = 10000;
    private int mapMiningChance = 3500;
    private int mapSurfaceMiningChance = 10000;
    private int mapHuntingChance = 100;
    private int mapUniqueChance = 10000;
    private int mapFishingChance = 10000;
    
    private int maxHeightDiff = 72;
    private double mapBaseDiff = 30d;
    
    private int[] mapDrops = new int[] { 11 /* troll */, 23 /* goblin */, 111 /* ogre mage */};
    
    private int[][] spawnGroups;
    private int[] groupWeights;
    private int[] spawnWeights = new int[10];
    private int[] spawnLimits = new int[10];
    
    private int baseMoneyReward = 10000;
    private float moneyMultiplier = 1.0f;
    
    private int baseKarmaReward = 100;
    private float karmaMultiplier = 9.0f;
    
    private int baseMetalWeight = 250;
    private float metalMultiplier = 1.0f;
    private float dragonMultiplier = 0.25f;
    private int[] metals = new int[] { 45, 44, 694, 698, 371, 372 };
    
    private int baseSleepPowderReward = 1;
    private float sleepPowderMultiplier = 1.0f;
    
    private float hotaChance = 10f;
    private float hotaMultiplier = 9.0f;
    private int[] hotaAux = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    
    private int[] tierGuaranteed = new int[10];
    private int[] tierOptional = new int[10];
    private int[] tierChances = new int[10];
    private int[][] tierGroups = new int[0][];
    private int[][] tierItems = new int[10][];
    
    private float rareChance = 10f;
    private float rareMultiplier = 9.0f;
    private int[] rareItems = new int[0];
    private int[] unfinishedRares = new int[0];
    
    private int veryRareChance = 10000;
    private int veryRareMultiplier = 1000;
    private int[] veryRares = new int[0];
    
    private int extremelyRareChance = 100000;
    private int extremelyRareMultiplier = 1000;
    private int[] extremelyRares = new int[0];
    
    private int unfinishedChance = 100;
    private int unfinishedMultiplier = 5;
    private int[] unfinisheds = new int[0];
    private int[] kingdomItems = new int[0];
    private int[] unfinishedKingdoms = new int[] { 1, 2, 3, 4 };
    
    private int rareRarity = 2;
    private int supremeRarity = 100;
    private int fantasticRarity = 10000;
    private int rareWindow = 180;
    
    private int minTreasureDistance = 0;
    private int maxTreasureDistance = Integer.MAX_VALUE;

    private TeleportToTreasureAction teleportAction;
    private CreateRandomTreasuremapAction createmapAction;
    private CreateTreasuremapHereAction createhereAction;
    private ReloadConfigAction reloadAction;
    private ReadTreasuremapAction readmapAction;
    private DigUpTreasureAction digAction;
    private UnloadFromTreasureAction unloadAction;
    private SpawnTreasurechestAction chestAction;
    private TreasureBehaviour behaviours;

    public TreasureOptions() {
    }
    
    public void configure(Properties p) {
        String[] fields = MiscConstants.emptyStringArray;
        
        /**
         * Value bounds are either sane limits, insane limits at the discretion
         * of any one server admin, or reasonable limits, or arbitrary ones.
         * 
         * The map reading damage multiplier with a minimum damage per use of
         * 0.0015 and a multiplier of 66667 will at most be 100.005, destroying
         * the map when unlucky, for example.
         */
        setExtraWarning(Boolean.valueOf(p.getProperty("extraWarning", String.valueOf(isExtraWarning()))));
        logger.log(Level.INFO, "Extra warning: {0}", isExtraWarning());
        
        setExtraSwirl(Boolean.valueOf(p.getProperty("extraSwirl", String.valueOf(isExtraSwirl()))));
        logger.log(Level.INFO, "Extra swirl: {0}", isExtraSwirl());
        
        setDamageCompass(Boolean.valueOf(p.getProperty("damageCompass", String.valueOf(isDamageCompass()))));
        logger.log(Level.INFO, "Damage compass: {0}", isDamageCompass());
        
        setDamageMap(Boolean.valueOf(p.getProperty("damageMap", String.valueOf(isDamageMap()))));
        logger.log(Level.INFO, "Damage map: {0}", isDamageMap());
        
        setDamageMultiplier(Float.valueOf(p.getProperty("damageMultiplier", String.valueOf(getDamageMultiplier()))));
        setDamageMultiplier(Math.min(66667.0f, Math.max(1.0f, getDamageMultiplier())));
        logger.log(Level.INFO, "Damage multiplier: {0}", getDamageMultiplier());
        
        setCreationTries(Integer.valueOf(p.getProperty("creationTries", String.valueOf(getCreationTries()))));
        setCreationTries(Math.min(1000, Math.max(1, getCreationTries())));
        logger.log(Level.INFO, "Map creation tries: {0}", getCreationTries());
        
        setLockChance(Float.valueOf(p.getProperty("lockChance", String.valueOf(getLockChance()))));
        setLockChance(Math.min(100f, Math.max(0f, getLockChance())));
        logger.log(Level.INFO, "Treasure chest lock chance: {0} percent", getLockChance());
        
        setLockMultiplier(Float.valueOf(p.getProperty("lockMultiplier", String.valueOf(getLockMultiplier()))));
        setLockMultiplier(Math.min(100f, Math.max(0.01f, getLockMultiplier())));
        logger.log(Level.INFO, "Lock quality multiplier: {0}", getLockMultiplier());

        
        
        setMapDiggingChance(Integer.valueOf(p.getProperty("mapDiggingChance", String.valueOf(getMapDiggingChance()))));
        setMapDiggingChance(Math.min(2147483647, Math.max(0, getMapDiggingChance())));
        logger.log(Level.INFO, "Map digging chance: {0}", getMapDiggingChance());

        setMapFishingChance(Integer.valueOf(p.getProperty("mapFishingChance", String.valueOf(getMapFishingChance()))));
        setMapFishingChance(Math.min(2147483647, Math.max(0, getMapFishingChance())));
        logger.log(Level.INFO, "Map fishing chance: {0}", getMapFishingChance());
        
        setMapMiningChance(Integer.valueOf(p.getProperty("mapMiningChance", String.valueOf(getMapMiningChance()))));
        setMapMiningChance(Math.min(2147483647, Math.max(0, getMapMiningChance())));
        logger.log(Level.INFO, "Map mining chance: {0}", getMapMiningChance());
        
        setMapSurfaceMiningChance(Integer.valueOf(p.getProperty("mapSurfaceMiningChance", String.valueOf(getMapSurfaceMiningChance()))));
        setMapSurfaceMiningChance(Math.min(2147483647, Math.max(0, getMapSurfaceMiningChance())));
        logger.log(Level.INFO, "Map surface mining chance: {0}", getMapSurfaceMiningChance());

        setMapHuntingChance(Integer.valueOf(p.getProperty("mapHuntingChance", String.valueOf(getMapHuntingChance()))));
        setMapHuntingChance(Math.min(2147483647, Math.max(0, getMapHuntingChance())));
        logger.log(Level.INFO, "Map hunting chance: {0}", getMapHuntingChance());

        setMapUniqueChance(Integer.valueOf(p.getProperty("mapUniqueChance", String.valueOf(getMapUniqueChance()))));
        setMapUniqueChance(Math.min(2147483647, Math.max(0, getMapUniqueChance())));
        logger.log(Level.INFO, "Map unique chance: {0}", getMapUniqueChance());

        setMapBaseDiff(Double.valueOf(p.getProperty("mapBaseDiff", String.valueOf(getMapBaseDiff()))));
        setMapBaseDiff(Math.min(100d, Math.max(4d, getMapBaseDiff())));
        logger.log(Level.INFO, "Base map creation difficulty: {0}", getMapBaseDiff());
        
        setMaxHeightDiff(Integer.valueOf(p.getProperty("maxHeightDiff", String.valueOf(getMaxHeightDiff()))));
        setMaxHeightDiff(Math.min(2147483647, Math.max(20, getMaxHeightDiff())));
        logger.log(Level.INFO, "Max height difference in a 3x3 area: {0}", getMaxHeightDiff());
        
        try {
            fields = p.getProperty("mapDrops", "11,23,111").split(",");
            int[] mapDrops = new int[fields.length];
            
            for (int i = 0; i < fields.length; i++)
                mapDrops[i] = Integer.valueOf(fields[i].trim());
            
            setMapDrops(mapDrops);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as map drops.", Arrays.toString(fields)), e); }
        logger.log(Level.INFO, "Creatures that drop treasuremaps: {0}", Arrays.toString(getMapDrops()));
        
        // Read the spawn groups.
        try {
            int groups = 0;
            for (int group = 0; ; group++) {
                if (p.containsKey(String.format("spawnGroup%d", group)))
                    groups++;
                else
                    break;
            }

            int[][] spawnGroups = new int[groups][];
            int[] groupWeights = new int[groups];
            
            for (int group = 0; ; group++) {
                String key = String.format("spawnGroup%d", group);
                if (!p.containsKey(key))
                    break;
                
                String[] groupFields = p.getProperty(key).trim().split(":");
                groupWeights[group] =  Integer.valueOf(groupFields[0].trim());

                String[] groupMobs = groupFields[1].split(",");
                spawnGroups[group] = new int[groupMobs.length];
                for (int i = 0; i < groupMobs.length; i++)
                    spawnGroups[group][i] = Integer.valueOf(groupMobs[i].trim());
            }
            
            setSpawnGroups(spawnGroups);
            setGroupWeights(groupWeights);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load mob spawn group.", e);
        }
        
        int index = 0;
        for (int[] group : getSpawnGroups())
            logger.info(String.format("Spawn group %d: %s", index++, Arrays.toString(group)));
        logger.info(String.format("Group weights: %s", Arrays.toString(getGroupWeights())));

        // Read the spawn tiers.
        try {
            int[] spawnWeights = new int[10];
            int[] spawnLimits = new int[10];
            
            for (int tier = 0; tier < 10; tier++) {
                String[] spawnFields = p.getProperty(String.format("tierSpawn%d", tier)).split(":");
                spawnWeights[tier] = Integer.valueOf(spawnFields[0].trim());
                spawnLimits[tier] = Integer.valueOf(spawnFields[1].trim());
            }
            
            setSpawnWeights(spawnWeights);
            setSpawnLimits(spawnLimits);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load treasure tier mob spawn weights.", e);
        }
        
        logger.info(String.format("Spawn weights: %s", Arrays.toString(getSpawnWeights())));
        logger.info(String.format("Spawn limits: %s", Arrays.toString(getSpawnLimits())));
        
        setBaseMoneyReward(Integer.valueOf(p.getProperty("baseMoneyReward", String.valueOf(getBaseMoneyReward()))));
        setBaseMoneyReward(Math.min(1000000, Math.max(0, getBaseMoneyReward())));
        logger.log(Level.INFO, "Base money reward: {0}", getBaseMoneyReward());
        
        setMoneyMultiplier(Float.valueOf(p.getProperty("moneyRewardMultiplier", String.valueOf(getMoneyMultiplier()))));
        setMoneyMultiplier(Math.min(100.0f, Math.max(0.0f, getMoneyMultiplier())));
        logger.log(Level.INFO, "Money reward multiplier: {0}", getMoneyMultiplier());
        
        setBaseKarmaReward(Integer.valueOf(p.getProperty("baseKarmaReward", String.valueOf(getBaseKarmaReward()))));
        setBaseKarmaReward(Math.min(45000, Math.max(0, getBaseKarmaReward())));
        logger.log(Level.INFO, "Base karma reward in grams: {0}", getBaseKarmaReward());
        
        setKarmaMultiplier(Float.valueOf(p.getProperty("karmaRewardMultiplier", String.valueOf(getKarmaMultiplier()))));
        setKarmaMultiplier(Math.min(45000f, Math.max(0f, getKarmaMultiplier())));
        logger.log(Level.INFO, "Karma reward multiplier: {0}", getKarmaMultiplier());
        
        setBaseMetalWeight(Integer.valueOf(p.getProperty("basePreciousMetalWeight", String.valueOf(getBaseMetalWeight()))));
        setBaseMetalWeight(Math.min(1000000, Math.max(0, getBaseMetalWeight())));
        logger.log(Level.INFO, "Base precious metal reward: {0}", getBaseMetalWeight());
        
        setMetalMultiplier(Float.valueOf(p.getProperty("preciousMetalMultiplier", String.valueOf(getMetalMultiplier()))));
        setMetalMultiplier(Math.min(64000f, Math.max(0f, getMetalMultiplier())));
        logger.log(Level.INFO, "Precious metal multiplier: {0}", getMetalMultiplier());
        
        setDragonMultiplier(Float.valueOf(p.getProperty("dragonMultiplier", String.valueOf(getDragonMultiplier()))));
        setDragonMultiplier(Math.min(100.0f, Math.max(0.01f, getDragonMultiplier())));
        logger.log(Level.INFO, "Dragon scale/Drake leather multiplier: {0}", getDragonMultiplier());
        
        fields = p.getProperty("preciousMetals", "45,44,694,698,371,372").replace(" ", "").split(",");
        int[] metals = new int[fields.length];
            
        try {
            for (int i = 0; i < fields.length; i++)
                metals[i] = Integer.valueOf(fields[i].trim());
            setMetals(metals);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse %s as precious metals.", Arrays.toString(fields)), e); }
        logger.log(Level.INFO, "Precious metals: {0}", Arrays.toString(getMetals()));
        
        setBaseSleepPowderReward(Integer.valueOf(p.getProperty("baseSleepPowderReward", String.valueOf(getBaseSleepPowderReward()))));
        setBaseSleepPowderReward(Math.min(100, Math.max(0, getBaseSleepPowderReward())));
        logger.log(Level.INFO, "Base sleep powder reward: {0}", getBaseSleepPowderReward());
        
        setSleepPowderMultiplier(Float.valueOf(p.getProperty("sleepPowderMultiplier", String.valueOf(getSleepPowderMultiplier()))));
        setSleepPowderMultiplier(Math.min(100f, Math.max(0f, getSleepPowderMultiplier())));
        logger.log(Level.INFO, "Sleep powder multiplier: {0}", getSleepPowderMultiplier());
        
        setHotaChance(Float.valueOf(p.getProperty("hotaChance", String.valueOf(getHotaChance()))));
        setHotaChance(Math.min(100.0f, Math.max(0f, getHotaChance())));
        logger.log(Level.INFO, "Base HOTA statue chance: {0}", getHotaChance());
        
        setHotaMultiplier(Float.valueOf(p.getProperty("hotaMultiplier", String.valueOf(getHotaMultiplier()))));
        setHotaMultiplier(Math.min(10000f, Math.max(0f, getHotaMultiplier())));
        logger.log(Level.INFO, "HOTA statue multiplier: {0}", getHotaMultiplier());
        
        // Read HOTA AuxData values.
        int[] hotaAux;
        try {
            fields = p.getProperty("hotaAux", "0,1,2,3,4,5,6,7,8,9").split(",");
            hotaAux = new int[fields.length];
            
            for (int i = 0; i < fields.length; i++)
                hotaAux[i] = Integer.valueOf(fields[i].trim());
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Could not parse %s as HotA aux data.", Arrays.toString(fields)), e);
            hotaAux = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        }
        setHotaAux(hotaAux);
        logger.log(Level.INFO, "HOTA AuxData: {0}", Arrays.toString(getHotaAux()));
        
        // Read reward groups.
        int[][] tierGroups = new int[0][];
        
        try {
            int groups = 0;
            
            for (int rewardGroup = 0; ; rewardGroup++) {
                String groupKey = String.format("rewardGroup%d", rewardGroup);
                if (!p.containsKey(groupKey))
                    break;
                groups++;
            }
            
            tierGroups = new int[groups][];
            
            for (int rewardGroup = 0; rewardGroup < groups; rewardGroup++) {
                String groupKey = String.format("rewardGroup%d", rewardGroup);

                try {
                    String[] groupFields = p.getProperty(groupKey).split(",");
                    tierGroups[rewardGroup] = new int[groupFields.length];
                
                    for (int i = 0; i < groupFields.length; i++)
                        tierGroups[rewardGroup][i] = Integer.valueOf(groupFields[i].trim());
                }
                catch (Exception inner) {
                    logger.log(Level.SEVERE, String.format("%s is malformed, make sure the syntax is correct.", groupKey), inner);
                }
            }
            
            setTierGroups(tierGroups);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Reward groups could not be read.", e);
        }
        
        for (int[] tierGroup : getTierGroups())
            logger.info(String.format("Tier reward group: %s", Arrays.toString(tierGroup)));
        
        // Read reward tiers.
        int[] tierGuaranteed = new int[10];
        int[] tierOptional = new int[10];
        int[] tierChances = new int[10];
        int[][] tierItems = new int[10][];
        
        for (int tier = 0; tier < 10; tier++) {
            try {
                String[] tierFields = p.getProperty("tierReward" + String.valueOf(tier)).replace(" ", "").split(":");
                
                tierGuaranteed[tier] = Integer.valueOf(tierFields[0].trim());
                tierOptional[tier] = Integer.valueOf(tierFields[1].trim());
                tierChances[tier] = Integer.valueOf(tierFields[2].trim());
                
                String[] tierRewards = tierFields[3].split(",");
                tierItems[tier] = new int[tierRewards.length];
                
                for (int reward = 0; reward < tierRewards.length; reward++)
                    tierItems[tier][reward] = Integer.valueOf(tierRewards[reward].trim());
                
                setTierGuaranteed(tierGuaranteed);
                setTierOptional(tierOptional);
                setTierChances(tierChances);
                setTierItems(tierItems);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Can't parse tierReward" + tier + " line. Make sure its syntax is correct.", e);
            }
        }
        logger.info(String.format("Guaranteed reward items: %s", Arrays.toString(getTierGuaranteed())));
        logger.info(String.format("Optional reward items: %s", Arrays.toString(getTierOptional())));
        logger.info(String.format("Optional reward item chances: %s", Arrays.toString(getTierChances())));
        for (int[] tierItem : getTierItems())
            logger.info(String.format("Tier reward items: %s", Arrays.toString(tierItem)));
        
        setRareChance(Float.valueOf(p.getProperty("rareChance", String.valueOf(getRareChance()))));
        setRareChance(Math.min(100f, Math.max(0f, getRareChance())));
        logger.log(Level.INFO, "Base rare item chance: {0}", getRareChance());
        
        setRareMultiplier(Float.valueOf(p.getProperty("rareMultiplier", String.valueOf(getRareMultiplier()))));
        setRareMultiplier(Math.min(10000f, Math.max(0f, getRareMultiplier())));
        logger.log(Level.INFO, "Rare item chance multiplier: {0}", getRareMultiplier());
        
        fields = p.getProperty("rareItems", "7,25,27,24,215,62,63,64,185,77,82,83,84,85,86,3,90,87,80,21,81,711,710,705,707,706,97,103,104,105,107,108,106,109,110,111,112,113,120,114,118,116,117,119,115,276,275,274,277,278,279,282,281,280,283,284,285,286,287,20,139,143,93,8,135,94,152,202,296,257,259,258,267,268,290,292,291,314,463,75,350,351,374,376,378,380,382,388,397,390,392,394,396,397,413,749,797,447,448,449,480,581,65,621,702,703,704,624,623,623,623,623,640,642,641,643,647,774,922").replace(" ", "").split(",");
        int[] rareItems = new int[fields.length];
            
        try { 
            for (int i = 0; i < fields.length; i++)
                rareItems[i] = Integer.valueOf(fields[i].trim());
            
            setRareItems(rareItems);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as rare items.", Arrays.toString(fields)), e); }
        logger.info(String.format("Rare items: %s", Arrays.toString(getRareItems())));
        
        fields = p.getProperty("unfinishedRare", "384,430,528,638,850,853,539,540,541,542,543,491,490,180,178,1023,1028,226,922").replace(" ", "").split(",");
        int[] unfinishedRares = new int[fields.length];
        
        try {
            for (int i = 0; i < fields.length; i++)
                 unfinishedRares[i] = Integer.valueOf(fields[i].trim());
            setUnfinishedRares(unfinishedRares);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as unfinished rares.", Arrays.toString(fields)), e); }
        logger.info(String.format("Unfinished rares: %s", Arrays.toString(getUnfinishedRares())));
        
        setVeryRareChance(Integer.valueOf(p.getProperty("veryRareChance", String.valueOf(getVeryRareChance()))));
        setVeryRareChance(Math.min(2147483647, Math.max(0, getVeryRareChance())));
        logger.log(Level.INFO, "Base very rare item chance: {0}", getVeryRareChance());
        
        setVeryRareMultiplier(Integer.valueOf(p.getProperty("veryRareMultiplier", String.valueOf(getVeryRareMultiplier()))));
        setVeryRareMultiplier(Math.min(238609294, Math.max(0, getVeryRareMultiplier())));
        logger.log(Level.INFO, "Very rare item multiplier: {0}", getVeryRareMultiplier());
        
        fields = p.getProperty("veryRare", "843,299,300,654,868,781,668,664,665,655,738,967").replace(" ", "").split(",");
        int[] veryRares = new int[fields.length];
        
        try {
            for (int i = 0; i < fields.length; i++)
                veryRares[i] = Integer.valueOf(fields[i]);
            setVeryRares(veryRares);
        }       
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as very rare items", Arrays.toString(fields)), e); }
        logger.info(String.format("Very rare items: %s", Arrays.toString(getVeryRares())));
        
        setExtremelyRareChance(Integer.valueOf(p.getProperty("extremelyRareChance", String.valueOf(getExtremelyRareChance()))));
        setExtremelyRareChance(Math.min(2147483647, Math.max(0, getExtremelyRareChance())));
        logger.log(Level.INFO, "Extremely rare item chance: {0}", getExtremelyRareChance());
        
        setExtremelyRareMultiplier(Integer.valueOf(p.getProperty("extremelyRareMultiplier", String.valueOf(getExtremelyRareMultiplier()))));
        setExtremelyRareMultiplier(Math.min(238609294, Math.max(0, getExtremelyRareMultiplier())));
        logger.log(Level.INFO, "Extremely rare item chance multiplier: {0}", getExtremelyRareMultiplier());
        
        fields = p.getProperty("extremelyRare", "806,794,795,796,797,798,809,808,807,810,799,800,801,802,803").replace(" ", "").split(",");
        int[] extremelyRares = new int[fields.length];
        
        try {
            for (int i = 0; i < fields.length; i++)
                extremelyRares[i] = Integer.valueOf(fields[i]);
            setExtremelyRares(extremelyRares);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as extremely rare items.", Arrays.toString(fields)), e); }
        logger.info(String.format("Extremely rare items: %s", Arrays.toString(getExtremelyRares())));
        
        setUnfinishedChance(Integer.valueOf(p.getProperty("unfinishedChance", String.valueOf(getUnfinishedChance()))));
        setUnfinishedChance(Math.min(2147483647, Math.max(0, getUnfinishedChance())));
        logger.log(Level.INFO, "Unfinished item chance: {0}", getUnfinishedChance());
        
        setUnfinishedMultiplier(Integer.valueOf(p.getProperty("unfinishedMultiplier", String.valueOf(getUnfinishedMultiplier()))));
        setUnfinishedMultiplier(Math.min(238609294, Math.max(0, getUnfinishedMultiplier())));
        logger.log(Level.INFO, "Unfinished item chance multiplier: {0}", getUnfinishedMultiplier());
      
        fields = p.getProperty("unfinishedItems", "384,430,528,638,850").replace(" ", "").split(",");
        int[] unfinisheds = new int[fields.length];
        
        try {
            for (int i = 0; i < fields.length; i++)
                unfinisheds[i] = Integer.valueOf(fields[i]);
            setUnfinisheds(unfinisheds);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as unfinished items.", Arrays.toString(fields)), e); }
        logger.info(String.format("Unfinished items: %s", Arrays.toString(getUnfinisheds())));
        
        fields = p.getProperty("kingdomItems", "579,578,831,999").replace(" ", "").split(",");
        int[] kingdomItems = new int[fields.length];
        
        try {
            for (int i = 0; i < fields.length; i++)
                kingdomItems[i] = Integer.valueOf(fields[i]);
            setKingdomItems(kingdomItems);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as kingdom items.", Arrays.toString(fields)), e); }
        logger.info(String.format("Kingdom items: %s", Arrays.toString(getKingdomItems())));
        
        fields = p.getProperty("unfinishedKingdoms", "1,2,3,4").replace(" ", "").split(",");
        int[] unfinishedKingdoms = new int[fields.length];
        
        try {
            for (int i = 0; i < fields.length; i++)
                unfinishedKingdoms[i] = Integer.valueOf(fields[i]);
            setUnfinishedKingdoms(unfinishedKingdoms);
        }
        catch (Exception e) { logger.log(Level.SEVERE, String.format("Could not parse '%s' as kingdom IDs.", Arrays.toString(fields)), e); }
        logger.info(String.format("Kingdoms: %s", Arrays.toString(getUnfinishedKingdoms())));
        
        setRareRarity(Integer.valueOf(p.getProperty("rareRarity", String.valueOf(getRareRarity()))));
        setRareRarity(Math.min(2147483647, Math.max(1, getRareRarity())));
        logger.log(Level.INFO, "rareRarity: {0}", getRareRarity());
        
        setSupremeRarity(Integer.valueOf(p.getProperty("supremeRarity", String.valueOf(getSupremeRarity()))));
        setSupremeRarity(Math.min(2147483647, Math.max(1, getSupremeRarity())));
        logger.log(Level.INFO, "supremeRarity: {0}", getSupremeRarity());
        
        setFantasticRarity(Integer.valueOf(p.getProperty("fantasticRarity", String.valueOf(getFantasticRarity()))));
        setFantasticRarity(Math.min(2147483647, Math.max(1, getFantasticRarity())));
        logger.log(Level.INFO, "fantasticRarity: {0}", getFantasticRarity());
        
        setRareWindow(Integer.valueOf(p.getProperty("rareWindow", String.valueOf(getRareWindow()))));
        setRareWindow(Math.min(2147483647, Math.max(1, getRareWindow())));
        logger.log(Level.INFO, "Rare window chance: {0}", getRareWindow());
        
        setTreasuremapTemplateId(Integer.valueOf(p.getProperty("treasuremapTemplateId", String.valueOf(getTreasuremapTemplateId()))));
        setTreasuremapTemplateId(Math.min(32767, Math.max(4200, getTreasuremapTemplateId())));
        logger.log(Level.INFO, "Treasuremap templateID: {0}", getTreasuremapTemplateId());
        
        setMinTreasureDistance(Integer.valueOf(p.getProperty("minTreasureDistance", String.valueOf(getMinTreasureDistance()))));
        logger.info(String.format("Treasuremap min distance from player: %d", getMinTreasureDistance()));
        
        setMaxTreasureDistance(Integer.valueOf(p.getProperty("maxTreasureDistance", String.valueOf(getMaxTreasureDistance()))));
        logger.info(String.format("Treasuremap max distance from player: %d", getMaxTreasureDistance()));
    }
    
    public int getTreasuremapTemplateId() {
        return treasuremapTemplateId;
    }

    public void setTreasuremapTemplateId(int treasuremapTemplateId) {
        this.treasuremapTemplateId = treasuremapTemplateId;
    }

    public boolean isExtraWarning() {
        return extraWarning;
    }

    public void setExtraWarning(boolean extraWarning) {
        this.extraWarning = extraWarning;
    }

    public boolean isExtraSwirl() {
        return extraSwirl;
    }

    public void setExtraSwirl(boolean extraSwirl) {
        this.extraSwirl = extraSwirl;
    }

    public boolean isDamageCompass() {
        return damageCompass;
    }

    public void setDamageCompass(boolean damageCompass) {
        this.damageCompass = damageCompass;
    }

    public boolean isDamageMap() {
        return damageMap;
    }

    public void setDamageMap(boolean damageMap) {
        this.damageMap = damageMap;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public float getLockChance() {
        return lockChance;
    }

    public void setLockChance(float lockChance) {
        this.lockChance = lockChance;
    }

    public float getLockMultiplier() {
        return lockMultiplier;
    }

    public void setLockMultiplier(float lockMultiplier) {
        this.lockMultiplier = lockMultiplier;
    }

    public int getCreationTries() {
        return creationTries;
    }

    public void setCreationTries(int creationTries) {
        this.creationTries = creationTries;
    }

    public int getMapDiggingChance() {
        return mapDiggingChance;
    }

    public void setMapDiggingChance(int mapDiggingChance) {
        this.mapDiggingChance = mapDiggingChance;
    }

    public int getMapMiningChance() {
        return mapMiningChance;
    }

    public void setMapMiningChance(int mapMiningChance) {
        this.mapMiningChance = mapMiningChance;
    }

    public int getMapHuntingChance() {
        return mapHuntingChance;
    }

    public void setMapHuntingChance(int mapHuntingChance) {
        this.mapHuntingChance = mapHuntingChance;
    }

    public int getMapUniqueChance() {
        return mapUniqueChance;
    }

    public void setMapUniqueChance(int mapUniqueChance) {
        this.mapUniqueChance = mapUniqueChance;
    }

    public int getMapFishingChance() {
        return mapFishingChance;
    }

    public void setMapFishingChance(int mapFishingChance) {
        this.mapFishingChance = mapFishingChance;
    }

    public int getMaxHeightDiff() {
        return maxHeightDiff;
    }

    public void setMaxHeightDiff(int maxHeightDiff) {
        this.maxHeightDiff = maxHeightDiff;
    }

    public double getMapBaseDiff() {
        return mapBaseDiff;
    }

    public void setMapBaseDiff(double mapBaseDiff) {
        this.mapBaseDiff = mapBaseDiff;
    }

    public int[] getMapDrops() {
        return mapDrops;
    }

    public void setMapDrops(int[] mapDrops) {
        this.mapDrops = mapDrops;
    }

    public int[][] getSpawnGroups() {
        return spawnGroups;
    }

    public void setSpawnGroups(int[][] spawnGroups) {
        this.spawnGroups = spawnGroups;
    }

    public int[] getGroupWeights() {
        return groupWeights;
    }

    public void setGroupWeights(int[] groupWeights) {
        this.groupWeights = groupWeights;
    }

    public int[] getSpawnWeights() {
        return spawnWeights;
    }

    public void setSpawnWeights(int[] spawnWeights) {
        this.spawnWeights = spawnWeights;
    }

    public int[] getSpawnLimits() {
        return spawnLimits;
    }

    public void setSpawnLimits(int[] spawnLimits) {
        this.spawnLimits = spawnLimits;
    }

    public int getBaseMoneyReward() {
        return baseMoneyReward;
    }

    public void setBaseMoneyReward(int baseMoneyReward) {
        this.baseMoneyReward = baseMoneyReward;
    }

    public float getMoneyMultiplier() {
        return moneyMultiplier;
    }

    public void setMoneyMultiplier(float moneyMultiplier) {
        this.moneyMultiplier = moneyMultiplier;
    }

    public int getBaseKarmaReward() {
        return baseKarmaReward;
    }

    public void setBaseKarmaReward(int baseKarmaReward) {
        this.baseKarmaReward = baseKarmaReward;
    }

    public float getKarmaMultiplier() {
        return karmaMultiplier;
    }

    public void setKarmaMultiplier(float karmaMultiplier) {
        this.karmaMultiplier = karmaMultiplier;
    }

    public int getBaseMetalWeight() {
        return baseMetalWeight;
    }

    public void setBaseMetalWeight(int baseMetalWeight) {
        this.baseMetalWeight = baseMetalWeight;
    }

    public float getMetalMultiplier() {
        return metalMultiplier;
    }

    public void setMetalMultiplier(float metalMultiplier) {
        this.metalMultiplier = metalMultiplier;
    }

    public float getDragonMultiplier() {
        return dragonMultiplier;
    }

    public void setDragonMultiplier(float dragonMultiplier) {
        this.dragonMultiplier = dragonMultiplier;
    }

    public int[] getMetals() {
        return metals;
    }

    public void setMetals(int[] metals) {
        this.metals = metals;
    }

    public int getBaseSleepPowderReward() {
        return baseSleepPowderReward;
    }

    public void setBaseSleepPowderReward(int baseSleepPowderReward) {
        this.baseSleepPowderReward = baseSleepPowderReward;
    }

    public float getSleepPowderMultiplier() {
        return sleepPowderMultiplier;
    }

    public void setSleepPowderMultiplier(float sleepPowderMultiplier) {
        this.sleepPowderMultiplier = sleepPowderMultiplier;
    }

    public float getHotaChance() {
        return hotaChance;
    }

    public void setHotaChance(float hotaChance) {
        this.hotaChance = hotaChance;
    }

    public float getHotaMultiplier() {
        return hotaMultiplier;
    }

    public void setHotaMultiplier(float hotaMultiplier) {
        this.hotaMultiplier = hotaMultiplier;
    }

    public int[] getHotaAux() {
        return hotaAux;
    }

    public void setHotaAux(int[] hotaAux) {
        this.hotaAux = hotaAux;
    }

    public int[] getTierGuaranteed() {
        return tierGuaranteed;
    }

    public void setTierGuaranteed(int[] tierGuaranteed) {
        this.tierGuaranteed = tierGuaranteed;
    }

    public int[] getTierOptional() {
        return tierOptional;
    }

    public void setTierOptional(int[] tierOptional) {
        this.tierOptional = tierOptional;
    }

    public int[] getTierChances() {
        return tierChances;
    }

    public void setTierChances(int[] tierChances) {
        this.tierChances = tierChances;
    }

    public int[][] getTierGroups() {
        return tierGroups;
    }

    public void setTierGroups(int[][] tierGroups) {
        this.tierGroups = tierGroups;
    }

    public int[][] getTierItems() {
        return tierItems;
    }

    public void setTierItems(int[][] tierItems) {
        this.tierItems = tierItems;
    }

    public float getRareChance() {
        return rareChance;
    }

    public void setRareChance(float rareChance) {
        this.rareChance = rareChance;
    }

    public float getRareMultiplier() {
        return rareMultiplier;
    }

    public void setRareMultiplier(float rareMultiplier) {
        this.rareMultiplier = rareMultiplier;
    }

    public int[] getRareItems() {
        return rareItems;
    }

    public void setRareItems(int[] rareItems) {
        this.rareItems = rareItems;
    }

    public int[] getUnfinishedRares() {
        return unfinishedRares;
    }

    public void setUnfinishedRares(int[] unfinishedRares) {
        this.unfinishedRares = unfinishedRares;
    }

    public int getVeryRareChance() {
        return veryRareChance;
    }

    public void setVeryRareChance(int veryRareChance) {
        this.veryRareChance = veryRareChance;
    }

    public int getVeryRareMultiplier() {
        return veryRareMultiplier;
    }

    public void setVeryRareMultiplier(int veryRareMultiplier) {
        this.veryRareMultiplier = veryRareMultiplier;
    }

    public int[] getVeryRares() {
        return veryRares;
    }

    public void setVeryRares(int[] veryRares) {
        this.veryRares = veryRares;
    }

    public int getExtremelyRareChance() {
        return extremelyRareChance;
    }

    public void setExtremelyRareChance(int extremelyRareChance) {
        this.extremelyRareChance = extremelyRareChance;
    }

    public int getExtremelyRareMultiplier() {
        return extremelyRareMultiplier;
    }

    public void setExtremelyRareMultiplier(int extremelyRareMultiplier) {
        this.extremelyRareMultiplier = extremelyRareMultiplier;
    }

    public int[] getExtremelyRares() {
        return extremelyRares;
    }

    public void setExtremelyRares(int[] extremelyRares) {
        this.extremelyRares = extremelyRares;
    }

    public int getUnfinishedChance() {
        return unfinishedChance;
    }

    public void setUnfinishedChance(int unfinishedChance) {
        this.unfinishedChance = unfinishedChance;
    }

    public int getUnfinishedMultiplier() {
        return unfinishedMultiplier;
    }

    public void setUnfinishedMultiplier(int unfinishedMultiplier) {
        this.unfinishedMultiplier = unfinishedMultiplier;
    }

    public int[] getUnfinisheds() {
        return unfinisheds;
    }

    public void setUnfinisheds(int[] unfinisheds) {
        this.unfinisheds = unfinisheds;
    }

    public int[] getKingdomItems() {
        return kingdomItems;
    }

    public void setKingdomItems(int[] kingdomItems) {
        this.kingdomItems = kingdomItems;
    }

    public int[] getUnfinishedKingdoms() {
        return unfinishedKingdoms;
    }

    public void setUnfinishedKingdoms(int[] unfinishedKingdoms) {
        this.unfinishedKingdoms = unfinishedKingdoms;
    }

    public int getRareRarity() {
        return rareRarity;
    }

    public void setRareRarity(int rareRarity) {
        this.rareRarity = rareRarity;
    }

    public int getSupremeRarity() {
        return supremeRarity;
    }

    public void setSupremeRarity(int supremeRarity) {
        this.supremeRarity = supremeRarity;
    }

    public int getFantasticRarity() {
        return fantasticRarity;
    }

    public void setFantasticRarity(int fantasticRarity) {
        this.fantasticRarity = fantasticRarity;
    }

    public int getRareWindow() {
        return rareWindow;
    }

    public void setRareWindow(int rareWindow) {
        this.rareWindow = rareWindow;
    }

    public TeleportToTreasureAction getTeleportAction() {
        return teleportAction;
    }

    public TeleportToTreasureAction setTeleportAction(TeleportToTreasureAction teleportAction) {
        return this.teleportAction = teleportAction;
    }

    public CreateRandomTreasuremapAction getCreatemapAction() {
        return createmapAction;
    }

    public CreateRandomTreasuremapAction setCreatemapAction(CreateRandomTreasuremapAction createmapAction) {
        return this.createmapAction = createmapAction;
    }

    public CreateTreasuremapHereAction getCreatehereAction() {
        return createhereAction;
    }

    public CreateTreasuremapHereAction setCreatehereAction(CreateTreasuremapHereAction createhereAction) {
        return this.createhereAction = createhereAction;
    }

    public ReloadConfigAction getReloadAction() {
        return reloadAction;
    }

    public ReloadConfigAction setReloadAction(ReloadConfigAction reloadAction) {
        return this.reloadAction = reloadAction;
    }

    public ReadTreasuremapAction getReadmapAction() {
        return readmapAction;
    }

    public ReadTreasuremapAction setReadmapAction(ReadTreasuremapAction readmapAction) {
        return this.readmapAction = readmapAction;
    }

    public DigUpTreasureAction getDigAction() {
        return digAction;
    }

    public DigUpTreasureAction setDigAction(DigUpTreasureAction digAction) {
        return this.digAction = digAction;
    }

    public UnloadFromTreasureAction getUnloadAction() {
        return unloadAction;
    }

    public UnloadFromTreasureAction setUnloadAction(UnloadFromTreasureAction unloadAction) {
        return this.unloadAction = unloadAction;
    }

    public SpawnTreasurechestAction getChestAction() {
        return chestAction;
    }

    public SpawnTreasurechestAction setChestAction(SpawnTreasurechestAction chestAction) {
        return this.chestAction = chestAction;
    }

    public TreasureBehaviour getBehaviours() {
        return behaviours;
    }

    public TreasureBehaviour setBehaviours(TreasureBehaviour behaviours) {
        return this.behaviours = behaviours;
    }

    public int getMapSurfaceMiningChance() {
        return mapSurfaceMiningChance;
    }

    public void setMapSurfaceMiningChance(int mapSurfaceMiningChance) {
        this.mapSurfaceMiningChance = mapSurfaceMiningChance;
    }
    
    public int getMinTreasureDistance() {
        return minTreasureDistance;
    }

    public void setMinTreasureDistance(int minTreasureDistance) {
        this.minTreasureDistance = Math.max(0, minTreasureDistance);
    }

    public int getMaxTreasureDistance() {
        return maxTreasureDistance;
    }

    public void setMaxTreasureDistance(int maxTreasureDistance) {
        this.maxTreasureDistance = Math.min(Integer.MAX_VALUE, maxTreasureDistance);
    }
}

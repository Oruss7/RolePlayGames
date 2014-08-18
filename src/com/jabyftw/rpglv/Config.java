package com.jabyftw.rpglv;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Rafael
 */
public class Config {

    private final RPGLeveling pl;
    public CustomConfig configYML, langYML, classesYML;
    public int mySQLTableVersion = 1;
    public boolean useVault;

    public Config(RPGLeveling pl) {
        this.pl = pl;
    }

    public void start() {
        configYML = new CustomConfig(pl, "config");
        langYML = new CustomConfig(pl, "lang");
        classesYML = new CustomConfig(pl, "classes");
        generateConfig();
        generateClasses();
        generateLang();
    }

    private void generateConfig() {
        FileConfiguration config = configYML.getConfig();
        config.addDefault("MySQL.username", "root");
        config.addDefault("MySQL.password", "pass");
        
        config.addDefault("MySQL.url", "jdbc:mysql://localhost:3306/database");
        config.addDefault("MySQL.mysqlTableVersion.DO_NOT_CHANGE_THIS", 1);
        config.addDefault("config.generateDefClassesYML", true);
        config.addDefault("config.useVaultSupport", true);
        config.addDefault("config.useEXPChangeEvent", false);
        config.addDefault("config.blockItemMoveOnInventory", true);
        config.addDefault("config.maxLevel", 30);
        String[] list = {"world", "world2"};
        config.addDefault("config.worlds",  Arrays.asList(list));
        configYML.saveConfig();
        mySQLTableVersion = config.getInt("MySQL.mysqlTableVersion.DO_NOT_CHANGE_THIS");
        useVault = config.getBoolean("config.useVaultSupport");
        pl.maxLevel = config.getInt("config.maxLevel");
        pl.useExp = config.getBoolean("config.useEXPChangeEvent");
        pl.blockItemMove = config.getBoolean("config.blockItemMoveOnInventory");
        pl.sql = new MySQL(pl, config.getString("MySQL.username"), config.getString("MySQL.password"), config.getString("MySQL.url"));
    }

    private void generateClasses() {
        FileConfiguration classes = classesYML.getConfig();
        String[] blocked = {"diamond_sword", "276"};
        classes.addDefault("options.blockedItems", Arrays.asList(blocked));
        if(configYML.getConfig().getBoolean("config.generateDefClassesYML")) {
            classes.addDefault("classes.noob.name", "Noob");
            classes.addDefault("classes.noob.permissionToJoin", "rpglevel.join");
            String[] rewards = {"10;playercommand;motd", "10;consolecommand;tell %player% hello", "10;permission;essentials.motd", "10;reallevel;15", "10;money;2500", "20;money;5000", "20;reallevel;30", "30;reallevel;60", "30;item_permission;diamond_sword"};
            classes.addDefault("classes.noob.rewards", Arrays.asList(rewards));
            String[] potionEffectRewards = {"10/health_boost;1", "15/health_boost;2", "25/damage_resistance;1"};
            classes.addDefault("classes.noob.potioneffects", Arrays.asList(potionEffectRewards));
            String[] levels = {"10", "20", "30"};
            classes.addDefault("classes.noob.broadcastLevels", Arrays.asList(levels));
            String[] kGain = {"zombie;12", "player;20", "creeper;15", "cave_spider;18", "skeleton;14", "spider;12", "chicken;3", "cow;3", "horse;2", "bat;3", "ender_dragon;8001", "enderman;7", "silverfish;5", "mushroom_cow;5", "ocelot;3", "sheep;2", "slime;8", "squid;4", "witch;12", "wither;1200", "wolf;5", "pig;5", "pig_zombie;15", "magma_cube;18", "blaze;15"};
            classes.addDefault("classes.noob.killGain", Arrays.asList(kGain));
            String[] bGain = {"gold_ore;10", "iron_ore;5", "coal_ore;2", "lapis_ore;12", "diamond_ore;20", "redstone_ore;8", "glowing_redstone_ore;8", "quartz_ore;10", "mob_spawner;30"};
            classes.addDefault("classes.noob.breakGain", Arrays.asList(bGain));
            String[] pGain = {"gold_ore;-10", "iron_ore;-5", "coal_ore;-2", "lapis_ore;-12", "diamond_ore;-20", "redstone_ore;-8", "glowing_redstone_ore;-8", "quartz_ore;-10", "mob_spawner;-30"};
            classes.addDefault("classes.noob.placeGain", Arrays.asList(pGain));
            String[] smeltGain = {"gold_ingot;3", "iron_ingot;3", "redstone;3", "stone;3", "glass;3", "coal;5", "brick;3", "hard_clay;3", "baked_potato;3", "cooked_beef;3", "cooked_chicken;3", "cooked_fish;3", "GRILLED_PORK;3", "ink_sack;3", "quartz;3"};
            classes.addDefault("classes.noob.smeltGain", Arrays.asList(smeltGain));
            classes.addDefault("classes.noob.default", true);
            classes.addDefault("classes.noob.levelingEquation", "100*(1.16^(%level-1))"); // Thanks phrstbrn and "Jobs"
            classesYML.saveConfig();
        }
        for(String s : classes.getStringList("options.blockedItems")) {
            pl.proibido.add(pl.getMatFromString(s));
        }
        for(String key : classes.getConfigurationSection("classes").getKeys(false)) {
            String name = classes.getString("classes." + key + ".name");
            String leveling = classes.getString("classes." + key + ".levelingEquation");
            String permission = classes.getString("classes." + key + ".permissionToJoin");
            boolean isDefaultClass = classes.getBoolean("classes." + key + ".default");
            Classe c = new Classe(pl, name, leveling, permission, classes.getStringList("classes." + key + ".broadcastLevels"), classes.getStringList("classes." + key + ".rewards"), classes.getStringList("classes." + key + ".potioneffects"), getGains(classes.getStringList("classes." + key + ".killGain")), getGains(classes.getStringList("classes." + key + ".breakGain")), getGains(classes.getStringList("classes." + key + ".placeGain")), getGains(classes.getStringList("classes." + key + ".smeltGain")));
            if(isDefaultClass) {
                pl.defaultClass = c;
            }
            pl.classes.add(c);
        }
    }

    private void generateLang() {
        FileConfiguration lang = langYML.getConfig();
        //lang.addDefault("lang.", "&");
        lang.addDefault("lang.proibitedItem", "&cProibited item! Can't use it yet.");
        lang.addDefault("lang.broadcastLevel", "%name &6reached level &e%level &6on class &e%class&6.");
        lang.addDefault("lang.youNowCanUse", "&6You can now use &e%material&6!");
        lang.addDefault("lang.youGainedAPermission", "&6You've received a new permission!");
        lang.addDefault("lang.youGainedMoney", "&6You've received &e%money &6money!");
        lang.addDefault("lang.youGainedPotionEffect", "&6You've received a constant &e%potioneffect&6!");
        lang.addDefault("lang.youGainedRealLevel", "&6You received &e%gained &6real levels to use. You have &e%balance &6now.");
        lang.addDefault("lang.noPermission", "&cNo permission.");
        lang.addDefault("lang.expNeeded", "&6You are &c%exp&6/&c%needed&6.");
        lang.addDefault("lang.classList", "&6Name: &e%name &6| First level exp needed: &e%exp");
        lang.addDefault("lang.alreadyOnOtherClass", "&cAlready on other class.");
        lang.addDefault("lang.youJoinedClass", "&6You joined class &e%name&6!");
        lang.addDefault("lang.youLeftClass", "&4You left %name. &cYour stats has been deleted.");
        lang.addDefault("lang.noClass", "&cYou arent on any class.");
        lang.addDefault("lang.reloadSucceeded", "&aReload succeeded.");
        lang.addDefault("lang.purgeSucceeded", "&aPurge succeeded.");
        lang.addDefault("lang.failedToPurge", "&4Purge failed.");
        lang.addDefault("lang.playerArentOnAnyClass", "&cPlayer arent on any class.");
        lang.addDefault("lang.realLevelUsedMessage", "&6You used %cost real level(s). You have now &e%balance&6 real levels.");
        lang.addDefault("lang.noRealLevelsEnough", "&cYou dont have enough real levels. You have &4%balance&c and it costs &4%cost&c.");
        langYML.saveConfig();
    }

    private Map<String, Integer> getGains(List<String> gains) {
        Map<String, Integer> l = new HashMap<String, Integer>();
        for(String s : gains) {
            String[] s1 = s.split(";");
            try {
                l.put(s1[0], Integer.parseInt(s1[1]));
            } catch(NumberFormatException ignored) {
            }
        }
        return l;
    }

    public void updateMySQLVersionOnFile() {
        configYML.getConfig().set("MySQL.mysqlTableVersion.DO_NOT_CHANGE_THIS", mySQLTableVersion);
        configYML.saveConfig();
    }

    public class CustomConfig {

        private final Plugin pl;
        private final String name;
        private final File file;
        private FileConfiguration fileConfig;

        public CustomConfig(Plugin pl, String name) {
            this.pl = pl;
            this.name = name;
            file = new File(pl.getDataFolder(), name + (name.contains(".yml") ? "" : ".yml"));
        }

        public FileConfiguration getConfig() {
            if(fileConfig == null) {
                reloadConfig();
            }
            return fileConfig;
        }

        private void reloadConfig() {
            fileConfig = YamlConfiguration.loadConfiguration(file);
            InputStream defConfigStream = pl.getResource(name + ".yml");
            if(defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                fileConfig.setDefaults(defConfig);
            }
        }

        public void saveConfig() {
            try {
                getConfig().options().copyDefaults(true);
                getConfig().save(file);
                reloadConfig();
            } catch(IOException ex) {
                pl.getLogger().log(Level.WARNING, "Couldn't save " + name + ".yml");
            }
        }
    }
}

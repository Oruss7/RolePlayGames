package com.jabyftw.rpglv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Rafael
 */
public class Config {

    private final RPGLeveling pl;
    private CustomConfig configYML, langYML, classesYML;
    public FileConfiguration classes;

    public Config(RPGLeveling pl) {
        this.pl = pl;
    }

    public void start() {
        configYML = new CustomConfig("config");
        langYML = new CustomConfig("lang");
        classesYML = new CustomConfig("classes");
        pl.defConfig = configYML.getCustomConfig();
        pl.lang = langYML.getCustomConfig();
        classes = classesYML.getCustomConfig();

        generateConfig();
        generateClasses();
        generateLang();
    }

    private void generateConfig() {
        FileConfiguration config = pl.defConfig;
        config.addDefault("MySQL.username", "root");
        config.addDefault("MySQL.password", "pass");
        config.addDefault("MySQL.url", "jdbc:mysql://localhost:3306/database");
        //config.addDefault("config.", false);
        config.addDefault("config.generateDefClassesYML", true);
        config.addDefault("config.maxLevel", 30);
        configYML.saveCustomConfig();
        pl.maxLevel = config.getInt("config.maxLevel");
        pl.sql = new MySQL(pl, config.getString("MySQL.username"), config.getString("MySQL.password"), config.getString("MySQL.url"));
    }

    private void generateClasses() {
        String[] blocked = {"diamond_sword"};
        classes.addDefault("options.blockedItems", Arrays.asList(blocked));
        if (pl.defConfig.getBoolean("config.generateDefClassesYML")) {
            classes.addDefault("classes.noob.name", "Noob");
            String[] rewards = {"10;permission;essentials.motd", "20;money;5000", "30;item_permission;diamond_sword"};
            classes.addDefault("classes.noob.rewards", Arrays.asList(rewards));
            classes.addDefault("classes.noob.default", true);
            classes.addDefault("classes.noob.levelingEquation", "100*(1.17^(%level-1))"); // Thanks phrstbrn and "Jobs"
            classesYML.saveCustomConfig();
        }
        for (String s : classes.getStringList("options.blockedItems")) {
            pl.proibido.add(pl.getMatFromString(s));
        }
        for (String key : classes.getConfigurationSection("classes").getKeys(false)) {
            String name = classes.getString("classes." + key + ".name");
            String leveling = classes.getString("classes." + key + ".levelingEquation");
            boolean defaultC = classes.getBoolean("classes." + key + ".default");
            Classe c = new Classe(pl, name, leveling, classes.getStringList("classes." + key + ".rewards"));
            if (defaultC) {
                pl.defaultClass = c;
            }
            pl.classes.add(c);
        }
    }

    private void generateLang() {
        FileConfiguration lang = pl.lang;
        //lang.addDefault("lang.", "&");
        lang.addDefault("lang.proibitedItem", "&cProibited item! Can't use it yet.");
        langYML.saveCustomConfig();
    }

    public class CustomConfig {

        private final String name;
        private File file;
        private FileConfiguration fileConfig;

        public CustomConfig(String name) {
            this.name = name;
        }

        public FileConfiguration getCustomConfig() {
            if (fileConfig == null) {
                reloadCustomConfig();
            }
            return fileConfig;
        }

        public void reloadCustomConfig() {
            if (fileConfig == null) {
                file = new File(pl.getDataFolder(), name + ".yml");
            }
            fileConfig = YamlConfiguration.loadConfiguration(file);

            InputStream defConfigStream = pl.getResource(name + ".yml");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                fileConfig.setDefaults(defConfig);
            }
        }

        public void saveCustomConfig() {
            if (file == null) {
                file = new File(pl.getDataFolder(), name + ".yml");
            }
            try {
                getCustomConfig().options().copyDefaults(true);
                getCustomConfig().save(file);
            } catch (IOException ex) {
                pl.getLogger().log(Level.WARNING, "Couldn't save .yml");
            }
        }
    }
}

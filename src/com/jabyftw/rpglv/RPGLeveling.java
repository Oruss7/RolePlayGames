package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Rafael
 */
public class RPGLeveling extends JavaPlugin {

    public MySQL sql;
    public Config config;
    public FileConfiguration defConfig, lang;
    public Permission perm = null;
    public Economy econ = null;
    // RPG things
    public int maxLevel;
    public List<Material> proibido = new ArrayList();
    public Map<Player, Jogador> players = new HashMap();
    public Classe defaultClass;
    public List<Classe> classes = new ArrayList();

    @Override
    public void onEnable() {
        config = new Config(this);
        config.start();
        sql.createTable();
        log("Loaded configuration!");
        setupVault();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginCommand("class").setExecutor(new ClassExecutor(this));
        getServer().getPluginCommand("rpg").setExecutor(new RPGExecutor(this));
        log("Registered commands, listeners and Vault!");
    }

    @Override
    public void onDisable() {
        for (Jogador j : players.values()) {
            j.savePlayer(false);
        }
        log("Disabled!");
    }

    public void log(String msg) {
        getLogger().log(Level.INFO, msg);
    }

    public void broadcast(String msg) {
        for (Player p : getServer().getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    public String getLang(String path) {
        return lang.getString("lang." + path).replaceAll("&", "§");
    }

    public Material getMatFromString(String s) {
        for (Material m : Material.values()) {
            if (m.toString().equalsIgnoreCase(s)) {
                return m;
            }
        }
        try {
            int id = Integer.parseInt(s);
            for (Material m : Material.values()) {
                if (m.getId() == id) {
                    return m;
                }
            }
        } catch (NumberFormatException e) {
        }
        return Material.DIAMOND_SPADE;
    }

    public Classe getClasse(String name) {
        for (Classe c : classes) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return defaultClass;

    }

    private void setupVault() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perm = permissionProvider.getProvider();
        }
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
    }

    public enum Type {

        PERMISSION, ITEM_UNLOCK, MONEY;
    }
}

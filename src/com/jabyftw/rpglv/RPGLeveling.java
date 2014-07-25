package com.jabyftw.rpglv;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
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
    public boolean useExp, blockItemMove;
    public List<Material> proibido = new ArrayList<Material>();
    public Map<Player, Jogador> players = new HashMap<Player, Jogador>();
    public Classe defaultClass;
    public PlayerListener playerListener;
    public List<Classe> classes = new ArrayList<Classe>();

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        config = new Config(this);
        config.start();
        sql.createTable();
        log("Loaded configuration!");
        setupVault();
        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginCommand("class").setExecutor(new ClassExecutor(this));
        getServer().getPluginCommand("rpg").setExecutor(new RPGExecutor(this));
        log("Registered commands, listeners and Vault!");
        log("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public void onDisable() {
        for(Jogador j : players.values()) {
            j.savePlayer(false);
        }
        sql.closeConn();
        getServer().getScheduler().cancelTasks(this);
        log("Disabled!");
    }

    public void onReload(CommandSender sender) {
        onDisable();
        players.clear();
        proibido.clear();
        classes.clear();
        econ = null;
        perm = null;
        config = null;
        sql = null;
        defaultClass = null;
        HandlerList.unregisterAll(this);
        playerListener = null;
        onEnable();
        sender.sendMessage(getLang("reloadSucceeded"));
        findPlayers();
    }

    public void findPlayers() {
        for(final Player p : getServer().getOnlinePlayers()) {
            //noinspection deprecation
            new BukkitRunnable() {
                @Override
                public void run() {
                    Jogador j = sql.getJogador(p.getUniqueId());
                    if(j != null) {
                        players.put(p, j);
                    } else {
                        for(PotionEffect pet : p.getActivePotionEffects()) {
                            p.removePotionEffect(pet.getType());
                        }
                    }
                }
            }.runTaskAsynchronously(this);
        }
    }

    public void log(String msg) {
        getLogger().log(Level.INFO, msg);
    }

    public void broadcast(String msg) {
        for(Player p : getServer().getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    public String getLang(String path) {
        return lang.getString("lang." + path).replaceAll("&", "§");
    }

    @SuppressWarnings("deprecation")
    public Material getMatFromString(String s) {
        for(Material m : Material.values()) {
            if(m.toString().equalsIgnoreCase(s)) {
                return m;
            }
        }
        try {
            int id = Integer.parseInt(s);
            for(Material m : Material.values()) {
                if(m.getId() == id) {
                    return m;
                }
            }
        } catch(NumberFormatException ignored) {
        }
        return Material.DIAMOND_SPADE;
    }

    public Classe getClasse(String name) {
        for(Classe c : classes) {
            if(c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return defaultClass;

    }

    private void setupVault() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if(permissionProvider != null) {
            perm = permissionProvider.getProvider();
        }
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if(economyProvider != null) {
            econ = economyProvider.getProvider();
        }
    }
}

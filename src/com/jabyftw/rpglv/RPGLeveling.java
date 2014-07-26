package com.jabyftw.rpglv;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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

/**
 * @author Rafael
 */
public class RPGLeveling extends JavaPlugin {

    public MySQL sql;
    public Config config;
    //    public FileConfiguration defConfig, lang;
    public Permission perm = null;
    public Economy econ = null;

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
        getLogger().info("Loaded configuration!");
        setupVault();
        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginCommand("class").setExecutor(new ClassExecutor(this));
        getServer().getPluginCommand("rpg").setExecutor(new RPGExecutor(this));
        getLogger().info("Registered commands, listeners and Vault!");
        getLogger().info("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public void onDisable() {
        for(Jogador jogador : players.values()) {
            jogador.savePlayer(false);
        }
        sql.closeConn();
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Disabled!");
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
        for(final Player player : getServer().getOnlinePlayers()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Jogador jogador = sql.getJogador(player.getUniqueId());
                    if(jogador != null) {
                        players.put(player, jogador);
                    } else {
                        for(PotionEffect potionEffect : player.getActivePotionEffects()) {
                            player.removePotionEffect(potionEffect.getType());
                        }
                    }
                }
            }.runTaskAsynchronously(this);
        }
    }

    public void broadcast(String message) {
        for(Player player : getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }
        getLogger().info("Broadcast: " + ChatColor.stripColor(message));
    }

    public String getLang(String path) {
        return config.langYML.getConfig().getString("lang." + path).replaceAll("&", "§");
    }

    @SuppressWarnings("deprecation")
    public Material getMatFromString(String searched) {
        for(Material material : Material.values()) {
            if(material.name().equalsIgnoreCase(searched)) {
                return material;
            }
        }
        if(NumberUtils.isNumber(searched)) {
            int id = Integer.parseInt(searched);
            for(Material material : Material.values()) {
                if(material.getId() == id) {
                    return material;
                }
            }
        }
        return Material.DIAMOND_SPADE;
    }

    public Classe getClasse(String name) {
        for(Classe classe : classes) {
            if(classe.getName().equalsIgnoreCase(name)) {
                return classe;
            }
        }
        return defaultClass;
    }

    private boolean setupVault() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if(permissionProvider != null)
            perm = permissionProvider.getProvider();
        if(economyProvider != null)
            econ = economyProvider.getProvider();
        return econ != null && perm != null;
    }
}

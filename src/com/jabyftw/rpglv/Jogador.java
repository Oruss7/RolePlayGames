package com.jabyftw.rpglv;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * @author Rafael
 */
public class Jogador {

    private final RPGLeveling pl;
    private final Player player;
    private final ArrayList<String> permissions = new ArrayList<String>();
    private final ArrayList<Material> allowedProibido = new ArrayList<Material>();
    private final Classe classe;
    private int level, exp, expNeeded, realLevel;

    public Jogador(RPGLeveling pl, Player player, int level, int exp, int realLevel, String clas) {
        this.pl = pl;
        this.player = player;
        this.level = level;
        this.realLevel = realLevel;
        this.exp = exp;
        classe = pl.getClasse(clas);
        expNeeded = classe.getExpNeeded(level);
        retrieveItemAndPermReward();
        sendStatsToPlayer();
    }

    public Classe getClasse() {
        return classe;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public int getExpNeeded() {
        return expNeeded;
    }

    public int getRealLevel() {
        return realLevel;
    }
    
    public void setRealLevel(int realLevel){
        this.realLevel = realLevel;
    }

    public void addRealLevel(int added) {
        realLevel += added;
    }

    public void addExp(int experience) {
        this.exp += experience;
        while(exp >= expNeeded) { // 15 > 10
            exp = (exp - expNeeded); // 15 - 10 = 5
            if(level < pl.maxLevel)
                addLevel(1, true);
        }// exp / exp needed
        sendStatsToPlayer();
        if(experience > 0) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.3F, 0);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void addLevel(int added, boolean legit) {
        int playerLevel = level;
        for(int i = 1; i <= added; i++) {
            playerLevel += i;
            if(playerLevel >= pl.maxLevel) {
                level = pl.maxLevel;
            } else {
                level = playerLevel;
                expNeeded = classe.getExpNeeded(level);
            }
            sendStatsToPlayer();
            classe.giveReward(level, this);
            if(legit) {
                broadcastLevel(level);
            }
        }
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0);
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 18);
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta data = firework.getFireworkMeta();
        data.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).build());
        firework.setFireworkMeta(data);
        savePlayer(true);
    }

    public void savePlayer(boolean async) {
        if(async) {
            pl.sql.updatePlayer(player.getUniqueId(), level, exp, realLevel, classe.getName());
        } else {
            pl.sql.updatePlayerSync(player.getUniqueId(), level, exp, realLevel, classe.getName());
        }
    }

    public ArrayList<Material> getItemRewardsAllowed() {
        return allowedProibido;
    }

    public void addItemPerm(Material reward) {
        if(pl.proibido.contains(reward) && classe.getProibido().contains(reward)) { // if true && false, this item is prohibited on other class and not disponible on this
            allowedProibido.add(reward);
        }
    }

    private void broadcastLevel(int level) {
        if(classe.getBroadcastLevels().contains(level)) {
            pl.broadcast(pl.getLang("broadcastLevel").replaceAll("%name", player.getDisplayName()).replaceAll("%level", Integer.toString(level)).replaceAll("%class", classe.getName()));
        }
    }

    private void retrieveItemAndPermReward() {
        classe.retrieveItemAndPermReward(this);
    }

    public void sendStatsToPlayer() {
        new BukkitRunnable() {

            @Override
            public void run() {
                player.setTotalExperience(0);
                if(exp > 0) {
                    player.setExp((exp * 1.0F / expNeeded * 1.0F));
                } else {
                    player.setExp(0);
                }
                player.setLevel(level);
            }
        }.runTaskLater(pl, 2);
        getClasse().retrivePotionEffects(this);
    }

    public void addPerm(String reward) {
        permissions.add(reward);
    }

    public void removeAllPermissions() {
        if(pl.config.useVault) {
            for(String permission : permissions) {
                for(World world : pl.getServer().getWorlds()) {
                    pl.perm.playerRemove(world.getName(), player, permission);
                }
            }
        }
    }
}

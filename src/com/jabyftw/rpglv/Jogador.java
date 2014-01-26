package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Rafael
 */
@SuppressWarnings("FieldMayBeFinal")
public class Jogador {
    
    private final RPGLeveling pl;
    private final Player p;
    private int level, exp, expNeeded, reallevel;
    private List<String> permissions = new ArrayList();
    private Classe classe;
    private List<Material> allowedProibido = new ArrayList();
    
    public Jogador(RPGLeveling pl, Player p, int level, int exp, int reallevel, String clas) {
        this.pl = pl;
        this.p = p;
        this.level = level;
        this.reallevel = reallevel;
        this.exp = exp;
        classe = pl.getClasse(clas);
        expNeeded = classe.getExpNeeded(level);
        retriveItemAndPermReward();
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
    
    public int getRealLevel() {
        return reallevel;
    }
    
    public void addRealLevel(int added) {
        reallevel += added;
    }
    
    public void addExp(int experience) {
        this.exp += experience;
        while (exp >= expNeeded) { // 15 > 10
            exp = (exp - expNeeded); // 15 - 10 = 5
            addLevel(1, true);
        }// exp / exp needed
        sendStatsToPlayer();
        if (experience > 0) {
            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 0.3F, 0);
        }
    }
    
    public Player getPlayer() {
        return p;
    }
    
    public void addLevel(int added, boolean legit) {
        int plevel = level;
        for (int i = 1; i <= added; i++) {
            plevel += i;
            if (plevel >= pl.maxLevel) {
                level = pl.maxLevel;
            } else {
                level = plevel;
                expNeeded = classe.getExpNeeded(level);
            }
            sendStatsToPlayer();
            classe.giveReward(level, this);
            if (legit) {
                broadcastLevel(level);
            }
        }
        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 0);
        p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, 18);
        Firework firework = p.getWorld().spawn(p.getLocation(), Firework.class);
        FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
        data.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).build());
        firework.setFireworkMeta(data);
        savePlayer(true);
    }
    
    public void savePlayer(boolean async) {
        if (async) {
            pl.sql.updatePlayer(p.getName().toLowerCase(), level, exp, reallevel, classe.getName());
        } else {
            pl.sql.updatePlayerSync(p.getName().toLowerCase(), level, exp, reallevel, classe.getName());
        }
    }
    
    public List<Material> getItemRewardsAllowed() {
        return allowedProibido;
    }
    
    public void addItemPerm(Material reward) {
        if (pl.proibido.contains(reward) && classe.getProibido().contains(reward)) { // if true && false, this item is proibited on other class and not disponible on this
            allowedProibido.add(reward);
        }
    }
    
    private void broadcastLevel(int level) {
        if (classe.getBroadcastLevels().contains(level)) {
            pl.broadcast(pl.getLang("broadcastLevel").replaceAll("%name", p.getDisplayName()).replaceAll("%level", Integer.toString(level)).replaceAll("%class", classe.getName()));
        }
    }
    
    private void retriveItemAndPermReward() {
        classe.retriveItemAndPermReward(this);
    }
    
    public void sendStatsToPlayer() {
        p.setLevel(level);
        if (exp > 0) {
            p.setExp((exp * 1.0F / expNeeded * 1.0F));
        } else {
            p.setExp(0);
        }
        getClasse().retrivePotionEffects(this);
    }
    
    public void addPerm(String reward) {
        permissions.add(reward);
    }
    
    public void removeAllPermissions() {
        for (String s : permissions) {
            pl.perm.playerRemove(p, s);
        }
    }
}

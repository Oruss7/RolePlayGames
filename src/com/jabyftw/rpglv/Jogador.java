package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class Jogador {

    private final RPGLeveling pl;
    private final Player p;
    private int level, exp, expNeeded;
    private Classe classe;
    private List<Material> allowedProibido = new ArrayList();

    public Jogador(RPGLeveling pl, Player p, int level, int exp, String clas) {
        this.pl = pl;
        this.p = p;
        this.level = level;
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

    public void addExp(int experience) {
        this.exp += experience;
        while (exp >= expNeeded) { // 15 > 10
            exp = (exp - expNeeded); // 15 - 10 = 5
            addLevel(1);
        }// exp / exp needed
        sendStatsToPlayer();
    }

    public Player getPlayer() {
        return p;
    }

    public void addLevel(int added) {
        int plevel = level + added; // prepared level = level + added
        if (plevel > pl.maxLevel) {
            level = pl.maxLevel; // if prepared level is greater than the max, set to the max
        } else {
            level = plevel; // else, use prepared level
        }
        sendStatsToPlayer();
        classe.giveReward(level, this);
        expNeeded = classe.getExpNeeded(level);
        broadcastLevel(level);
        savePlayer();
    }

    public void savePlayer() {
        pl.sql.updatePlayer(p.getName().toLowerCase(), level, exp, classe.getName());
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
    }
}

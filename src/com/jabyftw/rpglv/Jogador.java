package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.List;
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
        p.setExp((exp / expNeeded) * 1F);
    }

    public Player getPlayer() {
        return p;
    }

    public void addLevel(int added) {
        level += added;
        p.setLevel(level);
        classe.giveReward(level, this);
        expNeeded = classe.getExpNeeded(level);
        // TODO: check if he will win something, broadcast if maximum level
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
}

package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class Classe {

    private final RPGLeveling pl;
    private final String name, leveling;
    private Map<Integer, ItemReward> itemRewards = new HashMap();
    private Map<Integer, PermReward> permRewards = new HashMap();
    private Map<Integer, MoneyReward> moneyRewards = new HashMap();

    public Classe(RPGLeveling pl, String name, String leveling, List<String> reward) {
        this.pl = pl;
        this.name = name;
        this.leveling = leveling;
        for (String s : reward) {
            String[] s1 = s.split(";");
            if (s1[1].startsWith("i")) {
                itemRewards.put(Integer.parseInt(s1[0]), new ItemReward(s1[2]));
            } else if (s1[1].startsWith("m")) {
                moneyRewards.put(Integer.parseInt(s1[0]), new MoneyReward(s1[2]));
            } else {
                permRewards.put(Integer.parseInt(s1[0]), new PermReward(s1[2]));
            }
        }
    }

    public List<Material> getProibido() {
        List<Material> l = new ArrayList();
        for (ItemReward ir : itemRewards.values()) {
            l.add(ir.getReward());
        }
        return l;
    }

    public String getName() {
        return name;
    }

    public void giveReward(int level, Jogador j) {
        if (itemRewards.containsKey(level)) {
            itemRewards.get(level).giveReward(j);
        }
        if (permRewards.containsKey(level)) {
            permRewards.get(level).giveReward(j.getPlayer());
        }
        if (moneyRewards.containsKey(level)) {
            moneyRewards.get(level).giveReward(j.getPlayer().getName());
        }
    }

    public int getExpNeeded(int level) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        int result = Integer.MAX_VALUE;
        try {
            result = Integer.parseInt(engine.eval(leveling.replaceAll("%level", Integer.toString(level))).toString());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class ItemReward {

        private final Material reward;

        private ItemReward(String s) {
            this.reward = pl.getMatFromString(s);
        }

        public void giveReward(Jogador j) {
            j.addItemPerm(reward);
        }

        private Material getReward() {
            return reward;
        }
    }

    private class PermReward {

        private final String reward;

        private PermReward(String s) {
            this.reward = s;
        }

        public void giveReward(Player p) {
            pl.perm.playerAdd(p, reward);
        }
    }

    private class MoneyReward {

        private final double reward;

        private MoneyReward(String s) {
            this.reward = Double.parseDouble(s);
        }

        public void giveReward(String p) {
            pl.econ.bankDeposit(p, reward);
        }
    }
}

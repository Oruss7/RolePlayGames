package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
@SuppressWarnings("FieldMayBeFinal") // this messages sucks
public class Classe {

    private final RPGLeveling pl;
    private final String name, leveling, permission;
    private List<Integer> broadcastLevels = new ArrayList();
    private Map<EntityType, Integer> killgain = new HashMap();
    private Map<Material, Integer> breakgain = new HashMap();
    private Map<Material, Integer> placegain = new HashMap();
    private Map<Material, Integer> smeltgain = new HashMap();
    private Map<ItemReward, Integer> itemRewards = new HashMap();
    private Map<PermReward, Integer> permRewards = new HashMap();
    private Map<MoneyReward, Integer> moneyRewards = new HashMap();

    public Classe(RPGLeveling pl, String name, String leveling, String permission, List<String> broadcastLv, List<String> reward, Map<String, Integer> killg, Map<String, Integer> breakg, Map<String, Integer> placeg, Map<String, Integer> smeltg) {
        this.pl = pl;
        this.name = name;
        this.leveling = leveling;
        this.permission = permission.toLowerCase();
        for (String s : broadcastLv) {
            try {
                broadcastLevels.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
            }
        }
        for (String s : reward) {
            String[] s1 = s.split(";");
            if (s1[1].startsWith("i")) {
                itemRewards.put(new ItemReward(s1[2]), Integer.parseInt(s1[0]));
            } else if (s1[1].startsWith("m")) {
                moneyRewards.put(new MoneyReward(s1[2]), Integer.parseInt(s1[0]));
            } else {
                permRewards.put(new PermReward(s1[2]), Integer.parseInt(s1[0]));
            }
        }
        for (Map.Entry<String, Integer> set : killg.entrySet()) {
            for (EntityType et : EntityType.values()) {
                if (et.toString().equalsIgnoreCase(set.getKey())) {
                    this.killgain.put(et, set.getValue());
                }
            }
        }
        for (Map.Entry<String, Integer> set : breakg.entrySet()) {
            this.breakgain.put(pl.getMatFromString(set.getKey()), set.getValue());
        }
        for (Map.Entry<String, Integer> set : placeg.entrySet()) {
            this.placegain.put(pl.getMatFromString(set.getKey()), set.getValue());
        }
        for (Map.Entry<String, Integer> set : smeltg.entrySet()) {
            this.smeltgain.put(pl.getMatFromString(set.getKey()), set.getValue());
        }
    }

    public boolean canJoin(Player p) {
        return p.hasPermission(permission);
    }

    public boolean canJoin(CommandSender sender) {
        return sender.hasPermission(permission);
    }

    public int getGain(EntityType et) {
        if (killgain.containsKey(et)) {
            return killgain.get(et);
        }
        return 0;
    }

    public int getBreakGain(Material mat) {
        if (breakgain.containsKey(mat)) {
            return breakgain.get(mat);
        }
        return 0;
    }

    public int getPlaceGain(Material mat) {
        if (placegain.containsKey(mat)) {
            return placegain.get(mat);
        }
        return 0;
    }

    public int getSmeltGain(Material mat) {
        if (smeltgain.containsKey(mat)) {
            return smeltgain.get(mat);
        }
        return 0;
    }

    public List<Material> getProibido() {
        List<Material> l = new ArrayList();
        for (ItemReward ir : itemRewards.keySet()) {
            l.add(ir.getReward());
        }
        return l;
    }

    public String getName() {
        return name;
    }

    public void retriveItemAndPermReward(Jogador j) {
        for (int i = 0; i <= j.getLevel(); i++) {
            for (Map.Entry<ItemReward, Integer> set : itemRewards.entrySet()) {
                if (set.getValue().equals(i)) {
                    set.getKey().giveReward(j, false);
                }
            }
            for (Map.Entry<PermReward, Integer> set : permRewards.entrySet()) {
                if (set.getValue().equals(i)) {
                    set.getKey().giveReward(j, false);
                }
            }
        }
    }

    public void giveReward(int level, Jogador j) {
        for (Map.Entry<ItemReward, Integer> set : itemRewards.entrySet()) {
            if (set.getValue().equals(level)) {
                set.getKey().giveReward(j, true);
            }
        }
        for (Map.Entry<PermReward, Integer> set : permRewards.entrySet()) {
            if (set.getValue().equals(level)) {
                set.getKey().giveReward(j, true);
            }
        }
        for (Map.Entry<MoneyReward, Integer> set : moneyRewards.entrySet()) {
            if (set.getValue().equals(level)) {
                set.getKey().giveReward(j.getPlayer());
            }
        }
    }

    public int getExpNeeded(int level) {
        int lv = level;
        if (lv < 1) {
            lv = 1;
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        int result = Integer.MAX_VALUE;
        try {
            result = (int) Double.parseDouble(engine.eval(leveling.replaceAll("%level", Integer.toString(lv))).toString());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Integer> getBroadcastLevels() {
        return broadcastLevels;
    }

    public void addPlayer(Player p) {
        if (pl.players.containsKey(p)) {
            p.sendMessage(pl.getLang("alreadyOnOtherClass"));
        } else {
            Jogador j = new Jogador(pl, p, 0, 0, name);
            pl.players.put(p, j);
            pl.sql.insertPlayer(p.getName().toLowerCase(), 0, 0, name);
            p.sendMessage(pl.getLang("youJoinedClass").replaceAll("%name", name));
        }
    }

    private class ItemReward {

        private final Material reward;

        private ItemReward(String s) {
            this.reward = pl.getMatFromString(s);
        }

        public void giveReward(Jogador j, boolean announce) {
            j.addItemPerm(reward);
            if (announce) {
                j.getPlayer().sendMessage(pl.getLang("youNowCanUse").replaceAll("%material", reward.toString().toLowerCase().replaceAll("_", " ")));
            }
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

        public void giveReward(Jogador j, boolean announce) {
            j.addPerm(reward);
            if (pl.perm.playerAdd(j.getPlayer(), reward)) {
                if (announce) {
                    j.getPlayer().sendMessage(pl.getLang("youGainedAPermission"));
                }
            }
        }
    }

    private class MoneyReward {

        private final double reward;

        private MoneyReward(String s) {
            this.reward = Double.parseDouble(s);
        }

        public void giveReward(Player p) {
            pl.econ.bankDeposit(p.getName(), reward);
            p.sendMessage(pl.getLang("youGainedMoney").replaceAll("%money", Double.toString(reward)));
        }
    }
}

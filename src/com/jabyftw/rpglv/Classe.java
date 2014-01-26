package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    private Map<PotionEffectsReward, Integer> potionRewards = new HashMap();
    private Map<RealLevelReward, Integer> realRewards = new HashMap();
    
    public Classe(RPGLeveling pl, String name, String leveling, String permission, List<String> broadcastLv, List<String> reward, List<String> potioneffects, Map<String, Integer> killg, Map<String, Integer> breakg, Map<String, Integer> placeg, Map<String, Integer> smeltg) {
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
            } else if (s1[1].startsWith("r")) {
                realRewards.put(new RealLevelReward(s1[2]), Integer.parseInt(s1[0]));
            } else { // "p"
                permRewards.put(new PermReward(s1[2]), Integer.parseInt(s1[0]));
            }
        }
        for (String s : potioneffects) {
            String[] s1 = s.split("/");
            this.potionRewards.put(new PotionEffectsReward(s1[1]), Integer.parseInt(s1[0]));
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
        retrivePotionEffects(j);
    }
    
    public void retrivePotionEffects(Jogador j) {
        for (int i = 0; i <= j.getLevel(); i++) {
            for (Map.Entry<PotionEffectsReward, Integer> set : potionRewards.entrySet()) {
                if (set.getValue().equals(i)) {
                    set.getKey().giveReward(j.getPlayer(), false);
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
        for (Map.Entry<PotionEffectsReward, Integer> set : potionRewards.entrySet()) {
            if (set.getValue().equals(level)) {
                set.getKey().giveReward(j.getPlayer(), true);
            }
        }
        for (Map.Entry<RealLevelReward, Integer> set : realRewards.entrySet()) {
            if (set.getValue().equals(level)) {
                set.getKey().giveReward(j);
            }
        }
    }
    
    public int getExpNeeded(int level) {
        int lv = level;
        if (lv < 1) {
            lv = 1;
        } else if (lv >= pl.maxLevel) {
            return Integer.MAX_VALUE;
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
            Jogador j = new Jogador(pl, p, 0, 0, 0, name);
            pl.players.put(p, j);
            pl.sql.insertPlayer(p.getName().toLowerCase(), 0, 0, 0, name);
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
            if (announce) {
                j.getPlayer().sendMessage(pl.getLang("youGainedAPermission"));
            }
            for (World w : pl.getServer().getWorlds()) {
                pl.perm.playerAdd(w, j.getPlayer().getName(), reward);
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
    
    private class PotionEffectsReward {
        
        private final PotionEffect reward;
        
        private PotionEffectsReward(String s) {
            String[] s1 = s.split(";");
            this.reward = new PotionEffect(PotionEffectType.getByName(s1[0].toUpperCase()), Integer.MAX_VALUE, Integer.parseInt(s1[1]));
        }
        
        public void giveReward(Player p, boolean announce) {
            if (p.hasPotionEffect(reward.getType())) {
                p.removePotionEffect(reward.getType());
            }
            p.addPotionEffect(reward, false);
            if (announce) {
                p.sendMessage(pl.getLang("youGainedPotionEffect").replaceAll("%potioneffect", reward.getType().toString()));
            }
        }
    }
    
    private class RealLevelReward {
        
        private final int reward;
        
        private RealLevelReward(String s) {
            this.reward = Integer.parseInt(s);
        }
        
        public void giveReward(Jogador j) {
            j.addRealLevel(reward);
            j.getPlayer().sendMessage(pl.getLang("youGainedRealLevel").replaceAll("%gained", Integer.toString(reward)).replaceAll("%balance", Integer.toString(j.getRealLevel())));
        }
    }
}

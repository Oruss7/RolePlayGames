package com.jabyftw.rpglv;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Rafael
 */
@SuppressWarnings("UnusedDeclaration")
public class PlayerListener implements Listener {

    private final RPGLeveling pl;
    private final ArrayList<String> cooldown = new ArrayList<>();

    public PlayerListener(RPGLeveling pl) {
        this.pl = pl;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

            new BukkitRunnable() {

                @Override
                public void run() {
                    Jogador j = pl.sql.getJogador(p.getUniqueId());
                    if (j != null) {
                        pl.players.put(p, j);
                    } else {
                        for (PotionEffect pet : p.getActivePotionEffects()) {
                            p.removePotionEffect(pet.getType());
                        }
                    }
                }
            }.runTaskAsynchronously(pl);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (pl.players.containsKey(p)) {
                pl.players.get(p).sendStatsToPlayer();

            }
        }
    }
    
//    @EventHandler
//    public void onWorldChange(PlayerChangedWorldEvent event){
//        Player p = event.getPlayer();
//        
//        pl.getLogger().log(Level.INFO, "ancien xp: "+p.getExp());
//        
//        if (!pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
//            if (pl.players.containsKey(p)) {
//                pl.players.get(p).savePlayer(true);
//                p.setTotalExperience(pl.players.get(p).getRealLevel());
//            }
//        }else if(!pl.getConfig().getList("config.worlds").contains(event.getFrom())){
//            if (pl.players.containsKey(p)) {
//                pl.players.get(p).setRealLevel((int) p.getTotalExperience());
//                pl.players.get(p).sendStatsToPlayer();
//            }
//        }
//        
//        pl.getLogger().log(Level.INFO, "nouveau xp: "+p.getExp());
//        
//    }

    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
                if (e.getCurrentItem() != null) {
                    if (pl.players.containsKey(p)) {
                        if (e.getInventory() instanceof AnvilInventory) {
                            e.setCancelled(true);
                        } else if (e.getInventory() instanceof EnchantingInventory) {
                            e.setCancelled(true);
                        }
                        if (!pl.blockItemMove && pl.proibido.contains(e.getCurrentItem().getType()) && !pl.players.get(p).getItemRewardsAllowed().contains(e.getCurrentItem().getType()) && !p.hasPermission("rpglevel.bypass.itembanning")) {
                            p.getWorld().dropItemNaturally(p.getLocation(), e.getCurrentItem());
                            p.getInventory().remove(e.getCurrentItem());
                            p.sendMessage(pl.getLang("proibitedItem"));
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) {
            Player p = (Player) e.getPlayer();
            if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
                if (pl.players.containsKey(p)) {
                    pl.players.get(p).sendStatsToPlayer();
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (!pl.blockItemMove && pl.proibido.contains(e.getItem().getItemStack().getType()) && pl.players.containsKey(p)
                    && !pl.players.get(p).getItemRewardsAllowed().contains(e.getItem().getItemStack().getType()) && !p.hasPermission("rpglevel.bypass.itembanning")) {
                if (!cooldown.contains(p.getName())) {
                    p.sendMessage(pl.getLang("proibitedItem"));
                    addCooldown(p.getName(), 3);
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial() == Material.ENCHANTMENT_TABLE) {
                e.setCancelled(true);
            }
            if (e.getItem() != null) {
                if (pl.proibido.contains(e.getItem().getType()) && pl.players.containsKey(p) && !pl.players.get(p).getItemRewardsAllowed().contains(e.getItem().getType()) && !p.hasPermission("rpglevel.bypass.itembanning")) {
                    if (!cooldown.contains(p.getName())) {
                        p.sendMessage(pl.getLang("proibitedItem"));
                        addCooldown(p.getName(), 3);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onQuit(PlayerQuitEvent e) {
        save(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = false)
    public void onKick(PlayerKickEvent e) {
        save(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (pl.players.containsKey(p)) {
                e.setKeepLevel(true);
                e.setDroppedExp(0);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            if (pl.getConfig().getList("config.worlds").contains(killer.getWorld().getName())) {
                if (pl.players.containsKey(killer)) {
                    Jogador j = pl.players.get(killer);
                    if (pl.useExp) {
                        int gain = j.getClasse().getGain(e.getEntityType());
                        if (gain > 0) {
                            e.setDroppedExp(gain);
                        }
                    } else {
                        j.addExp(j.getClasse().getGain(e.getEntityType()));
                        e.setDroppedExp(0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
                if (pl.proibido.contains(p.getItemInHand().getType()) && pl.players.containsKey(p) && !pl.players.get(p).getItemRewardsAllowed().contains(p.getItemInHand().getType()) && !p.hasPermission("rpglevel.bypass.itembanning")) {
                    if (!cooldown.contains(p.getName())) {
                        p.sendMessage(pl.getLang("proibitedItem"));
                        addCooldown(p.getName(), 3);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (pl.players.containsKey(p)) {
                if (pl.proibido.contains(p.getItemInHand().getType())) {
                    if (!pl.players.get(p).getItemRewardsAllowed().contains(p.getItemInHand().getType()) && !p.hasPermission("rpglevel.bypass.itembanning")) {
                        if (!cooldown.contains(p.getName())) {
                            p.sendMessage(pl.getLang("proibitedItem"));
                            addCooldown(p.getName(), 3);
                        }
                        e.setCancelled(true);
                        return;
                    }
                }
                Jogador j = pl.players.get(p);
                j.addExp(j.getClasse().getBreakGain(e.getBlock().getType()));
                e.setExpToDrop(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        pl.getLogger().log(Level.INFO, p.getWorld().getName());
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (pl.players.containsKey(p)) {
                if (pl.proibido.contains(p.getItemInHand().getType()) && pl.players.containsKey(p)
                        && !pl.players.get(p).getItemRewardsAllowed().contains(p.getItemInHand().getType()) && !p.hasPermission("rpglevel.bypass.itembanning")) {
                    if (!cooldown.contains(p.getName())) {
                        p.sendMessage(pl.getLang("proibitedItem"));
                        addCooldown(p.getName(), 3);
                    }
                    e.setCancelled(true);
                    return;
                }
                Jogador j = pl.players.get(p);
                j.addExp(j.getClasse().getPlaceGain(e.getBlock().getType()));
            }
        }
    }

    @EventHandler
    public void onSmelt(FurnaceExtractEvent e) {
        Player p = e.getPlayer();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (pl.players.containsKey(p)) {
                Jogador j = pl.players.get(p);
                j.addExp((j.getClasse().getSmeltGain(e.getItemType())) * e.getItemAmount());
                e.setExpToDrop(0);
            }
        }
    }

    @EventHandler
    public void onExp(PlayerExpChangeEvent e) {
        Player p = e.getPlayer();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            if (pl.players.containsKey(p)) {
                if (pl.useExp) {
                    pl.players.get(p).addExp(e.getAmount());
                }
                e.setAmount(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEnchant(EnchantItemEvent e) {
        Player p = e.getEnchanter();
        if (pl.getConfig().getList("config.worlds").contains(p.getWorld().getName())) {
            e.setCancelled(true);
        }

    }

    private void save(Player p) {
        if (pl.players.containsKey(p)) {
            pl.players.get(p).savePlayer(true);
            pl.players.remove(p);
        }
    }

    public void addCooldown(final String player, int timeInSec) {
        cooldown.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldown.remove(player);
            }
        }.runTaskLater(pl, timeInSec * 20);
    }
}

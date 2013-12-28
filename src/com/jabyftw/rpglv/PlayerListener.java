package com.jabyftw.rpglv;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author Rafael
 */
public class PlayerListener implements Listener {

    private final RPGLeveling pl;

    public PlayerListener(RPGLeveling pl) {
        this.pl = pl;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // last when checking player join
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final String name = p.getName().toLowerCase();
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                Jogador j = pl.sql.getJogador(name);
                if (j != null) {
                    pl.players.put(p, j);
                }
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (pl.players.containsKey(p)) {
            pl.players.get(p).sendStatsToPlayer();

        }
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (pl.proibido.contains(e.getCurrentItem().getType())) {
                if (pl.players.containsKey(p)) {
                    if (!pl.players.get(p).getItemRewardsAllowed().contains(e.getCurrentItem().getType())) {
                        p.sendMessage(pl.getLang("proibitedItem"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPickup(InventoryPickupItemEvent e) {
        if (e.getInventory().getHolder() instanceof Player) {
            Player p = (Player) e.getInventory().getHolder();
            if (pl.proibido.contains(e.getItem().getItemStack().getType())) {
                if (pl.players.containsKey(p)) {
                    if (!pl.players.get(p).getItemRewardsAllowed().contains(e.getItem().getItemStack().getType())) {
                        p.sendMessage(pl.getLang("proibitedItem"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onIteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() != null) {
            if (pl.proibido.contains(e.getItem().getType())) {
                if (pl.players.containsKey(p)) {
                    if (!pl.players.get(p).getItemRewardsAllowed().contains(e.getItem().getType())) {
                        p.sendMessage(pl.getLang("proibitedItem"));
                        e.setCancelled(true);
                    }
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
    public void onEntityDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            if (pl.players.containsKey(killer)) {
                Jogador j = pl.players.get(killer);
                j.addExp(j.getClasse().getGain(e.getEntityType()));
                e.setDroppedExp(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (pl.players.containsKey(p)) {
            Jogador j = pl.players.get(p);
            j.addExp(j.getClasse().getBreakGain(e.getBlock().getType()));
            e.setExpToDrop(0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (pl.players.containsKey(p)) {
            Jogador j = pl.players.get(p);
            j.addExp(j.getClasse().getPlaceGain(e.getBlock().getType()));
        }
    }

    @EventHandler
    public void onSmelt(FurnaceExtractEvent e) {
        Player p = e.getPlayer();
        if (pl.players.containsKey(p)) {
            Jogador j = pl.players.get(p);
            j.addExp((j.getClasse().getSmeltGain(e.getItemType())) * e.getItemAmount());
            e.setExpToDrop(0);
        }
    }

    @EventHandler
    public void onExp(PlayerExpChangeEvent e) {
        if (pl.players.containsKey(e.getPlayer())) {
            e.setAmount(0);
        }
    }

    private void save(Player p) {
        if (pl.players.containsKey(p)) {
            pl.players.get(p).savePlayer();
            pl.players.remove(p);
        }
    }
}

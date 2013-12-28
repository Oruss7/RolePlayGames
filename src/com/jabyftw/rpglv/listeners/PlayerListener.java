package com.jabyftw.rpglv.listeners;

import com.jabyftw.rpglv.Jogador;
import com.jabyftw.rpglv.RPGLeveling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
    public void onItemMove(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (pl.proibido.contains(e.getCurrentItem().getType())) {
                p.sendMessage(pl.getLang("proibitedItem"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickup(InventoryPickupItemEvent e) {
        if (e.getInventory().getHolder() instanceof Player) {
            Player p = (Player) e.getInventory().getHolder();
            if (pl.proibido.contains(e.getItem().getItemStack().getType())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onIteract(PlayerInteractEvent e) {
        if (pl.proibido.contains(e.getItem().getType())) {
            e.getPlayer().sendMessage(pl.getLang("proibitedItem"));
            e.setCancelled(true);
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

    private void save(Player p) {
        if (pl.players.containsKey(p)) {
            pl.players.get(p).savePlayer();
            pl.players.remove(p);
        }
    }
}

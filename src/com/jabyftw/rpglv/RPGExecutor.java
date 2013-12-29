/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jabyftw.rpglv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Rafael
 */
public class RPGExecutor implements CommandExecutor {

    private final RPGLeveling pl;

    public RPGExecutor(RPGLeveling pl) {
        this.pl = pl;
    }

    @Override // /rpg level/kick (player name) (number - level)
    public boolean onCommand(CommandSender sender, Command cmd, String lavel, String[] args) {
        if (sender.hasPermission("rpglevel.manager")) {
            if (args.length < 1) { // rpg
                return false;
            } else {
                if (args[0].equalsIgnoreCase("kick")) {
                    if (args.length > 2) { // rpg kick (name)
                        Player p = pl.getServer().getPlayer(args[1]);
                        if (p != null && pl.players.containsKey(p)) {
                            pl.sql.deletePlayer(p.getName().toLowerCase());
                            p.setExp(0);
                            p.setLevel(0);
                            p.sendMessage(pl.getLang("youLeftClass").replaceAll("%name", pl.players.get(p).getClasse().getName()));
                            pl.players.get(p).removeAllPermissions();
                            pl.players.remove(p);
                            return true;
                        } else {
                            sender.sendMessage(pl.getLang("playerArentOnAnyClass"));
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (args.length < 2) { // /rpg 3
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            if (pl.players.containsKey(p)) {
                                try {
                                    pl.players.get(p).addLevel(Integer.parseInt(args[1]), false);
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                                sender.sendMessage("§eDone!");
                                return true;
                            } else {
                                sender.sendMessage(pl.getLang("noClass"));
                                return true;
                            }
                        } else {
                            sender.sendMessage("Only ingame.");
                            return true;
                        }
                    } else { // /rpg 3 jaby
                        Player p = pl.getServer().getPlayer(args[2]);
                        if (p != null && pl.players.containsKey(p)) {
                            try {
                                pl.players.get(p).addLevel(Integer.parseInt(args[1]), false);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                            sender.sendMessage("§eDone!");
                            return true;
                        } else {
                            sender.sendMessage(pl.getLang("playerArentOnAnyClass"));
                            return true;
                        }
                    }
                }
            }
        } else {
            sender.sendMessage(pl.getLang("noPermission"));
            return true;
        }
    }
}
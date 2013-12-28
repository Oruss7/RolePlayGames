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

    @Override // /class (name/list/exit)
    public boolean onCommand(CommandSender sender, Command cmd, String lavel, String[] args) {
        if (sender.hasPermission("rpglevel.join")) {
            if (args.length < 1) {
                return false;
            } else {
                if (args[0].equalsIgnoreCase("exit")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (pl.players.containsKey(p)) {
                            pl.sql.deletePlayer(p.getName().toLowerCase());
                            p.setExp(0);
                            p.setLevel(0);
                            p.sendMessage(pl.getLang("youLeftClass").replaceAll("%name", pl.players.get(p).getClasse().getName()));
                            pl.players.remove(p);
                            return true;
                        } else {
                            p.sendMessage(pl.getLang("noClass"));
                            return true;
                        }
                    } else {
                        sender.sendMessage("Only ingame.");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    for (Classe c : pl.classes) {
                        sender.sendMessage(pl.getLang("classList").replaceAll("%name", c.getName()).replaceAll("%exp", Double.toString(c.getExpNeeded(1))));
                    }
                    return true;
                } else {
                    if (sender instanceof Player) {
                        String classe = args[0];
                        pl.getClasse(classe).addPlayer(((Player) sender));
                        return true;
                    } else {
                        sender.sendMessage("Only ingame.");
                        return true;
                    }
                }
            }
        } else {
            sender.sendMessage(pl.getLang("noPermission"));
            return true;
        }
    }
}

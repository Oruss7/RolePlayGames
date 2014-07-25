package com.jabyftw.rpglv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * @author Rafael
 */
public class ClassExecutor implements CommandExecutor {

    private final RPGLeveling pl;

    public ClassExecutor(RPGLeveling pl) {
        this.pl = pl;
    }

    @Override // /class (name/list/exit)
    public boolean onCommand(CommandSender sender, Command cmd, String lavel, String[] args) {
        if(sender.hasPermission("rpglevel.join")) {
            if(args.length < 1) {
                return false;
            } else {
                if(args[0].equalsIgnoreCase("exit")) {
                    if(sender.hasPermission("rpglevel.exit")) {
                        if(sender instanceof Player) {
                            Player p = (Player) sender;
                            if(pl.players.containsKey(p)) {
                                pl.sql.deletePlayer(p.getUniqueId());
                                p.setExp(0);
                                p.setLevel(0);
                                for(PotionEffect pet : p.getActivePotionEffects()) {
                                    p.removePotionEffect(pet.getType());
                                }
                                p.sendMessage(pl.getLang("youLeftClass").replaceAll("%name", pl.players.get(p).getClasse().getName()));
                                pl.players.get(p).removeAllPermissions();
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
                    } else {
                        sender.sendMessage(pl.getLang("noPermission"));
                        return true;
                    }
                } else if(args[0].equalsIgnoreCase("list")) {
                    if(sender.hasPermission("rpglevel.list")) {
                        for(Classe c : pl.classes) {
                            if(c.canJoin(sender)) {
                                sender.sendMessage(pl.getLang("classList").replaceAll("%name", c.getName()).replaceAll("%exp", Double.toString(c.getExpNeeded(1))));
                            }
                        }
                        return true;
                    } else {
                        sender.sendMessage(pl.getLang("noPermission"));
                        return true;
                    }
                } else {
                    if(sender instanceof Player) {
                        String classe = args[0];
                        Classe c = pl.getClasse(classe);
                        if(c.canJoin(sender)) {
                            c.addPlayer(((Player) sender));
                            return true;
                        } else {
                            sender.sendMessage(pl.getLang("noPermission"));
                            return true;
                        }
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

package com.jabyftw.rpglv;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

public class MySQL {

    private final RPGLeveling pl;
    private final String user, pass, url;
    public Connection conn = null;

    public MySQL(RPGLeveling pl, String username, String password, String url) {
        this.pl = pl;
        this.user = username;
        this.pass = password;
        this.url = url;
    }

    public Connection getConn() {
        if (conn != null) {
            return conn;
        }
        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            pl.getLogger().log(Level.WARNING, "Couldn''t connect to MySQL: {0}", e.getMessage());
        }
        return conn;
    }

    public void closeConn() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException ex) {
                pl.getLogger().log(Level.WARNING, "Couldn''t connect to MySQL: {0}", ex.getMessage());
            }
        }
    }

    public void createTable() {
        pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `rpgplayers` (\n"
                            + "  `name` VARCHAR(20) NOT NULL,\n"
                            + "  `level` INT NOT NULL DEFAULT 0,\n"
                            + "  `exp` INT NOT NULL DEFAULT 0,\n"
                            + "  `reallevel` INT NOT NULL DEFAULT 0,\n"
                            + "  `class` VARCHAR(45) NOT NULL,\n"
                            + "  PRIMARY KEY (`name`),\n"
                            + "  UNIQUE INDEX `name_UNIQUE` (`name` ASC));");
                } catch (SQLException e) {
                    e.printStackTrace();
                    pl.getLogger().log(Level.SEVERE, "Disabling plugin, cant create table.");
                    pl.getServer().getPluginManager().disablePlugin(pl);
                }
                try {
                    if (Double.parseDouble(pl.getDescription().getVersion()) < 0.6D) {
                        getConn().createStatement().executeUpdate("ALTER TABLE `rpgplayers` \n"
                                + "ADD COLUMN `reallevel` INT NOT NULL DEFAULT 0 AFTER `exp`;");
                    }
                } catch (NumberFormatException e) {
                } catch (SQLException ex) {
                }
            }
        });
    }

    public void insertPlayer(String name, int level, int exp, int reallevel, String classe) {
        final String name2 = name.toLowerCase();
        final int level2 = level;
        final int reallevel2 = reallevel;
        final int exp2 = exp;
        final String classe2 = classe;
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("INSERT INTO `rpgplayers` (`name`, `level`, `exp`, `reallevel`, `class`) VALUES ('" + name2 + "', " + level2 + ", " + exp2 + ", " + reallevel2 + " ,'" + classe2 + "');");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updatePlayer(String name, int level, int exp, int reallevel, String classe) {
        final String name2 = name.toLowerCase();
        final int level2 = level;
        final int reallevel2 = reallevel;
        final int exp2 = exp;
        final String classe2 = classe;
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("UPDATE `rpgplayers` SET `level`=" + level2 + ", `exp`=" + exp2 + ", `reallevel`=" + reallevel2 + ", `class`='" + classe2 + "' WHERE `name`='" + name2 + "';");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Jogador getJogador(String name) {
        Jogador j = null; // not on database or not online
        try {
            ResultSet rs = getConn().createStatement().executeQuery("SELECT `level`, `exp`, `reallevel`, `class` FROM `rpgplayers` WHERE `name`='" + name + "';");
            while (rs.next()) {
                Player p = pl.getServer().getPlayer(name);
                if (p != null) {
                    j = new Jogador(pl, p, rs.getInt("level"), rs.getInt("exp"), rs.getInt("reallevel"), rs.getString("class"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return j;
    }

    public void deletePlayer(String n) {
        final String name = n.toLowerCase();
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("DELETE FROM `rpgplayers` WHERE `name`='" + name + "';");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updatePlayerSync(String name, int level, int exp, int reallevel, String classe) {
        try {
            getConn().createStatement().execute("UPDATE `rpgplayers` SET `level`=" + level + ", `exp`=" + exp + ", `reallevel`=" + reallevel + ", `class`='" + classe + "' WHERE `name`='" + name + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

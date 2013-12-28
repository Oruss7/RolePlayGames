package com.jabyftw.rpglv;

import java.sql.*;
import java.util.logging.Level;
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
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().executeUpdate(""); // (`name`, `level`, `exp`, `class`)
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void insertPlayer(String name, int level, int exp, String classe) {
        final String name2 = name;
        final int level2 = level;
        final int exp2 = exp;
        final String classe2 = classe;
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("INSERT INTO `rpgplayers` (`name`, `level`, `exp`, `class`) VALUES ('" + name2 + "', " + level2 + ", " + exp2 + ", '" + classe2 + "');");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updatePlayer(String name, int level, int exp, String classe) {
        final String name2 = name;
        final int level2 = level;
        final int exp2 = exp;
        final String classe2 = classe;
        pl.getServer().getScheduler().scheduleAsyncDelayedTask(pl, new Runnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("UPDATE `rpgplayers` SET `level`=" + level2 + ", `exp`=" + exp2 + ", `class`='" + classe2 + "' WHERE `name`='" + name2 + "';");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Jogador getJogador(String name) {
        Jogador j = null; // not on database or not online
        try {
            ResultSet rs = getConn().createStatement().executeQuery("SELECT `level`, `exp`, `class` FROM `rpgplayers` WHERE `name`='" + name + "';");
            while (rs.next()) {
                Player p = pl.getServer().getPlayer(name);
                if (p != null) {
                    j = new Jogador(pl, p, rs.getInt("level"), rs.getInt("exp"), rs.getString("class"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return j;
    }
}
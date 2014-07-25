package com.jabyftw.rpglv;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

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
        try {
            boolean invalidConnection = false;
            if(conn == null || (invalidConnection = !conn.isValid(2))) {
                if(invalidConnection) {
                    closeConn();
                }
                conn = DriverManager.getConnection(url, user, pass);
                pl.getLogger().info("Reconnected to MySQL (connection was closed or invalid).");
            }
            return conn;
        } catch(SQLException e) {
            pl.getLogger().warning("Couldn't connect to MySQL: " + e.getMessage());
        }
        return null;
    }

    public void closeConn() {
        if(conn != null) {
            try {
                conn.close();
                conn = null;
            } catch(SQLException ex) {
                pl.getLogger().log(Level.WARNING, "Couldn't connect to MySQL: " + ex.getMessage());
            }
        }
    }

    public void createTable() {
        if(pl.config.mySQLTableVersion < 2) {
            try {
                getConn().createStatement().executeUpdate("DROP TABLE IF EXISTS `rpgplayers`;");
                getConn().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `rpgplayers` (\n" +
                        "  `uuid`      CHAR(38)    NOT NULL,\n" +
                        "  `level`     INT         NOT NULL DEFAULT 0,\n" +
                        "  `exp`       INT         NOT NULL DEFAULT 0,\n" +
                        "  `reallevel` INT         NOT NULL DEFAULT 0,\n" +
                        "  `class`     VARCHAR(45) NOT NULL,\n" +
                        "  PRIMARY KEY (`uuid`),\n" +
                        "  UNIQUE INDEX `name_UNIQUE` (`uuid` ASC));");
                pl.config.mySQLTableVersion++;
                pl.config.updateMySQLVersionOnFile();
            } catch(SQLException e) {
                pl.getLogger().log(Level.SEVERE, "Disabling plugin, cant MySQL create table: " + e.getMessage());
                e.printStackTrace();
                pl.getServer().getPluginManager().disablePlugin(pl);
            }
        }
    }

    public void insertPlayer(final UUID uuid, final int level, final int exp, final int reallevel, final String classe) {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("INSERT INTO `rpgplayers` (`uuid`, `level`, `exp`, `reallevel`, `class`) VALUES ('" + uuid + "', " + level + ", " + exp + ", " + reallevel + ", '" + classe + "');");
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(pl);
    }

    public void updatePlayer(final UUID uuid, final int level, final int exp, final int reallevel, final String classe) {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("UPDATE `rpgplayers` SET `level`=" + level + ", `exp`=" + exp + ", `reallevel`=" + reallevel + ", `class`='" + classe + "' WHERE `uuid`='" + uuid + "';");
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(pl);
    }

    public Jogador getJogador(UUID uuid) {
        Jogador j = null; // not on database or not online
        try {
            ResultSet rs = getConn().createStatement().executeQuery("SELECT `level`, `exp`, `reallevel`, `class` FROM `rpgplayers` WHERE `uuid`='" + uuid + "';");
            while(rs.next()) {
                Player p = pl.getServer().getPlayer(uuid);
                if(p != null) {
                    j = new Jogador(pl, p, rs.getInt("level"), rs.getInt("exp"), rs.getInt("reallevel"), rs.getString("class"));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return j;
    }

    public void deletePlayer(final UUID uuid) {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    getConn().createStatement().execute("DELETE FROM `rpgplayers` WHERE `uuid`='" + uuid + "';");
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(pl);
    }

    public void updatePlayerSync(UUID uuid, int level, int exp, int reallevel, String classe) {
        try {
            getConn().createStatement().execute("UPDATE `rpgplayers` SET `level`=" + level + ", `exp`=" + exp + ", `reallevel`=" + reallevel + ", `class`='" + classe + "' WHERE `uuid`='" + uuid + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean purgeDatabase() {
        try {
            getConn().createStatement().execute("TRUNCATE TABLE `rpgplayers`;");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

package com.jabyftw.rpglv;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
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
        if(pl.config.mySQLTableVersion < 2) { // Default is 1, witch it is less than 2. So it'll drop and recreate when 1 is set
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
                    PreparedStatement preparedStatement = getConn().prepareStatement("INSERT INTO `rpgplayers` (`uuid`, `level`, `exp`, `reallevel`, `class`) VALUES (?, ?, ?, ?, ?);");
                    preparedStatement.setObject(1, uuid);
                    preparedStatement.setInt(2, level);
                    preparedStatement.setInt(3, exp);
                    preparedStatement.setInt(4, reallevel);
                    preparedStatement.setString(5, classe);
                    preparedStatement.execute();
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
                updatePlayerSync(uuid, level, exp, reallevel, classe);
            }
        }.runTaskAsynchronously(pl);
    }

    public Jogador getJogador(UUID uuid) {
        Jogador jogador = null; // not on database or not online
        try {
            PreparedStatement preparedStatement = getConn().prepareStatement("SELECT `level`, `exp`, `reallevel`, `class` FROM `rpgplayers` WHERE `uuid`=?;");
            preparedStatement.setObject(1, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                Player player = pl.getServer().getPlayer(uuid);
                if(player != null) {
                    jogador = new Jogador(pl, player, resultSet.getInt("level"), resultSet.getInt("exp"), resultSet.getInt("reallevel"), resultSet.getString("class"));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return jogador;
    }

    public void deletePlayer(final UUID uuid) {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = getConn().prepareStatement("DELETE FROM `rpgplayers` WHERE `uuid`=?;");
                    preparedStatement.setObject(1, uuid);
                    preparedStatement.execute();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(pl);
    }

    public void updatePlayerSync(UUID uuid, int level, int exp, int reallevel, String classe) {
        try {
            PreparedStatement preparedStatement = getConn().prepareStatement("UPDATE `rpgplayers` SET `level`=?, `exp`=?, `reallevel`=?, `class`=? WHERE `uuid`=?;");
            preparedStatement.setInt(1, level);
            preparedStatement.setInt(2, exp);
            preparedStatement.setInt(3, reallevel);
            preparedStatement.setString(4, classe);
            preparedStatement.setObject(5, uuid);
            preparedStatement.executeUpdate();
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

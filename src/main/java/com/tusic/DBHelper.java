package com.tusic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:music.db";
    private Connection conn;

    public DBHelper() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTableIfNotExists();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    private void createTableIfNotExists() throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS songs (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    title TEXT NOT NULL,\n"
                + "    path TEXT NOT NULL\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void addSong(String title, String path) {
        String sql = "INSERT INTO songs(title, path) VALUES(?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, path);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
        }
    }

    public void updateStatus(String title, String status) {
        String sql = "UPDATE songs SET status = ? WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            //   e.printStackTrace();
        }
    }

    public static ArrayList<String> listSongs() throws SQLException {
        String sql = "SELECT path FROM songs";
        ArrayList<String> songs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(rs.getString("path"));
            }
        }
        return songs;
    }
}

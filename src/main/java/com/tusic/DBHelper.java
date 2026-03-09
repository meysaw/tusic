package com.tusic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * SQLite database helper for managing the song library.
 */
public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:music.db";
    private Connection conn;

    public DBHelper() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            initializeSchema();
        } catch (SQLException e) {
            System.err.println("Database init failed: " + e.getMessage());
        }
    }

    /**
     * Creates the songs table if it doesn't already exist.
     */
    private void initializeSchema() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS songs ("
                + "  id   INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  title TEXT NOT NULL,"
                + "  path  TEXT NOT NULL UNIQUE"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Add a song to the library. Duplicates (same path) are silently ignored.
     *
     * @param title display name of the song
     * @param path  file system path to the audio file
     */
    public void addSong(String title, String path) {
        String sql = "INSERT OR IGNORE INTO songs(title, path) VALUES(?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, path);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add song: " + e.getMessage());
        }
    }

    /**
     * Delete a song from the library by title.
     *
     * @param title the song title to remove
     */
    public void deleteSong(String title) {
        String sql = "DELETE FROM songs WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete song: " + e.getMessage());
        }
    }

    /**
     * List all song file paths in the library.
     * Uses the shared connection instead of creating a separate one.
     *
     * @return list of file paths
     */
    public ArrayList<String> listSongs() {
        String sql = "SELECT path FROM songs";
        ArrayList<String> songs = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(rs.getString("path"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to list songs: " + e.getMessage());
        }
        return songs;
    }

    /**
     * Close the database connection.
     */
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close DB: " + e.getMessage());
        }
    }
}

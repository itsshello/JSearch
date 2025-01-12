package indexer;

import dataset.Settings;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class CustomIndexer {
    private static final String DB_URL = initializeDatabasePath();

    public CustomIndexer() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createCrawledDataTableSQL = "CREATE TABLE IF NOT EXISTS crawled_data (" +
                    "url TEXT PRIMARY KEY," +
                    "title TEXT," +
                    "body TEXT" +
                    ")";
            String createVisitedUrlsTableSQL = "CREATE TABLE IF NOT EXISTS visited_urls (" +
                    "url TEXT PRIMARY KEY" +
                    ")";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createCrawledDataTableSQL);
                stmt.execute(createVisitedUrlsTableSQL);
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public void indexData(String url, String title, String body) {
        String insertSQL = "INSERT OR REPLACE INTO crawled_data (url, title, body) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, url);
            pstmt.setString(2, title);
            pstmt.setString(3, body);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting data into database: " + e.getMessage());
        }

        // mark URL as visited
        markUrlAsVisited(url);
    }

    // add URL to visited list
    private void markUrlAsVisited(String url) {
        String insertSQL = "INSERT OR IGNORE INTO visited_urls (url) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, url);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding URL to visited list: " + e.getMessage());
        }
    }

    public ResultSet search(String query) throws SQLException {
        String searchSQL = "SELECT * FROM crawled_data WHERE title LIKE ? OR body LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");

            return pstmt.executeQuery();
        }
    }

    // close database connection if needed
    public void close() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    private static String initializeDatabasePath() {
        String dbPath = Settings.getSetting("CRAWL_DATA_PATH");
        if (!dbPath.endsWith(".db")) {
            dbPath += ".db";
        }

        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating database file: " + e.getMessage());
            }
        }

        return "jdbc:sqlite:" + dbPath;
    }
}
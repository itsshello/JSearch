package dataset;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class DatasetModule {

    private static final String DB_URL = initializeDatabasePath();
    private static Set<String> seedUrls = new HashSet<>();

    public static void addSeedUrl(String url) {
        seedUrls.add(url);
    }

    public static void addSeedUrl(String[] urls) {
        for (String url : urls) {
            seedUrls.add(url);
        }
    }

    public static Set<String> getSeedUrls() {
        seedUrls.add("https://en.wikipedia.org/wiki/pancake");
        // seedUrls.add("https://example.org");

        return seedUrls;
    }

    // all visited URLs
    public static Set<String> getVisitedUrls() {
        Set<String> visitedUrls = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String query = "SELECT url FROM visited_urls";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                visitedUrls.add(rs.getString("url"));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving visited URLs: " + e.getMessage());
        }

        return visitedUrls;
    }

    public static void init() {
        String createVisitedUrlsTableSQL = "CREATE TABLE IF NOT EXISTS visited_urls (" +
                                           "url TEXT PRIMARY KEY)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createVisitedUrlsTableSQL);
            System.out.println("Visited URLs table is ready.");
        } catch (SQLException e) {
            System.err.println("Error creating visited_urls table: " + e.getMessage());
        }
    }

    /**
     * This function marks all URLs in the database as visited
     * This was used to switch from V1 to V2 of the database schema
     * And this is no need now
     */
    public static void markAllUrlsAsVisited() {
        String selectAllUrlsSQL = "SELECT url FROM crawled_data";
        String insertVisitedSQL = "INSERT OR IGNORE INTO visited_urls (url) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (Statement selectStmt = conn.createStatement();
                 PreparedStatement insertStmt = conn.prepareStatement(insertVisitedSQL);
                 ResultSet rs = selectStmt.executeQuery(selectAllUrlsSQL)) {

                int totalUrls = 0;
                while (rs.next()) totalUrls++;
                rs.close();
                ResultSet rs2 = selectStmt.executeQuery(selectAllUrlsSQL);

                int processed = 0;

                System.out.println("Starting to mark URLs as visited...");
                while (rs2.next()) {
                    String url = rs2.getString("url");

                    insertStmt.setString(1, url);
                    insertStmt.addBatch();
                    processed++;

                    displayProgressBar(processed, totalUrls);
                }

                insertStmt.executeBatch();
                conn.commit();

                System.out.println("\nAll existing URLs have been marked as visited.");

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error updating visited URLs: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    private static void displayProgressBar(int processed, int total) {
        int progressBarWidth = 50;
        int progress = (int) ((processed / (double) total) * progressBarWidth);
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < progressBarWidth; i++) {
            if (i < progress) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ");
        bar.append(processed).append("/").append(total);

        System.out.print("\r" + bar.toString());
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
package dataset;

import indexer.CustomIndexer;

import java.sql.*;

public class SearchEngine {
    private CustomIndexer indexer;

    public SearchEngine() {
        indexer = new CustomIndexer();
    }

    public void search(String query) {
        try {
            ResultSet rs = indexer.search(query);

            if (!rs.isBeforeFirst()) {
                System.out.println("No results found for: " + query);
                return;
            }

            System.out.println("Search results for: " + query);
            while (rs.next()) {
                String url = rs.getString("url");
                String title = rs.getString("title");
                String body = rs.getString("body");

                System.out.println("URL: " + url);
                System.out.println("Title: " + title);
                System.out.println("Body (snippet): " + body.substring(0, Math.min(body.length(), 100)) + "...");
                System.out.println("---------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SearchEngine searchEngine = new SearchEngine();

        String query = "Java programming";
        searchEngine.search(query);
    }
}

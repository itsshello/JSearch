package dataset;

import java.sql.*;
import java.util.*;

public class BrowseData {
    private static final String DB_URL = String.join("", "jdbc:sqlite:", Settings.getSetting("CRAWL_DATA_PATH")); // SQLite database
    private static int MAX_RESULTS;
    

    public static void Search() {
        try {
            MAX_RESULTS = Integer.parseInt(Settings.getSetting("MAX_RESULTS"));
        } catch (NumberFormatException e) {
            System.err.println("Invalid MAX_RESULTS setting. Using default value: 5");
            MAX_RESULTS = 5;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your search query: ");
        String query = scanner.nextLine();
        scanner.close();

        System.out.println("Searching for: " + query);
        if (query == "") Search();
        searchAndPrintBestMatches(query);
    }

    public static void Search(int MAX_RESULTS_) {
        try {
            MAX_RESULTS = MAX_RESULTS_;
        } catch (NumberFormatException e) {
            System.err.println("Invalid MAX_RESULTS setting. Using default value: 5");
            MAX_RESULTS = 5;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your search query: ");
        String query = scanner.nextLine();
        scanner.close();

        System.out.println("Searching for: " + query);
        if (query == "") Search();
        searchAndPrintBestMatches(query);
    }

    public static void setMAXResults(int max) {
        MAX_RESULTS = max;
    }

    public static void searchAndPrintBestMatches(String query) {
        String sql = "SELECT url, title, body FROM crawled_data";
        List<Document> documents = new ArrayList<>();
        Map<String, Double> queryVector = createQueryVector(query);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String url = rs.getString("url");
                String title = rs.getString("title");
                String body = rs.getString("body");

                // Tokenize document content and calculate TF-IDF
                Document document = new Document(url, title, body);
                document.calculateTFIDF();
                documents.add(document);
            }

            // Rank documents by cosine similarity
            Map<String, Double> scores = new HashMap<>();
            for (Document document : documents) {
                double score = cosineSimilarity(queryVector, document.getTfIdfVector());
                scores.put(document.getUrl(), score);
            }

            scores.entrySet().stream()
                    .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                    .limit(MAX_RESULTS) // top results
                    .forEach(entry -> {
                        String url = entry.getKey();
                        double score = entry.getValue();
                        System.out.println("\nURL: " + url);
                        System.out.println("Relevance Score: " + score);
                    });

        } catch (SQLException e) {
            System.err.println("Error while searching the database: " + e.getMessage());
        }
    }

    private static Map<String, Double> createQueryVector(String query) {
        Map<String, Double> queryVector = new HashMap<>();
        String[] queryTerms = query.toLowerCase().split("\\s+");

        for (String term : queryTerms) {
            queryVector.put(term, 1.0); // all terms are weighted equally (TF = 1)
        }

        return queryVector;
    }

    private static double cosineSimilarity(Map<String, Double> queryVector, Map<String, Double> documentVector) {
        double dotProduct = 0.0;
        double queryMagnitude = 0.0;
        double docMagnitude = 0.0;

        // Calculate dot product and magnitudes
        for (String term : queryVector.keySet()) {
            if (documentVector.containsKey(term)) {
                dotProduct += queryVector.get(term) * documentVector.get(term);
            }
        }

        for (double value : queryVector.values()) {
            queryMagnitude += value * value;
        }

        for (double value : documentVector.values()) {
            docMagnitude += value * value;
        }

        queryMagnitude = Math.sqrt(queryMagnitude);
        docMagnitude = Math.sqrt(docMagnitude);

        return (dotProduct) / (queryMagnitude * docMagnitude);
    }

    // document data and TF-IDF vector
    static class Document {
        private String url;
        private String title;
        private String body;
        private Map<String, Double> tfIdfVector = new HashMap<>();

        public Document(String url, String title, String body) {
            this.url = url;
            this.title = title;
            this.body = body;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, Double> getTfIdfVector() {
            return tfIdfVector;
        }

        // Tokenize document and calculate TF-IDF vector
        public void calculateTFIDF() {
            String content = title + " " + body;
            Map<String, Integer> termFrequency = new HashMap<>();
            String[] words = content.toLowerCase().split("\\s+");

            // Count term frequency
            for (String word : words) {
                termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
            }

            // Calculate TF-IDF
            for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
                String term = entry.getKey();
                double tf = entry.getValue() / (double) words.length;   // Term frequency
                double idf = calculateIDF(term);                        // Inverse Document Frequency
                tfIdfVector.put(term, tf * idf);
            }
        }

        // Calculate IDF (Inverse Document Frequency) of a term
        private double calculateIDF(String term) {
            double totalDocs = 1000; // You can replace this with the actual document count
            double docFreq = getDocumentFrequency(term);
            return Math.log(totalDocs / (1 + docFreq)); // +1 to avoid division by zero
        }

        private double getDocumentFrequency(String term) {
            /**
             * This funtion needs alot of time and calculating that is near imposible for me.
             * You can replace this with the actual document frequency calculation.
             * But I will leave it blank beacuse my computer can't handle it.
             */
            return 100;
        }
    }
}

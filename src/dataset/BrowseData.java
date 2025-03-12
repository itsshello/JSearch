package dataset;

import java.math.BigDecimal;
import java.math.MathContext;
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
        Map<String, BigDecimal> queryVector = createQueryVector(query);

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
            Map<String, BigDecimal> scores = new HashMap<>();
            for (Document document : documents) {
                BigDecimal score = cosineSimilarity(queryVector, document.getTfIdfVector());

                if (score.compareTo(BigDecimal.valueOf(Double.NaN)) == 0) {
                    continue;
                }

                scores.put(document.getUrl(), score);
            }

            scores.entrySet().stream()
                    .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                    .limit(MAX_RESULTS) // top results
                    .forEach(entry -> {
                        String url = entry.getKey();
                        BigDecimal score = entry.getValue();
                        System.out.println("\nURL: " + url);
                        System.out.println("Relevance Score: " + score);
                    });

        } catch (SQLException e) {
            System.err.println("Error while searching the database: " + e.getMessage());
        }
    }

    private static Map<String, BigDecimal> createQueryVector(String query) {
        Map<String, BigDecimal> queryVector = new HashMap<>();
        String[] queryTerms = query.toLowerCase().split("\\s+");

        for (String term : queryTerms) {
            queryVector.put(term, BigDecimal.ONE); // all terms are weighted equally (TF = 1)
        }

        return queryVector;
    }

    private static BigDecimal cosineSimilarity(Map<String, BigDecimal> queryVector, Map<String, BigDecimal> documentVector) {
        BigDecimal dotProduct = BigDecimal.ZERO;
        BigDecimal queryMagnitude = BigDecimal.ZERO;
        BigDecimal docMagnitude = BigDecimal.ZERO;

        // Calculate dot product and magnitudes
        for (String term : queryVector.keySet()) {
            if (documentVector.containsKey(term)) {
                dotProduct = dotProduct.add(queryVector.get(term).multiply(documentVector.get(term)));
            }
        }

        for (BigDecimal value : queryVector.values()) {
            queryMagnitude = queryMagnitude.add(value.multiply(value));
        }

        for (BigDecimal value : documentVector.values()) {
            docMagnitude = docMagnitude.add(value.multiply(value));
        }

        queryMagnitude = sqrt(queryMagnitude);
        docMagnitude = sqrt(docMagnitude);

        if (queryMagnitude.equals(BigDecimal.ZERO) || docMagnitude.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO; // avoid dividing by zero
        }

        return dotProduct.divide(queryMagnitude.multiply(docMagnitude), MathContext.DECIMAL128);
    }

    // calculate square root
    private static BigDecimal sqrt(BigDecimal value) {
        return value.sqrt(new MathContext(16));
    }

    // document data and TF-IDF vector
    static class Document {
        private String url;
        private String title;
        private String body;
        private Map<String, BigDecimal> tfIdfVector = new HashMap<>();

        public Document(String url, String title, String body) {
            this.url = url;
            this.title = title;
            this.body = body;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, BigDecimal> getTfIdfVector() {
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
                BigDecimal tf = BigDecimal.valueOf(entry.getValue()).divide(BigDecimal.valueOf(words.length), MathContext.DECIMAL128);   // Term frequency
                BigDecimal idf = BigDecimal.valueOf(calculateIDF(term));                                                                 // Inverse Document Frequency
                tfIdfVector.put(term, tf.multiply(idf));
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
             * This function needs alot of time and calculating that is near imposible for me.
             * You can replace this with the actual document frequency calculation.
             * But I will leave it blank because my computer can't handle it.
             */
            return 100;
        }
    }
}

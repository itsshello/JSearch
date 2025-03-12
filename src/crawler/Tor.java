package crawler;

import dataset.DatasetModule;
import dataset.Settings;
import dataset.WriteToFile;
import indexer.CustomIndexer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.Proxy;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tor {
    private static Set<String> visitedUrls = new HashSet<>();
    private static Set<String> urlsToCrawl = new HashSet<>();
    private static CustomIndexer indexer = new CustomIndexer();
    private static int CRAWL_LIMIT = Integer.parseInt(Settings.getSetting("CRAWL_LIMIT"));

    // valid URLs
    private static final Pattern URL_PATTERN = Pattern.compile("http[s]?://[a-zA-Z0-9.-]+(?:/[a-zA-Z0-9/-]*)?");
    private static final Pattern ONION_URL_PATTERN = Pattern.compile("^https?:\\/\\/([a-z2-7]{16,56})\\.onion(?:\\/|$)"); // Onion URLs

    public static final String torProxyHost = "127.0.0.1"; // Tor SOCKS5 proxy host
    public static final int torProxyPort = 9150;

    private static final String CRAWL_LOGS_PATH = Settings.getSetting("CRAWL_LOGS_PATH");

    public static void crawl() {
        Set<String> seedUrls = DatasetModule.getSeedUrls();
        visitedUrls.addAll(DatasetModule.getVisitedUrls());     // Initialize visited URLs
        urlsToCrawl.addAll(seedUrls);                           // Add seed URLs

        int crawledCount = 0;

        WriteToFile.writeLog(CRAWL_LOGS_PATH, "[START] Starting Crawler. . . . ");
        while (!urlsToCrawl.isEmpty() && crawledCount < CRAWL_LIMIT) {
            String url = urlsToCrawl.iterator().next(); // get the first URL
            urlsToCrawl.remove(url);                    // Remove it from the list

            if (visitedUrls.contains(url)) {
                continue; // Skip if already visited
            }

            System.out.println("Crawling URL: " + url);
            WriteToFile.writeLog(CRAWL_LOGS_PATH, "Crawling URL: " + url);

            try {
                Document doc;

                if (isOnionUrl(url)) {
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
                    URL onionUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) onionUrl.openConnection(proxy);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.setConnectTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        System.err.println("Failed to fetch: " + url + " Response Code: " + responseCode);
                    }

                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine).append("\n");
                        }
                        doc = Jsoup.parse(response.toString(), url);
                    }
                } else {
                    Connection connection = Jsoup.connect(url);
                    connection.followRedirects(true);
                    doc = connection.get();
                };
                

                if (!robotsAllowed(url)) {
                    System.out.println("Skipping URL due to robots.txt: " + url);
                    WriteToFile.writeLog(CRAWL_LOGS_PATH, "[SKIP] [robots.txt] " + url);
                    continue;
                }

                String title = doc.title();
                String body = doc.body().text();

                indexer.indexData(url, title, body);

                Elements allLinks = doc.select("*[src], *[href], *[data-src]");
                for (Element element : allLinks) {
                    String linkUrl = element.attr("abs:href");
                    if (linkUrl.isEmpty()) {
                        linkUrl = element.attr("abs:src");
                    }

                    // handle relative paths and make them absolute
                    if (linkUrl.startsWith("/")) {
                        linkUrl = doc.baseUri() + linkUrl;
                    }

                    // add the link if it matches the pattern and hasn't been visited
                    if (isValidUrl(linkUrl) && !visitedUrls.contains(linkUrl) && !urlsToCrawl.contains(linkUrl)) {
                        urlsToCrawl.add(linkUrl);
                    }
                }

                visitedUrls.add(url);
                crawledCount++;

            } catch (IOException e) {
                System.err.println("Error fetching URL: " + url);
                WriteToFile.writeLog(CRAWL_LOGS_PATH, "[ERROR] [FETCH] " + url + " [] Error: " + e);
            }
        }

        System.out.println("Crawling complete. Crawled " + crawledCount + " URLs.");
    }

    public static void setLimit(int limit) {
        CRAWL_LIMIT = limit;
    }
    public static boolean isValidUrl(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }

    public static boolean isOnionUrl(String url) {
        Matcher matcher = ONION_URL_PATTERN.matcher(url);
        return matcher.matches();
    }

    public static boolean robotsAllowed(String url) {
        /**
         * This is a placeholder for a more advanced robots.txt handling
         * You can add a check here to see if the URL is allowed to be crawled
         * based on the robots.txt file of the website
         */
        return true;
    }
}

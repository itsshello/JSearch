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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomCrawler {
    private static Set<String> visitedUrls = new HashSet<>();
    private static Set<String> urlsToCrawl = new HashSet<>();
    private static CustomIndexer indexer = new CustomIndexer();
    private static int CRAWL_LIMIT = Integer.parseInt(Settings.getSetting("CRAWL_LIMIT"));

    // valid URLs
    private static final Pattern URL_PATTERN = Pattern.compile("http[s]?://[a-zA-Z0-9.-]+(?:/[a-zA-Z0-9/-]*)?");
    
    private static final Pattern WIKIPEDIA_URL_PATTERN = Pattern.compile("https?://[a-zA-Z]+\\.wikipedia\\.org/.*"); // Wikipedia URLs
    private static final Pattern ENGLISH_URL_PATTERN = Pattern.compile("https?://en\\.wikipedia\\.org/.*");          // English Wikipedia URLs

    private static final String CRAWL_LOGS_PATH = Settings.getSetting("CRAWL_LOGS_PATH");
    private static boolean CRAWL_ENG_WIKI_ONLY = Settings.getSetting("CRAWL_ENG_WIKI_ONLY") == "yes";

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

            if (isWikipediaUrl(url) && !isEnglishUrl(url) && CRAWL_ENG_WIKI_ONLY) {
                System.out.println("Skipping non-English Wikipedia URL: " + url);
                WriteToFile.writeLog(CRAWL_LOGS_PATH, "[SKIP] Non-English Wikipedia: " + url);
                continue; // Skip non-English Wikipedia URLs
            }

            System.out.println("Crawling URL: " + url);
            WriteToFile.writeLog(CRAWL_LOGS_PATH, "Crawling URL: " + url);

            try {
                Connection connection = Jsoup.connect(url);
                connection.followRedirects(true);
                Document doc = connection.get();

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
                WriteToFile.writeLog(CRAWL_LOGS_PATH, "[ERROR] [FETCH] " + url);
            }
        }

        System.out.println("Crawling complete. Crawled " + crawledCount + " URLs.");
    }

    public static void setLimit(int limit) {
        CRAWL_LIMIT = limit;
    }
    public static void setEngOnlyWIKI(boolean engOnly) {
        CRAWL_ENG_WIKI_ONLY = engOnly;
    }

    public static boolean isValidUrl(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }

    public static boolean isWikipediaUrl(String url) {
        Matcher matcher = WIKIPEDIA_URL_PATTERN.matcher(url);
        return matcher.matches();
    }

    // English Wikipedia URL*
    public static boolean isEnglishUrl(String url) {
        Matcher matcher = ENGLISH_URL_PATTERN.matcher(url);
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

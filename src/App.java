import crawler.CustomCrawler;

import dataset.Settings;
import dataset.DatasetModule;
import dataset.BrowseData;

public class App {
    public static void main(String[] args) {
        Settings.init();
        paseArgs(args);
    }
    public static void paseArgs(String[] args) {
        System.out.println("Starting JSearch ...");
        if (args.length == 0) {
            displayHelp();
            return;
        }

        String flag = args[0];
        switch (flag) {
            case "-c":
            case "--crawl":
                handleCrawl(args);
                break;
            case "-s":
            case "--search":
                handleSearch(args);
                break;
            case "-se":
            case "--settings":
                handleSettings(args);
                break;
            case "-h":
            case "--help":
                displayHelp();
                break;
            default:
                System.out.println("Invalid flag. Use -h or --help for usage information.");
                break;
        }
    }

    private static void displayHelp() {
        System.out.println("""
            Usage: [EXE] [FLAG] [OPTION(s)]
           
            Flags:
                -c, --crawl          Crawl the web
                -s, --search         Search the database
                -se, --settings      Display settings or Edit settings
                -h, --help           Display this help message
            
            Options:
                 [Note: this two are complitely optional, if not set it will use the default values for the top value and it will ask the user for the query]
                -q, --query          Search query [If Flag is -s, --search] [optional if -t, --top is set]
                -t, --top            Number of top results to display [If Flag is -s, --search]

                -l, --limit          Crawl limit [If Flag is -c, --crawl]
                -eg, --engwiki       English Wikipedia only [If Flag is -c, --crawl]
                -neg, --nengwiki     English Wikipedia only = False [If Flag is -c, --crawl]
                -sd, --seed          Seed URLs [If Flag is -c, --crawl] [must be comma-separated]

                -sh, --show          Show settings [If Flag is -se, --settings]
                -dp, --dbpath        Edit path to the database [If Flag is -se, --settings]
                -lp, --logspath      Edit path to the logs [If Flag is -se, --settings]
                -st, --searchtop     Edit number of top results [If Flag is -se, --settings]
                -cm, --crawlmax      Edit maximum crawl limit [If Flag is -se, --settings]
                -ew, --engwiki       Edit English Wikipedia only [If Flag is -se, --settings][yes/no]
        """);
    }

    private static void handleCrawl(String[] args) {
        int limit = Integer.parseInt(Settings.getSetting("CRAWL_LIMIT"));
        boolean engWikiOnly = (Settings.getSetting("CRAWL_ENG_WIKI_ONLY") == "yes" ? true : false); // default to English Wikipedia only
        String[] seedUrls = {};

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-l":
                case "--limit":
                    try {
                        limit = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid limit. Using default value: " + String.valueOf(limit));
                    }
                    break;
                case "-eg":
                case "--engwiki":
                    engWikiOnly = true;
                    break;
                case "-neg":
                case "--nengwiki":
                    engWikiOnly = false;
                    break;
                case "-sd":
                case "--seed":
                    seedUrls = args[++i].split(",");
                    break;
                default:
                    System.out.println("Invalid option for crawl. Use -h or --help for usage information.");
                    return;
            }
        }

        DatasetModule.addSeedUrl(seedUrls);
        System.out.println(
            "\n\nCrawling with limit: " + limit + ", \n" + 
            "English Wikipedia only: " + engWikiOnly + ", \n" + 
            "Seed URLs: " + String.join(", ", seedUrls));

        System.out.println("\n\n\nStarting Web Crawler ...");
        CustomCrawler.setLimit(limit);
        CustomCrawler.setEngOnlyWIKI(engWikiOnly);
        CustomCrawler.crawl();
    }

    private static void handleSearch(String[] args) {
        String query = "";
        int topResults = Integer.parseInt(Settings.getSetting("TOP_SEARCH_COUNT")); // default top results

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-q":
                case "--query":
                    query = args[++i];
                    break;
                case "-t":
                case "--top":
                    topResults = Integer.parseInt(args[++i]);
                    break;
                default:
                    System.out.println("Using the default max results, "+ String.valueOf(topResults));
                    System.out.println("Starting JSearch .. .\n [Calculating the best matches, could take few seconds depending on the size of the database and the computer speed]");
                    BrowseData.Search();
                    return;
            }
        }

        if (query == "") {
            System.out.println("Starting JSearch .. .\n [Calculating the best matches, could take few seconds depending on the size of the database and the computer speed]");
            BrowseData.Search(topResults);
            return;
        }

        System.out.println("Starting JSearch .. .\n [Calculating the best matches, could take few seconds depending on the size of the database and the computer speed]");
        BrowseData.setMAXResults(topResults);
        BrowseData.searchAndPrintBestMatches(query);
    }

    private static void handleSettings(String[] args) {
        if (args.length == 1) {
            displaySettings();
            return;
        }

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-sh":
                case "--show":
                    displaySettings();
                    break;
                case "-dp":
                case "--dbpath":
                    String dbPath = args[++i];
                    String pastDBpath = Settings.getSetting("CRAWL_DATA_PATH");
                    Settings.updateSetting("CRAWL_DATA_PATH", dbPath);
                    System.out.println("Database path updated from " + pastDBpath + " to: " + dbPath);
                    break;
                case "-lp":
                case "--logspath":
                    String logsPath = args[++i];
                    String pastLogpath = Settings.getSetting("CRAWL_LOGS_PATH");
                    Settings.updateSetting("CRAWL_LOGS_PATH", logsPath);
                    System.out.println("Logs path updated from " + pastLogpath + " to: " + logsPath);
                    break;
                case "-st":
                case "--searchtop":
                    String searchTop = args[++i];
                    String pastTopResultcount = Settings.getSetting("TOP_SEARCH_COUNT");
                    Settings.updateSetting("TOP_SEARCH_COUNT", searchTop);
                    System.out.println("Number of top results updated to: " + searchTop + " from: " + pastTopResultcount);
                    break;
                case "-cm":
                case "--crawlmax":
                    String crawlMax = args[++i];
                    String crawlmaxlimit = Settings.getSetting("CRAWL_LIMIT");
                    Settings.updateSetting("CRAWL_LIMIT", crawlMax);
                    System.out.println("Maximum crawl limit updated to: " + crawlMax + " from: " + crawlmaxlimit);
                    break;
                case "-ew":
                case "--engwiki":
                    String engWiki = args[++i];
                    String pastengWiki = Settings.getSetting("CRAWL_ENG_WIKI_ONLY");
                    Settings.updateSetting("CRAWL_ENG_WIKI_ONLY", engWiki);
                    System.out.println("English Wikipedia only setting updated to: " + engWiki + " from: " + pastengWiki);
                    System.out.println("Please note that this setting is case sensitive and should be 'yes' for true and 'no' for false but anything else then 'yes' will be considered as false");
                    break;
                default:
                    System.out.println("Invalid option for settings. Use -h or --help for usage information.");
                    return;
            }
        }
    }

    private static void displaySettings() {
        System.out.println("Displaying current settings... \n \n");
        System.out.println("All Settings:\n");
        System.out.println("CRAWL_DATA_PATH     :   " + Settings.getSetting("CRAWL_DATA_PATH"));
        System.out.println("CRAWL_LOGS_PATH     :   " + Settings.getSetting("CRAWL_LOGS_PATH"));
        System.out.println("CRAWL_LIMIT         :   " + Settings.getSetting("CRAWL_LIMIT"));
        System.out.println("CRAWL_ENG_WIKI_ONLY :   " + Settings.getSetting("CRAWL_ENG_WIKI_ONLY"));
        System.out.println("TOP_SEARCH_COUNT    :   " + Settings.getSetting("TOP_SEARCH_COUNT"));
    }
}
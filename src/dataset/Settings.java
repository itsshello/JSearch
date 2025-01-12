package dataset;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;


public class Settings {

    private static final String SETTINGS_FILE = "settings.properties";
    private static Properties settings;

    /**
     * Updates a setting with a new value and saves to file
     * @param key Setting key
     * @param value New value
     */
    public static void updateSetting(String key, String value) {
        if (settings == null) {
            loadSettings();
        }
        settings.setProperty(key, value);
        saveSettings();
    }

    /**
     * Saves current settings to the properties file
     */
    public static void saveSettings() {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            if (settings == null) {
                settings = new Properties();
            }
            settings.store(writer, "JSearch Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads settings from the properties file
     * Creates a new Properties object if file doesn't exist
     */
    public static void loadSettings() {
        settings = new Properties();
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                settings.load(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public Settings() {
        if (settings == null) {
            loadSettings();
        }
    }

    public static void init() {
        /**
         * Setting the defult values for the settings
         */
        if (getSetting("CRAWL_DATA_PATH") == null) updateSetting("CRAWL_DATA_PATH", "data/crawl_data.db");
        if (getSetting("CRAWL_LOGS_PATH") == null) updateSetting("CRAWL_LOGS_PATH", "data/crawl_logs.txt");
        if (getSetting("CRAWL_LIMIT") == null) updateSetting("CRAWL_LIMIT", "100");
        if (getSetting("CRAWL_ENG_WIKI_ONLY") == null) updateSetting("CRAWL_ENG_WIKI_ONLY", "yes");
        if (getSetting("TOP_SEARCH_COUNT") == null) updateSetting("TOP_SEARCH_COUNT", "5");
    }

    public static String getSetting(String key) {
        if (settings == null) {
            loadSettings();
        }
        return settings.getProperty(key);
    }
}

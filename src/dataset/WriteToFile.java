package dataset;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

public class WriteToFile {

    public static void AddLine(String filePath, String content) {
        createFileIfNotExists(filePath);
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(content + System.lineSeparator());
        } catch (IOException e) { 
            System.err.println("[FILE WRITE ERROR]: " + e.getMessage());
        }
    }

    public static void writeLog(String filePath, String content) {
        createFileIfNotExists(filePath);
        String timestamp = getCurrentTimestamp();
        String logEntry = "[" + timestamp + "] " + content;

        try (FileWriter writer = new FileWriter(filePath, true)) { // Append mode
            writer.write(logEntry + System.lineSeparator());              // Add new line
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the log file: " + e.getMessage());
        }
    }

    private static String getCurrentTimestamp() {
        // Format: YYYY-MM-DD hh:mm:ss.SSS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
    }

    private static void createFileIfNotExists(String filePath) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while creating the file: " + e.getMessage());
        }
    }
}

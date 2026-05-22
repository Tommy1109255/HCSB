//将字符串内容以日志的形式，追加写入到本地的一个文本文件中。
package com.hcbs.service;

import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FilePersistenceService {

    private static final String LOG_FILE = "data/booking_logs.txt";

    public FilePersistenceService() {
        // Ensure the data directory exists
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    /**
     * Appends a record to a local text file.
     * @param content The text to save
     */
    public void saveToFile(String content) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String entry = "[" + timestamp + "] " + content + System.lineSeparator();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}

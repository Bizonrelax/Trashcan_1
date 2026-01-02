package com.sqtext.history;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final String HISTORY_FILE = "sqtext_history.txt";
    private static final int MAX_ENTRIES = 100;
    
    public static class HistoryEntry {
        private LocalDateTime timestamp;
        private String operation; // "COMPRESS" или "DECOMPRESS"
        private int originalSize;
        private int resultSize;
        private double compressionRatio;
        private long timeMs;
        
        public HistoryEntry(String operation, int originalSize, int resultSize, long timeMs) {
            this.timestamp = LocalDateTime.now();
            this.operation = operation;
            this.originalSize = originalSize;
            this.resultSize = resultSize;
            this.timeMs = timeMs;
            this.compressionRatio = (double) resultSize / originalSize;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %d → %d байт (коэфф.: %.2f) за %d мс",
                timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")),
                operation.equals("COMPRESS") ? "Сжатие" : "Распаковка",
                originalSize, resultSize, compressionRatio, timeMs);
        }
        
        // Геттеры
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getOperation() { return operation; }
        public int getOriginalSize() { return originalSize; }
        public int getResultSize() { return resultSize; }
        public double getCompressionRatio() { return compressionRatio; }
        public long getTimeMs() { return timeMs; }
    }
    
    /**
     * Добавляет запись в историю
     */
    public static void addEntry(HistoryEntry entry) {
        try {
            List<String> lines = new ArrayList<>();
            Path path = Paths.get(HISTORY_FILE);
            
            // Читаем существующие записи, если файл есть
            if (Files.exists(path)) {
                lines = Files.readAllLines(path);
            }
            
            // Добавляем новую запись в начало
            lines.add(0, entry.toString());
            
            // Ограничиваем количество записей
            if (lines.size() > MAX_ENTRIES) {
                lines = lines.subList(0, MAX_ENTRIES);
            }
            
            // Сохраняем
            Files.write(path, lines);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения истории: " + e.getMessage());
        }
    }
    
    /**
     * Читает историю операций
     */
    public static List<String> getHistory() {
        try {
            Path path = Paths.get(HISTORY_FILE);
            if (Files.exists(path)) {
                return Files.readAllLines(path);
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения истории: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    /**
     * Очищает историю
     */
    public static void clearHistory() {
        try {
            Files.deleteIfExists(Paths.get(HISTORY_FILE));
        } catch (IOException e) {
            System.err.println("Ошибка удаления истории: " + e.getMessage());
        }
    }
}
package com.sqtext.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;


public class ChatCompressor {
    
    /**
     * Сжимает текст в формат, оптимизированный для чат-ботов
     */
    public static String compress(String text) {
    try {
        // Сжимаем текст
        byte[] gzipData;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(text.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            gzipData = baos.toByteArray();
        }
        
        String base64Data = Base64.getEncoder().encodeToString(gzipData);
        
        // Формируем JSON вручную
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"sqtext_format\": \"chat_v1\",\n");
        jsonBuilder.append("  \"compression\": \"gzip+base64\",\n");
        jsonBuilder.append("  \"original_size\": ").append(text.length()).append(",\n");
        jsonBuilder.append("  \"compressed_size\": ").append(base64Data.length()).append(",\n");
        jsonBuilder.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
        jsonBuilder.append("  \"data\": \"").append(base64Data).append("\"\n");
        jsonBuilder.append("}");
        
        // Красивый вывод с инструкциями
        String result = "// ===== SQText COMPRESSED DATA =====\n" +
                       "// INSTRUCTION FOR AI:\n" +
                       "// 1. This is Base64+GZIP compressed JSON\n" +
                       "// 2. Decode Base64, then decompress GZIP\n" +
                       "// 3. Read the JSON below\n" +
                       "// ==================================\n\n" +
                       jsonBuilder.toString();
        
        return result;
        
    } catch (Exception e) {
        return "Ошибка при сжатии: " + e.getMessage();
    }
}
    
    /**
     * Проверяет, является ли текст форматом для чата
     */
    public static boolean isChatFormat(String text) {
        return text.contains("SQText COMPRESSED DATA") && 
               text.contains("INSTRUCTION FOR AI");
    }
    
    /**
     * Извлекает данные из чат-формата
     */
    public static String extractFromChat(String chatText) throws IOException {
        // Находим начало JSON
        int start = chatText.indexOf("{");
        if (start == -1) {
            throw new IOException("Не найден JSON в чат-формате");
        }
        
        String jsonPart = chatText.substring(start);
        
        // Ищем данные
        int dataStart = jsonPart.indexOf("\"data\":\"");
        if (dataStart == -1) {
            throw new IOException("Не найден 'data' в JSON");
        }
        
        dataStart += 8;
        int dataEnd = jsonPart.indexOf("\"", dataStart);
        if (dataEnd == -1) {
            throw new IOException("Не найден конец 'data'");
        }
        
        String compressedData = jsonPart.substring(dataStart, dataEnd);
        
        // Распаковываем
        return GZipCompressor.decompress(compressedData);
    }
}
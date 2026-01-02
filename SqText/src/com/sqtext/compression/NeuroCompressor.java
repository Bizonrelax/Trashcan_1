package com.sqtext.compression;

import java.io.IOException;

public class NeuroCompressor {
    
    /**
     * Сжимает текст в формат, понятный нейросети (JSON с метаданными)
     */
    public static String compressForNeuro(String text, int depth, String algorithm) throws IOException {
        String compressedData;
        
        if ("GZIP".equals(algorithm)) {
            compressedData = GZipCompressor.compress(text);
        } else if ("Рекурсивный GZIP".equals(algorithm)) {
            compressedData = GZipCompressor.compressRecursive(text, depth);
        } else {
            compressedData = GZipCompressor.compress(text);
        }
        
        // Создаем JSON вручную
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"format\": \"sqz\",\n");
        json.append("    \"version\": \"1.0\",\n");
        json.append("    \"compressed\": true,\n");
        json.append("    \"algorithm\": \"").append(algorithm).append("\",\n");
        json.append("    \"depth\": ").append(depth).append(",\n");
        json.append("    \"original_length\": ").append(text.length()).append(",\n");
        json.append("    \"compressed_length\": ").append(compressedData.length()).append(",\n");
        json.append("    \"timestamp\": ").append(System.currentTimeMillis()).append("\n");
        json.append("  },\n");
        json.append("  \"data\": \"").append(compressedData).append("\"\n");
        json.append("}");
        
        // Добавляем инструкцию для нейросети в комментарий
        String result = "// SqText JSON Compressed Format v1.0\n";
        result += "// Для нейросети: Base64+GZIP compressed data in JSON\n";
        result += json.toString();
        
        return result;
    }
    
    /**
     * Определяет, является ли текст JSON форматом для нейросети
     */
    public static boolean isNeuroFormat(String text) {
        return text.contains("\"format\":\"sqz\"") && 
               text.contains("\"compressed\":true");
    }
    
    /**
     * Извлекает и распаковывает данные из JSON формата нейросети
     */
    public static String decompressFromNeuro(String neuroText) throws IOException {
        try {
            // Находим начало JSON (после комментариев)
            int jsonStart = neuroText.indexOf("{");
            if (jsonStart == -1) {
                throw new IOException("Не найден JSON в тексте");
            }
            
            String jsonPart = neuroText.substring(jsonStart);
            
            // Парсим JSON вручную (упрощенно)
            int dataStart = jsonPart.indexOf("\"data\":\"");
            if (dataStart == -1) {
                throw new IOException("Не найден 'data' в JSON");
            }
            
            dataStart += 8; // Длина "\"data\":\""
            int dataEnd = jsonPart.indexOf("\"", dataStart);
            if (dataEnd == -1) {
                throw new IOException("Не найден конец 'data' в JSON");
            }
            
            String compressedData = jsonPart.substring(dataStart, dataEnd);
            
            // Ищем algorithm и depth
            String algorithm = "GZIP";
            int depth = 1;
            
            int algoStart = jsonPart.indexOf("\"algorithm\":\"");
            if (algoStart != -1) {
                algoStart += 13;
                int algoEnd = jsonPart.indexOf("\"", algoStart);
                algorithm = jsonPart.substring(algoStart, algoEnd);
            }
            
            int depthStart = jsonPart.indexOf("\"depth\":");
            if (depthStart != -1) {
                depthStart += 8;
                int depthEnd = jsonPart.indexOf(",", depthStart);
                if (depthEnd == -1) depthEnd = jsonPart.indexOf("}", depthStart);
                String depthStr = jsonPart.substring(depthStart, depthEnd).trim();
                depth = Integer.parseInt(depthStr);
            }
            
            if ("GZIP".equals(algorithm)) {
                return GZipCompressor.decompress(compressedData);
            } else if ("Рекурсивный GZIP".equals(algorithm)) {
                return GZipCompressor.decompressRecursive(compressedData, depth);
            } else {
                return GZipCompressor.decompress(compressedData);
            }
            
        } catch (Exception e) {
            throw new IOException("Ошибка обработки JSON формата нейросети: " + e.getMessage());
        }
    }
}
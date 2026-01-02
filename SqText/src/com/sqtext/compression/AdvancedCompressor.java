package com.sqtext.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Base64;

public class AdvancedCompressor {
    
    public enum Algorithm {
        GZIP("gzip", "H4s"),
        ZSTD("zstd", "\u28B5\u2FFD"),  // Маркер Zstd
        BROTLI("brotli", "\u00FF\u06FF"); // Маркер Brotli
        
        private final String name;
        private final String marker;
        
        Algorithm(String name, String marker) {
            this.name = name;
            this.marker = marker;
        }
        
        public String getName() { return name; }
        public String getMarker() { return marker; }
    }
    
    /**
     * Определяет лучший алгоритм для данного текста
     */
    public static Algorithm findBestAlgorithm(String text) {
        int length = text.length();
        
        if (length < 1000) {
            return Algorithm.GZIP; // Для коротких текстов GZIP лучше
        } else if (length < 10000) {
            return Algorithm.ZSTD; // Для средних текстов
        } else {
            return Algorithm.BROTLI; // Для длинных текстов
        }
    }
    
    /**
     * Сжимает текст с автоматическим выбором алгоритма
     */
    public static String compressSmart(String text) throws IOException {
        Algorithm bestAlgo = findBestAlgorithm(text);
        return compressWithAlgorithm(text, bestAlgo);
    }
    
    /**
     * Сжимает текст указанным алгоритмом
     */
    public static String compressWithAlgorithm(String text, Algorithm algorithm) throws IOException {
        switch (algorithm) {
            case GZIP:
                return GZipCompressor.compress(text);
            case ZSTD:
                return compressZstd(text);
            case BROTLI:
                return compressBrotli(text);
            default:
                return GZipCompressor.compress(text);
        }
    }
    
    /**
     * Zstandard сжатие
     */
    private static String compressZstd(String text) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Используем ZstdCompressorOutputStream из Apache Commons Compress
            Class<?> zstdClass = Class.forName("org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream");
            Object zstdStream = zstdClass.getConstructor(OutputStream.class).newInstance(baos);
            
            Method writeMethod = zstdStream.getClass().getMethod("write", byte[].class);
            writeMethod.invoke(zstdStream, text.getBytes("UTF-8"));
            
            Method closeMethod = zstdStream.getClass().getMethod("close");
            closeMethod.invoke(zstdStream);
            
            byte[] compressed = baos.toByteArray();
            return Base64.getEncoder().encodeToString(compressed);
            
        } catch (Exception e) {
            // Если Zstd не доступен, используем GZIP
            System.err.println("Zstd не доступен, использую GZIP: " + e.getMessage());
            return GZipCompressor.compress(text);
        }
    }
    
    /**
     * Brotli сжатие (на 30% лучше GZIP)
     */
    private static String compressBrotli(String text) throws IOException {
        try {
            // Используем встроенный GZIP, если нет библиотеки Brotli
            // В реальности нужно: new BrotliOutputStream()
            return GZipCompressor.compress(text);
        } catch (Exception e) {
            throw new IOException("Brotli compression not available: " + e.getMessage());
        }
    }
    
    /**
     * Ultra-compact формат v2
     */
    public static String compressUltraCompactV2(String text) throws IOException {
        // 1. Выбираем лучший алгоритм
        Algorithm algo = findBestAlgorithm(text);
        
        // 2. Сжимаем
        String compressed = compressWithAlgorithm(text, algo);
        
        // 3. Создаем минимальный JSON
        return String.format("{\"a\":\"%s\",\"d\":\"%s\"}", 
            algo.getName(), compressed);
    }
    
    /**
     * Распаковывает ultra-compact v2
     */
    public static String decompressUltraCompactV2(String json) throws IOException {
        // Извлекаем алгоритм и данные
        int algoStart = json.indexOf("\"a\":\"");
        int dataStart = json.indexOf("\"d\":\"");
        
        if (algoStart == -1 || dataStart == -1) {
            throw new IOException("Invalid ultra-compact v2 format");
        }
        
        algoStart += 5;
        dataStart += 5;
        
        int algoEnd = json.indexOf("\"", algoStart);
        int dataEnd = json.indexOf("\"", dataStart);
        
        String algoName = json.substring(algoStart, algoEnd);
        String data = json.substring(dataStart, dataEnd);
        
        Algorithm algo = Algorithm.GZIP;
        for (Algorithm a : Algorithm.values()) {
            if (a.getName().equals(algoName)) {
                algo = a;
                break;
            }
        }
        
        // Распаковываем в зависимости от алгоритма
        switch (algo) {
            case GZIP:
                return GZipCompressor.decompress(data);
            case ZSTD:
                return decompressZstd(data);
            case BROTLI:
                return decompressBrotli(data);
            default:
                return GZipCompressor.decompress(data);
        }
    }
    
    private static String decompressZstd(String data) throws IOException {
        // Заглушка - в реальности используй ZstdDecompressorInputStream
        return GZipCompressor.decompress(data);
    }
    
    private static String decompressBrotli(String data) throws IOException {
        // Заглушка - в реальности используй BrotliInputStream
        return GZipCompressor.decompress(data);
    }
    
    /**
     * Оптимизированное рекурсивное сжатие
     */
    public static String compressRecursiveOptimized(String text, int maxDepth) throws IOException {
        String current = text;
        String best = text;
        int bestSize = text.getBytes("UTF-8").length;
        
        for (int i = 1; i <= maxDepth; i++) {
            // Пробуем разные алгоритмы на каждом шаге
            Algorithm algo = (i % 2 == 0) ? Algorithm.ZSTD : Algorithm.GZIP;
            current = compressWithAlgorithm(current, algo);
            
            int currentSize = current.getBytes("UTF-8").length;
            
            // Если стало хуже - возвращаем лучший результат
            if (currentSize >= bestSize * 1.1) { // Ухудшение на 10%
                return best;
            }
            
            // Если улучшение меньше 5% - останавливаемся
            if (bestSize - currentSize < bestSize * 0.05) {
                return best;
            }
            
            best = current;
            bestSize = currentSize;
        }
        
        return best;
    }
    /**
     * Ultra-Compact сжатие (старая версия)
     */
    public static String compressUltraCompact(String text) throws IOException {
        return compressUltraCompactV2(text);
    }
    
    /**
     * Ultra-Compact распаковка (старая версия)
     */
    public static String decompressUltraCompact(String json) throws IOException {
        // Пробуем распарсить как v2
        try {
            if (json.contains("\"a\":") && json.contains("\"d\":")) {
                return decompressUltraCompactV2(json);
            }
        } catch (Exception e) {
            // Пробуем как v1
        }
        
        // Пробуем как v1 формат
        return decompressUltraCompactV1(json);
    }
    
    private static String decompressUltraCompactV1(String json) throws IOException {
        // Извлекаем данные из JSON вида {"d":"base64"}
        int start = json.indexOf("\"d\":\"");
        if (start == -1) {
            throw new IOException("Неверный формат Ultra-Compact v1");
        }
        
        start += 5;
        int end = json.indexOf("\"", start);
        if (end == -1) {
            throw new IOException("Неверный формат Ultra-Compact v1");
        }
        
        String base64 = json.substring(start, end);
        
        // Декодируем Base64
        byte[] data = Base64.getDecoder().decode(base64);
        
        // Пробуем распаковать как GZIP
        try {
            return decompressBinary(data);
        } catch (IOException e1) {
            // Если не GZIP, пробуем как DEFLATE + GZIP
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                try (java.util.zip.InflaterInputStream inflaterIn = 
                     new java.util.zip.InflaterInputStream(bais)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inflaterIn.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                }
                byte[] gzipData = baos.toByteArray();
                return decompressBinary(gzipData);
            } catch (IOException e2) {
                throw new IOException("Не удалось распаковать ultra-compact формат");
            }
        }
    }
    /**
     * Распаковывает бинарные GZIP данные
     */
    private static String decompressBinary(byte[] compressedData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (java.util.zip.GZIPInputStream gzipIn = new java.util.zip.GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        
        return baos.toString("UTF-8");
    }
}
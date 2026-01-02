package com.sqtext.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipCompressor {
    
    /**
     * Сжимает текст с помощью GZIP и кодирует в Base64
     */
    public static String compress(String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(text.getBytes("UTF-8"));
        }
        
        byte[] compressedBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(compressedBytes);
    }
    
    /**
     * Рекурсивно сжимает текст несколько раз
     */
   public static String compressRecursive(String text, int depth) throws IOException {
    if (depth <= 1) {
        return compress(text);
    }
    
    String result = text;
    for (int i = 0; i < depth; i++) {
        // Каждый следующий этап сжимает результат предыдущего
        result = compress(result);
        System.out.println("Этап " + (i+1) + ": " + result.length() + " символов");
    }
    return result;
}


    
    /**
     * Распаковывает текст, сжатый методом compress()
     */
    public static String decompress(String compressedText) throws IOException {
        if (compressedText == null || compressedText.isEmpty()) {
            return "";
        }
        try {
            // Очищаем текст от возможных лишних символов
            String cleanText = compressedText.trim()
                .replaceAll("\\s+", "")  // удаляем все пробелы и переносы
                .replaceAll("[^A-Za-z0-9+/=]", ""); // удаляем не-Base64 символы
            
            byte[] gzipData = Base64.getDecoder().decode(cleanText);
        byte[] compressedBytes = Base64.getDecoder().decode(compressedText);
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        
        return baos.toString("UTF-8");
        } catch (IllegalArgumentException e) {
            throw new IOException("Некорректные Base64 данные: " + e.getMessage(), e);
        }
    }
    
    /**
     * Рекурсивно распаковывает текст, сжатый несколько раз
     */
    public static String decompressRecursive(String compressedText, int depth) throws IOException {
        if (depth <= 1) {
            return decompress(compressedText);
        }
        
        String result = compressedText;
        for (int i = 0; i < depth; i++) {
            result = decompress(result);
        }
        return result;
    }
    
    /**
     * Проверяет, является ли текст сжатым GZIP+Base64
     */
    public static boolean isCompressed(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            // Проверяем, что это валидный Base64
            byte[] bytes = Base64.getDecoder().decode(text);
            
            // Пытаемся распаковать как GZIP
            try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
                byte[] buffer = new byte[1];
                gzipIn.read(buffer);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            // Не валидный Base64
            return false;
        }
    }
    public static byte[] compressToBytes(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return baos.toByteArray();
    }
}
package com.sqtext.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileUtils {
    
    /**
     * Открывает диалог выбора файла и читает его содержимое
     */
    public static String readFile(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл для загрузки");
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        // Фильтры для разных типов файлов
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
            "Текстовые файлы (*.txt)", "txt");
        FileNameExtensionFilter javaFilter = new FileNameExtensionFilter(
            "Java файлы (*.java)", "java");
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter(
            "JSON файлы (*.json)", "json");
        FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter(
            "XML файлы (*.xml)", "xml");
        FileNameExtensionFilter allFilter = new FileNameExtensionFilter(
            "Все файлы (*.*)", "*");
        FileNameExtensionFilter mdFilter = new FileNameExtensionFilter(  
        	    "Markdown файлы (*.md)", "md");                             
        
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(javaFilter);
        fileChooser.addChoosableFileFilter(jsonFilter);
        fileChooser.addChoosableFileFilter(xmlFilter);
        fileChooser.addChoosableFileFilter(mdFilter);
        fileChooser.addChoosableFileFilter(allFilter);
        fileChooser.setFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(mdFilter);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                
                // Пытаемся определить кодировку
                String content;
                if (isBinary(bytes)) {
                    // Для бинарных файлов используем Base64
                    content = java.util.Base64.getEncoder().encodeToString(bytes);
                    JOptionPane.showMessageDialog(parent,
                        "Файл распознан как бинарный. Данные закодированы в Base64.",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Для текстовых файлов - UTF-8
                    content = new String(bytes, StandardCharsets.UTF_8);
                }
                
                return content;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                    "Ошибка чтения файла: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }
    
    /**
     * Сохраняет текст в файл
     */
    public static boolean saveFile(JFrame parent, String content, boolean isCompressed) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить файл");
        
        if (isCompressed) {
            fileChooser.setSelectedFile(new File("compressed_text.sqz"));
        } else {
            fileChooser.setSelectedFile(new File("text.txt"));
        }
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Добавляем расширение, если его нет
            if (!file.getName().contains(".")) {
                if (isCompressed) {
                    file = new File(file.getAbsolutePath() + ".sqz");
                } else {
                    file = new File(file.getAbsolutePath() + ".txt");
                }
            }
            
            try {
                if (isCompressed && content.startsWith("H4sIAAAAAAAA")) {
                    // Для сжатых данных пишем как есть
                    Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
                } else {
                    // Для обычного текста
                    Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
                }
                
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                    "Ошибка сохранения файла: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }
    
    /**
     * Проверяет, является ли файл бинарным
     */
    private static boolean isBinary(byte[] data) {
        // Если первые 1024 байта содержат нулевой байт - считаем бинарным
        int length = Math.min(data.length, 1024);
        for (int i = 0; i < length; i++) {
            if (data[i] == 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Вставляет текст из буфера обмена
     */
    public static String pasteFromClipboard() {
        try {
            java.awt.datatransfer.Clipboard clipboard = 
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            return (String) clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
        } catch (Exception e) {
            return "";
        }
    }
}
package com.sqtext.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;

public class MainWindow extends JFrame {
    // Компоненты
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JLabel statsLabel;
    private JComboBox<String> algorithmCombo;
    private JSpinner depthSpinner;
    private JButton compressBtn, decompressBtn, copyBtn, clearBtn, historyBtn;
    
    public MainWindow() {
        setTitle("SqText - Умное сжатие текста");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        // Текстовые области
        inputArea = new JTextArea();
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        
        // Выбор алгоритма
        String[] algorithms = {"GZIP", "DEFLATE", "LZ4", "Рекурсивный GZIP"};
        algorithmCombo = new JComboBox<>(algorithms);
        
        // Глубина рекурсии
        depthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        
        // Кнопки
        compressBtn = new JButton("Сжать");
        decompressBtn = new JButton("Распаковать");
        copyBtn = new JButton("Копировать результат");
        clearBtn = new JButton("Очистить");
        historyBtn = new JButton("История");
        
        // Статистика
        statsLabel = new JLabel("Готов к работе");
        
        // Назначаем действия (заглушки - реализуем позже)
        compressBtn.addActionListener(e -> compressText());
        decompressBtn.addActionListener(e -> decompressText());
        copyBtn.addActionListener(e -> copyToClipboard());
        clearBtn.addActionListener(e -> clearAll());
        historyBtn.addActionListener(e -> showHistory());
    }
    
    private void layoutComponents() {
        // Основной контейнер
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель управления
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Алгоритм:"));
        controlPanel.add(algorithmCombo);
        controlPanel.add(new JLabel("Глубина:"));
        controlPanel.add(depthSpinner);
        controlPanel.add(compressBtn);
        controlPanel.add(decompressBtn);
        controlPanel.add(copyBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(historyBtn);
        
        // Панель ввода/вывода
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        JPanel inputPanel = createTextPanel("Исходный текст:", inputArea);
        JPanel outputPanel = createTextPanel("Результат:", outputArea);
        
        splitPane.setTopComponent(inputPanel);
        splitPane.setBottomComponent(outputPanel);
        
        // Собираем всё вместе
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statsLabel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createTextPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(0, 250));
        
        // Кнопки для этой панели
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadFileBtn = new JButton("Загрузить файл");
        JButton saveFileBtn = new JButton("Сохранить");
        JButton pasteBtn = new JButton("Вставить");
        
        buttonPanel.add(loadFileBtn);
        buttonPanel.add(saveFileBtn);
        buttonPanel.add(pasteBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Методы-заглушки (реализуем в следующих шагах)
    private void compressText() {
        statsLabel.setText("Сжатие... (реализуем позже)");
    }
    
    private void decompressText() {
        statsLabel.setText("Распаковка... (реализуем позже)");
    }
    
    private void copyToClipboard() {
        String text = outputArea.getText();
        if (!text.isEmpty()) {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            statsLabel.setText("Скопировано в буфер обмена");
        }
    }
    
    private void clearAll() {
        inputArea.setText("");
        outputArea.setText("");
        statsLabel.setText("Очищено");
    }
    
    private void showHistory() {
        JOptionPane.showMessageDialog(this, "История операций (реализуем позже)");
    }
    
    public static void main(String[] args) {
        // Запуск в потоке GUI
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
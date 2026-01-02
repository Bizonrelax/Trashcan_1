package com.sqtext.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.sqtext.compression.AdvancedCompressor;
import com.sqtext.compression.ChatCompressor;
import com.sqtext.compression.GZipCompressor;
import com.sqtext.compression.NeuroCompressor;
import com.sqtext.history.HistoryManager;
import com.sqtext.settings.FontSettingsDialog;
import com.sqtext.utils.FileUtils;


public class MainWindow extends JFrame {
    // Компоненты
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JLabel statsLabel;
    private JComboBox<String> algorithmCombo;
    private JSpinner depthSpinner;
    private JButton compressBtn, decompressBtn, copyBtn, clearBtn, historyBtn;
    private JProgressBar progressBar;
    
    public MainWindow() {
        setTitle("SqText - Умное сжатие текста");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        
        // Меню
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Настройки");
        JMenuItem fontSettingsItem = new JMenuItem("Шрифт и тема...");
        fontSettingsItem.addActionListener(e -> showFontSettings());
        settingsMenu.add(fontSettingsItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);
        
        // Добавляем слушатель для автоопределения
        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { autoDetect(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { autoDetect(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { autoDetect(); }
            private void autoDetect() {
                autoDetectAndProcess();
            }
        });
    }
    
   private void initComponents() {
    // Устанавливаем крупный шрифт для всех компонентов
    Font largeFont = new Font("Tahoma", Font.PLAIN, 16);
    Font monospacedFont = new Font("Courier New", Font.PLAIN, 16);
    
    // Текстовые области с увеличенным шрифтом
    inputArea = new JTextArea();
    inputArea.setFont(monospacedFont);
    inputArea.setLineWrap(true);
    inputArea.setWrapStyleWord(true);
    
    outputArea = new JTextArea();
    outputArea.setFont(monospacedFont);
    outputArea.setEditable(false);
    outputArea.setLineWrap(true);
    outputArea.setWrapStyleWord(true);
    
    // Выбор алгоритма с увеличенным шрифтом
    String[] algorithms = {
        "GZIP", 
        "Рекурсивный GZIP", 
        "Для нейросети (JSON)", 
        "Ultra-Compact",
        "Smart (автовыбор)",
        "Для чат-бота"
    };
    algorithmCombo = new JComboBox<>(algorithms);
    algorithmCombo.setFont(largeFont);
    
    // Глубина рекурсии с увеличенным шрифтом
    depthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    JComponent editor = depthSpinner.getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
        ((JSpinner.DefaultEditor) editor).getTextField().setFont(largeFont);
    }
    
    // Кнопки (сначала создаем, потом устанавливаем шрифт)
    compressBtn = new JButton("Сжать (Ctrl+Enter)");
    decompressBtn = new JButton("Распаковать (Ctrl+Shift+Enter)");
    copyBtn = new JButton("Копировать результат");
    clearBtn = new JButton("Очистить");
    historyBtn = new JButton("История");
    
    // Прогресс-бар
    progressBar = new JProgressBar(0, 100);
    progressBar.setVisible(false);
    progressBar.setStringPainted(true);
    
    // Статистика с увеличенным шрифтом
    statsLabel = new JLabel("Готов к работе");
    statsLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
    statsLabel.setForeground(new Color(0, 100, 0)); // Темно-зеленый
    
    // Теперь устанавливаем шрифты для кнопок
    Font buttonFont = new Font("Tahoma", Font.BOLD, 14);
    compressBtn.setFont(buttonFont);
    decompressBtn.setFont(buttonFont);
    copyBtn.setFont(buttonFont);
    clearBtn.setFont(buttonFont);
    historyBtn.setFont(buttonFont);
    
    // Назначаем горячие клавиши
    setupHotKeys();
    
    // Назначаем действия
    compressBtn.addActionListener(e -> compressText());
    decompressBtn.addActionListener(e -> decompressText());
    copyBtn.addActionListener(e -> copyToClipboard());
    clearBtn.addActionListener(e -> clearAll());
    historyBtn.addActionListener(e -> showHistory());
}
    
    private void setupHotKeys() {
        // Ctrl+Enter - сжать
        KeyStroke compressKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        inputArea.getInputMap().put(compressKey, "compressAction");
        inputArea.getActionMap().put("compressAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compressText();
            }
        });
        
        // Ctrl+Shift+Enter - распаковать
        KeyStroke decompressKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        inputArea.getInputMap().put(decompressKey, "decompressAction");
        inputArea.getActionMap().put("decompressAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decompressText();
            }
        });
        
        // Ctrl+L - очистить
        KeyStroke clearKey = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        inputArea.getInputMap().put(clearKey, "clearAction");
        inputArea.getActionMap().put("clearAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(progressBar, BorderLayout.SOUTH);
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
        
        // Желтый квадратик справки
        JButton helpButton = new JButton("?");
        helpButton.setBackground(Color.YELLOW);
        helpButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        helpButton.setToolTipText("<html><b>Горячие клавиши:</b><br>" +
            "• Ctrl+Enter - Сжать<br>" +
            "• Ctrl+Shift+Enter - Распаковать<br>" +
            "• Ctrl+L - Очистить<br>" +
            "• Ctrl+C - Копировать результат<br>" +
            "• Ctrl+V - Вставить в поле ввода</html>");
        helpButton.addActionListener(e -> showHelp());
        controlPanel.add(helpButton);
        
        // Панель ввода/вывода
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(350);
        
        JPanel inputPanel = createTextPanel("Исходный текст:", inputArea, true);
        JPanel outputPanel = createTextPanel("Результат:", outputArea, false);
        
        splitPane.setTopComponent(inputPanel);
        splitPane.setBottomComponent(outputPanel);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statsLabel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createTextPanel(String title, JTextArea textArea, boolean isInputPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 200), 2), 
            title,
            0, 0,
            new Font("Tahoma", Font.BOLD, 14),
            new Color(0, 0, 150)
        ));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadFileBtn = new JButton("[Загрузить] Загрузить файл");
        JButton saveFileBtn = new JButton("[Сохранить] Сохранить");
        JButton pasteBtn = new JButton("[Вставить] Вставить");
        
        Font panelButtonFont = new Font("Tahoma", Font.PLAIN, 13);
        loadFileBtn.setFont(panelButtonFont);
        saveFileBtn.setFont(panelButtonFont);
        pasteBtn.setFont(panelButtonFont);
        
        if (isInputPanel) {
            loadFileBtn.addActionListener(e -> loadFileToInput());
            pasteBtn.addActionListener(e -> pasteToInput());
            saveFileBtn.setEnabled(false);
        } else {
            loadFileBtn.setEnabled(false);
            pasteBtn.setEnabled(false);
            saveFileBtn.addActionListener(e -> saveOutputToFile());
        }
        
        buttonPanel.add(loadFileBtn);
        buttonPanel.add(saveFileBtn);
        buttonPanel.add(pasteBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Основные методы
    private void compressText() {
    	showProgress("Начинаем сжатие...", 10);
        String algorithm = (String) algorithmCombo.getSelectedItem();
        
        if ("Для нейросети (JSON)".equals(algorithm)) {
            compressForNeuro();
            return;
        }
        
        if ("Ultra-Compact".equals(algorithm)) {
            compressUltraCompact();
            return;
        }
        
        if ("Smart (автовыбор)".equals(algorithm)) {
            smartCompress();
            return;
            
        }
        if ("Для чат-бота".equals(algorithm)) {
            compressForChat();
            return;
        }
        
        // Обычное или рекурсивное GZIP
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            statsLabel.setText("⚠️ Введите текст для сжатия");
            statsLabel.setForeground(Color.ORANGE);
            return;
        }
        
        int depth = (Integer) depthSpinner.getValue();
        
        try {
            long startTime = System.currentTimeMillis();
            String compressed;
            int originalSize = text.getBytes("UTF-8").length;
            
            if ("GZIP".equals(algorithm)) {
                compressed = GZipCompressor.compress(text);
            } else if ("Рекурсивный GZIP".equals(algorithm)) {
                compressed = GZipCompressor.compressRecursive(text, depth);
            } else {
                compressed = GZipCompressor.compress(text);
            }
            
            long endTime = System.currentTimeMillis();
            int compressedSize = compressed.getBytes("UTF-8").length;
            double ratio = (double) compressedSize / originalSize;
            long time = endTime - startTime;
            
            outputArea.setText(compressed);
            
            String stats = String.format("✅ Сжато: %d → %d байт (коэфф.: %.2f) за %d мс", 
                originalSize, compressedSize, ratio, time);
            statsLabel.setText(stats);
            statsLabel.setForeground(new Color(0, 150, 0));
            
            // История
            HistoryManager.HistoryEntry entry = new HistoryManager.HistoryEntry(
                "COMPRESS", originalSize, compressedSize, time);
            HistoryManager.addEntry(entry);
            
        } catch (Exception e) {
            statsLabel.setText("❌ Ошибка сжатия: " + e.getMessage());
            statsLabel.setForeground(Color.RED);
            e.printStackTrace();
            showProgress("Ошибка сжатия", 100);
        }
        showProgress("Сжатие завершено", 100);
    }
    
    private void decompressText() {
        String text = inputArea.getText().trim();
     // Проверяем, это чат-формат?
        if (ChatCompressor.isChatFormat(text)) {
            try {
                String decompressed = ChatCompressor.extractFromChat(text);
                outputArea.setText(decompressed);
                statsLabel.setText("✅ Распакован чат-формат");
                statsLabel.setForeground(new Color(0, 150, 0));
                return;
            } catch (Exception e) {
                statsLabel.setText("❌ Ошибка распаковки чат-формата: " + e.getMessage());
                statsLabel.setForeground(Color.RED);
                return;
            }
        }
        if (text.isEmpty()) {
            statsLabel.setText("⚠️ Введите текст для распаковки");
            statsLabel.setForeground(Color.ORANGE);
            return;
        }
        
        // Ultra-Compact формат
        if ((text.startsWith("{\"d\":\"") && text.endsWith("\"}")) ||
            (text.startsWith("{\"a\":\"") && text.contains("\"d\":\""))) {
            try {
                String decompressed = AdvancedCompressor.decompressUltraCompact(text);
                outputArea.setText(decompressed);
                statsLabel.setText("✅ Распакован Ultra-Compact формат");
                statsLabel.setForeground(new Color(0, 150, 0));
                return;
            } catch (Exception e) {
                statsLabel.setText("❌ Ошибка распаковки Ultra-Compact: " + e.getMessage());
                statsLabel.setForeground(Color.RED);
                return;
            }
        }
        
        // JSON формат для нейросети
        if (NeuroCompressor.isNeuroFormat(text)) {
            try {
                String decompressed = NeuroCompressor.decompressFromNeuro(text);
                outputArea.setText(decompressed);
                statsLabel.setText("✅ Распакован JSON формат нейросети");
                statsLabel.setForeground(new Color(0, 150, 0));
                return;
            } catch (Exception e) {
                statsLabel.setText("❌ Ошибка распаковки JSON формата: " + e.getMessage());
                statsLabel.setForeground(Color.RED);
                return;
            }
        }
        
        
        // Обычная распаковка
        int depth = (Integer) depthSpinner.getValue();
        String algorithm = (String) algorithmCombo.getSelectedItem();
        
        try {
            long startTime = System.currentTimeMillis();
            String decompressed;
            int compressedSize = text.getBytes("UTF-8").length;
            
            if ("GZIP".equals(algorithm)) {
                decompressed = GZipCompressor.decompress(text);
            } else if ("Рекурсивный GZIP".equals(algorithm)) {
                decompressed = GZipCompressor.decompressRecursive(text, depth);
            } else {
                decompressed = GZipCompressor.decompress(text);
            }
            
            long endTime = System.currentTimeMillis();
            int decompressedSize = decompressed.getBytes("UTF-8").length;
            double ratio = (double) compressedSize / decompressedSize;
            long time = endTime - startTime;
            
            outputArea.setText(decompressed);
            
            String stats = String.format("✅ Распаковано: %d → %d байт (коэфф.: %.2f) за %d мс", 
                compressedSize, decompressedSize, ratio, time);
            statsLabel.setText(stats);
            statsLabel.setForeground(new Color(0, 150, 0));
            
            // История
            HistoryManager.HistoryEntry entry = new HistoryManager.HistoryEntry(
                "DECOMPRESS", compressedSize, decompressedSize, time);
            HistoryManager.addEntry(entry);
            
        } catch (Exception e) {
            statsLabel.setText("❌ Ошибка распаковки: " + e.getMessage());
            statsLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }
    
    // Дополнительные методы
    private void copyToClipboard() {
        String text = outputArea.getText();
        if (!text.isEmpty()) {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            statsLabel.setText("📋 Скопировано в буфер обмена");
        }
    }
    
    private void clearAll() {
        inputArea.setText("");
        outputArea.setText("");
        statsLabel.setText("🧹 Очищено");
    }
    
    private void showHistory() {
        List<String> history = HistoryManager.getHistory();
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "История операций пуста\nФайл истории: sqtext_history.txt",
                "История", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Последние операции:\n\n");
            for (String entry : history) {
                sb.append(entry).append("\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane,
                "История операций (" + history.size() + " записей)",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void autoDetectAndProcess() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) return;
        
        boolean looksCompressed = GZipCompressor.isCompressed(text);
        if (looksCompressed) {
            statsLabel.setText("🔍 Обнаружен сжатый текст. Нажмите 'Распаковать'");
            statsLabel.setForeground(Color.BLUE);
            algorithmCombo.setSelectedItem("GZIP");
        } else {
            statsLabel.setText("📝 Обычный текст. Нажмите 'Сжать'");
            statsLabel.setForeground(Color.BLACK);
        }
    }
    
    private void loadFileToInput() {
        String content = FileUtils.readFile(this);
        if (content != null) {
            inputArea.setText(content);
            autoDetectAndProcess();
        }
    }
    
    private void pasteToInput() {
        String clipboardContent = FileUtils.pasteFromClipboard();
        if (!clipboardContent.isEmpty()) {
            inputArea.setText(clipboardContent);
            autoDetectAndProcess();
        } else {
            statsLabel.setText("📋 Буфер обмена пуст или содержит не текст");
            statsLabel.setForeground(Color.ORANGE);
        }
    }
    
    private void saveOutputToFile() {
        String content = outputArea.getText();
        if (!content.isEmpty()) {
            boolean isCompressed = GZipCompressor.isCompressed(content);
            boolean saved = FileUtils.saveFile(this, content, isCompressed);
            if (saved) {
                statsLabel.setText("💾 Файл успешно сохранен");
                statsLabel.setForeground(new Color(0, 150, 0));
            }
        } else {
            statsLabel.setText("⚠️ Нет данных для сохранения");
            statsLabel.setForeground(Color.ORANGE);
        }
    }
    
    private void compressForNeuro() {
    	showProgress("Начинаем сжатие...", 10);
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            statsLabel.setText("⚠️ Введите текст для сжатия");
            statsLabel.setForeground(Color.ORANGE);
            return;
        }
        
        int depth = (Integer) depthSpinner.getValue();
        
        try {
            long startTime = System.currentTimeMillis();
            String compressed = NeuroCompressor.compressForNeuro(text, depth, "GZIP");
            long endTime = System.currentTimeMillis();
            
            outputArea.setText(compressed);
            
            String stats = String.format("✅ Сжато в JSON-формат для нейросети за %d мс", 
                endTime - startTime);
            statsLabel.setText(stats);
            statsLabel.setForeground(new Color(0, 150, 0));
            
        } catch (Exception e) {
            statsLabel.setText("❌ Ошибка создания JSON формата: " + e.getMessage());
            statsLabel.setForeground(Color.RED);
            e.printStackTrace();
            showProgress("Ошибка сжатия", 100);
        }
        showProgress("Сжатие завершено", 100);
    }
    
    private void compressUltraCompact() {
    	showProgress("Начинаем сжатие...", 10);
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            statsLabel.setText("⚠️ Введите текст для сжатия");
            statsLabel.setForeground(Color.ORANGE);
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String compressed = AdvancedCompressor.compressUltraCompact(text);
            long endTime = System.currentTimeMillis();
            
            outputArea.setText(compressed);
            
            String stats = String.format("✅ Ultra-Compact сжатие: %d → %d байт за %d мс",
                text.getBytes("UTF-8").length, compressed.getBytes("UTF-8").length, 
                endTime - startTime);
            statsLabel.setText(stats);
            statsLabel.setForeground(new Color(0, 150, 0));
            
        } catch (Exception e) {
            statsLabel.setText("❌ Ошибка Ultra-Compact сжатия: " + e.getMessage());
            statsLabel.setForeground(Color.RED);
            e.printStackTrace();
            showProgress("Ошибка сжатия", 100);
        }
        showProgress("Сжатие завершено", 100);
    }
    
    private void smartCompress() {
    	showProgress("Начинаем сжатие...", 10);
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            statsLabel.setText("⚠️ Введите текст для сжатия");
            statsLabel.setForeground(Color.ORANGE);
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String compressed = AdvancedCompressor.compressSmart(text);
            long endTime = System.currentTimeMillis();
            
            String json = "{\n";
            json += "  \"format\": \"sqz-smart\",\n";
            json += "  \"original\": " + text.length() + ",\n";
            json += "  \"compressed\": " + compressed.length() + ",\n";
            json += "  \"ratio\": " + String.format("%.2f", (double)compressed.length()/text.length()) + ",\n";
            json += "  \"time_ms\": " + (endTime - startTime) + ",\n";
            json += "  \"data\": \"" + compressed + "\"\n";
            json += "}";
            
            outputArea.setText(json);
            
            String stats = String.format("✅ Smart сжатие: %d → %d байт (коэфф.: %.2f) за %d мс",
                text.length(), compressed.length(),
                (double)compressed.length()/text.length(),
                endTime - startTime);
            statsLabel.setText(stats);
            statsLabel.setForeground(new Color(0, 150, 0));
            
        } catch (Exception e) {
            statsLabel.setText("❌ Ошибка Smart сжатия: " + e.getMessage());
            statsLabel.setForeground(Color.RED);
            e.printStackTrace();
            showProgress("Ошибка сжатия", 100);
        }
        showProgress("Сжатие завершено", 100);
    }
    
    private void showFontSettings() {
        FontSettingsDialog dialog = new FontSettingsDialog(this);
        dialog.setCurrentSettings(16, false);
        dialog.setVisible(true);
        
        if (dialog.isApproved()) {
            Font newFont = new Font("Courier New", Font.PLAIN, dialog.getFontSize());
            inputArea.setFont(newFont);
            outputArea.setFont(newFont);
            
            if (dialog.isDarkTheme()) {
                inputArea.setBackground(Color.DARK_GRAY);
                inputArea.setForeground(Color.WHITE);
                outputArea.setBackground(Color.DARK_GRAY);
                outputArea.setForeground(Color.WHITE);
            } else {
                inputArea.setBackground(Color.WHITE);
                inputArea.setForeground(Color.BLACK);
                outputArea.setBackground(Color.WHITE);
                outputArea.setForeground(Color.BLACK);
            }
        }
    }
    
    private void showHelp() {
        JOptionPane.showMessageDialog(this,
            "<html><b>SqText - Горячие клавиши:</b><br><br>" +
            "• <b>Ctrl+Enter</b> - Сжать текст<br>" +
            "• <b>Ctrl+Shift+Enter</b> - Распаковать текст<br>" +
            "• <b>Ctrl+L</b> - Очистить все поля<br>" +
            "• <b>Ctrl+C</b> - Копировать результат<br>" +
            "• <b>Ctrl+V</b> - Вставить в поле ввода<br><br>" +
            "<b>Форматы файлов:</b> .txt, .java, .json, .xml, .md<br>" +
            "<b>Алгоритмы:</b> GZIP, Рекурсивный GZIP, Для нейросети (JSON), Ultra-Compact, Smart<br>" +
            "<b>Расширение сжатых файлов:</b> .sqz</html>",
            "Справка", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
    private void compressForChat() {
    	showProgress("Начинаем сжатие...", 10);
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            statsLabel.setText("⚠️ Введите текст для сжатия");
            statsLabel.setForeground(Color.ORANGE);
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String compressed = ChatCompressor.compress(text);
            long endTime = System.currentTimeMillis();
            
            outputArea.setText(compressed);
            
            String stats = String.format("✅ Сжато для чат-бота: %d → %d байт за %d мс",
                text.getBytes("UTF-8").length, 
                compressed.getBytes("UTF-8").length, 
                endTime - startTime);
            statsLabel.setText(stats);
            statsLabel.setForeground(new Color(0, 150, 0));
            
        } catch (Exception e) {
            statsLabel.setText("❌ Ошибка создания чат-формата: " + e.getMessage());
            statsLabel.setForeground(Color.RED);
            e.printStackTrace();
            showProgress("Ошибка сжатия", 100);
        }
        showProgress("Сжатие завершено", 100);
    }
 // Метод для показа прогресса:
    private void showProgress(String message, int value) {
    progressBar.setVisible(true);
    progressBar.setValue(value);
    progressBar.setString(message);
    if (value >= 100) {
        // Через 2 секунды скрыть
        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
            progressBar.setVisible(false);
            ((javax.swing.Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
 // Вставить Н
    private void applyFontSettings() {
        Font currentFont = inputArea.getFont();
        String fontName = currentFont.getName();
        int fontSize = currentFont.getSize();
        
        // Обновляем шрифт для всех компонентов
        updateComponentFonts(fontName, fontSize);
    }

    private void updateComponentFonts(String fontName, int fontSize) {
        Font newFont = new Font(fontName, Font.PLAIN, fontSize);
        Font newBoldFont = new Font(fontName, Font.BOLD, fontSize);
        
        inputArea.setFont(newFont);
        outputArea.setFont(newFont);
        algorithmCombo.setFont(newFont);
        statsLabel.setFont(newBoldFont);
        
        // Обновляем кнопки
        Font buttonFont = new Font(fontName, Font.BOLD, Math.max(12, fontSize - 2));
        compressBtn.setFont(buttonFont);
        decompressBtn.setFont(buttonFont);
        copyBtn.setFont(buttonFont);
        clearBtn.setFont(buttonFont);
        historyBtn.setFont(buttonFont);
        
        // Перерисовываем окно
        revalidate();
        repaint();
    }
    // Вставить К
}
package com.sqtext.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FontSettingsDialog extends JDialog {
    private JSpinner fontSizeSpinner;
    private JCheckBox darkThemeCheckbox;
    private JTextArea previewArea;
    private boolean approved = false;
    
    public FontSettingsDialog(JFrame parent) {
        super(parent, "Настройки шрифта и темы", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(16, 8, 36, 1));
        darkThemeCheckbox = new JCheckBox("Тёмная тема");
        
        previewArea = new JTextArea("Пример текста для предпросмотра\n" +
            "Размер шрифта можно изменить здесь.\n" +
            "Этот текст отображает текущие настройки.");
        previewArea.setEditable(false);
        
        // Кнопки
        JButton okButton = new JButton("Применить");
        JButton cancelButton = new JButton("Отмена");
        
        okButton.addActionListener(e -> {
            approved = true;
            setVisible(false);
        });
        
        cancelButton.addActionListener(e -> {
            approved = false;
            setVisible(false);
        });
        
        // Слушатель изменения размера шрифта
        fontSizeSpinner.addChangeListener(e -> {
            int size = (Integer) fontSizeSpinner.getValue();
            previewArea.setFont(new Font("Courier New", Font.PLAIN, size));
        });
        
        // Слушатель темы
        darkThemeCheckbox.addActionListener(e -> {
            if (darkThemeCheckbox.isSelected()) {
                previewArea.setBackground(Color.DARK_GRAY);
                previewArea.setForeground(Color.WHITE);
            } else {
                previewArea.setBackground(Color.WHITE);
                previewArea.setForeground(Color.BLACK);
            }
        });
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель настроек
        JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        settingsPanel.add(new JLabel("Размер шрифта:"));
        settingsPanel.add(fontSizeSpinner);
        settingsPanel.add(new JLabel("Тема:"));
        settingsPanel.add(darkThemeCheckbox);
        
        // Панель предпросмотра
        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setBorder(BorderFactory.createTitledBorder("Предпросмотр"));
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(new JButton("Применить"));
        buttonPanel.add(new JButton("Отмена"));
        
        mainPanel.add(settingsPanel, BorderLayout.NORTH);
        mainPanel.add(previewScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    public int getFontSize() {
        return (Integer) fontSizeSpinner.getValue();
    }
    
    public boolean isDarkTheme() {
        return darkThemeCheckbox.isSelected();
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setCurrentSettings(int fontSize, boolean darkTheme) {
        fontSizeSpinner.setValue(fontSize);
        darkThemeCheckbox.setSelected(darkTheme);
        
        // Применяем к превью
        previewArea.setFont(new Font("Courier New", Font.PLAIN, fontSize));
        if (darkTheme) {
            previewArea.setBackground(Color.DARK_GRAY);
            previewArea.setForeground(Color.WHITE);
        }
    }
}
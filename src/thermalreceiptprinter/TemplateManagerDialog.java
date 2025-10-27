package thermalreceiptprinter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class TemplateManagerDialog extends JDialog {

    private JList<String> templatesList;
    private DefaultListModel<String> listModel;
    private JTextArea previewArea;
    private JTextArea infoArea;
    private JButton loadButton;
    private JButton deleteButton;
    private JButton exportButton;
    private JButton importButton;
    private JButton saveCurrentButton;
    private JButton closeButton;
    private MainFrame parentFrame;
    private TemplateManager templateManager;

    public TemplateManagerDialog(MainFrame parent) {
        super(parent, "Template Manager", true);
        this.parentFrame = parent;
        this.templateManager = new TemplateManager();
        initComponents();
        loadTemplatesList();
        setupEventListeners();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(getParent());

        JPanel leftPanel = createTemplatesListPanel();

        JPanel rightPanel = createPreviewPanel();

        JPanel bottomPanel = createButtonPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTemplatesListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Available Templates"));
        panel.setPreferredSize(new Dimension(250, 400));

        listModel = new DefaultListModel<>();
        templatesList = new JList<>(listModel);
        templatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templatesList.setCellRenderer(new TemplateListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(templatesList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel countLabel = new JLabel("0 templates");
        countLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(countLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(new TitledBorder("Template Preview"));

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        previewArea.setBackground(new Color(248, 248, 248));
        previewArea.setText("Select a template to preview its content");

        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setPreferredSize(new Dimension(450, 300));
        previewPanel.add(previewScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Preview", previewPanel);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new TitledBorder("Template Information"));

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoArea.setBackground(new Color(252, 252, 252));
        infoArea.setText("Select a template to view its information");

        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setPreferredSize(new Dimension(450, 300));
        infoPanel.add(infoScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Info", infoPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        loadButton = new JButton("Load Template");
        deleteButton = new JButton("Delete Template");
        exportButton = new JButton("Export Template");
        importButton = new JButton("Import Template");
        saveCurrentButton = new JButton("Save Current as Template");
        closeButton = new JButton("Close");

        loadButton.setBackground(new Color(52, 152, 219));
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);

        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        exportButton.setBackground(new Color(46, 204, 113));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);

        importButton.setBackground(new Color(155, 89, 182));
        importButton.setForeground(Color.WHITE);
        importButton.setFocusPainted(false);

        saveCurrentButton.setBackground(new Color(243, 156, 18));
        saveCurrentButton.setForeground(Color.WHITE);
        saveCurrentButton.setFocusPainted(false);

        panel.add(loadButton);
        panel.add(deleteButton);
        panel.add(exportButton);
        panel.add(importButton);
        panel.add(saveCurrentButton);
        panel.add(closeButton);

        return panel;
    }

    private void setupEventListeners() {

        templatesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updatePreviewAndInfo();
                    updateButtonStates();
                }
            }
        });

        loadButton.addActionListener(e -> loadSelectedTemplate());
        deleteButton.addActionListener(e -> deleteSelectedTemplate());
        exportButton.addActionListener(e -> exportSelectedTemplate());
        importButton.addActionListener(e -> importTemplate());
        saveCurrentButton.addActionListener(e -> saveCurrentAsTemplate());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadTemplatesList() {
        listModel.clear();
        List<String> templates = templateManager.listTemplates();
        for (String template : templates) {
            listModel.addElement(template);
        }

        Component[] components = ((JPanel) getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setText(templates.size() + " template" + (templates.size() != 1 ? "s" : ""));
                break;
            }
        }

        updateButtonStates();
    }

    private void updatePreviewAndInfo() {
        String selectedTemplate = templatesList.getSelectedValue();
        if (selectedTemplate == null) {
            previewArea.setText("Select a template to preview its content");
            infoArea.setText("Select a template to view its information");
            return;
        }

        String content = templateManager.loadTemplate(selectedTemplate);
        if (content != null) {
            // Process the content to remove formatting tags for preview
            String processedContent = processTemplateForPreview(content);
            previewArea.setText(processedContent);
            previewArea.setCaretPosition(0);
        } else {
            previewArea.setText("Error loading template content");
        }

        TemplateManager.TemplateInfo info = templateManager.getTemplateInfo(selectedTemplate);
        if (info != null) {
            StringBuilder infoText = new StringBuilder();
            infoText.append("Template Name: ").append(info.getDisplayName()).append("\n\n");
            infoText.append("Description: ").append(info.getDescription()).append("\n\n");
            infoText.append("Created: ").append(info.getFormattedDate()).append("\n\n");

            if (content != null) {
                String[] lines = content.split("\n");
                infoText.append("Lines: ").append(lines.length).append("\n");
                infoText.append("Characters: ").append(content.length()).append("\n");

                long formatCommands = content.lines()
                        .filter(line -> line.contains("[") && line.contains("]"))
                        .count();
                infoText.append("Format Commands: ").append(formatCommands);
            }

            infoArea.setText(infoText.toString());
        } else {
            infoArea.setText("No information available for this template");
        }

        infoArea.setCaretPosition(0);
    }

    private void updateButtonStates() {
        boolean hasSelection = templatesList.getSelectedValue() != null;
        loadButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        exportButton.setEnabled(hasSelection);
    }

    private void loadSelectedTemplate() {
        String selectedTemplate = templatesList.getSelectedValue();
        if (selectedTemplate == null) {
            return;
        }

        String content = templateManager.loadTemplate(selectedTemplate);
        if (content != null) {
            parentFrame.setReceiptContent(content);
            JOptionPane.showMessageDialog(this,
                    "Template '" + selectedTemplate + "' loaded successfully!",
                    "Template Loaded",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to load template: " + selectedTemplate,
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedTemplate() {
        String selectedTemplate = templatesList.getSelectedValue();
        if (selectedTemplate == null) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the template '" + selectedTemplate + "'?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (templateManager.deleteTemplate(selectedTemplate)) {
                JOptionPane.showMessageDialog(this,
                        "Template '" + selectedTemplate + "' deleted successfully!",
                        "Template Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                loadTemplatesList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete template: " + selectedTemplate,
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportSelectedTemplate() {
        String selectedTemplate = templatesList.getSelectedValue();
        if (selectedTemplate == null) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Template");
        fileChooser.setSelectedFile(new File(selectedTemplate + ".template"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File exportFile = fileChooser.getSelectedFile();
            if (templateManager.exportTemplate(selectedTemplate, exportFile)) {
                JOptionPane.showMessageDialog(this,
                        "Template exported successfully to:\n" + exportFile.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to export template",
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Template");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File importFile = fileChooser.getSelectedFile();

            String templateName = JOptionPane.showInputDialog(this,
                    "Enter a name for the imported template:",
                    "Template Name",
                    JOptionPane.QUESTION_MESSAGE);

            if (templateName != null && !templateName.trim().isEmpty()) {

                if (templateManager.templateExists(templateName)) {
                    int result = JOptionPane.showConfirmDialog(this,
                            "A template with the name '" + templateName + "' already exists.\nDo you want to overwrite it?",
                            "Template Exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                if (templateManager.importTemplate(importFile, templateName)) {
                    JOptionPane.showMessageDialog(this,
                            "Template imported successfully as '" + templateName + "'",
                            "Import Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadTemplatesList();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to import template",
                            "Import Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void saveCurrentAsTemplate() {
        String currentContent = parentFrame.getReceiptContent();
        if (currentContent == null || currentContent.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No content to save. Please enter some text in the receipt editor first.",
                    "No Content",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SaveTemplateDialog saveDialog = new SaveTemplateDialog(this);
        saveDialog.setVisible(true);

        if (saveDialog.isConfirmed()) {
            String templateName = saveDialog.getTemplateName();
            String description = saveDialog.getDescription();

            if (templateManager.templateExists(templateName)) {
                int result = JOptionPane.showConfirmDialog(this,
                        "A template with the name '" + templateName + "' already exists.\nDo you want to overwrite it?",
                        "Template Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            if (templateManager.saveTemplate(templateName, currentContent, description)) {
                JOptionPane.showMessageDialog(this,
                        "Template '" + templateName + "' saved successfully!",
                        "Template Saved",
                        JOptionPane.INFORMATION_MESSAGE);
                loadTemplatesList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save template",
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class TemplateListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value != null) {
                setText(value.toString());
                setIcon(new TemplateIcon());
            }

            return this;
        }
    }

    private class TemplateIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(100, 149, 237));
            g2.fillRect(x + 2, y + 1, 10, 12);
            g2.setColor(Color.WHITE);
            g2.drawLine(x + 4, y + 3, x + 10, y + 3);
            g2.drawLine(x + 4, y + 5, x + 10, y + 5);
            g2.drawLine(x + 4, y + 7, x + 8, y + 7);
            g2.drawLine(x + 4, y + 9, x + 10, y + 9);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private String processTemplateForPreview(String content) {
        if (content == null) {
            return null;
        }

        String processed = content;

        // Remove formatting tags while preserving the content
        processed = processed.replaceAll(
                "\\[BOLD\\]|\\[/BOLD\\]|"
                + "\\[SIZE=\\d+\\]|\\[/SIZE\\]|"
                + "\\[FONT=[^\\]]+\\]|\\[/FONT\\]|"
                + "\\[CENTER\\]|\\[/CENTER\\]|"
                + "\\[RIGHT\\]|\\[/RIGHT\\]|"
                + "\\[LEFT\\]|\\[/LEFT\\]",
                ""
        );

        return processed;
    }
}

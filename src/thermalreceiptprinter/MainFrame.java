package thermalreceiptprinter;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrintQuality;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFrame extends JFrame {

    private JTextPane receiptTextPane;
    private JPanel previewPanel;
    private JLabel previewLabel;
    private JButton printButton;
    private JButton clearButton;
    private JButton templatesButton;
    private JButton boldButton;
    private JButton plainButton;
    private JSpinner fontSizeSpinner;
    private JComboBox<String> alignmentCombo;
    private JSpinner customFontSizeSpinner;

    private JComboBox<String> fontStyleCombo;
    private JSpinner lineSpacingSpinner;
    private String currentFontStyle = "Courier New";
    private String allSetFontStyle = "Courier New";
    private float lineSpacing = 1.0f;
    private JButton setAllFontStyle;
    public boolean isok = false;
    private int fontSize = 12;
    private String alignment = "LEFT";
    private final int RECEIPT_WIDTH = 42;
    private final int TSP100_CHAR_WIDTH = 42;
    private JTextPane previewTextPane;

    public MainFrame() {
        initPreviewTextPane();
        initComponents();
        setupEventListeners();
        updatePreview();
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/logo1.png")));
    }

    private void initComponents() {
        setTitle("Thermal Receipt Printer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        JPanel leftPanel = createEditorPanel();
        JPanel rightPanel = createPreviewPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newMenuItem = new JMenuItem("New Receipt");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newMenuItem.addActionListener(e -> clearReceipt());

        JMenuItem templatesMenuItem = new JMenuItem("Manage Templates...");
        templatesMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
        templatesMenuItem.addActionListener(e -> openTemplateManager());

        JMenuItem printMenuItem = new JMenuItem("Print");
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        printMenuItem.addActionListener(e -> printReceipt());

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(templatesMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(printMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        JMenu formatMenu = new JMenu("Format");

        JMenuItem boldMenuItem = new JMenuItem("Bold");
        boldMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
        boldMenuItem.addActionListener(e -> applyBold());

        JMenuItem plainMenuItem = new JMenuItem("Plain Text");
        plainMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));
        plainMenuItem.addActionListener(e -> applyPlain());

        formatMenu.add(boldMenuItem);
        formatMenu.add(plainMenuItem);

        JMenu helpMenu = new JMenu("Help");

        JMenuItem formatHelpItem = new JMenuItem("Formatting Help");
        formatHelpItem.addActionListener(e -> showFormattingHelp());

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> showAbout());

        helpMenu.add(formatHelpItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Receipt Editor"));
        panel.setPreferredSize(new Dimension(400, 600));

        receiptTextPane = new JTextPane();
        receiptTextPane.setFont(new Font("Courier New", Font.PLAIN, 14));

        StyledDocument doc = receiptTextPane.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "Courier New");
        StyleConstants.setFontSize(defaultStyle, 14);

        JScrollPane scrollPane = new JScrollPane(receiptTextPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel controlsPanel = createControlsPanel();
        JPanel formattingPanel = createFormattingPanel(); // New formatting panel
        JPanel buttonsPanel = createButtonsPanel();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlsPanel, BorderLayout.NORTH);
        topPanel.add(formattingPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormattingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Text Formatting"));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 9));

        boldButton = new JButton("B");
        boldButton.setFont(new Font("Arial", Font.BOLD, 10));
        boldButton.setPreferredSize(new Dimension(40, 22));
        boldButton.setToolTipText("Bold (Ctrl+B)");
        boldButton.setFocusPainted(false);
        boldButton.addActionListener(e -> applyBold());

        plainButton = new JButton("P");
        plainButton.setFont(new Font("Arial", Font.PLAIN, 10));
        plainButton.setPreferredSize(new Dimension(40, 22));
        plainButton.setToolTipText("Plain Text (Ctrl+U)");
        plainButton.setFocusPainted(false);
        plainButton.addActionListener(e -> applyPlain());

        customFontSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 8, 72, 1));
        customFontSizeSpinner.setPreferredSize(new Dimension(60, 22));
        customFontSizeSpinner.setToolTipText("Custom font size for selected text");

        String[] fontStyles = {
            "Courier New", "Arial", "Arial Black", "Arial Narrow", "Arial Rounded MT Bold", "Bahnschrift",
            "Calibri", "Cambria", "Candara", "Comic Sans MS", "Consolas", "Constantia", "Corbel", "Courier New",
            "Ebrima", "Franklin Gothic Medium", "Gabriola", "Georgia", "Impact", "Ink Free", "Javanese Text",
            "Lucida Console", "Lucida Sans Unicode", "Malgun Gothic", "Microsoft Himalaya",
            "Microsoft JhengHei", "Microsoft New Tai Lue", "Microsoft PhagsPa", "Microsoft Sans Serif",
            "Microsoft Tai Le", "Microsoft YaHei", "Microsoft Yi Baiti", "MingLiU-ExtB", "Mongolian Baiti",
            "MS Gothic", "MV Boli", "Myanmar Text", "Nirmala UI", "Palatino Linotype",
            "Segoe Print", "Segoe Script", "Segoe UI", "Segoe UI Emoji", "Segoe UI Historic", "Segoe UI Symbol",
            "SimSun", "Sitka", "Sylfaen", "Tahoma", "Times New Roman", "Trebuchet MS", "Verdana", "Yu Gothic"
        };
        fontStyleCombo = new JComboBox<>(fontStyles);
        fontStyleCombo.setPreferredSize(new Dimension(210, 22));
        fontStyleCombo.setToolTipText("Font style for selected text");

        fontStyleCombo.setRenderer(new EnhancedFontComboBoxRenderer());
        fontStyleCombo.setMaximumRowCount(10);
        fontStyleCombo.addActionListener(e -> applyFontStyle());

        setAllFontStyle = new JButton("Set All");
        setAllFontStyle.setFont(new Font("Arial", Font.PLAIN, 10));
        setAllFontStyle.setPreferredSize(new Dimension(95, 22));
        setAllFontStyle.setToolTipText("Set font style for all Text");
        setAllFontStyle.setFocusPainted(false);
        setAllFontStyle.addActionListener(e -> applyFontStyleToAll());

        lineSpacingSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 3.0, 0.1));
        lineSpacingSpinner.setPreferredSize(new Dimension(60, 22));
        lineSpacingSpinner.setToolTipText("Line spacing");
        lineSpacingSpinner.addChangeListener(e -> applyLineSpacing());

        topRow.add(new JLabel("Format:"));
        topRow.add(Box.createHorizontalStrut(9));
        topRow.add(boldButton);
        topRow.add(plainButton);
        topRow.add(Box.createHorizontalStrut(5));
        topRow.add(new JLabel("Size:"));
        topRow.add(customFontSizeSpinner);
        topRow.add(Box.createHorizontalStrut(5));
        topRow.add(new JLabel("Spacing:"));
        topRow.add(lineSpacingSpinner);

        bottomRow.add(new JLabel("Font Style:"));
        bottomRow.add(fontStyleCombo);
        bottomRow.add(setAllFontStyle);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(bottomRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Font Size:"));
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(12, 8, 24, 1));
        panel.add(fontSizeSpinner);

        panel.add(new JLabel("Default Alignment:"));
        alignmentCombo = new JComboBox<>(new String[]{"LEFT", "CENTER", "RIGHT"});
        panel.add(alignmentCombo);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        printButton = new JButton("Print Receipt");
        clearButton = new JButton("Clear");
        templatesButton = new JButton("Templates");

        printButton.setBackground(new Color(52, 152, 219));
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("Arial", Font.BOLD, 12));
        printButton.setFocusPainted(false);

        clearButton.setBackground(new Color(255, 51, 51));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));

        templatesButton.setBackground(new Color(46, 204, 113));
        templatesButton.setForeground(Color.WHITE);
        templatesButton.setFocusPainted(false);
        templatesButton.setFont(new Font("Arial", Font.BOLD, 12));

        panel.add(printButton);
        panel.add(clearButton);
        panel.add(templatesButton);

        return panel;
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Receipt Preview (80mm width)"));

        previewTextPane = new JTextPane();  // Change to JTextPane
        previewTextPane.setEditable(false);
        previewTextPane.setBackground(Color.WHITE);
        previewTextPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initPreviewTextPane();

        // Create scroll pane
        JScrollPane previewScroll = new JScrollPane(previewTextPane);
        previewScroll.setPreferredSize(new Dimension(320, 500));
        previewScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(previewScroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("<html><center>Preview shows exactly how your receipt will print<br/>42 characters wide for 80mm thermal paper</center></html>");
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel formatLabel = new JLabel("<html><center><small>Use [CENTER], [RIGHT], [LINE] for formatting<br/>Select text and click B for bold</small></center></html>");
        formatLabel.setHorizontalAlignment(JLabel.CENTER);
        formatLabel.setForeground(Color.GRAY);
        formatLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.add(formatLabel, BorderLayout.SOUTH);

        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEventListeners() {
        receiptTextPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
                updateBoldButton();
                updateFormatButtons();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
                updateBoldButton();
                updateFormatButtons();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
                updateBoldButton();
                updateFormatButtons();
            }
        });

        receiptTextPane.addCaretListener(e -> updateBoldButton());

        fontSizeSpinner.addChangeListener(e -> {
            fontSize = (Integer) fontSizeSpinner.getValue();
            updatePreview();
        });

        alignmentCombo.addActionListener(e -> {
            alignment = (String) alignmentCombo.getSelectedItem();
            updatePreview();
        });

        printButton.addActionListener(e -> printReceipt());
        clearButton.addActionListener(e -> clearReceipt());
        templatesButton.addActionListener(e -> openTemplateManager());

        receiptTextPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK), "bold");
        receiptTextPane.getActionMap().put("bold", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyBold();
            }
        });

        receiptTextPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "plain");
        receiptTextPane.getActionMap().put("plain", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPlain();
            }
        });

        customFontSizeSpinner.addChangeListener(e -> {
            int start = receiptTextPane.getSelectionStart();
            int end = receiptTextPane.getSelectionEnd();
            if (start != end) {
                applyCustomFontSize();
            }
        });

        receiptTextPane.addCaretListener(e -> updateFormatButtons());
        fontSizeSpinner.addChangeListener(e -> {
            fontSize = (Integer) fontSizeSpinner.getValue();
            updatePreviewFont(); // Update font immediately
            updatePreview();
        });
    }

    private void initPreviewTextPane() {
        previewTextPane = new JTextPane() {
            @Override
            public FontMetrics getFontMetrics(Font font) {
                // Scale font size for preview accuracy
                Font previewFont = font.deriveFont((float) (font.getSize() * 1.15));
                return super.getFontMetrics(previewFont);
            }
        };

        previewTextPane.setEditable(false);
        previewTextPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        previewTextPane.setFont(new Font("Courier New", Font.PLAIN, (int) (fontSize * 1.15)));
    }

    private void updatePreviewFont() {
        Font baseFont = new Font("Courier New", Font.PLAIN, fontSize);
        // Scale up for better visibility in preview
        Font previewFont = baseFont.deriveFont((float) (fontSize * 1.25));
        previewTextPane.setFont(previewFont);
    }

    private class EnhancedFontComboBoxRenderer extends DefaultListCellRenderer {

        private static final int DISPLAY_FONT_SIZE = 14;
        private final Set<String> availableFonts;

        public EnhancedFontComboBoxRenderer() {
            // Cache available fonts for performance
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            availableFonts = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value != null) {
                String fontName = value.toString();

                boolean fontAvailable = availableFonts.contains(fontName)
                        || availableFonts.stream().anyMatch(f -> f.equalsIgnoreCase(fontName));

                if (fontAvailable) {
                    try {
                        Font displayFont = new Font(fontName, Font.PLAIN, DISPLAY_FONT_SIZE);
                        setFont(displayFont);
                        setText(fontName);
                        setForeground(isSelected ? Color.WHITE : Color.BLACK);
                    } catch (Exception e) {
                        setFont(new Font("SansSerif", Font.PLAIN, DISPLAY_FONT_SIZE));
                        setText(fontName + " (unavailable)");
                        setForeground(isSelected ? Color.LIGHT_GRAY : Color.GRAY);
                    }
                } else {
                    setFont(new Font("SansSerif", Font.PLAIN, DISPLAY_FONT_SIZE));
                    setText(fontName + " (not installed)");
                    setForeground(isSelected ? Color.LIGHT_GRAY : Color.GRAY);
                }
                setToolTipText("Font: " + fontName + (fontAvailable ? " (available)" : " (not available)"));
            }
            return this;
        }
    }

    private void applyFontStyleToAll() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        String selectedFont = (String) fontStyleCombo.getSelectedItem();

        if (selectedFont == null) {
            return;
        }

        try {
            String plainText = doc.getText(0, doc.getLength());

            doc.remove(0, doc.getLength());

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, selectedFont);
            StyleConstants.setFontSize(attrs, 14); // Default size
            StyleConstants.setBold(attrs, false); // Default not bold

            doc.insertString(0, plainText, attrs);

            MutableAttributeSet inputAttrs = receiptTextPane.getInputAttributes();
            inputAttrs.removeAttributes(inputAttrs);
            StyleConstants.setFontFamily(inputAttrs, selectedFont);
            StyleConstants.setFontSize(inputAttrs, 14);
            StyleConstants.setBold(inputAttrs, false);

            //currentFontStyle = selectedFont;
            allSetFontStyle = selectedFont;
            isok = true;
            // Update preview
            updatePreview();

            // Show confirmation message
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error applying font style: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFontStyle() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();
        String selectedFont = (String) fontStyleCombo.getSelectedItem();

        if (start == end) {
            // No selection, set font for new text input
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            StyleConstants.setFontFamily(attrs, selectedFont);
            currentFontStyle = selectedFont;
        } else {

            try {
                // Process each character individually to maintain existing formatting
                for (int i = start; i < end; i++) {
                    AttributeSet existingAttrs = doc.getCharacterElement(i).getAttributes();
                    SimpleAttributeSet newAttrs = new SimpleAttributeSet(existingAttrs);
                    StyleConstants.setFontFamily(newAttrs, selectedFont);
                    doc.setCharacterAttributes(i, 1, newAttrs, false);
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Fallback: apply to entire selection with replace=false
                SimpleAttributeSet fallbackAttrs = new SimpleAttributeSet();
                StyleConstants.setFontFamily(fallbackAttrs, selectedFont);
                doc.setCharacterAttributes(start, end - start, fallbackAttrs, false);
            }

            // Update input attributes for new text
            MutableAttributeSet inputAttrs = receiptTextPane.getInputAttributes();
            StyleConstants.setFontFamily(inputAttrs, selectedFont);
            currentFontStyle = selectedFont;

            // Maintain selection
            receiptTextPane.setSelectionStart(start);
            receiptTextPane.setSelectionEnd(end);
        }

        receiptTextPane.requestFocusInWindow();
        updatePreview();
    }

    private void applyLineSpacing() {
        lineSpacing = ((Double) lineSpacingSpinner.getValue()).floatValue();

        StyledDocument doc = receiptTextPane.getStyledDocument();

        // Always apply to entire document
        int start = 0;
        int end = doc.getLength();

        // Calculate exact line height based on font metrics
        Font font = receiptTextPane.getFont();
        FontMetrics fm = receiptTextPane.getFontMetrics(font);
        int lineHeight = (int) (fm.getHeight() * lineSpacing);

        // Apply line spacing to paragraphs
        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(paragraphAttrs, 0); // Reset any existing spacing
        StyleConstants.setSpaceAbove(paragraphAttrs, 0);
        StyleConstants.setSpaceBelow(paragraphAttrs, lineHeight - fm.getHeight());
        doc.setParagraphAttributes(start, end - start, paragraphAttrs, false);

        receiptTextPane.requestFocusInWindow();
        updatePreview();
    }

    private void applyCustomFontSize() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            // No selection, set font size for new text input
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            int selectedFontSize = (Integer) customFontSizeSpinner.getValue();
            StyleConstants.setFontSize(attrs, selectedFontSize);
        } else {
            // Apply font size to selection
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            int selectedFontSize = (Integer) customFontSizeSpinner.getValue();
            StyleConstants.setFontSize(attrs, selectedFontSize);
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }

        receiptTextPane.requestFocusInWindow();
        updatePreview();
    }

    private void applyBold() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            // No selection, set bold for new text input
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            StyleConstants.setBold(attrs, true);
        } else {
            // Apply bold to selection
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBold(attrs, true);
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }

        receiptTextPane.requestFocusInWindow();
        updateFormatButtons();
    }

    private void applyPlain() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            // No selection, set plain for new text input
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            StyleConstants.setBold(attrs, false);
        } else {
            // Apply plain to selection
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBold(attrs, false);
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }

        receiptTextPane.requestFocusInWindow();
        updateFormatButtons();
    }

    private boolean isSelectionBold() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            // No selection, check input attributes
            return StyleConstants.isBold(receiptTextPane.getInputAttributes());
        } else {
            // Check if any part of selection is bold
            for (int i = start; i < end; i++) {
                AttributeSet attrs = doc.getCharacterElement(i).getAttributes();
                if (StyleConstants.isBold(attrs)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isSelectionPlain() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            // No selection, check input attributes
            return !StyleConstants.isBold(receiptTextPane.getInputAttributes());
        } else {
            // Check if any part of selection is plain (not bold)
            for (int i = start; i < end; i++) {
                AttributeSet attrs = doc.getCharacterElement(i).getAttributes();
                if (!StyleConstants.isBold(attrs)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void updateFormatButtons() {
        boolean isBold = isSelectionBold();
        boolean isPlain = isSelectionPlain();
        int fontSize = getSelectionFontSize();

        boldButton.setBackground(isBold ? new Color(200, 200, 255) : null);
        boldButton.setOpaque(isBold);

        plainButton.setBackground(isPlain ? new Color(220, 220, 220) : null);
        plainButton.setOpaque(isPlain);

        // Update the font size spinner without triggering change events
        SwingUtilities.invokeLater(() -> {
            customFontSizeSpinner.setValue(fontSize);
        });
    }

    private void updateBoldButton() {
        boolean isBold = isSelectionBold();
        boldButton.setBackground(isBold ? new Color(200, 200, 255) : null);
        boldButton.setOpaque(isBold);
    }

    // Method to convert styled text to plain text with formatting codes
    private String getFormattedText() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        StringBuilder result = new StringBuilder();

        try {
            String text = doc.getText(0, doc.getLength());

            // Track current formatting state
            boolean inBold = false;
            int currentFontSize = 14;
            String currentFont = "Courier New";

            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                AttributeSet attrs = doc.getCharacterElement(i).getAttributes();

                boolean charIsBold = StyleConstants.isBold(attrs);
                int charFontSize = StyleConstants.getFontSize(attrs);
                String charFontFamily = StyleConstants.getFontFamily(attrs);

                // Normalize font family name
                if (charFontFamily == null || charFontFamily.trim().isEmpty()) {
                    charFontFamily = "Courier New";
                }

                // Handle bold formatting changes
                if (charIsBold != inBold) {
                    if (charIsBold) {
                        result.append("[BOLD]");
                        inBold = true;
                    } else {
                        result.append("[/BOLD]");
                        inBold = false;
                    }
                }

                // Handle font size changes
                if (charFontSize != currentFontSize) {
                    if (currentFontSize != 14) {
                        result.append("[/SIZE]");
                    }
                    if (charFontSize != 14) {
                        result.append("[SIZE=").append(charFontSize).append("]");
                    }
                    currentFontSize = charFontSize;
                }

                // Handle font family changes
                if (!charFontFamily.equals(currentFont)) {
                    if (!currentFont.equals("Courier New")) {
                        result.append("[/FONT]");
                    }
                    if (!charFontFamily.equals("Courier New")) {
                        result.append("[FONT=").append(charFontFamily).append("]");
                    }
                    currentFont = charFontFamily;
                }

                result.append(currentChar);
            }

            // Close any open tags at the end
            if (inBold) {
                result.append("[/BOLD]");
            }
            if (currentFontSize != 14) {
                result.append("[/SIZE]");
            }
            if (!currentFont.equals("Courier New")) {
                result.append("[/FONT]");
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    private String formatForTSP100(String input) {
        if (input.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        String[] lines = input.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                formatted.append("\n");
                continue;
            }

            // Skip formatting for lines with tags (they'll be processed in updatePreview)
            if (line.startsWith("[BOLD]") || line.startsWith("[/BOLD]")
                    || line.startsWith("[SIZE=") || line.startsWith("[/SIZE]")
                    || line.startsWith("[FONT=") || line.startsWith("[/FONT]")) {
                formatted.append(line).append("\n");
                continue;
            }

            // Handle table rows
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                StringBuilder tableLine = new StringBuilder();

                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i].trim();
                    int colWidth = (i == 0) ? 20 : 10;

                    if (part.length() > colWidth) {
                        part = part.substring(0, colWidth);
                    } else {
                        part = String.format("%-" + colWidth + "s", part);
                    }

                    tableLine.append(part);
                    if (i < parts.length - 1) {
                        tableLine.append(" ");
                    }
                }

                line = tableLine.toString();
            }

            // Handle other formatting
            if (line.startsWith("[CENTER]")) {
                line = centerTextTSP100(line.substring(8));
            } else if (line.startsWith("[RIGHT]")) {
                line = rightAlignTextTSP100(line.substring(7));
            } else if (line.startsWith("[LINE]")) {
                line = createLineTSP100();
            } else if ("CENTER".equals(alignment)) {
                line = centerTextTSP100(line);
            } else if ("RIGHT".equals(alignment)) {
                line = rightAlignTextTSP100(line);
            }

            formatted.append(line).append("\n");
        }

        return formatted.toString();
    }

    private int getVisibleTextLength(String text) {
        // Remove ALL formatting tags to get actual visible character count
        return text.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", "").length();
    }

    private int getSelectionFontSize() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            // No selection, return the input font size
            return StyleConstants.getFontSize(receiptTextPane.getInputAttributes());
        } else {
            // Return the font size of the first character in the selection
            AttributeSet attrs = doc.getCharacterElement(start).getAttributes();
            return StyleConstants.getFontSize(attrs);
        }
    }

    private String truncateWithFormatting(String text, int maxLength) {
        StringBuilder result = new StringBuilder();
        int visibleCharCount = 0;
        boolean inBold = false;
        boolean inSize = false;
        boolean inFont = false;

        for (int i = 0; i < text.length() && visibleCharCount < maxLength; i++) {
            if (text.startsWith("[BOLD]", i)) {
                result.append("[BOLD]");
                inBold = true;
                i += 5;
            } else if (text.startsWith("[/BOLD]", i)) {
                result.append("[/BOLD]");
                inBold = false;
                i += 6;
            } else if (text.startsWith("[SIZE=", i)) {
                int endIndex = text.indexOf("]", i);
                if (endIndex != -1) {
                    result.append(text.substring(i, endIndex + 1));
                    inSize = true;
                    i = endIndex;
                } else {
                    result.append(text.charAt(i));
                }
            } else if (text.startsWith("[/SIZE]", i)) {
                result.append("[/SIZE]");
                inSize = false;
                i += 6;
            } else if (text.startsWith("[FONT=", i)) {
                int endIndex = text.indexOf("]", i);
                if (endIndex != -1) {
                    result.append(text.substring(i, endIndex + 1));
                    inFont = true;
                    i = endIndex;
                } else {
                    result.append(text.charAt(i));
                }
            } else if (text.startsWith("[/FONT]", i)) {
                result.append("[/FONT]");
                inFont = false;
                i += 6;
            } else {
                result.append(text.charAt(i));
                visibleCharCount++;
            }
        }

        // Close open tags
        if (inBold) {
            result.append("[/BOLD]");
        }
        if (inSize) {
            result.append("[/SIZE]");
        }
        if (inFont) {
            result.append("[/FONT]");
        }

        return result.toString();
    }

    private String centerTextTSP100(String text) {
        int cleanTextLength = getVisibleTextLength(text);
        if (cleanTextLength >= TSP100_CHAR_WIDTH) {
            return truncateWithFormatting(text, TSP100_CHAR_WIDTH);
        }
        int padding = (TSP100_CHAR_WIDTH - cleanTextLength) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

// Replace your existing rightAlignTextTSP100 method:
    private String rightAlignTextTSP100(String text) {
        int cleanTextLength = getVisibleTextLength(text);
        if (cleanTextLength >= TSP100_CHAR_WIDTH) {
            return truncateWithFormatting(text, TSP100_CHAR_WIDTH);
        }
        int padding = TSP100_CHAR_WIDTH - cleanTextLength;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private String createLineTSP100() {
        return "-".repeat(TSP100_CHAR_WIDTH);
    }

    private void showFormattingHelp() {
        String helpText = """
        =============================================
        THERMAL RECEIPT PRINTER - FORMATTING HELP
        =============================================
        
        [CENTER]TEXT[/CENTER]    - Centers the text on the line
        [RIGHT]TEXT[/RIGHT]      - Right-aligns the text on the line
        [LINE]                  - Creates a horizontal divider line
        [BOLD]TEXT[/BOLD]        - Makes text bold
        [SIZE=16]TEXT[/SIZE]     - Changes text size (8-24)
        [FONT=Arial]TEXT[/FONT]  - Changes font family
        
        ==============
        TEXT FORMATTING
        ==============
        • Select text and click 'B' button or press Ctrl+B to make it bold
        • Use the font size spinner to change text size
        • Choose from 50+ font families in the dropdown
        
        ============
        TABLE FORMAT
        ============
        | Item Name      | Qty | Price |
        |----------------|-----|-------|
        | Coffee         | 2   | $4.50 |
        | Sandwich       | 1   | $7.25 |
        
        =============
        RECEIPT WIDTH
        =============
        • Standard 80mm thermal paper: 42 characters
        • Wider receipts may need adjustment
        
        ===========
        KEY COMMANDS
        ===========
        Ctrl+N - New receipt
        Ctrl+T - Templates
        Ctrl+P - Print
        Ctrl+B - Bold text
        Ctrl+U - Plain text
        
        ======
        TIPS
        ======
        • Use empty lines for spacing
        • Commands must be at line start
        • Preview shows exact print layout
        • Test print on paper before production use
        """;

        JTextArea helpArea = new JTextArea(helpText);
        helpArea.setEditable(false);
        helpArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        helpArea.setBackground(new Color(240, 240, 240));
        helpArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(helpArea);
        scrollPane.setPreferredSize(new Dimension(550, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "Formatting Help & Quick Reference", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String aboutText = """
            Thermal Receipt Printer v1.1
            
            A simple application for creating and printing
            thermal receipts with template support and rich text formatting.
            
            Features:
            • Real-time preview
            • Template management
            • Custom formatting commands
            • Advanced text formatting
            • Custom font size support
            • Multiple font Style support
            • Multiple alignment options
            • Print support
            
            Perfect for small businesses, cafes, and retail stores.
            """;

        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePreview() {
        // Update font first
        updatePreviewFont();

        String formattedText = getFormattedText();
        String processedText = formatForTSP100(formattedText);

        StyledDocument doc = previewTextPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            // Process with scaled font sizes
            Pattern pattern = Pattern.compile("(\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=(\\d+)\\]|\\[/SIZE\\]|\\[FONT=([^\\]]+)\\]|\\[/FONT\\])");
            Matcher matcher = pattern.matcher(processedText);

            int lastIndex = 0;
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, "Courier New");
            StyleConstants.setFontSize(attrs, (int) (fontSize * 1.25));
            StyleConstants.setBold(attrs, false);

            while (matcher.find()) {
                // Insert text before the tag
                if (matcher.start() > lastIndex) {
                    String text = processedText.substring(lastIndex, matcher.start());
                    doc.insertString(doc.getLength(), text, attrs);
                }

                // Process the tag
                String tag = matcher.group(0);
                if (tag.equals("[BOLD]")) {
                    StyleConstants.setBold(attrs, true);
                } else if (tag.equals("[/BOLD]")) {
                    StyleConstants.setBold(attrs, false);
                } else if (tag.startsWith("[SIZE=")) {
                    try {
                        int size = Integer.parseInt(matcher.group(2));
                        StyleConstants.setFontSize(attrs, (int) (size * 1.25));
                    } catch (NumberFormatException e) {
                        StyleConstants.setFontSize(attrs, (int) (fontSize * 1.25));
                    }
                } else if (tag.equals("[/SIZE]")) {
                    StyleConstants.setFontSize(attrs, (int) (fontSize * 1.25));
                } else if (tag.startsWith("[FONT=")) {
                    StyleConstants.setFontFamily(attrs, matcher.group(3));
                } else if (tag.equals("[/FONT]")) {
                    StyleConstants.setFontFamily(attrs, "Courier New");
                }

                lastIndex = matcher.end();
            }

            // Insert remaining text after last tag
            if (lastIndex < processedText.length()) {
                doc.insertString(doc.getLength(), processedText.substring(lastIndex), attrs);
            }

            // Apply line spacing
            int length = doc.getLength();
            for (int i = 0; i < length;) {
                Element elem = doc.getParagraphElement(i);
                SimpleAttributeSet paraAttrs = new SimpleAttributeSet();
                StyleConstants.setLineSpacing(paraAttrs, lineSpacing - 1.0f);
                doc.setParagraphAttributes(elem.getStartOffset(),
                        elem.getEndOffset() - elem.getStartOffset(),
                        paraAttrs, false);
                i = elem.getEndOffset();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void clearReceipt() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the current receipt?",
                "Clear Receipt",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            receiptTextPane.setText("");

            // Reset default styles for new text input
            StyledDocument doc = receiptTextPane.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, "Courier New");
            StyleConstants.setFontSize(attrs, 14); // Default font size
            StyleConstants.setBold(attrs, false); // Default to not bold
            receiptTextPane.setCharacterAttributes(attrs, true); // Apply to entire document

            // Reset all UI controls to their default values
            fontSizeSpinner.setValue(12);
            alignmentCombo.setSelectedItem("LEFT");
            customFontSizeSpinner.setValue(14);
            fontStyleCombo.setSelectedItem("Courier New");
            lineSpacingSpinner.setValue(1.0);

            // Reset instance variables to defaults
            fontSize = 12;
            alignment = "LEFT";
            currentFontStyle = "Courier New";
            lineSpacing = 1.0f;
            isok = false;
            // Update format buttons appearance
            updateFormatButtons();

            updatePreview();
        }
    }

    private void openTemplateManager() {
        TemplateManagerDialog dialog = new TemplateManagerDialog(this);
        dialog.setVisible(true);
    }

    private void printReceipt() {
        try {
            String content = getFormattedText();

            if (content.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No content to print. Please enter some text first.",
                        "Nothing to Print",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            PrinterJob job = PrinterJob.getPrinterJob();

            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new Copies(1));
            attributes.add(MediaSizeName.INVOICE);
            attributes.add(PrintQuality.HIGH);
            fontSize = (Integer) fontSizeSpinner.getValue();
            job.setPrintable(new ReceiptPrintable(content, fontSize, alignment));

            if (job.printDialog(attributes)) {
                job.print(attributes);
                JOptionPane.showMessageDialog(this, "Receipt sent to printer!", "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage(), "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getReceiptContent() {
        return getFormattedText();
    }

    public void setReceiptContent(String content) {
        receiptTextPane.setText("");
        StyledDocument doc = receiptTextPane.getStyledDocument();

        SimpleAttributeSet defaultAttrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(defaultAttrs, "Courier New");
        StyleConstants.setFontSize(defaultAttrs, 14);
        StyleConstants.setBold(defaultAttrs, false);

        // Enhanced pattern to handle all formatting tags
        Pattern pattern = Pattern.compile("(\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=(\\d+)\\]|\\[/SIZE\\]|\\[FONT=([^\\]]+)\\]|\\[/FONT\\])");
        Matcher matcher = pattern.matcher(content);

        int lastIndex = 0;
        boolean currentBold = false;
        int currentSize = 14;
        String currentFont = "Courier New";

        try {
            while (matcher.find()) {
                // Insert text before the tag
                if (matcher.start() > lastIndex) {
                    String text = content.substring(lastIndex, matcher.start());
                    SimpleAttributeSet attrs = new SimpleAttributeSet(defaultAttrs);
                    StyleConstants.setBold(attrs, currentBold);
                    StyleConstants.setFontSize(attrs, currentSize);
                    StyleConstants.setFontFamily(attrs, currentFont);
                    doc.insertString(doc.getLength(), text, attrs);
                }

                // Process the tag
                String tag = matcher.group(0);
                if (tag.equals("[BOLD]")) {
                    currentBold = true;
                } else if (tag.equals("[/BOLD]")) {
                    currentBold = false;
                } else if (tag.startsWith("[SIZE=")) {
                    try {
                        currentSize = Integer.parseInt(matcher.group(2));
                    } catch (NumberFormatException e) {
                        currentSize = 14;
                    }
                } else if (tag.equals("[/SIZE]")) {
                    currentSize = 14;
                } else if (tag.startsWith("[FONT=")) {
                    currentFont = matcher.group(3);
                } else if (tag.equals("[/FONT]")) {
                    currentFont = "Courier New";
                }
                lastIndex = matcher.end();
            }

            // Insert remaining text
            if (lastIndex < content.length()) {
                String text = content.substring(lastIndex);
                SimpleAttributeSet attrs = new SimpleAttributeSet(defaultAttrs);
                StyleConstants.setBold(attrs, currentBold);
                StyleConstants.setFontSize(attrs, currentSize);
                StyleConstants.setFontFamily(attrs, currentFont);
                doc.insertString(doc.getLength(), text, attrs);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
            // Fallback: just insert plain text without formatting
            receiptTextPane.setText(content.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", ""));
        }

        receiptTextPane.setCaretPosition(0);
        updatePreview();
    }

    private class ReceiptPrintable implements Printable {

        private String content;
        private int fontSize;
        private String alignment;
        private float lineSpacing;
        private final int TSP100_PRINT_WIDTH = 42;
        private final int THERMAL_PRINT_FONT_SIZE;

        public ReceiptPrintable(String content, int fontSize, String alignment) {
            this.content = content;
            this.fontSize = fontSize;
            this.alignment = alignment;
            this.lineSpacing = MainFrame.this.lineSpacing; // Get line spacing from main frame
            this.THERMAL_PRINT_FONT_SIZE = (Integer) fontSizeSpinner.getValue();
        }

        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

            Font baseNormalFont = new Font("Courier New", Font.PLAIN, THERMAL_PRINT_FONT_SIZE);
            Font baseBoldFont = new Font("Courier New", Font.BOLD, THERMAL_PRINT_FONT_SIZE);

            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics(baseNormalFont);
            int baseLineHeight = fm.getHeight();
            int adjustedLineHeight = (int) (baseLineHeight * lineSpacing); // Apply line spacing
            double availableWidth = pf.getImageableWidth();

            String formattedContent = formatForTSP100Print(content, TSP100_PRINT_WIDTH);
            String[] lines = formattedContent.split("\n");

            int y = adjustedLineHeight;

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    y += adjustedLineHeight / 2;
                    continue;
                }

                printFormattedLine(g2d, line, 0, y, baseNormalFont, baseBoldFont, availableWidth);
                y += adjustedLineHeight;

                if (y > pf.getImageableHeight()) {
                    break;
                }
            }

            return PAGE_EXISTS;
        }

        private void printFormattedLine(Graphics2D g2d, String line, int x, int y,
                Font baseNormalFont, Font baseBoldFont, double availableWidth) {

            int currentX = x;
            boolean currentBold = false;
            int currentFontSize = THERMAL_PRINT_FONT_SIZE;
            String currentFontFamily = "Courier New";
            String allCurrentFontFamily = allSetFontStyle;

            System.out.println(allCurrentFontFamily);

            if (isok) {
                if (allCurrentFontFamily != "Courier New") {
                    System.out.println("ok");
                    currentFontFamily = allCurrentFontFamily;
                }
            }

            // Enhanced pattern to capture font names with spaces and special characters
            Pattern printPattern = Pattern.compile("(\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=(\\d+)\\]|\\[/SIZE\\]|\\[FONT=([^\\]]+)\\]|\\[/FONT\\])");
            Matcher printMatcher = printPattern.matcher(line);

            int lastIndex = 0;

            while (printMatcher.find()) {
                // Draw text before the tag
                if (printMatcher.start() > lastIndex) {
                    String textToDraw = line.substring(lastIndex, printMatcher.start());
                    if (!textToDraw.isEmpty()) {
                        // Create and set font before drawing
                        Font fontToUse = createPrintFont(currentFontFamily, currentBold, currentFontSize);
                        g2d.setFont(fontToUse);

                        g2d.drawString(textToDraw, currentX, y);
                        currentX += g2d.getFontMetrics(fontToUse).stringWidth(textToDraw);
                    }
                }

                String tag = printMatcher.group(0);
                if (tag.equals("[BOLD]")) {
                    currentBold = true;
                } else if (tag.equals("[/BOLD]")) {
                    currentBold = false;
                } else if (tag.startsWith("[SIZE=")) {
                    try {
                        currentFontSize = Integer.parseInt(printMatcher.group(2));
                    } catch (NumberFormatException e) {
                        currentFontSize = THERMAL_PRINT_FONT_SIZE;
                    }
                } else if (tag.equals("[/SIZE]")) {
                    currentFontSize = THERMAL_PRINT_FONT_SIZE;
                } else if (tag.startsWith("[FONT=")) {
                    String extractedFont = printMatcher.group(3);
                    if (extractedFont != null && !extractedFont.trim().isEmpty()) {
                        currentFontFamily = validateAndGetFont(extractedFont.trim());
                    }
                } else if (tag.equals("[/FONT]")) {
                    currentFontFamily = "Courier New";
                }

                lastIndex = printMatcher.end();
            }

            // Draw remaining text after all tags are processed
            if (lastIndex < line.length()) {
                String textToDraw = line.substring(lastIndex);
                if (!textToDraw.isEmpty()) {
                    Font fontToUse = createPrintFont(currentFontFamily, currentBold, currentFontSize);
                    g2d.setFont(fontToUse);
                    g2d.drawString(textToDraw, currentX, y);
                }
            }
        }

        private Font createPrintFont(String fontFamily, boolean bold, int fontSize) {
            int fontStyle = bold ? Font.BOLD : Font.PLAIN;

            try {
                // Validate font first
                String validatedFont = validateAndGetFont(fontFamily);

                // Create font with validated name
                Font font = new Font(validatedFont, fontStyle, fontSize);

                // Test if the font was actually created correctly
                if (!font.getFamily().equalsIgnoreCase("Dialog")) { // Dialog is Java's fallback
                    System.out.println("Successfully created font: " + font.getFontName());
                    return font;
                } else {
                    System.out.println("Font fell back to Dialog, using Courier New instead");
                    return new Font("Courier New", fontStyle, fontSize);
                }

            } catch (Exception e) {
                System.err.println("Error creating font '" + fontFamily + "': " + e.getMessage());
                return new Font("Courier New", fontStyle, fontSize);
            }
        }

        private String validateAndGetFont(String requestedFont) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFonts = ge.getAvailableFontFamilyNames();

            // Check if the exact font exists
            for (String availableFont : availableFonts) {
                if (availableFont.equalsIgnoreCase(requestedFont)) {
                    return availableFont;
                }
            }

            // Check for partial matches (useful for fonts with versions)
            for (String availableFont : availableFonts) {
                if (availableFont.toLowerCase().contains(requestedFont.toLowerCase())
                        || requestedFont.toLowerCase().contains(availableFont.toLowerCase())) {
                    System.out.println("Using closest match: " + availableFont + " for requested: " + requestedFont);
                    return availableFont;
                }
            }

            // For common substitutions
            String lowerFont = requestedFont.toLowerCase();
            if (lowerFont.contains("arial")) {
                return "Arial";
            } else if (lowerFont.contains("times")) {
                return "Times New Roman";
            } else if (lowerFont.contains("courier")) {
                return "Courier New";
            }

            // Last resort - return Courier New for thermal printer compatibility
            System.out.println("Font '" + requestedFont + "' not found, using Courier New");
            return "Courier New";
        }
// Also add this method to help debug font issues:

        private String formatForTSP100Print(String input, int maxWidth) {
            if (input.isEmpty()) {
                return "";
            }

            StringBuilder formatted = new StringBuilder();
            String[] lines = input.split("\n");

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    formatted.append("\n");
                    continue;
                }

                if (line.startsWith("[CENTER]")) {
                    String content = line.substring(8);
                    formatted.append(centerTextForPrint(content, maxWidth)).append("\n");
                } else if (line.startsWith("[RIGHT]")) {
                    String content = line.substring(7);
                    formatted.append(rightAlignTextForPrint(content, maxWidth)).append("\n");
                } else if (line.startsWith("[LINE]")) {
                    formatted.append("-".repeat(maxWidth)).append("\n");
                } else {
                    if ("CENTER".equals(this.alignment)) {
                        formatted.append(centerTextForPrint(line, maxWidth)).append("\n");
                    } else if ("RIGHT".equals(this.alignment)) {
                        formatted.append(rightAlignTextForPrint(line, maxWidth)).append("\n");
                    } else {
                        String processedLine = processLineLength(line, maxWidth);
                        formatted.append(processedLine).append("\n");
                    }
                }
            }

            return formatted.toString();
        }

        private String processLineLength(String line, int maxWidth) {
            int cleanLineLength = getVisibleTextLengthForPrint(line);
            if (cleanLineLength <= maxWidth) {
                return line;
            }

            StringBuilder result = new StringBuilder();
            int charCount = 0;
            boolean inBold = false;
            boolean inCustomSize = false;
            boolean inCustomFont = false;

            for (int i = 0; i < line.length() && charCount < maxWidth; i++) {
                if (line.startsWith("[BOLD]", i)) {
                    result.append("[BOLD]");
                    i += 5;
                    inBold = true;
                } else if (line.startsWith("[/BOLD]", i)) {
                    result.append("[/BOLD]");
                    i += 6;
                    inBold = false;
                } else if (line.startsWith("[SIZE=", i)) {
                    int endIndex = line.indexOf("]", i);
                    if (endIndex != -1) {
                        result.append(line.substring(i, endIndex + 1));
                        i = endIndex;
                        inCustomSize = true;
                    } else {
                        result.append(line.charAt(i));
                        charCount++;
                    }
                } else if (line.startsWith("[/SIZE]", i)) {
                    result.append("[/SIZE]");
                    i += 6;
                    inCustomSize = false;
                } else if (line.startsWith("[FONT=", i)) {
                    int endIndex = line.indexOf("]", i);
                    if (endIndex != -1) {
                        result.append(line.substring(i, endIndex + 1));
                        i = endIndex;
                        inCustomFont = true;
                    } else {
                        result.append(line.charAt(i));
                        charCount++;
                    }
                } else if (line.startsWith("[/FONT]", i)) {
                    result.append("[/FONT]");
                    i += 6;
                    inCustomFont = false;
                } else {
                    result.append(line.charAt(i));
                    charCount++;
                }
            }

            // Close any open formatting tags
            if (inBold) {
                result.append("[/BOLD]");
            }
            if (inCustomSize) {
                result.append("[/SIZE]");
            }
            if (inCustomFont) {
                result.append("[/FONT]");
            }

            return result.toString();
        }

        private String centerTextForPrint(String text, int width) {
            int cleanTextLength = getVisibleTextLengthForPrint(text);
            if (cleanTextLength >= width) {
                return processLineLength(text, width);
            }
            int padding = (width - cleanTextLength) / 2;
            return " ".repeat(Math.max(0, padding)) + text;
        }

        private String rightAlignTextForPrint(String text, int width) {
            int cleanTextLength = getVisibleTextLengthForPrint(text);
            if (cleanTextLength >= width) {
                return processLineLength(text, width);
            }
            int padding = width - cleanTextLength;
            return " ".repeat(Math.max(0, padding)) + text;
        }

        private int getVisibleTextLengthForPrint(String text) {
            return text.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", "").length();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                FlatLightLaf.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}

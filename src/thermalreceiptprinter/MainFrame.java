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
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    // Logo support
    private BufferedImage logoImage = null;
    private String logoPath = null;
    private JButton addLogoButton;
    private JButton removeLogoButton;
    private JLabel logoPreviewLabel;
    private JSpinner logoWidthSpinner;
    private JComboBox<String> logoAlignmentCombo;

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

        // Create split pane for resizable panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(1.0); // Left panel gets extra space
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(380); // Initial divider location

        add(splitPane, BorderLayout.CENTER);

        setSize(900, 730);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newMenuItem = new JMenuItem("New Receipt");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newMenuItem.addActionListener(e -> {
            clearReceipt();
        });

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

        JMenu imageMenu = new JMenu("Image");

        JMenuItem addLogoMenuItem = new JMenuItem("Add Logo...");
        addLogoMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        addLogoMenuItem.addActionListener(e -> addLogo());

        JMenuItem removeLogoMenuItem = new JMenuItem("Remove Logo");
        removeLogoMenuItem.addActionListener(e -> removeLogo());

        imageMenu.add(addLogoMenuItem);
        imageMenu.add(removeLogoMenuItem);

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
        menuBar.add(imageMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Receipt Editor"));
        panel.setMinimumSize(new Dimension(350, 600));

        receiptTextPane = new JTextPane();
        receiptTextPane.setFont(new Font("Courier New", Font.PLAIN, 14));

        StyledDocument doc = receiptTextPane.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "Courier New");
        StyleConstants.setFontSize(defaultStyle, 14);

        JScrollPane scrollPane = new JScrollPane(receiptTextPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel controlsPanel = createControlsPanel();
        JPanel logoPanel = createLogoPanel();
        JPanel formattingPanel = createFormattingPanel();
        JPanel buttonsPanel = createButtonsPanel();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlsPanel, BorderLayout.NORTH);

        // Create collapsible panels
        JPanel collapsibleSection = new JPanel(new BorderLayout());
        collapsibleSection.add(createCollapsiblePanel("Logo Settings", logoPanel), BorderLayout.NORTH);
        collapsibleSection.add(createCollapsiblePanel("Text Formatting", formattingPanel), BorderLayout.CENTER);

        topPanel.add(collapsibleSection, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCollapsiblePanel(String title, JPanel contentPanel) {
        JPanel wrapper = new JPanel(new BorderLayout());

        // Header panel with toggle button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        headerPanel.setBackground(new Color(240, 240, 240));

        JButton toggleButton = new JButton(title);
        toggleButton.setHorizontalAlignment(SwingConstants.LEFT);
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFont(new Font("Arial", Font.BOLD, 11));
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel arrowLabel = new JLabel("▼");
        arrowLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        headerPanel.add(toggleButton, BorderLayout.CENTER);
        headerPanel.add(arrowLabel, BorderLayout.EAST);

        // Content panel (initially visible)
        contentPanel.setVisible(true);

        // Toggle action
        toggleButton.addActionListener(e -> {
            boolean isVisible = contentPanel.isVisible();
            contentPanel.setVisible(!isVisible);
            arrowLabel.setText(isVisible ? "▶" : "▼");
            wrapper.revalidate();
            wrapper.repaint();
        });

        wrapper.add(headerPanel, BorderLayout.NORTH);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        wrapper.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return wrapper;
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));

        addLogoButton = new JButton("Add Logo");
        addLogoButton.setFont(new Font("Arial", Font.PLAIN, 10));
        addLogoButton.setPreferredSize(new Dimension(80, 22));
        addLogoButton.setToolTipText("Add a logo image to the receipt");
        addLogoButton.setFocusPainted(false);
        addLogoButton.addActionListener(e -> addLogo());

        Image originalImage = new ImageIcon(this.getClass().getResource("/images/trash-can.png")).getImage();
        Image scaledImage = originalImage.getScaledInstance(15, 15, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        removeLogoButton = new JButton(scaledIcon);
        removeLogoButton.setFont(new Font("Arial", Font.PLAIN, 10));
        removeLogoButton.setPreferredSize(new Dimension(40, 22));
        removeLogoButton.setToolTipText("Remove the logo");
        removeLogoButton.setFocusPainted(false);
        removeLogoButton.setEnabled(false);
        removeLogoButton.addActionListener(e -> removeLogo());

        logoWidthSpinner = new JSpinner(new SpinnerNumberModel(150, 50, 300, 10));
        logoWidthSpinner.setPreferredSize(new Dimension(60, 22));
        logoWidthSpinner.setToolTipText("Logo width in pixels");
        logoWidthSpinner.setEnabled(false);
        logoWidthSpinner.addChangeListener(e -> updatePreview());

        String[] alignments = {"LEFT", "CENTER", "RIGHT"};
        logoAlignmentCombo = new JComboBox<>(alignments);
        logoAlignmentCombo.setSelectedItem("CENTER");
        logoAlignmentCombo.setPreferredSize(new Dimension(80, 22));
        logoAlignmentCombo.setToolTipText("Logo alignment");
        logoAlignmentCombo.setEnabled(false);
        logoAlignmentCombo.addActionListener(e -> updatePreview());

        logoPreviewLabel = new JLabel("No logo");
        logoPreviewLabel.setForeground(Color.GRAY);
        logoPreviewLabel.setFont(new Font("Arial", Font.ITALIC, 10));

        controlsRow.add(addLogoButton);
        controlsRow.add(removeLogoButton);
        controlsRow.add(Box.createHorizontalStrut(5));
        controlsRow.add(new JLabel("Width:"));
        controlsRow.add(logoWidthSpinner);
        controlsRow.add(Box.createHorizontalStrut(5));
        controlsRow.add(new JLabel("Align:"));
        controlsRow.add(logoAlignmentCombo);
        controlsRow.add(Box.createHorizontalStrut(10));
        controlsRow.add(logoPreviewLabel);

        panel.add(controlsRow, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormattingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

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
        panel.setPreferredSize(new Dimension(320, 500));
        panel.setMinimumSize(new Dimension(320, 400));
        panel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));

        previewTextPane = new JTextPane();
        previewTextPane.setEditable(false);
        previewTextPane.setBackground(Color.WHITE);
        previewTextPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initPreviewTextPane();

        JScrollPane previewScroll = new JScrollPane(previewTextPane);
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

    private void addLogo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Logo Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg")
                        || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image Files (*.png, *.jpg, *.jpeg, *.gif, *.bmp)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                logoImage = ImageIO.read(selectedFile);
                logoPath = selectedFile.getAbsolutePath();

                logoPreviewLabel.setText(selectedFile.getName());
                logoPreviewLabel.setForeground(new Color(46, 204, 113));

                removeLogoButton.setEnabled(true);
                logoWidthSpinner.setEnabled(true);
                logoAlignmentCombo.setEnabled(true);

                updatePreview();

                JOptionPane.showMessageDialog(this,
                        "Logo added successfully!\nIt will appear at the top of your receipt.",
                        "Logo Added",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to load image: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeLogo() {
        logoImage = null;
        logoPath = null;

        logoPreviewLabel.setText("No logo");
        logoPreviewLabel.setForeground(Color.GRAY);

        removeLogoButton.setEnabled(false);
        logoWidthSpinner.setEnabled(false);
        logoAlignmentCombo.setEnabled(false);

        updatePreview();
    }

    private void setupEventListeners() {
        // Replace the existing document listener with this:
        setupAutoWrapListener();  // FIX #3: Use new auto-wrap listener

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
            updatePreviewFont();
            updatePreview();
        });
    }

    private void initPreviewTextPane() {
        previewTextPane = new JTextPane() {
            @Override
            public FontMetrics getFontMetrics(Font font) {
                Font previewFont = font.deriveFont((float) (font.getSize() * 1.15));
                return super.getFontMetrics(previewFont);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw logo at the top if available
                if (logoImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    int logoWidth = (Integer) logoWidthSpinner.getValue();
                    int logoHeight = (int) (logoImage.getHeight() * ((double) logoWidth / logoImage.getWidth()));

                    int x = 10; // Default left
                    String logoAlign = (String) logoAlignmentCombo.getSelectedItem();
                    int componentWidth = getWidth() - 20; // Account for padding

                    if ("CENTER".equals(logoAlign)) {
                        x = 10 + (componentWidth - logoWidth) / 2;
                    } else if ("RIGHT".equals(logoAlign)) {
                        x = 10 + componentWidth - logoWidth;
                    }

                    g2d.drawImage(logoImage, x, 10, logoWidth, logoHeight, null);
                    g2d.dispose();
                }
            }
        };

        previewTextPane.setEditable(false);
        previewTextPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        previewTextPane.setFont(new Font("Courier New", Font.PLAIN, (int) (fontSize * 1.15)));
    }

    private void setupAutoWrapListener() {
        receiptTextPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> checkAndWrapText(e));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
                updateBoldButton();
                updateFormatButtons();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
                updateBoldButton();
                updateFormatButtons();
            }
        });
    }

    private void checkAndWrapText(DocumentEvent e) {
        try {
            StyledDocument doc = receiptTextPane.getStyledDocument();
            int offset = e.getOffset();

            // Find the current line
            Element root = doc.getDefaultRootElement();
            int lineIndex = root.getElementIndex(offset);
            Element line = root.getElement(lineIndex);

            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset();
            String lineText = doc.getText(lineStart, lineEnd - lineStart);

            // Remove formatting tags to count actual characters
            String cleanText = lineText.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", "");

            // Check if line exceeds 42 characters (excluding newline)
            if (cleanText.length() > 42 && !cleanText.trim().isEmpty()) {
                // Find the last space within 42 characters
                int wrapPosition = 42;
                for (int i = 42; i >= 0; i--) {
                    if (i < cleanText.length() && cleanText.charAt(i) == ' ') {
                        wrapPosition = i;
                        break;
                    }
                }

                // If no space found, wrap at exactly 42 characters
                if (wrapPosition == 42 || cleanText.charAt(wrapPosition) != ' ') {
                    wrapPosition = Math.min(42, cleanText.length());
                }

                // Insert newline at wrap position if not already at end of line
                if (wrapPosition < cleanText.length() - 1) {
                    int actualPosition = lineStart + wrapPosition;

                    // Preserve formatting attributes
                    AttributeSet attrs = doc.getCharacterElement(actualPosition).getAttributes();

                    // Check if there's already a newline
                    if (!cleanText.substring(wrapPosition, wrapPosition + 1).equals("\n")) {
                        doc.insertString(actualPosition, "\n", attrs);
                    }
                }
            }

            updatePreview();
            updateBoldButton();
            updateFormatButtons();

        } catch (BadLocationException ex) {
            // Silently handle - text is being modified
        }
    }

    private void updatePreviewFont() {
        Font baseFont = new Font("Courier New", Font.PLAIN, fontSize);
        Font previewFont = baseFont.deriveFont((float) (fontSize * 1.25));
        previewTextPane.setFont(previewFont);
    }

    private class EnhancedFontComboBoxRenderer extends DefaultListCellRenderer {

        private static final int DISPLAY_FONT_SIZE = 14;
        private final Set<String> availableFonts;

        public EnhancedFontComboBoxRenderer() {
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
            StyleConstants.setFontSize(attrs, 14);
            StyleConstants.setBold(attrs, false);

            doc.insertString(0, plainText, attrs);

            MutableAttributeSet inputAttrs = receiptTextPane.getInputAttributes();
            inputAttrs.removeAttributes(inputAttrs);
            StyleConstants.setFontFamily(inputAttrs, selectedFont);
            StyleConstants.setFontSize(inputAttrs, 14);
            StyleConstants.setBold(inputAttrs, false);

            allSetFontStyle = selectedFont;
            isok = true;
            updatePreview();

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
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            StyleConstants.setFontFamily(attrs, selectedFont);
            currentFontStyle = selectedFont;
        } else {
            try {
                for (int i = start; i < end; i++) {
                    AttributeSet existingAttrs = doc.getCharacterElement(i).getAttributes();
                    SimpleAttributeSet newAttrs = new SimpleAttributeSet(existingAttrs);
                    StyleConstants.setFontFamily(newAttrs, selectedFont);
                    doc.setCharacterAttributes(i, 1, newAttrs, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                SimpleAttributeSet fallbackAttrs = new SimpleAttributeSet();
                StyleConstants.setFontFamily(fallbackAttrs, selectedFont);
                doc.setCharacterAttributes(start, end - start, fallbackAttrs, false);
            }

            MutableAttributeSet inputAttrs = receiptTextPane.getInputAttributes();
            StyleConstants.setFontFamily(inputAttrs, selectedFont);
            currentFontStyle = selectedFont;

            receiptTextPane.setSelectionStart(start);
            receiptTextPane.setSelectionEnd(end);
        }

        receiptTextPane.requestFocusInWindow();
        updatePreview();
    }

    private void applyLineSpacing() {
        lineSpacing = ((Double) lineSpacingSpinner.getValue()).floatValue();

        StyledDocument doc = receiptTextPane.getStyledDocument();

        int start = 0;
        int end = doc.getLength();

        Font font = receiptTextPane.getFont();
        FontMetrics fm = receiptTextPane.getFontMetrics(font);
        int lineHeight = (int) (fm.getHeight() * lineSpacing);

        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(paragraphAttrs, 0);
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
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            int selectedFontSize = (Integer) customFontSizeSpinner.getValue();
            StyleConstants.setFontSize(attrs, selectedFontSize);
        } else {
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
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            StyleConstants.setBold(attrs, true);
        } else {
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
            MutableAttributeSet attrs = receiptTextPane.getInputAttributes();
            StyleConstants.setBold(attrs, false);
        } else {
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
            return StyleConstants.isBold(receiptTextPane.getInputAttributes());
        } else {
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
            return !StyleConstants.isBold(receiptTextPane.getInputAttributes());
        } else {
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

        SwingUtilities.invokeLater(() -> {
            customFontSizeSpinner.setValue(fontSize);
        });
    }

    private void updateBoldButton() {
        boolean isBold = isSelectionBold();
        boldButton.setBackground(isBold ? new Color(200, 200, 255) : null);
        boldButton.setOpaque(isBold);
    }

    private String getFormattedText() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        StringBuilder result = new StringBuilder();

        try {
            String text = doc.getText(0, doc.getLength());

            boolean inBold = false;
            int currentFontSize = 14;
            String currentFont = "Courier New";

            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                AttributeSet attrs = doc.getCharacterElement(i).getAttributes();

                boolean charIsBold = StyleConstants.isBold(attrs);
                int charFontSize = StyleConstants.getFontSize(attrs);
                String charFontFamily = StyleConstants.getFontFamily(attrs);

                if (charFontFamily == null || charFontFamily.trim().isEmpty()) {
                    charFontFamily = "Courier New";
                }

                if (charIsBold != inBold) {
                    if (charIsBold) {
                        result.append("[BOLD]");
                        inBold = true;
                    } else {
                        result.append("[/BOLD]");
                        inBold = false;
                    }
                }

                if (charFontSize != currentFontSize) {
                    if (currentFontSize != 14) {
                        result.append("[/SIZE]");
                    }
                    if (charFontSize != 14) {
                        result.append("[SIZE=").append(charFontSize).append("]");
                    }
                    currentFontSize = charFontSize;
                }

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

            if (line.startsWith("[BOLD]") || line.startsWith("[/BOLD]")
                    || line.startsWith("[SIZE=") || line.startsWith("[/SIZE]")
                    || line.startsWith("[FONT=") || line.startsWith("[/FONT]")) {
                formatted.append(line).append("\n");
                continue;
            }

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
        return text.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", "").length();
    }

    private int getSelectionFontSize() {
        StyledDocument doc = receiptTextPane.getStyledDocument();
        int start = receiptTextPane.getSelectionStart();
        int end = receiptTextPane.getSelectionEnd();

        if (start == end) {
            return StyleConstants.getFontSize(receiptTextPane.getInputAttributes());
        } else {
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
        
        ==========
        LOGO SUPPORT
        ==========
        • Add logos with Ctrl+L or Image menu
        • Supports PNG, JPG, GIF, BMP formats
        • Adjust logo width and alignment
        • Logo prints at top of receipt
        
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
        Ctrl+L - Add logo
        
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
        scrollPane.setPreferredSize(new Dimension(550, 550));

        JOptionPane.showMessageDialog(this, scrollPane, "Formatting Help & Quick Reference", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String aboutText = """
            Thermal Receipt Printer v1.2
            
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
            • Logo image support
            • Print support
            
            Perfect for small businesses, cafes, and retail stores.
            """;

        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePreview() {
        updatePreviewFont();

        String formattedText = getFormattedText();
        String processedText = formatForTSP100(formattedText);

        StyledDocument doc = previewTextPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            // Add spacing for logo if present
            if (logoImage != null) {
                int logoWidth = (Integer) logoWidthSpinner.getValue();
                int logoHeight = (int) (logoImage.getHeight() * ((double) logoWidth / logoImage.getWidth()));
                int linesForLogo = (logoHeight / 15) + 2; // Approximate lines needed

                for (int i = 0; i < linesForLogo; i++) {
                    doc.insertString(doc.getLength(), "\n", null);
                }
            }

            Pattern pattern = Pattern.compile("(\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=(\\d+)\\]|\\[/SIZE\\]|\\[FONT=([^\\]]+)\\]|\\[/FONT\\])");
            Matcher matcher = pattern.matcher(processedText);

            int lastIndex = 0;
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, "Courier New");
            StyleConstants.setFontSize(attrs, (int) (fontSize * 1.25));
            StyleConstants.setBold(attrs, false);

            while (matcher.find()) {
                if (matcher.start() > lastIndex) {
                    String text = processedText.substring(lastIndex, matcher.start());
                    doc.insertString(doc.getLength(), text, attrs);
                }

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

            if (lastIndex < processedText.length()) {
                doc.insertString(doc.getLength(), processedText.substring(lastIndex), attrs);
            }

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

            previewTextPane.repaint();
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

            StyledDocument doc = receiptTextPane.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, "Courier New");
            StyleConstants.setFontSize(attrs, 14);
            StyleConstants.setBold(attrs, false);
            receiptTextPane.setCharacterAttributes(attrs, true);

            fontSizeSpinner.setValue(12);
            alignmentCombo.setSelectedItem("LEFT");
            customFontSizeSpinner.setValue(14);
            fontStyleCombo.setSelectedItem("Courier New");
            lineSpacingSpinner.setValue(1.0);

            fontSize = 12;
            alignment = "LEFT";
            currentFontStyle = "Courier New";
            lineSpacing = 1.0f;
            isok = false;

            // Clear logo
            removeLogo();

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

            if (content.trim().isEmpty() && logoImage == null) {
                JOptionPane.showMessageDialog(this,
                        "No content to print. Please enter some text or add a logo first.",
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
            job.setPrintable(new ReceiptPrintable(content, fontSize, alignment, logoImage,
                    (Integer) logoWidthSpinner.getValue(),
                    (String) logoAlignmentCombo.getSelectedItem()));

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

    public String getLogoPath() {
        return logoPath;
    }

    public Integer getLogoWidth() {
        return (Integer) logoWidthSpinner.getValue();
    }

    public String getLogoAlignment() {
        return (String) logoAlignmentCombo.getSelectedItem();
    }

    public void setReceiptContent(String content) {
        receiptTextPane.setText("");
        StyledDocument doc = receiptTextPane.getStyledDocument();

        SimpleAttributeSet defaultAttrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(defaultAttrs, "Courier New");
        StyleConstants.setFontSize(defaultAttrs, 14);
        StyleConstants.setBold(defaultAttrs, false);

        Pattern pattern = Pattern.compile("(\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=(\\d+)\\]|\\[/SIZE\\]|\\[FONT=([^\\]]+)\\]|\\[/FONT\\])");
        Matcher matcher = pattern.matcher(content);

        int lastIndex = 0;
        boolean currentBold = false;
        int currentSize = 14;
        String currentFont = "Courier New";

        try {
            while (matcher.find()) {
                if (matcher.start() > lastIndex) {
                    String text = content.substring(lastIndex, matcher.start());
                    SimpleAttributeSet attrs = new SimpleAttributeSet(defaultAttrs);
                    StyleConstants.setBold(attrs, currentBold);
                    StyleConstants.setFontSize(attrs, currentSize);
                    StyleConstants.setFontFamily(attrs, currentFont);
                    doc.insertString(doc.getLength(), text, attrs);
                }

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
            receiptTextPane.setText(content.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", ""));
        }

        receiptTextPane.setCaretPosition(0);
        updatePreview();
    }

    public void setLogoFromPath(String path, int width, String alignment) {
        if (path == null || path.isEmpty()) {
            return;
        }

        try {
            File logoFile = new File(path);
            if (logoFile.exists()) {
                logoImage = ImageIO.read(logoFile);
                logoPath = path;

                logoPreviewLabel.setText(logoFile.getName());
                logoPreviewLabel.setForeground(new Color(46, 204, 113));

                removeLogoButton.setEnabled(true);
                logoWidthSpinner.setEnabled(true);
                logoWidthSpinner.setValue(width);
                logoAlignmentCombo.setEnabled(true);
                logoAlignmentCombo.setSelectedItem(alignment);

                updatePreview();
            }
        } catch (Exception e) {
            System.err.println("Failed to load logo from path: " + e.getMessage());
        }
    }

    private class ReceiptPrintable implements Printable {

        private String content;
        private int fontSize;
        private String alignment;
        private float lineSpacing;
        private final int TSP100_PRINT_WIDTH = 42;
        private final int THERMAL_PRINT_FONT_SIZE;
        private BufferedImage logo;
        private int logoWidth;
        private String logoAlignment;

        public ReceiptPrintable(String content, int fontSize, String alignment,
                BufferedImage logo, int logoWidth, String logoAlignment) {
            this.content = content;
            this.fontSize = fontSize;
            this.alignment = alignment;
            this.lineSpacing = MainFrame.this.lineSpacing;
            this.THERMAL_PRINT_FONT_SIZE = (Integer) fontSizeSpinner.getValue();
            this.logo = logo;
            this.logoWidth = logoWidth;
            this.logoAlignment = logoAlignment;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            Font baseNormalFont = new Font("Courier New", Font.PLAIN, THERMAL_PRINT_FONT_SIZE);
            Font baseBoldFont = new Font("Courier New", Font.BOLD, THERMAL_PRINT_FONT_SIZE);

            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics(baseNormalFont);
            int baseLineHeight = fm.getHeight();
            int adjustedLineHeight = (int) (baseLineHeight * lineSpacing);
            double availableWidth = pf.getImageableWidth();

            int y = adjustedLineHeight;

            // Print logo if available
            if (logo != null) {
                int printLogoHeight = (int) (logo.getHeight() * ((double) logoWidth / logo.getWidth()));

                int logoX = 0;
                if ("CENTER".equals(logoAlignment)) {
                    logoX = (int) ((availableWidth - logoWidth) / 2);
                } else if ("RIGHT".equals(logoAlignment)) {
                    logoX = (int) (availableWidth - logoWidth);
                }

                g2d.drawImage(logo, logoX, y, logoWidth, printLogoHeight, null);
                y += printLogoHeight + adjustedLineHeight;
            }

            String formattedContent = formatForTSP100Print(content, TSP100_PRINT_WIDTH);
            String[] lines = formattedContent.split("\n");

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

            if (isok && allCurrentFontFamily != null && !allCurrentFontFamily.equals("Courier New")) {
                currentFontFamily = allCurrentFontFamily;
            }

            Pattern printPattern = Pattern.compile("(\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=(\\d+)\\]|\\[/SIZE\\]|\\[FONT=([^\\]]+)\\]|\\[/FONT\\])");
            Matcher printMatcher = printPattern.matcher(line);

            int lastIndex = 0;

            while (printMatcher.find()) {
                if (printMatcher.start() > lastIndex) {
                    String textToDraw = line.substring(lastIndex, printMatcher.start());
                    if (!textToDraw.isEmpty()) {
                        Font fontToUse = createPrintFont(currentFontFamily, currentBold, currentFontSize);
                        g2d.setFont(fontToUse);

                        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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
                    currentFontFamily = isok && allCurrentFontFamily != null ? allCurrentFontFamily : "Courier New";
                }

                lastIndex = printMatcher.end();
            }

            if (lastIndex < line.length()) {
                String textToDraw = line.substring(lastIndex);
                if (!textToDraw.isEmpty()) {
                    Font fontToUse = createPrintFont(currentFontFamily, currentBold, currentFontSize);
                    g2d.setFont(fontToUse);

                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    g2d.drawString(textToDraw, currentX, y);
                }
            }
        }

        private Font createPrintFont(String fontFamily, boolean bold, int fontSize) {
            int fontStyle = bold ? Font.BOLD : Font.PLAIN;

            try {
                String validatedFont = validateAndGetFont(fontFamily);
                Font font = new Font(validatedFont, fontStyle, fontSize);

                if (!font.getFamily().equalsIgnoreCase("Dialog")) {
                    return font;
                } else {
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

            for (String availableFont : availableFonts) {
                if (availableFont.equalsIgnoreCase(requestedFont)) {
                    Font testFont = new Font(availableFont, Font.PLAIN, 12);
                    if (!testFont.getFamily().equalsIgnoreCase("Dialog")) {
                        return availableFont;
                    }
                }
            }

            for (String availableFont : availableFonts) {
                if (availableFont.toLowerCase().contains(requestedFont.toLowerCase())
                        || requestedFont.toLowerCase().contains(availableFont.toLowerCase())) {
                    Font testFont = new Font(availableFont, Font.PLAIN, 12);
                    if (!testFont.getFamily().equalsIgnoreCase("Dialog")) {
                        System.out.println("Using closest match: " + availableFont + " for requested: " + requestedFont);
                        return availableFont;
                    }
                }
            }

            String lowerFont = requestedFont.toLowerCase();
            if (lowerFont.contains("arial") || lowerFont.contains("helvetica")) {
                return "Arial";
            } else if (lowerFont.contains("times")) {
                return "Times New Roman";
            } else if (lowerFont.contains("courier")) {
                return "Courier New";
            } else if (lowerFont.contains("verdana")) {
                return "Verdana";
            } else if (lowerFont.contains("tahoma")) {
                return "Tahoma";
            }

            System.out.println("Font '" + requestedFont + "' not found or cannot render, using Arial");
            return "Arial";
        }

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
            int totalPadding = width - cleanTextLength;
            int leftPadding = totalPadding / 2;
            int rightPadding = totalPadding - leftPadding;
            return " ".repeat(Math.max(0, leftPadding)) + text + " ".repeat(Math.max(0, rightPadding));
        }

        private String rightAlignTextForPrint(String text, int width) {
            int cleanTextLength = getVisibleTextLengthForPrint(text);
            if (cleanTextLength >= width) {
                return processLineLength(text, width);
            }
            int leftPadding = width - cleanTextLength;
            return " ".repeat(Math.max(0, leftPadding)) + text;
        }

        private int getVisibleTextLengthForPrint(String text) {
            return text.replaceAll("\\[BOLD\\]|\\[/BOLD\\]|\\[SIZE=\\d+\\]|\\[/SIZE\\]|\\[FONT=[^\\]]+\\]|\\[/FONT\\]", "").length();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}

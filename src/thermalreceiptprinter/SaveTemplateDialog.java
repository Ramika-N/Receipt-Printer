package thermalreceiptprinter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SaveTemplateDialog extends JDialog {

    private JTextField templateNameField;
    private JTextArea descriptionArea;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean confirmed = false;

    public SaveTemplateDialog(Dialog parent) {
        super(parent, "Save Template", true);
        initComponents();
        setupEventListeners();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = createFormPanel();

        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> templateNameField.requestFocusInWindow());
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBorder(new TitledBorder("Template Name"));

        templateNameField = new JTextField();
        templateNameField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel nameHelpLabel = new JLabel("<html><small>Enter a unique name for this template</small></html>");
        nameHelpLabel.setForeground(Color.GRAY);

        namePanel.add(templateNameField, BorderLayout.CENTER);
        namePanel.add(nameHelpLabel, BorderLayout.SOUTH);

        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBorder(new TitledBorder("Description (Optional)"));

        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JLabel descHelpLabel = new JLabel("<html><small>Briefly describe what this template is for</small></html>");
        descHelpLabel.setForeground(Color.GRAY);

        descPanel.add(descScroll, BorderLayout.CENTER);
        descPanel.add(descHelpLabel, BorderLayout.SOUTH);

        panel.add(namePanel, BorderLayout.NORTH);
        panel.add(descPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Template");
        cancelButton = new JButton("Cancel");

        saveButton.setBackground(new Color(46, 204, 113));
//        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(120, 30));

        cancelButton.setPreferredSize(new Dimension(80, 30));

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private void setupEventListeners() {

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTemplate();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        templateNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTemplate();
            }
        });

        templateNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateInput();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateInput();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateInput();
            }
        });

        validateInput();
    }

    private void validateInput() {
        String name = templateNameField.getText().trim();
        boolean isValid = !name.isEmpty() && name.length() >= 2;

        saveButton.setEnabled(isValid);

        if (name.isEmpty()) {
            templateNameField.setBackground(Color.WHITE);
        } else if (name.length() < 2) {
            templateNameField.setBackground(new Color(255, 240, 240));
        } else {
            templateNameField.setBackground(new Color(240, 255, 240));
        }
    }

    private void saveTemplate() {
        String name = templateNameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a template name.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
            templateNameField.requestFocusInWindow();
            return;
        }

        if (name.length() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Template name must be at least 2 characters long.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
            templateNameField.requestFocusInWindow();
            return;
        }

        if (!name.matches("^[a-zA-Z0-9\\s\\-_\\.]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Template name can only contain letters, numbers, spaces, hyphens, underscores, and periods.",
                    "Invalid Characters",
                    JOptionPane.WARNING_MESSAGE);
            templateNameField.requestFocusInWindow();
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTemplateName() {
        return templateNameField.getText().trim();
    }

    public String getDescription() {
        String desc = descriptionArea.getText().trim();
        return desc.isEmpty() ? "Custom receipt template" : desc;
    }
}

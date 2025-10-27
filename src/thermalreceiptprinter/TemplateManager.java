package thermalreceiptprinter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;

public class TemplateManager {

    private static final String TEMPLATES_DIR = "templates";
    private static final String TEMPLATE_EXTENSION = ".template";
    private static final String TEMPLATE_INFO_EXTENSION = ".info";

    public TemplateManager() {
        ensureTemplatesDirectory();
    }

    private void ensureTemplatesDirectory() {
        try {
            Path templatesPath = Paths.get(TEMPLATES_DIR);
            if (!Files.exists(templatesPath)) {
                Files.createDirectories(templatesPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create templates directory: " + e.getMessage());
        }
    }

    public boolean saveTemplate(String templateName, String content, String description) {
        if (templateName == null || templateName.trim().isEmpty()) {
            return false;
        }

        try {

            String fileName = sanitizeFileName(templateName);

            Path templatePath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_EXTENSION);
            Files.write(templatePath, content.getBytes("UTF-8"));

            TemplateInfo info = new TemplateInfo(templateName, description, new Date());
            saveTemplateInfo(fileName, info);

            return true;
        } catch (IOException e) {
            System.err.println("Failed to save template: " + e.getMessage());
            return false;
        }
    }

    public String loadTemplate(String templateName) {
        try {
            String fileName = sanitizeFileName(templateName);
            Path templatePath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_EXTENSION);

            if (!Files.exists(templatePath)) {
                return null;
            }

            return new String(Files.readAllBytes(templatePath), "UTF-8");
        } catch (IOException e) {
            System.err.println("Failed to load template: " + e.getMessage());
            return null;
        }
    }

    public TemplateInfo getTemplateInfo(String templateName) {
        try {
            String fileName = sanitizeFileName(templateName);
            return loadTemplateInfo(fileName);
        } catch (Exception e) {
            System.err.println("Failed to load template info: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteTemplate(String templateName) {
        try {
            String fileName = sanitizeFileName(templateName);

            Path templatePath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_EXTENSION);
            boolean templateDeleted = Files.deleteIfExists(templatePath);

            Path infoPath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_INFO_EXTENSION);
            Files.deleteIfExists(infoPath);

            return templateDeleted;
        } catch (IOException e) {
            System.err.println("Failed to delete template: " + e.getMessage());
            return false;
        }
    }

    public List<String> listTemplates() {
        List<String> templates = new ArrayList<>();

        try {
            Path templatesPath = Paths.get(TEMPLATES_DIR);
            if (!Files.exists(templatesPath)) {
                return templates;
            }

            Files.list(templatesPath)
                    .filter(path -> path.toString().endsWith(TEMPLATE_EXTENSION))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String templateName = fileName.substring(0, fileName.length() - TEMPLATE_EXTENSION.length());

                        try {
                            TemplateInfo info = loadTemplateInfo(templateName);
                            if (info != null) {
                                templates.add(info.getDisplayName());
                            } else {
                                templates.add(templateName);
                            }
                        } catch (Exception e) {
                            templates.add(templateName);
                        }
                    });

            Collections.sort(templates);
        } catch (IOException e) {
            System.err.println("Failed to list templates: " + e.getMessage());
        }

        return templates;
    }

    public boolean exportTemplate(String templateName, File exportFile) {
        try {
            String content = loadTemplate(templateName);
            if (content == null) {
                return false;
            }

            TemplateInfo info = getTemplateInfo(templateName);
            StringBuilder exportData = new StringBuilder();
            exportData.append("# Template Export\n");
            exportData.append("# Name: ").append(templateName).append("\n");
            exportData.append("# Description: ").append(info != null ? info.getDescription() : "").append("\n");
            exportData.append("# Created: ").append(info != null ? info.getCreatedDate() : new Date()).append("\n");
            exportData.append("# ---- TEMPLATE CONTENT BELOW ----\n");
            exportData.append(content);

            Files.write(exportFile.toPath(), exportData.toString().getBytes("UTF-8"));
            return true;
        } catch (IOException e) {
            System.err.println("Failed to export template: " + e.getMessage());
            return false;
        }
    }

    public boolean importTemplate(File importFile, String newTemplateName) {
        try {
            String content = new String(Files.readAllBytes(importFile.toPath()), "UTF-8");

            String templateContent = content;
            if (content.contains("# ---- TEMPLATE CONTENT BELOW ----\n")) {
                String[] parts = content.split("# ---- TEMPLATE CONTENT BELOW ----\n", 2);
                if (parts.length > 1) {
                    templateContent = parts[1];
                }
            }

            String description = "Imported template";
            if (content.startsWith("# Template Export")) {
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if (line.startsWith("# Description: ")) {
                        description = line.substring("# Description: ".length());
                        break;
                    }
                }
            }

            return saveTemplate(newTemplateName, templateContent, description);
        } catch (IOException e) {
            System.err.println("Failed to import template: " + e.getMessage());
            return false;
        }
    }

    public boolean templateExists(String templateName) {
        String fileName = sanitizeFileName(templateName);
        Path templatePath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_EXTENSION);
        return Files.exists(templatePath);
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void saveTemplateInfo(String fileName, TemplateInfo info) {
        try {
            Path infoPath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_INFO_EXTENSION);
            Properties props = new Properties();
            props.setProperty("displayName", info.getDisplayName());
            props.setProperty("description", info.getDescription());
            props.setProperty("createdDate", String.valueOf(info.getCreatedDate().getTime()));

            try (FileOutputStream fos = new FileOutputStream(infoPath.toFile())) {
                props.store(fos, "Template Information");
            }
        } catch (IOException e) {
            System.err.println("Failed to save template info: " + e.getMessage());
        }
    }

    private TemplateInfo loadTemplateInfo(String fileName) {
        try {
            Path infoPath = Paths.get(TEMPLATES_DIR, fileName + TEMPLATE_INFO_EXTENSION);
            if (!Files.exists(infoPath)) {
                return null;
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(infoPath.toFile())) {
                props.load(fis);
            }

            String displayName = props.getProperty("displayName", fileName);
            String description = props.getProperty("description", "");
            long timestamp = Long.parseLong(props.getProperty("createdDate", "0"));
            Date createdDate = timestamp > 0 ? new Date(timestamp) : new Date();

            return new TemplateInfo(displayName, description, createdDate);
        } catch (Exception e) {
            System.err.println("Failed to load template info: " + e.getMessage());
            return null;
        }
    }

    public static class TemplateInfo {

        private final String displayName;
        private final String description;
        private final Date createdDate;

        public TemplateInfo(String displayName, String description, Date createdDate) {
            this.displayName = displayName;
            this.description = description;
            this.createdDate = createdDate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public Date getCreatedDate() {
            return createdDate;
        }

        public String getFormattedDate() {
            return new SimpleDateFormat("MMM dd, yyyy HH:mm").format(createdDate);
        }
    }
}

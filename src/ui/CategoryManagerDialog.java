package ui;

import model.Category;
import service.CategoryService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Small modal screen for the categories belonging to the demo user. */
public class CategoryManagerDialog extends JDialog {
    private static final int DEMO_USER_ID = 1;
    private final CategoryService categoryService = new CategoryService();
    private final DefaultListModel<Category> categoryModel = new DefaultListModel<>();
    private final JList<Category> categoryList = new JList<>(categoryModel);
    private final Runnable afterChange;

    public CategoryManagerDialog(JFrame parent, Runnable afterChange) {
        super(parent, "Manage categories", true);
        this.afterChange = afterChange;
        setLayout(new BorderLayout(10, 10));
        UIStyles.styleDialog(this);
        setSize(560, 430);
        setMinimumSize(new Dimension(560, 430));
        setLocationRelativeTo(parent);
        UIStyles.styleList(categoryList);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        UIStyles.styleScrollPane(categoryScrollPane);
        categoryScrollPane.setPreferredSize(new Dimension(520, 270));
        add(categoryScrollPane, BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);
        reload();
    }

    private JPanel buildControls() {
        JTextField nameField = new JTextField(12);
        JTextField colorField = new JTextField("#888888", 8);
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete selected");
        UIStyles.styleInput(nameField);
        UIStyles.styleInput(colorField);
        UIStyles.styleButton(addButton, true);
        UIStyles.styleButton(deleteButton, UIStyles.ButtonTone.DANGER);
        addButton.addActionListener(e -> addCategory(nameField, colorField));
        deleteButton.addActionListener(e -> deleteSelected());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(UIStyles.APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        panel.add(styledLabel("Name"));
        panel.add(nameField);
        panel.add(styledLabel("Color"));
        panel.add(colorField);
        panel.add(addButton);
        panel.add(deleteButton);
        return panel;
    }

    private void addCategory(JTextField nameField, JTextField colorField) {
        try {
            categoryService.addCategory(nameField.getText().trim(), colorField.getText().trim(), DEMO_USER_ID);
            nameField.setText("");
            reload();
            afterChange.run();
        } catch (IllegalArgumentException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void deleteSelected() {
        Category category = categoryList.getSelectedValue();
        if (category == null) {
            showError("Select a category first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete '" + category.getName() + "'? Existing expenses will become uncategorized.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            categoryService.deleteCategory(category.getId());
            reload();
            afterChange.run();
        } catch (IllegalArgumentException | SQLException e) {
            showError(e.getMessage());
        }
    }

    private void reload() {
        try {
            categoryModel.clear();
            for (Category category : categoryService.getAll(DEMO_USER_ID)) categoryModel.addElement(category);
        } catch (SQLException e) {
            showError("Categories could not be loaded.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Expense Tracker", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        UIStyles.styleLabel(label);
        return label;
    }
}

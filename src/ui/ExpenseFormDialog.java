package ui;

import model.Category;
import model.Expense;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Branded add/edit form that keeps the expense workflow separate from its visual shell. */
public final class ExpenseFormDialog extends JDialog {
    private final JFormattedTextField amountField;
    private final JTextField descriptionField;
    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final DatePicker datePicker;
    private boolean confirmed;

    public ExpenseFormDialog(JFrame parent, Expense existing, List<Category> categories) {
        super(parent, existing == null ? "Add expense" : "Edit expense", true);
        amountField = createAmountField(existing);
        descriptionField = new JTextField(existing == null ? "" : existing.getDescription());
        datePicker = new DatePicker(existing == null ? LocalDate.now() : existing.getExpenseDate());

        for (Category category : categories) categoryBox.addItem(category);
        if (existing != null) selectCategory(existing.getCategory_id());

        configureWindow(existing == null ? "Add expense" : "Edit expense");
        setContentPane(buildContent());
        pack();
        setMinimumSize(new Dimension(430, getHeight()));
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public double getAmount() throws java.text.ParseException {
        amountField.commitEdit();
        Number amount = (Number) amountField.getValue();
        if (amount == null) throw new NumberFormatException("Amount is required.");
        return amount.doubleValue();
    }

    public String getDescription() {
        return descriptionField.getText().trim();
    }

    public Category getSelectedCategory() {
        return (Category) categoryBox.getSelectedItem();
    }

    public LocalDate getSelectedDate() {
        return datePicker.getSelectedDate();
    }

    private void configureWindow(String title) {
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setBorder(new LineBorder(UIStyles.BORDER));
        setTitle(title);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UIStyles.APP_BACKGROUND);
        content.add(buildTitleBar(), BorderLayout.NORTH);
        content.add(buildForm(), BorderLayout.CENTER);
        content.add(buildActions(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UIStyles.PRIMARY);
        titleBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 10));
        JLabel title = new JLabel(getTitle());
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 16f));
        UIStyles.styleLabel(title);
        JButton closeButton = new JButton("×");
        closeButton.setForeground(UIStyles.TEXT);
        closeButton.setBackground(UIStyles.PRIMARY);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(closeButton.getFont().deriveFont(Font.PLAIN, 22f));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(event -> dispose());
        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);
        return titleBar;
    }

    private JPanel buildForm() {
        UIStyles.styleInput(descriptionField);
        UIStyles.styleInput(categoryBox);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIStyles.APP_BACKGROUND);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 12, 12);
        constraints.anchor = GridBagConstraints.WEST;

        addRow(form, constraints, 0, "Amount *", amountField);
        addRow(form, constraints, 1, "Description", descriptionField);
        addRow(form, constraints, 2, "Category *", categoryBox);
        addRow(form, constraints, 3, "Date *", datePicker);
        return form;
    }

    private void addRow(JPanel form, GridBagConstraints constraints, int row, String labelText, JComponent input) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
        UIStyles.styleLabel(label);
        form.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 12, 0);
        input.setPreferredSize(new Dimension(210, 34));
        form.add(input, constraints);
        constraints.insets = new Insets(0, 0, 12, 12);
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setBackground(UIStyles.APP_BACKGROUND);
        actions.setBorder(BorderFactory.createEmptyBorder(4, 20, 20, 20));
        JButton saveButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        UIStyles.styleButton(saveButton, UIStyles.ButtonTone.PRIMARY);
        UIStyles.styleButton(cancelButton, UIStyles.ButtonTone.SECONDARY);
        saveButton.setPreferredSize(new Dimension(116, 38));
        cancelButton.setPreferredSize(new Dimension(116, 38));
        saveButton.addActionListener(event -> {
            confirmed = true;
            dispose();
        });
        cancelButton.addActionListener(event -> dispose());
        getRootPane().setDefaultButton(saveButton);
        actions.add(saveButton);
        actions.add(cancelButton);
        return actions;
    }

    private JFormattedTextField createAmountField(Expense existing) {
        NumberFormat amountFormat = NumberFormat.getNumberInstance(Locale.US);
        amountFormat.setGroupingUsed(false);
        amountFormat.setMaximumFractionDigits(2);
        javax.swing.text.NumberFormatter formatter = new javax.swing.text.NumberFormatter(amountFormat);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0d);
        formatter.setCommitsOnValidEdit(true);

        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setValue(existing == null ? null : existing.getAmount());
        field.setToolTipText("Enter an amount using digits; decimals are optional.");
        UIStyles.styleInput(field);
        return field;
    }

    private void selectCategory(int categoryId) {
        for (int index = 0; index < categoryBox.getItemCount(); index++) {
            if (categoryBox.getItemAt(index).getId() == categoryId) {
                categoryBox.setSelectedIndex(index);
                return;
            }
        }
    }
}

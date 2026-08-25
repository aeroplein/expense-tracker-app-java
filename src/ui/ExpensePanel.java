package ui;

import model.Category;
import model.Expense;
import service.CategoryService;
import service.ExpenseService;
import service.ReportService;
import ui.actions.ExpensePanelActions;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** The screen owns widgets; services own business rules and database calls. */
public class ExpensePanel extends JPanel implements ExpensePanelActions {
    private static final int DEMO_USER_ID = 1;

    private final ExpenseService expenseService = new ExpenseService();
    private final CategoryService categoryService = new CategoryService();
    private final ReportService reportService = new ReportService();
    private final MainFrame parent;

    private JTable table;
    private DefaultTableModel tableModel;
    private JRadioButton radioAll;
    private JRadioButton radioDaily;
    private JRadioButton radioWeekly;
    private JRadioButton radioMonthly;
    private JComboBox<Category> categoryFilter;
    private JLabel totalLabel;
    private List<Expense> activeExpenses = new ArrayList<>();

    public ExpensePanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        add(buildFilterPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        loadCategories();
        loadExpenses();
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        panel.setPreferredSize(new Dimension(180, 0));

        panel.add(new JLabel("Period"));
        radioAll = createPeriodButton("All", "ALL", true);
        radioDaily = createPeriodButton("Today", "DAILY", false);
        radioWeekly = createPeriodButton("Last 7 days", "WEEKLY", false);
        radioMonthly = createPeriodButton("This month", "MONTHLY", false);
        ButtonGroup periodGroup = new ButtonGroup();
        periodGroup.add(radioAll);
        periodGroup.add(radioDaily);
        periodGroup.add(radioWeekly);
        periodGroup.add(radioMonthly);
        panel.add(radioAll);
        panel.add(radioDaily);
        panel.add(radioWeekly);
        panel.add(radioMonthly);

        panel.add(Box.createVerticalStrut(18));
        panel.add(new JLabel("Category"));
        categoryFilter = new JComboBox<>();
        categoryFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        categoryFilter.addActionListener(e -> loadExpenses());
        panel.add(categoryFilter);

        panel.add(Box.createVerticalStrut(18));
        JButton resetButton = new JButton("Clear filters");
        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetButton.addActionListener(e -> {
            radioAll.setSelected(true);
            categoryFilter.setSelectedIndex(0);
            loadExpenses();
        });
        panel.add(resetButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JRadioButton createPeriodButton(String label, String period, boolean selected) {
        JRadioButton button = new JRadioButton(label, selected);
        button.setActionCommand(period);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> loadExpenses());
        return button;
    }

    private JPanel buildTablePanel() {
        String[] columns = {"ID", "Date", "Description", "Category", "Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton addButton = new JButton("Add expense");
        JButton editButton = new JButton("Edit selected");
        JButton deleteButton = new JButton("Delete selected");
        addButton.addActionListener(e -> openAddDialog());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteSelected());
        buttons.add(addButton);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(editButton);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(deleteButton);

        totalLabel = new JLabel("Total: 0.00");
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        footer.add(totalLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void openAddDialog() {
        showExpenseDialog(null);
    }

    @Override
    public void openEditDialog() {
        int expenseId = selectedExpenseId();
        if (expenseId == -1) return;
        try {
            showExpenseDialog(expenseService.getById(expenseId));
        } catch (SQLException e) {
            showError("The selected expense could not be loaded.", e);
        }
    }

    private void showExpenseDialog(Expense existing) {
        JTextField amountField = new JTextField(existing == null ? "" : String.valueOf(existing.getAmount()));
        JTextField descriptionField = new JTextField(existing == null ? "" : existing.getDescription());
        JComboBox<Category> categoryBox = new JComboBox<>();
        for (Category category : getCategories()) categoryBox.addItem(category);
        if (existing != null) selectCategory(categoryBox, existing.getCategory_id());
        JTextField dateField = new JTextField(existing == null ? LocalDate.now().toString() : existing.getExpenseDate().toString());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Amount *")); form.add(amountField);
        form.add(new JLabel("Description")); form.add(descriptionField);
        form.add(new JLabel("Category *")); form.add(categoryBox);
        form.add(new JLabel("Date (yyyy-MM-dd) *")); form.add(dateField);

        int result = JOptionPane.showConfirmDialog(parent, form,
                existing == null ? "Add expense" : "Edit expense", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Category selectedCategory = (Category) categoryBox.getSelectedItem();
            if (selectedCategory == null) throw new IllegalArgumentException("Create a category before adding an expense.");
            Expense expense = existing == null
                    ? new Expense(Double.parseDouble(amountField.getText().trim()), descriptionField.getText().trim(),
                    selectedCategory.getId(), DEMO_USER_ID, LocalDate.parse(dateField.getText().trim()))
                    : new Expense(existing.getId(), Double.parseDouble(amountField.getText().trim()), descriptionField.getText().trim(),
                    selectedCategory.getId(), DEMO_USER_ID, LocalDate.parse(dateField.getText().trim()));
            if (existing == null) expenseService.addExpense(expense); else expenseService.updateExpense(expense);
            parent.setStatus(existing == null ? "Expense added." : "Expense updated.");
            loadExpenses();
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.", e);
        } catch (DateTimeParseException e) {
            showError("Date must use yyyy-MM-dd, for example 2026-08-25.", e);
        } catch (IllegalArgumentException | SQLException e) {
            showError(e.getMessage(), e);
        }
    }

    @Override
    public void deleteSelected() {
        int expenseId = selectedExpenseId();
        if (expenseId == -1) return;
        if (JOptionPane.showConfirmDialog(parent, "Delete this expense?", "Confirm deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            expenseService.deleteExpense(expenseId);
            parent.setStatus("Expense deleted.");
            loadExpenses();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e.getMessage(), e);
        }
    }

    @Override
    public void exportToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("expenses.csv"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
        try {
            reportService.exportToCSV(file.getAbsolutePath(), activeExpenses);
            parent.setStatus("Exported " + activeExpenses.size() + " expenses.");
        } catch (Exception e) {
            showError("CSV could not be exported.", e);
        }
    }

    @Override
    public void loadExpenses() {
        if (tableModel == null || categoryFilter == null) return;
        try {
            List<Expense> expenses = expenseService.getFiltered(DEMO_USER_ID, selectedPeriod());
            Category selectedCategory = (Category) categoryFilter.getSelectedItem();
            if (selectedCategory != null && selectedCategory.getId() != 0) {
                expenses.removeIf(expense -> expense.getCategory_id() != selectedCategory.getId());
            }
            activeExpenses = expenses;
            tableModel.setRowCount(0);
            for (Expense expense : expenses) {
                tableModel.addRow(new Object[]{expense.getId(), expense.getExpenseDate(), expense.getDescription(),
                        expense.getCategoryName() == null ? "Uncategorized" : expense.getCategoryName(),
                        String.format("%.2f", expense.getAmount())});
            }
            totalLabel.setText(String.format("Total: %.2f", expenseService.calculateTotal(expenses)));
        } catch (SQLException e) {
            showError("Expenses could not be loaded. Check the database connection.", e);
        }
    }

    private void loadCategories() {
        if (categoryFilter == null) return;
        try {
            categoryFilter.removeAllItems();
            categoryFilter.addItem(new Category(0, "All categories", "#888888", DEMO_USER_ID));
            for (Category category : categoryService.getAll(DEMO_USER_ID)) categoryFilter.addItem(category);
        } catch (SQLException e) {
            showError("Categories could not be loaded.", e);
        }
    }

    public void refreshCategories() {
        loadCategories();
        loadExpenses();
    }

    private List<Category> getCategories() {
        try {
            return categoryService.getAll(DEMO_USER_ID);
        } catch (SQLException e) {
            showError("Categories could not be loaded.", e);
            return List.of();
        }
    }

    private void selectCategory(JComboBox<Category> categoryBox, int categoryId) {
        for (int index = 0; index < categoryBox.getItemCount(); index++) {
            if (categoryBox.getItemAt(index).getId() == categoryId) {
                categoryBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private int selectedExpenseId() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(parent, "Select an expense first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    private String selectedPeriod() {
        if (radioDaily.isSelected()) return "DAILY";
        if (radioWeekly.isSelected()) return "WEEKLY";
        if (radioMonthly.isSelected()) return "MONTHLY";
        return "ALL";
    }

    private void showError(String message, Exception exception) {
        parent.setStatus("Operation failed.");
        JOptionPane.showMessageDialog(parent, message, "Expense Tracker", JOptionPane.ERROR_MESSAGE);
        exception.printStackTrace();
    }
}

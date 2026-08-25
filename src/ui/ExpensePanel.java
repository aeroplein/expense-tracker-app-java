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
        setBackground(UIStyles.SOFT_CREAM);
        add(buildFilterPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        loadCategories();
        loadExpenses();
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIStyles.WARM_FOG);
        panel.setForeground(UIStyles.TEXT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UIStyles.BORDER),
                BorderFactory.createEmptyBorder(26, 24, 24, 18)));
        panel.setPreferredSize(new Dimension(230, 0));

        JLabel periodLabel = new JLabel("Period");
        periodLabel.setFont(periodLabel.getFont().deriveFont(Font.PLAIN, 15f));
        periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIStyles.styleLabel(periodLabel);
        panel.add(periodLabel);
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

        panel.add(Box.createVerticalStrut(28));
        JLabel categoryLabel = new JLabel("Category");
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.PLAIN, 15f));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIStyles.styleLabel(categoryLabel);
        panel.add(categoryLabel);
        categoryFilter = new JComboBox<>();
        UIStyles.styleInput(categoryFilter);
        categoryFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        categoryFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryFilter.addActionListener(e -> loadExpenses());
        panel.add(categoryFilter);

        panel.add(Box.createVerticalStrut(24));
        JButton resetButton = new JButton("Clear filters");
        UIStyles.styleButton(resetButton, false);
        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetButton.addActionListener(e -> {
            radioAll.setSelected(true);
            categoryFilter.setSelectedIndex(0);
            loadExpenses();
        });
        panel.add(resetButton);
        panel.add(Box.createVerticalStrut(12));
        JButton addCategoryButton = new JButton("Add category");
        UIStyles.styleButton(addCategoryButton, false);
        addCategoryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addCategoryButton.addActionListener(e -> parent.openCategoryManager());
        panel.add(addCategoryButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JRadioButton createPeriodButton(String label, String period, boolean selected) {
        JRadioButton button = new JRadioButton(label, selected);
        UIStyles.styleRadioButton(button);
        button.setActionCommand(period);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
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
        UIStyles.styleTable(table);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setBackground(UIStyles.SOFT_CREAM);
        JButton addButton = new JButton("Add expense");
        JButton editButton = new JButton("Edit selected");
        JButton deleteButton = new JButton("Delete selected");
        UIStyles.styleButton(addButton, true);
        UIStyles.styleButton(editButton, false);
        UIStyles.styleButton(deleteButton, UIStyles.ButtonTone.DANGER);
        addButton.addActionListener(e -> openAddDialog());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteSelected());
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        totalLabel = new JLabel("Total: 0.00");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.PLAIN, 14f));
        UIStyles.styleLabel(totalLabel);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIStyles.SOFT_CREAM);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        footer.add(totalLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIStyles.SOFT_CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(buttons, BorderLayout.NORTH);
        JScrollPane tableScrollPane = new JScrollPane(table);
        UIStyles.styleScrollPane(tableScrollPane);
        panel.add(tableScrollPane, BorderLayout.CENTER);
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
        ExpenseFormDialog dialog = new ExpenseFormDialog(parent, existing, getCategories());
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return;

        try {
            Category selectedCategory = dialog.getSelectedCategory();
            if (selectedCategory == null) throw new IllegalArgumentException("Create a category before adding an expense.");
            double amount = dialog.getAmount();
            Expense expense = existing == null
                    ? new Expense(amount, dialog.getDescription(), selectedCategory.getId(), DEMO_USER_ID,
                    dialog.getSelectedDate())
                    : new Expense(existing.getId(), amount, dialog.getDescription(),
                    selectedCategory.getId(), DEMO_USER_ID, dialog.getSelectedDate());
            if (existing == null) expenseService.addExpense(expense); else expenseService.updateExpense(expense);
            parent.setStatus(existing == null ? "Expense added." : "Expense updated.");
            loadExpenses();
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.", e);
        } catch (java.text.ParseException e) {
            showError("Amount must be a valid number.", e);
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
